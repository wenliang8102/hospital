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

## 提交前检查

```powershell
cd backend
.\mvnw.cmd verify

cd ..\frontend
npm run typecheck
npm run build
```
