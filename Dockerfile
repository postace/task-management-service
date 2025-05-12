FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace/app

# Copy the project files
COPY pom.xml .
COPY src src

# Package the application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre
VOLUME /tmp
COPY --from=build /workspace/app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
