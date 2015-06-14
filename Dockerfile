FROM phusion/baseimage:latest

RUN apt-get update \
  && apt-get --assume-yes --no-install-recommends install nginx leiningen redis-server python-pip wget \ 
  && echo deb http://apt.newrelic.com/debian/ newrelic non-free >> /etc/apt/sources.list.d/newrelic.list \
  && wget -O- https://download.newrelic.com/548C16BF.gpg | apt-key add - \
  && apt-get update \
  && apt-get --assume-yes --no-install-recommends install newrelic-sysmond \ 
  && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* \
  && pip install newrelic-plugin-agent

ENV LEIN_ROOT = "true"

# schedule retire trip task with cron
RUN echo "30 * * * * root cd /app/ ; lein run -m snap-spot.tasks.retire-trips > retire-trips.log 2>&1" >> /etc/cron.d/snap-spot-retire-trips-task

# configure nginix
# use ssl
#redis new relic monotoring

WORKDIR /app/

ADD project.clj /app/
RUN lein install

#need to add init scripts for lein run and nginx and cron for task

RUN nrsysmond-config --set license_key=cc10e2214fa2d6344e678152458db27623d46bb8 \
  && mkdir /etc/service/app /etc/service/redis /etc/service/new-relic \
  && mkdir /var/run/newrelic

ADD docker/app.sh /etc/service/app/run
ADD docker/redis.sh /etc/service/redis/run
ADD docker/newrelic-plugin-agent.cfg /etc/newrelic/

ADD . /app/

# Use baseimage-docker's init system.
CMD ["/sbin/my_init"]
