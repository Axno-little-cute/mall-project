package com.hmall.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmall.search.domain.po.Item;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author axno
 * @date 2024/11/29 21:57
 * @DESCRIPTION
 */
@Mapper
public interface SearchMapper extends BaseMapper<Item> {
}
