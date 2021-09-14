#!/usr/bin/env python3
import subprocess
from subprocess import Popen
import time

null = subprocess.DEVNULL

subprocess.check_call('./start-xvfb.sh', shell=True)
subprocess.check_call('./startremotecrawl.sh compile', shell=True)
text= 'start'
Popen('killall firefox firefox-bin geckodriver', shell=True).wait()
main = Popen('java -Xmx8G -Dlog4j.configurationFile=resources/log4j2.xml -jar target/Webdriver-1.0-SNAPSHOT-jar-with-dependencies.jar 2>&1 | tee -a log/crawler.log', shell=True)
main.wait()
try:
    with open('./log/checkpoint.txt') as f:
        text = "Crashed: progress " + f.read()
except FileNotFoundError:
    text = "Checkpoint file not found"
text += subprocess.check_output("cat log/crawler.log | grep 'Fatal' -i | tail",shell=True).decode().replace('"','').replace("'",'')
