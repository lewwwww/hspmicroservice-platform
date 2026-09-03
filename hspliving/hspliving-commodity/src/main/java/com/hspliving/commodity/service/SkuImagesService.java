package com.hspliving.commodity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hspliving.common.utils.PageUtils;
import com.hspliving.commodity.entity.SkuImagesEntity;

import java.util.Map;

/**
 * sku图片
 *
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

