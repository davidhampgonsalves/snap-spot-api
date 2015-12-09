# Snap-Spot
Annoymous location sharing API

##Usage
_Create Trip:_ `curl -X POST -H "Content-Type: application/json" -d '{"remaining-minutes":30}' http://localhost:9000/v1/trip/1234`

_Delete Trip:_ `curl -X DELETE http://localhost:9000/v1/trip/1234`

_Update Trip Duration:_ `curl -X PUT -H "Content-Type: application/json" -d '{"secret":<your-trip-secret>,"remaining-minutes":10}' http://localhost:9000/v1/trips/1249`

_Add Position_ `curl -X POST -H "Content-Type: application/json" -d '{"remaining-minutes":"30"}' http://localhost:9000/v1/trips/1234`
