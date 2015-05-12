FROM phusion/baseimage:latest

RUN apt-get update \
 && apt-get --assume-yes install nginx leiningen cron \ 
 && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENV LEIN_ROOT = "true"

#need to add init scripts for lein run and nginx and cron for task
RUN mkdir /etc/service/app
ADD docker/app.sh /etc/service/app/run

# configure nginix
# use ssl

#configure cron task

WORKDIR /app/

ADD . /app/

RUN lein install

# Use baseimage-docker's init system.
CMD ["/sbin/my_init"]
