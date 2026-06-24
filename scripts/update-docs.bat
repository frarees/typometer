@echo off
setlocal

set "PROJECT_DIR=%~dp0.."
if not exist "%PROJECT_DIR%\target" mkdir "%PROJECT_DIR%\target"

pandoc -s -t html5 ^
  -o "%PROJECT_DIR%\target\README.html" ^
  -H "%PROJECT_DIR%\src\doc\style.css" ^
  --lua-filter "%PROJECT_DIR%\src\doc\exclude.lua" ^
  "%PROJECT_DIR%\README.md"
