#!/usr/bin/env bash

echo 'Attempting to deploy artifacts if needed'

# Verify branch set in Travis
echo "Branch: $TRAVIS_BRANCH"
if [[ -z ${TRAVIS_BRANCH} ]]; then # Exit if $TRAVIS_BRANCH is unset
    echo 'Not Travis or branch undetected, exiting'
    exit -1
else
    echo 'Valid branch'
fi

# Verify pull-request status (should be false)
echo "Pull-request status: $TRAVIS_PULL_REQUEST"
if [[ ${TRAVIS_PULL_REQUEST} != 'false' ]]; then # Exit if TRAVIS_PULL_REQUEST is not 'false'
    echo "Pull-request status should be 'false'"
    exit -1
else
    echo 'Valid pull-request status'
fi

# Verify that JAVA_HOME is set
if [[ -z ${JAVA_HOME} ]]; then # Exit if JAVA_HOME is unset
    echo 'JAVA_HOME variable is unset, exiting'
    exit -1;
fi

# Get project version using special script
project_version=$(./project-version.sh)
echo "Got project version: ${project_version}"

if [[ ${project_version} == *-SNAPSHOT ]]; then # Try to deploy snapshot if version ends with '-SNAPSHOT'
    echo 'Snapshot version'
    # Snapshots deployment happens only for `development` branch excluding pull requests to it (but including merges)
    if [[ "$TRAVIS_BRANCH" = 'development' ]]; then
        echo "Deploying ${project_version} to Sonatype repository"
        .travis/ossrh-deploy.sh
    else
        echo 'Not deploying as branch is not `development`'
    fi
else # Try to deploy release if version doesn't end with '-SNAPSHOT'
    echo 'Release version'
    # Release deployment happens only for `releases` branch excluding pull requests to it (but including merges)
    if [[ "$TRAVIS_BRANCH" = 'releases' ]]; then
        echo "Deploying ${project_version} to Maven Central"
        .travis/ossrh-deploy.sh
    else
        echo 'Not deploying as branch is not `releases`'
    fi
fi