# GitHub Actions CI/CD 배포 가이드

이 문서는 SNS-Service 프로젝트를 GitHub Actions를 사용하여 EC2에 자동 배포하는 방법을 설명합니다.

## 📋 목차

1. [사전 준비사항](#사전-준비사항)
2. [GitHub Secrets 설정](#github-secrets-설정)
3. [EC2 서버 초기 설정](#ec2-서버-초기-설정)
4. [프로젝트 파일 생성 (EC2)](#프로젝트-파일-생성-ec2)
5. [환경 변수 파일 생성](#환경-변수-파일-생성)
6. [배포 프로세스](#배포-프로세스)

---

## 사전 준비사항

- GitHub 저장소
- AWS EC2 인스턴스
- Docker Hub 계정
- EC2 SSH 키 페어 (.pem 파일)

---

## GitHub Secrets 설정

### 1. GitHub 저장소에서 Secrets 추가

GitHub 저장소 → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

### 2. 필요한 Secrets 목록

| Secret 이름 | 설명 | 예시 |
|------------|------|------|
| `DOCKER_USERNAME` | Docker Hub 사용자명 | `yourusername` |
| `DOCKER_PASSWORD` | Docker Hub 비밀번호 또는 Access Token | `dckr_pat_xxxxx` |
| `EC2_HOST` | EC2 Public IP 또는 도메인 | `3.34.123.456` 또는 `api.example.com` |
| `EC2_USERNAME` | EC2 SSH 사용자명 (선택) | `ubuntu` (기본값) |
| `EC2_SSH_KEY` | EC2 SSH 개인키 전체 내용 | `-----BEGIN RSA PRIVATE KEY-----...` |
| `DEPLOY_PATH` | EC2에서 프로젝트 배포 경로 | `/home/ubuntu/app` |

### 3. SSH 키 내용 가져오기

**로컬 컴퓨터에서:**

```bash
# 방법 1: SSH 키 파일 내용 출력
cat ~/.ssh/your-ec2-key.pem

# 방법 2: 또는 절대 경로 사용
cat /path/to/your-ec2-key.pem

# 출력된 전체 내용을 복사하여 EC2_SSH_KEY Secret에 붙여넣기
# (-----BEGIN RSA PRIVATE KEY----- 부터 -----END RSA PRIVATE KEY----- 까지 전체)
```

---

## EC2 서버 초기 설정

### 1. EC2에 SSH 접속

```bash
ssh -i ~/.ssh/your-ec2-key.pem ubuntu@YOUR_EC2_IP
```

### 2. 시스템 업데이트 및 필수 패키지 설치

```bash
# 시스템 업데이트
sudo apt-get update
sudo apt-get upgrade -y

# Docker 설치
sudo apt-get install -y docker.io

# Docker Compose 설치 (v2)
sudo apt-get install -y docker-compose-plugin

# 또는 Docker Compose v1 설치 (v2가 안될 경우)
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker ubuntu

# 그룹 변경사항 적용 (새 세션 필요)
newgrp docker

# Docker 실행 확인
docker --version
docker-compose --version
```

### 3. 프로젝트 디렉토리 생성

```bash
# 배포 디렉토리 생성
mkdir -p ~/app
cd ~/app

# 또는 원하는 경로에 생성 (DEPLOY_PATH Secret과 일치해야 함)
# 예: /home/ubuntu/app
mkdir -p /home/ubuntu/app
cd /home/ubuntu/app
```

---

## 프로젝트 파일 생성 (EC2)

### 1. docker-compose.prod.yml 파일 생성

```bash
cd ~/app  # 또는 DEPLOY_PATH로 설정한 경로

# 파일 생성
nano docker-compose.prod.yml
```

다음 내용을 붙여넣기:

```yaml
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
```

저장: `Ctrl + X` → `Y` → `Enter`

### 2. 디렉토리 구조 생성

```bash
cd ~/app  # 또는 DEPLOY_PATH
mkdir -p front backend
```

---

## 환경 변수 파일 생성

### 1. 백엔드 환경 변수 파일 생성

```bash
cd ~/app
nano backend/.env.prod
```

다음 내용을 수정하여 붙여넣기:

```bash
# MySQL 데이터베이스 설정
MYSQL_ROOT_PASSWORD=your_secure_root_password_here
MYSQL_DATABASE=sns_db
MYSQL_USER=sns_user
MYSQL_PASSWORD=your_secure_db_password_here

# Spring Boot 데이터베이스 연결
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/sns_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=sns_user
SPRING_DATASOURCE_PASSWORD=your_secure_db_password_here
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver

# Spring Boot JPA 설정
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true

# Toss Payments (프로덕션 키로 변경 필요)
TOSS_PAYMENTS_SECRET_KEY=live_sk_xxxxxxxxxxxxx

# 기타 Spring Boot 설정
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

**중요:** 
- `MYSQL_ROOT_PASSWORD`: MySQL root 비밀번호 (강력한 비밀번호 사용)
- `MYSQL_PASSWORD`: 애플리케이션용 DB 비밀번호
- `TOSS_PAYMENTS_SECRET_KEY`: 프로덕션 키로 변경 (테스트 키: `test_sk_...`)

저장: `Ctrl + X` → `Y` → `Enter`

### 2. 프론트엔드 환경 변수 파일 생성

```bash
cd ~/app
nano front/.env.prod
```

다음 내용을 수정하여 붙여넣기:

```bash
# API URL (EC2 IP 또는 도메인으로 변경)
NEXT_PUBLIC_API_URL=http://YOUR_EC2_IP:8080
# 또는 도메인 사용 시
# NEXT_PUBLIC_API_URL=https://api.yourdomain.com

# Next.js 환경 설정
NODE_ENV=production
PORT=3000
```

**중요:** `YOUR_EC2_IP`를 실제 EC2 Public IP로 변경

저장: `Ctrl + X` → `Y` → `Enter`

### 3. 파일 권한 설정

```bash
cd ~/app
# 환경 변수 파일은 읽기 전용으로 설정
chmod 600 backend/.env.prod front/.env.prod
```

---

## 배포 프로세스

### 자동 배포 (GitHub Actions)

1. **코드 푸시**: `dev`, `deploy`, 또는 `main` 브랜치에 푸시
   ```bash
   git push origin dev
   ```

2. **GitHub Actions 실행 확인**
   - GitHub 저장소 → **Actions** 탭
   - 워크플로우 실행 상태 확인

3. **배포 과정**:
   - ✅ 코드 체크아웃
   - ✅ Gradle 빌드
   - ✅ Docker 이미지 빌드
   - ✅ Docker Hub에 이미지 푸시
   - ✅ EC2에 SSH 접속
   - ✅ 이미지 Pull
   - ✅ 컨테이너 재시작
   - ✅ 헬스체크 확인

### 수동 배포 (GitHub Actions)

1. GitHub 저장소 → **Actions** 탭
2. **SNS-Service Deploy to EC2** 워크플로우 선택
3. **Run workflow** 버튼 클릭
4. 브랜치 선택 후 **Run workflow** 실행

### 수동 배포 (EC2에서 직접)

EC2에 SSH 접속 후:

```bash
cd ~/app  # 또는 DEPLOY_PATH

# Docker Hub에서 최신 이미지 Pull
export DOCKER_USERNAME=your_docker_username
docker-compose -f docker-compose.prod.yml pull

# 기존 컨테이너 중지 및 삭제
docker-compose -f docker-compose.prod.yml down

# 새로운 컨테이너 시작
docker-compose -f docker-compose.prod.yml up -d

# 상태 확인
docker-compose -f docker-compose.prod.yml ps

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f
```

---

## 유용한 명령어

### EC2에서 컨테이너 관리

```bash
cd ~/app

# 컨테이너 상태 확인
docker-compose -f docker-compose.prod.yml ps

# 로그 확인
docker-compose -f docker-compose.prod.yml logs
docker-compose -f docker-compose.prod.yml logs -f  # 실시간 로그
docker-compose -f docker-compose.prod.yml logs back  # 백엔드만
docker-compose -f docker-compose.prod.yml logs front  # 프론트엔드만

# 컨테이너 재시작
docker-compose -f docker-compose.prod.yml restart

# 특정 서비스 재시작
docker-compose -f docker-compose.prod.yml restart back

# 컨테이너 중지
docker-compose -f docker-compose.prod.yml stop

# 컨테이너 삭제 (데이터는 유지)
docker-compose -f docker-compose.prod.yml down

# 컨테이너 삭제 (볼륨 포함 - 주의!)
docker-compose -f docker-compose.prod.yml down -v

# 오래된 이미지 정리
docker image prune -af --filter "until=24h"
```

### 문제 해결

```bash
# 특정 컨테이너에 접속
docker exec -it sns-backend-prod sh
docker exec -it sns-frontend-prod sh
docker exec -it sns-mysql-prod mysql -u root -p

# 컨테이너 내부 명령 실행
docker exec sns-backend-prod ls -la

# 시스템 리소스 확인
docker stats

# Docker 디스크 사용량 확인
docker system df
```

---

## EC2 보안 그룹 설정

AWS Console → EC2 → **Security Groups**에서 다음 포트 열기:

| 포트 | 프로토콜 | 소스 | 설명 |
|------|---------|------|------|
| 22 | TCP | Your IP | SSH 접속 |
| 3000 | TCP | 0.0.0.0/0 | 프론트엔드 |
| 8080 | TCP | 0.0.0.0/0 | 백엔드 API |
| 3306 | TCP | EC2 Security Group | MySQL (EC2 내부만) |

또는 AWS CLI로:

```bash
# SSH (22)
aws ec2 authorize-security-group-ingress \
  --group-id sg-xxxxxxxxx \
  --protocol tcp \
  --port 22 \
  --cidr YOUR_IP/32

# 프론트엔드 (3000)
aws ec2 authorize-security-group-ingress \
  --group-id sg-xxxxxxxxx \
  --protocol tcp \
  --port 3000 \
  --cidr 0.0.0.0/0

# 백엔드 (8080)
aws ec2 authorize-security-group-ingress \
  --group-id sg-xxxxxxxxx \
  --protocol tcp \
  --port 8080 \
  --cidr 0.0.0.0/0
```

---

## 문제 해결 체크리스트

### 배포 실패 시 확인사항

1. ✅ **GitHub Secrets 설정 확인**
   ```bash
   # GitHub 저장소 → Settings → Secrets에서 모든 Secret이 설정되었는지 확인
   ```

2. ✅ **EC2 SSH 접속 테스트**
   ```bash
   ssh -i ~/.ssh/your-key.pem ubuntu@YOUR_EC2_IP
   ```

3. ✅ **Docker 및 Docker Compose 설치 확인**
   ```bash
   docker --version
   docker-compose --version
   ```

4. ✅ **DEPLOY_PATH 경로 확인**
   ```bash
   # EC2에서
   ls -la ~/app  # 또는 DEPLOY_PATH로 설정한 경로
   # docker-compose.prod.yml 파일이 있어야 함
   ```

5. ✅ **환경 변수 파일 확인**
   ```bash
   cd ~/app
   ls -la backend/.env.prod front/.env.prod
   ```

6. ✅ **Docker Hub 이미지 확인**
   ```bash
   # Docker Hub에서 이미지가 푸시되었는지 확인
   # https://hub.docker.com/r/YOUR_USERNAME/sns-backend
   # https://hub.docker.com/r/YOUR_USERNAME/sns-frontend
   ```

7. ✅ **EC2 로그 확인**
   ```bash
   cd ~/app
   docker-compose -f docker-compose.prod.yml logs
   ```

---

## 완료! 🎉

이제 GitHub Actions를 통해 자동 배포가 가능합니다.

추가 질문이나 문제가 있으면 이슈를 생성해주세요.
