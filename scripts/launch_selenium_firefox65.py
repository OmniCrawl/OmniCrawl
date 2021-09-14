#!/usr/bin/env python3
from selenium.webdriver import Firefox
from selenium.webdriver.firefox.options import Options
from selenium.webdriver.firefox.firefox_binary import FirefoxBinary
from selenium.webdriver.firefox.firefox_profile import FirefoxProfile
from selenium.webdriver.common.proxy import Proxy, ProxyType
import sys, time, os

os.environ["DISPLAY"] = ":65.0"
firefox_path = '/path/to/firefox-wrapper.py'
geckodriver_path = '/path/to/geckodriver'

option = Options()
option.accept_insecure_certs = False

firefox_profile = FirefoxProfile()
i = 1
while i < (len(sys.argv) - 1):
    if any(j == sys.argv[i] for j in {'--profile', '-profile', '-P'}):
        firefox_profile = FirefoxProfile(profile_directory=sys.argv[i+1])
        i += 2
        continue
    option.add_argument(sys.argv[i])
    i += 1

def set_proxy(profile, addr_port):
    host, _, port = addr_port.partition(':')
    profile.set_preference("network.proxy.type", 1)
    profile.set_preference("network.proxy.http", host)
    profile.set_preference("network.proxy.http_port", int(port))
    profile.set_preference("network.proxy.ssl", host)
    profile.set_preference("network.proxy.ssl_port", int(port))

i = 1
while i < (len(sys.argv) - 1):
    if any(j == sys.argv[i] for j in {'--proxy', '-proxy'}):
        set_proxy(firefox_profile, sys.argv[i+1])
        i += 2
        continue
    elif sys.argv[i].startswith('--proxy='):
        p = sys.argv[i][len('--proxy='):]
        set_proxy(firefox_profile, p)
        i += 1
        continue
    i += 1


url = sys.argv[-1]

driver = Firefox(
    firefox_binary=FirefoxBinary(firefox_path=firefox_path),
    executable_path=geckodriver_path,
    firefox_profile=firefox_profile,
    options=option,
    service_log_path='/tmp/log65' #None
)
#Moto G5 Plus screen size (5.2')
driver.set_window_size(360, 592) #74
driver.get(url)

def handler(sig, frame):
    driver.close()
    driver.quit()
    exit(0)
import signal
signal.signal(signal.SIGINT, handler)
signal.signal(signal.SIGTERM, handler)
import time
while True:
    time.sleep(60)
