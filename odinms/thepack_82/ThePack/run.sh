#!/usr/bin/env bash

THIS_IP=`grep $HOSTNAME /etc/hosts | awk '{print $1}'`

sed -i "s/ODINMS_MYSQL_CONTAINER_IP_ADDRESS/$ODINMS_MYSQL_CONTAINER_IP_ADDRESS/" db.properties
sed -i "s/THIS_IP/$THIS_IP/" world.properties
sed -i "s/THIS_IP/$THIS_IP/" login.properties
sed -i "s/THIS_IP/$THIS_IP/" channel.properties

java -cp '.:dist/*' -Dnet.sf.odinms.recvops=recvops.properties -Dnet.sf.odinms.sendops=sendops.properties -Dnet.sf.odinms.wzpath=wz -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd net.sf.odinms.net.world.WorldServer &
sleep 10
java -cp '.:dist/*' -Dnet.sf.odinms.recvops=recvops.properties -Dnet.sf.odinms.sendops=sendops.properties -Dnet.sf.odinms.wzpath=wz -Dnet.sf.odinms.login.config=login.properties -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd net.sf.odinms.net.login.LoginServer &
sleep 10
java -cp '.:dist/*' -Dnet.sf.odinms.recvops=recvops.properties -Dnet.sf.odinms.sendops=sendops.properties -Dnet.sf.odinms.wzpath=wz -Dnet.sf.odinms.channel.config=channel.properties -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd net.sf.odinms.net.channel.ChannelServer -Dcom.sun.management.jmxremote.port=13373 -Dcom.sun.management.jmxremote.password.file=jmxremote.password -Dcom.sun.management.jmxremote.access.file=jmxremote.access &
