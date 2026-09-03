package com.hspliving.commodity.dao;

import com.hspliving.commodity.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类表
 * 
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
