# Snap-Spot
Annoymous location sharing API

##Usage
_Create Trip:_ `curl -X POST -d  "remaining-minutes=30" http://localhost:9000/v1/trip/1234`

_Delete Trip:_ `curl -X DELETE  http://localhost:9000/v1/trip/1234`

_Update Trip Duration:_ `curl -X PUT -d  "secret=db976777-657d-4003-bdfa-f514c9bd43d5&remaining-minutes=10" http://localhost:9000/v1/trips/1249`

_Add Position_ `curl -X POST -d  "secret=<trip-secret>&lat=30&lon=30" http://localhost:9000/v1/trips/1249/positions/`

