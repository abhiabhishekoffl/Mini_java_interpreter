# Build Stage
FROM maven:3.6.3-openjdk-14 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:13-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/novalang-web-1.0.0.jar app.jar
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["java", "-jar", "app.jar"]
