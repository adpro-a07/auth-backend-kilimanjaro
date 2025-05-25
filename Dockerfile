# Build stage
FROM docker.io/library/eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /src/kilimanjaro
COPY . .
RUN ./gradlew clean bootjar

# Runtime stage
FROM docker.io/library/eclipse-temurin:21-jre-jammy AS runner

ARG USER_NAME=kilimanjaro_usr
ARG USER_UID=1000
ARG USER_GID=${USER_UID}

# Create user and group
RUN groupadd -g ${USER_GID} ${USER_NAME} && \
    useradd -m -d /opt/kilimanjaro -u ${USER_UID} -g ${USER_GID} ${USER_NAME}

# Install dependencies for gRPC
RUN apt-get update && \
    apt-get install -y --no-install-recommends libstdc++6 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set up application
USER ${USER_NAME}
WORKDIR /opt/kilimanjaro
COPY --from=builder --chown=${USER_UID}:${USER_GID} /src/kilimanjaro/build/libs/*.jar app.jar

EXPOSE 8090 9090

# Configure JVM options for better container performance
ENTRYPOINT ["java"]
CMD ["-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar", "--server.port=8090"]
