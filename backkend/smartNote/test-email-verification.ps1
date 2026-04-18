# 邮箱验证码功能测试脚本 (PowerShell版本)
# 使用前请确保服务已启动在 http://localhost:8080

Write-Host "========== 测试1: 发送注册验证码 ==========" -ForegroundColor Cyan
$body1 = @{
    email = "test@example.com"
    type = "register"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/users/send-code" -Method Post -Body $body1 -ContentType "application/json"
Write-Host ""

$REGISTER_CODE = Read-Host "请输入收到的注册验证码"

Write-Host ""
Write-Host "========== 测试2: 邮箱验证码注册 ==========" -ForegroundColor Cyan
$body2 = @{
    username = "测试用户"
    email = "test@example.com"
    verificationCode = $REGISTER_CODE
    password = "123456"
    confirmPassword = "123456"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/users/email-register" -Method Post -Body $body2 -ContentType "application/json"
Write-Host ""

Write-Host ""
Write-Host "========== 测试3: 发送登录验证码 ==========" -ForegroundColor Cyan
$body3 = @{
    email = "test@example.com"
    type = "login"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/users/send-code" -Method Post -Body $body3 -ContentType "application/json"
Write-Host ""

$LOGIN_CODE = Read-Host "请输入收到的登录验证码"

Write-Host ""
Write-Host "========== 测试4: 邮箱验证码登录 ==========" -ForegroundColor Cyan
$body4 = @{
    email = "test@example.com"
    verificationCode = $LOGIN_CODE
} | ConvertTo-Json

$LOGIN_RESPONSE = Invoke-RestMethod -Uri "http://localhost:8080/users/email-login" -Method Post -Body $body4 -ContentType "application/json"
Write-Host "登录响应: " -NoNewline
$LOGIN_RESPONSE | ConvertTo-Json
Write-Host ""

if ($LOGIN_RESPONSE.data) {
    Write-Host "获取到的 Token: $($LOGIN_RESPONSE.data)" -ForegroundColor Green
}
Write-Host ""

Write-Host ""
Write-Host "========== 测试5: 发送重置密码验证码 ==========" -ForegroundColor Cyan
$body5 = @{
    email = "test@example.com"
    type = "reset"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/users/send-code" -Method Post -Body $body5 -ContentType "application/json"
Write-Host ""

$RESET_CODE = Read-Host "请输入收到的重置密码验证码"

Write-Host ""
Write-Host "========== 测试6: 邮箱验证码重置密码 ==========" -ForegroundColor Cyan
$body6 = @{
    email = "test@example.com"
    verificationCode = $RESET_CODE
    newPassword = "newpass123"
    confirmPassword = "newpass123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/users/reset-password" -Method Post -Body $body6 -ContentType "application/json"
Write-Host ""

Write-Host ""
Write-Host "========== 所有测试完成 ==========" -ForegroundColor Green
