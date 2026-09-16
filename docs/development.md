# 团队开发约定

## 分支与合并

- `main` 始终保持可编译、可启动。
- 功能分支使用 `feature/<module>-<topic>`，修复分支使用 `fix/<module>-<topic>`。
- 一个分支只处理一个清晰任务，通过 Pull Request 合并。
- 合并前至少执行后端测试、前端类型检查和构建。

## 三人并行边界

| 成员方向 | 后端目录 | 前端目录 |
| --- | --- | --- |
| 平台、基础数据、挂号收费 | `his-platform`、`his-master-data`、`his-registration` | `modules/master-data`、`modules/registration` |
| 门诊诊疗 | `his-outpatient` | `modules/outpatient` |
| 医技与药房 | `his-medical-tech`、`his-pharmacy` | `modules/medical-tech`、`modules/pharmacy` |

公共文件由当次任务负责人修改，其他成员通过独立 PR 协调：

- `backend/pom.xml`
- `his-application` 中的全局配置和 Flyway 目录
- `frontend/src/router/index.ts`
- `frontend/src/core/http.ts`
- `frontend/src/layouts/AppLayout.vue`

各前端模块已独立导出路由，常规新增页面不需要修改全局路由文件。

## 当前并行起点

数据库基线、接口契约和登录权限基线已经完成，团队可以从当前 `main` 同时开始：

| 成员 | 第一批任务 | 不应直接修改 |
| --- | --- | --- |
| A | 基础数据查询维护、挂号与收费 | 门诊、医技、药房模块内部代码 |
| B | 门诊患者队列、病历、诊断、医嘱和处方开立 | 挂号收费、医技执行和库存实现 |
| C | 医技队列与结果、药房发退药和库存 | 挂号、病历编辑实现 |

并行期间遵守以下约束：

- `docs/api/openapi.yaml` 是接口源契约。需要改字段或状态时先提交契约 PR，再写实现。
- 每张表只有架构文档指定的模块可以直接写入；跨模块动作调用应用服务，不跨模块引用 Mapper。
- 新迁移只新增 Flyway 文件，不修改已经共享的基线迁移；文件名使用各自时间戳。
- 登录、JWT、全局异常、HTTP 拦截器和布局属于平台公共能力，由一个负责人集中修改。
- 每个成员使用独立数据库或独立 schema，避免本地测试数据互相覆盖。

## 接口约定

- API 前缀统一为 `/api`。
- 返回体使用 `ApiResponse<T>`。
- 列表分页参数统一使用 `page`、`size`。
- HTTP 状态码表达请求结果，业务错误同时返回稳定的 `code`。
- 日期时间使用 ISO 8601 字符串，数据库统一按 `Asia/Shanghai` 解释。
- 状态流转在后端校验，前端隐藏按钮不能代替权限或状态检查。

## 持久层约定

每个业务模块使用相同目录结构：

```text
src/main/java/com/hospital/his/<module>/persistence/
├── mapper/                    # MyBatis Mapper 接口，只在所属模块内使用
└── model/                     # 数据库行映射和写入参数，不作为 API DTO
src/main/resources/mapper/<module>/
└── *Mapper.xml                # 查询、写入及状态条件 SQL
```

- 查询结果命名为 `*Row`，写入参数命名为 `*Draft`；API 请求和领域对象不得直接复用持久化模型。
- Mapper 接口使用 `Optional<T>` 表示单条可空结果，写操作返回受影响行数。
- 分页统一使用 `PageQuery` 和 `PageResult`，SQL 使用 `LIMIT`、`OFFSET`，列表查询必须有稳定排序。
- 新增记录需要回传主键时，`*Draft` 使用可写的 `id` 并配置 `useGeneratedKeys`。
- 状态流转 SQL 必须在 `WHERE` 中带当前状态，影响行数为 `0` 时由应用服务返回 `INVALID_STATE_TRANSITION`。
- 库存等高并发数据必须带版本条件，并在同一事务中写业务数据和流水。
- XML 中不得使用 `${}` 拼接用户输入。动态排序、表名等只能从后端固定白名单选择。
- 应用服务负责 `@Transactional` 边界，Controller 不直接注入 Mapper。

当前可复制的参考实现：

- `DepartmentMapper`：CRUD、动态筛选、分页和生成主键。
- `RegistrationMapper`：按旧状态更新挂号状态。
- `MedicalRecordMapper`：按一次就诊保存和更新病历。
- `CheckRequestMapper`：医技队列和条件状态流转。
- `DrugStockMapper`：库存非负校验与乐观锁。

## 提交前检查

```powershell
cd backend
.\mvnw.cmd verify

cd ..\frontend
npm run typecheck
npm run build
```
