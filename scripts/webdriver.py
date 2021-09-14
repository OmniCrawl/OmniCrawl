#!/usr/bin/env python3
# Python 3.8.0
import sys
from uuid import uuid4
from subprocess import Popen, check_call, CalledProcessError
from threading import Timer
from pathlib import Path
import shlex

from flask import Flask, request

app = Flask(__name__)
key = sys.argv[3] if len(sys.argv) > 3 else str(uuid4())

@app.route('/')
def index():
    if request.args.get('key', '') != key:
        return 'You need a correct to key to access this webdriver.', 403
    cmd = request.args.get('cmd')
    args = request.args.get('args')
    if not cmd or not args:
        return 'Please specify cmd, args.', 404
    process_name = Path(cmd).name
    if process_name == 'firefox.exe' or process_name == 'chrome.exe' or process_name == 'chromium.exe':
        return 'Please do not use a common process name. We will run taskkill to kill ANY process with the name.', 404
    try:
        # Command injection is possbile!
        cmds = [cmd] + shlex.split(args, posix=False) # otherwise single backslash will be removed
        shell = (request.args.get('shell', 'false').lower() == 'true')
        if request.args.get('async', 'false').lower() == 'true':
            # async
            p = Popen(cmds, shell=shell)
        else:
            # sync
            p = check_call(cmds, shell=shell)
    except FileNotFoundError:
        return f'The command cannot execute because {cmd} is not found.', 404
    except CalledProcessError:
        return f'Fail to execute {cmd} {args}: nonzero return value', 404
    print(cmds)
    return 'Success', 200

if __name__ == '__main__':
    print('key = ' + key)
    from waitress import serve
    serve(app, host=sys.argv[1], port=int(sys.argv[2]), threads=32)
