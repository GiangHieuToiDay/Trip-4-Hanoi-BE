# -------- Stage 1: Build the application --------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml & download dependencies first (better build caching)
COPY pom.xml .
# Thêm -B để log gọn hơn
RUN mvn -B dependency:go-offline

# Copy source code and build the JAR
COPY src ./src
# Thêm -B để log gọn hơn
RUN mvn -B package -DskipTests

# -------- Stage 2: Run the application --------
FROM eclipse-temurin:21-jdk-alpine

# Cài đặt giờ Việt Nam (Rất quan trọng cho App của bạn)
ENV TZ=Asia/Ho_Chi_Minh
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && \
    echo "Asia/Ho_Chi_Minh" > /etc/timezone

WORKDIR /app

# Copy the final jar from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]