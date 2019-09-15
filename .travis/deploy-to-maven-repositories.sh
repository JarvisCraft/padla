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

echo 'Deploying to Sonatype OSSRH (also used for Central integration)'
mvn deploy -P build-extras,sign,code-signing-credentials,sonatype-ossrh-deployment \
--settings .travis/.mvn/sonatype-ossrh-settings.xml
echo 'Deployed to Sonatype OSSRH'

echo 'Deploying to GitHub Package Registry'
mvn deploy -P build-extras,sign,code-signing-credentials \
--settings .travis/.mvn/github-package-registry-settings.xml \
-DaltDeploymentRepository=github-package-registry::default::https://maven.pkg.github.com/JarvisCraft/padla
echo 'Deployed to GiHub Package Registry'

echo 'Deployed artifacts'