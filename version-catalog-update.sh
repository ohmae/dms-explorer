#!/bin/sh
set -eu
UPDATES_FILE="./gradle/libs.versions.updates.toml"
if [ -f "${UPDATES_FILE}" ]; then
  echo "Remove existing ${UPDATES_FILE}"
  rm -f "${UPDATES_FILE}"
fi
./gradlew versionCatalogUpdate --interactive --no-configuration-cache
