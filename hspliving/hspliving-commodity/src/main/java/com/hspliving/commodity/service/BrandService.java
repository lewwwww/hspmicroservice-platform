package com.hspliving.commodity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hspliving.common.utils.PageUtils;
import com.hspliving.commodity.entity.BrandEntity;

import java.util.Map;

/**
 * 家居品牌
 *
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

