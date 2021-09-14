#!/usr/bin/env python3

import sys
from os.path import dirname, abspath, join
import subprocess

# Note this does not resolve symbolic links
# https://stackoverflow.com/a/17806123
FIREFOX_BINARY = join(dirname(abspath(__file__)), 'firefox')

argvs = list(sys.argv)
argvs[0] = FIREFOX_BINARY

# geckdriver will run `firefox -version` first to check the version
if len(sys.argv) == 2 and sys.argv[1] == '-version':
    subprocess.check_call(argvs)
    exit(0)

# First search for the -tmpprofile option
new_profile_path = None
for idx, argv in enumerate(sys.argv):
    if argv == '-tmpprofile':
        new_profile_path = sys.argv[idx + 1]
        break
# If it's present, replace profile with tmp_profile
if new_profile_path:
    for idx, argv in enumerate(sys.argv):
        if argv == '-profile':
            old_profile_path = sys.argv[idx + 1]
            subprocess.check_call(['rm', '-r', new_profile_path])
            subprocess.check_call(['cp', '-r', old_profile_path, new_profile_path])
            argvs[idx+1] = new_profile_path
            break

# Firefox will ignore the -tmpprofile option
subprocess.check_call(argvs)
