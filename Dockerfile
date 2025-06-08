FROM gradle:8.8-jdk17 as compiler

USER root
WORKDIR /code

COPY settings.gradle build.gradle /code/
COPY src /code/src

RUN gradle clean bootJar --no-daemon && \
    mkdir -p /build && \
    cp build/libs/du-main*.jar /build/du-main.jar && \
    rm -rf /code

FROM openjdk:17-slim-bullseye

ENV JAVA_OPTS="-XX:+UseG1GC" \
    APP_PORT="8080" \
    POSTGRES_HOST=${POSTGRES_HOST} \
    LOG_LEVEL="INFO"

COPY --from=compiler /build/ /app/

CMD java \
    ${JAVA_OPTS} \
    -Dserver.port=${APP_PORT} \
    -Dlogging.level.root=${LOG_LEVEL} \
    -jar /app/du-main.jar
