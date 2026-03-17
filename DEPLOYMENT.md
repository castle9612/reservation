# Deployment Guide

## Current readiness

This project is ready to deploy with:

- Spring Boot on port `8080`
- MySQL via `.env`
- React static build served by Spring Boot
- optional reverse proxy with Nginx

Recommended production shape:

1. `Spring Boot` app on `127.0.0.1:8080`
2. `MySQL` on the same server or private network
3. `Nginx` in front of Spring Boot
4. `HTTPS` with Let's Encrypt
5. domain connected to your public IP

## 1. Prepare `.env`

Create `.env` in the project root:

```env
DB_USERNAME=root
DB_PASSWORD=change-this-password
DB_URL=jdbc:mysql://mysql:3306/reservation?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
SPRING_PROFILES_ACTIVE=prod
```

If MySQL is on the same machine without Docker:

```env
DB_URL=jdbc:mysql://127.0.0.1:3306/reservation?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
```

## 2. Build and run

```powershell
cd C:\Users\MSY\Desktop\reservation_web\src\main\frontend
cmd /c npm run build

cd C:\Users\MSY\Desktop\reservation_web
.\gradlew clean test bootJar
java -jar .\build\libs\reservation_web-0.0.1-SNAPSHOT.jar
```

If the jar name differs, use the actual file inside `build\libs`.

## 3. Docker Compose option

```powershell
cd C:\Users\MSY\Desktop\reservation_web
docker compose up -d --build
```

This starts:

- MySQL on `3306`
- app on `8080`

## 4. External access basics

For public access from the internet, you need all of these:

1. public IP or domain
2. router port forwarding
3. firewall allow rules
4. reverse proxy
5. HTTPS

## 5. Port forwarding

On your router:

- forward external `80` to your server `80`
- forward external `443` to your server `443`

If you are not using Nginx yet and want a temporary test:

- forward external `8080` to server `8080`

Recommended final production is `80/443` through Nginx, not direct `8080` exposure.

## 6. Domain / public IP

### If you have a fixed public IP

Create DNS records:

- `A` record for `example.com` -> your public IP
- `A` record for `www.example.com` -> your public IP

### If you have a dynamic public IP

Use one of:

- your router's DDNS feature
- Cloudflare + dynamic DNS client
- DuckDNS or similar DDNS service

Then point your domain or DDNS hostname to the current public IP.

## 7. Nginx reverse proxy

Sample config is here:

- [nginx.conf.example](/C:/Users/MSY/Desktop/reservation_web/nginx.conf.example)

Typical Linux path:

```bash
/etc/nginx/sites-available/reservation
```

Then enable it:

```bash
sudo ln -s /etc/nginx/sites-available/reservation /etc/nginx/sites-enabled/reservation
sudo nginx -t
sudo systemctl reload nginx
```

## 8. HTTPS with Let's Encrypt

Typical Ubuntu example:

```bash
sudo apt update
sudo apt install nginx certbot python3-certbot-nginx
sudo certbot --nginx -d example.com -d www.example.com
```

After HTTPS is issued successfully, Certbot usually updates the Nginx config automatically.

Check renewal:

```bash
sudo certbot renew --dry-run
```

## 9. Windows Firewall / server firewall

If the app server itself is exposed internally:

```powershell
New-NetFirewallRule -DisplayName "Reservation HTTP 80" -Direction Inbound -Protocol TCP -LocalPort 80 -Action Allow
New-NetFirewallRule -DisplayName "Reservation HTTPS 443" -Direction Inbound -Protocol TCP -LocalPort 443 -Action Allow
```

Temporary direct app exposure only:

```powershell
New-NetFirewallRule -DisplayName "Reservation App 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```

## 10. Production checks

- set `SPRING_PROFILES_ACTIVE=prod`
- keep `.env` out of git
- do not expose MySQL directly to the internet
- use strong DB password
- open only `80/443` publicly when Nginx is used
- keep `8080` private if possible
- keep `3306` private
- verify uploads directory backup policy

## 11. Test checklist

From outside your local machine:

1. open `http://your-domain` or `https://your-domain`
2. confirm homepage loads
3. confirm login works
4. confirm reservation submit works
5. confirm uploaded images load
6. confirm admin page still requires admin login

## 12. Recommended final setup

Best practice:

- public internet -> Nginx (`80/443`)
- Nginx -> Spring Boot (`127.0.0.1:8080`)
- Spring Boot -> MySQL (`127.0.0.1:3306` or docker private network)

This is safer than opening Spring Boot directly to the internet.
