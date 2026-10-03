FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B clean package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/ODC-Academy-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
