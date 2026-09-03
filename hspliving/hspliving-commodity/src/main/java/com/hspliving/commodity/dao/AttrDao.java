package com.hspliving.commodity.dao;

import com.hspliving.commodity.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品属性表
 * 
 * @author hsp
 * @email hsp@gmail.com
 * @date 2022-11-26 14:02:57
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
	
}
