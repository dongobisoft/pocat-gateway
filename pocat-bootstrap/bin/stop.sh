#!/bin/sh

GATEWAY_HOME="$( cd "$( dirname "$0" )"&& cd .. && pwd -P )"
PID_FILE="${GATEWAY_HOME}/bin/gateway.pid"

if [ -f "${PID_FILE}" ]; then
  PID="$(cat "${PID_FILE}")"
  if [ -n "${PID}" ]; then
    kill "${PID}"
  fi
  rm "${PID_FILE}"
fi

