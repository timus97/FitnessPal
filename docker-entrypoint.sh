#!/bin/bash
set -e

# Create necessary directories
mkdir -p /data/db /var/log/mongodb /var/log/supervisor

# Set permissions for MongoDB
chown -R mongodb:mongodb /data/db /var/log/mongodb 2>/dev/null || true

# Function to wait for MongoDB to be ready
wait_for_mongodb() {
    echo "Waiting for MongoDB to start..."
    for i in {1..30}; do
        if mongosh --quiet --eval "db.adminCommand('ping')" > /dev/null 2>&1; then
            echo "MongoDB is ready!"
            return 0
        fi
        echo "Attempt $i: MongoDB not ready yet, waiting..."
        sleep 2
    done
    echo "MongoDB failed to start within 60 seconds"
    return 1
}

# Initialize MongoDB if data directory is empty
if [ ! "$(ls -A /data/db)" ]; then
    echo "Initializing MongoDB for the first time..."
    
    # Start MongoDB in background
    mongod --fork --logpath /var/log/mongodb/mongod.log --dbpath /data/db --bind_ip 0.0.0.0
    
    # Wait for MongoDB to be ready
    wait_for_mongodb
    
    # Create admin user
    echo "Creating admin user..."
    mongosh admin --eval "
        db.createUser({
            user: 'rootuser',
            pwd: 'rootpass',
            roles: [{ role: 'root', db: 'admin' }]
        });
    " || echo "Admin user may already exist"
    
    # Create fitness-tracker database and grant access
    echo "Setting up fitness-tracker database..."
    mongosh admin -u rootuser -p rootpass --eval "
        use fitness-tracker;
        db.createUser({
            user: 'rootuser',
            pwd: 'rootpass',
            roles: [{ role: 'readWrite', db: 'fitness-tracker' }]
        });
    " || echo "Database user may already exist"
    
    # Stop MongoDB (supervisor will start it)
    echo "Stopping MongoDB (supervisor will restart it)..."
    mongod --shutdown
    sleep 2
else
    echo "MongoDB data directory already exists, skipping initialization"
fi

# Execute the main command (supervisord)
exec "$@"
