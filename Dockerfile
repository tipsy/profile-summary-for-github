FROM maven:3.5.2-jdk-8-alpine as maven-build
WORKDIR /app
COPY . .
RUN mvn install
CMD ["java", "-jar", "target/github-profile-summary-1.0-jar-with-dependencies.jar"]