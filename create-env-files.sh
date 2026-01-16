#!/bin/bash

# EC2에서 환경 변수 파일 생성 스크립트
# 사용법: bash create-env-files.sh [DEPLOY_PATH]

set -e

DEPLOY_PATH="${1:-$HOME/app}"

echo "========================================="
echo "환경 변수 파일 생성 스크립트"
echo "========================================="
echo ""
echo "배포 경로: $DEPLOY_PATH"
echo ""

# 디렉토리 생성
mkdir -p "$DEPLOY_PATH/front" "$DEPLOY_PATH/backend"
cd "$DEPLOY_PATH"

# EC2 IP 가져오기
EC2_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "")

if [ -z "$EC2_IP" ]; then
  read -p "EC2 Public IP를 입력하세요: " EC2_IP
fi

echo ""
echo "[1/2] 백엔드 환경 변수 파일 생성 중..."
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

echo "✓ backend/.env.prod 파일 생성 완료"
echo ""
echo "⚠️  중요: backend/.env.prod 파일의 비밀번호를 변경해주세요!"
echo "   - MYSQL_ROOT_PASSWORD"
echo "   - MYSQL_PASSWORD"
echo "   - SPRING_DATASOURCE_PASSWORD"
echo ""

echo "[2/2] 프론트엔드 환경 변수 파일 생성 중..."
cat > front/.env.prod << EOF
# API URL
NEXT_PUBLIC_API_URL=http://${EC2_IP}:8080

# Next.js 환경 설정
NODE_ENV=production
PORT=3000
EOF

echo "✓ front/.env.prod 파일 생성 완료"
echo ""

# 파일 권한 설정
chmod 600 backend/.env.prod front/.env.prod
echo "✓ 파일 권한 설정 완료 (600)"
echo ""

echo "========================================="
echo "✅ 환경 변수 파일 생성 완료!"
echo "========================================="
echo ""
echo "📝 다음 단계:"
echo ""
echo "1. backend/.env.prod 파일 수정 (비밀번호 변경 필수):"
echo "   nano $DEPLOY_PATH/backend/.env.prod"
echo ""
echo "2. front/.env.prod 파일 확인 (API URL 확인):"
echo "   nano $DEPLOY_PATH/front/.env.prod"
echo ""
echo "3. 파일 내용 확인:"
echo "   ls -la $DEPLOY_PATH/backend/.env.prod"
echo "   ls -la $DEPLOY_PATH/front/.env.prod"
echo ""
echo "이제 GitHub Actions 배포를 다시 실행할 수 있습니다!"
echo ""
