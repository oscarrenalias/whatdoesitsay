#!/bin/sh
# MongoDb installed with Brew is required
mongod run --rest --bind_ip localhost,192.168.255.11 --config /usr/local/Cellar/mongodb/1.8.2-x86_64/mongod.conf