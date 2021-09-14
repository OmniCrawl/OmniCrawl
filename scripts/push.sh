set -ex

i="$1"
echo $i
#adb -s $i shell rm -rf '/data/local/tmp/*.tar.bz2'
for j in `ls *.tar.bz2`; do
    adb -s $i push $j /data/local/tmp/$j-file.tar.bz2
    cat << EOF | adb -s $i shell
rm -rf /data/local/tmp/$j
mkdir /data/local/tmp/$j
echo bunzip2 $j-file.tar.bz2
toybox bunzip2 -v /data/local/tmp/$j-file.tar.bz2
echo untar to /data/local/tmp/$j
tar xf /data/local/tmp/$j-file.tar.bz2 -C /data/local/tmp/$j
rm -rf /data/local/tmp/$j-file.tar.bz2
rm -rf /data/local/tmp/$j-file.tar
EOF

done
