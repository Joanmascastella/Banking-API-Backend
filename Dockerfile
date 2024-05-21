FROM ubuntu:latest AS build

# Install necessary packages
RUN apt-get update && \
    apt-get install -y openjdk-21-jdk maven

# Set the working directory
WORKDIR /app

# Copy the project files
COPY . /app

# Make sure mvnw is executable
RUN chmod +x mvnw

# Build the project
RUN ./mvnw clean install -U

# Expose the application port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["./mvnw", "spring-boot:run"]
