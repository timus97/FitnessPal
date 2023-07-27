# Dependencies Required for Single Docker Container

This document lists all dependencies needed to run the Fitness Tracking Application in a single Docker container.

## System-Level Dependencies

### 1. Operating System
- **Linux-based OS** (Alpine Linux or Ubuntu recommended)
  - Alpine: Lightweight, smaller image size
  - Ubuntu: More packages available, easier debugging

### 2. Java Runtime Environment
- **OpenJDK 17** (Java 17)
  - Required for running Spring Boot 3.1.1
  - Can use: `openjdk:17-jdk-alpine` or `openjdk:17-jdk-slim`

### 3. Build Tools
- **Maven 3.x** (for building the application)
  - OR use Maven Wrapper (`mvnw` / `mvnw.cmd`) included in project
  - Required to compile and package the Spring Boot application

### 4. Database Server
- **MongoDB** (latest stable version)
  - Required for data persistence
  - Port: 27017 (default)
  - Authentication: rootuser/rootpass (as per application.yaml)
  - Database name: fitness-tracker

### 5. Process Manager (Optional but Recommended)
- **Supervisor** or **dumb-init** or **tini**
  - To manage multiple processes (MongoDB + Spring Boot app) in single container
  - Ensures proper signal handling

## Application Dependencies (from pom.xml)

### Spring Boot Framework
- **Spring Boot 3.1.1** (parent)
  - Spring Boot Starter Data MongoDB
  - Spring Boot Starter Security
  - Spring Boot Starter Web

### Security & Authentication
- **JWT Libraries** (version 0.11.5)
  - `io.jsonwebtoken:jjwt-impl:0.11.5`
  - `io.jsonwebtoken:jjwt-api:0.11.5`
  - `io.jsonwebtoken:jjwt-jackson:0.11.5`

### Development Tools
- **Lombok** (compile-time only, optional at runtime)
  - Used for reducing boilerplate code

### Testing Dependencies (not required in production container)
- Spring Boot Starter Test
- Spring Security Test

## Configuration Dependencies

### Application Configuration (application.yaml)
- MongoDB connection settings:
  - Host: `localhost` (for single container) or `mongodb` (for multi-container)
  - Port: `27017`
  - Database: `fitness-tracker`
  - Username: `rootuser`
  - Password: `rootpass`
  - Authentication Database: `admin`

- JWT Configuration:
  - Secret key: `34872398FHDSFGSFGF78DSF376FSEFDSFGSF423GSDF`
  - Cookie name: `subproblem`
  - Expiration: `86400000` ms (24 hours)

## Network Dependencies

### Ports Required
- **8080**: Spring Boot application HTTP port
- **27017**: MongoDB port (internal, can be exposed if needed)

## Summary Checklist for Single Container

- [x] Linux OS (Alpine/Ubuntu base image)
- [x] OpenJDK 17
- [x] Maven (or Maven wrapper)
- [x] MongoDB server
- [x] Process manager (supervisor/dumb-init/tini)
- [x] Spring Boot application JAR file
- [x] Application configuration file (application.yaml)
- [x] MongoDB initialization (users, databases)

## Notes

1. **MongoDB in Single Container**: MongoDB needs to be installed and started as a service within the same container as the Spring Boot application.

2. **Build vs Runtime**: Maven is only needed during the build phase. The final container only needs:
   - Java 17 runtime
   - MongoDB server
   - The compiled JAR file

3. **Multi-stage Build**: Recommended approach:
   - Stage 1: Build the application (Maven + Java 17)
   - Stage 2: Runtime container (Java 17 + MongoDB + built JAR)

4. **Process Management**: Since both MongoDB and Spring Boot need to run, use a process manager to:
   - Start MongoDB first
   - Wait for MongoDB to be ready
   - Start Spring Boot application
   - Handle graceful shutdowns
