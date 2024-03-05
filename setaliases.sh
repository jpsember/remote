#!/usr/bin/env bash
set -eu

alias mk="mvn package -Dmaven.test.skip=true"
alias r="java -Dfile.encoding=UTF-8 -classpath target/remote-1.0-jar-with-dependencies.jar remote.Main"
