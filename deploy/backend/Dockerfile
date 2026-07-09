FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制 JAR
COPY target/techtalk-backend-1.0.0.jar app.jar

# 时区设置
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
