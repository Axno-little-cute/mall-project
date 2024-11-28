package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

/**
 * @author axno
 * @date 2024/11/16 16:34
 * @DESCRIPTION
 */
@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticDocumentTest {

    private RestHighLevelClient client;
    @Autowired
    private IItemService itemService;

    //文档新增API
    @Test
    void testIndexDoc() throws IOException {
        //0.1 准备文档数据
        Item item = itemService.getById(998188L);
        //0.2 把数据库数据转为文档数据
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);

        itemDoc.setPrice(66666);

        //1.准备一个request                             = item.getId.toString()
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
        //2.准备请求参数
        request.source(JSONUtil.toJsonStr(itemDoc),XContentType.JSON);
        //3.发送请求
        client.index(request,RequestOptions.DEFAULT);
    }

    //查询文档API
    @Test
    void testGetDoc() throws IOException {
        //1.准备一个request
        GetRequest request = new GetRequest("items","998188");
        //2.发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        //3.处理响应结果
        String json = response.getSourceAsString();
        //业务需要转成itemDoc
        ItemDoc itemDoc = JSONUtil.toBean(json, ItemDoc.class);
        System.out.println("doc = "+itemDoc);
    }


    //删除文档API
    // --add-opens java.base/java.lang=ALL-UNNAMED
    @Test
    void testDeleteDoc() throws IOException {
        //1.准备一个request
        DeleteRequest request = new DeleteRequest("items","998188");
        //2.发送请求
        client.delete(request,RequestOptions.DEFAULT);
    }

    //更新文档
    @Test
    void testUpdateDoc() throws IOException {
        //1.准备一个request
        UpdateRequest request = new UpdateRequest("items","998188");
        //2.准备请求参数
        request.doc(
                "price",12345
        );
        //3.发送请求
        client.update(request,RequestOptions.DEFAULT);
    }

    //批处理
    @Test
    void testBulkDoc() throws IOException {
        int pageNo = 1,pageSize=500;
        while (true){
            //0 准备文档数据
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(pageNo, pageSize));
            List<Item> records = page.getRecords();
            if (records == null || records.isEmpty()) {
                //没查到数据
                return;
            }
            //1.准备request
            BulkRequest request = new BulkRequest();
            //2.准备请求参数
            for (Item record : records) {
                request.add(new IndexRequest("items")
                        .id(record.getId().toString())
                        .source(JSONUtil.toJsonStr(BeanUtil.copyProperties(record, ItemDoc.class)),XContentType.JSON));
            }
            //3.发送请求
            client.bulk(request,RequestOptions.DEFAULT);
            //4.翻页
            pageNo++;
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
