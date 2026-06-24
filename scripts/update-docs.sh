#!/bin/sh

set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_dir=$(dirname "$script_dir")

mkdir -p "$project_dir/target"

pandoc -s -t html5 \
    -o "$project_dir/target/README.html" \
    -H "$project_dir/src/doc/style.css" \
    --lua-filter "$project_dir/src/doc/exclude.lua" \
    "$project_dir/README.md"
