# Deployment Guide

## 1. Local `.env` setup

Create a `.env` file in the project root:

```env
DB_USERNAME="name"
DB_PASSWORD="pwd"
DB_URL=jdbc:mysql://localhost:3306/reservation?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
SPRING_PROFILES_ACTIVE=dev
```

The application now reads `.env` automatically during startup.

## 2. Local run

```powershell
cd .\reservation_web\src\main\frontend
cmd /c npm run build

cd .\reservation_web
.\gradlew bootRun
```

## 3. Docker Compose run

Create `.env` in the project root, then run:

```powershell
cd .\reservation_web
docker compose up -d --build
```

## 4. Production checklist

- Set a strong `DB_PASSWORD`
- Use `SPRING_PROFILES_ACTIVE=prod`
- Set a real MySQL `DB_URL`
- Keep `.env` out of git
- Build frontend before packaging if serving React from Spring static assets

## 5. Verification

- Frontend build: `cmd /c npm run build`
- Backend tests: `.\gradlew test`
- App URL: `http://127.0.0.1:8080/`
