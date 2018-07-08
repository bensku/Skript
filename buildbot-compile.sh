#!/bin/bash
# Compiles Skript with some special settings used to produce unstable builds

# Version identifier: 2.2, date YYMMDD, git head
SKRIPT_VER="2.2-$(date +%y%m%d)-git-$(git rev-parse --short HEAD)"
export SKRIPT_VERSION=$SKRIPT_VER

./gradlew clean build # Clean all, then build

# Write name for upload script in my server to use
echo "$SKRIPT_VER" >buildbot-upload-version.txt
