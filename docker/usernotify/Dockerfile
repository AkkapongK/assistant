FROM b2papp.azurecr.io/b2p/basedocker:9.3.2

# create app folder
RUN mkdir -p /opt/app

# install BASH
RUN sed -i -e 's/http:/https:/' /etc/apk/repositories
RUN apk update
RUN apk add bash

# copy application.jar to docker
COPY build/libs/usernotify-0.0.1-SNAPSHOT.jar /opt/app/usernotify.jar
COPY lib/dd-java-agent-0.41.0.jar /opt/app/dd-java-agent.jar
COPY lib/elastic-apm-agent.jar /opt/app/elastic-apm-agent.jar

# change working directory
WORKDIR /opt/app

EXPOSE 10550

RUN ["chmod", "+x", "/opt/app/usernotify.jar"]
RUN ["chmod", "+x", "/opt/app/dd-java-agent.jar"]
RUN ["chmod", "+x", "/opt/app/elastic-apm-agent.jar"]
