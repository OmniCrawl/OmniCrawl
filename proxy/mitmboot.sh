#!/bin/bash

# http2 behind proxy will cause problem when redirecting
# e.g. ettoday.net, yahoo.co.jp
echo 'Note: Disable HTTP2 in mitmproxy'

mitmdump -s "proxy/injector.py" \
--set block_global=false \
--set js_filepath=proxy/inject.js \
--set timeout_msec=90000 \
--set log_filepath=./data/$1.log.sqlite3 \
--set dump_filepath=./data/$1.dump.sqlite3 \
--listen-host 127.0.0.1 \
--listen-port "$1" \
--anticache \
--no-http2 \
2>&1 | tee -a "./data/$1.mitmproxy.log"
