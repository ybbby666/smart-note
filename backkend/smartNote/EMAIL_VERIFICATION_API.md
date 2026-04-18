# 邮箱验证码功能 API 文档

## 概述

Smart Note 现已支持通过邮箱验证码进行注册、登录和找回密码功能。

## 配置说明

### 1. 邮箱配置

在 `application.yml` 中配置你的邮箱信息：

```yaml
spring:
  mail:
    host: smtp.qq.com  # QQ邮箱SMTP服务器
    port: 587
    username: your-email@qq.com  # 替换为你的邮箱
    password: your-authorization-code  # 替换为邮箱授权码
    protocol: smtp
    default-encoding: UTF-8
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

**获取邮箱授权码：**
- **QQ邮箱**: 设置 -> 账户 -> POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务 -> 开启服务 -> 生成授权码
- **163邮箱**: 设置 -> POP3/SMTP/IMAP -> 开启服务 -> 客户端授权密码
- **Gmail**: 需要开启两步验证并生成应用专用密码

### 2. Redis 配置

确保 Redis 已启动，并在 `application.yml` 中配置：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:  # 如果有密码则填写
      database: 0
```

## API 接口

### 1. 发送验证码

**接口地址**: `POST /users/send-code`

**请求参数**:
```json
{
  "email": "user@example.com",
  "type": "register"  // register-注册, login-登录, reset-找回密码
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": null
}
```

**说明**:
- 验证码为6位数字
- 有效期5分钟
- 同一邮箱同一类型验证码60秒内只能发送一次

---

### 2. 邮箱验证码注册

**接口地址**: `POST /users/email-register`

**请求参数**:
```json
{
  "username": "张三",
  "email": "user@example.com",
  "verificationCode": "123456",
  "password": "123456",
  "confirmPassword": "123456"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

**说明**:
- 必须先调用发送验证码接口
- 验证码使用后会自动失效
- 密码长度6-16位

---

### 3. 邮箱验证码登录

**接口地址**: `POST /users/email-login`

**请求参数**:
```json
{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "eyJhbGciOiJIUzI1NiJ9..."  // JWT Token
}
```

**说明**:
- 无需密码，只需邮箱和验证码
- 验证码使用后会自动失效
- 返回的 Token 用于后续请求的身份验证

---

### 4. 邮箱验证码重置密码

**接口地址**: `POST /users/reset-password`

**请求参数**:
```json
{
  "email": "user@example.com",
  "verificationCode": "123456",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

**说明**:
- 适用于忘记密码的场景
- 验证码使用后会自动失效
- 密码长度6-16位

---

## 使用流程示例

### 注册流程

```bash
# 1. 发送注册验证码
curl -X POST http://localhost:8080/users/send-code \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","type":"register"}'

# 2. 收到验证码后，完成注册
curl -X POST http://localhost:8080/users/email-register \
  -H "Content-Type: application/json" \
  -d '{
    "username":"张三",
    "email":"user@example.com",
    "verificationCode":"123456",
    "password":"123456",
    "confirmPassword":"123456"
  }'
```

### 登录流程

```bash
# 1. 发送登录验证码
curl -X POST http://localhost:8080/users/send-code \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","type":"login"}'

# 2. 收到验证码后，完成登录
curl -X POST http://localhost:8080/users/email-login \
  -H "Content-Type: application/json" \
  -d '{
    "email":"user@example.com",
    "verificationCode":"123456"
  }'
```

### 找回密码流程

```bash
# 1. 发送重置密码验证码
curl -X POST http://localhost:8080/users/send-code \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","type":"reset"}'

# 2. 收到验证码后，重置密码
curl -X POST http://localhost:8080/users/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email":"user@example.com",
    "verificationCode":"123456",
    "newPassword":"newpassword123",
    "confirmPassword":"newpassword123"
  }'
```

---

## 安全特性

1. **验证码有效期**: 5分钟后自动过期
2. **发送频率限制**: 同一邮箱60秒内只能发送一次
3. **一次性使用**: 验证码验证成功后立即删除
4. **类型隔离**: 注册、登录、重置密码的验证码互不影响
5. **密码加密**: 使用 BCrypt 加密存储
6. **Redis 存储**: 验证码存储在 Redis，自动过期清理

---

## 错误码说明

| 错误信息 | 说明 |
|---------|------|
| 邮箱格式不正确 | 邮箱地址格式错误 |
| 验证码类型不能为空 | type 参数必须为 register/login/reset |
| 发送过于频繁，请XX秒后再试 | 60秒内重复发送 |
| 验证码错误或已过期 | 验证码不正确或已过期 |
| 该邮箱已被注册 | 注册时邮箱已存在 |
| 该邮箱未注册，请先注册 | 登录时邮箱不存在 |
| 两次密码不一致 | 密码和确认密码不匹配 |
| 邮件发送失败，请稍后重试 | 邮箱配置错误或网络问题 |

---

## 常见问题

### Q1: 收不到验证码邮件？

**检查清单**:
1. 确认邮箱配置正确（host、username、password）
2. 确认使用的是授权码而非登录密码
3. 检查垃圾邮件文件夹
4. 确认网络连接正常
5. 查看后台日志是否有错误信息

### Q2: Redis 连接失败？

**解决方案**:
1. 确认 Redis 服务已启动
2. 检查 Redis 配置（host、port、password）
3. 测试 Redis 连接：`redis-cli ping`

### Q3: 如何更换邮箱服务商？

修改 `application.yml` 中的配置：

```yaml
# 163邮箱
spring:
  mail:
    host: smtp.163.com
    port: 587
    username: your-email@163.com
    password: your-authorization-code

# Gmail
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

### Q4: 如何调整验证码有效期？

修改 `VerificationCodeServiceImpl.java` 中的常量：

```java
private static final int CODE_EXPIRE_MINUTES = 5;  // 改为需要的分钟数
```

### Q5: 如何调整发送间隔？

修改 `VerificationCodeServiceImpl.java` 中的常量：

```java
private static final int SEND_INTERVAL_SECONDS = 60;  // 改为需要的秒数
```

---

## 技术实现

- **邮件发送**: Spring Boot Mail (JavaMailSender)
- **验证码存储**: Redis (StringRedisTemplate)
- **密码加密**: BCrypt (Hutool)
- **Token 生成**: JWT (JJWT)
- **参数校验**: Spring Validation

---

## 注意事项

1. ⚠️ **生产环境务必使用 HTTPS**
2. ⚠️ **不要将邮箱授权码提交到代码仓库**
3. ⚠️ **建议使用环境变量管理敏感配置**
4. ⚠️ **定期监控邮件发送日志**
5. ⚠️ **考虑添加图形验证码防止机器刷接口**
