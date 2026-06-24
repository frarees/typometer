#!/bin/bash

set -euo pipefail

target_dir=$1
jar_name=$2
app_version=$3

input_dir="$target_dir/jpackage-input"
output_dir="$target_dir/macos"
app_path="$output_dir/Typometer.app"

if [[ $(uname -s) != "Darwin" ]]; then
    echo "The macos-app profile must be built on macOS." >&2
    exit 1
fi

if ! command -v jpackage >/dev/null 2>&1; then
    echo "jpackage was not found. Install JDK 16 or newer." >&2
    exit 1
fi

rm -rf "$input_dir" "$app_path"
mkdir -p "$input_dir" "$output_dir"
trap 'rm -rf "$input_dir"' EXIT
cp "$target_dir/$jar_name" "$input_dir/$jar_name"

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
    --dest "$output_dir" \
    --add-modules java.base,java.desktop,java.logging \
    --jlink-options "--strip-debug --no-header-files --no-man-pages $compress_opt" \
    --mac-package-identifier com.pavelfatin.typometer \
    --vendor "Pavel Fatin" \
    --copyright "Copyright 2017 Pavel Fatin" \
    --java-options "-Dapple.awt.application.name=Typometer"
