package com.hspliving.commodity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 商品spu信息介绍
 * 
 * @author hsp
 * @email hsp@gmail.com
 * @date 2022-12-05 15:39:52
 */
@Data
@TableName("commodity_spu_info_desc")
public class SpuInfoDescEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 商品id
	 * 老师说明:
	 * 1. 因为 commodity_spu_info_desc表的 spu_id 字段不是自增长的
	 * 2. 而是我们添加SpuInfoDescEntity对象配置的id
	 *
	 */
	@TableId(type = IdType.INPUT)
	private Long spuId;
	/**
	 * 商品介绍图片
	 */
	private String decript;

}
