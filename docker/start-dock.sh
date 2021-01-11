#!/bin/sh
DIR=`dirname $0`

docker rm -f linebot

echo "✅ Start docker ..."
docker-compose -f $DIR/docker-compose.yml up -d --build linebot
