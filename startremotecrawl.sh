#!/usr/bin/env bash
if [ $# -ne 1 ]; then
    echo "Usage: $0 [compile|run]"
    exit -1
fi

if [ $1 == "compile" ]; then
  mvn clean compile -T `nproc`C assembly:single
elif [ $1 == "run" ]; then
  killall firefox firefox-bin geckodriver chrome chromedriver
  #export DISPLAY=:0.0
  java \
    -Xmx8G \
    -Dlog4j.configurationFile=resources/log4j2.xml \
    -jar target/Webdriver-1.0-SNAPSHOT-jar-with-dependencies.jar \
    2>&1 | tee -a log/crawler.log
else
  echo "Usage: $0 [compile|run]"
  exit -1
fi


