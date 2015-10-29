# Snap-Spot

Annoymous location sharing API

##Basic Usage:
_Create Trip:_ `curl -X POST -d  "remaining-minutes=30" http://localhost:9000/trip/create/1234`
* /trip/create/12331231232?duration=<minutes>
* /trip/update/12331231232?secret=<secret returned by trip create>&duration=<new duration>
* /position/add/12331231232?secret=<secret returned by trip create>&lat=43&lon=42&order=1

