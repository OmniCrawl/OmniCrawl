#!/usr/bin/env bash

set -e

if [ $# -ne 3 ]; then
  echo "Usage: $0 deviceAndroidId appname profileDirectoryInTarBZ2";
  echo "E.g: $0 FOOBAR0123 org.mozilla.firefox /tmp/firefox-mobile-profile.tar.bz2";
  exit -1
fi


androidid=$1
appname=$2
profilebz2=$3

if [ ! -f "$profilebz2" ]; then
    echo "$profilebz2 : No such file or directory"
    exit -1
fi

# create src hash
if [ ! -f "${profilebz2}-hash" ]; then
  srchash=`md5sum "$profilebz2" | cut -f1 -d ' '`
  echo "$srchash" > "${profilebz2}-hash"
else
  srchash=`cat ${profilebz2}-hash`
fi
# check dst hash
dsthash=`adb -s $androidid shell cat "/data/local/tmp/${appname}-hash" || echo nohash`

ownership=`adb -s $androidid shell su -c \"stat -c %u:%g /data/data/$appname\"`

if [ ! $srchash = $dsthash ]; then 
  cat $profilebz2 | adb -s $androidid shell "cat > /data/local/tmp/$appname.tar.bz2"
  cat << EOF | adb -s $androidid shell
md5sum /data/local/tmp/$appname.tar.bz2 | cut -f1 -d ' ' > /data/local/tmp/$appname-hash
rm -rf /data/local/tmp/$appname
mkdir /data/local/tmp/$appname
toybox bunzip2 -h
if [ "\$?" == 127 ]; then
  echo "use tar bzip" 1>&2
  tar jxf /data/local/tmp/$appname.tar.bz2 -C /data/local/tmp/$appname &>/dev/null
else
  echo "use toybox bunzip2" 1>&2
  toybox bunzip2 /data/local/tmp/$appname.tar.bz2
  tar xf /data/local/tmp/$appname.tar.bz2 -C /data/local/tmp/$appname &>/dev/null
fi
rm -rf /data/local/tmp/$appname.tar.bz2
EOF
fi

cat << EOF | adb -s $androidid shell su
setenforce 0
am force-stop $appname
sync
sync
sync
rm -rf /data/data/$appname
cp -r /data/local/tmp/$appname /data/data/$appname
chown $ownership -R /data/data/$appname
EOF
