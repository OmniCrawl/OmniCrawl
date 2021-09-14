#!/usr/bin/env bash

if [ $# -ne 2 ]; then
  echo "Usage: $0 deviceAndroidId path";
  echo "E.g: $0 FOOBAR0123 /tmp/firefox-mobile.png";
  exit -1
fi

androidid=$1
png=$2
tmppng="/data/local/tmp/screncap.png"
adb -s $androidid shell screencap -p $tmppng
adb -s $androidid shell cat $tmppng > $png
