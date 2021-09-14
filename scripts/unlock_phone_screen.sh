#!/bin/bash

set -e

if [ $# -ne 2 ]; then
  echo "Usage: $0 deviceAndroidId (lock|unlock)";
  echo "E.g: $0 FOOBAR0123 unlock";
  exit -1
fi

androidid=$1
action=$2

unlock () {
  if [[ "$2" = "unlock" ]]
  then
    echo "Unlocking $1..."
    # Turn on screen
    adb -s $1 shell input keyevent 224
    # Swipe up
    adb -s $1 shell input touchscreen swipe 930 880 930 380
    # Enter passcode
    adb -s $1 shell input text 1234
    # Send ok
    adb -s $1 shell input keyevent 66
  elif [[ "$2" = "lock" ]]
  then
    echo "Locking $1..."
    # Turn off screen
    adb -s $1 shell input keyevent 26
  else
    echo "Expected action=lock|unlock; got $2"
  fi
}

ids="TODO: FILL"

if [[ "$androidid" == "all" ]]
then
  for id in $ids
  do
    unlock $id $action
  done
else
  unlock $androidid $action
fi

echo "Done."
