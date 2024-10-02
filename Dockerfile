# OpenJDK 17 Alpine 이미지를 사용
FROM openjdk:17-jdk-alpine

# JAR 파일을 빌드된 경로에서 가져옴
ARG JAR_FILE=build/libs/*.jar

# 가져온 JAR 파일을 컨테이너 내에서 app.jar로 복사
COPY ${JAR_FILE} app.jar

# JAR 파일을 실행
ENTRYPOINT ["java","-jar","/app.jar"]