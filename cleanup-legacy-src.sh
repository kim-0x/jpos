#!/usr/bin/env zsh
set -euo pipefail

cd "$(dirname "$0")"

if [[ ! -d "src/main/java" || ! -d "src/test/java" ]]; then
  echo "Expected Maven folders src/main/java and src/test/java were not found."
  exit 1
fi

echo "Removing legacy pre-Maven source tree under src/..."
rm -rf src/Main.java src/Model src/Repository src/Service src/Utils src/View

echo "Done. Remaining src layout:"
find src -maxdepth 2 -type d | sort
