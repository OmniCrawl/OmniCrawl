#!/usr/bin/env python3
from selenium.webdriver import Chrome
from selenium.webdriver.chrome.options import Options
import sys

chrome_path = r'TODO: FILL'
chromedriver_path = r'TODO: FILL'

op = Options()
op.binary_location = chrome_path
url = None
for argv in sys.argv[1:]:
    if argv.startswith('-'):
        op.add_argument(argv)
    else:
        url = argv
driver = Chrome(executable_path=chromedriver_path, options=op)
driver.get(url)
#import time
#time.sleep(1234)
#driver.close()
#driver.quit()
