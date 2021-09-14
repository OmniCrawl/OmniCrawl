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

public class RemoteDesktopWorker extends AbstractWorker {
    public RemoteDesktopWorker(List<RemoteDesktopBrowser> browsers, List<String> urls, CyclicBarrier barrier,
                               int numOfSyncUrl,
                         int hardTimeoutSec) {
        super(new ArrayList<AbstractBrowser>(browsers), urls, barrier, numOfSyncUrl, hardTimeoutSec);
        this.logger = LogManager.getLogger("RemoteDesktopWorker");
    }
    public RemoteDesktopWorker(RemoteDesktopBrowser browser, List<String> urls, CyclicBarrier barrier, int numOfSyncUrl,
                         int hardTimeoutSec) {
        this(Arrays.asList(browser), urls, barrier, numOfSyncUrl, hardTimeoutSec);
    }

    @Override
    protected void browse(AbstractBrowser abstractBrowser, String url) throws IOException {
        RemoteDesktopBrowser browser = (RemoteDesktopBrowser)abstractBrowser;
        Monitor.start(browser.browserNickname);
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
            "browser=remote-desktop-" + browser.browserName + "-" + browser.browserNickname,
            "scroll=" + (browser.browserName.toLowerCase().contains("scroll") ? "true" : "false"),
        });
        logger.info("Getting url " + redir_url);

        try {
            // This should never timeout!
            browser.browse(redir_url);
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

