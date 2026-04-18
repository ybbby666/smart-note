# 邮箱验证码功能测试脚本
# 使用前请确保服务已启动在 http://localhost:8080

# ==================== 测试1: 发送注册验证码 ====================
echo "========== 测试1: 发送注册验证码 =========="
curl -X POST http://localhost:8080/users/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "type": "register"
  }'
echo ""
echo ""

# 等待用户输入收到的验证码
read -p "请输入收到的注册验证码: " REGISTER_CODE

# ==================== 测试2: 邮箱验证码注册 ====================
echo "========== 测试2: 邮箱验证码注册 =========="
curl -X POST http://localhost:8080/users/email-register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"测试用户\",
    \"email\": \"test@example.com\",
    \"verificationCode\": \"$REGISTER_CODE\",
    \"password\": \"123456\",
    \"confirmPassword\": \"123456\"
  }"
echo ""
echo ""

# ==================== 测试3: 发送登录验证码 ====================
echo "========== 测试3: 发送登录验证码 =========="
curl -X POST http://localhost:8080/users/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "type": "login"
  }'
echo ""
echo ""

# 等待用户输入收到的验证码
read -p "请输入收到的登录验证码: " LOGIN_CODE

# ==================== 测试4: 邮箱验证码登录 ====================
echo "========== 测试4: 邮箱验证码登录 =========="
LOGIN_RESPONSE=$(curl -X POST http://localhost:8080/users/email-login \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"test@example.com\",
    \"verificationCode\": \"$LOGIN_CODE\"
  }")
echo "$LOGIN_RESPONSE"
echo ""
echo ""

# 提取 Token (需要安装 jq: brew install jq 或 apt-get install jq)
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data')
echo "获取到的 Token: $TOKEN"
echo ""

# ==================== 测试5: 发送重置密码验证码 ====================
echo "========== 测试5: 发送重置密码验证码 =========="
curl -X POST http://localhost:8080/users/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "type": "reset"
  }'
echo ""
echo ""

# 等待用户输入收到的验证码
read -p "请输入收到的重置密码验证码: " RESET_CODE

# ==================== 测试6: 邮箱验证码重置密码 ====================
echo "========== 测试6: 邮箱验证码重置密码 =========="
curl -X POST http://localhost:8080/users/reset-password \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"test@example.com\",
    \"verificationCode\": \"$RESET_CODE\",
    \"newPassword\": \"newpass123\",
    \"confirmPassword\": \"newpass123\"
  }"
echo ""
echo ""

echo "========== 所有测试完成 =========="
