package com.hmall.api.client;

import com.hmall.api.config.DefaultFeignConfig;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.api.client.fallback.ItemClientFallbackFactory;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * @author axno
 * @date 2024/10/19 23:06
 * @DESCRIPTION
 */
@FeignClient(value = "item-service",fallbackFactory = ItemClientFallbackFactory.class)
public interface ItemClient {
    @GetMapping("/items")
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);

    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);

    //根据id查询商品 记得添上/items
    @GetMapping("/items/{id}")
    public ItemDTO queryItemById(@PathVariable("id") Long id);

    //新增商品
    @PostMapping("/items")
    public void saveItem(@RequestBody ItemDTO item);

}
