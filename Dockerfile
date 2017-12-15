# For DockerFile Test
FROM ubuntu:latest
RUN apt-get update \
	&& apt-get install -y openjdk-8-jdk openjdk-8-jre maven git
WORKDIR /root
RUN git clone https://github.com/tipsy/github-profile-summary.git \
	&& cd github-profile-summary \
	&& mvn install
EXPOSE 7070
ENTRYPOINT java -jar /root/github-profile-summary/target/github-profile-summary-1.0-jar-with-dependencies.jar \
	&& /bin/bash