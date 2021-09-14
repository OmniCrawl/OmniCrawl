#!/usr/bin/env python3
# Usage: mitmdump -s "../src/injector.py" --set block_global=false --anticache
# Note: the exact path (i.e. to "injector.py" or "inject.js") may depend on file structure

import sys
import socket
from http.cookies import SimpleCookie
from bs4 import BeautifulSoup
from mitmproxy import ctx
from mitmproxy import http
import json
import traceback
import re
import time
import datetime
import base64
import hashlib
import zlib
import secrets

import sqlitedb
import htmljsdb

def getTime():
    return datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S ')

class JsInjection:
    timeout_msec = None
    ending_timestamp_msec = None
    should_scroll = False
    openwpm_mobile_86 = False

    def __init__(self):
        self.injectScript = ""
        self.injectScriptHash = ""

    def load(self, entry):
        # We will parse the argument in configure(self, updates)
        # https://docs.mitmproxy.org/stable/addons-options/#handling-configuration-updates
        entry.add_option(
            name = 'js_filepath',
            typespec = str,
            default = '',
            help = 'The filepath of the injecting javascript',
        )
        entry.add_option(
            name = 'timeout_msec',
            typespec = int,
            default = int(90 * 1000),
            help = 'Timeout in msec for visiting a page',
        )
        assert any('js_filepath=' in argv for argv in sys.argv), 'You must provide an injecting js_filepath'
        assert any('timeout_msec=' in argv for argv in sys.argv), 'You must provide timeout in msec'

    def configure(self, updates):
        options = ctx.options
        self.filepath = options.js_filepath
        JsInjection.timeout_msec = options.timeout_msec
        """
        Event Handler: "Called when an addon is first loaded. This event
        receives a Loader object, which contains methods for adding options
        and commands. This method is where the addon configures itself."
        """
        ctx.log.info("Loading addon...")
        ctx.log.info(f'timeout is {JsInjection.timeout_msec} msec')
        with open(self.filepath, "r") as injectJS:
            self.injectScript = injectJS.read()
            self.injectScriptHash = base64.b64encode(hashlib.sha256(self.injectScript.encode()).digest()).decode()
            ctx.log.info(f'Loading javascript from {self.filepath} complete, obtained inject script')

    def response(self, flow):
        # Note: the mitm log API doesn't contain content-type in response
        # TODO: it is possible some html responses have empty content-type
        # TODO: mime-sniffing
        if 'text/html' in flow.response.headers.get("content-type", ''):
            # We ignore 'content-security-policy-report-only' since it's not enforced
            csp_header_name = 'content-security-policy'
            csp = flow.response.headers.get(csp_header_name)
            http_raw_nonce = None
            if csp:
                flow.response.headers[csp_header_name] = self._removeScriptSrcHash(csp)
            html = BeautifulSoup(flow.response.content, "html.parser")
            self._removeHtmlMetaCsp(html)
            script = html.new_tag("script", nonce='deadbeefdeadbeef')
            assert JsInjection.ending_timestamp_msec is not None, 'ending timestamp is not set. You must visit /start API first.'
            script.string = self.injectScript.replace('__ENDING_TIMESTAMP_MSEC', str(JsInjection.ending_timestamp_msec))
            script.string = script.string.replace('__SHOULD_SCROLL', str(JsInjection.should_scroll).lower())
            script.string = script.string.replace('__OWPM86', str(JsInjection.openwpm_mobile_86).lower())

            # For legacy python: script.async = False
            # Note that async is a keyword in Python3.5+
            script.isAsync = False


            if html.head:
                html.head.insert(0, script)
            else:
                head = html.new_tag("head")
                head.insert(0, script)
                html.insert(0, head)

            flow.response.content = html.encode('utf-8')
            if JsInjection.should_scroll:
                ctx.log.info('--- Injected inject.js with scrolling ---')
            elif JsInjection.openwpm_mobile_86:
                ctx.log.info('--- Injected inject.js with OWPM86 overrides ---')
            else:
                ctx.log.info('--- Injected inject.js ---')

    def _removeHtmlMetaCsp(self, html):
        raw_nonce = None
        # <meta http-equiv> has to be in <head>
        # it can also be inside a <noscript> element but we don't care
        # all browsers support JavaScript execution
        # https://developer.mozilla.org/en-US/docs/Web/HTML/Element/meta
        if html.head:
            meta_csps = [
                tag for tag in html.head.findAll('meta')
                if tag.attrs.get('http-equiv', '').lower() == 'content-security-policy' # bs4 attribute value is case-sensitive
            ]
            for meta_csp in meta_csps:
                ctx.log('Found meta csp: ' + str(meta_csp.attrs['content']))
                new_csp = self._removeScriptSrcHash(str(meta_csp.attrs['content']))
                ctx.log('new csp:' + new_csp)
                meta_csp.attrs['content'] = new_csp
                ctx.log('Replace with : ' + str(meta_csp.attrs['content']))

    def _removeScriptSrcHash(self, csp_str):
        def allowInlineScript(directive):
            '''
            1. allow inline script:
              - 'unsafe-inline' present: do nothing
                and hash or nonce value is NOT present
            2. does not allow inline script:
              - 'none' present: remove it 
              - other cases: add an additional nonce
            '''
            sources = directive.split(' ')[1:]
            has_unsafe_inline, has_nonce_or_hash = False, False
            for source in sources:
                if source == "'unsafe-inline'":
                    has_unsafe_inline = True
                if any(source.startswith(i) for i in ["'nonce-", "'sha256-", "'sha384-", "'sha512-"]):
                    has_nonce_or_hash = True
            if has_unsafe_inline and not has_nonce_or_hash:
                return directive
            new_sources = ['script-src', "'nonce-deadbeefdeadbeef'"] + [source for source in sources if source != "'none'"]
            return ' '.join(new_sources)

        new_directives = []
        script_src_directive, default_src_directive = None, None
        for directive in csp_str.split(';'): # should be '; ', but some webiste omit the space
            directive = directive.strip()
            if directive.startswith('script-src'):
                script_src_directive = directive
                continue # we will rewrite script-src later
            if directive.startswith('default-src'):
                default_src_directive = directive
            new_directives.append(directive)

        '''
        script_src + default_src: rewrite script_src
        script_src: rewrite script_src
        default_src: copy default_src to script_src and rewrite script_src
        none: do nothing
        '''

        if default_src_directive and not script_src_directive:
            script_src_directive = 'script-src' + default_src_directive[len('default-src'):]

        if script_src_directive:
            new_directives.append(allowInlineScript(script_src_directive))

        return '; '.join(new_directives)



'''
Remove all the httponly attribute in the cookies
'''
class HttoOnlyCookieInjection():
    def load(self, entry):
        ctx.log.info('httpOnly cookie Injection plugin loaded')

    def response(self, flow):
        if flow.response.headers.get('Set-Cookie'):
            try:
                flow.response.headers['Set-Cookie'] = self._removeHttpOnlyAttribute(cookies)
            except AssertionError:
                ctx.log.info(traceback.format_exc())
                ctx.log.info(cookies)
                ctx.log.info('Not modifying this cookies......')

    def _removeHttpOnlyAttribute(cookies_str: str) -> str:
        cookies = parseCookies(cookies_str)
        for cookie_key, cookie in cookies.items():
            # Ensure that there is no secure prefix because browsers will ignore
            # this kind of cookie if httpOnly is removed
            # https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie#Directives
            assert not cookie_key.lower().startswith('__Secure-'.lower()), 'Cannot remove httpOnly attribute of secure prefix cookie, see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie#Directives'
            cookie['httponly'] = False if cookie['httponly'] == True else cookie['httponly']
        # We don't need Set-Cookie header
        return cookies.output(header='', sep=',')

    def _parseCookies(cookies_str: str) -> SimpleCookie:
        assert cookies_str, 'Cannot parse empty cookies string'
        # Split by ", "
        # However, because expires attribute also contains ", "
        # Ignore "expires=Mon, 11-Oct-1988" cookie attributes
        ends = []
        pos = cookies_str.find(', ', 0)
        while pos != -1:
            if cookies_str[pos-4] != '=':
                ends.append(pos)
            pos = cookies_str.find(', ', pos+1)
        ends.append(len(cookies_str))

        # Extract cookie string
        new_cookies_str =''
        start = 0
        for end in ends:
            cookie = cookies_str[start:end] + '\r\n'
            assert SimpleCookie(cookie).output(), 'Cookie parse failed: ' + cookies_str[start:end] + 'of ' + cookies_str
            new_cookies_str += cookie
            start = end + len(', ')
        cookies = SimpleCookie(new_cookies_str)
        assert cookies.output(), 'Cookie parse failed: ' + repr(new_cookies_str)
        return cookies

class LogHandler():
    skipped_filetypes = {
        'empty': lambda x: len(x) == 0,
        'png': lambda x: x[1:4]== b'PNG',
        'jpg': lambda x: x[:2] == b'\xFF\xD8',
        'gif': lambda x: x[:3] == b'GIF',
        'webm': lambda x: x[:4] == b'\x1A\x45\xDF\xA3',
        'riff': lambda x: x[:4] == b'RIFF',
        'wof': lambda x: x[:3] == b'wOF',
        'truetype': lambda x:x[:5] == b'\x00\x01\x00\x00\x00',
        'iso': lambda x:x[4:11] == b'\x66\x74\x79\x70\x69\x73\x6f',
    }

    small_file_size = 2 ** 12

    def __init__(self):
        self._resetLogBucket()
        self.log_db = None
        self.dump_db = None

    def load(self, entry):
        # We will parse the argument in configure(self, updates)
        # https://docs.mitmproxy.org/stable/addons-options/#handling-configuration-updates
        entry.add_option(
            name = 'log_filepath',
            typespec = str,
            default = '',
            help = 'Where to save the log path',
        )
        entry.add_option(
            name = 'dump_filepath',
            typespec = str,
            default = '',
            help = 'The sqlite3 path of the html&js dumped',
        )
        assert any('log_filepath=' in argv for argv in sys.argv), 'You must provide log_filepath'
        assert any('dump_filepath=' in argv for argv in sys.argv), 'You must provide a path where HTML and js is dumped'

    def configure(self, updates):
        ctx.log.info('LogHandler plugin loaded')
        log_filepath = ctx.options.log_filepath
        self.log_db = sqlitedb.connect(log_filepath)
        ctx.log.info('All the log is saved to ' + log_filepath)
        dump_filepath = ctx.options.dump_filepath
        self.dump_db = htmljsdb.connect(dump_filepath)
        ctx.log.info(f'Dump all html and js to ' + dump_filepath)

    def request(self, flow):
        if '240.240.240.240' == flow.request.host:
            self._handleLoggingCommand(flow)
        # TODO: this is overgeneral
        elif 'non-exist-api' in flow.request.path:
            ctx.log.info(getTime() + 'Receive non-exist-api, time = ' + str(int(time.time())) + ' ' + flow.request.pretty_url)
            assert flow.request.method == "POST" and len(flow.request.content) > 0
            self._processSameOriginRequest(flow)
        # general request will be handled in response

    '''
    Called when the addon shuts down
    '''
    def done(self):
        self.dump_db.commit()
        self.dump_db.close()
        self.log_db.close()

    def _handleLoggingCommand(self, flow):
        if 'favicon.ico' in flow.request.path:
            flow.response = http.HTTPResponse.make(404, b'', {})
        elif 'start' in flow.request.path:
            # last log is timeout
            if self.log_bucket['_is_logging']:
                ctx.log.info(getTime() + "force timeout: last log doesn't stop or timeout")
                self._forceTimeout(flow)
            self.dump_db.commit()
            ctx.log.info(getTime() + 'Receive start, time = ' + str(int(time.time())))
            self._startLogging(flow)
        elif 'stop' in flow.request.path:
            if self.log_bucket['_is_logging']:
                ctx.log.info(getTime() + 'Receive stop, time = ' + str(int(time.time())))
                diff_msec = (int(time.time()) - int(self.log_bucket['timestamp'])) * 1000
                # If the stop command arrives too early, simply ignore it
                if diff_msec <= (JsInjection.timeout_msec * 0.40):
                    msg = 'Ignore this early stop command'
                    ctx.log.info(msg)
                    flow.response = http.HTTPResponse.make(200, msg, {})
                else:
                    self._stopLogging(flow)
            else:
                # When navigating to a new url, some browser will send a request to the last tab
                # again. It's because the url of the last tab is 240.240.240.240/stop. We just
                # ignore this request.
                msg = 'Ignore this duplicated stop command'
                ctx.log.info(msg)
                flow.response = http.HTTPResponse.make(200, msg, {})
        else:
            raise RuntimeError('Logging command endpoint API is not found: ' + flow.request.path)

    def response(self, flow):
        if not self.log_bucket['_is_logging']:
            ctx.log.info('not logging now......ignore this request & response')
            return
        if self._isSkippedUrl(flow):
            ctx.log.info('not logging this request......ignore this proxy API request')
            return
        # Log general requests
        # Request: save the content if its size > 0 (filter GET/OPTION and empty POST request)
        req_uid = ''
        req_content = flow.request.get_content(strict=False)
        req_size = len(req_content)
        if req_size > 0:
            req_uid = secrets.token_urlsafe(16)
            self.dump_db.insert(req_uid, req_content, commit=False)
        # Response: save the content if it's non-empty <= 4KB
        # OR it's not an known skipped filetypes
        res_uid = ''
        res_content = flow.response.get_content(strict=False)
        res_filetype = self._determineFileType(res_content)
        res_size = len(res_content)
        if 0 < res_size <= LogHandler.small_file_size or res_filetype == 'unknown':
            res_uid = secrets.token_urlsafe(16)
            self.dump_db.insert(res_uid, res_content, commit=False)
        self.log_bucket['requests'].append({
            'timestamp': flow.request.timestamp_end,
            'scheme': flow.request.scheme,
            'host': flow.request.host,
            'url': flow.request.url, # query is included
            'method': flow.request.method,
            'uid': req_uid,
            'size': req_size,
            'filetype': self._determineFileType(req_content),
            'headers': dict(flow.request.headers),
            'response': {
                'status_code': flow.response.status_code,
                'headers': dict(flow.response.headers),
                'timestamp': flow.response.timestamp_end,
                'uid': res_uid,
                'filetype': res_filetype,
                'size': res_size,
            }
        })


    def _resetLogBucket(self):
        ctx.log.info('Reset log bucket')
        self.log_bucket = {
            '_is_logging': False,
            'url': '',
            'browser': '',
            'timestamp': -1,
            '_sync_host': '',
            '_sync_port': -1,
            'timeout': False,
            'requests': [],
            'frames':[]
        }

    def _saveToLogDb(self, log_bucket):
        self.log_db.insert(
          browser=log_bucket['browser'],
          alexa_url=log_bucket['url'],
          timeout=log_bucket['timeout'],
          raw_data={
              k: v for k, v in log_bucket.items() if not k.startswith('_')
          }
        )

    def _startLogging(self, flow):
        self.log_bucket['_is_logging'] = True
        self.log_bucket['url'] = flow.request.query['url']
        self.log_bucket['browser'] = flow.request.query['browser']
        self.log_bucket['timestamp'] = str(int(time.time()))
        self.log_bucket['_sync_host'] = flow.request.query['sync_host']
        self.log_bucket['_sync_port'] = int(flow.request.query['sync_port'])
        self.log_bucket['scroll'] = (flow.request.query['scroll'].lower() == 'true')
        JsInjection.should_scroll = self.log_bucket['scroll']
        JsInjection.openwpm_mobile_86 = ('owpm86' in flow.request.query)
        ctx.log.info(getTime() + 'Start logging cookis and requests of ' + self.log_bucket['url'])
        JsInjection.ending_timestamp_msec = int(time.time()*1000) + JsInjection.timeout_msec
        # In order to conceal the referer, we are not using 301/302 redirecton here
        # For desktop browser in selenium, the driver.get() API is synchronous, which
        # means the function will return after all js and HTML are loaded and executed.
        # In order to prevent browsers from hanging, we set a callback function here.
        # Therefore, the function will return and the script can still be executed.
        flow.response = http.HTTPResponse.make(
            200,
            f'''
            <a href="{self.log_bucket['url']}" rel="noreferrer" id="autoclick"></a>
            <script>setTimeout(()=>document.getElementById('autoclick').click(),100);</script>
            '''.encode(),
            {}
        )

    def _stopLogging(self, flow):
        ctx.log.info(getTime() + 'Stop logging cookis and requests!')
        self._saveToLogDb(self.log_bucket)
        self._awaitCrawler()

        self._resetLogBucket()
        JsInjection.ending_timestamp_msec = None

        flow.response = http.HTTPResponse.make(200, 'OK', {})

    def _forceTimeout(self, flow):
        ctx.log.info(getTime() + 'force timeout ......')
        self.log_bucket['timeout'] = True
        self._saveToLogDb(self.log_bucket)

        self._resetLogBucket()
        JsInjection.ending_timestamp_msec = None

    def _processSameOriginRequest(self, flow):
        frame_log = json.loads(flow.request.content.decode())
        # TODO: anything we want to collect from this same-origin request?
        frame_log['http_cookies'] = flow.request.headers.get('Cookie', '')
        # The main frame will have is_iframe = false
        self.log_bucket['frames'].append(frame_log)
        flow.response = http.HTTPResponse.make(200, b'ACK', {})

    '''
    This function is used to sync with the crawler.
    '''
    def _awaitCrawler(self):
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(3.0)
        try:
            host, port = str(self.log_bucket['_sync_host']), int(self.log_bucket['_sync_port'])
            ctx.log.info(f"connecting to {host}:{port}")
            sock.connect((host, port))
            sock.send(b'SYN\n')
            ctx.log.info("sent SYN, await ACK......")
            assert sock.recv(4).decode().strip() == 'ACK', 'Failed, did not receive ACK from Java crawler!'
        except Exception as e:
            ctx.log.error(traceback.format_exc())
        finally:
            sock.close()

    def _determineFileType(self, data):
        for filetype, f in LogHandler.skipped_filetypes.items():
            if f(data):
                return filetype
        return 'unknown'

    def _isSkippedUrl(self, flow):
        return '240.240.240.240' == flow.request.host or 'non-exist-api' in flow.request.path

addons = [
    LogHandler(),
    JsInjection(),
]
