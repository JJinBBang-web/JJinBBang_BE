# syntax=docker/dockerfile:1

### 빌드 단계 ###
FROM gradle:8.7-jdk17-alpine AS builder
WORKDIR /workspace/app

# 캐싱 최적화를 위해 Gradle wrapper와 설정 파일을 먼저 복사
COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle
RUN chmod +x gradlew

# 애플리케이션 소스 복사
COPY src ./src
COPY wait-for-it.sh ./wait-for-it.sh

# Spring Boot 애플리케이션 빌드 및 실행 가능한 JAR 추출
RUN ./gradlew --no-daemon clean bootJar \
    && JAR_FILE="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)" \
    && cp "${JAR_FILE}" app.jar

### 런타임 단계 ###
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# 애플리케이션 아티팩트 복사
COPY --from=builder /workspace/app/app.jar ./app.jar
COPY --from=builder /workspace/app/wait-for-it.sh ./wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]