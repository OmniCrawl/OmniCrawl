import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;

public class Resource {
    static final Logger logger = LogManager.getLogger("Resource");
    public static int loadCheckpoint(String path) {
        File file = new File(path);
        if (file.isFile()) {
            try {
                Scanner s = new Scanner(file);
                int startIndex = Integer.parseInt(s.nextLine().trim());
                s.close();
                System.err.println("Load last checkpoint: start crawling from index: " + String.valueOf(startIndex));
                return startIndex;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        System.err.println("Warning: checkpoint file " + path + " does not exist. Assume " +
                "starting from 0");
        return 0;
    }

    public static void saveCheckpoint(String path, int increment) {
        int index = loadCheckpoint(path) + increment;
        try {
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.println(String.valueOf(index));
            writer.close();
            System.err.println("Save checkpoint: index: " + String.valueOf(index));
        } catch (FileNotFoundException | UnsupportedEncodingException e){
            e.printStackTrace();
            throw new RuntimeException("Cannot save checkpoint file to " + path);
        }
        try {
            System.err.println("Backup db and logs.....");
            Runtime.getRuntime().exec(new String[]{"bash", "-c",
              "mkdir -p backup && cp log/* backup"
            }).waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Fail to backup db and logs");
        }
    }

    public static ArrayList<String> loadTrancoList(int k) {
        String path = Config.TRANCO_LIST;
        Scanner s = null;
        try {
            s = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot load tranco list from " + path);
        }
        ArrayList<String> list = new ArrayList<>();
        while (s.hasNextLine()) {
            String line = s.nextLine().trim();
            if (line.startsWith("http://") || line.startsWith("https://")){
                list.add(line);
            } else {
                list.add("http://" + line);
            }
        }
        s.close();
        if (k > 0 && k < list.size()) {
            return new ArrayList(list.subList(0, k));
        } else if (k == -1) {
            return list;
        } else {
            throw new RuntimeException(
                "Requested number of sites " 
                + Integer.toString(k) 
                + " is invalid for list of size " 
                + Integer.toString(list.size())
            );
        }
    }

    public static String sendHttpRequest(String urlString) {
        // https://stackoverflow.com/a/1359700
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                throw new RuntimeException("Response is not HTTP 200: " + br.readLine());
            }
            return new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
        } catch (Exception e) {
            e.printStackTrace();
            Resource.perror("Failed to send http request to " + urlString, e);
            return "this will never be reached";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void copyDirectory(String src, String dst) {
        try {
            FileUtils.copyDirectory(new File(src), new File(dst));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void perror(String msg, Exception e) {
        logger.fatal(msg, e);
        System.exit(-1);
    }

    public static void perror(String msg) {
        logger.fatal(msg);
        System.exit(-1);
    }
}
