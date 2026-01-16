# EC2 서버 완전 초기 설정 가이드

새로 만든 EC2 서버를 처음부터 설정하는 완전한 가이드입니다.

## 📋 사전 준비

### 1. EC2 인스턴스 생성 시 확인사항

- **Instance Type**: t2.micro 또는 t3.micro (무료 티어)
- **Storage**: 20GB 이상 (30GB 권장)
- **Security Group**: SSH(22), HTTP(80), HTTPS(443), Custom TCP(3000, 8080, 3306) 허용
- **Key Pair**: SSH 키 페어 생성 또는 기존 키 사용

### 2. 보안 그룹 설정 (중요!)

AWS Console → EC2 → Security Groups에서 인바운드 규칙 추가:

| Type | Port | Source | 설명 |
|------|------|--------|------|
| SSH | 22 | 0.0.0.0/0 | SSH 접속 |
| Custom TCP | 3000 | 0.0.0.0/0 | 프론트엔드 |
| Custom TCP | 8080 | 0.0.0.0/0 | 백엔드 API |
| Custom TCP | 3306 | EC2 Security Group | MySQL (내부만) |

---

## 📋 1단계: SSH 접속

### 로컬 컴퓨터에서

```bash
# SSH 키 파일 권한 설정
chmod 400 ~/.ssh/sns-service.pem

# SSH 접속
ssh -i ~/.ssh/sns-service.pem ubuntu@YOUR_EC2_IP

# 또는 Downloads 디렉토리에서
cd ~/Downloads
ssh -i sns-service.pem ubuntu@YOUR_EC2_IP
```

**접속 성공 확인:**
- 프롬프트가 `ubuntu@ip-xxx-xxx-xxx-xxx:~$` 형태로 바뀌면 성공!

---

## 📋 2단계: EC2 서버 기본 설정

### SSH 접속 후 EC2에서 실행

```bash
# 1. 시스템 업데이트
sudo apt-get update
sudo apt-get upgrade -y

# 2. 필수 패키지 설치
sudo apt-get install -y curl wget git

# 3. Docker 공식 설치 방법
# 기존 Docker 제거 (있다면)
sudo apt-get remove -y docker docker-engine docker.io containerd runc

# 필수 패키지
sudo apt-get install -y ca-certificates curl gnupg lsb-release

# Docker GPG 키 추가
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# Docker 저장소 추가
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Docker 설치
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Docker 서비스 시작
sudo systemctl enable docker
sudo systemctl start docker

# 사용자를 docker 그룹에 추가
sudo usermod -aG docker ubuntu
newgrp docker

# 설치 확인
docker --version
docker compose version

# 테스트
sudo docker run hello-world
```

---

## 📋 3단계: 디스크 공간 관리 설정

### Docker 로그 및 데이터 정리 자동화

```bash
# Docker 데몬 설정 파일 생성
sudo tee /etc/docker/daemon.json > /dev/null <<EOF
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "storage-driver": "overlay2"
}
EOF

# Docker 재시작
sudo systemctl restart docker

# 디스크 정리 스크립트 생성
cat > ~/cleanup-docker.sh << 'EOF'
#!/bin/bash
# Docker 정리 스크립트
echo "=== Docker Disk Cleanup ==="
echo "Before cleanup:"
docker system df

# 중지된 컨테이너 삭제
docker container prune -f

# 사용하지 않는 이미지 삭제 (24시간 이상)
docker image prune -af --filter "until=24h"

# 사용하지 않는 볼륨 삭제
docker volume prune -f

# 빌드 캐시 삭제
docker builder prune -af

echo ""
echo "After cleanup:"
docker system df
EOF

chmod +x ~/cleanup-docker.sh

# 주기적으로 실행하기 (선택사항 - crontab)
# crontab -e
# 다음 줄 추가: 0 3 * * 0 /home/ubuntu/cleanup-docker.sh
```

### 디스크 사용량 모니터링

```bash
# 디스크 사용량 확인
df -h

# Docker 디스크 사용량 확인
docker system df

# 큰 파일 찾기
sudo du -h --max-depth=1 / 2>/dev/null | sort -hr | head -20
```

---

## 📋 4단계: 배포 디렉토리 및 파일 생성

### 디렉토리 생성

```bash
# 홈 디렉토리로 이동
cd ~

# 배포 디렉토리 생성
mkdir -p ~/app/front ~/app/backend

# 디렉토리로 이동
cd ~/app

# 확인
pwd  # /home/ubuntu/app 이어야 함
ls -la
```

### docker-compose.prod.yml 파일 생성

```bash
cd ~/app

# DOCKER_USERNAME을 실제 Docker Hub 사용자명으로 변경
export DOCKER_USERNAME=hyen00

# docker-compose.prod.yml 파일 생성
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

# 파일 확인
cat docker-compose.prod.yml
```

---

## 📋 5단계: 환경 변수 파일 생성

### backend/.env.prod 파일 생성

```bash
cd ~/app

cat > backend/.env.prod << 'ENVEOF'
# MySQL 데이터베이스 설정
MYSQL_ROOT_PASSWORD=1234
MYSQL_DATABASE=testdb
MYSQL_USER=test
MYSQL_PASSWORD=1234

# Spring Boot 데이터베이스 연결
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/testdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=test
SPRING_DATASOURCE_PASSWORD=1234
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver

# Spring Boot JPA 설정
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true

# Toss Payments
TOSS_PAYMENTS_SECRET_KEY=test_sk_KNbdOvk5rkO5b1pjAwOArn07xlzm

# JWT 설정 (⚠️ 반드시 강력한 비밀번호로 변경!)
JWT_SECRET=MySuperSecretJWTKeyForSNSApplication2024MustBeAtLeast32CharactersLong
JWT_ACCESS_TOKEN_MS=3600000
JWT_REFRESH_TOKEN_MS=86400000

# 기타 Spring Boot 설정
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
ENVEOF

# 파일 권한 설정
chmod 600 backend/.env.prod

# 확인
head -5 backend/.env.prod
```

**⚠️ 중요:** 
- `JWT_SECRET`: 최소 32자 이상의 강력한 비밀번호로 변경
- `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`: 강력한 비밀번호로 변경 권장

### front/.env.prod 파일 생성

```bash
cd ~/app

# EC2 IP 자동 가져오기
EC2_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
echo "EC2 IP: $EC2_IP"

# front/.env.prod 파일 생성
cat > front/.env.prod << EOF
NEXT_PUBLIC_API_URL=http://${EC2_IP}:8080
NODE_ENV=production
PORT=3000
EOF

# 파일 권한 설정
chmod 600 front/.env.prod

# 파일 확인
cat front/.env.prod
```

---

## 📋 6단계: 배포 스크립트 생성

```bash
cd ~/app

# 배포 스크립트 생성
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

echo "=== Waiting 30 seconds ==="
sleep 30

echo "=== Container Status ==="
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml ps

echo "=== Recent Logs ==="
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml logs --tail=30

echo "✅ Deployment complete!"
EOF

chmod +x deploy.sh

# 확인
ls -la deploy.sh
```

---

## 📋 7단계: 첫 배포 실행

```bash
cd ~/app

# Docker Hub 로그인 (필요시)
docker login

# 배포 스크립트 실행
./deploy.sh

# 또는 수동 실행
export DOCKER_USERNAME=hyen00
DOCKER_COMPOSE_CMD=$(command -v docker-compose || echo "docker compose")
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml pull
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml down
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml up -d

# 상태 확인
sleep 30
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml ps
```

---

## 📋 8단계: 디스크 공간 모니터링

### 정기적인 디스크 정리

```bash
# 수동 정리
~/cleanup-docker.sh

# 또는 직접 명령어
docker system prune -af --filter "until=24h"

# 디스크 사용량 확인
df -h
docker system df
```

### 자동 정리 설정 (선택사항)

```bash
# crontab 편집
crontab -e

# 다음 줄 추가 (매주 일요일 새벽 3시에 정리)
0 3 * * 0 /home/ubuntu/cleanup-docker.sh

# 저장: Ctrl+X → Y → Enter
```

---

## 📋 한 번에 실행하는 초기 설정 스크립트

```bash
#!/bin/bash
set -e

echo "========================================="
echo "EC2 초기 설정 시작"
echo "========================================="

# 1. 시스템 업데이트 및 Docker 설치
echo ""
echo "[1/6] 시스템 업데이트 및 Docker 설치..."
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg lsb-release git

# Docker 공식 설치
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Docker 로그 제한 설정
sudo tee /etc/docker/daemon.json > /dev/null <<EOF
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF

sudo systemctl enable docker
sudo systemctl start docker
sudo systemctl restart docker
sudo usermod -aG docker ubuntu
newgrp docker

# 2. 배포 디렉토리 생성
echo ""
echo "[2/6] 배포 디렉토리 생성..."
mkdir -p ~/app/{front,backend}
cd ~/app

# 3. docker-compose.prod.yml 생성
echo ""
echo "[3/6] docker-compose.prod.yml 생성..."
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

# 4. 환경 변수 파일 생성
echo ""
echo "[4/6] 환경 변수 파일 생성..."

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
ENVEOF

# front/.env.prod
EC2_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
cat > front/.env.prod << EOF
NEXT_PUBLIC_API_URL=http://${EC2_IP}:8080
NODE_ENV=production
PORT=3000
EOF

chmod 600 backend/.env.prod front/.env.prod

# 5. 디스크 정리 스크립트 생성
echo ""
echo "[5/6] 디스크 정리 스크립트 생성..."
cat > ~/cleanup-docker.sh << 'CLEANEOF'
#!/bin/bash
echo "=== Docker Disk Cleanup ==="
docker container prune -f
docker image prune -af --filter "until=24h"
docker volume prune -f
docker builder prune -af
echo "Cleanup complete!"
docker system df
CLEANEOF
chmod +x ~/cleanup-docker.sh

# 6. 배포 스크립트 생성
echo ""
echo "[6/6] 배포 스크립트 생성..."
cat > ~/app/deploy.sh << 'DEPLOYEOF'
#!/bin/bash
set -e
export DOCKER_USERNAME=hyen00
DOCKER_COMPOSE_CMD=$(command -v docker-compose || echo "docker compose")
cd ~/app
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml pull
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml down
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml up -d
sleep 30
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml ps
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml logs --tail=30
echo "✅ Deployment complete!"
DEPLOYEOF
chmod +x ~/app/deploy.sh

echo ""
echo "========================================="
echo "✅ 초기 설정 완료!"
echo "========================================="
echo ""
echo "📝 다음 단계:"
echo "1. backend/.env.prod 파일 수정 (비밀번호 변경 권장)"
echo "   nano ~/app/backend/.env.prod"
echo ""
echo "2. 첫 배포 실행:"
echo "   cd ~/app && ./deploy.sh"
echo ""
echo "3. 디스크 정리 (필요시):"
echo "   ~/cleanup-docker.sh"
echo ""
echo "========================================="
```

**스크립트 저장 및 실행:**

```bash
# EC2에서
nano setup-ec2.sh
# 위 스크립트 내용 붙여넣기
# 저장: Ctrl+X → Y → Enter

chmod +x setup-ec2.sh
./setup-ec2.sh
```

---

## 📝 빠른 설정 요약 (순서대로 실행)

### EC2 서버에서 실행:

```bash
# === 1. 기본 설정 ===
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg lsb-release git
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

# === 2. 디렉토리 및 파일 생성 ===
mkdir -p ~/app/{front,backend}
cd ~/app

# docker-compose.prod.yml (위의 내용 복사)

# backend/.env.prod (위의 내용 복사, 비밀번호 변경!)

# front/.env.prod
EC2_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
cat > front/.env.prod << EOF
NEXT_PUBLIC_API_URL=http://${EC2_IP}:8080
NODE_ENV=production
PORT=3000
EOF
chmod 600 front/.env.prod backend/.env.prod

# === 3. 배포 스크립트 생성 ===
cat > deploy.sh << 'EOF'
#!/bin/bash
set -e
export DOCKER_USERNAME=hyen00
DOCKER_COMPOSE_CMD=$(command -v docker-compose || echo "docker compose")
cd ~/app
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml pull
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml down
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml up -d
sleep 30
$DOCKER_COMPOSE_CMD -f docker-compose.prod.yml ps
EOF
chmod +x deploy.sh
```

---

## 💾 디스크 공간 관리 팁

### 1. Docker 로그 제한 (이미 설정됨)
- 각 컨테이너 로그: 최대 10MB, 3개 파일까지만 보관

### 2. 정기적인 정리

```bash
# 수동 정리
docker system prune -af --filter "until=24h"

# 스크립트 사용
~/cleanup-docker.sh
```

### 3. 디스크 사용량 모니터링

```bash
# 전체 디스크 사용량
df -h

# Docker 디스크 사용량
docker system df

# 큰 디렉토리 찾기
sudo du -h --max-depth=1 / 2>/dev/null | sort -hr | head -10
```

### 4. 자동 정리 (주 1회)

```bash
# crontab 설정
crontab -e

# 추가: 매주 일요일 새벽 3시에 정리
0 3 * * 0 /home/ubuntu/cleanup-docker.sh
```

---

## ✅ 설정 완료 체크리스트

- [ ] Docker 설치 완료
- [ ] Docker Compose 설치 완료
- [ ] ubuntu 사용자를 docker 그룹에 추가
- [ ] ~/app 디렉토리 생성
- [ ] docker-compose.prod.yml 파일 생성 (로그 제한 포함)
- [ ] backend/.env.prod 파일 생성 (비밀번호 변경)
- [ ] front/.env.prod 파일 생성 (EC2 IP 확인)
- [ ] 배포 스크립트(deploy.sh) 생성
- [ ] 디스크 정리 스크립트 생성
- [ ] Docker 로그 제한 설정
- [ ] 첫 배포 성공

---

## ⚠️ 주의사항

1. **보안 그룹**: EC2 보안 그룹에서 3000, 8080 포트 허용 확인
2. **비밀번호**: `backend/.env.prod`의 비밀번호를 강력한 값으로 변경
3. **JWT_SECRET**: 최소 32자 이상의 강력한 비밀번호 사용
4. **디스크 모니터링**: 주기적으로 `df -h`와 `docker system df` 확인

---

**초기 설정 완료!** 이제 `cd ~/app && ./deploy.sh`로 배포할 수 있습니다! 🎉
