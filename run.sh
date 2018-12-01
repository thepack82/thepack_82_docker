#!/usr/bin/env bash

docker build -t odinms_mysql odinms_mysql
docker build -t odinms odinms

docker run -d --name odinms_mysql odinms_mysql
docker run -d -p 7575:7575 -p 7576:7576 -p 7577:7577 -p 7578:7578 -p 8484:8484 --name odinms odinms

