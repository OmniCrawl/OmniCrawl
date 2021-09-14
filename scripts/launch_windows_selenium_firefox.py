#!/usr/bin/env python3
from selenium.webdriver import Firefox
from selenium.webdriver.firefox.options import Options
from selenium.webdriver.firefox.firefox_binary import FirefoxBinary
import sys, time, os

firefox_path = r'TODO: FILL'
geckodriver_path = r'TODO: FILL'

options = Options()
profile_path = None
i = 1
while i < (len(sys.argv) - 1):
    if any(j == sys.argv[i] for j in {'--profile', '-profile', '-P'}):
        profile_path = sys.argv[i+1]
        options.add_argument('-profile')
        options.add_argument(profile_path)
        i += 2
        continue
    elif sys.argv[i].startswith('--profile='):
        profile_path = sys.argv[i][len('--profile='):]
        options.add_argument('-profile')
        options.add_argument(profile_path)
        i += 1
        continue
    options.add_argument(sys.argv[i])
    i += 1

if profile_path is None:
    raise RuntimeError('You must provide a profile')

url = sys.argv[-1]

port = None
with open(os.path.join(profile_path, 'user.js')) as f:
    for l in f.read().strip().splitlines():
        if l.startswith('user_pref("marionette.port",'):
            _, _, number = l.partition(',')
            port = int(number.rstrip('); \n'))
if port is None:
    raise RuntimeError("Cannot find marionette port in user.js!")

driver = Firefox(
    firefox_binary=FirefoxBinary(firefox_path=firefox_path),
    service_args=['--marionette-port', str(port)], # depends on the profile
    executable_path=geckodriver_path,
    options=options,
    service_log_path=None
)
driver.get(url)
#time.sleep(50)
#driver.close()
#driver.quit()
