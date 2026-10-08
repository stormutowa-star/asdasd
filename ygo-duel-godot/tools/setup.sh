#!/usr/bin/env bash
# Prepara las dependencias para compilar la extensión (Linux / macOS / Git Bash).
set -e
cd "$(dirname "$0")/.."
git submodule update --init --recursive
python3 tools/get_sqlite.py
python3 -m pip install --user scons >/dev/null 2>&1 || true
echo "Listo. Compila con: scons platform=linux target=template_debug"
