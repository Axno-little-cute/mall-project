package com.hmall.search.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.api.dto.ItemDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.query.ItemPageQuery;
import com.hmall.search.service.ISearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apiguardian.api.API;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author axno
 * @date 2024/11/28 21:09
 * @DESCRIPTION
 */
@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ISearchService searchService;
    /*
    private final RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(HttpHost.create("http://192.168.200.130:9200"))
    );

    @AfterEach
    void tearDown() throws IOException {
        if (client != null){
            client.close();
        }
    }*/

    //搜索功能接口
    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<ItemDoc> search(ItemPageQuery query){
        return searchService.EsSearch(query);
    }


    //测试接口
    /*@ApiOperation("ES搜索商品测试")
    @GetMapping("/{id}")
    public ItemDTO search(@PathVariable("id") Long id) throws IOException {
        //1.创建request对象
        GetRequest request = new GetRequest("items");
        //2.构造条件
        request.id(id.toString());
        //3.发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        //4.解析结果
        String json = response.getSourceAsString();
        ItemDoc itemDoc = JSONUtil.toBean(json, ItemDoc.class);
        return BeanUtil.copyProperties(itemDoc, ItemDTO.class);
    }*/


}
