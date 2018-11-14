FROM openjdk:8
COPY ./target/ruleengine-1.0.0-SNAPSHOT.jar /app/app.jar

VOLUME /tmp
VOLUME /var/logs

# create working directory
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE="prod"

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","/app/app.jar"]