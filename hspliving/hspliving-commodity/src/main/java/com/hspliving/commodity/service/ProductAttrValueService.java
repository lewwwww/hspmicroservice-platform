package com.hspliving.commodity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hspliving.common.utils.PageUtils;
import com.hspliving.commodity.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu基本属性值
 *
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
    //保存基本属性-支持批量添加
    void saveProductAttr(List<ProductAttrValueEntity> productAttrValueEntities);
}

