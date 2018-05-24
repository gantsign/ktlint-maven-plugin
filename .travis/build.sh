#!/bin/bash

set -e

./mvnw install --batch-mode --show-version --settings .travis/settings.xml

./mvnw site-deploy -P site-deploy  --batch-mode --show-version --settings .travis/settings.xml
