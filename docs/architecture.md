# 系统架构与模块边界

## 架构原则

当前采用前后端分离的模块化单体。所有后端业务模块由一个 Spring Boot 应用加载并部署，共享一个 MySQL 数据库，但代码、接口和表的所有权按模块划分。

该结构适合三人团队：本地运行和事务处理简单，同时保留未来独立拆分医技或药房服务的空间。

## 后端模块

| Maven 模块 | Java 包 | 职责 | 主要数据所有权 |
| --- | --- | --- | --- |
| `his-common` | `common` | 统一响应、异常、通用类型 | 无业务表 |
| `his-platform` | `platform` | 登录、权限、用户、角色、菜单、日志 | `sys_user`、`sys_role`、`sys_permission`、用户角色与角色权限表、`operation_log` |
| `his-master-data` | `masterdata` | 基础数据查询和维护 | `department`、`employee`、`disease`、`medical_technology`、`drug_info`、`regist_level`、`scheduling`、`settle_category` |
| `his-registration` | `registration` | 挂号、退号、收费、退费 | `register`、`charge_item`、`payment_transaction` |
| `his-outpatient` | `outpatient` | 病历、诊断、申请开立、处方开立 | `medical_record`、`medical_record_disease`、申请与处方的开立规则 |
| `his-medical-tech` | `medicaltech` | 检查、检验、处置执行与结果 | `check_request`、`inspection_request`、`disposal_request` 的执行字段 |
| `his-pharmacy` | `pharmacy` | 发药、退药、库存 | `prescription` 的发药字段、`drug_stock`、`drug_stock_transaction` |
| `his-application` | `application` | 启动、组装、配置和数据库迁移 | Flyway 迁移脚本 |

申请表和处方表跨越“开立”和“执行”两个阶段。数据表归属以状态流转责任划分：门诊模块创建申请，医技或药房模块执行；任何状态变化必须经过所属模块的应用服务，其他模块不得直接更新其字段。

## 依赖规则

```text
his-application
  -> 所有业务模块

业务模块
  -> his-common

his-common
  -> 不依赖任何业务模块
```

业务模块之间默认不添加 Maven 依赖。跨模块协作先通过模块公开的应用接口完成；需要解除编译期耦合时再引入领域事件，不在项目初期增加消息队列。

## 前端边界

`frontend/src/modules` 按业务模块分目录，每个模块自行导出路由。公共布局、HTTP 客户端和登录状态放在 `src/core`、`src/layouts` 和 `src/stores`。新增业务页面应放入对应模块，不直接堆放到全局 `views`。

## 数据库迁移

所有结构变化通过 `his-application/src/main/resources/db/migration` 下的 Flyway 文件提交。原始 SQL 含 `DROP TABLE` 和演示数据，只作为数据字典参考，不能直接在共享环境执行。

迁移版本使用时间戳以减少多人冲突，例如：

```text
V202609161430__create_register_table.sql
```
