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

# JVM tuning for low-resource containers (Render free tier: 512 MB / 0.1 CPU).
#
#   -Xms128m -Xmx256m       fixed heap so the JVM doesn't waste cycles
#                           growing it; leaves ~250 MB for native + metaspace
#                           + Tomcat + thread stacks within the 512 MB cap.
#   -XX:TieredStopAtLevel=1 stop at C1 JIT — slower steady-state code but
#                           dramatically faster startup on tiny CPUs, which
#                           is what matters for getting under Render's
#                           port-bind deadline.
#   -XX:+ExitOnOutOfMemoryError  if we ever do OOM, exit immediately and
#                           let Render's logs show "OutOfMemoryError" rather
#                           than the JVM struggling on for minutes first.
#   -XX:+UseContainerSupport  honour cgroup memory/CPU limits.
ENV JAVA_OPTS="-XX:+UseContainerSupport -Xms128m -Xmx256m -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError"
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
