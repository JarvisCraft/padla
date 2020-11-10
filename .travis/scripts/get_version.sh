#!/usr/bin/env bash

# This should be called from repository root

# shellcheck disable=SC2016
# this is intentional as it is the value passed to Maven
mvn -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec --quiet
