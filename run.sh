#!/usr/bin/env bash

# Build images.
docker build -t odinms_mysql odinms_mysql
docker build -t odinms odinms

# Create private bridge network - connects the game and database servers.
NETWORK=`docker network ls | grep thepack | awk {'print $2'}`
if [[ ! $NETWORK == "thepack" ]]; then
	docker network create thepack
fi

# Run containers.
docker run -d --network thepack --name maplestory-db odinms_mysql
docker run -d -p 7575:7575 -p 7576:7576 -p 7577:7577 -p 7578:7578 -p 8484:8484 --network thepack --name maplestory odinms

