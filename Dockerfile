#docker build -t name .
# note : -ipAdress: ipAdress mysql docker container
#docker run --name spring-tutorial -p 8080:8080 -e DBMS_CONNECTION=jdbc:mysql://ipAdress:3306/dbname name(docker-buid)
#or using docker-compose.yml

# Stage 1: build
# Start with a Maven image that includes JDK 21
FROM maven:3.9.8-amazoncorretto-21 AS build

# Copy source code and pom.xml file to /app folder
WORKDIR /app
COPY pom.xml .
COPY src ./src

#Formatted code
RUN mvn spotless:apply

# Build source code with maven
RUN mvn package -DskipTests

# Stage 2: create image
# Start with Amazon Corretto JDK 21
FROM amazoncorretto:21.0.4

# Set working folder to /app and copy the compiled file from the above step
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

