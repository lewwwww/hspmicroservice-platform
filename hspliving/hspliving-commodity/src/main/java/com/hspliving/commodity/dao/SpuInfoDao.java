package com.hspliving.commodity.dao;

import com.hspliving.commodity.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品spu信息
 *
 * @author hsp
 * @email hsp@gmail.com
 * @date 2022-12-05 14:26:19
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    //方法: 修改spu的发布状态
    void updateSpuStatus(@Param("spuId") Long spuId, @Param("statusCode") int statusCode);
}
