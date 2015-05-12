#!/bin/sh

cd /app
exec lein run >> /var/log/snap-spot-api 2>&1
