#!/usr/bin/env python
import redis
import psycopg2
import copy
import simplejson as json
from hashlib import sha1
from psycopg2.extras import RealDictCursor
from base64 import b64decode, b64encode


REDIS_HOST = "localhost"
REDIS_PORT = 6379
REDIS_DB   = 0

POSTG_HOST = "localhost"
POSTG_PORT = 5432
#
POSTG_ACDB = "signal"
POSTG_WHDB = "whitelistdb"
#
POSTG_USER = "octopus"
POSTG_PASS = "octopus"

redis_cli = redis.StrictRedis(host=REDIS_HOST, port=REDIS_PORT, db=REDIS_DB)

def update_directory(number):
    def create_hash(number):
        return b64encode(sha1(number).digest()[:10])
    encoded = create_hash(number)

    dec = b64decode(encoded.replace('-', '+').replace('_', '/'))
    redis_cli.hset("directory", dec, json.dumps({"r":None,"v":True}))


def update_old_records():
    acc_params = {
        "host":    POSTG_HOST,
        "port":    POSTG_PORT,
        "database":POSTG_ACDB,
        "user":    POSTG_USER,
        "password":POSTG_PASS
    }
    whi_params = {
        "host":    POSTG_HOST,
        "port":    POSTG_PORT,
        "database":POSTG_WHDB,
        "user":    POSTG_USER,
        "password":POSTG_PASS
    }
    whi_get_conn = psycopg2.connect(**whi_params)
    acc_get_conn = psycopg2.connect(**acc_params)
    acc_update_conn = psycopg2.connect(**acc_params)
    
    whi_get_cursor = whi_get_conn.cursor(cursor_factory=RealDictCursor)
    acc_get_cursor = acc_get_conn.cursor(cursor_factory=RealDictCursor)
    acc_update_cursor = acc_update_conn.cursor()

    acc_get_cursor.execute("select * from accounts")
    for account in acc_get_cursor.fetchall():
        number      = account.get("number")
        data        = account.get("data")
        #
        whi_get_cursor.execute("select type from whitelist where number = '%s'" % number)
        type = whi_get_cursor.fetchone()
        whitelisted = type.get("type") if type else 0
        data["whitelisted"] = whitelisted
        serialized_data = json.dumps(data)
        acc_update_cursor.execute("UPDATE accounts SET data = CAST('%s' AS json) WHERE number = '%s'" % (serialized_data, number))
        redis_cli.set("Account5%s" % number, serialized_data)
        #update_directory(number)
    #
    acc_update_conn.commit()
    #
    whi_get_conn.close()
    acc_get_conn.close()
    acc_update_conn.close()

if __name__ == "__main__":
    update_old_records()
