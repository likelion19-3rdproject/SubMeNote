# SNS-Service

## 백엔드 실행
main 에서 실행 한 적 있는 경우 빌드 삭제 필요
```
cd backend
rm -rf build
./gradlew clean
```

프로젝트 실행
```
cd backend && docker compose -f docker-compose.dev.yml up -d --build
```

실행 후 `localhost:8080/home` 접속해서 Home 단어가 보인다면 실행 성공


## 프론트엔드 실행
```
cd front && docker compose -f docker-compose.dev.yml up -d --build
```

실행 후 `localhost:3000` 접속해서 Next.js 홈화면이 보인다면 실행 성공

## 동시 실행
```
docker compose -f docker-compose.dev.yml up -d --build
```

실행 후 위 두 개의 링크가 모두 접속에 성공한다면 실행 성공

## 종료
```
docker compose -f docker-compose.dev.yml down -v # 볼륨 함께 삭제
```
