package com.hspliving.commodity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hspliving.common.utils.PageUtils;
import com.hspliving.commodity.entity.SkuSaleAttrValueEntity;

import java.util.Map;

/**
 * sku的销售属性/值表
 *
 * @author hsp
 * @email hsp@gmail.com
 * @date 2022-12-08 14:50:25
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

