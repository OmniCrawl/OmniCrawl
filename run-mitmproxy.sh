#!/bin/bash
killall -s SIGINT mitmdump
sleep 10
killall -9 mitmdump
sleep 10
for i in {10000..10009} {38080..38088} {39000..39001}; do 
    (./proxy/mitmboot.sh $i &)  &>/dev/null
done
sleep 3
ss -tlpn
