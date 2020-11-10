#!/usr/bin/env bash

# This should be called from repository root

# Import encrypted code_signing.asc file
openssl aes-256-cbc -K "${ENCRYPTED_CODE_SIGNING_GPG_KEY_KEY}" -iv "${ENCRYPTED_CODE_SIGNING_GPG_KEY_IV}" \
-in ./.travis/gpg/code_signing.asc.enc -out ./.travis/gpg/code_signing.asc -d

gpg2 --allow-secret-key-import --batch --passphrase="${CODE_SIGNING_GPG_KEY_PASSPHRASE}" \
--keyring "${TRAVIS_BUILD_DIR}/pubring.gpg" --no-default-keyring --import ./.travis/gpg/code_signing.asc
