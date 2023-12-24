#!/bin/bash

BUILD_JAR=$(ls /home/ec2-user/build/build/libs/*.jar)
JAR_NAME=$(basename $BUILD_JAR)
echo ">>> build filename: $JAR_NAME" >> /home/ec2-user/build/deploy.log

echo ">>> copy build file" >> /home/ec2-user/build/deploy.log
DEPLOY_PATH=/home/ec2-user/build/
cp $BUILD_JAR $DEPLOY_PATH

sudo chmod 666 /var/run/docker.sock
sudo chmod +x /usr/local/bin/docker-compose
sudo docker-compose -f /home/ec2-user/build/docker-compose-dev.yml down -v
sudo docker-compose -f /home/ec2-user/build/docker-compose-dev.yml build
sudo docker-compose -f /home/ec2-user/build/docker-compose-dev.yml pull
sudo docker-compose -f /home/ec2-user/build/docker-compose-dev.yml up -d
