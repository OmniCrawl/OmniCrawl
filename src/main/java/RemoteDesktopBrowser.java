import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.taskdefs.condition.Http;
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

import java.util.Random;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RemoteDesktopBrowser extends AbstractBrowser {
    final int windowWidth = Config.DESKTOP_WINDOW_WIDTH;
    final int windowHeight = Config.DESKTOP_WINDOW_HEIGHT;
    final int httpConnectionTimeoutMsec = 600 * 1000;
    final int httpReadTimeoutMsec = 600 * 1000;
    final int deferDeleteTmpProfileMsec = 120 * 1000;

    // fields
    String browserName;
    String browserNickname;
    String proxyAddr;
    String profileDirectoryRemotePath;
    boolean resetProfileBeforeVisiting;

    String tmpProfileDirectoryRemotePath = null;
    String nextTmpProfileDirectoryRemotePath = null;
    Thread nextTmpProfileThread = null;
    Logger logger;

    public RemoteDesktopBrowser(String browserName, String browserNickname, String proxyAddr,
                                String profileDirectoryRemotePath,
                                boolean resetProfileBeforeVisiting) {
        this.browserName = browserName;
        this.browserNickname = browserNickname;
        this.proxyAddr = proxyAddr;
        this.profileDirectoryRemotePath = profileDirectoryRemotePath;
        this.resetProfileBeforeVisiting = resetProfileBeforeVisiting;

        this.logger = LogManager.getLogger(browserName);
        // kill any old session
        kill();
    }

    private String buildTempProfileDirectory() {
        if (this.profileDirectoryRemotePath == null || this.profileDirectoryRemotePath.isEmpty()) {
            throw new RuntimeException("Invalid config: Remote Desktop Browser does not support empty profile.");
        }

        if (this.resetProfileBeforeVisiting) {
            if (this.nextTmpProfileThread == null) {
                this.nextTmpProfileDirectoryRemotePath =
                    Config.REMOTE_TMP_DIR + Config.REMOTE_PATH_SEPERATOR + UUID.randomUUID().toString();
                this.nextTmpProfileThread = new Thread() {
                    public void run() {
                        System.err.println("pro " + profileDirectoryRemotePath);
                        System.err.println("copy to " + nextTmpProfileDirectoryRemotePath);
                        sendSyncCommand("xcopy", String.join(" ", new String[] {
                            profileDirectoryRemotePath,
                            nextTmpProfileDirectoryRemotePath,
                            "/E", // include empty files
                            "/H", // include hidden files
                            "/I", // assume destination is a directory
                            "/Y", // suppress prompting
                            ">NUL" // suppress output to speed up
                        }), true, false);
                    }
                };
                this.nextTmpProfileThread.start();
                logger.info("first profile copy...");
            }
            try {
                logger.info("await profile copy thread...");
                this.nextTmpProfileThread.join();
                logger.info("thread join!");
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.tmpProfileDirectoryRemotePath = this.nextTmpProfileDirectoryRemotePath;
            this.nextTmpProfileDirectoryRemotePath =
                Config.REMOTE_TMP_DIR + Config.REMOTE_PATH_SEPERATOR + UUID.randomUUID().toString();
            this.nextTmpProfileThread = new Thread() {
                public void run() {
                    System.err.println("pro " + profileDirectoryRemotePath);
                    System.err.println("copy to " + nextTmpProfileDirectoryRemotePath);
                    sendSyncCommand("xcopy", String.join(" ", new String[] {
                        profileDirectoryRemotePath,
                        nextTmpProfileDirectoryRemotePath,
                        "/E", // include empty files
                        "/H", // include hidden files
                        "/I", // assume destination is a directory
                        "/Y", // suppress prompting
                        ">NUL" // suppress output to speed up
                    }), true, false);
                }
            };
            this.nextTmpProfileThread.start();
            logger.info("scheduled next profile copy thread!");
            logger.info("current " + this.tmpProfileDirectoryRemotePath);
            return this.tmpProfileDirectoryRemotePath;
        }
        return this.profileDirectoryRemotePath;
    }

    public void browse(String url) {
        // First, set up profile directory
        String profileDir = buildTempProfileDirectory();

        // For firefox-based browers (firefox, tor), delete the parent.lock file
        sendSyncCommand("del", String.join(" ", new String[] {
            "/F", // delete read-only file
            "/Q", // don't ask
            profileDir + Config.REMOTE_PATH_SEPERATOR + "parent.lock"
        }), true, true);
        

        // windows delete will not delete the file immediately
        // current workaround is just await 3 seconds
        //TODO: maybe try sync64.exe?
        //logger.info("del await random 0-30 more seconds...");
        logger.info("del await 3 more seconds...");
        try {
          //Thread.sleep(new Random().nextInt(30 * 1000));
          Thread.sleep(3 * 1000);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }

        // Ask the client to crawl the URL (async)
        sendAsyncCommand(getBrowserBinaryRemotePath(), buildArgs(browserName, profileDir, url), false, false);
    }

    public void kill() {
        // kill the browser process

        // Don't use Paths.get(...).getFileName() because it's platform-dependent.
        // https://stackoverflow.com/a/14526362
        String binaryPath = getBrowserBinaryRemotePath();
        String processName = binaryPath.substring(binaryPath.lastIndexOf("\\") + 1);
        // Some child processes will be killed by its parents
        // this command might return non-zero value. We just ignore it.
        sendSyncCommand("taskkill", String.join(" ", new String[] {
          "/F", // forcefully kill
            "/T", // kill its child process
            "/IM", // specify the image name
            processName
        }), false, true);

        if (browserName.equals("chrome72selenium") || browserName.equals("chrome88selenium")) {
          sendSyncCommand("taskkill", String.join(" ", new String[] {
            "/F", // forcefully kill
              "/T", // kill its child process
              "/IM", // specify the image name
              "chromedriver.exe"
          }), false, true);
        } else if (browserName.equals("firefox65selenium") || browserName.equals("firefox86selenium")) {
          sendSyncCommand("taskkill", String.join(" ", new String[] {
            "/F", // forcefully kill
              "/T", // kill its child process
              "/IM", // specify the image name
              "geckodriver.exe"
          }), false, true);
        }

        // taskkill will not kill the process immediately even if the command returns
        // it will accidentally kill our next crawling browser
        // current workaround is just await 3 seconds
        logger.info("taskkill " + processName + " await 3 more seconds...");
        try {
          Thread.sleep(3 * 1000);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }
    }

    public void quit() {
        try {
            kill();

            // clean up the tmp profile directory
            if (this.tmpProfileDirectoryRemotePath != null) {
                // Due to the race condition of process killing / file lock in Windows,
                // it will fail to remove the files because they are still in use.
                // We defer the removal with a new thread.
                String dir = this.tmpProfileDirectoryRemotePath;
                new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(deferDeleteTmpProfileMsec);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        sendSyncCommand("rmdir", String.join(" ", new String[] {
                            "/S", // recursively
                            "/Q", // don't ask
                            dir
                        }), true, false);
                    }
                }.start();
            }
        } catch(Exception e) {
            e.printStackTrace();
            Resource.perror("Fail to execute browser.quit()", e);
        }
    }

    private void sendSyncCommand(String cmd, String args, boolean isShell, boolean ignoreError) {
        sendCommand(cmd, args, false, isShell, ignoreError);
    }
    private void sendAsyncCommand(String cmd, String args, boolean isShell, boolean ignoreError) {
        sendCommand(cmd, args, true, isShell, ignoreError);
    }

    private void sendCommand(String cmd, String args, boolean isAsync, boolean isShell, boolean ignoreError) {
        // https://stackoverflow.com/a/1359700
        HttpURLConnection connection = null;
        try {
            connection = buildConnection(cmd, args, isAsync, isShell);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && ! ignoreError) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                throw new RuntimeException("Response is not HTTP 200: " + br.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Resource.perror("Failed to send command " + cmd + " " + args, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    private HttpURLConnection buildConnection(String cmd, String args, boolean isAsync, boolean isShell) throws IOException,
        UnsupportedEncodingException,
        MalformedURLException {
        String charset = "UTF-8";
        String query = String.format(String.join("&", new String[] {
                "key" + "=%s",
                "cmd" + "=%s",
                "args" + "=%s",
                "async" + "=%s",
                "shell" + "=%s"
            }),
            URLEncoder.encode(Config.REMOTE_WEBDRIVER_KEY, charset),
            URLEncoder.encode(cmd, charset),
            URLEncoder.encode(args, charset),
            URLEncoder.encode(String.valueOf(isAsync), charset),
            URLEncoder.encode(String.valueOf(isShell), charset)
        );
        URL url = new URL(Config.REMOTE_WEBDRIVER_HTTP_ENDPOINT + "?" + query);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(httpConnectionTimeoutMsec);
        connection.setReadTimeout(httpReadTimeoutMsec);
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        return connection;
    }

    private String getBrowserBinaryRemotePath() {
        switch (browserName.toLowerCase()) {
            case "chrome72":
                return Config.REMOTE_CHROME72_PATH;
            case "chrome72b":
                return Config.REMOTE_CHROME72B_PATH;
            case "chrome72scroll":
                return Config.REMOTE_CHROME72SCROLL_PATH;
            case "chrome88":
                return Config.REMOTE_CHROME88_PATH;
            case "chrome88b":
                return Config.REMOTE_CHROME88B_PATH;
            case "chrome88scroll":
                return Config.REMOTE_CHROME88SCROLL_PATH;
            case "firefox65":
                return Config.REMOTE_FIREFOX65_PATH;
            case "firefox65ghostery":
                return Config.REMOTE_FIREFOX65GHOSTERY_PATH;
            case "firefox86":
                return Config.REMOTE_FIREFOX86_PATH;
            case "firefox86ghostery":
                return Config.REMOTE_FIREFOX86GHOSTERY_PATH;
            case "firefox45":
                return Config.REMOTE_FIREFOX45_PATH;
            case "firefox65selenium":
                return Config.REMOTE_SELENIUM_FIREFOX65_PATH;
            case "chrome72selenium":
                return Config.REMOTE_SELENIUM_CHROME72_PATH;
            case "firefox86selenium":
                return Config.REMOTE_SELENIUM_FIREFOX86_PATH;
            case "chrome88selenium":
                return Config.REMOTE_SELENIUM_CHROME88_PATH;
            case "brave":
                return Config.REMOTE_BRAVE_PATH;
            case "tor":
                return Config.REMOTE_TOR_PATH;
            default:
                Resource.perror("Invalid browser name: " + browserName);
                return "";
        }
    }

    private String buildArgs(String browserName, String profileDir, String url) {
        switch (browserName.toLowerCase()) {
            case "chrome72":
            case "chrome72b":
            case "chrome72scroll":
            case "chrome72selenium":
            case "chrome88":
            case "chrome88b":
            case "chrome88scroll":
            case "chrome88selenium":
            case "brave":
                return buildChromeArgs(profileDir, url);

            case "firefox65selenium":
            case "firefox65":
            case "firefox65ghostery":
            case "firefox86":
            case "firefox86ghostery":
            case "firefox86selenium":
            case "firefox45":
            case "tor":
                return buildFirefoxArgs(profileDir, url);

            default:
                Resource.perror("Invalid browser name: " + browserName);
                return "";
        }
    } 

    private String buildChromeArgs(String profileDir, String browseUrl) {
        List<String> args = new ArrayList<>();
        args.add("--window-size=" + String.valueOf(windowWidth) + "," + String.valueOf(windowHeight));
        args.add("--media-cache-size=1");
        args.add("--disk-cache-size=1");
        args.add("--disk-cache-dir=nul");
        args.add("--disable-application-cache");
        args.add("--user-data-dir=" + profileDir);
        if (this.proxyAddr != null && this.proxyAddr != "") {
            args.add("--proxy-server=" + this.proxyAddr);
        }
        // Chrome will crash on some OS if using sandbox
        // args.add("--no-sandbox");
        args.add(browseUrl);
        return String.join(" ", args);
    }

    private String buildFirefoxArgs(String profileDir, String browseUrl) {
        List<String> args = new ArrayList<>();
        this.logger.warn("Specifying proxyAddr in Firefox has no effect."
            + " Firefox loads proxy address from the profile.");
        args.add("--no-remote");
        args.add("-profile");
        args.add(profileDir);
        // note in the newer firefox the window size is overwritten by `xulstore.json`
        args.add("-width");
        args.add(String.valueOf(windowWidth));
        args.add("-height");
        args.add(String.valueOf(windowHeight));
        args.add(browseUrl);
        return String.join(" ", args);
    }
}
