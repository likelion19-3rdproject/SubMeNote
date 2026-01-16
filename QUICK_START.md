# 🚀 빠른 시작 가이드

## EC2에서 실행할 명령어 모음

### 1️⃣ EC2 초기 설정 (한 번만 실행)

```bash
# 방법 1: 스크립트 사용 (권장)
curl -o setup-ec2.sh https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPO/main/setup-ec2.sh
# 또는 로컬에서 스크립트를 EC2로 복사한 후:
bash setup-ec2.sh

# 방법 2: 수동 설치
sudo apt-get update && sudo apt-get install -y docker.io docker-compose-plugin
sudo usermod -aG docker ubuntu
newgrp docker
mkdir -p ~/app && cd ~/app
mkdir -p front backend
```

### 2️⃣ 환경 변수 파일 생성

**backend/.env.prod 파일 생성:**
```bash
cd ~/app
cat > backend/.env.prod << 'EOF'
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=sns_db
MYSQL_USER=sns_user
MYSQL_PASSWORD=your_secure_password
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/sns_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=sns_user
SPRING_DATASOURCE_PASSWORD=your_secure_password
TOSS_PAYMENTS_SECRET_KEY=test_sk_KNbdOvk5rkO5b1pjAwOArn07xlzm
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
EOF
chmod 600 backend/.env.prod
```

**front/.env.prod 파일 생성:**
```bash
EC2_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
cat > front/.env.prod << EOF
NEXT_PUBLIC_API_URL=http://${EC2_IP}:8080
NODE_ENV=production
PORT=3000
EOF
chmod 600 front/.env.prod
```

**docker-compose.prod.yml 파일 생성:**
```bash
cd ~/app
# 파일 내용은 DEPLOYMENT.md 참조하거나 GitHub 저장소에서 다운로드
```

### 3️⃣ GitHub Secrets 설정

GitHub 저장소 → Settings → Secrets and variables → Actions:

```
DOCKER_USERNAME = your_docker_username
DOCKER_PASSWORD = your_docker_password_or_token
EC2_HOST = YOUR_EC2_IP
EC2_SSH_KEY = [SSH 개인키 전체 내용]
DEPLOY_PATH = /home/ubuntu/app
```

**SSH 키 가져오기:**
```bash
# 로컬 컴퓨터에서
cat ~/.ssh/your-ec2-key.pem
# 출력된 전체 내용을 EC2_SSH_KEY에 복사
```

### 4️⃣ 배포 실행

```bash
# GitHub에 코드 푸시 (자동 배포)
git push origin dev

# 또는 수동 배포 (EC2에서)
cd ~/app
export DOCKER_USERNAME=your_username
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

---

## 📋 필수 명령어

### 상태 확인
```bash
docker-compose -f docker-compose.prod.yml ps
docker-compose -f docker-compose.prod.yml logs -f
```

### 재시작
```bash
docker-compose -f docker-compose.prod.yml restart
docker-compose -f docker-compose.prod.yml restart back
```

### 정리
```bash
docker-compose -f docker-compose.prod.yml down
docker image prune -af
```

---

## ⚠️ 주의사항

1. **비밀번호 변경 필수**: `.env.prod` 파일의 모든 `CHANGE_THIS` 부분 수정
2. **SSH 키**: `EC2_SSH_KEY`에 전체 키 내용 포함 (줄바꿈 포함)
3. **보안 그룹**: EC2에서 22, 3000, 8080 포트 열기
4. **DEPLOY_PATH**: GitHub Secrets의 `DEPLOY_PATH`와 EC2 실제 경로 일치 확인

---

자세한 내용은 [DEPLOYMENT.md](./DEPLOYMENT.md) 참조
