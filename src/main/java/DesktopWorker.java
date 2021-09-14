import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.UnhandledAlertException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class DesktopWorker extends AbstractWorker {
    public DesktopWorker(List<DesktopBrowser> browsers, List<String> urls, CyclicBarrier barrier, int numOfSyncUrl,
                         int hardTimeoutSec) {
        super(new ArrayList<AbstractBrowser>(browsers), urls, barrier, numOfSyncUrl, hardTimeoutSec);
        this.logger = LogManager.getLogger("DesktopWorker");
    }
    public DesktopWorker(DesktopBrowser browser, List<String> urls, CyclicBarrier barrier, int numOfSyncUrl,
                         int hardTimeoutSec) {
        this(Arrays.asList(browser), urls, barrier, numOfSyncUrl, hardTimeoutSec);
    }

    @Override
    protected void browse(AbstractBrowser abstractBrowser, String url) throws IOException{
        DesktopBrowser browser = (DesktopBrowser)abstractBrowser;
        Monitor.start(browser.browserNickname);
        // If the browser is not ready yet
        if (!browser.isReady())
            browser.restart();
        ServerSocket listener = new ServerSocket(0, 50, InetAddress.getByName(Config.MAIN_CRAWLER_LISTEN_HOST));
        logger.info("listening... on port " + listener.getLocalPort());
        listener.setSoTimeout(hardTimeoutSec * 1000);
        logger.info("set socket timeout to " + String.valueOf(hardTimeoutSec * 1000) + " msec");

        logger.info("Crawling " + url);
        String redir_url = "http://240.240.240.240/start?" + String.join("&", new String[] {
            "sync=true",
            "sync_port=" + listener.getLocalPort(),
            "sync_host=" + Config.BROWSER_SYNC_HOST,
            "url=" + url,
            "browser=desktop-" + browser.browserName + "-" + browser.browserNickname,
            "scroll=" + (browser.browserName.toLowerCase().contains("scroll") ? "true" : "false"),
        });
        logger.info("Getting url " + redir_url);

        try {
            // This should never timeout!
            browser.driver.get(redir_url);
            logger.info("Redirection done. Start crawling " + url);
            logger.info("awaiting mtim......");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logger.info("wait start TS = " + timestamp.getTime());
            awaitMitmproxy(listener);
            Monitor.reportStatus(browser.browserNickname, "success");
        }  catch (SocketTimeoutException | UnhandledAlertException e) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logger.error(browser.browserNickname + " timeout " + url + " ... mitm does not send SYN, TS = " + timestamp.getTime());
            Monitor.reportStatus(browser.browserNickname, "timeout");
        } catch (Exception unknownException) {
            // firefox will throw org.openqa.selenium.WebDriverException if reaching error page
            //
            // 16:11:47.055 [Thread-2] ERROR Worker - Unknown exception:
            // org.openqa.selenium.WebDriverException: Reached error page: about:neterror?e=netTimeout&u=http%3A//240.240.240.240/start%3Fsync%3Dfalse%26url%3Dhttps%3A//example.com%26browser%3Dfirefox&c=UTF-8&f=regular&d=The%20server%20at%20240.240.240.240%20is%20taking%20too%20long%20to%20respond.
            // Build info: version: 'unknown', revision: 'unknown', time: 'unknown'
            // System info: host: 'will-OptiPlex-990', ip: '127.0.1.1', os.name: 'Linux', os.arch: 'amd64', os.version: '4.15.0-39-generic', java.version: '10.0.2'
            // Driver info: DesktopBrowser$1
            // Capabilities {acceptInsecureCerts: true, browserName: firefox, browserVersion: 63.0.3, javascriptEnabled: true, moz:accessibilityChecks: false, moz:geckodriverVersion: 0.23.0, moz:headless: false, moz:processID: 20439, moz:profile: /tmp/rust_mozprofile.E1M6p9..., moz:useNonSpecCompliantPointerOrigin: false, moz:webdriverClick: true, pageLoadStrategy: normal, platform: LINUX, platformName: LINUX, platformVersion: 4.15.0-39-generic, rotatable: false, setWindowRect: true, timeouts: {implicit: 0, pageLoad: 300000, script: 30000}, unhandledPromptBehavior: dismiss and notify}
            // Session ID: 5d929c50-a7dd-4451-9abb-f3c53ff56055
            // at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
            // at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
            // at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
            // at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:488)
            // at org.openqa.selenium.remote.http.W3CHttpResponseCodec.createException(W3CHttpResponseCodec.java:187)
            // at org.openqa.selenium.remote.http.W3CHttpResponseCodec.decode(W3CHttpResponseCodec.java:122)
            // at org.openqa.selenium.remote.http.W3CHttpResponseCodec.decode(W3CHttpResponseCodec.java:49)
            // at org.openqa.selenium.remote.HttpCommandExecutor.execute(HttpCommandExecutor.java:158)
            // at org.openqa.selenium.remote.service.DriverCommandExecutor.execute(DriverCommandExecutor.java:83)
            // at org.openqa.selenium.remote.RemoteWebDriver.execute(RemoteWebDriver.java:543)
            // at org.openqa.selenium.remote.RemoteWebDriver.get(RemoteWebDriver.java:271)
            // at DesktopWorker.browse(DesktopWorker.java:65)
            // at DesktopWorker.run(DesktopWorker.java:40)
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logger.error("timeout " + url + "... the browser throws an unknown exception " + unknownException + ", TS" +
                " = " + timestamp.getTime());
            unknownException.printStackTrace();
            Monitor.reportStatus(browser.browserNickname, "timeout");
        }
        listener.close();
        browser.quit();
        Monitor.end(browser.browserNickname);
    }
}
