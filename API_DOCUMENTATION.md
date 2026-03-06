# WebSocket 聊天室 REST API 接口文档

## 基本信息

- **基础 URL**: `http://localhost:8080`
- **API 版本**: v3.0.0
- **数据格式**: JSON
- **字符编码**: UTF-8

## 目录

1. [鉴权说明](#鉴权说明)
2. [错误响应说明](#错误响应说明)
3. [用户管理 API](#用户管理-api)
4. [房间管理 API](#房间管理-api)
5. [消息管理 API](#消息管理-api)
6. [管理员 API](#管理员-api)
7. [敏感词管理 API](#敏感词管理-api)
8. [WebSocket 连接](#websocket-连接)

---

## 鉴权说明

### JWT Token 认证

系统使用 JWT (JSON Web Token) 进行身份认证。需要管理员权限的接口必须在请求头中提供有效的 JWT Token。

### 获取 Token

首先通过登录接口获取 Token：

**登录接口**: `POST /api/auth/login`

**请求示例**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例**:
```json
{
  "message": "登录成功",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxMjM0NTY3ODkwIiwidXNlcm5hbWUiOiJhZG1pbiIsImlzQWRtaW4iOnRydWV9.xxx",
  "userId": "1234567890",
  "username": "admin",
  "isAdmin": true
}
```

### 使用 Token

在请求头中添加 Authorization 字段：

```
Authorization: Bearer <token>
```

**示例**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxMjM0NTY3ODkwIiwidXNlcm5hbWUiOiJhZG1pbiIsImlzQWRtaW4iOnRydWV9.xxx
```

### 权限说明

- **公开接口**: 无需任何权限即可访问
- **管理员接口**: 需要管理员权限，必须在请求头中提供有效的 JWT Token

### Token 有效期

- Token 默认有效期为 7 天（604800000 毫秒）
- 过期后需要重新登录获取新 Token

### 退出登录

**退出登录接口**: `POST /api/auth/logout`

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "message": "退出登录成功",
  "username": "admin"
}
```

**错误响应**:
```json
{
  "error": "Token无效或已过期"
}
```

### 错误响应

**401 Unauthorized** - 缺少或无效的 Token
```json
{
  "error": "缺少授权令牌，请在请求头中添加 Authorization: Bearer <token>"
}
```

或

```json
{
  "error": "Token无效或已过期"
}
```

**403 Forbidden** - 权限不足
```json
{
  "error": "权限不足，需要管理员权限"
}
```

---

## 用户管理 API

### 1. 注册用户

**接口地址**: `POST /api/users/register`

**权限**: 公开

**请求参数**:
```json
{
  "username": "string (必填)",
  "password": "string (可选)"
}
```

**响应示例**:
```json
{
  "message": "用户注册成功",
  "userId": "1234567890",
  "username": "testuser"
}
```

**错误响应**:
```json
{
  "error": "用户名已存在"
}
```
---

### 用户登录

首先通过登录接口获取 Token：

**登录接口**: `POST /api/auth/login`

**请求示例**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例**:
```json
{
  "message": "登录成功",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxMjM0NTY3ODkwIiwidXNlcm5hbWUiOiJhZG1pbiIsImlzQWRtaW4iOnRydWV9.xxx",
  "userId": "1234567890",
  "username": "admin",
  "isAdmin": true
}
```
---

### 退出登录

**退出登录接口**: `POST /api/auth/logout`

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "message": "退出登录成功",
  "username": "admin"
}
```

**错误响应**:
```json
{
  "error": "Token无效或已过期"
}
```

---

## 错误响应说明

### 标准错误响应格式

所有API接口在发生错误时都会返回标准化的错误响应格式：

```json
{
  "errorCode": "AUTH_001",
  "errorMessage": "缺少授权令牌",
  "errorCategory": {
    "code": "AUTH",
    "description": "认证错误"
  },
  "errorDetails": "请在请求头中添加 Authorization: Bearer <token>",
  "path": "/api/users/123",
  "timestamp": "2026-03-05T12:00:00.000"
}
```

### 错误响应字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| errorCode | String | 错误代码，唯一标识错误类型 |
| errorMessage | String | 错误消息，用户友好的错误描述 |
| errorCategory | Object | 错误分类对象 |
| errorCategory.code | String | 错误分类代码 |
| errorCategory.description | String | 错误分类描述 |
| errorDetails | String | 错误详细信息（可选） |
| path | String | 请求路径 |
| timestamp | DateTime | 错误发生时间（ISO 8601格式） |
| validationErrors | Array | 验证错误列表（验证错误时返回） |

### 错误分类

系统将错误分为以下几类：

| 错误分类 | 代码 | 说明 |
|---------|------|------|
| 认证错误 | AUTH | 用户身份认证相关错误 |
| 授权错误 | PERMISSION | 用户权限相关错误 |
| 参数错误 | PARAM | 请求参数相关错误 |
| 验证错误 | VALIDATION | 数据验证相关错误 |
| 业务错误 | BUSINESS | 业务逻辑相关错误 |
| 系统错误 | SYSTEM | 系统内部错误 |
| 资源不存在 | NOT_FOUND | 资源不存在错误 |
| 限流错误 | RATE_LIMIT | 请求频率限制错误 |

### 常见错误代码

#### 认证错误 (AUTH)

| 错误代码 | 错误消息 | HTTP状态码 |
|---------|---------|-----------|
| AUTH_001 | 缺少授权令牌 | 401 |
| AUTH_002 | Token无效或已过期 | 401 |
| AUTH_003 | Token已过期 | 401 |
| AUTH_004 | 用户名或密码错误 | 401 |
| AUTH_005 | 用户不存在 | 401 |

#### 授权错误 (PERMISSION)

| 错误代码 | 错误消息 | HTTP状态码 |
|---------|---------|-----------|
| PERM_001 | 权限不足，需要管理员权限 | 403 |
| PERM_002 | 权限不足 | 403 |

#### 参数错误 (PARAM)

| 错误代码 | 错误消息 | HTTP状态码 |
|---------|---------|-----------|
| PARAM_001 | 缺少必要参数 | 400 |
| PARAM_002 | 参数格式错误 | 400 |
| PARAM_003 | 参数类型错误 | 400 |
| PARAM_004 | 参数超出范围 | 400 |
| PARAM_005 | 参数不能为空 | 400 |

#### 验证错误 (VALIDATION)

| 错误代码 | 错误消息 | HTTP状态码 |
|---------|---------|-----------|
| VAL_001 | 数据验证失败 | 400 |
| VAL_002 | 用户名已存在 | 400 |
| VAL_003 | 房间名称已存在 | 400 |
| VAL_004 | 消息包含敏感词 | 400 |
| VAL_005 | 密码强度不足 | 400 |

#### 业务错误 (BUSINESS)

| 错误代码 | 错误消息 | HTTP状态码 |
|---------|---------|-----------|
| BIZ_001 | 用户不存在 | 400 |
| BIZ_002 | 房间不存在 | 400 |
| BIZ_003 | 房间已满 | 400 |
| BIZ_004 | 房间密码错误 | 400 |
| BIZ_005 | 房间为私有房间 | 400 |
| BIZ_006 | 消息不存在 | 400 |
| BIZ_007 | 消息已被撤回 | 400 |
| BIZ_008 | 消息撤回超时 | 400 |
| BIZ_009 | 用户已被禁言 | 400 |
| BIZ_010 | 用户已在线 | 400 |
| BIZ_011 | 用户已在房间中 | 400 |
| BIZ_012 | 用户不在房间中 | 400 |
| BIZ_013 | 敏感词已存在 | 400 |
| BIZ_014 | 敏感词不存在 | 400 |
| BIZ_015 | 操作失败 | 400 |

#### 系统错误 (SYSTEM)

| 错误代码 | 错误消息 | HTTP状态码 |
|---------|---------|-----------|
| SYS_001 | 系统内部错误 | 500 |
| SYS_002 | 数据库错误 | 500 |
| SYS_003 | 文件操作错误 | 500 |
| SYS_004 | 网络错误 | 500 |
| SYS_005 | 服务不可用 | 500 |
| SYS_006 | 请求超时 | 500 |

#### 资源不存在 (NOT_FOUND)

| 错误代码 | 错误消息 | HTTP状态码 |
|---------|---------|-----------|
| NF_001 | 资源不存在 | 404 |
| NF_002 | 接口不存在 | 404 |
| NF_003 | 请求方法不支持 | 405 |

#### 限流错误 (RATE_LIMIT)

| 错误代码 | 错误消息 | HTTP状态码 |
|---------|---------|-----------|
| RATE_001 | 请求过于频繁，请稍后再试 | 429 |

### 错误响应示例

#### 参数验证失败示例

```json
{
  "errorCode": "VAL_001",
  "errorMessage": "数据验证失败",
  "errorCategory": {
    "code": "VALIDATION",
    "description": "验证错误"
  },
  "validationErrors": [
    "username: 用户名不能为空",
    "password: 密码长度至少为6位"
  ],
  "path": "/api/users/register",
  "timestamp": "2026-03-05T12:00:00.000"
}
```

#### 认证失败示例

```json
{
  "errorCode": "AUTH_004",
  "errorMessage": "用户名或密码错误",
  "errorCategory": {
    "code": "AUTH",
    "description": "认证错误"
  },
  "path": "/api/auth/login",
  "timestamp": "2026-03-05T12:00:00.000"
}
```

#### 权限不足示例

```json
{
  "errorCode": "PERM_001",
  "errorMessage": "权限不足，需要管理员权限",
  "errorCategory": {
    "code": "PERMISSION",
    "description": "授权错误"
  },
  "path": "/api/admin/kick",
  "timestamp": "2026-03-05T12:00:00.000"
}
```

#### 业务错误示例

```json
{
  "errorCode": "BIZ_002",
  "errorMessage": "房间不存在",
  "errorCategory": {
    "code": "BUSINESS",
    "description": "业务错误"
  },
  "errorDetails": "房间ID: 1234567890",
  "path": "/api/rooms/1234567890",
  "timestamp": "2026-03-05T12:00:00.000"
}
```

#### 系统错误示例

```json
{
  "errorCode": "SYS_001",
  "errorMessage": "系统内部错误",
  "errorCategory": {
    "code": "SYSTEM",
    "description": "系统错误"
  },
  "errorDetails": "系统内部错误，请稍后重试",
  "path": "/api/messages/123",
  "timestamp": "2026-03-05T12:00:00.000"
}
```

---

## 用户管理 API

### 1. 获取用户信息（通过用户ID）

**接口地址**: `GET /api/users/{userId}`

**权限**: 公开

**路径参数**:
- `userId`: 用户ID

**响应示例**:
```json
{
  "userId": "1234567890",
  "username": "testuser",
  "isOnline": true,
  "isAdmin": false,
  "isMuted": false,
  "roomId": "9876543210"
}
```

---

### 3. 获取用户信息（通过用户名）

**接口地址**: `GET /api/users/username/{username}`

**权限**: 公开

**路径参数**:
- `username`: 用户名

**响应示例**:
```json
{
  "userId": "1234567890",
  "username": "testuser",
  "isOnline": true,
  "isAdmin": false,
  "isMuted": false
}
```

---

### 4. 获取房间内用户列表

**接口地址**: `GET /api/users/room/{roomId}`

**权限**: 公开

**路径参数**:
- `roomId`: 房间ID

**响应示例**:
```json
{
  "roomId": "9876543210",
  "userCount": 5,
  "users": [
    {
      "userId": "1234567890",
      "username": "testuser",
      "isOnline": true,
      "isAdmin": false,
      "isMuted": false
    }
  ]
}
```

---

### 5. 获取在线用户列表

**接口地址**: `GET /api/users/online`

**权限**: 公开

**响应示例**:
```json
{
  "userCount": 10,
  "users": [
    {
      "userId": "1234567890",
      "username": "testuser",
      "isAdmin": false,
      "isMuted": false
    }
  ]
}
```

---

### 6. 设置管理员状态

**接口地址**: `PUT /api/users/{userId}/admin`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `userId`: 用户ID

**请求参数**:
```json
{
  "isAdmin": true
}
```

**响应示例**:
```json
{
  "message": "管理员状态已更新",
  "userId": "1234567890",
  "username": "testuser",
  "isAdmin": true
}
```

---

### 7. 禁言用户

**接口地址**: `PUT /api/users/{userId}/mute`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `userId`: 用户ID

**请求参数**:
```json
{
  "minutes": 30
}
```

**响应示例**:
```json
{
  "message": "用户已被禁言",
  "userId": "1234567890",
  "username": "testuser",
  "isMuted": true,
  "mutedUntil": "2026-03-05T12:51:33"
}
```

---

### 8. 解除用户禁言

**接口地址**: `PUT /api/users/{userId}/unmute`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `userId`: 用户ID

**响应示例**:
```json
{
  "message": "用户禁言已解除",
  "userId": "1234567890",
  "username": "testuser",
  "isMuted": false
}
```

---

### 9. 更新用户头像

**接口地址**: `PUT /api/users/{userId}/avatar`

**权限**: 用户本人

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `userId`: 用户ID

**请求参数**:
```json
{
  "avatar": "头像URL或Base64编码"
}
```

**响应示例**:
```json
{
  "message": "头像已更新",
  "userId": "1234567890",
  "avatar": "https://example.com/avatar.jpg"
}
```

---

### 10. 更新用户个性签名

**接口地址**: `PUT /api/users/{userId}/signature`

**权限**: 用户本人

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `userId`: 用户ID

**请求参数**:
```json
{
  "signature": "个性签名内容"
}
```

**响应示例**:
```json
{
  "message": "个性签名已更新",
  "userId": "1234567890",
  "signature": "这是我的个性签名"
}
```

---

### 11. 更新用户资料

**接口地址**: `PUT /api/users/{userId}/profile`

**权限**: 用户本人

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `userId`: 用户ID

**请求参数**:
```json
{
  "avatar": "头像URL或Base64编码",
  "signature": "个性签名内容"
}
```

**响应示例**:
```json
{
  "message": "个人资料已更新",
  "userId": "1234567890",
  "avatar": "https://example.com/avatar.jpg",
  "signature": "这是我的个性签名"
}
```

---

## 房间管理 API

### 1. 创建房间

**接口地址**: `POST /api/rooms`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
  "name": "string (必填)",
  "description": "string (可选)",
  "maxUsers": 50,
  "isPrivate": false,
  "password": "string (可选，私有房间需要)"
}
```

**响应示例**:
```json
{
  "message": "房间创建成功",
  "roomId": "9876543210",
  "name": "测试房间",
  "description": "这是一个测试房间",
  "maxUsers": 50,
  "isPrivate": false,
  "isActive": true
}
```

---

### 2. 获取房间信息

**接口地址**: `GET /api/rooms/{roomId}`

**权限**: 公开

**路径参数**:
- `roomId`: 房间ID

**响应示例**:
```json
{
  "roomId": "9876543210",
  "name": "测试房间",
  "creator": "admin",
  "description": "这是一个测试房间",
  "maxUsers": 50,
  "isPrivate": false,
  "isActive": true,
  "createTime": "2026-03-05T12:00:00"
}
```

---

### 3. 获取所有房间列表

**接口地址**: `GET /api/rooms`

**权限**: 公开

**响应示例**:
```json
{
  "roomCount": 5,
  "rooms": [
    {
      "roomId": "9876543210",
      "name": "测试房间",
      "creator": "admin",
      "description": "这是一个测试房间",
      "maxUsers": 50,
      "isPrivate": false,
      "isActive": true,
      "userCount": 10
    }
  ]
}
```

---

### 4. 更新房间信息

**接口地址**: `PUT /api/rooms/{roomId}`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `roomId`: 房间ID

**请求参数**:
```json
{
  "name": "新房间名",
  "description": "新描述",
  "maxUsers": 100
}
```

**响应示例**:
```json
{
  "message": "房间更新成功",
  "roomId": "9876543210",
  "name": "新房间名",
  "description": "新描述",
  "maxUsers": 100
}
```

---

### 5. 删除房间

**接口地址**: `DELETE /api/rooms/{roomId}`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `roomId`: 房间ID

**响应示例**:
```json
{
  "message": "房间删除成功",
  "roomId": "9876543210"
}
```

---

### 6. 验证房间密码

**接口地址**: `POST /api/rooms/{roomId}/validate-password`

**权限**: 公开

**路径参数**:
- `roomId`: 房间ID

**请求参数**:
```json
{
  "password": "string"
}
```

**响应示例**:
```json
{
  "roomId": "9876543210",
  "isValid": true
}
```

---

### 7. 更新房间密码

**接口地址**: `PUT /api/rooms/{roomId}/password`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `roomId`: 房间ID

**请求参数**:
```json
{
  "oldPassword": "旧密码",
  "newPassword": "新密码"
}
```

**响应示例**:
```json
{
  "message": "密码更新成功",
  "roomId": "9876543210",
  "isPrivate": true
}
```

---

### 8. 更新房间隐私设置

**接口地址**: `PUT /api/rooms/{roomId}/privacy`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `roomId`: 房间ID

**请求参数**:
```json
{
  "isPrivate": true,
  "password": "房间密码（设为私有房间时需要）"
}
```

**响应示例**:
```json
{
  "message": "隐私设置更新成功",
  "roomId": "9876543210",
  "isPrivate": true,
  "hasPassword": true
}
```

---

## 消息管理 API

### 1. 获取消息详情

**接口地址**: `GET /api/messages/{messageId}`

**权限**: 公开

**路径参数**:
- `messageId`: 消息ID

**响应示例**:
```json
{
  "messageId": "1112223334",
  "roomId": "9876543210",
  "senderId": "1234567890",
  "senderName": "testuser",
  "content": "Hello World",
  "type": "CHAT",
  "isRecalled": false,
  "recallTime": null,
  "createTime": "2026-03-05T12:00:00"
}
```

---

### 2. 获取房间消息列表

**接口地址**: `GET /api/messages/room/{roomId}`

**权限**: 公开

**路径参数**:
- `roomId`: 房间ID

**响应示例**:
```json
{
  "roomId": "9876543210",
  "messageCount": 100,
  "messages": [
    {
      "messageId": "1112223334",
      "senderId": "1234567890",
      "senderName": "testuser",
      "content": "Hello World",
      "type": "CHAT",
      "createTime": "2026-03-05T12:00:00"
    }
  ]
}
```

---

### 3. 获取房间最近消息

**接口地址**: `GET /api/messages/room/{roomId}/recent`

**权限**: 公开

**路径参数**:
- `roomId`: 房间ID

**查询参数**:
- `limit`: 返回消息数量，默认10

**响应示例**:
```json
{
  "roomId": "9876543210",
  "limit": 10,
  "messageCount": 10,
  "messages": [
    {
      "messageId": "1112223334",
      "senderId": "1234567890",
      "senderName": "testuser",
      "content": "Hello World",
      "type": "CHAT",
      "createTime": "2026-03-05T12:00:00"
    }
  ]
}
```

---

### 4. 撤回消息

**接口地址**: `PUT /api/messages/{messageId}/recall`

**权限**: 公开（仅消息发送者）

**路径参数**:
- `messageId`: 消息ID

**请求参数**:
```json
{
  "userId": "string (必填，消息发送者ID)"
}
```

**注意**: 只能撤回自己发送的消息，且消息发送时间不超过2分钟

**响应示例**:
```json
{
  "message": "消息撤回成功",
  "messageId": "1112223334",
  "isRecalled": true,
  "recallTime": "2026-03-05T12:05:00"
}
```

---

### 5. 搜索消息

**接口地址**: `GET /api/messages/search`

**权限**: 公开

**查询参数**:
- `keyword`: 搜索关键词（必填）
- `roomId`: 房间ID（可选）
- `senderId`: 发送者ID（可选）
- `limit`: 返回结果数量限制（可选，默认50）

**响应示例**:
```json
{
  "keyword": "搜索关键词",
  "roomId": "9876543210",
  "senderId": "1234567890",
  "totalCount": 100,
  "returnedCount": 50,
  "limit": 50,
  "messages": [
    {
      "messageId": "1112223334",
      "roomId": "9876543210",
      "senderId": "1234567890",
      "senderName": "testuser",
      "content": "包含关键词的消息内容",
      "type": "CHAT",
      "createTime": "2026-03-05T12:00:00"
    }
  ]
}
```

---

## 公告管理 API

### 1. 创建公告

**接口地址**: `POST /api/announcements`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
  "title": "string (必填)",
  "content": "string (必填)",
  "creatorId": "string (必填)",
  "creatorName": "string (可选)",
  "priority": 1,
  "isActive": true,
  "startTime": "2026-03-05T12:00:00",
  "endTime": "2026-03-06T12:00:00"
}
```

**参数说明**:
- `title`: 公告标题（必填）
- `content`: 公告内容（必填）
- `creatorId`: 创建者ID（必填）
- `creatorName`: 创建者用户名（可选）
- `priority`: 优先级，数字越大优先级越高（可选，默认1）
- `isActive`: 是否启用（可选，默认true）
- `startTime`: 开始时间（可选，为空则立即生效）
- `endTime`: 结束时间（可选，为空则永久有效）

**响应示例**:
```json
{
  "message": "公告创建成功",
  "announcementId": "1112223334",
  "title": "系统维护通知",
  "content": "系统将于今晚进行维护",
  "priority": 1,
  "isActive": true,
  "createTime": "2026-03-05T12:00:00"
}
```

---

### 2. 获取公告详情

**接口地址**: `GET /api/announcements/{announcementId}`

**权限**: 公开

**路径参数**:
- `announcementId`: 公告ID

**响应示例**:
```json
{
  "announcementId": "1112223334",
  "title": "系统维护通知",
  "content": "系统将于今晚进行维护",
  "creatorId": "1234567890",
  "creatorName": "admin",
  "priority": 1,
  "isActive": true,
  "startTime": "2026-03-05T12:00:00",
  "endTime": "2026-03-06T12:00:00",
  "createTime": "2026-03-05T12:00:00",
  "updateTime": "2026-03-05T12:00:00"
}
```

---

### 3. 获取所有公告

**接口地址**: `GET /api/announcements`

**权限**: 公开

**响应示例**:
```json
{
  "announcementCount": 5,
  "announcements": [
    {
      "announcementId": "1112223334",
      "title": "系统维护通知",
      "content": "系统将于今晚进行维护",
      "creatorId": "1234567890",
      "creatorName": "admin",
      "priority": 1,
      "isActive": true,
      "startTime": "2026-03-05T12:00:00",
      "endTime": "2026-03-06T12:00:00",
      "createTime": "2026-03-05T12:00:00",
      "updateTime": "2026-03-05T12:00:00"
    }
  ]
}
```

---

### 4. 获取活跃公告

**接口地址**: `GET /api/announcements/active`

**权限**: 公开

**说明**: 返回当前时间范围内且已启用的公告

**响应示例**:
```json
{
  "announcementCount": 3,
  "announcements": [
    {
      "announcementId": "1112223334",
      "title": "系统维护通知",
      "content": "系统将于今晚进行维护",
      "creatorId": "1234567890",
      "creatorName": "admin",
      "priority": 1,
      "startTime": "2026-03-05T12:00:00",
      "endTime": "2026-03-06T12:00:00",
      "createTime": "2026-03-05T12:00:00"
    }
  ]
}
```

---

### 5. 按优先级获取活跃公告

**接口地址**: `GET /api/announcements/active/priority/{minPriority}`

**权限**: 公开

**路径参数**:
- `minPriority`: 最小优先级

**说明**: 返回优先级大于等于指定值且当前时间范围内已启用的公告

**响应示例**:
```json
{
  "minPriority": 2,
  "announcementCount": 2,
  "announcements": [
    {
      "announcementId": "1112223334",
      "title": "重要通知",
      "content": "这是重要通知内容",
      "creatorId": "1234567890",
      "creatorName": "admin",
      "priority": 2,
      "startTime": "2026-03-05T12:00:00",
      "endTime": "2026-03-06T12:00:00",
      "createTime": "2026-03-05T12:00:00"
    }
  ]
}
```

---

### 6. 获取创建者的公告

**接口地址**: `GET /api/announcements/creator/{creatorId}`

**权限**: 公开

**路径参数**:
- `creatorId`: 创建者ID

**响应示例**:
```json
{
  "creatorId": "1234567890",
  "announcementCount": 10,
  "announcements": [
    {
      "announcementId": "1112223334",
      "title": "系统维护通知",
      "content": "系统将于今晚进行维护",
      "priority": 1,
      "isActive": true,
      "startTime": "2026-03-05T12:00:00",
      "endTime": "2026-03-06T12:00:00",
      "createTime": "2026-03-05T12:00:00",
      "updateTime": "2026-03-05T12:00:00"
    }
  ]
}
```

---

### 7. 搜索公告

**接口地址**: `GET /api/announcements/search`

**权限**: 公开

**查询参数**:
- `keyword`: 搜索关键词（必填）

**响应示例**:
```json
{
  "keyword": "维护",
  "announcementCount": 5,
  "announcements": [
    {
      "announcementId": "1112223334",
      "title": "系统维护通知",
      "content": "系统将于今晚进行维护",
      "creatorId": "1234567890",
      "creatorName": "admin",
      "priority": 1,
      "isActive": true,
      "startTime": "2026-03-05T12:00:00",
      "endTime": "2026-03-06T12:00:00",
      "createTime": "2026-03-05T12:00:00"
    }
  ]
}
```

---

### 8. 更新公告

**接口地址**: `PUT /api/announcements/{announcementId}`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `announcementId`: 公告ID

**请求参数**:
```json
{
  "title": "新标题",
  "content": "新内容",
  "priority": 2,
  "isActive": false,
  "startTime": "2026-03-05T12:00:00",
  "endTime": "2026-03-06T12:00:00"
}
```

**说明**: 所有参数都是可选的，只更新提供的字段

**响应示例**:
```json
{
  "message": "公告更新成功",
  "announcementId": "1112223334",
  "title": "新标题",
  "content": "新内容",
  "priority": 2,
  "isActive": false,
  "startTime": "2026-03-05T12:00:00",
  "endTime": "2026-03-06T12:00:00",
  "updateTime": "2026-03-05T12:00:00"
}
```

---

### 9. 切换公告状态

**接口地址**: `PUT /api/announcements/{announcementId}/toggle`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `announcementId`: 公告ID

**说明**: 切换公告的启用/禁用状态

**响应示例**:
```json
{
  "message": "公告状态切换成功",
  "announcementId": "1112223334",
  "isActive": false
}
```

---

### 10. 删除公告

**接口地址**: `DELETE /api/announcements/{announcementId}`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `announcementId`: 公告ID

**响应示例**:
```json
{
  "message": "公告删除成功",
  "announcementId": "1112223334"
}
```

---

## 管理员 API

### 1. 踢出用户

**接口地址**: `POST /api/admin/kick`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
  "userId": "string (必填)"
}
```

**响应示例**:
```json
{
  "message": "用户已被踢出",
  "userId": "1234567890",
  "username": "testuser"
}
```

---

### 2. 禁言用户

**接口地址**: `POST /api/admin/mute`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
  "userId": "string (必填)",
  "minutes": 30
}
```

**响应示例**:
```json
{
  "message": "用户已被禁言",
  "userId": "1234567890",
  "username": "testuser",
  "isMuted": true,
  "mutedUntil": "2026-03-05T12:51:33"
}
```

---

### 3. 解除用户禁言

**接口地址**: `POST /api/admin/unmute`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
  "userId": "string (必填)"
}
```

**响应示例**:
```json
{
  "message": "用户禁言已解除",
  "userId": "1234567890",
  "username": "testuser",
  "isMuted": false
}
```

---

### 4. 设置管理员权限

**接口地址**: `POST /api/admin/set-admin`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
  "userId": "string (必填)",
  "isAdmin": true
}
```

**响应示例**:
```json
{
  "message": "管理员权限已设置",
  "userId": "1234567890",
  "username": "testuser",
  "isAdmin": true
}
```

---

### 5. 删除房间

**接口地址**: `DELETE /api/admin/room/{roomId}`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `roomId`: 房间ID

**响应示例**:
```json
{
  "message": "房间已删除",
  "roomId": "9876543210"
}
```

---

### 6. 获取系统统计信息

**接口地址**: `GET /api/admin/stats`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "totalUsers": 20,
  "onlineUsers": 10,
  "totalMessages": 1500
}
```

---

## 敏感词管理 API

**说明**: 敏感词存储在文本文件中，文件位置：`{工作目录}/sensitive_words.txt`，每行一个敏感词，使用UTF-8编码。系统会每60秒自动从文件重新加载敏感词，无需手动重载。

### 1. 获取所有敏感词

**接口地址**: `GET /api/sensitive-words`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "count": 5,
  "words": ["敏感词1", "敏感词2", "敏感词3", "敏感词4", "敏感词5"],
  "filePath": "C:\\Users\\Administrator\\Desktop\\ws-room\\sensitive_words.txt"
}
```

---

### 2. 添加敏感词

**接口地址**: `POST /api/sensitive-words`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
  "word": "string (必填)"
}
```

**响应示例**:
```json
{
  "message": "敏感词添加成功",
  "word": "新敏感词",
  "total": 6
}
```

**错误响应**:
```json
{
  "error": "敏感词已存在或添加失败",
  "word": "新敏感词"
}
```

---

### 3. 删除敏感词

**接口地址**: `DELETE /api/sensitive-words/{word}`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
- `word`: 敏感词

**响应示例**:
```json
{
  "message": "敏感词删除成功",
  "word": "敏感词",
  "total": 4
}
```

**错误响应**:
```json
{
  "error": "敏感词不存在或删除失败",
  "word": "敏感词"
}
```

---

### 4. 批量导入敏感词

**接口地址**: `POST /api/sensitive-words/batch`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
  "words": ["敏感词1", "敏感词2", "敏感词3"]
}
```

**响应示例**:
```json
{
  "message": "批量导入敏感词成功",
  "imported": 3,
  "total": 8
}
```

**错误响应**:
```json
{
  "error": "批量导入敏感词失败"
}
```

---

### 5. 检测文本敏感词

**接口地址**: `POST /api/sensitive-words/check`

**权限**: 公开

**请求参数**:
```json
{
  "text": "string (必填)"
}
```

**响应示例**:
```json
{
  "contains": true,
  "detectedWords": ["敏感词1", "敏感词2"],
  "detectedCount": 2,
  "filteredText": "***包含***敏感词",
  "originalText": "这是包含敏感词1和敏感词2的文本"
}
```

---

### 6. 过滤文本敏感词

**接口地址**: `POST /api/sensitive-words/filter`

**权限**: 公开

**请求参数**:
```json
{
  "text": "string (必填)",
  "replaceChar": "*"
}
```

**响应示例**:
```json
{
  "filteredText": "这是***和***的文本",
  "originalText": "这是敏感词1和敏感词2的文本"
}
```

---

### 7. 获取敏感词文件信息

**接口地址**: `GET /api/sensitive-words/info`

**权限**: 管理员

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
  "filePath": "C:\\Users\\Administrator\\Desktop\\ws-room\\sensitive_words.txt",
  "wordCount": 5
}
```

---

## WebSocket 连接

### 连接地址

**WebSocket URL**: `ws://localhost:8080/ws/chat`

### 连接参数

#### Token 认证

- **token**: JWT Token（通过URL参数传递）
  - 示例: `ws://localhost:8080/ws/chat?token=eyJhbGciOiJIUzI1NiJ9...`

### 连接示例

#### JavaScript 示例（Token认证）
```javascript
const token = 'eyJhbGciOiJIUzI1NiJ9...';
const ws = new WebSocket(`ws://localhost:8080/ws/chat?token=${encodeURIComponent(token)}`);

ws.onopen = function() {
    console.log('WebSocket连接已建立');
};

ws.onmessage = function(event) {
    const message = JSON.parse(event.data);
    console.log('收到消息:', message);
};

ws.onerror = function(error) {
    console.error('WebSocket错误:', error);
};

ws.onclose = function() {
    console.log('WebSocket连接已关闭');
};
```

---

### 消息类型

### 1. JOIN - 加入房间

加入指定的聊天房间。

#### 请求格式
```json
{
  "type": "JOIN",
  "roomId": "9876543210",
  "content": "房间密码（私有房间需要）"
}
```

#### 参数说明
- `type`: 固定为 "JOIN"
- `roomId`: 房间ID（必填）
- `content`: 房间密码（私有房间必填，公开房间可选）

#### 响应格式
```json
{
  "type": "JOIN",
  "content": "成功加入房间: 测试房间",
  "roomId": "9876543210",
  "timestamp": 1709625600000
}
```

加入房间后会收到最近10条历史消息。

---

### 2. LEAVE - 离开房间

离开当前所在的房间。

#### 请求格式
```json
{
  "type": "LEAVE"
}
```

---

### 3. CHAT - 发送聊天消息

向当前房间发送聊天消息。

#### 请求格式
```json
{
  "type": "CHAT",
  "content": "Hello World"
}
```

#### 参数说明
- `type`: 固定为 "CHAT"
- `content`: 消息内容（必填）

**注意**: 消息会经过敏感词检测，包含敏感词的消息会被拒绝。

#### 错误响应
```json
{
  "type": "ERROR",
  "content": "消息包含敏感词: 敏感词1, 敏感词2"
}
```

---

### 4. CREATE_ROOM - 创建房间（管理员）

创建新的聊天房间（仅管理员可用）。

#### 请求格式
```json
{
  "type": "CREATE_ROOM",
  "content": "房间名称",
  "description": "房间描述",
  "maxUsers": 50,
  "isPrivate": false,
  "password": "房间密码（私有房间需要）"
}
```

---

### 5. DELETE_ROOM - 删除房间（管理员）

删除指定的聊天房间（仅管理员可用）。

#### 请求格式
```json
{
  "type": "DELETE_ROOM",
  "roomId": "9876543210"
}
```

---

### 6. ROOM_LIST - 获取房间列表

获取所有活跃的房间列表。

#### 请求格式
```json
{
  "type": "ROOM_LIST"
}
```

---

### 7. USER_LIST - 获取用户列表

获取用户列表，可以指定房间或获取所有在线用户。

#### 请求格式
```json
{
  "type": "USER_LIST",
  "roomId": "9876543210"
}
```

---

### 8. KICK - 踢出用户（管理员）

将指定用户踢出房间（仅管理员可用）。

#### 请求格式
```json
{
  "type": "KICK",
  "targetUserId": "1234567890"
}
```

---

### 9. MUTE - 禁言用户（管理员）

禁言指定用户（仅管理员可用）。

#### 请求格式
```json
{
  "type": "MUTE",
  "targetUserId": "1234567890",
  "muteMinutes": 30
}
```

---

### 10. UNMUTE - 解除禁言（管理员）

解除指定用户的禁言（仅管理员可用）。

#### 请求格式
```json
{
  "type": "UNMUTE",
  "targetUserId": "1234567890"
}
```

---

### 11. RECALL - 撤回消息

撤回之前发送的消息（仅消息发送者可用，且消息发送时间不超过2分钟）。

#### 请求格式
```json
{
  "type": "RECALL",
  "messageId": "1112223334"
}
```

---

### 12. ADMIN - 设置管理员权限（管理员）

设置或取消用户的管理员权限（仅管理员可用）。

#### 请求格式
```json
{
  "type": "ADMIN",
  "targetUserId": "1234567890",
  "isAdmin": true
}
```

---

### 13. CHECK_SENSITIVE - 检测敏感词

检测文本中是否包含敏感词，并返回检测到的敏感词列表和过滤后的文本。

#### 请求格式
```json
{
  "type": "CHECK_SENSITIVE",
  "content": "这是包含敏感词的文本"
}
```

#### 响应格式
```json
{
  "type": "CHECK_SENSITIVE",
  "content": "这是包含敏感词的文本",
  "contains": true,
  "detectedWords": ["敏感词1", "敏感词2"],
  "filteredText": "这是包含***的文本",
  "timestamp": 1709625600000
}
```

---

### 14. FILTER_SENSITIVE - 过滤敏感词

过滤文本中的敏感词，返回过滤后的文本。

#### 请求格式
```json
{
  "type": "FILTER_SENSITIVE",
  "content": "这是包含敏感词的文本"
}
```

#### 响应格式
```json
{
  "type": "FILTER_SENSITIVE",
  "content": "这是包含敏感词的文本",
  "filteredText": "这是包含***的文本",
  "timestamp": 1709625600000
}
```

---

### 15. PRIVATE_MESSAGE - 发送私聊消息

向指定用户发送私聊消息（点对点消息）。

#### 请求格式
```json
{
  "type": "PRIVATE_MESSAGE",
  "content": "私聊消息内容",
  "targetUserId": "1234567890"
}
```

#### 响应格式
```json
{
  "type": "PRIVATE_MESSAGE",
  "content": "私聊消息内容",
  "sender": "发送者用户名",
  "targetUserId": "1234567890",
  "messageId": "1112223334",
  "timestamp": 1709625600000,
  "isRecalled": false
}
```

#### 说明
- 私聊消息只发送给发送者和接收者
- 消息会经过敏感词检测
- 被禁言的用户无法发送私聊消息
- 接收者必须在线才能收到私聊消息

---

### 16. MESSAGE_READ - 标记消息为已读

标记指定的消息为已读状态。

#### 请求格式
```json
{
  "type": "MESSAGE_READ",
  "messageId": "1112223334",
  "roomId": "9876543210"
}
```

#### 响应格式
```json
{
  "type": "MESSAGE_READ",
  "messageId": "1112223334",
  "content": "消息已标记为已读",
  "roomId": "9876543210",
  "timestamp": 1709625600000
}
```

---

### 17. MESSAGE_READ_RECEIPT - 获取消息已读回执

获取指定消息的已读回执，显示有多少人已阅读该消息。

#### 请求格式
```json
{
  "type": "MESSAGE_READ_RECEIPT",
  "messageId": "1112223334",
  "roomId": "9876543210"
}
```

#### 响应格式
```json
{
  "type": "MESSAGE_READ_RECEIPT",
  "messageId": "1112223334",
  "content": "消息已读回执",
  "roomId": "9876543210",
  "readCount": 5,
  "timestamp": 1709625600000
}
```

---

### 18. UPDATE_ROOM - 更新房间信息（管理员）

更新房间的名称、描述和最大用户数（仅管理员可用）。

#### 请求格式
```json
{
  "type": "UPDATE_ROOM",
  "roomId": "9876543210",
  "content": "新的房间名称",
  "description": "新的房间描述",
  "maxUsers": 100
}
```

#### 响应格式
```json
{
  "type": "SYSTEM",
  "content": "房间更新成功",
  "timestamp": 1709625600000
}
```

---

### 19. UPDATE_PASSWORD - 更新房间密码（管理员）

更新私有房间的密码（仅管理员可用）。

#### 请求格式
```json
{
  "type": "UPDATE_PASSWORD",
  "roomId": "9876543210",
  "password": "旧密码",
  "content": "新密码"
}
```

#### 响应格式
```json
{
  "type": "SYSTEM",
  "content": "房间密码更新成功",
  "timestamp": 1709625600000
}
```

---

### 20. UPDATE_PRIVACY - 更新房间隐私设置（管理员）

更新房间的隐私设置，可以将公开房间改为私有房间，或反之（仅管理员可用）。

#### 请求格式
```json
{
  "type": "UPDATE_PRIVACY",
  "roomId": "9876543210",
  "isPrivate": true,
  "password": "房间密码（设为私有房间时需要）"
}
```

#### 响应格式
```json
{
  "type": "SYSTEM",
  "content": "房间隐私设置更新成功",
  "timestamp": 1709625600000
}
```

---

### 21. SEARCH_MESSAGES - 搜索消息

根据关键词搜索消息，支持按房间、发送者等条件筛选。

#### 请求格式
```json
{
  "type": "SEARCH_MESSAGES",
  "content": "搜索关键词",
  "roomId": "9876543210",
  "sender": "发送者用户名",
  "maxUsers": 50
}
```

#### 参数说明
- `content`: 搜索关键词（必填）
- `roomId`: 房间ID（可选，指定房间搜索）
- `sender`: 发送者用户名（可选，指定发送者搜索）
- `maxUsers`: 返回结果数量限制（可选，默认50）

#### 响应格式
```json
{
  "type": "SEARCH_MESSAGES",
  "content": "搜索结果",
  "roomId": "9876543210",
  "sender": "发送者用户名",
  "searchResults": [
    {
      "type": "CHAT",
      "content": "消息内容",
      "sender": "发送者用户名",
      "roomId": "9876543210",
      "messageId": "1112223334",
      "timestamp": 1709625600000,
      "isRecalled": false
    }
  ],
  "timestamp": 1709625600000
}
```

---

### 22. ANNOUNCEMENT - 获取公告详情

获取指定公告的详细信息。

#### 请求格式
```json
{
  "type": "ANNOUNCEMENT",
  "announcementId": "1112223334"
}
```

#### 参数说明
- `announcementId`: 公告ID（必填）

#### 响应格式
```json
{
  "type": "ANNOUNCEMENT",
  "content": "公告标题",
  "description": "公告内容",
  "sender": "创建者用户名",
  "messageId": "1112223334",
  "isAdmin": true,
  "timestamp": 1709625600000
}
```

---

### 23. ANNOUNCEMENT_LIST - 获取公告列表

获取活跃公告列表，支持按优先级筛选。

#### 请求格式
```json
{
  "type": "ANNOUNCEMENT_LIST",
  "priority": 1
}
```

#### 参数说明
- `priority`: 最小优先级（可选，不指定则返回所有活跃公告）

#### 响应格式
```json
{
  "type": "ANNOUNCEMENT_LIST",
  "content": "公告列表",
  "searchResults": [
    {
      "type": "ANNOUNCEMENT",
      "content": "公告标题",
      "description": "公告内容",
      "sender": "创建者用户名",
      "messageId": "1112223334",
      "isAdmin": true,
      "timestamp": 1709625600000
    }
  ],
  "timestamp": 1709625600000
}
```

---

### 24. CREATE_ANNOUNCEMENT - 创建公告（管理员）

创建新的公告（仅管理员可用）。

#### 请求格式
```json
{
  "type": "CREATE_ANNOUNCEMENT",
  "content": "公告标题",
  "description": "公告内容",
  "priority": 1
}
```

#### 参数说明
- `content`: 公告标题（必填）
- `description`: 公告内容（必填）
- `priority`: 优先级（可选，默认1）

#### 响应格式
```json
{
  "type": "CREATE_ANNOUNCEMENT",
  "content": "公告创建成功",
  "messageId": "1112223334",
  "timestamp": 1709625600000
}
```

#### 说明
- 只有管理员可以创建公告
- 创建成功后，公告会广播给所有在线用户

---

### 25. UPDATE_ANNOUNCEMENT - 更新公告（管理员）

更新已存在的公告（仅管理员可用）。

#### 请求格式
```json
{
  "type": "UPDATE_ANNOUNCEMENT",
  "announcementId": "1112223334",
  "content": "新标题",
  "description": "新内容",
  "priority": 2,
  "isAdmin": false
}
```

#### 参数说明
- `announcementId`: 公告ID（必填）
- `content`: 新标题（可选）
- `description`: 新内容（可选）
- `priority`: 新优先级（可选）
- `isAdmin`: 是否启用（可选）

#### 响应格式
```json
{
  "type": "UPDATE_ANNOUNCEMENT",
  "content": "公告更新成功",
  "messageId": "1112223334",
  "timestamp": 1709625600000
}
```

---

### 26. DELETE_ANNOUNCEMENT - 删除公告（管理员）

删除指定的公告（仅管理员可用）。

#### 请求格式
```json
{
  "type": "DELETE_ANNOUNCEMENT",
  "announcementId": "1112223334"
}
```

#### 参数说明
- `announcementId`: 公告ID（必填）

#### 响应格式
```json
{
  "type": "DELETE_ANNOUNCEMENT",
  "content": "公告删除成功",
  "messageId": "1112223334",
  "timestamp": 1709625600000
}
```

---

## 错误码说明

| HTTP 状态码 | 说明 |
|------------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（缺少或无效的 Token） |
| 403 | 禁止访问（权限不足） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 默认账户

系统初始化时会创建以下默认账户：

### 管理员账户
- **用户名**: admin
- **密码**: a1653611988
- **权限**: 管理员

---

## 注意事项

1. **时间格式**: 所有时间字段使用 ISO 8601 格式（如：2026-03-05T12:00:00）
2. **ID 格式**: 所有ID为纯数字字符串，使用UUID生成的数字ID
3. **JWT Token**: 管理员接口需要在请求头中添加 `Authorization: Bearer <token>`，且该用户必须具有管理员权限
4. **Token 有效期**: Token 默认有效期为 7 天，过期后需要重新登录
5. **消息撤回**: 只能撤回自己发送的消息，且消息发送时间不超过2分钟
6. **私有房间**: 加入私有房间需要提供正确的密码
7. **权限控制**: 管理员权限通过 `@AdminRequired` 注解控制，拦截器会自动验证 JWT Token
8. **敏感词检测**: 所有聊天消息都会经过敏感词检测，包含敏感词的消息会被拒绝
9. **历史消息**: 加入房间后会收到最近10条历史消息
10. **WebSocket认证**: 推荐使用Token认证方式连接WebSocket
11. **敏感词自动重载**: 系统每60秒自动从文件重新加载敏感词，修改文件后无需手动重载
12. **WebSocket敏感词检测**: 支持通过WebSocket协议进行敏感词检测（CHECK_SENSITIVE和FILTER_SENSITIVE消息类型），功能与REST API一致
13. **用户头像和个性签名**: 用户可以设置头像和个性签名，这些信息会在用户列表中显示
14. **消息已读状态**: 支持标记消息为已读，并获取消息已读回执
15. **私聊功能**: 支持点对点私聊消息，私聊消息只发送给发送者和接收者
16. **房间编辑**: 管理员可以编辑房间名称、描述和最大用户数
17. **房间密码修改**: 管理员可以修改私有房间的密码
18. **房间隐私设置**: 管理员可以更改房间为公开或私有
19. **消息搜索**: 支持根据关键词搜索消息，可按房间、发送者等条件筛选
20. **公告管理**: 管理员可以创建、更新、删除公告，支持优先级和时间范围设置
21. **公告广播**: 创建公告后会自动广播给所有在线用户
22. **公告优先级**: 公告按优先级排序，数字越大优先级越高
23. **公告时间范围**: 公告可以设置开始时间和结束时间，只在指定时间范围内显示

---

## 更新日志

### v5.0.0 (2026-03-06)
- 添加远程公告管理功能
- 新增Announcement实体和Repository
- 新增AnnouncementService服务
- 新增AnnouncementController控制器
- 支持创建、更新、删除公告
- 支持公告优先级设置
- 支持公告时间范围控制（开始时间、结束时间）
- 支持公告启用/禁用状态切换
- 添加公告搜索功能
- 添加WebSocket公告消息类型（ANNOUNCEMENT、ANNOUNCEMENT_LIST、CREATE_ANNOUNCEMENT、UPDATE_ANNOUNCEMENT、DELETE_ANNOUNCEMENT）
- 支持通过WebSocket获取公告列表和详情
- 支持管理员通过WebSocket创建、更新、删除公告
- 新增公告创建成功后广播给所有在线用户
- 添加10个公告管理REST API接口

### v4.0.0 (2026-03-06)
- 添加用户头像和个性签名功能
- 添加消息已读状态功能
- 添加私聊功能（点对点消息）
- 添加房间编辑和密码修改功能
- 添加消息搜索功能
- 新增MessageReadStatus实体和Repository
- 新增PrivateMessage实体和Repository
- 新增MessageReadStatusService服务
- 新增PrivateMessageService服务
- 扩展WebSocket消息类型支持新功能
- 更新用户信息包含头像和个性签名字段
- 添加用户资料更新API接口
- 添加房间密码和隐私设置更新API接口
- 添加消息搜索API接口
- 完善错误处理和日志记录

### v3.0.0 (2026-03-05)
- 实现统一的错误处理机制
- 创建标准化的错误响应模型（ErrorResponse）
- 定义完整的错误代码枚举和错误分类标准
- 实现全局异常处理器（GlobalExceptionHandler）
- 创建错误日志记录服务（ErrorLogService）
- 所有错误信息持久化存储到数据库
- 添加错误日志定时清理任务（保留30天）
- 更新API文档，添加详细的错误响应说明

### v2.0.0 (2026-03-05)
- 添加 WebSocket Token 认证支持
- 实现敏感词检测功能（KMP/TRIE/AC三种算法）
- 修改ID生成方式为纯数字UUID
- 修改历史消息数量从50条改为10条
- 添加敏感词管理API
- 优化WebSocket连接认证流程
- 实现敏感词文件存储机制（sensitive_words.txt）
- 添加敏感词定时自动重载功能（每60秒）
- 添加WebSocket敏感词检测接口（CHECK_SENSITIVE和FILTER_SENSITIVE）

### v1.2.0 (2026-03-05)
- 添加 JWT Token 认证机制
- 创建登录接口获取 Token
- 更新拦截器支持 JWT 验证
- 所有管理员接口使用 JWT Token 鉴权
- 添加 Token 验证接口
- 更新 API 文档，添加 JWT 使用说明

### v2.2.0 (2026-03-05)
- Token有效期从24小时延长至7天
- 新增退出登录接口
- 更新API文档，添加退出登录说明

### v2.1.0 (2026-03-05)
- 移除用户名认证方式，仅支持Token认证
- 优化WebSocket连接认证流程

### v1.1.0 (2026-03-05)
- 添加管理员鉴权功能
- 删除多余接口
- 优化 API 结构
- 添加请求头鉴权机制
- 更新错误处理

### v1.0.0 (2026-03-05)
- 初始版本发布
- 实现用户管理功能
- 实现房间管理功能
- 实现消息管理功能
- 实现管理员功能
- 实现 WebSocket 实时通信
