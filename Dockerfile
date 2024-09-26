FROM gradle:8.10.1-jdk21 AS build
WORKDIR /home/gradle/messaner

RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g npm@latest && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY --chown=gradle:gradle . .
RUN apt-get
RUN gradle build --no-daemon
RUN ls -al /home/gradle/messaner/src/main/resources/static

FROM openjdk:21-jdk-slim
WORKDIR /app
ARG JAR_FILE=/home/gradle/messaner/build/libs/messaner-0.0.1-SNAPSHOT.jar
COPY --from=build ${JAR_FILE} .
RUN ls -al ./
ENV Spring.profiles.active=dev
ENV app.name=messaner
ENV RUN_FILE=${JAR_FILE}
EXPOSE 8080
ENTRYPOINT ["nohup", "java", "-jar", "/app/messaner-0.0.1-SNAPSHOT.jar", "1>", "~/messaner/logs", "2>&1", "&"]