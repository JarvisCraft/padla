#!/usr/bin/env bash

echo 'Decrypting encryption key'
openssl aes-256-cbc -K ${ENCRYPTED_CODESIGNING_KEY} -iv ${ENCRYPTED_CODESIGNING_IV} \
-in .travis/gpg/codesigning.asc.enc -out .travis/gpg/codesigning.asc -d
echo 'Decrypted encryption key'

echo 'Importing encryption key'
gpg --fast-import .travis/gpg/codesigning.asc
echo 'Imported encryption key'

echo 'Deploying artifacts'
# Generate source and javadocs, sign binaries, deploy to Sonatype using credentials from env.
mvn deploy -P build-extras,sign,ossrh-env-credentials,ossrh-deploy --settings .travis/.mvn/settings.xml
echo 'Deployed artifacts'