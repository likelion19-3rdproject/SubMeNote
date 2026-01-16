# 수동 배포 완전 가이드 (스왑 파일 포함)

EC2 서버를 처음부터 설정하고 수동 배포하는 방법입니다.

## 📋 사전 준비

### EC2 인스턴스 생성 시 확인사항

- **Instance Type**: t2.micro 또는 t3.micro (최소 1GB RAM)
- **Storage**: 20GB 이상 (30GB 권장)
- **Security Group**: SSH(22), Custom TCP(3000, 8080) 허용

---

## 📋 1단계: SSH 접속

### 로컬 컴퓨터에서

```bash
cd ~/Downloads
chmod 400 sns-service.pem
ssh -i sns-service.pem ubuntu@YOUR_EC2_IP
```

---

## 📋 2단계: 스왑 파일 생성 (램 부족 해결)

**⚠️ 중요: Docker 설치 전에 먼저 실행하세요!**

```bash
# 1. 현재 메모리 확인
free -h

# 2. 스왑 파일 생성 (2GB 권장, 필요시 4GB까지 가능)
sudo fallocate -l 2G /swapfile

# 또는 dd 명령어 사용 (fallocate가 안 될 경우)
# sudo dd if=/dev/zero of=/swapfile bs=1M count=2048

# 3. 권한 설정
sudo chmod 600 /swapfile

# 4. 스왑 파일로 설정
sudo mkswap /swapfile

# 5. 스왑 활성화
sudo swapon /swapfile

# 6. 확인
free -h
# Swap에 2G가 표시되어야 함

# 7. 영구적으로 설정 (재부팅 후에도 유지)
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 8. swappiness 조정 (선택사항 - 메모리 부족 시 스왑 사용 빈도)
# 기본값 60, 더 적극적으로 사용하려면 100
sudo sysctl vm.swappiness=60
echo 'vm.swappiness=60' | sudo tee -a /etc/sysctl.conf

# 9. 최종 확인
free -h
swapon --show
```

**스왑 파일 크기 권장사항:**
- 1GB RAM: 2GB 스왑
- 2GB RAM: 2-4GB 스왑
- 4GB RAM: 2GB 스왑 (선택사항)

---

## 📋 3단계: Docker 설치

```bash
# 1. 시스템 업데이트
sudo apt-get update
sudo apt-get upgrade -y

# 2. 필수 패키지 설치
sudo apt-get install -y ca-certificates curl gnupg lsb-release

# 3. Docker GPG 키 추가
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# 4. Docker 저장소 추가
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 5. Docker 설치
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 6. Docker 로그 제한 설정 (디스크 공간 절약)
sudo tee /etc/docker/daemon.json > /dev/null <<EOF
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF

# 7. Docker 서비스 시작
sudo systemctl enable docker
sudo systemctl start docker
sudo systemctl restart docker

# 8. 사용자를 docker 그룹에 추가
sudo usermod -aG docker ubuntu
newgrp docker

# 9. 설치 확인
docker --version
docker compose version
docker run hello-world
```

---

## 📋 4단계: 배포 디렉토리 및 파일 생성

### 디렉토리 생성

```bash
mkdir -p ~/app/{front,backend}
cd ~/app
pwd  # /home/ubuntu/app 확인
```

### docker-compose.prod.yml 파일 생성

```bash
cd ~/app

export DOCKER_USERNAME=hyen00

cat > docker-compose.prod.yml << 'COMPOSEEOF'
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
      test: ["CMD-SHELL", "curl -f http://localhost:3000 || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

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
      test: ["CMD-SHELL", "curl -f http://localhost:8080/api/home || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 90s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

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
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

volumes:
  mysql_data:
COMPOSEEOF

cat docker-compose.prod.yml
```

---

## 📋 5단계: 환경 변수 파일 생성

### backend/.env.prod 파일 생성

```bash
cd ~/app

cat > backend/.env.prod << 'ENVEOF'
MYSQL_ROOT_PASSWORD=1234
MYSQL_DATABASE=testdb
MYSQL_USER=test
MYSQL_PASSWORD=1234
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/testdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=test
SPRING_DATASOURCE_PASSWORD=1234
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true
TOSS_PAYMENTS_SECRET_KEY=test_sk_KNbdOvk5rkO5b1pjAwOArn07xlzm
JWT_SECRET=MySuperSecretJWTKeyForSNSApplication2024MustBeAtLeast32CharactersLong
JWT_ACCESS_TOKEN_MS=3600000
JWT_REFRESH_TOKEN_MS=86400000
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_CONNECTIONTIMEOUT=5000
SPRING_MAIL_PROPERTIES_MAIL_SMTP_TIMEOUT=5000
SPRING_MAIL_PROPERTIES_MAIL_SMTP_WRITETIMEOUT=5000
ENVEOF

chmod 600 backend/.env.prod
```

### front/.env.prod 파일 생성

```bash
cd ~/app

EC2_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
cat > front/.env.prod << EOF
NEXT_PUBLIC_API_URL=http://${EC2_IP}:8080
NODE_ENV=production
PORT=3000
EOF

chmod 600 front/.env.prod
cat front/.env.prod
```

---

## 📋 6단계: 배포 스크립트 생성

```bash
cd ~/app

cat > deploy.sh << 'EOF'
#!/bin/bash
set -e

export DOCKER_USERNAME=hyen00
DOCKER_COMPOSE_CMD=$(command -v docker-compose || echo "docker compose")

cd ~/app

echo "=== Pulling latest images ==="
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml pull

echo "=== Stopping old containers ==="
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml down

echo "=== Starting new containers ==="
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml up -d

echo "=== Waiting 60 seconds for services to start ==="
sleep 60

echo "=== Container Status ==="
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml ps

echo "=== Recent Logs ==="
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml logs --tail=30

echo "✅ Deployment complete!"
EOF

chmod +x deploy.sh
ls -la deploy.sh
```

---

## 📋 7단계: 첫 배포 실행

```bash
cd ~/app

# 배포 실행
./deploy.sh

# 또는 수동 실행
export DOCKER_USERNAME=hyen00
DOCKER_COMPOSE_CMD=$(command -v docker-compose || echo "docker compose")
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml pull
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml down
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml up -d

# 상태 확인
sleep 60
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml ps
```

---

## 📋 8단계: 메모리 및 디스크 모니터링

### 메모리 확인

```bash
# 메모리 사용량 확인
free -h

# 스왑 사용량 확인
swapon --show

# 프로세스별 메모리 사용량
docker stats --no-stream
```

### 디스크 사용량 확인

```bash
# 전체 디스크
df -h

# Docker 디스크 사용량
docker system df

# 큰 디렉토리 찾기
sudo du -h --max-depth=1 / 2>/dev/null | sort -hr | head -10
```

### 디스크 정리

```bash
# Docker 정리
docker system prune -af --filter "until=24h"

# 또는 스크립트 생성
cat > ~/cleanup.sh << 'EOF'
#!/bin/bash
echo "=== Docker Cleanup ==="
docker container prune -f
docker image prune -af --filter "until=24h"
docker volume prune -f
docker builder prune -af
echo "Done!"
docker system df
EOF

chmod +x ~/cleanup.sh
~/cleanup.sh
```

---

## 📋 한 번에 실행하는 초기 설정 스크립트

```bash
#!/bin/bash
set -e

echo "========================================="
echo "EC2 초기 설정 시작"
echo "========================================="

# === 1. 스왑 파일 생성 ===
echo ""
echo "[1/7] 스왑 파일 생성 (2GB)..."
if [ ! -f /swapfile ]; then
  sudo fallocate -l 2G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
  sudo sysctl vm.swappiness=60
  echo 'vm.swappiness=60' | sudo tee -a /etc/sysctl.conf
  echo "✓ Swap file created"
else
  echo "✓ Swap file already exists"
fi
free -h

# === 2. Docker 설치 ===
echo ""
echo "[2/7] Docker 설치..."
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg lsb-release
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Docker 로그 제한
sudo tee /etc/docker/daemon.json > /dev/null <<EOF
{
  "log-driver": "json-file",
  "log-opts": {"max-size": "10m", "max-file": "3"}
}
EOF

sudo systemctl enable docker && sudo systemctl start docker && sudo systemctl restart docker
sudo usermod -aG docker ubuntu
newgrp docker
echo "✓ Docker installed"

# === 3. 디렉토리 생성 ===
echo ""
echo "[3/7] 배포 디렉토리 생성..."
mkdir -p ~/app/{front,backend}
cd ~/app
echo "✓ Directories created"

# === 4. docker-compose.prod.yml 생성 ===
echo ""
echo "[4/7] docker-compose.prod.yml 생성..."
export DOCKER_USERNAME=hyen00
cat > docker-compose.prod.yml << 'COMPOSEEOF'
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
      test: ["CMD-SHELL", "curl -f http://localhost:3000 || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

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
      test: ["CMD-SHELL", "curl -f http://localhost:8080/api/home || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 90s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

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
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

volumes:
  mysql_data:
COMPOSEEOF
echo "✓ docker-compose.prod.yml created"

# === 5. 환경 변수 파일 생성 ===
echo ""
echo "[5/7] 환경 변수 파일 생성..."

# backend/.env.prod
cat > backend/.env.prod << 'ENVEOF'
MYSQL_ROOT_PASSWORD=1234
MYSQL_DATABASE=testdb
MYSQL_USER=test
MYSQL_PASSWORD=1234
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/testdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=test
SPRING_DATASOURCE_PASSWORD=1234
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true
TOSS_PAYMENTS_SECRET_KEY=test_sk_KNbdOvk5rkO5b1pjAwOArn07xlzm
JWT_SECRET=MySuperSecretJWTKeyForSNSApplication2024MustBeAtLeast32CharactersLong
JWT_ACCESS_TOKEN_MS=3600000
JWT_REFRESH_TOKEN_MS=86400000
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_CONNECTIONTIMEOUT=5000
SPRING_MAIL_PROPERTIES_MAIL_SMTP_TIMEOUT=5000
SPRING_MAIL_PROPERTIES_MAIL_SMTP_WRITETIMEOUT=5000
ENVEOF

# front/.env.prod
EC2_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
cat > front/.env.prod << EOF
NEXT_PUBLIC_API_URL=http://${EC2_IP}:8080
NODE_ENV=production
PORT=3000
EOF

chmod 600 backend/.env.prod front/.env.prod
echo "✓ Environment files created"

# === 6. 배포 스크립트 생성 ===
echo ""
echo "[6/7] 배포 스크립트 생성..."
cat > deploy.sh << 'DEPLOYEOF'
#!/bin/bash
set -e
export DOCKER_USERNAME=hyen00
DOCKER_COMPOSE_CMD=$(command -v docker-compose || echo "docker compose")
cd ~/app
echo "=== Pulling latest images ==="
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml pull
echo "=== Stopping old containers ==="
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml down
echo "=== Starting new containers ==="
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml up -d
echo "=== Waiting 60 seconds ==="
sleep 60
echo "=== Container Status ==="
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml ps
echo "=== Recent Logs ==="
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml logs --tail=30
echo "✅ Deployment complete!"
DEPLOYEOF
chmod +x deploy.sh
echo "✓ deploy.sh created"

# === 7. 정리 스크립트 생성 ===
echo ""
echo "[7/7] 정리 스크립트 생성..."
cat > ~/cleanup.sh << 'CLEANEOF'
#!/bin/bash
echo "=== Docker Cleanup ==="
docker container prune -f
docker image prune -af --filter "until=24h"
docker volume prune -f
docker builder prune -af
echo "Done!"
docker system df
CLEANEOF
chmod +x ~/cleanup.sh
echo "✓ cleanup.sh created"

echo ""
echo "========================================="
echo "✅ 초기 설정 완료!"
echo "========================================="
echo ""
echo "📊 현재 상태:"
free -h
df -h | head -2
echo ""
echo "📝 다음 단계:"
echo "1. 첫 배포 실행: cd ~/app && ./deploy.sh"
echo "2. 디스크 정리: ~/cleanup.sh"
echo ""
echo "========================================="
```

**스크립트 저장 및 실행:**

```bash
# EC2에서
nano setup.sh
# 위 스크립트 전체 복사해서 붙여넣기
# 저장: Ctrl+X → Y → Enter

chmod +x setup.sh
./setup.sh
```

---

## 📝 빠른 설정 요약 (복사해서 실행)

### EC2 서버에서 순서대로:

```bash
# === 1. 스왑 파일 생성 ===
sudo fallocate -l 2G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
free -h

# === 2. Docker 설치 ===
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg lsb-release
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo tee /etc/docker/daemon.json > /dev/null <<EOF
{"log-driver":"json-file","log-opts":{"max-size":"10m","max-file":"3"}}
EOF
sudo systemctl enable docker && sudo systemctl start docker && sudo systemctl restart docker
sudo usermod -aG docker ubuntu
newgrp docker

# === 3. 디렉토리 및 파일 생성 ===
mkdir -p ~/app/{front,backend}
cd ~/app

# docker-compose.prod.yml (위 4단계 내용)
# backend/.env.prod (위 5단계 내용)
# front/.env.prod (위 5단계 내용)

# === 4. 배포 스크립트 ===
cat > deploy.sh << 'EOF'
#!/bin/bash
set -e
export DOCKER_USERNAME=hyen00
DOCKER_COMPOSE_CMD=$(command -v docker-compose || echo "docker compose")
cd ~/app
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml pull
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml down
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml up -d
sleep 60
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml ps
EOF
chmod +x deploy.sh

# === 5. 첫 배포 ===
./deploy.sh
```

---

## 💾 스왑 파일 관리

### 스왑 파일 크기 변경

```bash
# 기존 스왑 비활성화
sudo swapoff /swapfile

# 새 크기로 재생성 (예: 4GB)
sudo rm /swapfile
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# 확인
free -h
```

### 스왑 사용량 모니터링

```bash
# 실시간 모니터링
watch -n 1 free -h

# 또는
watch -n 1 'free -h && swapon --show'
```

---

## ✅ 설정 완료 체크리스트

- [ ] 스왑 파일 생성 (2GB)
- [ ] Docker 설치 완료
- [ ] Docker 로그 제한 설정
- [ ] ~/app 디렉토리 생성
- [ ] docker-compose.prod.yml 파일 생성
- [ ] backend/.env.prod 파일 생성
- [ ] front/.env.prod 파일 생성
- [ ] deploy.sh 스크립트 생성
- [ ] 첫 배포 성공
- [ ] 메모리 사용량 확인 (free -h)

---

## 🚀 수동 배포 프로세스

### 로컬에서 이미지 빌드 및 푸시

```bash
# 로컬 컴퓨터에서
cd /Users/parkhyeeun/Documents/likelion/sns/SNS-Service

# Docker Hub 로그인
docker login

# 백엔드 빌드 및 푸시
cd backend
./gradlew clean build -x test
docker build -f Dockerfile.dev -t hyen00/sns-backend:latest .
docker push hyen00/sns-backend:latest

# 프론트엔드 빌드 및 푸시
cd ../front
docker build -f Dockerfile.prod -t hyen00/sns-frontend:latest .
docker push hyen00/sns-frontend:latest
```

### EC2에서 배포

```bash
# EC2에서
cd ~/app
./deploy.sh
```

---

## 🔄 GitHub Actions 자동 배포 설정

수동 배포가 성공했다면, 이제 GitHub Actions를 통해 자동 배포를 설정할 수 있습니다.

### 📋 사전 준비

- ✅ EC2 서버가 정상 작동 중
- ✅ `backend/.env.prod`와 `front/.env.prod` 파일이 EC2에 생성됨
- ✅ Docker Hub 계정 준비
- ✅ EC2 SSH 키 파일 (`.pem` 파일)

---

## 📋 1단계: GitHub Secrets 설정

### GitHub 저장소에서 Secrets 추가

1. GitHub 저장소로 이동
2. **Settings** → **Secrets and variables** → **Actions** 클릭
3. **New repository secret** 클릭

### 필요한 Secrets 목록

다음 Secrets를 모두 추가하세요:

| Secret 이름 | 설명 | 예시 값 |
|------------|------|---------|
| `DOCKER_USERNAME` | Docker Hub 사용자명 | `hyen00` |
| `DOCKER_PASSWORD` | Docker Hub 비밀번호 또는 Access Token | `dckr_pat_xxxxx` |
| `EC2_HOST` | EC2 Public IP 주소 | `3.106.134.167` |
| `EC2_USERNAME` | EC2 SSH 사용자명 (선택, 기본값: ubuntu) | `ubuntu` |
| `EC2_SSH_KEY` | EC2 SSH 개인키 전체 내용 | `-----BEGIN RSA PRIVATE KEY-----...` |
| `DEPLOY_PATH` | EC2 배포 경로 | `/home/ubuntu/app` |

### SSH 키 내용 가져오기

**로컬 컴퓨터에서:**

```bash
# SSH 키 파일 전체 내용 출력
cat ~/Downloads/sns-service.pem

# 또는 절대 경로 사용
cat /path/to/sns-service.pem
```

**중요:** 
- `-----BEGIN RSA PRIVATE KEY-----` 부터 `-----END RSA PRIVATE KEY-----` 까지 **전체 내용**을 복사
- 줄바꿈 포함하여 정확히 복사
- `EC2_SSH_KEY` Secret에 붙여넣기

### Docker Hub Access Token 생성 (권장)

일반 비밀번호 대신 Access Token 사용을 권장합니다:

1. Docker Hub 로그인 → **Account Settings** → **Security**
2. **New Access Token** 클릭
3. Token 이름 입력 (예: `github-actions`)
4. **Read & Write** 권한 선택
5. **Generate** 클릭
6. 생성된 토큰을 복사하여 `DOCKER_PASSWORD` Secret에 저장

---

## 📋 2단계: deploy.yml 파일 확인

프로젝트에 이미 `.github/workflows/deploy.yml` 파일이 있습니다. 확인해보세요:

```bash
# 로컬에서
cat .github/workflows/deploy.yml
```

이 파일은 다음을 수행합니다:
- ✅ 코드 체크아웃
- ✅ Gradle로 백엔드 빌드
- ✅ Docker 이미지 빌드 (백엔드, 프론트엔드)
- ✅ Docker Hub에 이미지 푸시
- ✅ EC2에 SSH 접속
- ✅ 최신 이미지 Pull
- ✅ 컨테이너 재시작
- ✅ 헬스체크 확인

---

## 📋 3단계: EC2에서 환경 변수 파일 확인

GitHub Actions는 EC2에 있는 환경 변수 파일을 사용합니다. 확인하세요:

```bash
# EC2에서
cd ~/app

# 파일 존재 확인
ls -la backend/.env.prod front/.env.prod

# 파일 내용 확인 (민감한 정보는 마스킹)
head -5 backend/.env.prod
cat front/.env.prod
```

**중요:** 
- `backend/.env.prod`에 이메일 설정이 포함되어 있어야 함
- `front/.env.prod`에 올바른 `NEXT_PUBLIC_API_URL`이 설정되어 있어야 함

---

## 📋 4단계: 첫 자동 배포 실행

### 방법 1: 코드 푸시로 자동 배포

```bash
# 로컬에서
git add .
git commit -m "feat: GitHub Actions 자동 배포 설정"
git push origin main
# 또는
git push origin dev
```

**트리거 브랜치:** `dev`, `deploy`, `main` 브랜치에 푸시하면 자동 실행

### 방법 2: 수동 실행 (workflow_dispatch)

1. GitHub 저장소 → **Actions** 탭
2. **SNS-Service Deploy to EC2** 워크플로우 선택
3. **Run workflow** 버튼 클릭
4. 브랜치 선택 후 **Run workflow** 실행

---

## 📋 5단계: 배포 상태 확인

### GitHub Actions에서 확인

1. GitHub 저장소 → **Actions** 탭
2. 실행 중인 워크플로우 클릭
3. 각 단계별 로그 확인:
   - ✅ Checkout
   - ✅ Set up JDK 21
   - ✅ Build Backend
   - ✅ Build & Push Backend Image
   - ✅ Build & Push Frontend Image
   - ✅ Deploy to EC2

### EC2에서 확인

```bash
# EC2에서
cd ~/app
export DOCKER_USERNAME=hyen00
DOCKER_COMPOSE_CMD=$(command -v docker-compose || echo "docker compose")

# 컨테이너 상태 확인
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml ps

# 최근 로그 확인
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml logs --tail=50
```

---

## 🔧 문제 해결

### 배포 실패 시 확인사항

#### 1. GitHub Secrets 확인

```bash
# GitHub 저장소 → Settings → Secrets에서 확인
# 모든 Secret이 올바르게 설정되었는지 확인
```

#### 2. SSH 접속 테스트

```bash
# 로컬에서
ssh -i ~/Downloads/sns-service.pem ubuntu@YOUR_EC2_IP
```

#### 3. EC2에서 환경 변수 파일 확인

```bash
# EC2에서
cd ~/app
ls -la backend/.env.prod front/.env.prod

# 파일이 없으면 생성 필요
```

#### 4. Docker Hub 이미지 확인

- Docker Hub에서 이미지가 푸시되었는지 확인
- `https://hub.docker.com/r/YOUR_USERNAME/sns-backend`
- `https://hub.docker.com/r/YOUR_USERNAME/sns-frontend`

#### 5. GitHub Actions 로그 확인

- **Actions** 탭 → 실패한 워크플로우 클릭
- 각 단계의 로그를 확인하여 오류 원인 파악

### 자주 발생하는 오류

#### 오류 1: `ERROR: Environment files not found!`

**원인:** EC2에 `backend/.env.prod` 또는 `front/.env.prod` 파일이 없음

**해결:**
```bash
# EC2에서
cd ~/app
# 파일 생성 (위 5단계 참조)
```

#### 오류 2: `Permission denied (publickey)`

**원인:** `EC2_SSH_KEY` Secret이 잘못 설정됨

**해결:**
- SSH 키 전체 내용을 다시 복사
- 줄바꿈 포함하여 정확히 복사
- `-----BEGIN` 부터 `-----END` 까지 전체

#### 오류 3: `denied: requested access to the resource is denied`

**원인:** Docker Hub 로그인 실패

**해결:**
- `DOCKER_USERNAME`과 `DOCKER_PASSWORD` 확인
- Access Token 사용 권장

#### 오류 4: `Connection timed out`

**원인:** `EC2_HOST` IP가 잘못되었거나 Security Group 설정 문제

**해결:**
- EC2 Public IP 확인
- Security Group에서 SSH(22) 포트 허용 확인

---

## ✅ GitHub Actions 설정 체크리스트

- [ ] GitHub Secrets 설정 완료
  - [ ] `DOCKER_USERNAME`
  - [ ] `DOCKER_PASSWORD` (또는 Access Token)
  - [ ] `EC2_HOST` (Public IP)
  - [ ] `EC2_SSH_KEY` (전체 SSH 키)
  - [ ] `DEPLOY_PATH` (`/home/ubuntu/app`)
- [ ] EC2에 환경 변수 파일 존재 확인
  - [ ] `backend/.env.prod`
  - [ ] `front/.env.prod`
- [ ] `.github/workflows/deploy.yml` 파일 확인
- [ ] 첫 배포 테스트 성공
- [ ] 브라우저에서 접속 확인

---

## 🎉 완료!

이제 코드를 푸시하면 자동으로 배포됩니다!

```bash
# 자동 배포 예시
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin main
# → GitHub Actions가 자동으로 배포 실행
```

---

**초기 설정 완료!** 이제 수동 배포와 자동 배포 모두 가능합니다! 🎉
