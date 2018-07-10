FROM mysql:5.5.46

ENV MYSQL_ROOT_PASSWORD=root

ADD thepack.sql /docker-entrypoint-initdb.d

EXPOSE 3306
