package com.hmall.search.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.utils.CollUtils;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.mapper.SearchMapper;
import com.hmall.search.query.ItemPageQuery;
import com.hmall.search.service.ISearchService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author axno
 * @date 2024/11/29 21:56
 * @DESCRIPTION
 */
@Service
public class SearchServiceImpl extends ServiceImpl<SearchMapper, Item> implements ISearchService {

    private RestHighLevelClient restHighLevelClient;
    @BeforeEach
    void setUp() {
        restHighLevelClient = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.200.130:9200")
        ));
    }
    //结束方法
    @AfterEach
    void tearDown() throws IOException {
        if (restHighLevelClient != null){
            restHighLevelClient.close();
        }
    }

    @Override
    public PageDTO<ItemDoc> EsSearch(ItemPageQuery query) {
        PageDTO<ItemDoc> result = new PageDTO<>();
        //构建请求
        SearchRequest searchRequest = new SearchRequest("items");
        //构建查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        searchRequest.source().trackTotalHits(true);
        //查询关键字key
        if (query.getKey() != null && !"".equals(query.getKey())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("name",query.getKey()));
        }
        //高亮
        searchRequest.source().highlighter(
                SearchSourceBuilder.highlight()
                        .field("name")
                        .preTags("<em>")
                        .postTags("</em>")
        );
        //分页
        searchRequest.source().from(query.from()).size(query.getPageSize());
        //排序
        if (query.getSortBy()!=null && !"".equals(query.getSortBy())){
            searchRequest.source().sort(query.getSortBy(),query.getIsAsc()? SortOrder.ASC:SortOrder.DESC);
        }else{
            searchRequest.source().sort("updateTime",query.getIsAsc()?SortOrder.ASC:SortOrder.DESC);
        }
        //分类
        if (query.getCategory() != null && !"".equals(query.getCategory())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category",query.getCategory()));
        }
        //品牌
        if (query.getBrand() != null && !"".equals(query.getBrand())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand",query.getBrand()));
        }
        //价格
        if (query.getMinPrice()!=null && query.getMaxPrice()!=null){
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price")
                    .gte(query.getMinPrice()).lte(query.getMaxPrice()));
        }
        //排名 广告优先
        searchRequest.source().query(QueryBuilders.functionScoreQuery(boolQueryBuilder,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery("isAD", true),
                                ScoreFunctionBuilders.weightFactorFunction(100))
                }).boostMode(CombineFunction.MULTIPLY));

        //解析结果
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //解析
            result.setTotal(search.getHits().getTotalHits().value);
            result.setPages((search.getHits().getTotalHits().value) % (query.getPageSize()) ==0 ?
                    search.getHits().getTotalHits().value/query.getPageSize() :
                    search.getHits().getTotalHits().value/query.getPageSize()+1 );
            SearchHit[] hits = search.getHits().getHits();
            List<ItemDoc> list=new ArrayList<>();
            for (SearchHit hit : hits) {
                ItemDoc itemDoc = JSONUtil.toBean(hit.getSourceAsString(), ItemDoc.class);
                Map<String, HighlightField> hfs = hit.getHighlightFields();
                if (CollUtils.isNotEmpty(hfs)) {
                    // 5.1.有高亮结果，获取name的高亮结果
                    HighlightField hf = hfs.get("name");
                    if (hf != null) {
                        // 5.2.获取第一个高亮结果片段，就是商品名称的高亮值
                        String hfName = hf.getFragments()[0].string();
                        itemDoc.setName(hfName);
                    }
                }
                list.add(itemDoc);
            }
            result.setList(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                restHighLevelClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        return null;
    }


}
