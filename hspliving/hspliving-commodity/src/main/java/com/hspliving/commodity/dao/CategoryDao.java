package com.hspliving.commodity.dao;

import com.hspliving.commodity.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类表
 * 
 * @author hsp
 * @email hsp@gmail.com
 * @date 2022-10-27 10:26:11
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
