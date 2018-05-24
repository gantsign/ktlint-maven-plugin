#!/bin/bash

set -e

./mvnw install --batch-mode --show-version --settings .travis/settings.xml

if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
    if [ "$TRAVIS_TAG" != "" ]; then
        # See http://www.debonair.io/post/maven-cd/ for instructions
        openssl aes-256-cbc -K $encrypted_5ecbb759dc3f_key -iv $encrypted_5ecbb759dc3f_iv \
            -in .travis/codesigning.asc.enc -out .travis/codesigning.asc -d
        gpg --fast-import .travis/codesigning.asc

        ./mvnw deploy -P publish-artifacts --batch-mode --show-version --settings .travis/settings.xml \
            -Denforcer.skip=true

        ./mvnw site-deploy -P site-deploy --batch-mode --show-version --settings .travis/settings.xml
    fi
fi
