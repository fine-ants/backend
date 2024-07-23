#!/bin/bash

if [ "$DEPLOYMENT_GROUP_NAME" == "release" ]
then
    cp -R /home/ec2-user/build/temp/* /home/ec2-user/build/release
fi

if [ "$DEPLOYMENT_GROUP_NAME" == "production" ]
then
    cp -R /home/ec2-user/build/temp/* /home/ec2-user/build/production
fi
