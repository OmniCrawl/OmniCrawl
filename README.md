# OmniCrawl: Multi-Platform Web Measurement Infrastructure

This is the repository for the web measurement infrastructure OmniCrawl, from the paper "OmniCrawl: Comprehensive Measurement of Web Tracking With Real Desktop and Mobile Browsers", to appear at [PETS'22](https://petsymposium.org/index.php).

OmniCrawl is a web measurement tool that allows for recording of web requests and JavaScript browser API acccesses on multiple platforms: Linux, Windows, and Android. We have built in support for several browsers as well: mobile (Android)and desktop Chrome, Firefox, Brave, and Tor, as well as mobile Firefox Focus, DuckDuckGo, and Ghostery.

This repository will allow one to set up the infrastructure itself but does not provide any browsers or browser profiles (except for the [demonstration VM](documentation/Artifact.md)).

Below we discuss general installation and how to run a crawl using the infrastructure. Other documentation, including setup notes for the [browsers](documentation/Browsers.md), [browser profiles](documentation/Profiles.md), and [proxy](documentation/Proxy.md) can be found in the `documentation` folder.

## System Overview

To make full use of OmniCrawl, multiple non-virtual machines are required. The default configuration we use runs 42 browsers across 22 machines across two geographic locations (11 machines per location). The breakdown of machines _for a single location_ is as follows:
1. One Linux machine to host OpenWPM-Mobile browsers and run the crawler's controller.
2. One Linux machine to run the proxy
3. One Windows machine to host the desktop browsers (Chrome, Firefox, Brave, and Tor).
4. Nine Android phones to each host a separate browser (Chrome, Firefox, Brave, Tor, Firefox Focus, Ghostery, and DuckDuckGo).

Note that 1 and 2 can be the same machine if the machine is sufficiently powerful (typically running the proxy requires an entire machine if all browsers are used, but there may exist hardware that is capable of running more).

In our [demonstration VM](documentation/Artifact.md) we showcase a minimal version of the above setup that only has Chrome and Firefox, running on Linux, along with the proxy. Note that this setup may not be ecologically valid, since most users of Chrome and Firefox use Windows. The reason for this minimal setup is to be able to share a small, self-contained virtual machine.

## Installation

Installation of this software has been tested exclusively with **Ubuntu LTS 18.05, Windows 10, and Android 8.1**. We suspect that much of it will work with similarly on newer versions of those operating systems but it is possible that software incompatibilities may be encountered.

### Crawler Setup

This section describes setup for the Linux machine that will host the crawler ("crawler machine"), the OpenWPM-Mobile browsers, and connect to the mobile phones.

Prerequisites:
1. `python3`, `pip3`, `libffi-dev`, `libpq-dev`
2. Maven
3. `adb`
4. Appium

Setup steps:
1. `pip3 install -r requirements.txt`
2. Connect the mobile phones via `usb` and ensure they show up under `adb devices`. Note their device ID as that will need to be recorded in the crawler configuration.

OpenWPM-Mobile must also be installed and configured. See the setup notes for the [browsers](documentation/Browsers.md) and [browser profiles](documentation/Profiles.md).

### Proxy Setup

This section describes setup for the Linux machine ("proxy machine") that will host the proxy.

Prerequisites:
1. `python3`, `pip3`, `libffi-dev`, `libpq-dev`
2. `mitmproxy` version 4.0.4 (installable via `pipx install mitmproxy==4.0.4`)

Setup 
1. `pip3 install -r proxy-requirements.txt`
2. Create a folder to store crawl data (e.g., `./data`) and adjust `proxy/mitmboot.sh` to point to it.

Please see the [proxy](documentation/Proxy.md) documentation for notes on setting up networking.

### Windows Machine Setup

This section describes setup for the Windows machine that will host the desktop browsers ("windows machine").

Prerequisites:
1. `python3`, `pip3`, `pytools`

Setup
1. `pip3 install -r windows-requirements.txt`

The desktop browsers (Chrome, Firefox, Brave, and Tor) must also be installed and configured. See the setup notes for the [browsers](documentation/Browsers.md) and [browser profiles](documentation/Profiles.md).


## Running a crawl

Below, we describe the steps required to run a crawl over a set of sites specified in the [resources](src/main/java/Resource.java).

1. On the proxy machine start the proxy: `run-mitmproxy.sh`
2. On the windows machine start the webdriver `python3 webdriver.py`
3. On the crawler machine:
   1. Start Appium
   2. Start the crawler `python3 start.py`

Crawl data and log files for each browser are stored in the proxy's configured data directory (`./data` by default) and are prefixed with the listening port assigned to the browser.

- `PORT.log.sqlite3`: all network logs. Basically this is the only file we will use in the analysis.
- `PORT.mitmproxy.log`: mitmdump raw logs
- `PORT.dump.sqlite3`: saved resources (js, html)
