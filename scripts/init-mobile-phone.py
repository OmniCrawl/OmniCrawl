#!/usr/bin/env python3
import sys
import argparse
import subprocess
import datetime, pytz
from time import sleep
from pathlib import Path


def set_firefox_prefs(sids, browser_name, warmup=False):
    if len(sids) > 1:
        raise Exception('Can only configure firefox preferences of one phone at a time')
    sid = sids[0]
    prefs_name = 'prefs.js'
    if browser_name == 'firefox':
        ff_browser = 'org.mozilla.firefox'
    elif browser_name == 'focus':
        ff_browser = 'org.mozilla.focus'
    elif browser_name == 'tor':
        ff_browser = 'org.torproject.torbrowser'
        prefs_name = 'user.js'
    elif browser_name == 'ghostery':
        ff_browser = 'com.ghostery.android.ghostery'
    else:
        raise Exception(f'Unsupported Firefox-based browser: {browser_name}')
    try:
        if warmup:
            # Start the browser to generate profile directory
            subprocess.check_call(['adb', '-s', sid, 'shell', 'monkey', '-p', ff_browser, '-c', 'android.intent.category.LAUNCHER', str(1)])
            sleep(5)
        # Find the preferences dir
        moz_dir = f'/data/user/0/{ff_browser}/files/mozilla'
        moz_files = subprocess.check_output(['adb', '-s', sid, 'shell', 'su', '-c', 'ls', moz_dir]).decode().strip().split('\n')
        prefs_dir = None
        for moz_file in moz_files:
            if '.default' in moz_file:
                prefs_dir = moz_file
        if prefs_dir is None:
            raise Exception('Could not find preferences directory')
        prefs_file = f'{moz_dir}/{prefs_dir}/{prefs_name}'
        subprocess.check_call(['adb', '-s', sid, 'shell', 'su', '-c', 'touch', prefs_file])
        print(f'Prefs: {prefs_file}')
        # Transfer prefs
        subprocess.check_call(['adb', '-s', sid, 'shell', 'su', '-c', 'cp', prefs_file, '/sdcard/Download'])
        subprocess.check_call(['adb', '-s', sid, 'pull', f'/sdcard/Download/{prefs_name}', '.'])
        # Modify prefs
        prefs = {
            'security.nocertdb': False,
            'security.enterprise_roots.enabled': True,
            'security.cert_pinning.enforcement_level': 1,
        }
        if ff_browser == 'org.torproject.torbrowser':
            prefs.update({
                'extensions.torbutton.use_nontor_proxy': True,
                'network.proxy.type': 5,
                'network.proxy.socks': '""',
                'network.proxy.socks_port': '""',
            })
        with open(f'./{prefs_name}', 'r') as ff_prefs_file:
            lines = [x.strip() for x in ff_prefs_file.readlines()]
            not_found = []
            for k, v in prefs.items():
                found = False
                for i, line in enumerate(lines):
                    if k in line:
                        new_pref = f'user_pref("{k}", {str(v).lower()});'
                        print(f'Modifying pref: {new_pref}')
                        lines[i] = new_pref
                        found = True
                        break
                if not found:
                    not_found.append(k)
            for k in not_found:
                v = prefs[k]
                new_pref = f'user_pref("{k}", {str(v).lower()});'
                print(f'Adding pref: {new_pref}')
                lines.append(new_pref)
            file_data = '\n'.join(lines) + '\n'
        with open(f'./{prefs_name}', 'w') as ff_prefs_file:
            ff_prefs_file.write(file_data)
        # Put it back
        subprocess.check_call(['adb', '-s', sid, 'push', f'./{prefs_name}', f'/sdcard/Download/{prefs_name}'])
        subprocess.check_call(['adb', '-s', sid, 'shell', 'su', '-c', 'cp', f'/sdcard/Download/{prefs_name}', prefs_file])
        # DEBUG: subprocess.check_call(['adb', '-s', sid, 'shell', 'su', '-c', 'cat', prefs_file])
        # Force-kill the app
        pidlist = subprocess.check_output(['adb', '-s', sid, 'shell', 'su', '-c', 'ps', '|', 'grep', ff_browser]).decode().strip().split('\n')
        pids = [[x.strip() for x in y.split(' ') if x != ''][1] for y in pidlist]
        for pid in pids:
            try:
                subprocess.check_output(['adb', '-s', sid, 'shell', 'su', '-c', 'kill', '-9', pid])
            except Exception as exn:
                print(exn)
        # Cleanup
        subprocess.check_call(['rm', f'./{prefs_name}'])
    except subprocess.CalledProcessError as e:
        print(e)


def main(argv):
    print("Starting")
    if argv.sid == 'all':
        sids = [line.partition('\t')[0] for line in subprocess.check_output(['adb', 'devices']).decode().strip().splitlines()[1:]]
    else:
        sids = argv.sid.split(',')
    if argv.install_cert or argv.install_user_cert:
        # Upload system-wide certificates. The certificate will be permanent.
        # https://blog.ropnop.com/configuring-burp-suite-with-android-nougat/#mofiyingandrepackaginganapp
        # not tested yetrint("Starting...")
        mitmproxy_cert_pem = argv.cert
        assert 'PEM certificate' in subprocess.check_output(['file', mitmproxy_cert_pem]).decode().strip()
        cert_filename = subprocess.check_output(['openssl', 'x509', '-inform','PEM','-subject_hash_old','-in',mitmproxy_cert_pem]).decode().strip().splitlines()[0] + '.0'
        cert_src_path = '/data/local/tmp/' + cert_filename
        if argv.install_cert:
            cert_dst_path = '/system/etc/security/cacerts/' + cert_filename
        elif argv.install_user_cert:
            cert_dst_path = '/data/misc/user/0/cacerts-added/' + cert_filename
        for sid in sids:
            print('Install CA cert on', sid)
            subprocess.check_call(['adb', '-s', sid, 'push', mitmproxy_cert_pem, cert_src_path])
            if argv.install_cert:
                subprocess.check_call(f'echo "mount -o rw,remount /system; cp {cert_src_path} {cert_dst_path}; chmod 644 {cert_dst_path}" | adb -s {sid} shell su',shell=True)
            elif argv.install_user_cert:
                subprocess.check_call(f'echo "mkdir /data/misc/user/0/cacerts-added/" | adb -s {sid} shell su',shell=True)
                subprocess.check_call(f'echo "cp {cert_src_path} {cert_dst_path}" | adb -s {sid} shell su',shell=True)
                subprocess.check_call(f'echo "chmod 644 {cert_dst_path}" | adb -s {sid} shell su',shell=True)
    if argv.disable_update:
        # Disable moto auto updates
        for sid in sids:
            print('Disable update on', sid)
            for app in ['com.motorola.ccc.devicemanagement','com.motorola.ccc.checkin','com.motorola.ccc.mainplm','com.motorola.ccc.ota','com.motorola.ccc.notification']:
                subprocess.check_call(f'echo pm disable {app} | adb -s {sid} shell su',shell=True)
    if argv.enable_update:
        # Disable moto auto updates
        for sid in sids:
            print('Enable update on', sid)
            for app in ['com.motorola.ccc.devicemanagement','com.motorola.ccc.checkin','com.motorola.ccc.mainplm','com.motorola.ccc.ota','com.motorola.ccc.notification']:
                subprocess.check_call(f'echo pm enable {app} | adb -s {sid} shell su',shell=True)
    if argv.disable_google:
        for sid in sids:
            print('Disable Google on', sid)
            for app in ['com.google.android.googlequicksearchbox']:
                subprocess.check_call(f'echo pm disable {app} | adb -s {sid} shell su',shell=True)
    if argv.set_firefox_prefs:
        set_firefox_prefs(sids, argv.set_firefox_prefs, argv.warmup)
    if argv.install_apps:
        apk_dir = Path('mobile_browsers/')
        apps = {
            'com.android.chrome': 'com.android.chrome_88.0.4324.181-432418123.apk',
            'org.mozilla.firefox': 'org.mozilla.firefox_86.1.1-2015794881.apk',
            'com.duckduckgo.mobile.android': 'ddg_mod.apk',
            'com.ghostery.android.ghostery': 'com.ghostery.android.ghostery_22251829-22251829.apk',
            'org.mozilla.focus': 'org.mozilla.focus_8.13.1-350532254.apk',
            'com.brave.browser': 'com.brave.browser_1.20.103-412010320.apk',
            'org.torproject.torbrowser': 'org.torproject.torbrowser_10.0.12.apk',
        }
        # Reinstall all the browsers
        for sid in sids:
            print('Install apps on', sid)
            for app_name, apk_filename in apps.items():
                print(app_name)
                try:
                    subprocess.check_call(['adb', '-s', sid, 'uninstall', app_name])
                except subprocess.CalledProcessError as e:
                    print(e)
                subprocess.check_call(['adb', '-s', sid, 'install', '-r', apk_dir / apk_filename])
    if argv.set_proxy:
        for sid in sids:
            print('Set proxy on', sid)
            for attr in ['http_proxy', 'global_http_proxy_host', 'global_http_proxy_port']:
                subprocess.check_call(['adb', '-s', sid, 'shell', 'settings', 'delete', 'global', attr]);
            proxy_addr = argv.set_proxy
            subprocess.check_call(['adb', '-s', sid, 'shell', 'settings', 'put', 'global', 'http_proxy', proxy_addr])
    if argv.unset_proxy:
        for sid in sids:
            print('Unset proxy on', sid)
            subprocess.check_call(['adb', '-s', sid, 'shell', 'settings', 'put', 'global', 'http_proxy', ':0'])
    if argv.synctime:
        for sid in sids:
            print('Set time on', sid)
            subprocess.check_call(['adb', '-s', sid, 'shell', 'su', '-c', 'date -u "@%d" SET' % datetime.datetime.now(pytz.utc).timestamp()])
    print('Done')

def parseArgv():
    parser = argparse.ArgumentParser(prog=sys.argv[0])
    parser.add_argument('-s', '--sid', type=str, required=True, help='Android sid, separated by comma. You can also specify all sids by --sid all.')

    parser.add_argument('--install-cert', action='store_true', help='Install CA root certificates in system')
    parser.add_argument('--install-user-cert', action='store_true', help='Install CA user certificates in system')
    parser.add_argument('--cert', type=str, default=Path.home() / '.mitmproxy/mitmproxy-ca-cert.pem', help='CA cert path')

    parser.add_argument('--disable-update', action='store_true', help='Disable OS update on Moto G5 Plus')

    parser.add_argument('--enable-update', action='store_true', help='Enable OS update on Moto G5 Plus')

    parser.add_argument('--disable-google', action='store_true', help='Disable Google Search Box (Google assistent)')

    parser.add_argument('--synctime', action='store_true', help='Set time to current date')

    parser.add_argument('--install-apps', action='store_true', help='Install all the applications')

    parser.add_argument('--set-proxy', type=str, default=None, help='Set proxy address, format: 127.0.0.1:8000')

    parser.add_argument('--unset-proxy', action='store_true', help='Clear proxy setting')

    parser.add_argument('--set-firefox-prefs', type=str, default=None, help='Configure Firefox-based browser preferences. Expects: firefox | focus | tor | ghoster')

    parser.add_argument('--warmup', type=str, default=False, help='Warmup for configuring Firefox-based browser preferences. Expects: true | false')

    return parser.parse_args()

if __name__ == '__main__':
    argv = parseArgv()
    main(argv)
