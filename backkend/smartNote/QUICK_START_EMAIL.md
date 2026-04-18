# 邮箱验证码功能快速启动指南

## 📋 前置要求

1. ✅ Java 17+
2. ✅ Maven 3.6+
3. ✅ MySQL 5.7+
4. ✅ Redis 5.0+
5. ✅ 一个可用的邮箱账号（QQ邮箱/163邮箱/Gmail等）

---

## 🚀 快速开始

### 步骤1: 启动 Redis

**Windows:**
```bash
# 如果已安装 Redis，直接启动
redis-server

# 或者使用 Docker
docker run -d -p 6379:6379 redis:latest
```

**Linux/Mac:**
```bash
# 启动 Redis
sudo systemctl start redis

# 或者使用 Docker
docker run -d -p 6379:6379 redis:latest
```

验证 Redis 是否启动成功：
```bash
redis-cli ping
# 应返回: PONG
```

---

### 步骤2: 配置邮箱

#### 2.1 获取邮箱授权码

**QQ邮箱:**
1. 登录 QQ 邮箱网页版
2. 点击「设置」→「账户」
3. 找到「POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务」
4. 开启「IMAP/SMTP服务」
5. 点击「生成授权码」，按提示操作
6. 复制生成的授权码（16位字符串）

**163邮箱:**
1. 登录 163 邮箱网页版
2. 点击「设置」→「POP3/SMTP/IMAP」
3. 开启「IMAP/SMTP服务」
4. 点击「客户端授权密码」
5. 设置并复制授权码

**Gmail:**
1. 开启两步验证
2. 生成应用专用密码
3. 使用该密码作为授权码

#### 2.2 修改配置文件

打开 `src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  mail:
    host: smtp.qq.com  # 根据你的邮箱修改
    port: 587
    username: your-email@qq.com  # ⚠️ 替换为你的邮箱地址
    password: your-authorization-code  # ⚠️ 替换为你的授权码
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

**常见邮箱 SMTP 配置:**

| 邮箱 | Host | Port |
|------|------|------|
| QQ邮箱 | smtp.qq.com | 587 |
| 163邮箱 | smtp.163.com | 587 |
| Gmail | smtp.gmail.com | 587 |
| Outlook | smtp.office365.com | 587 |

---

### 步骤3: 配置 Redis（可选）

如果 Redis 有密码或不在默认端口，修改 `application.yml`:

```yaml
spring:
  data:
    redis:
      host: localhost  # Redis 地址
      port: 6379       # Redis 端口
      password: your-redis-password  # 如果有密码
      database: 0
```

---

### 步骤4: 编译项目

```bash
cd smartNote
mvn clean compile
```

---

### 步骤5: 启动应用

```bash
mvn spring-boot:run
```

或者打包后运行：
```bash
mvn clean package
java -jar target/smartNote-0.0.1-SNAPSHOT.jar
```

看到以下日志表示启动成功：
```
Started SmartNoteApplication in X.XXX seconds
```

---

## 🧪 测试功能

### 方式1: 使用 PowerShell 测试脚本（推荐 Windows 用户）

```powershell
.\test-email-verification.ps1
```

按提示输入收到的验证码即可。

### 方式2: 使用 Bash 测试脚本（推荐 Mac/Linux 用户）

```bash
chmod +x test-email-verification.sh
./test-email-verification.sh
```

### 方式3: 使用 Postman/Apifox

参考 `EMAIL_VERIFICATION_API.md` 中的 API 文档进行测试。

### 方式4: 手动测试

**1. 发送注册验证码:**
```bash
curl -X POST http://localhost:8080/users/send-code \
  -H "Content-Type: application/json" \
  -d '{"email":"your-email@example.com","type":"register"}'
```

**2. 查看邮箱，收到验证码后完成注册:**
```bash
curl -X POST http://localhost:8080/users/email-register \
  -H "Content-Type: application/json" \
  -d '{
    "username":"测试用户",
    "email":"your-email@example.com",
    "verificationCode":"123456",
    "password":"123456",
    "confirmPassword":"123456"
  }'
```

---

## ❓ 常见问题

### 问题1: 收不到邮件

**检查清单:**
- [ ] 邮箱配置是否正确（host、username、password）
- [ ] 是否使用的是授权码而非登录密码
- [ ] 垃圾邮件文件夹
- [ ] 网络连接是否正常
- [ ] 查看后台日志错误信息

**查看日志:**
```bash
tail -f logs/app.log
```

### 问题2: Redis 连接失败

**解决方案:**
```bash
# 检查 Redis 是否启动
redis-cli ping

# 如果没有安装 Redis，使用 Docker
docker run -d -p 6379:6379 redis:latest
```

### 问题3: 端口被占用

**修改端口:**
在 `application.yml` 中添加：
```yaml
server:
  port: 8081  # 改为其他端口
```

### 问题4: 数据库连接失败

**检查 MySQL:**
```bash
# 确认 MySQL 已启动
mysql -u root -p

# 确认数据库存在
CREATE DATABASE IF NOT EXISTS smart_note CHARACTER SET utf8mb4;
```

---

## 🔐 安全建议

1. **不要将授权码提交到 Git**
   - 使用环境变量或配置中心管理敏感信息
   
2. **生产环境使用 HTTPS**
   - 配置 SSL 证书
   
3. **添加图形验证码**
   - 防止机器批量刷接口
   
4. **限制每日发送次数**
   - 同一邮箱每日最多发送10次
   
5. **监控异常行为**
   - 记录并分析邮件发送日志

---

## 📊 功能特性

✅ **验证码有效期**: 5分钟  
✅ **发送频率限制**: 60秒/次  
✅ **一次性使用**: 验证后立即失效  
✅ **类型隔离**: 注册/登录/重置互不影响  
✅ **密码加密**: BCrypt 加密存储  
✅ **自动清理**: Redis 自动过期删除  

---

## 📞 技术支持

如有问题，请查看：
1. 应用日志: `logs/app.log`
2. API 文档: `EMAIL_VERIFICATION_API.md`
3. 测试脚本: `test-email-verification.ps1` 或 `test-email-verification.sh`

---

## 🎉 完成！

现在你可以使用邮箱验证码进行：
- ✨ 新用户注册
- 🔑 免密登录
- 🔄 找回密码

享受便捷安全的用户体验吧！
