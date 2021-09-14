#!/usr/bin/env bash

if [ $# -ne 3 ]; then
  echo "Usage: $0 deviceAndroidId appname profileDirectoryInTarBZ2";
  echo "E.g: $0 FOOBAR0123 org.mozilla.firefox /tmp/firefox-mobile-profile.tar.bz2";
  exit -1
fi

androidid=$1
appname=$2
profile=`basename $3`

ownership=`adb -s $androidid shell su -c \"stat -c %u:%g /data/data/$appname\"`
cat << EOF | adb -s $androidid shell su
setenforce 0
echo forcestop
am force-stop $appname
sync
sync
sync
rm -rf /data/data/$appname
cp -r /data/local/tmp/$profile /data/data/$appname
chown $ownership -R /data/data/$appname
EOF
