package com.hmall.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.common.domain.PageDTO;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.query.ItemPageQuery;

/**
 * @author axno
 * @date 2024/11/29 21:36
 * @DESCRIPTION
 */
public interface ISearchService extends IService<Item> {
    //ES搜索
    PageDTO<ItemDoc> EsSearch(ItemPageQuery query);

}
