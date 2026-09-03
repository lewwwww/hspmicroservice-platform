# hspliving 使用 Docker 替代虚拟机（推荐）

原来虚拟机环境用 Vagrant + VirtualBox（CentOS 192.168.56.100）跑 MySQL / Nginx。
本目录把中间件全部搬到 Docker Desktop，**以后不用再 `vagrant up`**。

## 1. 启动前准备

- 安装并启动 **Docker Desktop**（本机已装，确认小鲸鱼图标在运行）
- 后端服务仍在 IDEA 里启动（需要 JDK8、Maven、Nacos）

## 2. 一键启动中间件

在 `docker` 目录下执行：

```bash
docker compose up -d
```

- 默认启动 **MySQL 5.7**（端口 3306，root/root，utf8mb4）
- 首次启动会自动执行 `mysql/init/` 下的 SQL：建两个库 + 导入后台库数据 + 建商品库 13 张表
- Nacos / Nginx 默认不启动（见 `docker-compose.yml` 里的注释，需要时取消注释）

验证：

```bash
docker compose ps                 # 三个服务状态 healthy/running
docker exec -it hspliving-mysql mysql -uroot -proot -e "show databases;"
```

## 3. 启动后端（照旧，无需虚拟机）

IDEA 打开 `hspliving/pom.xml`，按顺序启动：

1. `renren-fast`（8080）—— 后台管理
2. `hspliving-commodity`（9090）—— 商品服务
3. `hspliving-service`（7070）—— 第三方服务（依赖 Nacos 配置中心）
4. `hspliving-gateway`（5050）—— 网关

> 数据库连接地址已改为 `127.0.0.1:3306`，若以后想切回虚拟机，改回
> `192.168.56.100:3306` 即可（涉及两个文件）：
> `hspliving-commodity/src/main/resources/application.yml`
> `renren-fast/src/main/resources/application-dev.yml`

## 4. 启动前端（照旧）

```bash
cd renren-fast-vue
npm install
npm run dev     # 访问 http://localhost:8001  admin/admin
```

## 5. 常见操作

| 操作 | 命令 |
| --- | --- |
| 启动/停止中间件 | `docker compose up -d` / `docker compose down` |
| 查看日志 | `docker compose logs -f mysql` |
| 进入 MySQL | `docker exec -it hspliving-mysql mysql -uroot -proot` |
| 重新初始化数据库 | `docker compose down -v && docker compose up -d`（会清空数据卷，慎用） |

## 6. 与虚拟机方案的差异

| 项 | 虚拟机方案 | Docker 方案 |
| --- | --- | --- |
| MySQL | 虚拟机内 Docker | 本机 Docker Desktop，`127.0.0.1:3306` |
| Nacos | 宿主机 `127.0.0.1:8848` | 不变（可选搬进 Docker） |
| Nginx | 虚拟机内 | 可选，见 compose 注释 |
| 后端服务 | IDEA 启动 | 不变 |

## 7. 注意事项

- **商品库建表 SQL**：仓库未提供原始建表 SQL，`mysql/init/03-hspliving_commodity.sql`
  是根据实体类生成的（字段类型为合理推断）。若你手上有官方 SQL，可替换该文件
  后执行 `docker compose down -v && docker compose up -d` 重新初始化。
- **hspliving-service 的 Nacos 配置中心**：若把 Nacos 也搬进 Docker，需在全新 Nacos
  中重建 `namespace: 15f5838a-532a-4090-bc1b-93c447409530`、`group: dev`、
  `dataId: hspliving-service.yml`（OSS 与端口配置）。
- 不要把真实云 AK/SK 提交到公开仓库。
