# Profiles

### Stateless profiles

### Build stateless profiles

#### Firefox 45, Firefox 65

- Create a empty Firefox profiles.
  - Firefox: Launch Firefox by `firefox --profile newProfileDir --no-remote`
- Modify the proxy settings

  - Preference -> Network settings -> Manual proxy configuration -> Fill in IP address and port -> Checked Also use this for FTP and HTTPS -> OK	

- Trust certificates

  - Visit `mitm.it` and trust the certificate. We'll keep this profile directory because both the proxy and certificate are configured. (Proxy is saved in `prefs.js` and certificate is in `cert9.db`)

- Quit Firefox

  - Press `Alt` once -> File -> Quit

- Apply Firefox general configurations

  - Modify the windows size

    - Modify `xulstore.json` in the profile directory.
  - Disable self-support in `prefs.js` (For Firefox 45)

    - https://support.mozilla.org/en-US/questions/1067502#answer-742049
    - `user_pref("browser.selfsupport.url", "");`
  - Disable safe mode in `prefs.js` or `about:config`:
    - https://stackoverflow.com/a/21294259
    - Set `toolkit.startup.max_resumed_crashes` in `about:config` to `-1`.
  - Disable session restore:
    - https://askubuntu.com/a/426448/1039231
    - `browser.sessionstore.resume_from_crash = false`
  - Disable cache:
    - http://kb.mozillazine.org/Browser.cache.check_doc_frequency
    - `browser.cache.check_doc_frequency = 1`
  - Disable update (For Firefox version < 60):
    - Edit `app.update.auto` and `app.update.enabled` in `about:config` 
    - For firefox version >= 60, See [Browsers](#Browsers) for details.

#### OpenWPM-Mobile

- OS-level font configuration
  - See https://github.com/sensor-js/OpenWPM-mobile/blob/mobile_sensors/EmulatingAndroidFonts.md
- Copy a stateless Firefox 45 profile.
- Manually parse https://github.com/sensor-js/OpenWPM-mobile/blob/1834f4c5b8fd0d3976b2e57f5310fd72860a681f/automation/DeployBrowsers/configure_firefox.py and add these configuration in `prefs.js` in the profile directory.

#### Firefox 65 Ghostery

- Copy a stateless Firefox 65 profile.
- Launch firefox with `fireflx --profile=firefox65profile` and install the Ghostery add-on on https://addons.mozilla.org/en-US/firefox/.
- Quit Firefox

#### Firefox 65 Selenium

- Copy a stateless Firefox 65 profiles.
- Add the following configutation in `users.json`:
```
user_pref("app.normandy.api_url", "");
user_pref("app.update.auto", false);
user_pref("app.update.checkInstallTime", false);
user_pref("app.update.disabledForTesting", true);
user_pref("app.update.enabled", false);
user_pref("browser.EULA.3.accepted", true);
user_pref("browser.EULA.override", true);
user_pref("browser.displayedE10SNotice", 4);
user_pref("browser.dom.window.dump.enabled", true);
user_pref("browser.download.manager.showWhenStarting", false);
user_pref("browser.laterrun.enabled", false);
user_pref("browser.link.open_external", 2);
user_pref("browser.link.open_newwindow", 2);
user_pref("browser.newtab.url", "about:blank");
user_pref("browser.newtabpage.enabled", false);
user_pref("browser.offline", false);
user_pref("browser.reader.detectedFirstArticle", true);
user_pref("browser.safebrowsing.blockedURIs.enabled", false);
user_pref("browser.safebrowsing.downloads.enabled", false);
user_pref("browser.safebrowsing.enabled", false);
user_pref("browser.safebrowsing.malware.enabled", false);
user_pref("browser.safebrowsing.passwords.enabled", false);
user_pref("browser.safebrowsing.phishing.enabled", false);
user_pref("browser.search.update", false);
user_pref("browser.selfsupport.url", "");
user_pref("browser.sessionstore.resume_from_crash", false);
user_pref("browser.shell.checkDefaultBrowser", false);
user_pref("browser.startup.homepage", "about:blank");
user_pref("browser.startup.homepage_override.mstone", "ignore");
user_pref("browser.startup.page", 0);
user_pref("browser.tabs.closeWindowWithLastTab", false);
user_pref("browser.tabs.warnOnClose", false);
user_pref("browser.tabs.warnOnOpen", false);
user_pref("browser.uitour.enabled", false);
user_pref("browser.usedOnWindows10.introURL", "about:blank");
user_pref("browser.warnOnQuit", false);
user_pref("datareporting.healthreport.about.reportUrl", "http://%(server)s/dummy/abouthealthreport/");
user_pref("datareporting.healthreport.documentServerURI", "http://%(server)s/dummy/healthreport/");
user_pref("datareporting.healthreport.logging.consoleEnabled", false);
user_pref("datareporting.healthreport.service.enabled", false);
user_pref("datareporting.healthreport.service.firstRun", false);
user_pref("datareporting.healthreport.uploadEnabled", false);
user_pref("datareporting.policy.dataSubmissionEnabled", false);
user_pref("datareporting.policy.dataSubmissionPolicyAccepted", false);
user_pref("datareporting.policy.dataSubmissionPolicyBypassNotification", true);
user_pref("devtools.console.stdout.chrome", true);
user_pref("devtools.errorconsole.enabled", true);
user_pref("dom.disable_open_during_load", false);
user_pref("dom.ipc.reportProcessHangs", false);
user_pref("dom.max_chrome_script_run_time", 30);
user_pref("dom.max_script_run_time", 30);
user_pref("dom.report_all_js_exceptions", true);
user_pref("extensions.autoDisableScopes", 10);
user_pref("extensions.blocklist.enabled", false);
user_pref("extensions.checkCompatibility.nightly", false);
user_pref("extensions.enabledScopes", 5);
user_pref("extensions.installDistroAddons", false);
user_pref("extensions.showMismatchUI", false);
user_pref("extensions.update.enabled", false);
user_pref("extensions.update.notifyUser", false);
user_pref("focusmanager.testmode", true);
user_pref("general.useragent.updates.enabled", false);
user_pref("geo.provider.testing", true);
user_pref("geo.wifi.scan", false);
user_pref("hangmonitor.timeout", 0);
user_pref("javascript.enabled", true);
user_pref("javascript.options.showInConsole", true);
user_pref("marionette.log.level", "Info");
user_pref("marionette.port", 58718);
user_pref("media.gmp-manager.updateEnabled", false);
user_pref("network.captive-portal-service.enabled", false);
user_pref("network.http.phishy-userpass-length", 255);
user_pref("network.manage-offline-status", false);
user_pref("network.sntp.pools", "%(server)s");
user_pref("offline-apps.allow_by_default", true);
user_pref("plugin.state.flash", 0);
user_pref("prompts.tab_modal.enabled", false);
user_pref("services.settings.server", "http://%(server)s/dummy/blocklist/");
user_pref("signon.rememberSignons", false);
user_pref("startup.homepage_welcome_url", "about:blank");
user_pref("startup.homepage_welcome_url.additional", "about:blank");
user_pref("toolkit.networkmanager.disable", true);
user_pref("toolkit.startup.max_resumed_crashes", -1);
user_pref("toolkit.telemetry.enabled", false);
user_pref("toolkit.telemetry.prompted", 2);
user_pref("toolkit.telemetry.rejected", true);
user_pref("toolkit.telemetry.server", "https://%(server)s/dummy/telemetry/");
user_pref("webdriver_accept_untrusted_certs", true);
user_pref("webdriver_assume_untrusted_issuer", true);
user_pref("webdriver_enable_native_events", true);
user_pref("xpinstall.signatures.required", false);
user_pref("xpinstall.whitelist.required", false);
```

- You must change the `marionette_port` above to a unique port number. `lanuch_selenium...` script will automatically fetch the port number from `user.js`.

#### Tor

- Copy a stateless firefox 65 profiles.

- Modify the following values in `about:config` or `prefs.js`:

  ```
  extensions.torbutton.use_nontor_proxy=true
  security.nocertdb=false
  security.enterprise_roots.enabled=true
  security.cert_pinning.enforcement_level=1
  toolkit.startup.max_resumed_crashes=-1
  browser.sessionstore.resume_from_crash=false
  
  network.proxy.type=1
  network.proxy.http=IP_ADDR
  network.proxy.http_port=PORT
  network.proxy.ssl=IP_ADDR
  network.proxy.ssl_port=PORT
  network.proxy.socks=<empty>
  network.proxy.socks_port=<empty>
  
  privacy.window.maxInnerWidth = 1600
  privacy.window.maxInnerHeight = 900
  ```

#### Chrome 72, Chrome 72 Selenium

- Launch Chrome with `chromium --user-data-dir=chrome72-profile` 
- Close all login tabs

#### Brave

- Launch Brave with command line options`--user-data-dir=brave-profile` 
- Modify settings to make brave **NOT** continue where you left off
  - Settings --> Startup --> Select blank page 

### Stateful profiles

To build a stateful profiles, use the stateless profiles as the starting profiles in `src/main/java/Config.java`, and modify `src/main/java/MultithreadCrawler.java`:

- For remote desktop and openwpm-mobile, specify the last parameter as `false`:
  - `new RemoteDesktopBrowser("chrome72", "r_chrome72", "172.19.159.218:10000", Config.R_CH72_SL, false)`
- For mobile browsers
  - `new MobileBrowser("duckduckgo", Config.M_DD_SL, "ZY22527SVQ", "ZY22527SVQ-dd-sl", appiumUrl, false, true)`

Then the stateful profiles will be created according to the paths in `src/main/java/Config.java`.

### Pruning profiles

After collecting a stateful profiles, we have to prune them because copying a large stateful profile is time-consuming. Please use `scripts/prune_profile.py` to prune stateful profiles.

Note: If this profile contains add-ons like ghostery, plesae run `firefox -profile profile_path http://example.com` once.
The add-on will automatically rebuild its databases. Otherwise the add-ons might not work properly.
