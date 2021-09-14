import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper Class for creating Browser instances and capabilities
 */
public class OpenWPMMobile extends AbstractBrowser {
    String browserName;
    String browserNickname;
    int hardTimeoutSec;
    String proxyAddr;
    String profileDirectoryPath;
    Logger logger;
    WebDriver driver;
    boolean resetProfileBeforeVisiting;
    String tmpProfilePath;
    List<String> commandLineArguments = new ArrayList<>();
    Process process = null;

    // Moto G5 Plus Firefox Screen Size
    public static int windowWidth = 360;
    public static int windowHeight = 592;

    public static final String tmpProfilePrefix = "crawler-tmp-";

    public OpenWPMMobile(String browserName, String browserNickname, int hardTimeoutSec, String proxyAddr,
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
        create(browserName, hardTimeoutSec, proxyAddr, profileDirectoryPath, resetProfileBeforeVisiting);
    }

    public void get(String url) {
        try {
            List<String> cmds = new ArrayList<>(commandLineArguments);
            cmds.add(url);
            System.err.println("");
            for (String cmd: cmds)
                System.err.print(cmd + " ");
            System.err.println("");
            ProcessBuilder pb = new ProcessBuilder(cmds);
            process = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Firefox45 fails to visit " + url);
        }
    }

    public void quit() {
        if (!resetProfileBeforeVisiting) {
            String srcPath = tmpProfilePath;
            if (browserName.equals("firefox45")) {
                // firefox45 will create a tmp directory under the directory
				File[] files_arr = new File(tmpProfilePath).listFiles();
				Arrays.sort(files_arr, Comparator.comparingLong(File::lastModified).reversed());
				List<File> files = Arrays.asList(files_arr);
                if (files.size() == 0) {
                    Resource.perror("firefox45 cannot find profile directory in " + tmpProfilePath);
                } else if (files.size() > 1) {
                    logger.error("Firefox45 tmp profile directory contains more than one directory. Pick the one with latest update time.");
                }
				srcPath = tmpProfilePath + "/" + files.get(0).getName() + "/webdriver-py-profilecopy";
            }

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
                logger.info("save profile back: from " + srcPath + " to " + profileDirectoryPath);
                for (String storage_file : storage_files) {
                    String src = srcPath + "/" + storage_file;
                    String dst = profileDirectoryPath;
                    logger.info("save profile back: cp -r " + src + " to " + dst);
                    Runtime.getRuntime().exec(new String[]{"cp", "-r", src, dst}).waitFor();
                }

                logger.info("profile saving done");
            } catch (IOException | InterruptedException e) {
                logger.error("Fail to move the temp profile to " + profileDirectoryPath);
            }
        }

        // SIGTERM
        process.destroy();

        // Ensure that the launch script is killed
        String killPath = null;
        switch (browserName) {
            case "firefox45":
                killPath = "[p]ython2 browsers/launch_selenium_firefox45_python2.py";
                break;
            case "firefox86":
                killPath = "[p]ython3 browsers/launch_selenium_firefox86.py";
                break;
            default:
                throw new UnsupportedOperationException("Desktop browser " + browserName + " not found!");
        }
        String[] cmd = new String[]{"bash", "kill", "$(ps aux | grep '" + killPath + "' | awk '{print $2}')"};
        try {
            logger.info("Executing command: " + Arrays.toString(cmd));
            Runtime.getRuntime().exec(cmd).waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to kill " + browserNickname + " | Error: " + e.toString());
        }

        try {
            logger.info("Executing command: rm -r " + tmpProfilePath);
            Runtime.getRuntime().exec(new String[]{"rm", "-r", tmpProfilePath}).waitFor();
            logger.info("Executing command: mkdir " + tmpProfilePath);
            Runtime.getRuntime().exec(new String[]{"mkdir", tmpProfilePath}).waitFor();
        } catch (IOException | InterruptedException e) {
            logger.error("Fail to remove the temp profile in " + tmpProfilePath);
        }
    }

    private void create(String browser, int timeoutSec, String proxyAddr, String profileDirectoryPath,
                             boolean resetProfileBeforeVisiting) {
        if (proxyAddr != null && proxyAddr.isEmpty()) {
            throw new IllegalArgumentException("You cannot pass an empty proxy address. Use null instead.");
        }
        if (profileDirectoryPath == null || profileDirectoryPath.isEmpty()) {
            throw new IllegalArgumentException("Profile directory path cannot be empty or null. If you want to start " +
                "from a stateless crawl, consider specifying a empty directory.");
        }

        switch (browser) {
            case "firefox45":
                logger.info("OpenWPM-Mobile Firefox 45 is creating...");
                setCommandLineArguments(proxyAddr, profileDirectoryPath, this.tmpProfilePath, Config.PATH_TO_OPENWPM_MOBILE_FIREFOX45);
                logger.info("OpenWPM-Mobile Firefox 45 driver created!");
                break;
            case "firefox65":
                logger.info("OpenWPM-Mobile Firefox 65 is creating...");
                setCommandLineArguments(proxyAddr, profileDirectoryPath, this.tmpProfilePath, Config.PATH_TO_OPENWPM_MOBILE_FIREFOX65);
                logger.info("OpenWPM-Mobile Firefox 65 driver created!");
                break;
            case "firefox86":
                logger.info("OpenWPM-Mobile Firefox 86 is creating...");
                setCommandLineArguments(proxyAddr, profileDirectoryPath, this.tmpProfilePath, Config.PATH_TO_OPENWPM_MOBILE_FIREFOX86);
                logger.info("OpenWPM-Mobile Firefox 86 driver created!");
                break;
            default:
                throw new UnsupportedOperationException("Desktop browser " + browser + " not found!");
        }

    }

    private void setCommandLineArguments(String proxy, String profilePath, String tmpProfilePath,
                                                  String firefoxBinaryPath){
        if (profilePath == null) {
            throw new UnsupportedOperationException("Firefox does not support stateless crawling. You must specify a " +
                "profile directory");
        } else {
            logger.warn("Note: In Firefox, specifying proxy address has no effect when using a profile");
            logger.warn("Firefox will load proxy configuration from prefs.js in the profile directory");
        }
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

        commandLineArguments.add(firefoxBinaryPath);

        commandLineArguments.add("-width");
        commandLineArguments.add(String.valueOf(windowWidth));
        commandLineArguments.add("-height");
        commandLineArguments.add(String.valueOf(windowHeight));
        commandLineArguments.add("--no-remote");
        commandLineArguments.add("-profile");
        commandLineArguments.add(profilePath);
        // This command line option does NOT present in official Firefox binary.
        // We will use a script as a wrapper and parse this option.
        // The script will use the specified -tmpprofile as the temporary profile path.
        commandLineArguments.add("-tmpprofile");
        commandLineArguments.add(tmpProfilePath);
    }

}
