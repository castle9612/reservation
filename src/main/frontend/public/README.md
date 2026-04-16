# Frontend Public Assets

운영에서 파일만 교체해서 바꿀 수 있는 대표 이미지들입니다.

## 로고

- 파일명: `brand-logo.png`
- 사용 위치: 좌측 상단 헤더 로고
- 관련 코드:
  - `src/main/frontend/src/App.jsx`
  - `src/main/frontend/src/styles.css`

## 메인 비주얼 이미지

- 파일명:
  - `hero-1.jpg`
  - `hero-2.jpg`
  - `hero-3.jpg`
- 사용 위치: 메인 홈 큰 비주얼 이미지
- 관련 코드:
  - `src/main/frontend/src/App.jsx`

## 변경 방법

같은 이름으로 파일만 교체하면 됩니다.

예시:

- 로고 변경: `brand-logo.png` 교체
- 메인 비주얼 변경: `hero-1.jpg`, `hero-2.jpg`, `hero-3.jpg` 교체

변경 후에는 프론트 빌드를 다시 해야 합니다.

```powershell
cd C:\Users\MSY\Desktop\reservation_web\src\main\frontend
cmd /c npm run build
```
