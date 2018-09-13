#!/bin/bash

set -e

mvn site \
    -DskipTests \
    -Dinvoker.skip=true \
    --batch-mode \
    --show-version

mvn scm-publish:publish-scm \
    --settings .travis/settings.xml \
    -DskipTests \
    -Dinvoker.skip=true \
    --batch-mode
