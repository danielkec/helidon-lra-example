#!/bin/bash
COORDINATOR_URL=http://localhost:9000/api/v1/lra-coordinator
CLIENT_ID=$((1 + $RANDOM % 100));

curl -s -I -G \
--data-urlencode "TimeLimit=0" \
--data-urlencode "ClientId=$CLIENT_ID" \
-X POST "${COORDINATOR_URL}/start"
