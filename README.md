# Hospital HIS

面向医院门诊业务的 HIS 信息管理系统，覆盖挂号、门诊诊疗、医技执行、收费和药房发药等核心流程。

## 技术栈

- 前端：Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router、Axios
- 后端：JDK 21、Spring Boot 3、Spring Security、MyBatis、Flyway、Maven
- 数据库：MySQL 8

## 项目结构

```text
hospital/
├── backend/                    # Spring Boot 模块化单体
│   ├── his-common/             # 统一响应、异常等公共能力
│   ├── his-platform/           # 登录、权限、系统能力
│   ├── his-master-data/        # 科室、员工、疾病、项目、药品等基础数据
│   ├── his-registration/       # 挂号、退号、收费、退费
│   ├── his-outpatient/         # 门诊病历、诊断、申请、处方
│   ├── his-medical-tech/       # 检查、检验、处置执行
│   ├── his-pharmacy/           # 发药、退药、库存
│   └── his-application/        # 应用启动与运行配置
├── frontend/                   # Vue 3 管理端
└── docs/                       # 架构、接口与协作文档
```

模块边界和团队协作方式见 [docs/architecture.md](docs/architecture.md) 与 [docs/development.md](docs/development.md)。业务状态、错误码与模块所有权见 [docs/contracts.md](docs/contracts.md)，接口以 [docs/api/openapi.yaml](docs/api/openapi.yaml) 为准。详细业务需求见 [HIS医院信息管理系统需求说明.md](HIS医院信息管理系统需求说明.md)。

## 开发基线

- 数据库：3 个 Flyway 迁移已定义平台、基础数据、门诊业务、收费与库存共 19 张表。
- 契约：跨模块状态机、数据所有权、角色权限、错误码和第一阶段 OpenAPI 已固定。
- 认证：后端 JWT 登录与 RBAC、首次管理员初始化，前端登录、路由守卫、令牌注入和退出已接通。
- 持久层：业务模块已统一 MyBatis Mapper/XML、分页、条件状态更新和库存乐观锁模板。

该基线已满足三名成员按 `docs/development.md` 的目录边界并行开发。涉及数据库迁移、OpenAPI 或全局认证的变更仍需单独 PR 协调。

## 本地启动

环境要求：JDK 21、Node.js 22+、MySQL 8.x。

先在本机 MySQL 中创建开发数据库：

```sql
CREATE DATABASE his
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
```

```powershell
# 1. 配置本机数据库连接
$env:DB_URL="jdbc:mysql://localhost:3306/his?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的本机 MySQL 密码"
$env:HIS_JWT_SECRET="至少 32 位的本机开发密钥"
$env:HIS_BOOTSTRAP_ADMIN_PASSWORD="首次启动时创建 admin 使用的密码"

# 2. 启动后端
cd backend
.\mvnw.cmd spring-boot:run -pl his-application -am

# 3. 启动前端（另一个终端）
cd frontend
npm install
npm run dev
```

默认地址：

- 前端：http://localhost:5173
- 后端：http://localhost:8090
- 健康检查：http://localhost:8090/actuator/health
- OpenAPI：http://localhost:8090/swagger-ui.html

数据库和端口均可通过环境变量覆盖，变量名参考根目录 `.env.example` 与 `frontend/.env.example`。

首次启动仅在用户表为空且设置了 `HIS_BOOTSTRAP_ADMIN_PASSWORD` 时创建管理员。后续启动不会重置已有账号密码。
