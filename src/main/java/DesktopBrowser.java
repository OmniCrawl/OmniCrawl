import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper Class for creating Browser instances and capabilities
 */
public class DesktopBrowser extends AbstractBrowser {
    String browserName;
    String browserNickname;
    int hardTimeoutSec;
    String proxyAddr;
    String profileDirectoryPath;
    Logger logger;
    WebDriver driver;
    boolean resetProfileBeforeVisiting;
    String tmpProfilePath;

    public static int windowWidth = 1600;
    public static int windowHeight = 900;

    public static final String tmpProfilePrefix = "crawler-tmp-";

    public DesktopBrowser(String browserName, String browserNickname, int hardTimeoutSec, String proxyAddr,
                          String profileDirectoryPath,
                          boolean resetProfileBeforeVisiting) {
        this.browserName = browserName;
        this.browserNickname = browserNickname;
        this.hardTimeoutSec = hardTimeoutSec;
        this.proxyAddr = proxyAddr;
        this.profileDirectoryPath = profileDirectoryPath;
        this.logger = LogManager.getLogger(browserNickname);
        this.resetProfileBeforeVisiting = resetProfileBeforeVisiting;
        try {
            this.tmpProfilePath = Files.createTempDirectory(tmpProfilePrefix).toString();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create tmp profile!");
        }
        this.driver = create(browserName, hardTimeoutSec, proxyAddr, profileDirectoryPath, resetProfileBeforeVisiting);
    }

    public void quit() {
        if (isReady()) {
            this.driver.quit();
            this.driver = null;
        } else {
            throw new RuntimeException("This webdirver has already quit. You cannot quit twice!");
        }
    }

    public void restart() {
        if (isReady())
            quit();
        this.driver = create(browserName, hardTimeoutSec, proxyAddr, profileDirectoryPath, resetProfileBeforeVisiting);
    }

    public boolean isReady() {
        return this.driver != null;
    }

    private WebDriver create(String browser, int timeoutSec, String proxyAddr, String profileDirectoryPath,
                             boolean resetProfileBeforeVisiting) {
        if (proxyAddr != null && proxyAddr.isEmpty()) {
            throw new IllegalArgumentException("You cannot pass an empty proxy address. Use null instead.");
        }
        if (profileDirectoryPath == null || profileDirectoryPath.isEmpty()) {
            throw new IllegalArgumentException("Profile directory path cannot be empty or null. If you want to start " +
                "from a stateless crawl, consider specifying a empty directory.");
        }
        WebDriver driver = null;

        // Create the webdriver here is prone to race condition when the machine is heavy-loaded
        // In other workds, the webdriver has not yet fully booted.
        // Therefore, retrying helps to address this issue.
        // - Chrome: org.openqa.selenium.WebDriverException: unknown error: DevToolsActivePort file doesn't exist
        // - firefox: org.openqa.selenium.WebDriverException: connection refused
        final int maxRetry = 3;
        int retry = maxRetry;
        boolean isCreateSuccess = false;
        while (!isCreateSuccess && retry > 0) {
            try {
                switch (browser.toLowerCase()) {
                    case "chrome69":
                        DesiredCapabilities chromeCaps69 = standardChromeCaps(proxyAddr, profileDirectoryPath,
                            resetProfileBeforeVisiting, Config.PATH_TO_CHROME69_BINARY, this.tmpProfilePath);
                        logger.info("Desktop Chrome 69 is creating...");
                        driver = buildChromeDriver(chromeCaps69, profileDirectoryPath, resetProfileBeforeVisiting);
                        logger.info("Desktop Chrome 69 driver created!");
                        break;
                    case "chrome72":
                        DesiredCapabilities chromeCaps72 = standardChromeCaps(proxyAddr, profileDirectoryPath,
                            resetProfileBeforeVisiting, Config.PATH_TO_CHROME72_BINARY, this.tmpProfilePath);
                        logger.info("Desktop Chrome 72 is creating...");
                        driver = buildChromeDriver(chromeCaps72, profileDirectoryPath, resetProfileBeforeVisiting);
                        logger.info("Desktop Chrome 72 driver created!");
                        break;
                    case "chrome88":
                        DesiredCapabilities chromeCaps88 = standardChromeCaps(proxyAddr, profileDirectoryPath,
                            resetProfileBeforeVisiting, Config.PATH_TO_CHROME88_BINARY, this.tmpProfilePath);
                        logger.info("Desktop Chrome 88 is creating...");
                        driver = buildChromeDriver(chromeCaps88, profileDirectoryPath, resetProfileBeforeVisiting);
                        logger.info("Desktop Chrome 88 driver created!");
                        break;
                    case "firefox62":
                        FirefoxOptions options62 = standardFirefoxOptions(proxyAddr, profileDirectoryPath,
                            this.tmpProfilePath, Config.PATH_TO_FIREFOX62_WRAPPER);
                        logger.info("Desktop Firefox 62 is creating...");
                        driver = buildFirefoxDriver(options62, profileDirectoryPath, resetProfileBeforeVisiting,
                            this.tmpProfilePath);
                        logger.info("Desktop Firefox 62 driver created!");
                        break;
                    case "firefox65":
                        FirefoxOptions options65 = standardFirefoxOptions(proxyAddr, profileDirectoryPath,
                            this.tmpProfilePath, Config.PATH_TO_FIREFOX65_WRAPPER);
                        logger.info("Desktop Firefox 65 is creating...");
                        driver = buildFirefoxDriver(options65, profileDirectoryPath, resetProfileBeforeVisiting,
                            this.tmpProfilePath);
                        logger.info("Desktop Firefox 65 driver created!");
                        break;
                    case "firefox65ghostery":
                        FirefoxOptions options65ghostery = standardFirefoxOptions(proxyAddr, profileDirectoryPath,
                            this.tmpProfilePath, Config.PATH_TO_FIREFOX65_WRAPPER);
                        logger.info("Desktop Firefox 65 with Ghostery is creating...");
                        driver = buildFirefoxDriver(options65ghostery, profileDirectoryPath, resetProfileBeforeVisiting,
                            this.tmpProfilePath);
                        logger.info("Desktop Firefox 65 with Ghostery driver created!");
                        break;
                    case "firefox86":
                        FirefoxOptions options86 = standardFirefoxOptions(proxyAddr, profileDirectoryPath,
                            this.tmpProfilePath, Config.PATH_TO_FIREFOX86_WRAPPER);
                        logger.info("Desktop Firefox 86 is creating...");
                        driver = buildFirefoxDriver(options86, profileDirectoryPath, resetProfileBeforeVisiting,
                            this.tmpProfilePath);
                        logger.info("Desktop Firefox 86 driver created!");
                        break;
                    case "opera":
                        // TODO: DesiredCapabilities is deprecated. Use Options instead.
                        driver = new OperaDriver(standardOperaCaps(proxyAddr, profileDirectoryPath, resetProfileBeforeVisiting));
                        logger.info("Desktop Opera driver created!");
                        break;
                    default:
                        throw new UnsupportedOperationException("Desktop browser " + browser + " not found!");
                }
                isCreateSuccess = true;
            } catch (WebDriverException exp) {
                exp.printStackTrace();
                logger.error("Fail to create desktop webdriver for " + browser);
                retry--;
                logger.error("try again: " + String.valueOf(retry) + " times chance left");
            }
        }

        if (retry == 0) {
            String msg = "Fail to create desktop webdriver " + browser + " " + String.valueOf(maxRetry) + " times.";
            logger.fatal(msg);
            throw new RuntimeException(msg);
        }

        //set Timeout
        driver.manage().timeouts().pageLoadTimeout(timeoutSec, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(timeoutSec, TimeUnit.SECONDS);

        return driver;
    }

    private RemoteWebDriver buildRemoteChromeDriver(DesiredCapabilities chromeCaps, String remoteUrl) {
        RemoteWebDriver driver = null;
        try {
            driver = new RemoteWebDriver(new URL(remoteUrl), chromeCaps);
        } catch (MalformedURLException e) {
            logger.error("remote URL is broken");
            e.printStackTrace();
            System.exit(-1);
        }
        return driver;
    }

    private DesiredCapabilities standardRemoteChromeCaps(String proxy, String profile,
                                                         boolean resetProfileBeforeVisiting,
                                                         String remoteChromeBinaryPath, String tmpProfilePath,
                                                         String remoteTmpProfilePath) {
        // TODO: DesiredCapabilities is deprecated. Use Options instead.
        // Initialize capabilities/options for Chrome Driver
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

        ChromeOptions chromeOptions = new ChromeOptions();
        if (remoteChromeBinaryPath != "")
            chromeOptions.setBinary(remoteChromeBinaryPath);
        chromeOptions.addArguments("--window-size=" + String.valueOf(windowWidth) + "," + String.valueOf(windowHeight));
        // Chrome will crash on some OSs if using sandbox.
        chromeOptions.addArguments("--no-sandbox");
        if (profile != null) {
            if (resetProfileBeforeVisiting) {
                try {
                    logger.info("Running command rm -r " + tmpProfilePath);
                    Runtime.getRuntime().exec(new String[]{"rm", "-r", tmpProfilePath}).waitFor();
                    String commandLine = "cp -r " + profile + " " + tmpProfilePath;
                    logger.info("Running command " + commandLine);
                    Runtime.getRuntime().exec(new String[]{"bash", "-c",
                        commandLine
                    }).waitFor();
                } catch (IOException | InterruptedException e) {
                    logger.error("Fail to execute clean the profile");
                    logger.error("Cannot copy the profile to " + tmpProfilePath);
                }
                // Delete the cache
                // Chrome seems to cache the page too aggressively. It will lead to potential problems of injected script.
                // For example, if visiting the same static website twice, Chrome will no longer send a HTTP request.
                // The cached page will be used.
                cleanChromeCache(tmpProfilePath);
            }
            chromeOptions.addArguments("--user-data-dir=" + remoteTmpProfilePath);
        }
        if (proxy != null){
            chromeOptions.addArguments("--proxy-server=" + proxy);
        }

        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        return desiredCapabilities;
    }

    private ChromeDriver buildChromeDriver(DesiredCapabilities chromeCaps,
                                           String profileDirectoryPath, boolean resetProfileBeforeVisiting) {
        return new ChromeDriver(chromeCaps) {
            @Override
            public void quit() {
               try {
                   super.quit();
               } catch (org.openqa.selenium.remote.UnreachableBrowserException exp) {
                 // Chrome sometimes throws this error when invoking quit():
                 // org.openqa.selenium.remote.UnreachableBrowserException: Error communicating with the remote browser. It may have died.
                 // It occurs on both Chrome 69 and 72.
                 // This issue could be related to the Selenium version. Related issues:
                 // - https://stackoverflow.com/a/52779559
                 // - https://stackoverflow.com/a/42104887
                 // TODO: Is there any resources / temp profiles we should clean ourselves?
                 exp.printStackTrace();
                 logger.error("Chrome fails to quit()");
               }
            }
        };
    }

    private void cleanChromeCache(String profileDirectoryPath) {
        String cachePath = profileDirectoryPath + "/Default/Cache";
        try {
          logger.info("Executing command: rm -r " + cachePath);
          Runtime.getRuntime().exec(new String[]{"rm", "-r", cachePath}).waitFor();
        } catch (IOException | InterruptedException e) {
          logger.error("Fail to remove Chrome cache at " + cachePath);
        }
    }

    private FirefoxDriver buildFirefoxDriver(FirefoxOptions options, String profilePath,
                                             boolean resetProfileBeforeVisiting, String tmpProfilePath) {
        // TODO: exception throw when driver.quit() runs > 2000 ms
        // but it seems like the exception can simply be ignored
        //
        // org.apache.commons.exec.ExecuteException: The stop timeout of 2000 ms was exceeded (Exit value: -559038737)
        // at org.apache.commons.exec.PumpStreamHandler.stopThread(PumpStreamHandler.java:295)
        // at org.apache.commons.exec.PumpStreamHandler.stop(PumpStreamHandler.java:181)
        // at org.openqa.selenium.os.OsProcess.destroy(OsProcess.java:135)
        // at org.openqa.selenium.os.CommandLine.destroy(CommandLine.java:153)
        // at org.openqa.selenium.remote.service.DriverService.stop(DriverService.java:222)
        // at org.openqa.selenium.remote.service.DriverCommandExecutor.execute(DriverCommandExecutor.java:95)
        // at org.openqa.selenium.remote.RemoteWebDriver.execute(RemoteWebDriver.java:543)
        // at org.openqa.selenium.remote.RemoteWebDriver.execute(RemoteWebDriver.java:600)
        // at org.openqa.selenium.remote.RemoteWebDriver.quit(RemoteWebDriver.java:443)
        // at DesktopBrowser$1.quit(DesktopBrowser.java:68)
        // at Main.handleDesktop(Main.java:128)
        // at Main.main(Main.java:154)
        if (profilePath == null) {
            throw new UnsupportedOperationException("Firefox does not support stateless crawling. You must specify a " +
                "profile directory.");
        }

        // We have to manually copy the temporary session to our profile
        // https://stackoverflow.com/a/33350778
        // https://stackoverflow.com/a/46089240
        return new FirefoxDriver(options) {
            @Override
            public void quit() {
                // The original behavior already resets profile before each visiting,
                // which means it will never modifies the user-provided profile directory
                if (!resetProfileBeforeVisiting) {
                    // Geckodriver will only copy the profilePath to a temporary directory
                    // /tmp/rust_mozprofile.randomstring, and launch the driver through
                    // `firefox -profile /tmp/rust_mozprofile.randomstring`
                    // This directory will be removed once the driver quits.
                    // Thus, in order to restore the profile to our specified profilePath,
                    // we have to manually copy them.
                    //
                    // Although we can overwrite the -profile argument directly, but it doesn't work.
                    // geckodriver cannot communicate with FireFox if using customized profile.
                    // The reason may be related user_pref("marionette.port", 39369) in user.js
                    try {
                        // https://github.com/mozilla/OpenWPM/blob/a2ace2e1046bf945d7866a8a2052677f8daafa65/automation/Commands/profile_commands.py#L151-L165
                        String[] storage_files = new String[]{
                            "cookies.sqlite",  // cookies
                            "cookies.sqlite-shm",
                            "cookies.sqlite-wal",
                            "places.sqlite",  // history
                            "places.sqlite-shm",
                            "places.sqlite-wal",
                            "webappsstore.sqlite",  // localStorage
                            "webappsstore.sqlite-shm",
                            "webappsstore.sqlite-wal",
                            "webapps",  // related to localStorage?
                            "storage", // directory for IndexedDB
                        };
                        logger.info("save profile back: from " + tmpProfilePath + " to " + profilePath);
                        for (String storage_file : storage_files) {
                            String src = tmpProfilePath + "/" + storage_file;
                            String dst = profilePath;
                            Runtime.getRuntime().exec(new String[]{"cp", "-r", src, dst}).waitFor();
                        }

                        logger.info("profile saving done");
                    } catch (IOException | InterruptedException e) {
                        logger.error("Fail to move the temp profile to " + profilePath);
                    }
                }
                // Sometimes Firefox 65 will throw an error when invoking quit().
                // org.openqa.selenium.WebDriverException: Failed to decode response from marionette
                // It could be related to this bug https://stackoverflow.com/a/49800671
                try {
                    // FirefoxDriver will remove the original /tmp/rust_mozprofile.RANDOM
                    super.quit();
                } catch (org.openqa.selenium.WebDriverException exp) {
                    // Note that if we simply ignore the error, the temporary profile /tmp/rust_mozprofile.RANDOM
                    // will NOT be cleaned.
                    // TODO: manually clean /tmp/rust_mozprofile.RANDOM
                    exp.printStackTrace();
                    logger.error("Firefox fails to quit() and clean the /tmp/rust_mozprofile.RANDOM profiles.");
                }

                // TODO: do we really need to clean the tmpProfile here?
                try {
                    logger.info("Executing command: rm -r " + tmpProfilePath);
                    Runtime.getRuntime().exec(new String[]{"rm", "-r", tmpProfilePath}).waitFor();
                    logger.info("Executing command: mkdir " + tmpProfilePath);
                    Runtime.getRuntime().exec(new String[]{"mkdir", tmpProfilePath}).waitFor();
                } catch (IOException | InterruptedException e) {
                    logger.error("Fail to remove the temp profile in " + tmpProfilePath);
                }
            }
        };
    }

    /**
     * Desired Capabilities for a Desktop Chrome Browser.
     *
     * @param proxy         if not null, String specifying path to HTTP proxy
     * @return              Desired Capabilities Object
     */
    private DesiredCapabilities standardChromeCaps(String proxy, String profile, boolean resetProfileBeforeVisiting,
                                                   String chromeBinaryPath, String tmpProfilePath){
        System.setProperty("webdriver.chrome.driver", Config.PATH_TO_CHROME_DRIVER);

        // TODO: DesiredCapabilities is deprecated. Use Options instead.
        // Initialize capabilities/options for Chrome Driver
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setBinary(chromeBinaryPath);
        chromeOptions.addArguments("--window-size=" + String.valueOf(windowWidth) + "," + String.valueOf(windowHeight));
        // Chrome will crash on some OSs if using sandbox.
        chromeOptions.addArguments("--no-sandbox");
        if (profile != null) {
            if (resetProfileBeforeVisiting) {
                try {
                    logger.info("Running command rm -r " + tmpProfilePath);
                    Runtime.getRuntime().exec(new String[]{"rm", "-r", tmpProfilePath}).waitFor();
                    String commandLine = "cp -r " + profile + " " + tmpProfilePath;
                    logger.info("Running command " + commandLine);
                    Runtime.getRuntime().exec(new String[]{"bash", "-c",
                        commandLine
                    }).waitFor();
                } catch (IOException | InterruptedException e) {
                    logger.error("Fail to execute clean the profile");
                    logger.error("Cannot copy the profile to " + tmpProfilePath);
                }
                chromeOptions.addArguments("--user-data-dir=" + tmpProfilePath);
                // Delete the cache
                // Chrome seems to cache the page too aggressively. It will lead to potential problems of injected script.
                // For example, if visiting the same static website twice, Chrome will no longer send a HTTP request.
                // The cached page will be used.
                cleanChromeCache(tmpProfilePath);
            } else {
                chromeOptions.addArguments("--user-data-dir=" + profile);
            }
        }
        if (proxy != null){
            chromeOptions.addArguments("--proxy-server=" + proxy);
        }

        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

        // Enable console capturing
        // LoggingPreferences logs = new LoggingPreferences();
        // logs.enable(LogType.BROWSER, Level.ALL);
        // desiredCapabilities.setCapability(CapabilityType.LOGGING_PREFS, logs);

        return desiredCapabilities;
    }

    private FirefoxOptions standardFirefoxOptions(String proxy, String profilePath, String tmpProfilePath,
                                                  String firefoxBinaryPath){
        if (profilePath == null) {
            throw new UnsupportedOperationException("Firefox does not support stateless crawling. You must specify a " +
                "profile directory");
            // TODO: Stateless Firefox
            // 1. set up proxy
            // 2. set up mitm cert (cert9.db)
            //if (proxy != null) {
            //    String[] hostname_port = proxy.split(":");
            //    String hostname = hostname_port[0];
            //    int port = Integer.parseInt(hostname_port[1]);

            //    // Refer to https://stackoverflow.com/a/5166310
            //    profile.setPreference("network.proxy.http", hostname);
            //    profile.setPreference("network.proxy.http_port", port);
            //    profile.setPreference("network.proxy.ssl", hostname);
            //    profile.setPreference("network.proxy.ssl_port", port);
            //    profile.setPreference("network.proxy.type", 1); // 1: Manual config proxy
            //}
        } else {
            logger.warn("Note: In Firefox, specifying proxy address has no effect when using a profile");
            logger.warn("Firefox will load proxy configuration from prefs.js in the profile directory");
        }
        System.setProperty("webdriver.gecko.driver", Config.PATH_TO_FIREFOX_DRIVER);
        
        // Clean tmp profile directory
        try {
            logger.info("Running command rm -r " + tmpProfilePath);
            Runtime.getRuntime().exec(new String[]{"rm", "-r", tmpProfilePath}).waitFor();
            logger.info("Running command mkdir " + tmpProfilePath);
            Runtime.getRuntime().exec(new String[]{"mkdir", tmpProfilePath}).waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            logger.error("Cannot clean the tmp profile directory " + tmpProfilePath);
            throw new RuntimeException("Cannot clean the tmp profile directory " + tmpProfilePath);
        }


        FirefoxOptions options = new FirefoxOptions();
        FirefoxBinary bin = new FirefoxBinary(new File(firefoxBinaryPath));
        bin.addCommandLineOptions("-width", String.valueOf(windowWidth));
        bin.addCommandLineOptions("-height", String.valueOf(windowHeight));

        // This command line option does NOT present in official Firefox binary.
        // We will use a script as a wrapper and parse this option.
        // The script will use the specified -tmpprofile as the temporary profile path.
        bin.addCommandLineOptions("-tmpprofile", tmpProfilePath);
        options.setBinary(bin);


        FirefoxProfile profile = new FirefoxProfile(new File(profilePath));
        options.setProfile(profile);

        // Disable annoying verbose log
        // https://stackoverflow.com/a/46308351
        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
        return options;
    }

    private DesiredCapabilities standardOperaCaps(String proxy, String profile, boolean resetProfileBeforeVisiting){
        System.setProperty("webdriver.opera.driver", Config.PATH_TO_OPERA_DRIVER);

        // Initialize capabilities/options for OperaDriver
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

        // https://github.com/operasoftware/operachromiumdriver/issues/31#issuecomment-377510393
        // https://github.com/operasoftware/operachromiumdriver/issues/9#issuecomment-396538539
        OperaOptions options = new OperaOptions();
        options.setBinary(Config.PATH_TO_OPERA_BINARY);
        options.addArguments("--window-size=" + String.valueOf(windowWidth) + "," + String.valueOf(windowHeight));
        if (profile != null) {
            if (resetProfileBeforeVisiting) {
                try {
                    Runtime.getRuntime().exec(new String[]{"bash", "-c",
                        "rm -rf /tmp/opera-profile"
                    }).waitFor();
                    Runtime.getRuntime().exec(new String[]{"bash", "-c",
                        "cp -r " + profile + " /tmp/opera-profile"
                    }).waitFor();
                } catch (IOException | InterruptedException e) {
                    logger.error("Cannot overwrite /tmp/opera-profile");
                }
                options.addArguments("--user-data-dir=/tmp/opera-profile");
            } else {
                options.addArguments("--user-data-dir=" + profile);
            }
        }
        if (proxy != null) {
            options.addArguments("--proxy-server=" + proxy);
        }

        desiredCapabilities.setCapability(OperaOptions.CAPABILITY, options);

        // Enable console capturing
        // LoggingPreferences logs = new LoggingPreferences();
        // logs.enable(LogType.BROWSER, Level.ALL);
        // desiredCapabilities.setCapability(CapabilityType.LOGGING_PREFS, logs);

        return desiredCapabilities;
    }
}
