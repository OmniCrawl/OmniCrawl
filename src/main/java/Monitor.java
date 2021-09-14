import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;

public class Monitor {
    static final HashMap<String, HashMap<String, Integer>> crawlingStatus = new HashMap<>();
    static final Logger logger = LogManager.getLogger("Monitor");

    public static void start(String browserName) {
        createOrIgnoreBrowser(browserName);
        HashMap<String, Integer> log = crawlingStatus.get(browserName);
        log.put("last_t", (int)(System.currentTimeMillis() / 1000));
    }

    public static void end(String browserName) {
        createOrIgnoreBrowser(browserName);
        HashMap<String, Integer> log = crawlingStatus.get(browserName);
        int span = (int)(System.currentTimeMillis() / 1000) - log.get("last_t");
        log.put("elapsed_time", log.get("elapsed_time") + span);
    }

    public static void reportStatus(String browserName, String status) {
        createOrIgnoreBrowser(browserName);
        HashMap<String, Integer> log = crawlingStatus.get(browserName);
        if (status.equals("success")) {
            log.put("consecutive_timeout", 0);
        } else if (status.equals("timeout")) {
            log.put("consecutive_timeout", log.get("consecutive_timeout") + 1);
        } else {
            throw new UnsupportedOperationException("Status " + status + " is invalud!");
        }
        log.put(status, log.get(status) + 1);
        //logger.info(
        //    "Monitoring: " + browserName
        //        + " timeout " + String.valueOf(log.get("timeout"))
        //        + " success " + String.valueOf(log.get("success"))
        //        + " consecutive_timeout " + String.valueOf(log.get("consecutive_timeout"))
        //        + " timeout percent " + 100 * (((double)log.get("timeout")) / ((double)(log.get("success") + log.get(
        //        "timeout"))))
        //        + " crawling time in total " + String.valueOf(log.get("elapsed_time"))
        //        + " crawling time avg " + (((double)log.get("elapsed_time")) / ((double)(log.get("success") + log.get(
        //        "timeout"))))
        //);
    }

    public static void loadOrCreate(String path) {
        try {
            File f = new File(path);
            InputStream is = new FileInputStream(f);
            String jsonTxt = IOUtils.toString(is, "UTF-8");
            JSONObject json = new JSONObject(jsonTxt);
            for (int i = 0; i < json.names().length(); i++) {
                String browserName = json.names().getString(i);
                HashMap<String, Integer> log = new HashMap<>();
                JSONObject browserStatus = (JSONObject)json.get(browserName);
                for (int j = 0; j < browserStatus.names().length(); j++) {
                    String key = browserStatus.names().getString(j);
                    log.put(key, (int)browserStatus.get(key));
                }
                crawlingStatus.put(browserName, log);
            }
        } catch (FileNotFoundException e) {
            logger.warn("Monitoring status file " + path + " not found. Create a new one...");
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot load status from " + path);
        }
    }
    public static void dump(String path) {
        JSONObject json = new JSONObject(crawlingStatus);
        try {
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.println(json);
            writer.close();
            System.err.println("Dump log to " + path);
        } catch (FileNotFoundException | UnsupportedEncodingException e){
            e.printStackTrace();
            throw new RuntimeException("Cannot dump status to " + path);
        }
    }

    public static void createOrIgnoreBrowser(String browserName) {
        if (! crawlingStatus.containsKey(browserName)) {
            HashMap<String, Integer> log = new HashMap<>();
            log.put("success", 0);
            log.put("timeout", 0);
            log.put("consecutive_timeout", 0);
            log.put("elapsed_time", 0);
            crawlingStatus.put(browserName, log);
        }
    }

}
