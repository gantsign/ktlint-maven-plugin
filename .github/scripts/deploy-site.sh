#!/bin/bash

set -e

./mvnw site \
    -DskipTests \
    -Dinvoker.skip=true \
    --batch-mode \
    --show-version

./mvnw site:stage \
    -DskipTests \
    -Dinvoker.skip=true \
    --batch-mode

./mvnw scm-publish:publish-scm \
    --settings .travis/settings.xml \
    -P gh-pages \
    -DskipTests \
    -Dinvoker.skip=true \
    --batch-mode
