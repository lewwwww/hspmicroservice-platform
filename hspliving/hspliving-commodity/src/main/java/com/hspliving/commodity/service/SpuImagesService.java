package com.hspliving.commodity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hspliving.common.utils.PageUtils;
import com.hspliving.commodity.entity.SpuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * spu图片集
 *
 * @author hsp
 * @email hsp@gmail.com
 * @date 2022-12-05 16:30:17
 */
public interface SpuImagesService extends IService<SpuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    // 批量保存spu对应的图片集
    void saveImages(Long id, List<String> images);
}

