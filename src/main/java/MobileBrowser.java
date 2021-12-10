import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class MobileBrowser extends AbstractBrowser{

    private static String platformName = "Android";
    private static String platformVersion = "8.1";
    // Appium should never timeout, otherwise it will kill the session
    private static int newCommandTimeout = 0;
    // Don't reset the application because most browsers have first-time use pop-ups,
    // which is highly possible to block the requests
    private static boolean noReset = true;

    String browserName;
    String profilePath;
    String deviceId;
    String deviceName;
    String appiumUrl;
    Logger logger;
    boolean resetProfileBeforeVisiting;
    boolean saveBackToProfileDirectory;
    AppiumDriver<MobileElement> driver;
    String appName = null;

    public MobileBrowser(String browserName, String profilePath, String deviceId, String deviceName,
                         String appiumUrl, boolean resetProfileBeforeVisiting) {
        this(browserName,profilePath,deviceId,deviceName,appiumUrl,resetProfileBeforeVisiting, false);
    }

    public MobileBrowser(String browserName, String profilePath, String deviceId, String deviceName,
                         String appiumUrl, boolean resetProfileBeforeVisiting, boolean saveBackToProfileDirectory) {
        this.browserName = browserName;
        this.profilePath = profilePath;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.appiumUrl = appiumUrl;
        this.logger = LogManager.getLogger(deviceName + ": "+ browserName);
        this.resetProfileBeforeVisiting = resetProfileBeforeVisiting;
        this.saveBackToProfileDirectory = saveBackToProfileDirectory;
        this.driver = create(browserName, profilePath, deviceId, deviceName, appiumUrl, resetProfileBeforeVisiting);
        if (this.appName == null) {
            throw new RuntimeException("Must set appName in constructor. Do you forget to set it in create()?");
        }
    }

    public void restart() {
        // It's necessary to create this browser again before invoking driver.get(URL)
        // Otherwise the Appium session will be terminated by other MobileBrowsers
        // There can only be one Appium session at a time on a mobile phone.

        if (resetProfileBeforeVisiting) {
            this.driver = create(browserName, profilePath, deviceId, deviceName, appiumUrl, resetProfileBeforeVisiting);
        } else {
            // If we don't need to reset profile, then it's the same using current profile in the device.
            this.driver = create(browserName, "", deviceId, deviceName, appiumUrl, resetProfileBeforeVisiting);
        }
    }

    public void get(String url) {
        try {
            this.driver.get(url);
        } catch(NoSuchSessionException ex) {
            throw new RuntimeException("If you have instantiate multiple MobileBrowser on a mobile phone, please " +
                    "invoke restart() before visiting any URL. There can only be an Appium session at a time on a " +
                    "mobile phone.");
        }
    }

    public void saveBackToProfile() {
        if (!saveBackToProfileDirectory)
            return;
        logger.info("save mobile phone profile back to " + profilePath);
        try {
            // The script will force-stop the application to prevent race condition
            String[] cmd = new String[]{
                "bash", "scripts/download.sh", this.deviceId, appName, profilePath
            };
            String logLine = "Executing ";
            for (String s: cmd) {
                logLine += s + " ";
            }
            logger.info(logLine);
            if (0 != Runtime.getRuntime().exec(cmd).waitFor())
                throw new RuntimeException("download.sh return non-zero value");
        } catch (IOException | InterruptedException | RuntimeException e) {
            logger.fatal("Fail to download to " + profilePath + " from mobile phone");
            System.exit(-1);
        }
        logger.info("done");
    }

    private AppiumDriver<MobileElement> create(String browser, String profilePath, String deviceId,
                                              String deviceName, String appiumURL, boolean resetProfileBeforeVisiting) {

        URL appiumServer = null;
        try {
            appiumServer = new URL(appiumURL);
        } catch (MalformedURLException e) {
            logger.fatal(deviceName + ": MobileBrowser Error: Unable to turn String appiumURL into URL");
            System.exit(-1);
        }

        DesiredCapabilities caps = buildAppiumGeneralCaps();
        caps.setCapability("deviceName", deviceName);
        caps.setCapability("udid", deviceId);

        setBrowserCaps(browser, caps);
        this.appName = getAppNameFromCapability(caps);

        if (profilePath == null)
            profilePath = "";
        if (profilePath.isEmpty() && resetProfileBeforeVisiting == true) {
            throw new IllegalArgumentException("You cannot set profile directory path empty or null and set" +
                " reset profile = true at the same time. If you want to start a stateless crawl, consider specifying " +
                "a empty directory");
        }
        if (profilePath.isEmpty()) {
            logger.warn("The crawl will use current profile in the device.");
        } else {
            logger.warn("Upload the profile to the device");
            setBrowserProfile(appName, profilePath);
        }


        AppiumDriver<MobileElement> driver = new AppiumDriver<>(appiumServer, caps);
        logger.info(deviceName + ": " + browser + " Driver created successfully");
        // Because Appium webdriver will automatically launch the application,
        // invoking driver.get(URL) will lead to timeout
        // Thus we first close this app as a workaround.
        driver.closeApp();
        return driver;
    }

    private void setBrowserProfile(String appPackage, String profilePath) {
        logger.info("copy " + profilePath + " to mobile phone......");
        try {
            // The script will force-stop the application in case of race condition
            String[] cmd = new String[]{
                "bash", this.saveBackToProfileDirectory ? "scripts/upload.sh": "scripts/upload-cached.sh", this.deviceId, appPackage, profilePath
            };
            String logLine = "Executing ";
            for (String s: cmd) {
                logLine += s + " ";
            }
            logger.info(logLine);

            Process proc1 = Runtime.getRuntime().exec(cmd);
            BufferedReader stdInput1 = new BufferedReader(new 
                InputStreamReader(proc1.getInputStream()));
            BufferedReader stdError1 = new BufferedReader(new 
                InputStreamReader(proc1.getErrorStream()));
            // Read the output from the command
            String s1 = null;
            while ((s1 = stdInput1.readLine()) != null) {
                // logger.info(s1);
            }
            // Read any errors from the attempted command
            while ((s1 = stdError1.readLine()) != null) {
                logger.error(s1);
            }
            // if (0 != Runtime.getRuntime().exec(cmd).waitFor())
            //     throw new RuntimeException("upload.sh return non-zero value");
            if (appPackage == "org.torproject.torbrowser") {
                String[] cmd2 = new String[]{
                    "python3", "scripts/init-mobile-phone.py", "-s", this.deviceId, "--set-firefox-prefs", "tor"
                };
                String logLine2 = "Executing ";
                for (String s: cmd2) {
                    logLine2 += s + " ";
                }
                logger.info(logLine2);
                Process proc = Runtime.getRuntime().exec(cmd2);
                BufferedReader stdInput = new BufferedReader(new 
                    InputStreamReader(proc.getInputStream()));
                BufferedReader stdError = new BufferedReader(new 
                    InputStreamReader(proc.getErrorStream()));
                // Read the output from the command
                String s = null;
                while ((s = stdInput.readLine()) != null) {
                    logger.info(s);
                }
                // Read any errors from the attempted command
                while ((s = stdError.readLine()) != null) {
                    logger.error(s);
                }
            }
        } catch (IOException | RuntimeException e) {
            logger.error("Fail to upload " + profilePath + " to mobile phone");
            System.exit(-1);
        }
        logger.info("done");
    }

    private DesiredCapabilities setBrowserCaps(String browser, DesiredCapabilities caps) {
        switch(browser.toLowerCase()) {
            case "opera":
                return setOperaCaps(caps);
            case "chrome":
            case "chrome-scroll":
                return setGoogleChromeCaps(caps);
            case "firefox":
                return setFirefoxCaps(caps);
            case "tor":
                return setTorCaps(caps);
            case "ucbrowser":
                return setUCBrowserCaps(caps);
            case "ghostery":
                return setGhosteryCaps(caps);
            case "firefoxfocus":
                return setFirefoxFocusCaps(caps);
            case "brave":
                return setBraveCaps(caps);
            case "duckduckgo":
                return setDuckDuckGoCaps(caps);
            default:
                throw new NotFoundException("Browser " + browser + " not found!");
        }
    }

    private String getAppNameFromCapability(Capabilities caps) {
        String appPackage = (String)caps.getCapability("appPackage");
        if (appPackage == null) {
            throw new RuntimeException("The appPackage of the capability is not set!");
        }
        return appPackage;
    }

    /*Desired Capabilities for Android--------------------------------------------------------------------------------*/

    /**
     * These are general, standard DesiredCapabilities that are required to run an Appium session. It uses global
     * variables that can be manually configured depending on the phone/android version. This method is used by
     * other android___Caps methods to set up the foundation of capabilities.
     * @return              General Appium desired capabilities
     */
    public DesiredCapabilities buildAppiumGeneralCaps() {
        DesiredCapabilities caps = new DesiredCapabilities();
        // Actually platform version and name are optional
        caps.setCapability("platformVersion", platformVersion);
        caps.setCapability("platformName", platformName);

        caps.setCapability("newCommandTimeout", newCommandTimeout);
        caps.setCapability("noReset", noReset);
        return caps;
    }

    /**
     * Builds desired capabilities for an Appium session with Opera Mobile
     * Must include : appPackage, appActivity
     * @return              Capabilities for Appium Opera driver instantiation
     */
    private DesiredCapabilities setOperaCaps(DesiredCapabilities caps) {
        caps.setCapability("appPackage", "com.opera.browser");
        caps.setCapability("appActivity", "com.opera.android.BrowserActivity");
        return caps;
    }

    /**
     * Builds desired capabilities for an Appium session with UC Browser Mobile
     * Must include : appPackage, appActivity
     * @return              Capabilities for Appium UC Browser driver instantiation
     */
    private DesiredCapabilities setUCBrowserCaps(DesiredCapabilities caps) {
        caps.setCapability("appPackage", "com.UCMobile.intl");
        caps.setCapability("appActivity", "com.UCMobile.main.UCMobile");
        return caps;
    }

    /**
     * Builds desired capabilities for an Appium session with Firefox Mobile
     * Must include : appPackage, appActivity
     * @return              Capabilities for Appium Firefox driver instantiation
     */
    private DesiredCapabilities setFirefoxCaps(DesiredCapabilities caps) {
        caps.setCapability("appPackage", "org.mozilla.firefox");
        caps.setCapability("appActivity", "org.mozilla.firefox.App");
        return caps;
    }

    private DesiredCapabilities setTorCaps(DesiredCapabilities caps) {
        caps.setCapability("appPackage", "org.torproject.torbrowser");
        caps.setCapability("appActivity", "org.torproject.torbrowser.App");
        return caps;
    }

    /**
     * Builds desired capabilities for an Appium session with Chrome Mobile
     * Must include : appPackage, appActivity
     * @return              Capabilities for Appium Chrome driver instantiation
     */
    private DesiredCapabilities setGoogleChromeCaps(DesiredCapabilities caps) {
        caps.setCapability("appPackage", "com.android.chrome");
        caps.setCapability("appActivity", "com.google.android.apps.chrome.Main");
        return caps;
    }

    /**
     * Builds desired capabilities for an Appium session with Firefox Focus Mobile
     * Must include : appPackage, appActivity
     * @return              Capabilities for Appium Firefox Focus driver instantiation
     */
    private DesiredCapabilities setFirefoxFocusCaps(DesiredCapabilities caps) {
        caps.setCapability("appPackage", "org.mozilla.focus");
        caps.setCapability("appActivity", "org.mozilla.focus.activity.MainActivity");
        return caps;
    }

    /**
     * Builds desired capabilities for an Appium session with Ghostery Mobile
     * Must include : appPackage, appActivity
     * @return              Capabilities for Appium Ghostery driver instantiation
     */
    private DesiredCapabilities setGhosteryCaps(DesiredCapabilities caps) {
        caps.setCapability("appPackage", "com.ghostery.android.ghostery");
        caps.setCapability("appActivity", "org.mozilla.fenix.App");
        return caps;
    }

    /**
     * Builds desired capabilities for an Appium session with DuckDuckGo Mobile
     * Must include : appPackage, appActivity
     * @return              Capabilities for Appium DuckDuckGo driver instantiation
     */
    private DesiredCapabilities setDuckDuckGoCaps(DesiredCapabilities caps) {
        caps.setCapability("appPackage", "com.duckduckgo.mobile.android");
        caps.setCapability("appActivity", "com.duckduckgo.app.browser.BrowserActivity");
        return caps;
    }

    /**
     * Builds desired capabilities for an Appium session
     * Must include : appPackage, appActivity
     * @return              Capabilities for Appium Brave Browser driver instantiation
     */
    private DesiredCapabilities setBraveCaps(DesiredCapabilities caps) {
        caps.setCapability("appPackage", "com.brave.browser");
        caps.setCapability("appActivity", "com.google.android.apps.chrome.Main");
        return caps;
    }

}
