#!/bin/bash

set -euo pipefail

app_path=$1
readme_path=$2
license_path=$3
output_path=$4

if [[ $(uname -s) != "Darwin" ]]; then
    echo "DMG packaging must be run on macOS." >&2
    exit 1
fi

if ! command -v hdiutil >/dev/null 2>&1; then
    echo "hdiutil was not found." >&2
    exit 1
fi

for path in "$app_path" "$readme_path" "$license_path"; do
    if [[ ! -e "$path" ]]; then
        echo "Required input not found: $path" >&2
        exit 1
    fi
done

output_dir=$(dirname "$output_path")
staging_dir=$(mktemp -d "${TMPDIR:-/tmp}/typometer-dmg.XXXXXX")
trap 'rm -rf "$staging_dir"' EXIT

mkdir -p "$output_dir"
rm -f "$output_path"

cp -R "$app_path" "$staging_dir/Typometer.app"
cp "$readme_path" "$staging_dir/README.html"
cp "$license_path" "$staging_dir/LICENSE"
ln -s /Applications "$staging_dir/Applications"

hdiutil create \
    -quiet \
    -volname Typometer \
    -srcfolder "$staging_dir" \
    -format UDZO \
    -ov \
    "$output_path"
