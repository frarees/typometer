param(
    [Parameter(Mandatory = $true)]
    [string]$JarPath,

    [Parameter(Mandatory = $true)]
    [string]$AppVersion,

    [Parameter(Mandatory = $true)]
    [string]$OutputDirectory,

    [Parameter(Mandatory = $true)]
    [string]$ReadmePath,

    [Parameter(Mandatory = $true)]
    [string]$LicensePath
)

$ErrorActionPreference = "Stop"

if ([System.Environment]::OSVersion.Platform -ne [System.PlatformID]::Win32NT) {
    throw "Windows packaging must be run on Windows."
}

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw "jpackage was not found. Install JDK 16 or newer."
}

if (-not (Test-Path -LiteralPath $JarPath -PathType Leaf)) {
    throw "Required input not found: $JarPath"
}

if (-not (Test-Path -LiteralPath $ReadmePath -PathType Leaf)) {
    throw "Required input not found: $ReadmePath"
}

if (-not (Test-Path -LiteralPath $LicensePath -PathType Leaf)) {
    throw "Required input not found: $LicensePath"
}

$architecture = switch ($env:PROCESSOR_ARCHITECTURE) {
    "AMD64" { "x64" }
    "ARM64" { "arm64" }
    default { throw "Unsupported Windows architecture: $env:PROCESSOR_ARCHITECTURE" }
}

$javaVersionOutput = (& java -version 2>&1) -join "`n"
$javaMajor = if ($javaVersionOutput -match '"(\d+)') { [int]$Matches[1] } else { 0 }
$compressOption = if ($javaMajor -ge 21) { "--compress=zip-6" } else { "--compress=2" }

$jar = Get-Item -LiteralPath $JarPath
$readme = Get-Item -LiteralPath $ReadmePath
$license = Get-Item -LiteralPath $LicensePath
$inputDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "typometer-windows-$([guid]::NewGuid())"
$outputPath = Join-Path $OutputDirectory "typometer-$AppVersion-windows-$architecture.msi"

try {
    New-Item -ItemType Directory -Force $inputDirectory, $OutputDirectory | Out-Null
    Copy-Item -LiteralPath $jar.FullName -Destination $inputDirectory
    Copy-Item -LiteralPath $readme.FullName -Destination $inputDirectory
    Copy-Item -LiteralPath $license.FullName -Destination $inputDirectory
    Get-ChildItem -LiteralPath $OutputDirectory -Filter "Typometer-*.msi" |
        Remove-Item -Force

    jpackage `
        --type msi `
        --name Typometer `
        --app-version $AppVersion `
        --input $inputDirectory `
        --main-jar $jar.Name `
        --main-class com.pavelfatin.typometer.Application `
        --dest $OutputDirectory `
        --add-modules java.base,java.desktop,java.logging `
        --jlink-options "--strip-debug --no-header-files --no-man-pages $compressOption" `
        --vendor "Pavel Fatin" `
        --copyright "Copyright 2017 Pavel Fatin" `
        --win-menu `
        --win-shortcut

    if ($LASTEXITCODE -ne 0) {
        throw "jpackage failed with exit code $LASTEXITCODE."
    }

    $generatedMsi = Get-ChildItem -LiteralPath $OutputDirectory -Filter "Typometer-*.msi" |
        Select-Object -First 1
    if (-not $generatedMsi) {
        throw "jpackage did not create an MSI."
    }

    Remove-Item -LiteralPath $outputPath -Force -ErrorAction SilentlyContinue
    Move-Item -LiteralPath $generatedMsi.FullName -Destination $outputPath
}
finally {
    Remove-Item -LiteralPath $inputDirectory -Recurse -Force -ErrorAction SilentlyContinue
}
