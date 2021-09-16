# Browser Setup

### OpenWPM-mobile

- OpenWPM-mobile (OPM) desktop browsers
  - Install [Firefox 45](https://ftp.mozilla.org/pub/firefox/releases/45.0/linux-x86_64/en-US/firefox-45.0.tar.bz2) and [Firefox 65](https://ftp.mozilla.org/pub/firefox/releases/65.0/linux-x86_64/en-US/firefox-65.0.tar.bz2) for linux.
  - Follow the instruction in [openwpm-mobile](https://github.com/sensor-js/OpenWPM-mobile/blob/1834f4c5b8fd0d3976b2e57f5310fd72860a681f/EmulatingAndroidFonts.md) to set up the fonts.
  - Firefox 45: To use selenium to drive this version, you don't need geckodriver. However, we need [Python2 selenium==2.53.0](https://github.com/sensor-js/OpenWPM-mobile/blob/1834f4c5b8fd0d3976b2e57f5310fd72860a681f/requirements.txt). Please install it.
  - Firefox 65: Use geckodriver v0.26 and Python3 Selenium.


### Android

  - The certificate, update, un-google all already done. For more information please see `./scripts/init-mobile-phone.py --help`
  - Run `./scripts/init-mobile-phone.py -s all --install-apps` to install apps on all mobile phones.
    - apks: https://www.dropbox.com/sh/0elofqs7wg2m8sf/AAD_YNIhw5MT9yigJVHgQTrOa?dl=0
  - Run `./scripts/init-mobile-phone.py -s ADB_ID --set-proxy 128.237.152.11:38001` to set the proxy ip of the phone.

#### Certificates

We install the mitmproxy certificate as a system root certificate, so except for Firefox-based browsers, you don't have to import the certificates.

#### Browser settings

- Open each app to skip all the first-launch pop-ups
  - Chrome: Not help make Chrome better -> Accept and continue -> No thanks
    - Firefox: 
    - Brave: Accept and continue
    - Firefox Focus: Skip
    - DuckDuckGo: Continue -> Continue
    - Ghostery: Done
    - UCBrowser: Tap the right-corner X -> Deny all permissions -> Agree and enter -> Tap the right-corner X -> Deny all permissions
- Firefox-based: Visit `mitm.it` and download the certificate. Choose "trust to identiy websites".
- Chrome-useragent: `--user-agent="az3"` See https://stackoverflow.com/a/52948221/11712282

#### Tor on Android

```
extensions.torbutton.use_nontor_proxy=true
network.proxy.type=5
network.proxy.socks = "" (empty)
network.proxy.socks_port = "" (empty)

security.nocertdb=false
security.enterprise_roots.enabled=true
security.cert_pinning.enforcement_level=1
```

in `about:config`

Download cert in `mitm.it` and verify as websites.

### Windows Browsers

All browser executables have to be renamed such that `taskkill` can kill the process with the image name. For example, rename `firefox.exe` to `firefox65.exe`.

#### Windows Selenium browsers

For Selenium-drived browsers, use the scripts in `scripts/launch_windows*` as the executable wrapper. The script has to be first compiled with `pyinstaller -F`, and renamed to match the image name.

For example, if the executable is named `seleleniumfirefox65.exe`, both the pyinstaller-wrapped script and the underlying browser executable have to be named `seleleniumfirefox65`.

##### Firefox 65 Selenium Windows

- Because Selenium IPC is much slower on Windows (especially on HDD), we have a optimized Windows version of `launch_selenium...`. 

- You must change the `marionette_port` to a unique port number. `lanuch_selenium...` will automatically fetch the port number from `user.js`.

- In the origin version, selenium will disable a few security features,  but we removed those lines. Here are the lines included in the origin Selenium version:

```
user_pref("security.certerrors.mitm.priming.enabled", false);
user_pref("security.csp.enable", false);
user_pref("security.fileuri.origin_policy", 3);
user_pref("security.fileuri.strict_origin_policy", false);
```

##### Firefox Selenium Wrapper scripts explained

1. We pass `-profile Profile -tmpprofile TmpProfile` to `launch_selenium...py` script. The TmpProfile should be an empty directory.
2. The script uses Selenium API to create a FirefoxProfile based on `-profile Profile`.
3. Selenium / geckodriver will create a tmp directory with random filename `/tmp/rust_mozprofile3aAK6d/` containing a copy of the profile (but not exactly the same)
4. Selenium executes the `firefox-wrapper.py` with arguments `-profile /tmp/rust_mozprofile3aAK6d/ -tmpprofile TmpProfile`.
5. The wrapper simply copies `/tmp/rust_mozprofile3aAK6d/` to TmpProfile.
6. If it's stateless crawl, we can just delete the TmpProfile here. If not, we'll copy the files in TmpProfile back to original Profile.

#### Tor

- Tor browser 9.0.4: https://archive.torproject.org/tor-package-archive/torbrowser/9.0.4/
- Download the installer and specify the install path.
- To launch the browser without tor protocol, specify in Windows environment variable: `TOR_SKIP_LAUNCH=1`. [reference here](https://www.ghacks.net/2018/11/26/can-you-use-the-tor-browser-without-tor-connection/#comment-4443139)
- Delete `updater.exe` in tor installation directory


#### Brave

- Brave 1.2.43 (based on Chromium 79.1.2.43): https://github.com/brave/brave-browser/releases/tag/v1.2.43
- Extract Brave in `program files`. Delete `BraveUpdate.exe` to prevent it from auto-updating.


#### Chrome
- See https://www.chromium.org/getting-involved/download-chromium#TOC-Downloading-old-builds-of-Chrome-Chromium

- chrome 69.0.3497.81
  https://commondatastorage.googleapis.com/chromium-browser-snapshots/index.html?prefix=Win_x64/576753/
- chrome 72.0.3626.81
  https://commondatastorage.googleapis.com/chromium-browser-snapshots/index.html?prefix=Win_x64/612439/
- chrome latest 79.0.3945.88 (2020/01/06)
  https://commondatastorage.googleapis.com/chromium-browser-snapshots/index.html?prefix=Win_x64/706915/

- Use --window-size to specify the window size, `--check-for-update-interval=60480000` to disable update check.

#### Firefox

- Always download `.exe` so we can use 7zip to extract the `core` folder from it.
- Firefox 45:https://ftp.mozilla.org/pub/firefox/releases/45.0/win64/en-US/Firefox%20Setup%2045.0.exe
- Firefox 65: https://ftp.mozilla.org/pub/firefox/releases/65.0/win64/en-US/
- windows size is stored in `xulstore.json`.
- proxy is in `prefs.json` / `users.json`
- https cert is in `cert9.db`
- Disable update:
  - Remove `updater.exe` in the directory
  - Disable auto downloading updates:
    - `< firefox60`: disable `app.update.auto` and `app.update.enabled` in `about:config` 
    - `>= firefox60`: Using [policy](https://github.com/mozilla/policy-templates/blob/master/README.md#disableappupdate). Create a directory `distribution` in the install directory, and create a file `policies.json` with `{"policies": {"DisableAppUpdate": true, "DisableSystemAddonUpdate": true}}`.

##### Caveats

- The temporary files and directories sometimes make the HDD slower. Frequently remove `rust_mozprofile.*`, `mozilla-temp*` and , `crawler-tmp*` under `/tmp/`.
