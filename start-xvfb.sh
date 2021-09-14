#!/bin/bash

echo "Killing existing Xvfb instances..."
killall Xvfb

echo "Starting Xvfb display for OpenWPM-Mobile-45"
Xvfb :45 -s 360x752x24 &

echo "Starting Xvfb display for OpenWPM-Mobile-86"
Xvfb :86 -s 360x752x24 &

echo "Done"
