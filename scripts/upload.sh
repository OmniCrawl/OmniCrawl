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

# Using adb push/pull a large file will freeze at [100%] for unknown reason.
# Current workaround is not using push/pull.
cat $profilebz2 | adb -s $androidid shell "cat > /data/local/tmp/$androidid.tar.bz2"
ownership=`adb -s $androidid shell su -c \"stat -c %u:%g /data/data/$appname\"`
# Actually, we should also maintain the SELinux extended file permission
# but we execute `setenforce 0` such that it will not check SELinux permission

# Android does not have built-in `gunzip`.

# The Android `toybox bunzip2` will return an error, but the .tar.bz2 file
# is actually unzipped. So we just untar the original filename .tar.bz2

cat << EOF | adb -s $androidid shell su
setenforce 0
am force-stop $appname
rm -rf /data/data/$appname
mkdir /data/data/$appname
toybox bunzip2 -h
if [ "\$?" == 127 ]; then
  echo "use tar bzip" 1>&2
  tar jxvf /data/local/tmp/$androidid.tar.bz2 -C /data/data/$appname
else
  echo "use toybox bunzip2" 1>&2
  toybox bunzip2 -v /data/local/tmp/$androidid.tar.bz2
  tar xf /data/local/tmp/$androidid.tar.bz2 -C /data/data/$appname
fi
chown $ownership -R /data/data/$appname
EOF

exit 0