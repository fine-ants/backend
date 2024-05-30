#!/bin/bash
# scripts/stop.sh

if [ "$DEPLOYMENT_GROUP_NAME" == "release" ]; then
    if [ -f /home/ec2-user/build/release/docker-compose-release.yml ]; then
        cd /home/ec2-user/build/release || exit
        sudo docker-compose -f /home/ec2-user/build/release/docker-compose-release.yml down -v
    else
        echo "docker-compose-release.yml 파일이 존재하지 않습니다."
    fi
elif [ "$DEPLOYMENT_GROUP_NAME" == "dev" ]; then
    if [ -f /home/ec2-user/build/dev/docker-compose-dev.yml ]; then
        cd /home/ec2-user/build/dev || exit
        sudo docker-compose -f /home/ec2-user/build/dev/docker-compose-dev.yml down -v
    else
        echo "docker-compose-release.yml 파일이 존재하지 않습니다."
    fi
fi
