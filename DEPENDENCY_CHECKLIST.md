# Dependency Checklist for Single Docker Container

## ✅ Complete Dependency List

### Build Phase Dependencies
- [x] **Maven 3.9+** - Build tool
- [x] **Java 17 JDK** - Compiler
- [x] **Maven Wrapper** (`mvnw`) - Included in project

### Runtime Dependencies

#### System Level
- [x] **Linux OS** (Ubuntu 22.04 or Alpine)
- [x] **OpenJDK 17 JRE** - Java runtime
- [x] **MongoDB 7.0** - Database server
- [x] **Supervisor** - Process manager
- [x] **Bash** - Shell interpreter
- [x] **curl/wget** - Network tools

#### Application Dependencies (Auto-included in JAR)
- [x] **Spring Boot 3.1.1**
  - [x] spring-boot-starter-web
  - [x] spring-boot-starter-security
  - [x] spring-boot-starter-data-mongodb
- [x] **JWT Libraries 0.11.5**
  - [x] jjwt-impl
  - [x] jjwt-api
  - [x] jjwt-jackson
- [x] **Lombok** (compile-time)

### Configuration Files
- [x] **application.yaml** - App configuration
- [x] **supervisord.conf** - Process manager config
- [x] **docker-entrypoint.sh** - Startup script

### Network Requirements
- [x] **Port 8080** - HTTP API
- [x] **Port 27017** - MongoDB (optional external access)

### MongoDB Setup
- [x] **Admin User**: rootuser/rootpass
- [x] **Database**: fitness-tracker
- [x] **Data Directory**: /data/db
- [x] **Log Directory**: /var/log/mongodb

## 📦 Files Created/Modified

1. **DEPENDENCIES.md** - Detailed dependency documentation
2. **Dockerfile.single-container** - Alpine-based Dockerfile
3. **Dockerfile.single-container-ubuntu** - Ubuntu-based Dockerfile (recommended)
4. **supervisord.conf** - Supervisor configuration
5. **docker-entrypoint.sh** - Container startup script
6. **SINGLE_CONTAINER_SETUP.md** - Complete setup guide
7. **DEPENDENCY_CHECKLIST.md** - This file

## 🚀 Quick Build Command

```bash
# Build
docker build -f Dockerfile.single-container-ubuntu -t fitness-app-single .

# Run
docker run -d -p 8080:8080 --name fitness-app fitness-app-single

# Test
curl http://localhost:8080/api/v1/auth/register
```

## 📊 Dependency Summary

| Category | Item | Version | Purpose |
|----------|------|---------|---------|
| **OS** | Ubuntu | 22.04 | Base operating system |
| **Runtime** | OpenJDK | 17 | Java runtime |
| **Database** | MongoDB | 7.0 | Data persistence |
| **Process Manager** | Supervisor | Latest | Manage multiple processes |
| **Framework** | Spring Boot | 3.1.1 | Application framework |
| **Security** | JWT | 0.11.5 | Authentication |
| **Build Tool** | Maven | 3.9+ | Compile application |

## ⚠️ Important Notes

1. **First Run**: MongoDB initialization takes ~30-60 seconds
2. **Data Persistence**: Use volumes for `/data/db` in production
3. **Security**: Change default passwords in production
4. **Resource Requirements**: Minimum 512MB RAM recommended
5. **Port Conflicts**: Ensure ports 8080 and 27017 are available

## 🔍 Verification Steps

After building and running:

1. ✅ Container is running: `docker ps | grep fitness-app`
2. ✅ MongoDB is accessible: `docker exec fitness-app mongosh --eval "db.version()"`
3. ✅ Spring Boot is running: `curl http://localhost:8080/actuator/health` (if actuator enabled)
4. ✅ API responds: `curl http://localhost:8080/api/v1/auth/register`
5. ✅ Supervisor manages both: `docker exec fitness-app supervisorctl status`

## 📝 All Dependencies Installed

When you build the Docker image, all these dependencies are automatically installed:

### Base Image Provides:
- Linux OS
- Package manager (apt/apk)

### Dockerfile Installs:
- OpenJDK 17
- MongoDB 7.0
- Supervisor
- Bash
- curl/wget

### Maven Build Includes:
- All Spring Boot dependencies
- JWT libraries
- MongoDB driver
- Security framework
- Web framework

### No Manual Installation Required! 🎉

Everything is automated in the Dockerfile.
