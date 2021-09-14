#!/usr/bin/env python3

import sqlite3
import zlib
import hashlib
import sys

def connect(db_path):
    return _DB(db_path)

class _DB():
    auto_commit_counter = 0

    def __init__(self, db_path):
        self.db = sqlite3.connect(db_path)
        # This will improve the performance for HDD, but the DB COULD BE CORRUPTED if encounrtering power loss
        # Be sure to backup the db.
        # If using a SSD this optimization is subtle.
        # self.db.execute('pragma synchronous=0')
        self.db.execute('''
CREATE TABLE IF NOT EXISTS uid2md5 (
    uid TEXT PRIMARY KEY UNIQUE NOT NULL,
    md5 TEXT
)''')
        self.db.execute('''
CREATE TABLE IF NOT EXISTS content (
    md5 TEXT PRIMARY KEY UNIQUE NOT NULL,
    data BLOB
)''')
        self.db.commit()

    def commit(self):
        self.db.commit()

    def close(self):
        self.db.close()

    def insert(self, uid:str, data:bytes, commit=False):
        md5 = hashlib.md5(data).hexdigest()
        compressed = zlib.compress(data)
        self.db.execute('INSERT INTO uid2md5 VALUES(:uid, :md5)', dict(uid=uid, md5=md5))
        self.db.execute('INSERT OR IGNORE INTO content VALUES(:md5, :data)', dict(md5=md5, data=compressed))
        _DB.auto_commit_counter += 1
        if commit or _DB.auto_commit_counter % 100 == 0:
            self.commit()
