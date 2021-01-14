FROM b2papp.azurecr.io/b2p/basedocker:1.2.0

# create app folder
RUN mkdir -p /opt/app

# install BASH
RUN apk update
RUN apk add bash

# copy application.jar to docker
COPY build/libs/line-bot-0.0.1-SNAPSHOT.jar /opt/app/linebot.jar

# change working directory
WORKDIR /opt/app

EXPOSE 8080

RUN ["chmod", "+x", "/opt/app/linebot.jar"]
