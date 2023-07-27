FROM swe-arena-base

ENV COMMIT_HASH=5582b62aa1f16f41c687316088f110ce04bd9c08
ENV REPO_URL=https://github.com/timus97/FitnessPal.git
ENV REPO_NAME=fitnesspal

WORKDIR /testbed/${REPO_NAME}

RUN git init && \
  git remote add origin ${REPO_URL} && \
  git fetch --depth 1 origin ${COMMIT_HASH} && \
  git checkout FETCH_HEAD && \
  git remote remove origin


RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    maven \
    wget \
    gnupg \
    curl \
    bash \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Install MongoDB
RUN wget -qO - https://www.mongodb.org/static/pgp/server-7.0.asc | gpg --dearmor -o /usr/share/keyrings/mongodb-server-7.0.gpg && \
    echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-7.0.list && \
    apt-get update && \
    apt-get install -y mongodb-org && \
    rm -rf /var/lib/apt/lists/*


RUN mkdir -p /data/db /var/log/mongodb


RUN chmod +x mvnw mvnw.cmd 2>/dev/null || true


RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests


RUN if [ -f src/main/resources/application.properties ]; then \
        sed -i 's/spring.data.mongodb.host=mongodb/spring.data.mongodb.host=localhost/g' src/main/resources/application.properties || \
        sed -i 's/spring.data.mongodb.host=.*/spring.data.mongodb.host=localhost/g' src/main/resources/application.properties || true; \
    fi && \
    if [ -f src/main/resources/application.yaml ]; then \
        sed -i 's/host: mongodb/host: localhost/g' src/main/resources/application.yaml || \
        sed -i 's/host:.*mongodb.*/host: localhost/g' src/main/resources/application.yaml || true; \
    fi


EXPOSE 27017

# Start MongoDB only
CMD ["mongod", "--bind_ip", "0.0.0.0", "--port", "27017", "--dbpath", "/data/db"]
