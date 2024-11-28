package com.hmall.item.es;

import cn.hutool.json.JSONUtil;
import com.hmall.item.domain.po.ItemDoc;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author axno
 * @date 2024/11/18 22:19
 * @DESCRIPTION
 * --add-opens java.base/java.lang=ALL-UNNAMED
 */
//@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticSearchTest {

    private RestHighLevelClient client;

    @Test
    void testMatchAll() throws IOException {
        //1.创建request对象
        SearchRequest request = new SearchRequest("items");
        //2.配置request参数
        request.source()
                .query(QueryBuilders.matchAllQuery());
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println("response = "+response);
        //4.解析结果
        SearchHits searchHits = response.getHits();
        //获取记录总条数
        long value = searchHits.getTotalHits().value;
        System.out.println("total value = "+ value);
        //遍历获取每个source
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            //System.out.println("hits = :" + json);
            ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
            System.out.println("doc = " + doc);
        }
    }

    @Test
    void testComplexSearch() throws IOException {
        //1.创建request对象
        SearchRequest request = new SearchRequest("items");
        //2.设置参数
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.matchQuery("name","脱脂牛奶"));
        boolQuery.filter(QueryBuilders.termQuery("brand","德亚"));
        boolQuery.filter(QueryBuilders.rangeQuery("price").lt(30000));
        request.source().query(boolQuery);
        /*
        request.source().query(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("name","脱脂牛奶"))
                        .filter(QueryBuilders.termQuery("brand","德亚"))
                        .filter(QueryBuilders.rangeQuery("price").lt(30000))
        );
        */
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println("response = " + response);
        //4解析
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            ItemDoc itemDoc = JSONUtil.toBean(json, ItemDoc.class);
            System.out.println("itemDoc = " + itemDoc);
        }

    }


    @Test
    void testSortAndPageSearch() throws IOException {
        //0模拟前端传的分页参数
        int pageNo = 2,pageSize = 5;

        //1.创建request对象
        SearchRequest request = new SearchRequest("items");
        //2.设置参数  前端传的是页码和每页大小
        //查询
        request.source().query(QueryBuilders.matchAllQuery());
        //分页
        request.source().from( (pageNo-1) * pageSize ).size(pageSize);
        //排序 (按销量排) 降序  销量一样 按价格升序
        request.source()
                .sort("sold", SortOrder.DESC)
                .sort("price",SortOrder.ASC);
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println("response = " + response);
        //4解析
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            ItemDoc itemDoc = JSONUtil.toBean(json, ItemDoc.class);
            System.out.println("itemDoc = " + itemDoc);
        }
    }

    @Test
    void testHighLightenerSearch() throws IOException {
        //1.创建request对象
        SearchRequest request = new SearchRequest("items");
        //2.设置参数
        request.source().query(QueryBuilders.matchQuery("name","脱脂牛奶"));
        //高亮条件
        //TODO 注意这里的name和下面的name的区别
        request.source().highlighter(
                //new HighlightBuilder().field()
                SearchSourceBuilder.highlight().field("name")
        );
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println("response = " + response);
        //4解析
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            ItemDoc itemDoc = JSONUtil.toBean(json, ItemDoc.class);
            //System.out.println("itemDoc = " + itemDoc);
            //解析出高亮字段
            //4.1 获取高亮部分的信息
            Map<String, HighlightField> hfs = hit.getHighlightFields();
            if (hfs != null && !hfs.isEmpty()) {
                //4.2 根据高亮字段获取高亮结果
                HighlightField hl = hfs.get("name");
                //4.3 获取高亮结果，覆盖非高亮结果
                String hfName = hl.getFragments()[0].string();
                itemDoc.setName(hfName);
            }
            System.out.println("itemDoc = " + itemDoc);
        }
    }

    //聚合查询
    @Test
    void testAggregationSearch() throws IOException {
        //1.构建请求对象
        SearchRequest request = new SearchRequest("items");
        //2.设置请求参数
        request.source().size(0);
        request.source().aggregation(
                AggregationBuilders
                        .terms("brand_agg")
                        .field("brand")
                        .size(10)
        );
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.获取结果，解析结果
        Aggregations aggregations = response.getAggregations();
        Terms brandTerms = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        //遍历获取每一个bucket
        for (Terms.Bucket bucket : buckets) {
            String brand = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            System.out.println("品牌：" +brand +"的库存数量为：" + docCount);
        }
    }


    //初始化
    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.200.130:9200")
        ));
    }

    //结束方法
    @AfterEach
    void tearDown() throws IOException {
        if (client != null){
            client.close();
        }
    }
}
