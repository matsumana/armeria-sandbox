#!/bin/sh

# ref: https://medium.com/@gchudnov/trapping-signals-in-docker-containers-7a57fdda7d86

set -x

pid=0

# SIGTERM-handler
term_handler() {
  if [ $pid -ne 0 ]; then
    kill -SIGTERM "$pid"
    wait "$pid"
  fi
  exit 143; # 128 + 15 -- SIGTERM
}

# setup handler
trap 'kill ${!}; term_handler' SIGTERM

# start the app
java ${JAVA_OPTS_DEV} ${JAVA_OPTS} -jar /root/app.jar "$@" &
pid="$!"

# wait shutting down the app
while true
do
  tail -f /dev/null & wait ${!}
done
