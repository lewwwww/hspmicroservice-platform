-- ============================================================
-- hspliving_commodity 商品库建表语句
-- 说明：仓库内未提供原始商品库 SQL，本文件根据
--   hspliving-commodity 的 13 个实体类（@TableName + 字段）生成，
--   字段类型/长度为依据字段语义的合理推断，建议与官方 SQL 比对后使用。
-- 商品库无种子数据，可在后台管理中手工创建分类/品牌等。
-- ============================================================

-- 强制连接字符集为 utf8mb4，避免中文被双重编码导致乱码
SET NAMES utf8mb4;

USE `hspliving_commodity`;

-- 1. 商品三级分类表
CREATE TABLE `commodity_category` (
  `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name`        varchar(100) NOT NULL COMMENT '名称',
  `parent_id`   bigint(20) DEFAULT NULL COMMENT '父分类id',
  `cat_level`   int(11) DEFAULT NULL COMMENT '层级',
  `is_show`     tinyint(4) DEFAULT '1' COMMENT '0不显示，1显示',
  `sort`        int(11) DEFAULT NULL COMMENT '排序',
  `icon`        varchar(255) DEFAULT NULL COMMENT '图标',
  `pro_unit`    varchar(50) DEFAULT NULL COMMENT '统计单位',
  `pro_count`   int(11) DEFAULT NULL COMMENT '商品数量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 2. 家居品牌表
CREATE TABLE `commodity_brand` (
  `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name`          varchar(100) NOT NULL COMMENT '品牌名',
  `logo`          varchar(255) DEFAULT NULL COMMENT 'logo(图片URL)',
  `description`   varchar(255) DEFAULT NULL COMMENT '说明',
  `isshow`        tinyint(4) DEFAULT '1' COMMENT '显示状态 0隐藏 1显示',
  `first_letter`  char(1) DEFAULT NULL COMMENT '检索首字母 a-z A-Z',
  `sort`          int(11) DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家居品牌表';

-- 3. 品牌分类关联表
CREATE TABLE `commodity_category_brand_relation` (
  `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `brand_id`      bigint(20) DEFAULT NULL COMMENT '品牌id',
  `category_id`   bigint(20) DEFAULT NULL COMMENT '分类id',
  `brand_name`    varchar(100) DEFAULT NULL COMMENT '品牌名称(冗余)',
  `category_name` varchar(100) DEFAULT NULL COMMENT '分类名称(冗余)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌分类关联表';

-- 4. 家居商品属性分组表
CREATE TABLE `commodity_attrgroup` (
  `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name`        varchar(100) DEFAULT NULL COMMENT '组名',
  `sort`        int(11) DEFAULT NULL COMMENT '排序',
  `description` varchar(255) DEFAULT NULL COMMENT '说明',
  `icon`        varchar(255) DEFAULT NULL COMMENT '组图标',
  `category_id` bigint(20) DEFAULT NULL COMMENT '所属分类id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品属性分组表';

-- 5. 商品属性表（规格参数 + 销售属性）
CREATE TABLE `commodity_attr` (
  `attr_id`      bigint(20) NOT NULL AUTO_INCREMENT COMMENT '属性id',
  `attr_name`    varchar(100) DEFAULT NULL COMMENT '属性名',
  `search_type`  tinyint(4) DEFAULT NULL COMMENT '是否需要检索[0-不需要，1-需要]',
  `icon`         varchar(255) DEFAULT NULL COMMENT '图标',
  `value_select` varchar(255) DEFAULT NULL COMMENT '可选值列表[用逗号分隔]',
  `attr_type`    tinyint(4) DEFAULT NULL COMMENT '属性类型[0-销售属性，1-基本属性]',
  `enable`       bigint(20) DEFAULT NULL COMMENT '启用状态[0-禁用，1-启用]',
  `category_id`  bigint(20) DEFAULT NULL COMMENT '所属分类',
  `show_desc`    tinyint(4) DEFAULT NULL COMMENT '快速展示[0-否 1-是]',
  PRIMARY KEY (`attr_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品属性表';

-- 6. 商品属性和属性组关联表
CREATE TABLE `commodity_attr_attrgroup_relation` (
  `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `attr_id`       bigint(20) DEFAULT NULL COMMENT '属性id',
  `attr_group_id` bigint(20) DEFAULT NULL COMMENT '属性分组id',
  `attr_sort`     int(11) DEFAULT NULL COMMENT '属性组内排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品属性和属性组关联表';

-- 7. spu 基本属性值表
CREATE TABLE `commodity_product_attr_value` (
  `id`         bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `spu_id`     bigint(20) DEFAULT NULL COMMENT '商品id',
  `attr_id`    bigint(20) DEFAULT NULL COMMENT '属性id',
  `attr_name`  varchar(100) DEFAULT NULL COMMENT '属性名',
  `attr_value` varchar(255) DEFAULT NULL COMMENT '属性值',
  `attr_sort`  int(11) DEFAULT NULL COMMENT '顺序',
  `quick_show` tinyint(4) DEFAULT NULL COMMENT '快速展示[0-否 1-是]',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='spu基本属性值表';

-- 8. 商品 spu 信息表
CREATE TABLE `commodity_spu_info` (
  `id`              bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `spu_name`        varchar(200) DEFAULT NULL COMMENT '商品名称',
  `spu_description` varchar(500) DEFAULT NULL COMMENT '商品描述',
  `catalog_id`      bigint(20) DEFAULT NULL COMMENT '所属分类id',
  `brand_id`        bigint(20) DEFAULT NULL COMMENT '品牌id',
  `weight`          decimal(18,2) DEFAULT NULL COMMENT '重量',
  `publish_status`  tinyint(4) DEFAULT NULL COMMENT '上架状态[0-下架，1-上架]',
  `create_time`     datetime DEFAULT NULL COMMENT '创建时间',
  `update_time`     datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品spu信息表';

-- 9. 商品 spu 信息介绍表（spu_id 由程序传入，非自增）
CREATE TABLE `commodity_spu_info_desc` (
  `spu_id`  bigint(20) NOT NULL COMMENT '商品id(INPUT)',
  `decript` text COMMENT '商品介绍图片',
  PRIMARY KEY (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品spu信息介绍表';

-- 10. spu 图片集表
CREATE TABLE `commodity_spu_images` (
  `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `spu_id`      bigint(20) DEFAULT NULL COMMENT 'spu_id',
  `img_name`    varchar(255) DEFAULT NULL COMMENT '图片名',
  `img_url`     varchar(255) DEFAULT NULL COMMENT '图片地址',
  `img_sort`    int(11) DEFAULT NULL COMMENT '顺序',
  `default_img` tinyint(4) DEFAULT NULL COMMENT '是否默认图[0-否 1-是]',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='spu图片集表';

-- 11. sku 信息表
CREATE TABLE `commodity_sku_info` (
  `sku_id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'skuId',
  `spu_id`          bigint(20) DEFAULT NULL COMMENT 'spuId',
  `sku_name`        varchar(200) DEFAULT NULL COMMENT 'sku名称',
  `sku_desc`        text COMMENT 'sku介绍描述',
  `catalog_id`      bigint(20) DEFAULT NULL COMMENT '所属分类id',
  `brand_id`        bigint(20) DEFAULT NULL COMMENT '品牌id',
  `sku_default_img` varchar(255) DEFAULT NULL COMMENT '默认图片',
  `sku_title`       varchar(255) DEFAULT NULL COMMENT '标题',
  `sku_subtitle`    varchar(255) DEFAULT NULL COMMENT '副标题',
  `price`           decimal(18,2) DEFAULT NULL COMMENT '价格',
  `sale_count`      bigint(20) DEFAULT NULL COMMENT '销量',
  PRIMARY KEY (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='sku信息表';

-- 12. sku 图片表
CREATE TABLE `commodity_sku_images` (
  `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id`      bigint(20) DEFAULT NULL COMMENT 'sku_id',
  `img_url`     varchar(255) DEFAULT NULL COMMENT '图片地址',
  `img_sort`    int(11) DEFAULT NULL COMMENT '排序',
  `default_img` tinyint(4) DEFAULT NULL COMMENT '默认图[0-否 1-是]',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='sku图片表';

-- 13. sku 销售属性/值表
CREATE TABLE `commodity_sku_sale_attr_value` (
  `id`         bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id`     bigint(20) DEFAULT NULL COMMENT 'sku_id',
  `attr_id`    bigint(20) DEFAULT NULL COMMENT 'attr_id',
  `attr_name`  varchar(100) DEFAULT NULL COMMENT '销售属性名',
  `attr_value` varchar(255) DEFAULT NULL COMMENT '销售属性值',
  `attr_sort`  int(11) DEFAULT NULL COMMENT '顺序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='sku的销售属性/值表';
