# ws-room API 文档（基于代码实现重写）

> 文档依据源码控制器与拦截器整理（`src/main/java/com/chat/controller`、`com/chat/interceptor/AuthInterceptor`、`com/chat/config/WebMvcConfig`）。
> 
> 本文档仅描述 **HTTP REST API**。WebSocket 协议请参考仓库根目录下的 `WEBSOCKET_DOCUMENTATION.md`。

---

## 1. 基本信息

- 服务默认地址：`http://localhost:8080`
- REST 基础路径：`/api`
- 默认数据格式：`application/json`
- WebSocket 连接地址：`ws://localhost:8080/ws/chat`

---

## 2. 鉴权与权限模型

### 2.1 Token 获取

通过登录接口获取 JWT：

- `POST /api/auth/login`

### 2.2 Token 传递方式

在请求头中传递：

```http
Authorization: Bearer <token>
```

### 2.3 权限控制规则

- 仅被 `@AdminRequired` 标注的方法会被 `AuthInterceptor` 拦截并校验管理员权限。
- 已配置拦截路径（`WebMvcConfig`）：
  - `/api/admin/**`
  - `/api/users/*/admin`
  - `/api/users/*/mute`
  - `/api/users/*/unmute`
- 其他接口（例如用户更新自己资料）虽然读取了 `request.userId`，但不会自动拦截认证，调用方需要自行保证请求链路已注入该属性。

---

## 3. 响应格式说明

### 3.1 成功响应

多数接口返回：

```json
{
  "message": "操作成功",
  "...": "业务字段"
}
```

### 3.2 错误响应（当前项目存在两类）

1) 统一异常处理器返回（主要用于 auth 异常等）：

```json
{
  "errorCode": "AUTH_001",
  "errorMessage": "认证失败",
  "errorCategory": "AUTHENTICATION_ERROR",
  "errorDetails": "详细信息",
  "path": "/api/auth/validate",
  "timestamp": "2026-03-09T10:00:00"
}
```

2) 控制器内部 `try/catch` 直接返回：

```json
{
  "error": "具体错误信息"
}
```

### 3.3 常见 HTTP 状态码

- `200 OK`：查询/更新成功
- `201 Created`：创建成功（如用户注册、创建房间）
- `400 Bad Request`：参数错误/业务校验失败
- `401 Unauthorized`：未登录或 Token 无效（管理员拦截与部分 auth 接口）
- `403 Forbidden`：权限不足（管理员接口）
- `404 Not Found`：资源不存在
- `405 Method Not Allowed`：请求方法不支持

---

## 4. 数据对象（摘要）

### 4.1 User（接口常见字段）

| 字段 | 类型 | 说明 |
|---|---|---|
| userId | string | 业务用户 ID |
| username | string | 用户名 |
| isOnline | boolean | 是否在线 |
| isAdmin | boolean | 是否管理员 |
| isMuted | boolean | 是否禁言 |
| roomId | string | 当前所在房间（可空） |
| avatar | string | 头像 URL |
| signature | string | 个性签名 |
| mutedUntil | string(datetime) | 禁言结束时间（可空） |

### 4.2 Room

| 字段 | 类型 | 说明 |
|---|---|---|
| roomId | string | 房间 ID |
| name | string | 房间名称 |
| creator | string | 创建者 userId |
| description | string | 描述 |
| maxUsers | integer | 最大人数 |
| isPrivate | boolean | 是否私密房间 |
| isActive | boolean | 是否启用 |
| createTime | string(datetime) | 创建时间 |
| userCount | integer | 当前人数（列表接口附带） |

### 4.3 Message

| 字段 | 类型 | 说明 |
|---|---|---|
| messageId | string | 消息 ID |
| roomId | string | 房间 ID |
| senderId | string | 发送者 ID |
| senderName | string | 发送者名称 |
| content | string | 内容 |
| type | string | 消息类型（CHAT/SYSTEM/...） |
| isRecalled | boolean | 是否撤回 |
| recallTime | string(datetime) | 撤回时间（可空） |
| createTime | string(datetime) | 创建时间 |

### 4.4 Announcement

| 字段 | 类型 | 说明 |
|---|---|---|
| announcementId | string | 公告 ID |
| title | string | 标题 |
| content | string | 内容 |
| creatorId | string | 创建者 ID |
| creatorName | string | 创建者名称 |
| priority | integer | 优先级 |
| isActive | boolean | 是否激活 |
| startTime | string(datetime) | 生效开始时间（可空） |
| endTime | string(datetime) | 生效结束时间（可空） |
| createTime | string(datetime) | 创建时间 |
| updateTime | string(datetime) | 更新时间 |

---

## 5. 认证模块（`/api/auth`）

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/api/auth/login` | 否 | 用户登录 |
| GET | `/api/auth/login` | 否 | 登录用法提示（返回 405） |
| POST | `/api/auth/validate` | Bearer Token | 校验 Token |
| POST | `/api/auth/logout` | Bearer Token | 退出登录（使该用户所有 token 失效） |

### 5.1 登录

`POST /api/auth/login`

请求体：

```json
{
  "username": "admin",
  "password": "a1653611988"
}
```

成功响应：

```json
{
  "message": "登录成功",
  "token": "<jwt>",
  "userId": "1001",
  "username": "admin",
  "isAdmin": true
}
```

### 5.2 校验 Token

`POST /api/auth/validate`

请求头：`Authorization: Bearer <token>`

成功响应：

```json
{
  "valid": true,
  "userId": "1001",
  "username": "admin",
  "isAdmin": true
}
```

### 5.3 退出登录

`POST /api/auth/logout`

请求头：`Authorization: Bearer <token>`

成功响应：

```json
{
  "message": "退出登录成功",
  "username": "admin"
}
```

---

## 6. 用户模块（`/api/users`）

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/users` | 否 | 获取全部用户 |
| POST | `/api/users/register` | 否 | 注册用户 |
| GET | `/api/users/{userId}` | 否 | 按 userId 获取用户 |
| GET | `/api/users/username/{username}` | 否 | 按用户名获取用户 |
| GET | `/api/users/room/{roomId}` | 否 | 获取房间用户列表 |
| GET | `/api/users/online` | 否 | 获取在线用户 |
| PUT | `/api/users/{userId}/admin` | 管理员 | 设置管理员状态 |
| PUT | `/api/users/{userId}/mute` | 管理员 | 禁言用户 |
| PUT | `/api/users/{userId}/unmute` | 管理员 | 解除禁言 |
| PUT | `/api/users/{userId}/avatar` | 依赖 request.userId | 修改头像（仅本人） |
| PUT | `/api/users/{userId}/signature` | 依赖 request.userId | 修改签名（仅本人） |
| PUT | `/api/users/{userId}/profile` | 依赖 request.userId | 修改资料（仅本人） |

### 6.1 注册用户

`POST /api/users/register`

```json
{
  "username": "tom",
  "password": "123456"
}
```

成功（201）：

```json
{
  "message": "用户注册成功",
  "userId": "1002",
  "username": "tom",
  "avatar": "https://...",
  "signature": "这个人很懒，还没有签名"
}
```

### 6.2 设置管理员

`PUT /api/users/{userId}/admin`

```json
{
  "isAdmin": true
}
```

### 6.3 禁言/解除禁言

- `PUT /api/users/{userId}/mute`
  - 请求体：`{"minutes": 30}`（缺省或 `<=0` 时默认 30）
- `PUT /api/users/{userId}/unmute`
  - 无需请求体

---

## 7. 房间模块（`/api/rooms`）

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/api/rooms` | 管理员 | 创建房间 |
| GET | `/api/rooms/{roomId}` | 否 | 获取房间详情 |
| GET | `/api/rooms` | 否 | 获取房间列表 |
| PUT | `/api/rooms/{roomId}` | 管理员 | 更新房间信息 |
| DELETE | `/api/rooms/{roomId}` | 管理员 | 删除房间 |
| POST | `/api/rooms/{roomId}/validate-password` | 否 | 校验房间密码 |
| PUT | `/api/rooms/{roomId}/password` | 管理员 | 更新房间密码 |
| PUT | `/api/rooms/{roomId}/privacy` | 管理员 | 更新隐私设置 |

### 7.1 创建房间

`POST /api/rooms`

```json
{
  "name": "技术讨论",
  "description": "Java / Spring",
  "maxUsers": 200,
  "isPrivate": true,
  "password": "123456"
}
```

成功（201）：

```json
{
  "message": "房间创建成功",
  "roomId": "2001",
  "name": "技术讨论",
  "description": "Java / Spring",
  "maxUsers": 200,
  "isPrivate": true,
  "isActive": true
}
```

### 7.2 校验密码

`POST /api/rooms/{roomId}/validate-password`

```json
{
  "password": "123456"
}
```

响应：

```json
{
  "roomId": "2001",
  "isValid": true
}
```

---

## 8. 消息模块（`/api/messages`）

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/messages/{messageId}` | 否 | 获取消息详情 |
| GET | `/api/messages/room/{roomId}` | 否 | 获取房间全部消息 |
| GET | `/api/messages/room/{roomId}/recent?limit=10` | 否 | 获取最近消息 |
| PUT | `/api/messages/{messageId}/recall` | 否 | 撤回消息（请求体需 userId） |
| GET | `/api/messages/search` | 否 | 搜索消息 |

### 8.1 撤回消息

`PUT /api/messages/{messageId}/recall`

```json
{
  "userId": "1002"
}
```

成功响应：

```json
{
  "message": "消息撤回成功",
  "messageId": "3001",
  "isRecalled": true,
  "recallTime": "2026-03-09T10:00:00"
}
```

### 8.2 搜索消息

`GET /api/messages/search?keyword=hello&roomId=2001&senderId=1002&limit=50`

查询参数：

- `keyword`：必填
- `roomId`：可选
- `senderId`：可选
- `limit`：可选，默认 50

---

## 9. 公告模块（`/api/announcements`）

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/api/announcements` | 管理员 | 创建公告 |
| GET | `/api/announcements/{announcementId}` | 否 | 公告详情 |
| GET | `/api/announcements` | 否 | 全部公告 |
| GET | `/api/announcements/active` | 否 | 当前有效公告 |
| GET | `/api/announcements/active/priority/{minPriority}` | 否 | 按最低优先级筛选有效公告 |
| GET | `/api/announcements/creator/{creatorId}` | 否 | 查询创建者公告 |
| GET | `/api/announcements/search?keyword=...` | 否 | 搜索公告 |
| PUT | `/api/announcements/{announcementId}` | 管理员 | 更新公告 |
| PUT | `/api/announcements/{announcementId}/toggle` | 管理员 | 切换激活状态 |
| DELETE | `/api/announcements/{announcementId}` | 管理员 | 删除公告 |

### 9.1 创建公告

`POST /api/announcements`

```json
{
  "title": "系统维护通知",
  "content": "今晚 23:00-23:30 升级",
  "creatorId": "1001",
  "creatorName": "admin",
  "priority": 3,
  "isActive": true,
  "startTime": "2026-03-09T23:00:00",
  "endTime": "2026-03-09T23:30:00"
}
```

> `startTime` / `endTime` 需为 ISO-8601 本地时间格式（`LocalDateTime.parse` 默认格式），例如 `yyyy-MM-ddTHH:mm:ss`。
> 说明：`LocalDateTime` 不包含时区信息，服务端按运行环境 JVM 的系统默认时区解释传入值。

---

## 10. 管理员模块（`/api/admin`）

> 本模块所有接口均要求管理员权限（`@AdminRequired`）。

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/admin/kick` | 踢出用户 |
| POST | `/api/admin/mute` | 禁言用户 |
| POST | `/api/admin/unmute` | 解除禁言 |
| POST | `/api/admin/set-admin` | 设置管理员权限 |
| DELETE | `/api/admin/room/{roomId}` | 删除房间 |
| GET | `/api/admin/stats` | 获取系统统计 |

请求体示例：

- `POST /api/admin/kick`

```json
{ "userId": "1002" }
```

- `POST /api/admin/mute`

```json
{ "userId": "1002", "minutes": 30 }
```

- `POST /api/admin/set-admin`

```json
{ "userId": "1002", "isAdmin": true }
```

`GET /api/admin/stats` 成功响应：

```json
{
  "totalUsers": 12,
  "onlineUsers": 5,
  "totalMessages": 325
}
```

---

## 11. 敏感词模块（`/api/sensitive-words`）

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/api/sensitive-words` | 管理员 | 获取敏感词列表 |
| POST | `/api/sensitive-words` | 管理员 | 添加敏感词 |
| DELETE | `/api/sensitive-words/{word}` | 管理员 | 删除敏感词 |
| POST | `/api/sensitive-words/batch` | 管理员 | 批量导入 |
| POST | `/api/sensitive-words/check` | 否 | 检测文本是否含敏感词 |
| POST | `/api/sensitive-words/filter` | 否 | 过滤文本敏感词 |
| GET | `/api/sensitive-words/info` | 管理员 | 获取敏感词文件信息 |

### 11.1 添加敏感词

`POST /api/sensitive-words`

```json
{ "word": "违禁词" }
```

### 11.2 批量导入

`POST /api/sensitive-words/batch`

```json
{
  "words": ["词1", "词2", "词3"]
}
```

### 11.3 检测文本

`POST /api/sensitive-words/check`

```json
{ "text": "这是一段测试文本" }
```

响应示例：

```json
{
  "contains": false,
  "detectedWords": [],
  "detectedCount": 0,
  "filteredText": "这是一段测试文本",
  "originalText": "这是一段测试文本"
}
```

### 11.4 过滤文本

`POST /api/sensitive-words/filter`

```json
{
  "text": "包含违禁词的文本",
  "replaceChar": "*"
}
```

---

## 12. 默认管理员账户（首次初始化数据库）

当数据库无用户数据时，会自动创建：

- 用户名：`admin`
- 密码：`a1653611988`

> 来源：`DatabaseInitializer`。

---

## 13. 与源码一致性的说明

1. 文档按当前代码行为编写，不是理想化 OpenAPI 契约。
2. 部分接口（尤其用户“仅本人可修改”的接口）依赖 `request` 中的 `userId` 属性，若未经过注入链路可能返回 403。
3. 错误响应目前存在两套格式（`error` 与 `errorCode/errorMessage`），调用方需兼容。
4. WebSocket 消息类型与字段较多，请以专门文档 `WEBSOCKET_DOCUMENTATION.md` 为准。
