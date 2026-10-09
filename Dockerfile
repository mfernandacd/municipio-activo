FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY . .
RUN mvn -f backend/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/backend/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar
RUN mkdir -p /app/uploads && chown -R 10001:10001 /app

USER 10001:10001
ENV PORT=10000
EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
