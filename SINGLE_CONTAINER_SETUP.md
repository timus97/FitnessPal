# Single Container Setup Guide

This guide explains how to run the Fitness Tracking Application in a single Docker container with all dependencies.

## Complete Dependency List

### 1. Build-Time Dependencies
- **Maven 3.9+** - Build tool for compiling the Spring Boot application
- **Java 17 JDK** - Required for compiling the application
- **Maven Wrapper** (`mvnw`) - Included in project, no separate installation needed

### 2. Runtime Dependencies

#### System Dependencies
- **Linux OS** (Ubuntu 22.04 or Alpine Linux)
- **OpenJDK 17 JRE** - Java runtime for executing the Spring Boot application
- **MongoDB 7.0** - NoSQL database server
- **Supervisor** - Process manager to run both MongoDB and Spring Boot app
- **Bash** - Shell for running initialization scripts
- **curl/wget** - For downloading MongoDB (if needed)

#### Application Dependencies (Managed by Maven)
All these are included in the JAR file when built:

**Spring Boot Framework (3.1.1)**
- `spring-boot-starter-web` - Web framework
- `spring-boot-starter-security` - Security framework
- `spring-boot-starter-data-mongodb` - MongoDB integration

**JWT Libraries (0.11.5)**
- `jjwt-impl` - JWT implementation
- `jjwt-api` - JWT API
- `jjwt-jackson` - JWT Jackson support

**Other Libraries**
- `lombok` - Code generation (compile-time only)

### 3. Configuration Dependencies
- **application.yaml** - Application configuration file
  - MongoDB connection settings
  - JWT secret and cookie configuration

## Quick Start

### Option 1: Using Ubuntu-based Dockerfile (Recommended)

```bash
# Build the image
docker build -f Dockerfile.single-container-ubuntu -t fitness-app-single .

# Run the container
docker run -d -p 8080:8080 -p 27017:27017 --name fitness-app fitness-app-single

# Check logs
docker logs -f fitness-app
```

### Option 2: Using Alpine-based Dockerfile

```bash
# Build the image
docker build -f Dockerfile.single-container -t fitness-app-single .

# Run the container
docker run -d -p 8080:8080 -p 27017:27017 --name fitness-app fitness-app-single
```

## What Gets Installed in the Container

### Build Stage
1. Maven 3.9 with Java 17
2. Application source code compilation
3. JAR file creation

### Runtime Stage
1. **Java 17 JRE** - For running the Spring Boot application
2. **MongoDB 7.0** - Database server
3. **Supervisor** - Process manager
4. **Application JAR** - Built Spring Boot application
5. **Configuration files** - application.yaml, supervisord.conf
6. **Startup script** - docker-entrypoint.sh

## Container Structure

```
Container
├── MongoDB (Port 27017)
│   ├── Data: /data/db
│   ├── Logs: /var/log/mongodb
│   └── Admin User: rootuser/rootpass
│
├── Spring Boot App (Port 8080)
│   ├── JAR: /app/app.jar
│   ├── Config: /app/application.yaml
│   └── Logs: /var/log/supervisor/spring-boot.out.log
│
└── Supervisor
    └── Manages both processes
```

## Process Flow

1. **Container starts** → `docker-entrypoint.sh` executes
2. **MongoDB initialization** (if first run):
   - Creates data directories
   - Starts MongoDB temporarily
   - Creates admin user (rootuser/rootpass)
   - Creates fitness-tracker database
   - Stops MongoDB
3. **Supervisor starts**:
   - Starts MongoDB service (priority 1)
   - Waits for MongoDB to be ready
   - Starts Spring Boot application (priority 2)
4. **Application ready** → Accessible on port 8080

## Verification

### Check if MongoDB is running:
```bash
docker exec fitness-app mongosh admin -u rootuser -p rootpass --eval "db.adminCommand('ping')"
```

### Check if Spring Boot is running:
```bash
curl http://localhost:8080/api/v1/auth/register
```

### View all logs:
```bash
docker logs fitness-app
```

### View supervisor status:
```bash
docker exec fitness-app supervisorctl status
```

## Ports

- **8080** - Spring Boot application HTTP endpoint
- **27017** - MongoDB (optional, for external access)

## Environment Variables

The application uses hardcoded values in `application.yaml`:
- MongoDB username: `rootuser`
- MongoDB password: `rootpass`
- MongoDB database: `fitness-tracker`
- JWT secret: `34872398FHDSFGSFGF78DSF376FSEFDSFGSF423GSDF`

## Data Persistence

MongoDB data is stored in `/data/db` inside the container. To persist data:

```bash
docker run -d \
  -p 8080:8080 \
  -v fitness-data:/data/db \
  --name fitness-app \
  fitness-app-single
```

## Troubleshooting

### MongoDB won't start
- Check logs: `docker logs fitness-app | grep mongodb`
- Verify permissions on `/data/db`
- Check if port 27017 is already in use

### Spring Boot won't start
- Check logs: `docker logs fitness-app | grep spring-boot`
- Verify MongoDB is running first
- Check application.yaml configuration

### Both services not starting
- Check supervisor logs: `docker exec fitness-app cat /var/log/supervisor/supervisord.log`
- Verify entrypoint script executed: `docker exec fitness-app ls -la /data/db`

## File Sizes

- Base Ubuntu image: ~80MB
- Java 17 JRE: ~200MB
- MongoDB: ~300MB
- Application JAR: ~50MB
- **Total estimated size: ~630MB**

## Notes

1. **First startup** takes longer due to MongoDB initialization
2. **Subsequent startups** are faster as data persists
3. **MongoDB authentication** is configured during first run
4. **Supervisor** ensures both services restart if they crash
5. **Single container** means simpler deployment but less isolation
