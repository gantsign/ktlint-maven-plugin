#!/bin/bash

set -e
set +x

# See http://www.debonair.io/post/maven-cd/ for instructions
openssl aes-256-cbc \
    -K "$encrypted_5ecbb759dc3f_key" \
    -iv "$encrypted_5ecbb759dc3f_iv" \
    -in .travis/codesigning.asc.enc \
    -out .travis/codesigning.asc -d
gpg --fast-import .travis/codesigning.asc

./mvnw deploy \
    --settings .travis/settings.xml \
    -P publish-artifacts \
    -DskipTests \
    -Dinvoker.skip=true \
    --batch-mode \
    --show-version
