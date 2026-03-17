# Reservation Web

Spring Boot + React reservation service for therapy / body care booking.

## Stack

- Java 17
- Spring Boot 3.2
- Spring Security
- Spring Data JPA
- MySQL 8
- React + Vite

## Run locally

Create `.env` in the project root:

```env
DB_USERNAME=root
DB_PASSWORD=1234
DB_URL=jdbc:mysql://127.0.0.1:3306/reservation?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
SPRING_PROFILES_ACTIVE=dev
```

Build frontend:

```powershell
cd C:\Users\MSY\Desktop\reservation_web\src\main\frontend
cmd /c npm run build
```

Run backend:

```powershell
cd C:\Users\MSY\Desktop\reservation_web
.\gradlew bootRun
```

Open:

```text
http://127.0.0.1:8080/
```

## Test

```powershell
cd C:\Users\MSY\Desktop\reservation_web
.\gradlew test
```

## Docker Compose

```powershell
cd C:\Users\MSY\Desktop\reservation_web
docker compose up -d --build
```

## Production

Production deployment guide:

- [DEPLOYMENT.md](/C:/Users/MSY/Desktop/reservation_web/DEPLOYMENT.md)

Nginx reverse proxy example:

- [nginx.conf.example](/C:/Users/MSY/Desktop/reservation_web/nginx.conf.example)

Environment example:

- [.env.example](/C:/Users/MSY/Desktop/reservation_web/.env.example)

## Notes

- React build output is served by Spring Boot from `src/main/resources/static/react-app`
- user-facing pages use React
- admin pages use existing server-rendered templates

made by msy