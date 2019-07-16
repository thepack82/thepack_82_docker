#!/usr/bin/env bash

java -cp '.:dist/*' -Dnet.sf.odinms.recvops=recvops.properties -Dnet.sf.odinms.sendops=sendops.properties -Dnet.sf.odinms.wzpath=wz -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd net.sf.odinms.net.world.WorldServer &
sleep 10
java -cp '.:dist/*' -Dnet.sf.odinms.recvops=recvops.properties -Dnet.sf.odinms.sendops=sendops.properties -Dnet.sf.odinms.wzpath=wz -Dnet.sf.odinms.login.config=login.properties -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd net.sf.odinms.net.login.LoginServer &
sleep 10
java -cp '.:dist/*' -Dnet.sf.odinms.recvops=recvops.properties -Dnet.sf.odinms.sendops=sendops.properties -Dnet.sf.odinms.wzpath=wz -Dnet.sf.odinms.channel.config=channel.properties -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd net.sf.odinms.net.channel.ChannelServer -Dcom.sun.management.jmxremote.port=13373 -Dcom.sun.management.jmxremote.password.file=jmxremote.password -Dcom.sun.management.jmxremote.access.file=jmxremote.access &
