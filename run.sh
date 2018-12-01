#!/usr/bin/env bash

# Build images.
docker build -t odinms_mysql odinms_mysql
docker build -t odinms odinms

# Create private bridge network - connects the game and database servers.
NETWORK=`docker network ls | grep thepack | awk {'print $2'}`
if [[ ! $NETWORK == "thepack" ]]; then
	docker network create thepack
fi

# Create volume for MySQL data.
VOLUME=`docker volume ls | grep -w thepack-db | awk {'print $2'}`
if [[ ! $VOLUME == "thepack-db" ]]; then
	docker volume create thepack-db
fi

# Run containers.
DB_CONTAINER=`docker container ps | grep -w odinms_mysql | awk {'print $2'}`
if [[ ! $DB_CONTAINER == "odinms_mysql" ]]; then
	docker run -d --volume thepack-db:/var/lib/mysql --network thepack --name maplestory-db odinms_mysql
fi

# Sleep needed to avoid race condition.
sleep 5

MS_CONTAINER=`docker container ps | grep -w odinms | awk {'print $2'}`
if [[ ! $MS_CONTAINER == "odinms" ]]; then

	# Best effort attempt at getting the server IP address. idk, better than localhost?
	HOST_IP=`ifconfig | grep -w inet | grep -v 127.0.0.1 | grep -v broadcast\ 172 | awk {'print $2'} | tail -n 1`

	docker run -d -e THIS_IP=${HOST_IP} -p 7575:7575 -p 7576:7576 -p 7577:7577 -p 7578:7578 -p 8484:8484 --network thepack --name maplestory odinms

	# Add "maplestory" container to the default bridge network - so it can use the host IP.
	docker network connect bridge maplestory
fi
