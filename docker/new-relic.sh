#!/bin/sh

/etc/init.d/newrelic-sysmond start
newrelic-plugin-agent -c /etc/newrelic/newrelic-plugin-agent.cfg
