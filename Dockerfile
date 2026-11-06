# Giai đoạn build: dùng Maven và JDK 21 để đóng gói ứng dụng Spring Boot.
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn/ .mvn/
# Cấp quyền chạy cho Maven Wrapper trong môi trường Linux.
RUN chmod +x mvnw

COPY src/ src/
# Đóng gói ứng dụng; bỏ qua việc chạy test trong quá trình build image.
RUN ./mvnw -B -DskipTests package

# Giai đoạn chạy: chỉ dùng JRE, không mang theo Maven và mã nguồn build.
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Tạo user hệ thống riêng để chạy ứng dụng thay vì chạy bằng root.
RUN groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --home-dir /app app

# Sao chép JAR đã đóng gói từ giai đoạn build sang image chạy.
COPY --from=build --chown=app:app /app/target/Backend-0.0.1-SNAPSHOT.jar /app/app.jar

# Chạy ứng dụng với user không có quyền root.
USER app

# Cổng mặc định khi chạy local; Render sẽ cấp giá trị PORT riêng.
EXPOSE 8080

# Ưu tiên cổng PORT do môi trường cung cấp, nếu thiếu thì dùng cổng 8080.
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
