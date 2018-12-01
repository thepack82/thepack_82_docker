#!/usr/bin/env bash

sed -i "s/THIS_IP/$THIS_IP/g" ./ThePack/world.properties

cd ThePack
source compile.sh && source run.sh

sleep infinity
