package com.hspliving.commodity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 商品属性表
 * 
 * @author hsp
 * @email hsp@gmail.com
 * @date 2022-11-26 14:02:57
 */
@Data
@TableName("commodity_attr")
public class AttrEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 属性id
	 */
	@TableId
	private Long attrId;
	/**
	 * 属性名
	 */
	private String attrName;
	/**
	 * 是否需要检索[0-不需要，1-需要]
	 */
	private Integer searchType;
	/**
	 * 图标
	 */
	private String icon;
	/**
	 * 可选值列表[用逗号分隔]
	 */
	private String valueSelect;
	/**
	 * 属性类型[0-销售属性，1-基本属性]
	 */
	private Integer attrType;
	/**
	 * 启用状态[0 - 禁用，1 - 启用]
	 */
	private Long enable;
	/**
	 * 所属分类
	 */
	private Long categoryId;
	/**
	 * 快速展示【是否展示在介绍上；0-否 1-是】
	 */
	private Integer showDesc;

	/**
	 * 1. 增加 属性/字段, 表示该商品属性(基本属性)关联的属性组的id
	 * 2. 注意attrGroupId , 在表中并没有对应的字段 @TableField(exist = false)
	 */
	@TableField(exist = false)
	private Long attrGroupId;

}
