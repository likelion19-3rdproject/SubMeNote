#!/bin/bash

# SNS-Service EC2 초기 설정 스크립트
# 사용법: bash setup-ec2.sh

set -e

echo "========================================="
echo "SNS-Service EC2 초기 설정을 시작합니다."
echo "========================================="

# 1. 시스템 업데이트 및 Docker 설치
echo ""
echo "[1/6] 시스템 업데이트 및 Docker 설치 중..."
sudo apt-get update
sudo apt-get upgrade -y

if ! command -v docker &> /dev/null; then
    echo "Docker 설치 중..."
    sudo apt-get install -y docker.io
    sudo systemctl enable docker
    sudo systemctl start docker
else
    echo "Docker가 이미 설치되어 있습니다."
fi

# 2. Docker Compose 설치 확인
echo ""
echo "[2/6] Docker Compose 설치 확인 중..."
if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose v1 설치 중..."
    sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
else
    echo "Docker Compose가 이미 설치되어 있습니다."
fi

# Docker Compose plugin 확인 (v2)
if ! docker compose version &> /dev/null; then
    echo "Docker Compose v2 plugin 설치 중..."
    sudo apt-get install -y docker-compose-plugin
fi

# 3. Docker 그룹에 사용자 추가
echo ""
echo "[3/6] Docker 권한 설정 중..."
if ! groups $USER | grep -q docker; then
    sudo usermod -aG docker $USER
    echo "⚠️  사용자를 docker 그룹에 추가했습니다."
    echo "⚠️  변경사항을 적용하려면 'newgrp docker'를 실행하거나 로그아웃 후 다시 로그인하세요."
else
    echo "사용자가 이미 docker 그룹에 있습니다."
fi

# 4. 프로젝트 디렉토리 생성
echo ""
echo "[4/6] 프로젝트 디렉토리 생성 중..."
DEPLOY_PATH="${HOME}/app"
mkdir -p "${DEPLOY_PATH}"
cd "${DEPLOY_PATH}"
mkdir -p front backend
echo "디렉토리 생성 완료: ${DEPLOY_PATH}"

# 5. docker-compose.prod.yml 파일 생성
echo ""
echo "[5/6] docker-compose.prod.yml 파일 생성 중..."
cat > docker-compose.prod.yml << 'EOF'
services:
  front:
    image: ${DOCKER_USERNAME}/sns-frontend:latest
    container_name: sns-frontend-prod
    ports:
      - "3000:3000"
    env_file:
      - front/.env.prod
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:3000/api/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  back:
    image: ${DOCKER_USERNAME}/sns-backend:latest
    container_name: sns-backend-prod
    ports:
      - "8080:8080"
    env_file:
      - backend/.env.prod
    depends_on:
      db:
        condition: service_healthy
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  db:
    image: mysql:8.0
    container_name: sns-mysql-prod
    env_file:
      - backend/.env.prod
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      timeout: 10s
      retries: 10
      start_period: 30s

volumes:
  mysql_data:
EOF
echo "docker-compose.prod.yml 파일 생성 완료"

# 6. 환경 변수 파일 템플릿 생성
echo ""
echo "[6/6] 환경 변수 파일 템플릿 생성 중..."

# 백엔드 환경 변수 템플릿
cat > backend/.env.prod << 'ENVEOF'
# MySQL 데이터베이스 설정
MYSQL_ROOT_PASSWORD=CHANGE_THIS_SECURE_ROOT_PASSWORD
MYSQL_DATABASE=sns_db
MYSQL_USER=sns_user
MYSQL_PASSWORD=CHANGE_THIS_SECURE_DB_PASSWORD

# Spring Boot 데이터베이스 연결
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/sns_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=sns_user
SPRING_DATASOURCE_PASSWORD=CHANGE_THIS_SECURE_DB_PASSWORD
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver

# Spring Boot JPA 설정
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true

# Toss Payments (프로덕션 키로 변경 필요)
TOSS_PAYMENTS_SECRET_KEY=test_sk_KNbdOvk5rkO5b1pjAwOArn07xlzm

# 기타 Spring Boot 설정
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
ENVEOF

# 프론트엔드 환경 변수 템플릿
EC2_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "YOUR_EC2_IP")
cat > front/.env.prod << ENVEOF
# API URL
NEXT_PUBLIC_API_URL=http://${EC2_IP}:8080

# Next.js 환경 설정
NODE_ENV=production
PORT=3000
ENVEOF

chmod 600 backend/.env.prod front/.env.prod
echo "환경 변수 파일 템플릿 생성 완료"

# 완료 메시지
echo ""
echo "========================================="
echo "✅ EC2 초기 설정이 완료되었습니다!"
echo "========================================="
echo ""
echo "📝 다음 단계:"
echo ""
echo "1. 환경 변수 파일 수정:"
echo "   cd ${DEPLOY_PATH}"
echo "   nano backend/.env.prod  # 비밀번호 변경 필수!"
echo "   nano front/.env.prod    # API URL 확인"
echo ""
echo "2. Docker 그룹 적용 (필요시):"
echo "   newgrp docker"
echo "   # 또는 로그아웃 후 다시 로그인"
echo ""
echo "3. GitHub Secrets 설정:"
echo "   - DOCKER_USERNAME"
echo "   - DOCKER_PASSWORD"
echo "   - EC2_HOST: $(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo 'IP를 확인하세요')"
echo "   - EC2_SSH_KEY: SSH 개인키 전체 내용"
echo "   - DEPLOY_PATH: ${DEPLOY_PATH}"
echo ""
echo "4. 배포 디렉토리: ${DEPLOY_PATH}"
echo ""
echo "========================================="
