# 家居商品微服务平台

基于 **Spring Cloud** 的微服务学习项目，采用前后端分离架构，围绕家居商品发布流程实现后台管理、商品分类、品牌管理、属性分组、规格参数、销售属性、SPU/SKU、OSS 文件上传、Gateway 路由、Nacos 注册发现和 Nginx 反向代理等功能。适合学习 Spring Cloud Alibaba 微服务、网关路由、服务注册发现、MyBatis-Plus 等技术的参考实践。

> 环境方案：本仓库已配置 **Docker 一键环境**，中间件（MySQL 等）用 Docker Desktop 启动，后端服务直接连 `127.0.0.1`，不再需要 Vagrant/CentOS 虚拟机。详细说明见 [docker/README-docker.md](docker/README-docker.md)。

## 快速运行

以下步骤适用于本地开发（Windows、Linux 或 macOS）。项目包含多个 Spring Boot 微服务，不能只运行根目录的 Maven 工程。

### 1. 准备环境

- JDK 8、Maven 3.x
- Docker Desktop（负责 MySQL 等中间件，替代虚拟机）
- Node.js 10.16.3（前端依赖 `node-sass@4.13.1`，使用过高版本 Node 可能安装失败）
- Nacos 1.x/2.x（默认地址 `127.0.0.1:8848`，本机运行即可，也可容器化）
- Git；Redis 默认关闭，不启动也可以运行后台服务

### 2. 启动中间件（Docker）

```bash
cd docker
docker compose up -d
```

首次启动会自动初始化数据库：创建 `hspliving_manage`（后台库，含 admin 账号和菜单）和 `hspliving_commodity`（商品库 13 张表），并统一 utf8mb4 字符集。验证：

```bash
docker compose ps          # hspliving-mysql 应为 healthy
```

### 3. 启动 Nacos

启动本机 Nacos 后访问：

```text
http://127.0.0.1:8848/nacos
```

注意：`hspliving-service` 依赖 Nacos 配置中心（namespace/group 见"项目部署"章节），启动前需保证配置已存在。

### 4. 编译后端

```bash
cd hspliving
mvn clean package -DskipTests
```

也可以直接用 IDEA 打开 `hspliving/pom.xml`，等待 Maven 导入完成。

### 5. 启动后端（按顺序）

先启动 Nacos，再分别运行下面 4 个启动类：

1. `io.renren.RenrenApplication`（后台服务，`8080`）
2. `com.hspliving.commodity.HsplivingCommodityApplication`（商品服务，`9090`）
3. `com.hspliving.service.HsplivingServiceApplication`（第三方服务，`7070`，依赖 Nacos 配置中心）
4. `com.hspliving.gateway.HsplivingGatewayApplication`（网关，`5050`）

确认服务都已注册到 Nacos 后，可测试：

```text
http://localhost:5050/api/commodity/category/list/tree
http://localhost:5050/api/commodity/brand/list
```

### 6. 启动前端

```bash
cd renren-fast-vue
npm install
npm run dev
```

浏览器访问 <http://localhost:8001>，默认账号为 `admin`，密码为 `admin`。前端接口默认配置为 `http://localhost:5050/api`，如网关地址或端口有变化，请修改 `renren-fast-vue/static/config/index.js`。

## 项目结构

```text
.
├── hspliving/                 后端 Maven 聚合工程
│   ├── hspliving-common       公共模块
│   ├── hspliving-commodity    商品微服务
│   ├── hspliving-service      第三方服务微服务
│   ├── hspliving-gateway      网关微服务
│   ├── renren-fast            后台管理脚手架后端
│   └── renren-generator       代码生成器
├── renren-fast-vue/           后台管理前端
└── docker/                    Docker 环境（compose、MySQL 初始化脚本、Nginx 配置）
```

## 微服务与模块

| 模块 | 服务名 | 默认端口 | 数据库 | 说明 |
| --- | --- | --- | --- | --- |
| `hspliving-common` | 无独立服务 | 无 | 无 | 公共工具、统一返回、异常码、校验分组、公共依赖 |
| `hspliving-commodity` | `hspliving-commodity` | `9090` | `hspliving_commodity` | 商品服务，负责分类、品牌、属性分组、SPU、SKU 等业务 |
| `hspliving-service` | `hspliving-service` | `7070`（以 Nacos 配置为准） | 无业务库 | 第三方服务，课程中主要用于阿里云 OSS 上传签名 |
| `hspliving-gateway` | `hspliving-gateway` | `5050` | 无 | Spring Cloud Gateway，统一路由、跨域、服务转发 |
| `renren-fast` | `renren-fast` | `8080` | `hspliving_manage` | 后台管理脚手架后端，提供登录、权限、菜单、验证码、OSS、定时任务等能力 |
| `renren-generator` | 无注册服务 | `80` | `hspliving_commodity` | 代码生成器，根据数据库表生成 Controller、Service、Dao、Entity、Mapper |
| `renren-fast-vue` | 前端工程 | `8001` | 无 | 后台管理前端，通过 Gateway 访问后端接口 |

Gateway 路由关系：

```text
/api/commodity/**  -> hspliving-commodity
/api/service/**    -> hspliving-service
/api/**            -> renren-fast
Host=**.hspliving.com -> hspliving-commodity
```

## 数据库与数据表

项目涉及两个主要 MySQL 数据库：

```text
hspliving_commodity    商品业务库
hspliving_manage       后台管理库
```

### 商品业务库：hspliving_commodity

商品库由 `hspliving-commodity` 使用，表名来自商品服务实体类中的 `@TableName`。初始化脚本为 `docker/mysql/init/03-hspliving_commodity.sql`。

| 表名 | 对应实体 | 归属模块 | 说明 |
| --- | --- | --- | --- |
| `commodity_category` | `CategoryEntity` | 商品分类 | 三级分类表，保存分类名称、父级分类、层级、排序、图标、计量单位、商品数量等 |
| `commodity_brand` | `BrandEntity` | 品牌管理 | 品牌表，保存品牌名称、logo、描述、显示状态、首字母、排序 |
| `commodity_category_brand_relation` | `CategoryBrandRelationEntity` | 分类品牌关联 | 维护分类与品牌的多对多关系，并冗余分类名、品牌名 |
| `commodity_attrgroup` | `AttrgroupEntity` | 属性分组 | 属性分组表，保存分组名称、排序、描述、图标、所属分类 |
| `commodity_attr` | `AttrEntity` | 商品属性 | 属性表，保存规格参数和销售属性，包括可检索、可选值、属性类型、所属分类、所属分组等 |
| `commodity_attr_attrgroup_relation` | `AttrAttrgroupRelationEntity` | 属性分组关联 | 维护属性与属性分组之间的关系 |
| `commodity_product_attr_value` | `ProductAttrValueEntity` | SPU 属性值 | 保存 SPU 对应的规格参数值 |
| `commodity_spu_info` | `SpuInfoEntity` | SPU 管理 | SPU 基本信息表，保存商品名称、描述、分类、品牌、重量、发布状态、创建/更新时间 |
| `commodity_spu_info_desc` | `SpuInfoDescEntity` | SPU 管理 | SPU 图文详情表，保存商品介绍详情 |
| `commodity_spu_images` | `SpuImagesEntity` | SPU 图片 | 保存 SPU 图片名称、地址、排序、默认图 |
| `commodity_sku_info` | `SkuInfoEntity` | SKU 管理 | SKU 基本信息表，保存 SKU 名称、描述、分类、品牌、默认图片、标题、副标题、价格、销量 |
| `commodity_sku_images` | `SkuImagesEntity` | SKU 图片 | 保存 SKU 图片地址、排序、默认图 |
| `commodity_sku_sale_attr_value` | `SkuSaleAttrValueEntity` | SKU 销售属性 | 保存 SKU 对应的销售属性值，如颜色、版本等 |

### 后台管理库：hspliving_manage

后台管理库由 `renren-fast` 使用，初始化脚本位于：

```text
hspliving/renren-fast/db/mysql.sql
```

| 表名 | 归属模块 | 说明 |
| --- | --- | --- |
| `sys_menu` | 权限菜单 | 菜单管理表，保存后台菜单、按钮权限、URL、授权标识、排序等 |
| `sys_user` | 系统用户 | 后台管理员用户表，保存用户名、密码、邮箱、手机号、状态、创建时间等 |
| `sys_user_token` | 登录认证 | 系统用户 Token 表，保存后台用户登录 token 和过期时间 |
| `sys_captcha` | 验证码 | 系统验证码表，保存验证码 uuid、验证码内容、过期时间 |
| `sys_role` | 角色管理 | 后台角色表，保存角色名称、备注、创建者、创建时间 |
| `sys_user_role` | 用户角色关联 | 用户和角色的关联表 |
| `sys_role_menu` | 角色菜单关联 | 角色和菜单权限的关联表 |
| `sys_config` | 系统配置 | 系统配置信息表，保存参数 key、value、备注、状态 |
| `sys_log` | 系统日志 | 操作日志表，保存请求方法、参数、IP、耗时、操作人等 |
| `sys_oss` | 文件上传 | 文件上传记录表，保存上传 URL、创建时间 |
| `schedule_job` | 定时任务 | 定时任务表，保存 bean、方法、参数、cron、状态等 |
| `schedule_job_log` | 定时任务日志 | 定时任务执行日志表，保存执行结果、错误信息、耗时等 |
| `tb_user` | App 用户 | renren-fast App 示例用户表，使用 JWT 认证 |
| `QRTZ_JOB_DETAILS` | Quartz | Quartz 任务详情表 |
| `QRTZ_TRIGGERS` | Quartz | Quartz 触发器主表 |
| `QRTZ_SIMPLE_TRIGGERS` | Quartz | Quartz 简单触发器表 |
| `QRTZ_CRON_TRIGGERS` | Quartz | Quartz Cron 触发器表 |
| `QRTZ_SIMPROP_TRIGGERS` | Quartz | Quartz SimpleProperties 触发器表 |
| `QRTZ_BLOB_TRIGGERS` | Quartz | Quartz Blob 触发器表 |
| `QRTZ_CALENDARS` | Quartz | Quartz 日历表 |
| `QRTZ_PAUSED_TRIGGER_GRPS` | Quartz | Quartz 暂停触发器组表 |
| `QRTZ_FIRED_TRIGGERS` | Quartz | Quartz 已触发触发器表 |
| `QRTZ_SCHEDULER_STATE` | Quartz | Quartz 调度器状态表 |
| `QRTZ_LOCKS` | Quartz | Quartz 锁表 |

## 项目部署（Docker 方式）

本节只写运行项目之前的环境部署和配置准备。具体启动顺序见"项目启动"。

### 环境要求

- JDK 8、Maven 3.x
- Docker Desktop（Windows/macOS）或 Docker Engine（Linux）
- Node.js 10.16.3，课程项目推荐固定该版本，避免 `node-sass` 兼容问题
- Nacos，默认访问 `127.0.0.1:8848`

### MySQL 部署（Docker）

已封装在 `docker/docker-compose.yml`，一键启动：

```bash
cd docker
docker compose up -d
```

- 镜像：`mysql:5.7`，端口 `3306:3306`，账号 `root/root`，utf8mb4
- 数据持久化到 `docker/mysql/data`；初始化脚本在 `docker/mysql/init`
- 常用命令：

```bash
docker compose ps                 # 查看状态
docker compose logs -f mysql      # 查看日志
docker compose down               # 停止（保留数据）
docker compose down -v            # 停止并清空数据卷，重新初始化数据库（慎用）
```

数据库连接配置（各服务已默认指向）：

```text
地址：127.0.0.1:3306
用户名：root
密码：root
```

### Nacos 部署

后端服务默认注册到：

```text
127.0.0.1:8848
```

启动 Nacos 后访问：

```text
http://127.0.0.1:8848/nacos
```

需要注册到 Nacos 的服务：

```text
renren-fast
hspliving-commodity
hspliving-service
hspliving-gateway
```

`hspliving-service` 使用 Nacos 配置中心，当前 `bootstrap.properties` 指向：

```text
namespace：15f5838a-532a-4090-bc1b-93c447409530
group：dev
dataId：hspliving-service.yml
```

如果使用配置中心，需要在 Nacos 中创建对应 namespace、group 和 `hspliving-service.yml`（内容含服务端口与 OSS 配置）。如果不使用配置中心，可以恢复 `hspliving-service/src/main/resources/application.yml` 中被注释的端口、OSS、Nacos 配置。

### 后端配置检查

启动前检查这些配置文件：

```text
hspliving/hspliving-commodity/src/main/resources/application.properties
hspliving/hspliving-commodity/src/main/resources/application.yml
hspliving/hspliving-service/src/main/resources/bootstrap.properties
hspliving/hspliving-gateway/src/main/resources/application.yml
hspliving/renren-fast/src/main/resources/application.yml
hspliving/renren-fast/src/main/resources/application-dev.yml
hspliving/renren-generator/src/main/resources/application.yml
```

重点确认：

- MySQL 地址（`127.0.0.1:3306`）、用户名、密码正确
- Nacos 地址可访问
- 服务名与 Gateway 路由一致
- `hspliving-service` 的 Nacos 配置已创建
- OSS 的 `endpoint`、`bucket`、`access-key`、`secret-key` 已替换为自己的配置

不要把真实云服务密钥提交到公开仓库。

### 前端配置检查

前端接口地址位于：

```text
renren-fast-vue/static/config/index.js
```

当前配置：

```js
window.SITE_CONFIG['baseUrl'] = 'http://localhost:5050/api';
```

该配置表示前端请求先进入 Gateway，再由 Gateway 转发到后台管理服务、商品服务或第三方服务。

### Nginx 部署，可选

本地开发可以不部署 Nginx，直接使用：

```text
前端：http://localhost:8001
网关：http://localhost:5050
```

如需用 `www.hspliving.com` 域名部署商城首页，可在 `docker/docker-compose.yml` 中取消 Nginx 的注释并启用（配置见 `docker/nginx/conf/nginx.conf`）：

```nginx
server {
    listen 80;
    server_name www.hspliving.com;

    location / {
        root /usr/share/nginx/html/hspliving;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://host.docker.internal:5050;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

注意：Nginx 容器内转发到宿主机 IDEA 启动的网关，需用 `host.docker.internal` 指向宿主机。

## 项目启动

首次运行前先完成"项目部署"。

### 启动基础组件

```bash
# 1. 启动 MySQL（Docker）
cd docker
docker compose up -d

# 2. 启动 Nacos，并确认控制台可访问
# http://127.0.0.1:8848/nacos
```

### 启动后端

建议使用 IDEA 打开：

```text
hspliving/pom.xml
```

等待 Maven 依赖加载完成后，按顺序启动：

1. `renren-fast`

```text
hspliving/renren-fast/src/main/java/io/renren/RenrenApplication.java
```

访问：

```text
http://localhost:8080/renren-fast
```

2. `hspliving-commodity`

```text
hspliving/hspliving-commodity/src/main/java/com/hspliving/commodity/HsplivingCommodityApplication.java
```

测试：

```text
http://localhost:9090/commodity/category/list/tree
```

3. `hspliving-service`

```text
hspliving/hspliving-service/src/main/java/com/hspliving/service/HsplivingServiceApplication.java
```

通过 Gateway 测试：

```text
http://localhost:5050/api/service/oss/policy
```

4. `hspliving-gateway`

```text
hspliving/hspliving-gateway/src/main/java/com/hspliving/gateway/HsplivingGatewayApplication.java
```

Gateway 测试：

```text
http://localhost:5050/api/commodity/category/list/tree
http://localhost:5050/api/commodity/brand/list
```

### 启动前端

```bash
cd renren-fast-vue
npm install
npm run dev
```

访问：

```text
http://localhost:8001
```

默认账号：

```text
admin / admin
```

## 从虚拟机迁移到 Docker

本仓库原课程环境使用 Vagrant + VirtualBox（CentOS `192.168.56.100`）运行 MySQL/Nginx，现已迁移到 Docker Desktop。相关改动：

- 新增 `docker/` 目录：`docker-compose.yml`、MySQL 初始化脚本、Nginx 配置
- 数据库连接地址由 `192.168.56.100:3306` 改为 `127.0.0.1:3306`，涉及两个文件：
  - `hspliving/hspliving-commodity/src/main/resources/application.yml`
  - `hspliving/renren-fast/src/main/resources/application-dev.yml`
- 商品库建表脚本 `docker/mysql/init/03-hspliving_commodity.sql` 由实体类生成（仓库未提供课程 SQL，字段类型为合理推断；如有课程官方 SQL 可替换后 `docker compose down -v && docker compose up -d` 重新初始化）

如需切回虚拟机方案，把上述两个配置改回 `192.168.56.100:3306` 即可。

### 迁移后的两个重要调整

1. **跨域统一由网关处理**：为避免浏览器报 `Access-Control-Allow-Origin` 重复头（网关与 renren-fast 都配 CORS 时会出现，表现为"登录成功又被弹回登录页"），已注释 `renren-fast/.../config/CorsConfig.java` 的 `addCorsMappings`，跨域统一在 `HsplivingGatewayCorsConfiguration` 处理。
2. **中文乱码防护**：`docker/mysql/init/` 下的初始化 SQL 头部均加了 `SET NAMES utf8mb4;`，防止中文在导入时被双重编码成 `ç³»ç»Ÿ...` 一类乱码（详见下文"常见问题与排障"第 3 条）。

## 常见问题与排障

### 1. 前端页面显示模板源码（`<% if (process.env.NODE_ENV...`）

原因是 Node 版本过高（如 v20）与 webpack3/node-sass4 不兼容，编译失败。使用 **Node 10.16.3** 重新 `npm install --no-audit --no-fund` 后 `npm run dev` 即可。

### 2. 登录成功又被弹回登录页

浏览器 console 报 `Access-Control-Allow-Origin 包含多个值`：网关与 renren-fast 都配置了 CORS。已通过注释 `renren-fast` 的 `CorsConfig.addCorsMappings` 修复（见上文"迁移后的两个重要调整"）。

### 3. 后台菜单中文乱码（如 `ç³»ç»Ÿ...`）

初始化 SQL 导入时连接字符集未指定 utf8mb4，导致中文被双重编码。排查可用 `HEX(name)`：正常中文是 `E5/E7` 开头（3 字节/字），双重编码是 `C3A7C2B3` 开头（6 字节/字）。修复存量数据：

```sql
UPDATE sys_menu SET name = CONVERT(CAST(CONVERT(name USING latin1) AS BINARY) USING utf8mb4);
```

init 脚本已加 `SET NAMES utf8mb4;` 防复发；若已运行过旧脚本，可 `docker compose down -v && docker compose up -d` 重新初始化。

### 4. 登录后没有"商品管理"菜单

动态菜单由 `sys_menu` 表驱动。若前端 `src/views/modules/commodity/` 页面存在但菜单没有入口，需向 `sys_menu` 插入商品管理目录及子菜单记录。

### 5. 登录提示"验证码不正确"

验证码是**一次性**的（校验后即删）。刷新页面需用新的 uuid + 验证码；本地调试可从 `sys_captcha` 表按 uuid 查答案：`SELECT code FROM sys_captcha WHERE uuid='<uuid>'`。
