# Virtual Machine Artifact

## Overview
To facilitate reproduction of this infrastructure for testing purposes, we have provided a VirtualBox virtual machine with OmniCrawl pre-loaded and ready to run over a set of websites.

The virtual machine can be found [here](https://zenodo.org/records/10841769)<sup>1</sup> in Open Virtualization Format (OVF). Please note that it is fairly large at around 11 GB.

### A note on validity
This virtual machine runs just a subset of the desktop browsers from our crawl. It does not replicate the entirety of the setup (described in the [readme](../README.md)), which requires 22 physical machines, including 18 Android phones. Additionally, to simplify setup, the crawler, proxy, and desktop browsers are all run on a single Linux host (the VM). In our crawl, we run all of these components on different machines, and the desktop browsers in particular are run on Windows to ensure ecological validity. Thus, this VM serves to simply showcase the underlying crawling infrastructure.
## Setup
Requirements: [VirtualBox](https://www.virtualbox.org) or equivalent virtualization software that supports OVF files must be used. VirtualBox itself supports the x86_64 architecture so machines of other architectures (e.g., `arm64` as found on Apple Silicon Macs) will not be compatible.

Under the _default configuration_ the host machine running the VM must have at least a dual-core CPU, 8 GB of RAM, and around 15 GB of disk space (ideally using an SSD). The configuration used for testing this VM was a Core i7-9750H processor with 32GB of RAM and a SSD with sufficient disk space and ~2GB/s read and write speeds.

Setup steps:
1. Download the virtual machine image from [here](https://zenodo.org/records/10841769)<sup>1</sup>. The SHA256 checksum for this file is `2551f723d2d91e33c9e17af0981d63ce3a7f20981ed4b723ba89448399b03d43`.
2. Import the image into into VirtualBox. (Please see this [documentation](https://docs.oracle.com/cd/E26217_01/E26796/html/qs-import-vm.html) for instructions).
3. Start the VM (named OmniCrawl). It should login automatically, but if it does not both the username and the password are `omnicrawl`.
4. Configure the crawler according to your available time and resources (see below) if desired. If not, the pre-loaded default configuration can be used without modifications.

### Modifications for more or less time and resources

The **default configuration** runs the proxy, Chrome, and Firefox. It crawls the Tranco Top 100 sites using this version of the [Tranco list](https://tranco-list.eu/list/4ZWX).
Under this configuration the host machine running the VM must have at least a dual-core CPU, 8 GB of RAM, and around 15 GB of disk space (ideally using an SSD). The crawl itself will take around **4 hours**.
- To change the amount of **time** the crawl takes, adjust the number of sites being crawled by using the command line flag `--num-sites=X`. During step 5 of [running the crawl](#running-the-test-crawl), use the this flag. For example, to run with just 10 sites: `python3 start.py --num-sites=10`.
  - If a different set of sites is desired for use that can be done by changing, in `src/main/java/Resource.md`, the line `public final static String TRANCO_LIST` to point to a different list.
- To reduce the amount of **resources** the crawl uses, reduce the set of browsers to only Chrome or only Firefox by commenting out one of those two browsers from the list of desktop browsers in this file: `src/main/java/MultithreadCrawler.java`, this list: `List<List<DesktopBrowser>> desktopBrowsers = Arrays.asList(...);`.

For the above modifications, nothing needs to be done besides editing the listed files as described. Compilation will happen automatically when the infrastructure is run.


## Running the test crawl

After the virtual machine has been imported and the crawl is configured, running the crawl uses a similar set of instructions as found in our main [readme](../README.md):
1. Run the Ubuntu terminal application by either selecting it from the dock on the left, or by right-clicking on the desktop and selecting "Open Terminal".
2. Navigate to the repository folder which is at `/home/omnicrawl/omnicrawl`. This can be done with `cd ~/omnicrawl`.
3. Run the proxy with `./run-mitmproxy.sh`. You should see output that looks like this: ![proxy start output](images/proxy_start.png)
4. Verify that the proxy instances have launched: `ps aux | grep mitm | head -n 10`. You should see output like this: ![proxy verification output](images/proxy_check.png)
5. Run the crawler with `python3 start.py`. This will compile the infrastructure and run the crawler. At the end of the compilation, you should see this output: ![crawler start output](images/crawl_start.png)

## Expected behavior

Once the crawler is running, Chrome and Firefox should appear and disappear onscreen, each time loading a different site from the list of sites to crawl. Within the VM we have provided an [example log](./example_crawl.log) that shows what the expected behavior in the console logs looks like when visiting the first five sites from `resources/test100.txt`. Please note that during a given run of the crawler for a set of sites many of the details in the log will be different due to variations in network activity, machine performance, and site behavior.

### Data

Crawl data and log files for each browser are stored in the proxy's configured data directory (`./data` by default, but configurable in [mitmboot.sh](../proxy/mitmboot.sh)) and are prefixed with the listening port assigned to the browser (`10000` for Chrome and `10003` for Firefox, by default).

- `PORT.log.sqlite3`: log of requests and API accesses.
- `PORT.mitmproxy.log`: mitmproxy raw logs
- `PORT.dump.sqlite3`: saved resources (js, html)

At the end of the crawl, the main crawl data will be in the data directory in `10000.log.sqlite3` and `10003.log.sqlite3`. These sqlite3 databases can then be loaded and inspected using any software compatible with sqlite3 (for our analysis we use the Python3 `sqlite3` module). There will also be files for other ports (e.g., `39001.log.sqlite3`) but they will not contain any data because only Chrome and Firefox are being used in this test crawl.

#### Manually viewing crawl data
We have included an installation of sqlite3 in the VM that can be used to view crawl data. For example, below we show how to view the first two crawler entries in the crawl table for the browser connected to port `10000` (Chrome), for all columns except the data column since that needs to be decompressed.
```
$ sqlite3 data/10000.log.sqlite3
> SELECT browser,alexa_url,timeout FROM crawl LIMIT 2;
desktop-chrome88-d_chrome88|http://google.com|0
desktop-chrome88-d_chrome88|http://youtube.com|0
```
As we can see, in this database, the desktop Chrome v88 browser's first two site visits were to google.com and youtube.com. Neither of these two visits timed out. To see recorded requests and API accesses for those two visits we need to decompress the `data` column's value (not shown above) as described below.

#### Database table schema
Each sqlite3 log database (e.g., `10000.log.sqlite3`) contains a single table, `crawl`. The schema of the `crawl` table is as follows:
```
browser | alexa_url | timeout | data
TEXT    | TEXT      | INTEGER | BLOB
```

The `browser` column indicates the browser used (e.g., `desktop-chrome88-d_chrome88`). The `alexa_url` column contains the URL that was visited by the browser (note that even though the column title includes `alexa`, we used URLs from the Tranco list). The `timeout` column indicates whether the visit to that URL was successful (`0`) or timed out `1`. Finally, the `data` column contains compressed JSON data from the visit of that site, which can be decompressed with python3's `zlib.decompress`. The schema for that data is listed below:
```javascript
- url
- browser
- timestamp
- timeout
- requests // List of request objects, each containing:
    - timestamp
    - scheme
    - host
    - url
    - method
    - uid
    - size
    - filetype
    - headers
    - response
        - status_code
        - headers
        - timestamp
        - uid
        - filetype
        - size
    - is_ad // Classified (later) as tracking
    - is_tr // Classified (later) as advertising
- frames // For the main frame and iframes, an object containing:
    - js_logs
        - api
        - method
        - type
        - argv
        - ret
        - stacks
        - count
    - document_cookies
    - local_storage
    - session_storage
    - is_iframe
    - referrer
    - iframes
    - url
    - scroll_behavior // Identified page scroll behavior
    - http_cookies
- scroll // Scrolling or not scrolling
```

### Analysis

Data of the above format, gathered from a run of the crawling infrastructure, is exactly what is used in the paper (as described in Section 3.2).
Below we discuss some details about how we performed data preprocessing and analysis.

#### Data preprocessing for analysis

Our data preprocessing pipeline consists of multiple steps. Each is described below.
1. Merging: We combine the data from all of the `*.log.sqlite3` files into a single database `merged.log.sqlite3`. This is done simply by creating a new database (with the same crawl table schema) and populating it with the data from each `*.log.sqlite3` file.
2. Labeling: After running the crawl over a set of sites, we have a record of requested URLs for each (browser, site) pair. However, a core component of our paper is distinguishing and measuring tracking-and-advertising requests. To do this, we additionally label each request with the fields `is_ad` and `is_tr`. The first denotes advertising, as measured by advertising-focused blocker lists, and the second denotes tracking, as measured by tracking-focused blocker lists. The lists used for this labeling process are cited in the paper. Concretely, to perform the labeling, we use the python3 package [adblockparser](https://pypi.org/project/adblockparser/) which can match URLs against a list of blocker rules. Separately, we also label requests as first-party and third-party.
3. Filtering: We filter out requests that are known to be browser-generated. As described in the paper, the methodology for doing this is to find URLs that requested during more than 50 website visits for only one browser but not on any other. We then manually validate whether each of these URLs is actually browser-generated.
4. Provenance: We associate each requested third-party URL with an owning company using the [webxray](https://github.com/timlib/webXray) dataset.
  
Finally, we load our data into our analysis environment (a Jupyter notebook using the Python3 kernel) by reading it into a [pandas](https://pandas.pydata.org) `DataFrame`.
All analysis is performed over the `DataFrame` object. To reduce RAM requirements, when appropriate we compress the list of requests to just counts of first-party, third-party, and first- and third-party tracking-and-advertising requests.

#### Analysis results (figures and statistics)

To generate the figures and perform the statistical analyses in our paper, we rely on a number of Python3 data science packages. 

For figure generation, we use the [seaborn](https://seaborn.pydata.org) and [matplotlib](https://matplotlib.org) packages. Figures are generated using standard functions for generation of bar charts (usually with a `hue` component): please see the [seaborn documentation](https://matplotlib.org) for details.

For statistical analysis, we rely primarily on the [scipy stats](https://docs.scipy.org/doc/scipy/reference/stats.html) library. For post-hoc tests, we use the [scikit-posthocs](https://scikit-posthocs.readthedocs.io/en/latest/) package. Finally, for multiple-testing correction we used the [statsmodels](https://www.statsmodels.org/dev/generated/statsmodels.stats.multitest.multipletests.html) package.

Thus, reproduction of a result from the paper involves a few steps:
1. Extract the relevant data from the preprocessed `DataFrame`. For example, to compare third-party tracking-and-advertising requests of Chrome and Firefox, we would select the count of third-party requests labeled as `is_ad` or `is_tr` for every site successfully crawled by both Chrome and Firefox in two groups; one for each browser.
2. Next, we can plot the data using the seaborn boxplot API, which accepts dictionaries or nested lists of data (for example, `{'group1': [...], 'group2': [...]}`).
3. Finally, we can apply the appropriate statistical analysis; for example, a two-group comparison with the Mann-Whitney U test (`scipy.stats.mannwhitneyu(group1, group2)`).

Please note that if repeated significance testing is performed, multiple testing correction should be applied. We used the Holm-Bonferroni method as supported by `statsmodels.stats.multitest`.


<hr>

### Footnotes

1. Please note that this artifact has been updated due to the recent RCE vulnerability disclosed for log4j ([CVE-2021-44228](https://github.com/advisories/GHSA-jfh8-c2jp-5v3q)). The [old link](https://cmu.box.com/s/56lckmrh14v38pfb3ehueeln801f2g04) is still accessible but we do not recommend its use. The SHA-256 checksum for the old link was `763659C3E5C60200DB2052A8AB3DB1B20482E333E30BE59DF36FD14ADC1C9500`.
