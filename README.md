# ktlint Maven Plugin

[![Release](https://github.com/gantsign/ktlint-maven-plugin/workflows/Build/badge.svg)](https://github.com/gantsign/ktlint-maven-plugin/actions?query=workflow%3ABuild)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.gantsign.maven/ktlint-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.gantsign.maven/ktlint-maven-plugin)
[![codecov](https://codecov.io/gh/gantsign/ktlint-maven-plugin/branch/main/graph/badge.svg)](https://codecov.io/gh/gantsign/ktlint-maven-plugin)
[![Known Vulnerabilities](https://snyk.io/test/github/gantsign/ktlint-maven-plugin/badge.svg)](https://snyk.io/test/github/gantsign/ktlint-maven-plugin)

This plugin provides the ability to use
[ktlint](https://github.com/pinterest/ktlint) to format and check your source
code against the ktlint anti-bikeshedding code style.

## Using Java 17 and later

Java 17 is the first LTS release to enforce strong encapsulation. For `ktlint`
to work we need to add `--add-opens java.base/java.lang=ALL-UNNAMED` to the JVM
arguments.

We recommend that you add a `.mvn/jvm.config` file (relative to the top level
project directory) to all of your projects using this plugin. The file should
have the following contents:

```
--add-opens java.base/java.lang=ALL-UNNAMED
```

We also recommend adding this to all of your projects using this plugin and
building with Java 11, as it'll suppress an illegal-access warning during the
build.

For other options see:
[https://maven.apache.org/configure.html](https://maven.apache.org/configure.html)

## Goals Overview

  * [ktlint:format](http://gantsign.com/ktlint-maven-plugin/format-mojo.html)
    format your Kotlin sources using ktlint.

  * [ktlint:check](http://gantsign.com/ktlint-maven-plugin/check-mojo.html)
    check your Kotlin sources for code style violations using ktlint.

  * [ktlint:ktlint](http://gantsign.com/ktlint-maven-plugin/ktlint-mojo.html)
    generate project report of code style violations using ktlint.

## Usage

General instructions on how to use the ktlint plugin can be found on the
[usage page](http://gantsign.com/ktlint-maven-plugin/usage.html).

## License

This software is licensed under the terms in the file named "LICENSE" in the
root directory of this project. This project has dependencies that are under
different licenses.

## Author Information

John Freeman

GantSign Ltd.
Company No. 06109112 (registered in England)
