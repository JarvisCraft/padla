#!/usr/bin/env bash

# This should be called from repository root

# Valid deployment modes:
# - sonatype-ossrh
# - github-package-registry

function deploy() {
  if [[ $1 != sonatype-ossrh && $1 != github-package-registry ]]; then
    echo "Unknown deployment target: $1"
    return 1
  fi

  maven_profiles=build-extras,sign-artifacts,import-env-code-signing-credentials,"$1"-deployment
  if [[ $2 == release && $1 == github-package-registry ]]; then
    maven_profiles="${maven_profiles},automatic-central-release"
  fi
  echo "Using maven profiles: [${maven_profiles}]"

  mvn deploy --settings ./.travis/maven/"$1"-settings.xml --activate-profiles "$maven_profiles" -B -V
}

deploy sonatype-ossrh "$1"
deploy github-package-registry "$1"