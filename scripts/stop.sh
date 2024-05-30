#!/bin/bash
# scripts/stop.sh

if [ "$DEPLOYMENT_GROUP_NAME" == "release" ]; then
    cd /home/ec2-user/build/release || exit
    sudo docker-compose -f /home/ec2-user/build/release/docker-compose-release.yml down -v
elif [ "$DEPLOYMENT_GROUP_NAME" == "dev" ]; then
    cd /home/ec2-user/build/dev || exit
    sudo docker-compose -f /home/ec2-user/build/dev/docker-compose-dev.yml down -v
fi
