#!/usr/bin/env bash

set -e

if [ $# -ne 3 ]; then
  echo "Usage: $0 deviceAndroidId appname outputCompressFile";
  echo "E.g: $0 FOOBAR0123 org.mozilla.firefox /tmp/firefox.tar.bz2";
  exit -1
fi

androidid=$1
appname=$2
output=$3
tmpfile="/data/local/tmp/$appname.tar.gz"
uuid=`uuidgen`

# Force-stop the application in case of race condition
adb -s $androidid shell am force-stop $appname

cat << EOF | adb -s $androidid shell su
cd /data/data/$appname
tar zcf $tmpfile \`find . -not -name cache -not -name . -maxdepth 1\`
EOF

tmptargz="/tmp/$uuid.tar.gz"
# Using adb push/pull a large file will freeze at [100%] for unknown reason.
# Current workaround is not using adb push/pull.
adb -s $androidid shell cat $tmpfile > $tmptargz

localtmpdir="/tmp/$uuid"
mkdir $localtmpdir
tar zxf $tmptargz -C $localtmpdir
tar cjSf $output -C $localtmpdir .
rm $tmptargz
rm -rf $localtmpdir
