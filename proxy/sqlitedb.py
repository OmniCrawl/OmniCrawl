#!/usr/bin/env python3

import sqlite3
import zlib
import json

def connect(db_path):
    return _DB(db_path)

class _DB():
    def __init__(self, db_path):
        self.db = sqlite3.connect(db_path)
        # This will improve the performance for HDD, but the DB COULD BE CORRUPTED if encounrtering power loss
        # Be sure to backup the db.
        # If using a SSD this optimization is subtle.
        # self.db.execute('pragma synchronous=0')
        self.db.execute('''
CREATE TABLE IF NOT EXISTS crawl (
    browser TEXT,
    alexa_url TEXT,
    timeout INTEGER,
    data BLOB
)''')
        self.db.commit()

    def insert(self, browser:str, alexa_url:str, timeout:bool, raw_data:dict):
        assert isinstance(raw_data, dict)
        payload = {
            'browser': browser,
            'alexa_url': alexa_url,
            'timeout': int(timeout),
            'data': zlib.compress(json.dumps(raw_data).encode())
        }
        assert isinstance(payload['timeout'], int) and 0 <= payload['timeout'] <= 1
        self.db.execute('INSERT INTO crawl VALUES(:browser, :alexa_url, :timeout, :data)', payload)
        self.db.commit()

    def close(self):
        self.db.close()
