# What is Supervisor?

**Supervisor** is a process control system for Linux that allows you to:
- Monitor and control multiple processes
- Automatically restart processes if they crash
- Manage processes that need to run in the background
- Start multiple services from a single container

## Why We Use Supervisor in This Project

In our Docker container, we need to run **two services simultaneously**:
1. **MongoDB** - The database server
2. **Spring Boot Application** - The web application

Instead of running them separately, Supervisor manages both:
- Starts MongoDB first (priority 1)
- Waits a bit, then starts Spring Boot (priority 2)
- Automatically restarts either service if it crashes
- Keeps both services running in the background

## How It Works

1. **Configuration File**: `supervisord.conf` defines which programs to run
2. **Supervisor Daemon**: The `supervisord` command reads the config and starts all programs
3. **Process Management**: Supervisor monitors each process and restarts them if needed

## Supervisor Commands (Inside Container)

Once inside the container, you can use these commands:

```bash
# Check status of all services
supervisorctl status

# Start a service
supervisorctl start mongodb
supervisorctl start spring-boot

# Stop a service
supervisorctl stop mongodb
supervisorctl stop spring-boot

# Restart a service
supervisorctl restart mongodb
supervisorctl restart spring-boot

# View logs
tail -f /var/log/supervisor/mongodb.out.log
tail -f /var/log/supervisor/spring-boot.out.log
```

## Alternative Without Supervisor

Without Supervisor, you would need to:
- Run MongoDB in one terminal/process
- Run Spring Boot in another terminal/process
- Manually restart them if they crash
- Handle process management yourself

Supervisor simplifies this by managing everything automatically!
