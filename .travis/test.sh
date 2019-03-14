#!/bin/bash

set -e

curl --silent 'https://get.sdkman.io' | bash
echo sdkman_auto_answer=true > ~/.sdkman/etc/config
source ~/.sdkman/bin/sdkman-init.sh
sdk install maven "$MAVEN_VERSION"

export M2_HOME="$HOME/.sdkman/candidates/maven/$MAVEN_VERSION"

"$M2_HOME/bin/mvn" install --batch-mode --show-version
