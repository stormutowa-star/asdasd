#!/bin/bash
# Regenera todos los modelos: spec -> .bbmodel -> textura -> Java (y capturas si SHOTS=1).
# Uso: all.sh [modelo ...]   (sin argumentos: todas las specs de blockbench/specs/*.js)
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../../../.." && pwd)"
export NODE_PATH="${NODE_PATH:-$(npm root -g)}"
cd "$ROOT"
if [ $# -gt 0 ]; then MODELS=("$@"); else MODELS=($(ls blockbench/specs/*.js | xargs -n1 basename | sed 's/\.js$//')); fi
for m in "${MODELS[@]}"; do
  cls=$(echo "$m" | sed -E 's/(^|_)([a-z0-9])/\U\2/g')
  echo "== $m ($cls)"
  node "$HERE/build_model.js" "blockbench/specs/$m.js" "blockbench/$m.bbmodel"
  python3 -I "$HERE/paint.py" "blockbench/$m.bbmodel"
  node "$HERE/bb2java.mjs" "blockbench/$m.bbmodel" "$cls"
  if [ "${SHOTS:-0}" = "1" ]; then node "$HERE/shots.js" "blockbench/$m.bbmodel"; fi
done
