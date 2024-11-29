package com.hmall.search.Listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.search.domain.po.ItemDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @author axno
 * @date 2024/11/28 22:08
 * @DESCRIPTION
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ItemListener {
    private final ItemClient itemClient;

    private final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
            HttpHost.create("http://192.168.200.130:9200")
    ));

    /**
     * 新增操作
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.add.queue",durable = "true"),
            exchange = @Exchange(value = "search.direct",type = ExchangeTypes.DIRECT),
            key = "item.add"
    ))
    public void listenItemIndex(Long id) throws IOException {
        ItemDTO itemDTO = itemClient.queryItemById(id);
        if (itemDTO == null) {
            return;
        }
        ItemDoc itemDoc = BeanUtil.copyProperties(itemDTO, ItemDoc.class);
        String jsonStr = JSONUtil.toJsonStr(itemDoc);
        IndexRequest request = new IndexRequest("items").id(id.toString());
        request.source(jsonStr,XContentType.JSON);
        client.index(request,RequestOptions.DEFAULT);
        log.info("mq发来商品新增的消息，往ES中添加{}",jsonStr);
    }

    /**
     * 更新商品状态操作
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.status.queue",durable = "true"),
            exchange = @Exchange(value = "search.direct",type = ExchangeTypes.DIRECT),
            key = "item.updateStatus"
    ))
    public void listenItemUpdateStatus(Long id) throws IOException {
        //通过feign获取数据库对象
        ItemDTO itemDTO = itemClient.queryItemById(id);
        if (itemDTO == null) {
            return;
        }
        //对ES索引库中对应数据进行修改
        ItemDoc itemDoc = BeanUtil.copyProperties(itemDTO, ItemDoc.class);
        String jsonStr = JSONUtil.toJsonStr(itemDoc);
        UpdateRequest request = new UpdateRequest("items",itemDoc.getId());
        request.doc(jsonStr, XContentType.JSON);
        client.update(request, RequestOptions.DEFAULT);
        log.info("mq发来商品状态update的消息，往ES中更新{}",jsonStr);
    }

    /**
     * 更新商品操作
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.update.queue",durable = "true"),
            exchange = @Exchange(value = "search.direct",type = ExchangeTypes.DIRECT),
            key = "item.update"
    ))
    public void listenItemUpdate(ItemDTO itemDTO) throws IOException {
        //通过feign获取数据库对象
        if (itemDTO == null) {
            return;
        }
        //对ES索引库中对应数据进行修改
        ItemDoc itemDoc = BeanUtil.copyProperties(itemDTO, ItemDoc.class);
        String jsonStr = JSONUtil.toJsonStr(itemDoc);
        UpdateRequest request = new UpdateRequest("items",itemDoc.getId());
        request.doc(jsonStr, XContentType.JSON);
        client.update(request, RequestOptions.DEFAULT);
        log.info("mq发来商品update的消息，往ES中更新{}",jsonStr);
    }

    /**
     * 根据id删除操作
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.delete.queue",durable = "true"),
            exchange = @Exchange(value = "search.direct",type = ExchangeTypes.DIRECT),
            key = "item.delete"
    ))
    public void listenItemDelete(Long id) throws IOException {
        DeleteRequest request = new DeleteRequest("items",id.toString());
        client.delete(request,RequestOptions.DEFAULT);
        log.info("mq发来商品delete的消息，从ES中删除{}",id);
    }

    /**
     * 批量扣减库存

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.delete.queue",durable = "true"),
            exchange = @Exchange(value = "search.direct",type = ExchangeTypes.DIRECT),
            key = "item.delete"
    ))
    public void listenItemDeductStock(List<OrderDetailDTO> items) throws IOException {
        DeleteRequest request = new DeleteRequest("items",id.toString());
        client.delete(request,RequestOptions.DEFAULT);
        log.info("mq发来商品delete的消息，从ES中删除{}",id);
    }
     */

}
