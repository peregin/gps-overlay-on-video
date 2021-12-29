#!/usr/bin/env bash

CHECKSUMS_FILE=${1?"file name to persist the generated checksum"}

find . -type f -name build.sbt -o -name plugins.sbt | sort | xargs md5sum | cut -d" " -f1 > $CHECKSUMS_FILE
