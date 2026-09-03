package com.hspliving.commodity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hspliving.common.utils.PageUtils;
import com.hspliving.commodity.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * 商品spu信息介绍
 *
 * @author hsp
 * @email hsp@gmail.com
 * @date 2022-12-05 15:39:52
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfoDesc(SpuInfoDescEntity spuInfoDescEntity);
}

