FROM maven:3.5.2-jdk-8-alpine as maven-build
WORKDIR /app
COPY . .
RUN mvn verify

FROM openjdk:8-jre-alpine
RUN adduser \
 -h /var/github-summary \
 -D -u 1000 \
 ghsum ghsum

USER ghsum
WORKDIR /var/github-summary

ENV TOKENS=""
ENTRYPOINT ["java", "-Dapi-tokens=${TOKENS}", "-jar", "github-profile-summary.jar"]
EXPOSE 7070

COPY --from=maven-build \
 --chown=ghsum:ghsum \
 /app/target/github-profile-summary-jar-with-dependencies.jar github-profile-summary.jar
