import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class MultithreadCrawler {
    final static Logger logger = LogManager.getLogger("MainCrawler");

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile","resources/log4j2.xml");

        final String appiumUrl = "http://127.0.0.1:4723/wd/hub";
        final int numOfSyncUrl = 5;
        final int hardTimeoutSec = 90 + 40; // The mobile browsers take about 10-15 secs to send stop command.
        final List<String> allUrls = Resource.loadTrancoList(0);
        int startIndex = Resource.loadCheckpoint(Config.CHECKPOINT_PATH);
        final List<String> urls = allUrls.subList(startIndex, allUrls.size());

        Monitor.loadOrCreate(Config.MONITORING_STATUS_PATH);

        final String firefoxProxy = "Firefox proxy address depends on the profile";

        List<List<DesktopBrowser>> desktopBrowsers = Arrays.asList(
        );

        List<List<RemoteDesktopBrowser>> remoteDesktopBrowsers = Arrays.asList(
            // Arrays.asList(
            //     new RemoteDesktopBrowser("chrome88", "r_chrome88", "127.0.0.1:10000", Config.R_CH88_SL, true)
            // ),
            // Arrays.asList(
            //     new RemoteDesktopBrowser("chrome88b", "r_chrome88b", "127.0.0.1:10001", Config.R_CH88B_SL, true)
            // ),
            // Arrays.asList(
            //     new RemoteDesktopBrowser("chrome88scroll", "r_chrome88scroll", "127.0.0.1:10002", Config.R_CH88S_SL, true)
            // ),
            // Arrays.asList(
            //     new RemoteDesktopBrowser("firefox86", "r_firefox86", "profile:127.0.0.1:10003", Config.R_FF86_SL, true)
            // ),
            // Arrays.asList(
            //    new RemoteDesktopBrowser("firefox86ghostery", "r_firefox86ghostery", "profile:127.0.0.1:10004", Config.R_FF86G_SL,true)
            // ),
            // Arrays.asList(
            //     new RemoteDesktopBrowser("firefox45", "r_firefox45", "profile:127.0.0.1:10005", Config.R_FF45_SL, true)
            // ), 
            // Arrays.asList(
            //     new RemoteDesktopBrowser("brave", "r_brave", "127.0.0.1:10006", Config.R_BR_SL, true)
            // ),
            // Arrays.asList(
            //     new RemoteDesktopBrowser("tor", "r_tor", "profile:127.0.0.1:10007", Config.R_TOR_SL, true)
            // ), 
            // Arrays.asList(
            //    new RemoteDesktopBrowser("chrome88selenium", "r_chrome88selenium", "127.0.0.1:10008", Config.R_SCH88_SL, true)
            // ),
            // Arrays.asList(
            //     new RemoteDesktopBrowser("firefox86selenium", "r_firefox86selenium", "profile:127.0.0.1:10009", Config.R_SFF86_SL, true)
            // )
        );

        List<List<OpenWPMMobile>> owmBrowsers = Arrays.asList(
        //   Arrays.asList(
        //       new OpenWPMMobile("firefox45", "openwpm-mobile-45", hardTimeoutSec, "depends on profile", Config.OWM45_SL_39000, true)
        //   ),
        //   Arrays.asList(
        //       new OpenWPMMobile("firefox65", "openwpm-mobile-65", hardTimeoutSec, "depends on profile", Config.OWM65_SL_39001, true)
        //   ),
        //   Arrays.asList(
        //       new OpenWPMMobile("firefox86", "openwpm-mobile-86", hardTimeoutSec, "depends on profile", Config.OWM86_SL_39001, true)
        //   )
        );

        // Mobile
        List<List<MobileBrowser>> mobileBrowsers = Arrays.asList(
            // Arrays.asList( // 38080
            //     new MobileBrowser("tor", Config.M_TOR_SL, "DEVICEID", "DEVICEID-tor-sl", appiumUrl, true)
            // ),
            // Arrays.asList( // 38081
            //     // customized user-agent chrome
            //     new MobileBrowser("chrome", Config.M_CHU_SL, "DEVICEID", "DEVICEID-chu-sl", appiumUrl, true)
            // ), 
            // Arrays.asList( // 38082
            //     // regular chrome
            //     new MobileBrowser("chrome", Config.M_CH_SL, "DEVICEID", "DEVICEID-ch-sl", appiumUrl, true)
            // ),
            // Arrays.asList( // 38083
            //     new MobileBrowser("firefox", Config.M_FF_SL, "DEVICEID", "DEVICEID-ff-sl", appiumUrl, true)
            // ),
            // Arrays.asList( // 38084
            //     new MobileBrowser("ghostery", Config.M_GHOST_SL, "DEVICEID", "DEVICEID-gh-sl", appiumUrl, true)
            // ),
            // Arrays.asList( // 38085
            //     new MobileBrowser("brave", Config.M_BR_SL, "DEVICEID", "DEVICEID-br-sl", appiumUrl, true)
            // ),
            // Arrays.asList( // 38086
            //     new MobileBrowser("firefoxfocus", Config.M_FOCUS_SL, "DEVICEID", "DEVICEID-fff-sl", appiumUrl, true)
            // ),
            // Arrays.asList( // 38087
            //     // chrome with scrolling
            //     new MobileBrowser("chrome-scroll", Config.M_CHS_SL, "DEVICEID" , "DEVICEID-chs-sl", appiumUrl, true)
            // ),
            // Arrays.asList( // 38088
            //     new MobileBrowser("duckduckgo", Config.M_DD_SL, "DEVICEID", "DEVICEID-dd-sl", appiumUrl, true)
            // )
        );

        List<Runnable> workers = new ArrayList<>();
        final int numOfBrowsers = desktopBrowsers.size() + remoteDesktopBrowsers.size() + mobileBrowsers.size() + owmBrowsers.size();
        CyclicBarrier barrier = new CyclicBarrier(numOfBrowsers, new Runnable(){
            long lastTimestamp = -1;
            long penalty = 0;
            @Override
            public void run() {
                Resource.saveCheckpoint(Config.CHECKPOINT_PATH, numOfSyncUrl);
                Monitor.dump(Config.MONITORING_STATUS_PATH);
            }
        });


        for (List<DesktopBrowser> desktopBrowser: desktopBrowsers) {
            workers.add(new DesktopWorker(desktopBrowser, urls, barrier, numOfSyncUrl,hardTimeoutSec));
        }
        for (List<RemoteDesktopBrowser> remoteDesktopBrowser: remoteDesktopBrowsers) {
            workers.add(new RemoteDesktopWorker(remoteDesktopBrowser, urls, barrier, numOfSyncUrl, hardTimeoutSec));
        }
        for (List<MobileBrowser> mobileBrowser: mobileBrowsers) {
            workers.add(new MobileWorker(mobileBrowser, urls, barrier, numOfSyncUrl, hardTimeoutSec));
        }
        for (List<OpenWPMMobile> owmBrowser: owmBrowsers) {
            workers.add(new OpenWPMMobileWorker(owmBrowser, urls, barrier, numOfSyncUrl, hardTimeoutSec));
        }

        startAndJoinAllWorkers(workers);
    }

    public static void startAndJoinAllWorkers(List<Runnable> workers) {
        Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread th, Throwable ex) {
                ex.printStackTrace();
                logger.fatal("Abort crawling because thread " + String.valueOf(th.getId() % workers.size() +1) +
                    " throws " + ex);
                System.exit(-1);
            }
        };

        List<Thread> threads = new ArrayList<>();
        for (Runnable worker: workers) {
            Thread t = new Thread(worker);
            t.setUncaughtExceptionHandler(handler);
            threads.add(t);
        }
        for (Thread t: threads)
            t.start();
        for (Thread t: threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
}

