#!/usr/bin/env python3
"""Genera los datos que usa el juego a partir de los repos de ProjectIgnis.

Copia a game/data/:
  - script/*.lua            scripts base (constant.lua, utility.lua, proc_*.lua...)
  - script/official/c*.lua  scripts de las cartas usadas en los mazos de game/decks/*.ydk
  - cards.cdb               base de datos reducida con esas cartas
  - strings.conf / strings_es.conf

Uso:
  python3 tools/build_data.py --scripts RUTA/CardScripts --cdb RUTA/BabelCDB/cards.cdb \
      [--strings RUTA/Distribution/config] [--extra 12345 67890] [--all]

Con --all se copian TODOS los scripts y la base de datos completa (cualquier mazo funcionará).
"""
import argparse
import glob
import os
import shutil
import sqlite3

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GAME = os.path.join(ROOT, "game")
DATA = os.path.join(GAME, "data")


def read_deck_codes(path):
    codes = []
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line.isdigit():
                codes.append(int(line))
    return codes


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--scripts", required=True, help="carpeta CardScripts")
    ap.add_argument("--cdb", required=True, help="cards.cdb de BabelCDB")
    ap.add_argument("--strings", help="carpeta config de ProjectIgnis/Distribution")
    ap.add_argument("--extra", nargs="*", type=int, default=[], help="códigos adicionales")
    ap.add_argument("--all", action="store_true", help="copiar todo el pool de cartas")
    args = ap.parse_args()

    script_out = os.path.join(DATA, "script")
    os.makedirs(os.path.join(script_out, "official"), exist_ok=True)

    for f in glob.glob(os.path.join(args.scripts, "*.lua")):
        shutil.copy2(f, script_out)

    if args.all:
        for sub in ("official", "unofficial", "pre-release"):
            src = os.path.join(args.scripts, sub)
            if os.path.isdir(src):
                shutil.copytree(src, os.path.join(script_out, sub), dirs_exist_ok=True)
        shutil.copy2(args.cdb, os.path.join(DATA, "cards.cdb"))
        print("Copiado el pool completo")
    else:
        codes = set(args.extra)
        for deck in glob.glob(os.path.join(GAME, "decks", "*.ydk")):
            codes.update(read_deck_codes(deck))
        src = sqlite3.connect(args.cdb)
        # Incluye también las cartas con alias (artes alternativos) de las usadas
        rows = src.execute("SELECT id FROM datas WHERE alias IN (%s)" % ",".join("?" * len(codes)), list(codes)).fetchall()
        codes.update(r[0] for r in rows)
        missing_scripts = []
        for code in sorted(codes):
            name = "c%d.lua" % code
            found = None
            for sub in ("official", "unofficial", "pre-release", "goat"):
                p = os.path.join(args.scripts, sub, name)
                if os.path.isfile(p):
                    found = p
                    break
            if found:
                shutil.copy2(found, os.path.join(script_out, "official", name))
            else:
                missing_scripts.append(code)
        out_path = os.path.join(DATA, "cards.cdb")
        if os.path.exists(out_path):
            os.remove(out_path)
        dst = sqlite3.connect(out_path)
        for (sql,) in src.execute("SELECT sql FROM sqlite_master WHERE type='table'"):
            dst.execute(sql)
        q = ",".join("?" * len(codes))
        for table in ("datas", "texts"):
            rows = src.execute("SELECT * FROM %s WHERE id IN (%s)" % (table, q), list(codes)).fetchall()
            if rows:
                dst.executemany("INSERT INTO %s VALUES (%s)" % (table, ",".join("?" * len(rows[0]))), rows)
        dst.commit()
        n = dst.execute("SELECT COUNT(*) FROM datas").fetchone()[0]
        dst.execute("VACUUM")
        dst.close()
        print("Cartas en cards.cdb: %d" % n)
        print("Sin script (normales u otras): %s" % missing_scripts)

    if args.strings:
        shutil.copy2(os.path.join(args.strings, "strings.conf"), os.path.join(DATA, "strings.conf"))
        es = os.path.join(args.strings, "languages", "Español", "strings.conf")
        if os.path.isfile(es):
            shutil.copy2(es, os.path.join(DATA, "strings_es.conf"))


if __name__ == "__main__":
    main()
