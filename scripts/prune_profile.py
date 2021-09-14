#!/usr/bin/env python3
from shutil import rmtree
from pathlib import Path
Path.rmtree = lambda self: rmtree(self)
import tempfile
from itertools import product
import subprocess
import sys
from glob import glob
filename = sys.argv[1]

if sys.argv[1].endswith('.tar.bz2'):
    with tempfile.TemporaryDirectory() as tdir:
        p = Path(tdir)
        f = Path(filename)
        before_size = f.stat().st_size / 1024 / 1024
        subprocess.check_call(['tar', 'jxf', str(f), '-C', str(p)])
        paths = [p / mid / suf for mid, suf in product(
            ['app_chrome/Default', 'app_webview'],
            ['Service Worker', 'Code Cache', 'Cache']
        )]
        ff_glob = glob(str(p / Path('files/mozilla/*.default')))
        if ff_glob:
            root = Path(ff_glob[0])
            paths += [
                root / 'storage/default',
                root / 'cache2',
                root / 'datareporting'
            ]
        for path in paths:
            if path.is_dir():
                print('delete', path)
                path.rmtree()
        subprocess.check_call(['tar', 'jcf', str(f), '-C', str(p), '.'])
        after_size = f.stat().st_size / 1024 / 1024
        print(f, f'{before_size:.2f} MB -> {after_size:.2f} MB')
elif Path(filename).is_dir():
    d = Path(filename)
    paths = [
        # chrome
        d / 'Default/Cache',
        d / 'Default/Code Cache',
        d / 'Default/Service Worker',
        d / 'Stability',
        # firefox
        d / 'storage/default',
        d / 'cache2',
        d / 'datareporting'
    ]
    for path in paths:
        if path.is_dir():
            print('delete', path)
            path.rmtree()
    print('''
If this profile contains add-ons like ghostery, plesae run firefox -profile this_profile http://example.com once
The add-on will automatically rebuild its databases. Otherwise the add-ons might not work properly.
''')
else:
    print('Cannot recognize profile type')
