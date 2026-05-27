# ---- Build stage --------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# Copy source and build.
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Runtime stage ------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a non-root user.
RUN addgroup -S app && adduser -S -G app app
USER app

COPY --from=build /app/target/splitwise-*.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
