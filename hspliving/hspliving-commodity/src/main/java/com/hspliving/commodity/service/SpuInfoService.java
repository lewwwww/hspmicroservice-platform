package com.hspliving.commodity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hspliving.common.utils.PageUtils;
import com.hspliving.commodity.entity.SpuInfoEntity;
import com.hspliving.commodity.vo.SpuSaveVO;

import java.util.Map;

/**
 * 商品spu信息
 *
 * @author hsp
 * @email hsp@gmail.com
 * @date 2022-12-05 14:26:19
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVO spuSaveVO);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);


    //通过携带的检索条件，进行分页查询
    PageUtils queryPageByCondition(Map<String, Object> params);

    //商品SPU上架
    void up(Long spuId);

    //商品SPU下架
    void down(Long spuId);
}

