#!/bin/bash

set -e

./mvnw site-deploy \
    --settings .travis/settings.xml \
    -P site-deploy \
    -DskipTests \
    -Dinvoker.skip=true \
    --batch-mode \
    --show-version
