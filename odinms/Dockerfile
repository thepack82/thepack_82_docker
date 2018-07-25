FROM java:6

RUN apt-get update
RUN apt-get install -y vim

RUN echo 'alias ll="ls -lha --color=auto"' >> ~/.bashrc

WORKDIR /odinms

COPY thepack_82 .

EXPOSE 7575
EXPOSE 7576
EXPOSE 7577
EXPOSE 7578
EXPOSE 8484
