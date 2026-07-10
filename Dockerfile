FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw.cmd ./

RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

COPY src src/

RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests -B -Pprod

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /build/target/recommender-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=$SPRING_PROFILES_ACTIVE"]
