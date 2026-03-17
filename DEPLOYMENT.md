# 배포 가이드

이 문서는 `reservation_web` 프로젝트를 실제 운영 환경에 배포하기 위한 안내서입니다.

현재 프로젝트는 다음 구조를 기준으로 배포하는 것을 권장합니다.

- Spring Boot 애플리케이션
- MySQL
- React 정적 파일을 Spring Boot가 직접 서빙
- Nginx 리버스 프록시
- HTTPS

권장 운영 구조:

1. 외부 사용자 -> `Nginx (80/443)`
2. `Nginx` -> `Spring Boot (127.0.0.1:8080)`
3. `Spring Boot` -> `MySQL (127.0.0.1:3306 또는 Docker 내부 네트워크)`

## 1. 배포 전 준비

필수 준비 항목:

- 공인 IP 또는 도메인
- 공유기 포트포워딩 가능 여부
- `.env` 파일 준비
- `80`, `443` 포트 사용 가능 여부
- 업로드 폴더 백업 정책

## 2. `.env` 파일 설정

프로젝트 루트에 `.env` 파일을 생성합니다.

예시:

```env
DB_USERNAME=root
DB_PASSWORD=강한비밀번호로변경
DB_URL=jdbc:mysql://127.0.0.1:3306/reservation?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
SPRING_PROFILES_ACTIVE=prod
```

Docker Compose로 MySQL까지 같이 띄우는 경우:

```env
DB_USERNAME=root
DB_PASSWORD=강한비밀번호로변경
DB_URL=jdbc:mysql://mysql:3306/reservation?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
SPRING_PROFILES_ACTIVE=prod
```

## 3. 공통 배포 순서

### 3-1. 프론트 빌드

```powershell
cd C:\Users\MSY\Desktop\reservation_web\src\main\frontend
cmd /c npm run build
```

### 3-2. 백엔드 테스트

```powershell
cd C:\Users\MSY\Desktop\reservation_web
.\gradlew test
```

### 3-3. 배포용 JAR 생성

```powershell
cd C:\Users\MSY\Desktop\reservation_web
.\gradlew clean bootJar
```

생성 위치:

- `C:\Users\MSY\Desktop\reservation_web\build\libs`

## 4. 윈도우 배포 방법

윈도우 서버 또는 개인 PC에서 직접 운영할 때 사용하는 방법입니다.

### 4-1. 실행

```powershell
cd C:\Users\MSY\Desktop\reservation_web
java -jar .\build\libs\reservation_web-0.0.1-SNAPSHOT.jar
```

실제 파일명은 `build\libs` 안의 이름에 맞춰 실행하면 됩니다.

### 4-2. 윈도우 방화벽 열기

Nginx를 앞단에 두는 경우:

```powershell
New-NetFirewallRule -DisplayName "Reservation HTTP 80" -Direction Inbound -Protocol TCP -LocalPort 80 -Action Allow
New-NetFirewallRule -DisplayName "Reservation HTTPS 443" -Direction Inbound -Protocol TCP -LocalPort 443 -Action Allow
```

테스트용으로 앱을 직접 공개할 때만:

```powershell
New-NetFirewallRule -DisplayName "Reservation App 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```

주의:

- 운영에서는 `8080`을 외부에 직접 공개하지 않는 것이 좋습니다.
- MySQL `3306`은 외부에 절대 공개하지 않는 것을 권장합니다.

### 4-3. 윈도우에서 앱을 서비스처럼 실행하기

윈도우에서 장시간 운영할 경우 `java -jar`를 직접 켜두는 방식보다 서비스 등록이 편합니다.

가장 쉬운 방법은 `NSSM`을 사용하는 것입니다.

다운로드:

- [NSSM](https://nssm.cc/download)

예시:

```powershell
nssm install ReservationWeb
```

설정 값:

- `Path`: `C:\Program Files\Java\jdk-17\bin\java.exe`
- `Startup directory`: `C:\Users\MSY\Desktop\reservation_web`
- `Arguments`: `-jar C:\Users\MSY\Desktop\reservation_web\build\libs\reservation_web-0.0.1-SNAPSHOT.jar`

등록 후:

```powershell
nssm start ReservationWeb
nssm status ReservationWeb
```

중지:

```powershell
nssm stop ReservationWeb
```

### 4-4. 윈도우에서 Nginx 사용하기

윈도우에서도 Nginx를 리버스 프록시로 둘 수 있습니다.

다운로드:

- [Nginx for Windows](https://nginx.org/en/download.html)

예시 설치 경로:

```text
C:\nginx
```

설정 파일 예시:

- [nginx.conf.example](/C:/Users/MSY/Desktop/reservation_web/nginx.conf.example)

윈도우용으로 쓸 때는 인증서 경로만 윈도우 경로로 바꾸면 됩니다.

예시:

```nginx
ssl_certificate      C:/nginx/certs/fullchain.pem;
ssl_certificate_key  C:/nginx/certs/privkey.pem;
```

실행:

```powershell
cd C:\nginx
.\nginx.exe
```

설정 반영:

```powershell
cd C:\nginx
.\nginx.exe -s reload
```

중지:

```powershell
cd C:\nginx
.\nginx.exe -s stop
```

### 4-5. 윈도우에서 HTTPS 인증서 발급

윈도우에서는 `win-acme`를 많이 사용합니다.

다운로드:

- [win-acme](https://www.win-acme.com/)

일반적인 흐름:

1. 도메인이 현재 공인 IP를 가리키게 설정
2. 공유기에서 `80`, `443` 포트포워딩
3. Nginx가 `80` 포트에서 응답 가능해야 함
4. `win-acme`로 인증서 발급
5. 발급된 인증서를 Nginx 설정에 연결

주의:

- 인증서 발급 전에는 반드시 도메인이 외부에서 접근 가능해야 합니다.
- `80` 포트가 막혀 있으면 인증서 발급이 실패할 수 있습니다.

### 4-6. 윈도우에서 다른 PC 접속 테스트

서버 PC의 내부 IP 확인:

```powershell
ipconfig
```

다른 PC에서 접속:

```text
http://서버PC_IP:8080
```

예시:

```text
http://192.168.0.15:8080
```

## 5. 리눅스 배포 방법

리눅스 서버에서 운영할 때 사용하는 방법입니다.

### 5-1. JAR 실행

```bash
cd /home/ubuntu/reservation_web
java -jar ./build/libs/reservation_web-0.0.1-SNAPSHOT.jar
```

백그라운드 실행 예시:

```bash
nohup java -jar ./build/libs/reservation_web-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

### 5-2. systemd 서비스 등록 예시

파일:

```bash
/etc/systemd/system/reservation.service
```

예시 내용:

```ini
[Unit]
Description=Reservation Web
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/reservation_web
ExecStart=/usr/bin/java -jar /home/ubuntu/reservation_web/build/libs/reservation_web-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

적용:

```bash
sudo systemctl daemon-reload
sudo systemctl enable reservation
sudo systemctl start reservation
sudo systemctl status reservation
```

## 6. Docker Compose 배포 방법

프로젝트 루트에서 실행:

```powershell
cd C:\Users\MSY\Desktop\reservation_web
docker compose up -d --build
```

이 방식은 다음을 함께 띄웁니다.

- MySQL
- Spring Boot 앱

확인:

```powershell
docker compose ps
```

로그 확인:

```powershell
docker compose logs -f app
docker compose logs -f mysql
```

## 7. 공유기 포트포워딩

외부 인터넷에서 접속하려면 공유기 설정이 필요합니다.

권장 설정:

- 외부 `80` -> 내부 서버 `80`
- 외부 `443` -> 내부 서버 `443`

임시 테스트만 할 경우:

- 외부 `8080` -> 내부 서버 `8080`

하지만 실제 운영은 `8080` 직접 공개보다 `Nginx + 80/443` 구조를 권장합니다.

## 8. 공인 IP 또는 도메인 연결

### 고정 공인 IP가 있는 경우

도메인 DNS에 `A 레코드`를 설정합니다.

예시:

- `example.com` -> 공인 IP
- `www.example.com` -> 공인 IP

### 유동 공인 IP인 경우

다음 중 하나를 사용합니다.

- 공유기 DDNS 기능
- Cloudflare + DDNS 클라이언트
- DuckDNS 같은 DDNS 서비스

## 9. Nginx 설정

예시 파일:

- [nginx.conf.example](/C:/Users/MSY/Desktop/reservation_web/nginx.conf.example)

리눅스 보통 경로:

```bash
/etc/nginx/sites-available/reservation
```

심볼릭 링크 연결:

```bash
sudo ln -s /etc/nginx/sites-available/reservation /etc/nginx/sites-enabled/reservation
sudo nginx -t
sudo systemctl reload nginx
```

역할:

- `80`, `443` 포트 수신
- Spring Boot `8080`으로 프록시
- HTTPS 종료 처리

## 10. HTTPS 적용

우분투 기준 예시:

```bash
sudo apt update
sudo apt install nginx certbot python3-certbot-nginx
sudo certbot --nginx -d example.com -d www.example.com
```

인증서 자동 갱신 점검:

```bash
sudo certbot renew --dry-run
```

## 11. 운영 체크리스트

배포 전 확인:

- `.env`에 운영 DB 정보 입력
- `SPRING_PROFILES_ACTIVE=prod`
- 강한 DB 비밀번호 사용
- `3306` 외부 미공개
- `8080` 외부 미공개
- `80`, `443`만 외부 공개
- 도메인 연결 확인
- 업로드 폴더 백업 여부 확인
- 관리자 계정 로그인 확인
- 리뷰 이미지/공지 첨부파일 확인

## 12. 배포 후 점검

점검 순서:

1. 메인 화면 접속 확인
2. 회원 로그인 확인
3. 회원 예약 확인
4. 비회원 예약 조회 확인
5. 리뷰 등록/이미지 확인
6. 관리자 로그인 확인
7. 관리자 코스/스태프/예약 관리 확인
8. 업로드 이미지 정상 노출 확인

## 13. 추천 운영 형태

가장 추천하는 운영 방식:

- 리눅스 서버 또는 항상 켜져 있는 PC
- Docker Compose 또는 systemd
- Nginx
- HTTPS
- 도메인 연결

즉 최종 구조는 아래처럼 가는 것이 가장 무난합니다.

- 외부 사용자 -> 도메인 -> Nginx(443)
- Nginx -> Spring Boot(127.0.0.1:8080)
- Spring Boot -> MySQL(내부망)

## 14. 빠른 요약

윈도우에서 테스트용 외부 공개:

1. `.\gradlew bootRun`
2. 방화벽에서 `8080` 허용
3. 공유기에서 `8080` 포트포워딩
4. 공인 IP로 접속

실제 운영 권장 방식:

1. `.env`를 `prod` 기준으로 설정
2. `npm run build`
3. `bootJar` 또는 `docker compose up -d --build`
4. Nginx 설치
5. 공유기 `80/443` 포트포워딩
6. 도메인 연결
7. HTTPS 발급
