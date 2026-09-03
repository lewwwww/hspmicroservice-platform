package com.hspliving.commodity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hspliving.common.utils.PageUtils;
import com.hspliving.commodity.entity.SkuImagesEntity;

import java.util.Map;

/**
 * sku图片
 *
 * @author hsp
 * @email hsp@gmail.com
 * @date 2022-12-08 13:27:29
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

