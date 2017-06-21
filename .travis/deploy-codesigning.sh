#!/usr/bin/env bash

echo "Deploying code signing key..."

cd ./.travis || exit

openssl aes-256-cbc -K "$encrypted_b1c707644e9a_key" -iv "$encrypted_b1c707644e9a_iv" -in codesigning.asc.enc -out codesigning.asc -d
gpg --fast-import codesigning.asc

cd ../ || exit
