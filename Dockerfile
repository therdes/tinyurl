FROM maven:3.6-jdk-8-slim AS build

ENV APP_HOME=/usr/src/tinyurl
RUN mkdir -p $APP_HOME
WORKDIR $APP_HOME

COPY pom.xml ${APP_HOME}
COPY src ${APP_HOME}/src

RUN mvn -DskipTests=true clean package

FROM openjdk:8-jre-slim

COPY --from=build /usr/src/tinyurl/target/tinyurl.jar /usr/local/lib/tinyurl.jar

ENV JAVA_OPTS=""
ENV APP_PORT 10293

EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-jar", "/usr/local/lib/tinyurl.jar"]
