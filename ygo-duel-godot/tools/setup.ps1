# Prepara las dependencias para compilar la extensión en Windows (PowerShell).
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
git submodule update --init --recursive
python tools/get_sqlite.py
python -m pip install scons
Write-Host "Listo. Compila con: scons platform=windows target=template_debug"
