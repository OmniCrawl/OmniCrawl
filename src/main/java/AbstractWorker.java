import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public abstract class AbstractWorker implements Runnable {
    public List<AbstractBrowser> browsers;
    public List<String> urls;
    public CyclicBarrier barrier;
    public int numOfSyncUrl;
    public Logger logger;
    public int hardTimeoutSec;

    public AbstractWorker(List<AbstractBrowser> browsers, List<String> urls, CyclicBarrier barrier, int numOfSyncUrl,
                          int hardTimeoutSec) {
        this.browsers = browsers;
        this.urls = urls;
        this.barrier = barrier;
        this.numOfSyncUrl = numOfSyncUrl;
        this.hardTimeoutSec = hardTimeoutSec;

        this.logger = LogManager.getLogger("Worker");
    }

    protected abstract void browse(AbstractBrowser browser, String url) throws IOException;

    @Override
    public void run() {
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            logger.warn("URL progress: index " + String.valueOf(i) + " / " + String.valueOf(urls.size()));
            for (int j = 0; j < browsers.size(); j++) {
                AbstractBrowser browser = browsers.get(j);
                logger.warn("Browser progress: index " + String.valueOf(j) + " / " + String.valueOf(browsers.size()));
                try {
                    browse(browser, url);
                } catch (IOException ex) {
                    logger.fatal("crawler cannot finish socket operation");
                }
            }
            if (i > 0 && i % numOfSyncUrl == 0)
                awaitOtherBrowsers();
        }
    }

    protected void awaitOtherBrowsers() {
        try {
            logger.warn("waiting for other browsers to finish......");
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
            logger.fatal("Synchronizing thread failed.");
            System.exit(-1);
        }
    }

    protected void awaitMitmproxy(ServerSocket listener) throws SocketTimeoutException, IOException {
        logger.info("wait for mitmproxy to connect...");
        Socket socket = listener.accept();
        logger.info("accept!");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logger.info("wait end TS = " + timestamp.getTime());
        String input = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
        if (!input.equals("SYN"))
            throw new RuntimeException("mitmproxy does not send syn");
        new PrintWriter(socket.getOutputStream(), true).println("ACK");
        logger.info("ACK");
        socket.close();
    }
}
