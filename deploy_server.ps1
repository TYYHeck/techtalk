# TechTalk 一键部署到阿里云 ECS
# 使用方法：PowerShell 中运行此脚本

$server = "8.138.24.27"
$user = "root"
$deployDir = "g:\Documents\简历\techtalk\deploy"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TechTalk 一键部署到 ECS" -ForegroundColor Cyan
Write-Host "  目标: $user@$server" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 步骤1：上传部署包
Write-Host "`n[1/4] 上传部署文件到服务器..." -ForegroundColor Yellow
scp -o StrictHostKeyChecking=no -r "$deployDir/*" "$user@$server:/opt/techtalk/"
if ($LASTEXITCODE -ne 0) {
    Write-Host "上传失败，请检查 SSH 连接" -ForegroundColor Red
    exit 1
}
Write-Host "上传完成!" -ForegroundColor Green

# 步骤2：检查并安装 Docker
Write-Host "`n[2/4] 检查服务器环境..." -ForegroundColor Yellow
$dockerCheck = ssh -o StrictHostKeyChecking=no "$user@$server" 'docker --version 2>/dev/null && echo "DOCKER_OK" || echo "NEED_DOCKER"'
if ($dockerCheck -match "NEED_DOCKER") {
    Write-Host "正在安装 Docker..." -ForegroundColor Yellow
    ssh -o StrictHostKeyChecking=no "$user@$server" @"
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
systemctl enable docker
systemctl start docker
"@
    Write-Host "Docker 安装完成!" -ForegroundColor Green
} else {
    Write-Host "Docker 已安装" -ForegroundColor Green
}

# 步骤3：切换到部署目录并启动服务
Write-Host "`n[3/4] 启动全部服务（MySQL + Redis + 后端 + Nginx）..." -ForegroundColor Yellow
ssh -o StrictHostKeyChecking=no "$user@$server" @"
cd /opt/techtalk
docker compose down 2>/dev/null
docker compose up -d
echo '---'
docker compose ps
"@

# 步骤4：等待启动完成
Write-Host "`n[4/4] 等待服务就绪..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  部署完成!" -ForegroundColor Green
Write-Host "  前端页面: http://8.138.24.27" -ForegroundColor Green
Write-Host "  API 文档: http://8.138.24.27:8080/swagger-ui.html" -ForegroundColor Green
Write-Host "  管理员: admin / admin123" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

# 查看服务状态
Write-Host "`n服务状态：" -ForegroundColor Yellow
ssh -o StrictHostKeyChecking=no "$user@$server" "cd /opt/techtalk && docker compose ps"
