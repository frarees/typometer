#!/bin/bash

set -euo pipefail

jar_path=$1
app_version=$2
output_dir=$3
readme_path=$4
license_path=$5

if [[ $(uname -s) != "Linux" ]]; then
    echo "Linux packaging must be run on Linux." >&2
    exit 1
fi

if ! command -v jpackage >/dev/null 2>&1; then
    echo "jpackage was not found. Install JDK 16 or newer." >&2
    exit 1
fi

if [[ ! -f "$jar_path" ]]; then
    echo "Required input not found: $jar_path" >&2
    exit 1
fi

if [[ ! -f "$readme_path" ]]; then
    echo "Required input not found: $readme_path" >&2
    exit 1
fi

if [[ ! -f "$license_path" ]]; then
    echo "Required input not found: $license_path" >&2
    exit 1
fi

case $(uname -m) in
    x86_64|amd64)
        architecture=x64
        ;;
    aarch64|arm64)
        architecture=arm64
        ;;
    *)
        echo "Unsupported Linux architecture: $(uname -m)" >&2
        exit 1
        ;;
esac

jar_name=$(basename "$jar_path")
input_dir=$(mktemp -d "${TMPDIR:-/tmp}/typometer-linux.XXXXXX")
app_image_dir="$output_dir/app-image"
archive_path="$output_dir/typometer-$app_version-linux-$architecture.tar.gz"
trap 'rm -rf "$input_dir" "$app_image_dir"' EXIT

rm -rf "$app_image_dir"
mkdir -p "$input_dir" "$output_dir"
cp "$jar_path" "$input_dir/$jar_name"

java_major=$(java -version 2>&1 | awk -F'"' '/version/ {print $2}' | cut -d. -f1)
if [[ "$java_major" -ge 21 ]]; then
    compress_opt="--compress=zip-6"
else
    compress_opt="--compress=2"
fi

jpackage \
    --type app-image \
    --name Typometer \
    --app-version "$app_version" \
    --input "$input_dir" \
    --main-jar "$jar_name" \
    --main-class com.pavelfatin.typometer.Application \
    --dest "$app_image_dir" \
    --add-modules java.base,java.desktop,java.logging \
    --jlink-options "--strip-debug --no-header-files --no-man-pages $compress_opt" \
    --vendor "Pavel Fatin" \
    --copyright "Copyright 2017 Pavel Fatin"

cp "$readme_path" "$app_image_dir/README.html"
cp "$license_path" "$app_image_dir/LICENSE"

rm -f "$archive_path"
tar -C "$app_image_dir" -czf "$archive_path" Typometer README.html LICENSE
