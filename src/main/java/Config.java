/**
 * Wrapper class for all the paths
 */
public class Config {

    public final static String TEST_CASES = "resources/testCases.txt";
    public final static String TRANCO_LIST = "resources/test100.txt";
    public final static String CHECKPOINT_PATH = "log/checkpoint.txt";
    public final static String MONITORING_STATUS_PATH = "log/status.json";
    public final static String MOBILE_TIMEOUT_SCREENCAP_DIR = "log";

    // In most cases the two are the same.
    public final static String BROWSER_SYNC_HOST = "127.0.0.1";
    public final static String MAIN_CRAWLER_LISTEN_HOST = "127.0.0.1";

    public final static String PATH_TO_CHROME69_BINARY = "path/to/chrome";
    public final static String PATH_TO_CHROME72_BINARY = "path/to/chrome";
    public final static String PATH_TO_CHROME_DRIVER = "path/to/chromedriver";

    public final static String PATH_TO_OPERA_BINARY = "";
    public final static String PATH_TO_OPERA_DRIVER = "";

    // The firefox binary should be specified in the wrapper script.
    public final static String PATH_TO_FIREFOX62_WRAPPER = "/path/to/firefox62.py";
    public final static String PATH_TO_FIREFOX65_WRAPPER = "/path/to/firefox65.py";
    public final static String PATH_TO_FIREFOX_DRIVER = "path/to/geckodriver";
    // These files can be found in scripts/ but must be transfered to the browser's directory.
    public final static String PATH_TO_OPENWPM_MOBILE_FIREFOX45 = "path/to/launch_selenium_firefox45_python2.py";
    public final static String PATH_TO_OPENWPM_MOBILE_FIREFOX65 = "path/to/launch_selenium_firefox65.py";
    public final static String PATH_TO_OPENWPM_MOBILE_FIREFOX86 = "path/to/launch_selenium_firefox86.py";

    public final static int DESKTOP_WINDOW_WIDTH = 1600;
    public final static int DESKTOP_WINDOW_HEIGHT = 900;

    // deskop
    public final static String CH69_SL_10000 ="profiles/ch-69-sl-10000";
    public final static String FFG65_SF_10001 = "profiles/ffg-65-sf-10001";
    public final static String CH72_SL_10002 = "profiles/ch-72-sl-10002";
    public final static String FF65_SF_10003 = "profiles/ff-65-sf-10003";
    public final static String FF62_SL_10004 = "profiles/ff-62-sl-10004";
    public final static String FF62_SF_10005 = "profiles/ff-62-sf-10005";
    public final static String FF65_SL_10006 = "profiles/ff-65-sl-10006";
    public final static String CH72_SF_10007 = "profiles/ch-72-sf-10007";
    public final static String FFG65_SL_10008 = "profiles/ffg-65-sl-10008";
    public final static String CH69_SF_10009 = "profiles/ch-69-sf-10009";
    public final static String OWM45_SL_39000 = "profiles/openwpm-mobile-45";
    public final static String OWM65_SL_39001 = "profiles/openwpm-mobile-65";
    public final static String OWM86_SL_39001 = "profiles/openwpm-mobile-86";

    // Remote (Windows) desktop
    public final static String REMOTE_TMP_DIR = "D:\\profiles\\tmp2";
    public final static String R_CH72_SL = "D:\\profiles\\chrome72";
    public final static String R_CH72B_SL = "D:\\profiles\\chrome72b";
    public final static String R_CH72S_SL = "D:\\profiles\\chrome72scroll";
    public final static String R_CH88_SL = "D:\\profiles\\chrome88";
    public final static String R_CH88B_SL = "D:\\profiles\\chrome88b";
    public final static String R_CH88S_SL = "D:\\profiles\\chrome88scroll";
    public final static String R_FF65_SL = "D:\\profiles\\firefox65";
    public final static String R_FF65G_SL = "D:\\profiles\\firefox65ghostery";
    public final static String R_FF86_SL = "D:\\profiles\\firefox86";
    public final static String R_FF86G_SL = "D:\\profiles\\firefox86ghostery";
    public final static String R_FF45_SL = "D:\\profiles\\firefox45";
    public final static String R_SCH72_SL = "D:\\profiles\\chrome72selenium";
    public final static String R_SFF65_SL = "D:\\profiles\\firefox65selenium";
    public final static String R_SCH88_SL = "D:\\profiles\\chrome88selenium";
    public final static String R_SFF86_SL = "D:\\profiles\\firefox86selenium";
    public final static String R_BR_SL = "D:\\profiles\\brave88";
    public final static String R_TOR_SL = "D:\\profiles\\tor10";

    public final static String REMOTE_PATH_SEPERATOR = "\\";
    public final static String REMOTE_WEBDRIVER_KEY = "";
    public final static String REMOTE_WEBDRIVER_HTTP_ENDPOINT = "";
    public final static String REMOTE_CHROME72_PATH = "D:\\bin\\chrome72\\chrome72.exe";
    public final static String REMOTE_CHROME72B_PATH = "D:\\bin\\chrome72b\\chrome72b.exe";
    public final static String REMOTE_CHROME72SCROLL_PATH = "D:\\bin\\chrome72scroll\\chrome72scroll.exe";
    public final static String REMOTE_CHROME88_PATH = "D:\\bin\\chrome88\\chrome88.exe";
    public final static String REMOTE_CHROME88B_PATH = "D:\\bin\\chrome88b\\chrome88b.exe";
    public final static String REMOTE_CHROME88SCROLL_PATH = "D:\\bin\\chrome88scroll\\chrome88scroll.exe";
    public final static String REMOTE_FIREFOX65_PATH = "D:\\bin\\firefox65\\firefox65.exe";
    public final static String REMOTE_FIREFOX65GHOSTERY_PATH = "D:\\bin\\firefox65ghostery\\firefox65ghostery.exe";
    public final static String REMOTE_FIREFOX86_PATH = "D:\\bin\\firefox86\\firefox86.exe";
    public final static String REMOTE_FIREFOX86GHOSTERY_PATH = "D:\\bin\\firefox86ghostery\\firefox86ghostery.exe";
    public final static String REMOTE_FIREFOX45_PATH = "D:\\bin\\firefox45\\firefox45.exe";
    public final static String REMOTE_SELENIUM_CHROME72_PATH = "D:\\bin\\seleniumchrome72.exe";
    public final static String REMOTE_SELENIUM_FIREFOX65_PATH = "D:\\bin\\seleniumfirefox65.exe";
    public final static String REMOTE_SELENIUM_CHROME88_PATH = "D:\\bin\\chrome88selenium.exe";
    public final static String REMOTE_SELENIUM_FIREFOX86_PATH = "D:\\bin\\firefox86selenium.exe";
    public final static String REMOTE_BRAVE_PATH = "D:\\bin\\brave88\\brave.exe";
    public final static String REMOTE_TOR_PATH = "D:\\bin\\tor10\\tor.exe"; 

    // Mobile
    public final static String M_CH_SL = "profiles/chrome88.tar.bz2";
    public final static String M_CH_SF = "profiles/com.android.chrome.sf.tar.bz2";
    public final static String M_FF_SL = "profiles/firefox86.tar.bz2";
    public final static String M_FF_SF = "profiles/org.mozilla.firefox.sf.tar.bz2";
    public final static String M_GHOST_SL = "profiles/ghostery.tar.bz2";
    public final static String M_GHOST_SF = "profiles/com.ghostery.android.ghostery.sf.tar.bz2";
    public final static String M_CHU_SL = "profiles/chrome88-UA.tar.bz2";
    public final static String M_CHU_SF = "profiles/com.android.chrome-useragent.sf.tar.bz2";
    public final static String M_FOCUS_SL = "profiles/focus.tar.bz2";
    public final static String M_FOCUS_SF = "profiles/org.mozilla.focus.sf.tar.bz2";
    public final static String M_UC_SL = "profiles/com.UCMobile.intl.sl.tar.bz2";
    public final static String M_UC_SF = "profiles/com.UCMobile.intl.sf.tar.bz2";
    public final static String M_DD_SL = "profiles/duckduckgo.tar.bz2";
    public final static String M_DD_SF = "profiles/com.duckduckgo.mobile.android.sf.tar.bz2";
    public final static String M_CHS_SL = "profiles/chrome88-scroll.tar.bz2";
    public final static String M_CHS_SF = "profiles/com.android.chrome-scroll.sf.tar.bz2";
    public final static String M_TOR_SL = "profiles/tor.tar.bz2";
    public final static String M_TOR_SF = "profiles/org.torproject.torbrowser.sf.tar.bz2";
    public final static String M_BR_SL = "profiles/brave.tar.bz2";
    public final static String M_BR_SF = "profiles/com.brave.browser.sf.tar.bz2";
}

