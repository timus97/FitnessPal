# Fitness Tracking Application

Fitness Tracking Application is a web application that allows users to track their workouts, daily macros, and calories. Users can create an account, log in, and utilize various features to monitor their fitness progress.

## Features

- User Registration: Users can create an account by providing their details such as name, email, and password.
- Authentication and Authorization: JWT-based authentication is implemented to secure user access. HttpOnly cookies are used to store the JWT token for enhanced security.
- Role-Based Access Control: Two roles are defined: Admin and User. Admins have additional privileges such as adding new products to the system and managing user accounts.
- Workout Tracking: Users can track their workouts by recording the intensity, burned calories, and exercise type.
- Daily Macros and Calorie Tracking: Users can monitor their daily macros and calorie intake to maintain a healthy diet.
- Food Catalog: Users can search for food items and add them to their daily food catalog, enabling them to track their nutrition accurately.
- Product Management: Admins have the ability to add new products to the system, ensuring a comprehensive food catalog for users.
- **Weight Tracker:** Users can log weight entries and track their weight-loss journey. The app auto-calculates BMI (when height is set) and returns the user's current BMI plus a target (goal) weight and target BMI—either user-set or suggested from a healthy BMI of 22.

## Technologies Used

The project is built using the following technologies:

- Spring Boot: Provides the foundation for developing the application.
- Spring Security: Ensures secure authentication and authorization using JWT tokens stored in HttpOnly cookies.
- Spring Data: Simplifies database interactions with MongoDB.
- MongoDB: A NoSQL database used for storing user accounts, workouts, and food items.
- Docker: Used to containerize the MongoDB database for easy deployment and scalability.
- Hibernate: An Object-Relational Mapping (ORM) framework for managing the database entities.


## API Endpoints

The application exposes the following RESTful API endpoints:

### Authentication Endpoints
- `POST /api/v1/auth/register` - Register a new user account.
- `POST /api/v1/auth/authenticate` - User login to obtain JWT token (stored in HttpOnly cookie).
- `POST /api/v1/auth/logout` - User logout.

### User Endpoints (Requires Authentication)
- `GET /api/v1/secured/user` - Get current user's profile information.
- `PUT /api/v1/secured/user/profile` - Update profile (height in cm, target weight in kg). Body: `{ "heightCm": 175, "targetWeightKg": 70 }`.
- `GET /api/v1/secured/user/credentials` - Get user credentials only.
- `GET /api/v1/secured/user/products` - Get user's added products.
- `POST /api/v1/secured/user/products/{code}` - Add a product to user's list.

### Weight Tracker Endpoints (Requires Authentication)
- `POST /api/v1/secured/user/weight` - Add a weight entry. Body: `{ "weightKg": 75.5, "date": "2025-03-03", "note": "optional" }`. BMI is auto-calculated when user has height set.
- `GET /api/v1/secured/user/weight?from=2025-01-01&to=2025-03-03` - Get weight entries (optional date range).
- `GET /api/v1/secured/user/weight/journey` - Get weight-loss journey summary: current weight, current BMI, target weight, target BMI (user-set or suggested from healthy BMI 22), and recent entries.
- `DELETE /api/v1/secured/user/weight/{id}` - Delete a weight entry.

### Workout Endpoints (Requires Authentication)
- `POST /api/v1/secured/user/workout` - Add a new workout.
- `DELETE /api/v1/secured/user/workout/{id}` - Delete a workout by ID.

### Admin Endpoints (Requires Admin Role)
- `GET /api/v1/secured/admin/users` - Get all users information.
- `GET /api/v1/secured/admin/product` - Get all products in the catalog.
- `POST /api/v1/secured/admin/product` - Add a new product to the catalog.
- `POST /api/v1/secured/admin/product/image/{code}` - Upload product image.
- `DELETE /api/v1/secured/admin/product/{code}` - Delete a product from the catalog.

## Running the Application

### Prerequisites

- Docker installed on your Linux machine
- Linux OS (the container runs on Linux)

### Single Container Setup

This setup runs both MongoDB and the Spring Boot application in a single Docker container. You will build, start, and test the application inside the container.

#### Step 1: Build the Docker Image

Build the Docker image from the Dockerfile:

```bash
docker build -t fitness-app .
```

This will:
- Clone the repository from GitHub
- Install all dependencies (Java 17, Maven, MongoDB, Supervisor, curl, etc.)
- Build the Spring Boot application
- Configure MongoDB and the application

#### Step 2: Run the Container

Start the container in detached mode:

```bash
docker run -d -p 8080:8080 --name fitness-app fitness-app
```

Or run in interactive mode to see logs immediately:

```bash
docker run -it -p 8080:8080 --name fitness-app fitness-app
```

#### Step 3: Verify Container is Running

Check if the container is running:

```bash
docker ps
```

You should see the `fitness-app` container in the list.

#### Step 4: Check Application Logs

View the application logs to ensure everything started correctly:

```bash
docker logs -f fitness-app
```

Wait for messages indicating:
- MongoDB has started successfully
- Spring Boot application has started on port 8080

You should see something like: `Started FitnessTrackingAppApplication in X.XXX seconds`

#### Step 5: Access the Container for Testing

Execute into the running container to test the application:

```bash
docker exec -it fitness-app bash
```

You are now inside the container. The working directory is `/testbed/fitnesspal` where the application code is located.

#### Step 6: Verify Services are Running

Inside the container, check if both services are running:

```bash
# Check MongoDB status
supervisorctl status mongodb

# Check Spring Boot status
supervisorctl status spring-boot

# View all services
supervisorctl status
```

Both should show `RUNNING` status.

**Note:** If this is the first time running the container, you may need to initialize MongoDB with the admin user. See "MongoDB Initialization" section below.

#### Step 7: Test the Application

You can now test the application using curl commands (see Testing section below). The application is accessible at `http://localhost:8080` from inside the container.

#### Step 8: Exit and Manage Container

- **Exit the container:** Type `exit` or press `Ctrl+D`
- **Stop the container:** `docker stop fitness-app`
- **Start the container again:** `docker start fitness-app`
- **Remove the container:** `docker rm -f fitness-app`
- **View logs:** `docker logs fitness-app`
- **Restart container:** `docker restart fitness-app`

### MongoDB Initialization (First Run Only)

If this is the first time running the container, you need to initialize MongoDB with the admin user and database. **Do this inside the container:**

```bash
# Access the container
docker exec -it fitness-app bash

# Wait for MongoDB to be ready (it should already be running via supervisor)
# Initialize MongoDB admin user
mongosh admin --eval "db.createUser({user: 'rootuser', pwd: 'rootpass', roles: [{role: 'root', db: 'admin'}]})"

# Create fitness-tracker database and user
mongosh admin -u rootuser -p rootpass --eval "use fitness-tracker; db.createUser({user: 'rootuser', pwd: 'rootpass', roles: [{role: 'readWrite', db: 'fitness-tracker'}]})"

# Verify it worked
mongosh admin -u rootuser -p rootpass --eval "db.adminCommand('listDatabases')"
```

**Note:** If you see "user already exists" errors, that's fine - it means MongoDB was already initialized.

### Container Management Commands

```bash
# Start container
docker start fitness-app

# Stop container
docker stop fitness-app

# Restart container
docker restart fitness-app

# Remove container
docker rm -f fitness-app

# View logs
docker logs fitness-app

# Follow logs in real-time
docker logs -f fitness-app

# Execute command in running container
docker exec -it fitness-app bash

# Check container status
docker ps -a | grep fitness-app
```

### What is Supervisor?

**Supervisor** is a process manager that runs multiple services in a single container. In this setup, it manages:
- **MongoDB** - Database server
- **Spring Boot** - Web application

Supervisor automatically starts both services, monitors them, and restarts them if they crash. See `SUPERVISOR_EXPLANATION.md` for more details.

**Useful Supervisor commands (inside container):**
```bash
supervisorctl status          # Check all services
supervisorctl restart mongodb  # Restart MongoDB
supervisorctl restart spring-boot  # Restart Spring Boot
```

## Testing the Application

This section provides detailed steps to test the backend API using `curl` commands **inside the Docker container**. The application uses JWT tokens stored in HttpOnly cookies for authentication.

### Prerequisites for Testing

1. **Ensure the container is running:**
   ```bash
   docker ps | grep fitness-app
   ```

2. **Access the container:**
   ```bash
   docker exec -it fitness-app bash
   ```

3. **Verify services are running:**
   ```bash
   supervisorctl status
   ```

   Both `mongodb` and `spring-boot` should show `RUNNING` status.

4. **Verify the application is accessible:**
   ```bash
   curl -i http://localhost:8080/api/v1/auth/register
   ```

   You should receive a response (likely an error about missing request body, which confirms the server is running).

**Note:** All curl commands below should be executed **inside the container** where you have shell access. The application is accessible at `http://localhost:8080` from within the container.

### Step 2: Register a New User

Register a regular user:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstname": "John",
    "lastname": "Doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "dateOfBirth": "1990-01-15"
  }'
```

**Expected Response:** `200 OK` with a success message.

Register an admin user (if your system supports it during registration, otherwise you may need to manually set the role in the database):

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstname": "Admin",
    "lastname": "User",
    "email": "admin@example.com",
    "password": "admin123",
    "dateOfBirth": "1985-05-20"
  }'
```

### Step 2: Authenticate (Login)

Login and save the cookie for subsequent requests:

```bash
curl -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

**Expected Response:** `200 OK` with authentication success. The JWT token is stored in the HttpOnly cookie and saved to `cookies.txt`.

**Note:** The `-c cookies.txt` flag saves cookies to a file, and `-b cookies.txt` loads them for subsequent requests. The cookie file will be saved in `/testbed/fitnesspal/` directory inside the container.

### Step 3: Test User Endpoints

#### Get User Profile

```bash
curl -X GET http://localhost:8080/api/v1/secured/user \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** User information including firstname, lastname, email, workouts, and products.

#### Get User Credentials Only

```bash
curl -X GET http://localhost:8080/api/v1/secured/user/credentials \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** User credentials (email and possibly other credential information).

#### Get User's Products

```bash
curl -X GET http://localhost:8080/api/v1/secured/user/products \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** List of products added by the user (initially empty).

### Step 4: Test Admin Endpoints (As Admin)

**Note:** To test admin endpoints, you need a user with ADMIN role. You may need to manually set the role in MongoDB or create an admin user during registration. For now, we'll test with a regular user to see the authorization error.

First, login as admin (if you have an admin account):

```bash
curl -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -c admin-cookies.txt \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

**Alternative:** If you don't have an admin user, you can create one by modifying the user role in MongoDB:

```bash
# Inside the container, access MongoDB
mongosh admin -u rootuser -p rootpass

# In MongoDB shell, switch to fitness-tracker database
use fitness-tracker

# Update a user to have ADMIN role (replace email with your user's email)
db.users.updateOne(
  { email: "john.doe@example.com" },
  { $set: { role: "ADMIN" } }
)

# Exit MongoDB
exit
```

#### Get All Products (Admin)

```bash
curl -X GET http://localhost:8080/api/v1/secured/admin/product \
  -b admin-cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** List of all products in the catalog.

#### Add a New Product (Admin)

```bash
curl -X POST http://localhost:8080/api/v1/secured/admin/product \
  -b admin-cookies.txt \
  -H "Content-Type: application/json" \
  -d '{
    "code": 1001,
    "name": "Chicken Breast",
    "description": "Grilled chicken breast, 100g",
    "calories": 165,
    "protein": 31.0,
    "carbs": 0.0,
    "fat": 3.6
  }'
```

**Expected Response:** `200 OK` with success message.

Add more products:

```bash
curl -X POST http://localhost:8080/api/v1/secured/admin/product \
  -b admin-cookies.txt \
  -H "Content-Type: application/json" \
  -d '{
    "code": 1002,
    "name": "Brown Rice",
    "description": "Cooked brown rice, 100g",
    "calories": 111,
    "protein": 2.6,
    "carbs": 23.0,
    "fat": 0.9
  }'
```

```bash
curl -X POST http://localhost:8080/api/v1/secured/admin/product \
  -b admin-cookies.txt \
  -H "Content-Type: application/json" \
  -d '{
    "code": 1003,
    "name": "Banana",
    "description": "Medium banana",
    "calories": 105,
    "protein": 1.3,
    "carbs": 27.0,
    "fat": 0.4
  }'
```

#### Get All Users (Admin)

```bash
curl -X GET http://localhost:8080/api/v1/secured/admin/users \
  -b admin-cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** List of all users in the system.

#### Delete a Product (Admin)

```bash
curl -X DELETE http://localhost:8080/api/v1/secured/admin/product/1003 \
  -b admin-cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** `200 OK` with success message.

### Step 5: Add Products to User's List

Switch back to regular user cookies and add products:

```bash
curl -X POST http://localhost:8080/api/v1/secured/user/products/1001 \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

```bash
curl -X POST http://localhost:8080/api/v1/secured/user/products/1002 \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** `200 OK` with success message.

Verify products were added:

```bash
curl -X GET http://localhost:8080/api/v1/secured/user/products \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** List containing the products you just added.

### Step 6: Test Workout Endpoints

#### Add a Workout

```bash
curl -X POST http://localhost:8080/api/v1/secured/user/workout \
  -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{
    "workoutDate": "2024-01-15",
    "exerciseType": "Running",
    "performanceMetricsResponse": {
      "duration": 30.0,
      "caloriesBurned": 300,
      "intensity": 7
    }
  }'
```

**Expected Response:** `200 OK` with workout information including the generated ID.

Add another workout:

```bash
curl -X POST http://localhost:8080/api/v1/secured/user/workout \
  -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{
    "workoutDate": "2024-01-16",
    "exerciseType": "Weight Training",
    "performanceMetricsResponse": {
      "duration": 45.0,
      "caloriesBurned": 400,
      "intensity": 8
    }
  }'
```

#### Get User Profile (to see workouts)

```bash
curl -X GET http://localhost:8080/api/v1/secured/user \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** User information including the workouts you just added.

#### Delete a Workout

First, get the workout ID from the user profile response, then delete it:

```bash
curl -X DELETE http://localhost:8080/api/v1/secured/user/workout/{workout-id} \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

Replace `{workout-id}` with the actual workout ID from the previous response.

**Expected Response:** `200 OK` with success message.

### Step 7: Test Logout

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** `200 OK` with logout success message.

After logout, try accessing a secured endpoint:

```bash
curl -X GET http://localhost:8080/api/v1/secured/user \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** `401 Unauthorized` or `403 Forbidden` (authentication required).

### Step 8: Test Error Cases

#### Try to access secured endpoint without authentication:

```bash
curl -X GET http://localhost:8080/api/v1/secured/user \
  -H "Content-Type: application/json"
```

**Expected Response:** `401 Unauthorized` or `403 Forbidden`.

#### Try to register with existing email:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstname": "John",
    "lastname": "Doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "dateOfBirth": "1990-01-15"
  }'
```

**Expected Response:** Error message indicating email already exists.

#### Try to access admin endpoint as regular user:

```bash
curl -X GET http://localhost:8080/api/v1/secured/admin/users \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** `403 Forbidden` (insufficient privileges).

### Complete Testing Workflow Script

Here's a complete bash script that tests all endpoints in sequence. **Save this script inside the container** at `/testbed/fitnesspal/test-api.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"
USER_EMAIL="testuser@example.com"
ADMIN_EMAIL="admin@example.com"
PASSWORD="password123"

echo "=== Testing Fitness Tracking Application ==="
echo "Running inside Docker container..."

# Step 1: Register User
echo -e "\n1. Registering user..."
curl -X POST ${BASE_URL}/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"firstname\": \"Test\",
    \"lastname\": \"User\",
    \"email\": \"${USER_EMAIL}\",
    \"password\": \"${PASSWORD}\",
    \"dateOfBirth\": \"1990-01-15\"
  }"

# Step 2: Login
echo -e "\n\n2. Logging in..."
curl -X POST ${BASE_URL}/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d "{
    \"email\": \"${USER_EMAIL}\",
    \"password\": \"${PASSWORD}\"
  }"

# Step 3: Get User Profile
echo -e "\n\n3. Getting user profile..."
curl -X GET ${BASE_URL}/api/v1/secured/user \
  -b cookies.txt

# Step 4: Login as Admin (if exists)
echo -e "\n\n4. Logging in as admin..."
curl -X POST ${BASE_URL}/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -c admin-cookies.txt \
  -d "{
    \"email\": \"${ADMIN_EMAIL}\",
    \"password\": \"${PASSWORD}\"
  }"

# Step 5: Add Product as Admin
echo -e "\n\n5. Adding product as admin..."
curl -X POST ${BASE_URL}/api/v1/secured/admin/product \
  -b admin-cookies.txt \
  -H "Content-Type: application/json" \
  -d '{
    "code": 2001,
    "name": "Test Product",
    "description": "Test product description",
    "calories": 100,
    "protein": 10.0,
    "carbs": 20.0,
    "fat": 5.0
  }'

# Step 6: Add Product to User
echo -e "\n\n6. Adding product to user..."
curl -X POST ${BASE_URL}/api/v1/secured/user/products/2001 \
  -b cookies.txt

# Step 7: Add Workout
echo -e "\n\n7. Adding workout..."
curl -X POST ${BASE_URL}/api/v1/secured/user/workout \
  -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{
    "workoutDate": "2024-01-20",
    "exerciseType": "Cycling",
    "performanceMetricsResponse": {
      "duration": 60.0,
      "caloriesBurned": 500,
      "intensity": 8
    }
  }'

# Step 8: Get User Profile (with workouts and products)
echo -e "\n\n8. Getting updated user profile..."
curl -X GET ${BASE_URL}/api/v1/secured/user \
  -b cookies.txt

# Step 9: Logout
echo -e "\n\n9. Logging out..."
curl -X POST ${BASE_URL}/api/v1/auth/logout \
  -b cookies.txt

echo -e "\n\n=== Testing Complete ==="
```

**To use this script inside the container:**

1. **Access the container:**
   ```bash
   docker exec -it fitness-app bash
   ```

2. **Create the script file:**
   ```bash
   cd /testbed/fitnesspal
   cat > test-api.sh << 'EOF'
   [paste the script content above]
   EOF
   ```

3. **Make it executable:**
   ```bash
   chmod +x test-api.sh
   ```

4. **Run the script:**
   ```bash
   ./test-api.sh
   ```

### Troubleshooting

#### Inside the Container

1. **Check if services are running:**
   ```bash
   supervisorctl status
   ```
   Both `mongodb` and `spring-boot` should show `RUNNING`.

2. **Restart a service if needed:**
   ```bash
   supervisorctl restart mongodb
   supervisorctl restart spring-boot
   ```

3. **Check MongoDB connection:**
   ```bash
   mongosh admin -u rootuser -p rootpass --eval "db.version()"
   ```

4. **Check application logs:**
   ```bash
   tail -f /var/log/supervisor/spring-boot.out.log
   tail -f /var/log/supervisor/mongodb.out.log
   ```

5. **Check if port 8080 is listening:**
   ```bash
   netstat -tlnp | grep 8080
   # or
   ss -tlnp | grep 8080
   ```

#### From Host Machine

1. **Connection Refused:**
   - Ensure the container is running: `docker ps | grep fitness-app`
   - Check container logs: `docker logs fitness-app`
   - Verify port mapping: `docker port fitness-app`

2. **401 Unauthorized:**
   - Check if cookies are being sent: Use `-v` flag with curl to see request/response headers
   - Verify login was successful and cookie file exists
   - Make sure you're using `-b cookies.txt` in curl commands

3. **403 Forbidden:**
   - Ensure you're using the correct role (admin vs user)
   - Check if the user has the required permissions
   - Verify user role in MongoDB

4. **MongoDB Connection Error:**
   - Verify MongoDB is running inside container: `docker exec fitness-app supervisorctl status mongodb`
   - Check MongoDB logs: `docker exec fitness-app tail -f /var/log/supervisor/mongodb.out.log`

5. **Application Not Starting:**
   - Check application logs: `docker logs fitness-app`
   - Check supervisor logs: `docker exec fitness-app tail -f /var/log/supervisor/supervisord.log`
   - Verify all dependencies are installed correctly
   - Rebuild the image if needed: `docker build -t fitness-app .`

6. **Container exits immediately:**
   - Check logs: `docker logs fitness-app`
   - Run container interactively to see errors: `docker run -it --name fitness-app fitness-app`
   - Verify entrypoint script: `docker exec fitness-app cat /docker-entrypoint.sh`

