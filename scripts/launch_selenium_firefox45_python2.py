#!/usr/bin/env python2
# Python 2.7 + selenium==2.53.0
from selenium.webdriver import Firefox
from selenium.webdriver.firefox.firefox_binary import FirefoxBinary
from selenium.webdriver.firefox.firefox_profile import FirefoxProfile
from selenium.webdriver.common.proxy import Proxy, ProxyType
import sys, time, os

os.environ["DISPLAY"] = ":45.0"

firefox_path = '/path/to/firefox'
argvs = []

profile_directory = None
i = 1
while i < (len(sys.argv) - 1):
    if any(j == sys.argv[i] for j in {'--profile', '-profile', '-P'}):
        profile_directory = sys.argv[i+1]
        i += 2
        continue
    if any(j == sys.argv[i] for j in {'--tmpprofile', '-tmpprofile'}):
        os.environ["TMPDIR"] = sys.argv[i+1]
        i += 2
        continue
    argvs.append(sys.argv[i])
    i += 1

firefox_profile = FirefoxProfile(profile_directory=profile_directory)

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
    i += 1


url = sys.argv[-1]
binary = FirefoxBinary(firefox_path=firefox_path)
binary.add_command_line_options(*argvs)

driver = Firefox(
    firefox_binary=binary,
    firefox_profile=firefox_profile,
)
#Moto G5 Plus screen size (5.2')
driver.set_window_size(360, 592)
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
