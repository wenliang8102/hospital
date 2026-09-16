# HIS v1 业务契约

本文件与 `docs/api/openapi.yaml` 共同构成第一阶段开发契约。数据库列名是持久化细节，模块之间只通过 API 字段和状态码协作。

## 标识与时间

- 所有主键和外键在 API 中使用 64 位整数。
- 时间使用带时区的 ISO 8601 字符串，例如 `2026-09-16T15:30:00+08:00`。
- 金额使用十进制定点数，单位为人民币元，保留两位小数。
- 分页从 `page=1` 开始，默认 `size=20`，最大 `size=100`。

## 状态机

### 挂号 `VisitState`

```text
REGISTERED -> IN_CONSULTATION -> COMPLETED
     |
     +-> CANCELLED
```

| 当前状态 | 目标状态 | 操作方 | 前置条件 |
| --- | --- | --- | --- |
| `REGISTERED` | `IN_CONSULTATION` | 门诊 | 当前医生接诊 |
| `REGISTERED` | `CANCELLED` | 挂号收费 | 未接诊且无已缴费项目 |
| `IN_CONSULTATION` | `COMPLETED` | 门诊 | 已保存病历和最终诊断 |

### 医技申请 `MedicalOrderState`

```text
CREATED -> PAID -> ACCEPTED -> EXECUTED -> RESULT_REPORTED
   |        |
   +------> CANCELLED
            |
            +-> REFUNDED
```

- 门诊模块只创建 `CREATED` 申请和作废未缴费申请。
- 收费模块负责 `CREATED -> PAID` 与可退费状态到 `REFUNDED`。
- 医技模块负责 `PAID -> ACCEPTED -> EXECUTED -> RESULT_REPORTED`。
- `CHECK`、`INSPECTION`、`DISPOSAL` 使用相同状态机，但仍保留独立业务表。

### 处方 `PrescriptionState`

```text
CREATED -> PAID -> DISPENSED -> RETURNED
   |        |
   +------> CANCELLED
            |
            +-> REFUNDED
```

- 门诊模块创建处方。
- 收费模块确认缴费或退费。
- 药房模块发药、退药并原子更新库存流水。

### 收费明细 `ChargeItemState`

```text
UNPAID -> PAID -> REFUNDED
   |
   +-> VOID
```

支付和退款必须写入 `payment_transaction`，业务状态变化与交易流水处于同一数据库事务。

## 模块所有权

| 数据或操作 | 创建方 | 后续状态所有者 |
| --- | --- | --- |
| 挂号 | 挂号收费 | 挂号收费、门诊 |
| 病历与诊断 | 门诊 | 门诊 |
| 医技申请 | 门诊 | 收费、医技 |
| 处方 | 门诊 | 收费、药房 |
| 收费明细与支付流水 | 挂号收费 | 挂号收费 |
| 药品库存与流水 | 药房 | 药房 |

跨模块不得直接调用对方 Mapper。当前模块化单体中通过应用服务接口协作；接口稳定后可按相同边界拆分服务。

## 角色码

| 角色码 | 中文名称 | 模块权限 |
| --- | --- | --- |
| `ROOT` | 系统管理员 | 平台和所有查询，不执行门诊业务 |
| `REGISTRATION_CASHIER` | 挂号收费员 | 挂号、收费、退号、退费 |
| `OUTPATIENT_DOCTOR` | 门诊医生 | 接诊、病历、申请、处方 |
| `CHECK_DOCTOR` | 检查医生 | 检查执行和结果 |
| `INSPECTION_DOCTOR` | 检验医生 | 检验执行和结果 |
| `DISPOSAL_DOCTOR` | 处置医生 | 处置执行和结果 |
| `PHARMACY_ADMIN` | 药房管理员 | 发药、退药、库存 |

## 错误码

| 错误码 | HTTP 状态 | 含义 |
| --- | --- | --- |
| `VALIDATION_ERROR` | 400 | 参数格式或必填项错误 |
| `INVALID_STATE_TRANSITION` | 409 | 当前状态不允许执行该操作 |
| `AUTH_INVALID_CREDENTIALS` | 401 | 用户名或密码错误 |
| `AUTH_TOKEN_EXPIRED` | 401 | 登录令牌已过期 |
| `ACCESS_DENIED` | 403 | 当前角色无权限 |
| `RESOURCE_NOT_FOUND` | 404 | 业务对象不存在 |
| `DUPLICATE_RESOURCE` | 409 | 编码、流水号等唯一值重复 |
| `REGISTRATION_QUOTA_EXCEEDED` | 409 | 医生指定日期和午别的号额已满 |
| `INSUFFICIENT_STOCK` | 409 | 药品库存不足 |
| `INTERNAL_ERROR` | 500 | 未预期的服务端错误 |

## 并发约束

- 药品库存使用 `drug_stock.version` 做乐观锁，并校验扣减后数量不小于零。
- 收费和退款以交易流水号保证幂等。
- 医技申请、处方和挂号状态更新必须同时校验当前状态，避免重复执行。
- 病历以 `register_id` 唯一，一次就诊只有一份病历首页。
- 挂号请求使用客户端生成的 `requestId` 保证重复提交幂等；医生记录加锁后再校验日期、午别号额。
