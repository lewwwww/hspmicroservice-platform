package com.hspliving.commodity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hspliving.common.utils.PageUtils;
import com.hspliving.commodity.entity.AttrAttrgroupRelationEntity;

import java.util.Map;

/**
 * 商品属性和商品属性组的关联表
 *
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

