package com.hmall.api.client.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author axno
 * @date 2024/10/29 19:50
 * @DESCRIPTION
 */
@Slf4j
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("查询商品失败：{}",cause);
                return CollUtils.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.error("扣减商品库存失败：{}",cause);
                //当前业务为研究，先抛异常
                throw new RuntimeException(cause);
            }

            @Override
            public ItemDTO queryItemById(Long id) {
                log.error("查询商品失败：{}",cause);
                return null;
            }

            @Override
            public void saveItem(ItemDTO item) {
                log.error("新增商品失败：{}",cause);
                //当前业务为研究，先抛异常
                throw new RuntimeException(cause);
            }
        };
    }
}
