#!/bin/bash
# usage: ./send-tcp-msg.sh "ORDER_ID|1001;PICKUP|WH1;DELIVERY|Addr 123;CLIENT_ID|C42;"

msg="$1"

printf "%s\n" "$msg" | nc localhost 5003

