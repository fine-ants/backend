#!/bin/bash

BUILD_JAR=$(ls /home/ec2-user/action/build/libs/*.jar)
JAR_NAME=$(basename $BUILD_JAR)
echo ">>> build filename: $JAR_NAME" >> /home/ec2-user/action/deploy.log

echo ">>> copy build file" >> /home/ec2-user/action/deploy.log
DEPLOY_PATH=/home/ec2-user/action/
cp $BUILD_JAR $DEPLOY_PATH

echo ">>> check current pid application with running" >> /home/ec2-user/action/deploy.log
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z $CURRENT_PID ]
then
  echo ">>> No applications are currently running and will not shut down." >> /home/ec2-user/action/deploy.log
else
  echo ">>> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
echo ">>> deploy DEPLOY_JAR"    >> /home/ec2-user/action/deploy.log
sudo chmod 666 /var/run/docker.sock
sudo chmod +x /usr/local/bin/docker-compose
docker-compose -f /home/ec2-user/action/docker-compose-dev.yml down -v
docker-compose -f /home/ec2-user/action/docker-compose-dev.yml build
docker-compose -f /home/ec2-user/action/docker-compose-dev.yml pull
docker-compose -f /home/ec2-user/action/docker-compose-dev.yml up -d
