import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

class MobileWorker extends AbstractWorker {
    public MobileWorker(List<MobileBrowser> browsers, List<String> urls, CyclicBarrier barrier, int numOfSyncUrl,
                        int hardTimeoutSec) {
        super(new ArrayList<AbstractBrowser>(browsers), urls, barrier, numOfSyncUrl, hardTimeoutSec);
        this.logger = LogManager.getLogger("MobileWorker");
    }

    public MobileWorker(MobileBrowser browser, List<String> urls, CyclicBarrier barrier, int numOfSyncUrl,
                        int hardTimeoutSec) {
        this(Arrays.asList(browser), urls, barrier, numOfSyncUrl, hardTimeoutSec);
    }
    @Override
    protected void browse(AbstractBrowser abstractBrowser, String url) throws IOException{
        MobileBrowser browser = (MobileBrowser)abstractBrowser;
        Monitor.start(browser.deviceName);
        browser.restart();
        ServerSocket listener = new ServerSocket(0, 50, InetAddress.getByName(Config.MAIN_CRAWLER_LISTEN_HOST));
        logger.info("listening... on port " + listener.getLocalPort());
        listener.setSoTimeout(hardTimeoutSec * 1000);
        logger.info("set socket timeout to " + String.valueOf(hardTimeoutSec * 1000) + " msec");

        /*
         * A workaround to bypass the annoying tor button.
         * When we browse the link example.com, tor will load it in the non-private window,
         * and thus the request it's blocked (Tor only allow browsing from a private window).
         * Once we clicked the tor "connect" button, tor will load example.com in the private window again.
         *
         * What is interesting is that we can just visit another link cnn.com after clicking example.com.
         * Tor will somehow load cnn.com in the private window, as if it's "overflowed".
         * This cnn.com is not blocked because we use our own proxy.
         *
         * The annoying button seems to be just floating on the window, so cnn.com will load without any problem.
         */
        // if (browser.browserName.equals("tor")) {
        //     logger.info("Getting url about:blank....");
        //     browser.get("about:blank");
        //     logger.info("Getting url about:blank done");
        // }
        // For mobile web drivers, this function is asynchronous
        // Thus we need await mitmproxy's SYN to know that it's done.
        logger.info("Crawling " + url);
        String redir_url = "http://240.240.240.240/start?" + String.join("&", new String[] {
            "sync=true",
            "sync_port=" + listener.getLocalPort(),
            "sync_host=" + Config.BROWSER_SYNC_HOST,
            "url=" + url,
            "browser=mobile-" + browser.browserName + "-" + browser.deviceId + "-" + browser.deviceName,
            "scroll=" + (browser.browserName.toLowerCase().contains("scroll") ? "true" : "false"),
        });
        logger.info("Getting url " + redir_url);
        if (browser.browserName.equals("tor")) {
	    try {
	        browser.get(redir_url);
	    } catch(org.openqa.selenium.WebDriverException excep) {
	        // ASUS Zenfone: this command will timeout when using tor browser
	        // should be safe to ignore
	        logger.warn("Ignore Tor ASUS Zenfone timeout");
	    }
        } else {
            browser.get(redir_url);
        }

        try {
            logger.info("awaiting mtim......");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logger.info("wait start TS = " + timestamp.getTime());
            awaitMitmproxy(listener);
            Monitor.reportStatus(browser.deviceName, "success");
        }  catch (SocketTimeoutException e) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logger.error(browser.browserName + " " + browser.deviceName + " timeout " + url + " ... mitm does not send SYN, TS = " + timestamp.getTime());
            Monitor.reportStatus(browser.deviceName, "timeout");
            String path =
                Config.MOBILE_TIMEOUT_SCREENCAP_DIR + "/" +
                String.join("-",
                    browser.deviceId,
                    browser.deviceName,
                    url.replace("http://", "").replace("https://","")
                ) + ".png";
            try {
                String[] cmd = new String[]{
                    "bash", "scripts/screencap.sh", browser.deviceId, path
                };
                Runtime.getRuntime().exec(cmd).waitFor();
                logger.fatal("Running command bash scripts/screencap.sh " + browser.deviceId + " " + path);
            } catch (IOException | InterruptedException exp) {
                exp.printStackTrace();
                logger.fatal("Fail to download to " + path + " from mobile phone");
                System.exit(-1);
            }

        }
        listener.close();
        browser.saveBackToProfile();
        Monitor.end(browser.deviceName);
    }
}

