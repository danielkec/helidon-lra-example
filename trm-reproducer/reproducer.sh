#!/bin/bash

# Running TMM on port 9000 is expected
# docker run --rm -v `pwd`:/app/config -w /app/config --network="host" --env CONFIG_FILE=tcs.yaml --name otmm tmm:22.1.1

cleanup() {
  echo "Cleanup ..."
  sudo killall -q MockServer
  rm ./MockServer.go
}
trap cleanup EXIT

# Setup simple HTTP server dumping all requests to this terminal
SRC=$(cat << EOF
package main; import ("fmt"; "log"; "net/http"; "net/http/httputil")
func main() { http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		reqDump, err := httputil.DumpRequest(r, true); if err != nil {log.Println(err)}
		fmt.Printf("\n==== INCOMING REQUEST ====>\n%s", string(reqDump))
	}); http.ListenAndServe(":8090", nil)}
EOF
)
echo "$SRC" > MockServer.go

# Kill any previous instances
sudo killall -q MockServer
# Run and detach
go run MockServer.go &

COORDINATOR_URL=http://localhost:9000/api/v1/lra-coordinator
CLIENT_ID=$((1 + $RANDOM % 100));
COMPLETE_URL='http://localhost:8090/lra-participant/complete/io.helidon.example.lra.booking.BookingResource/paymentSuccessful'
COMPENSATE_URL='http://localhost:8090/lra-participant/compensate/io.helidon.example.lra.booking.BookingResource/paymentFailed'
COMPENSATION_LINKS="<$COMPENSATE_URL>; rel=\"compensate\"; title=\"compensate URI\"; type=\"text/plain\""
COMPENSATION_LINKS="$COMPENSATION_LINKS,<$COMPLETE_URL>; rel=\"complete\"; title=\"complete URI\"; type=\"text/plain\""

# Start LRA
LRA_ID=$(curl -s -I -G \
--data-urlencode "TimeLimit=10000" \
--data-urlencode "ClientId=$CLIENT_ID" \
-X POST "${COORDINATOR_URL}/start" | \
grep -Fi 'Long-Running-Action' | \
sed -r 's/Long-Running-Action:\s*([a-zA-Z0-9\:\/\-\.]]*)/\1/' | tr -d '\r')

echo "LRA started - raw LRA_ID $LRA_ID"
LRA_ID=$(echo "$LRA_ID" | sed -r 's/.*lra-coordinator\/([a-zA-Z0-9\-]*)/\1/')
echo "LRA_ID - $LRA_ID"

# JOIN LRA
RECOVERY_URL=$(curl -s \
-H "Link: $COMPENSATION_LINKS" \
-d "$COMPENSATION_LINKS" \
-X PUT "${COORDINATOR_URL}/$LRA_ID?TimeLimit=30000")

echo "Join - RECOVERY_URL - $RECOVERY_URL"

# CLOSE LRA
CLOSE_RESULT=$(curl -s \
-H "Link: $COMPENSATION_LINKS" \
-d "$COMPENSATION_LINKS" \
-X PUT "${COORDINATOR_URL}/$LRA_ID/close")
echo "Closing LRA - $LRA_ID - $CLOSE_RESULT"

# !!! This pause is needed for reproducing the issue  !!!
# If you comment it out, TMM calls compensation endpoint of second LRA as expected
sleep 1

# Start another LRA
LRA_ID=$(curl -s -I -G \
--data-urlencode "TimeLimit=30000" \
--data-urlencode "ClientId=$CLIENT_ID" \
-X POST "${COORDINATOR_URL}/start" | \
grep -Fi 'Long-Running-Action' | \
sed -r 's/Long-Running-Action:\s*([a-zA-Z0-9\:\/\-\.]]*)/\1/' | tr -d '\r')

echo "Second LRA started - raw LRA_ID $LRA_ID"
LRA_ID=$(echo "$LRA_ID" | sed -r 's/.*lra-coordinator\/([a-zA-Z0-9\-]*)/\1/')
echo "LRA_ID - $LRA_ID"

# Join second LRA
RECOVERY_URL=$(curl -s \
-H "Link: $COMPENSATION_LINKS" \
-d "$COMPENSATION_LINKS" \
-X PUT "${COORDINATOR_URL}/$LRA_ID?TimeLimit=10000")

echo "Joined second LRA - RECOVERY_URL - $RECOVERY_URL"

echo "Waiting 60 sec for compensate after timeout(should be after 30 sec)"

for i in {60..1}; do
  sleep 1
  echo -ne "$i\033[0K\r"
done

echo "Waited for 60 seconds, exiting"