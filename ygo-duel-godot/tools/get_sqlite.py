#!/usr/bin/env python3
"""Descarga la amalgamación de SQLite (dominio público) en thirdparty/sqlite/."""
import io
import os
import sys
import urllib.request
import zipfile

URL = "https://www.sqlite.org/2024/sqlite-amalgamation-3460100.zip"
DEST = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "thirdparty", "sqlite")


def main():
    if os.path.isfile(os.path.join(DEST, "sqlite3.c")):
        print("SQLite ya está en", DEST)
        return 0
    os.makedirs(DEST, exist_ok=True)
    print("Descargando", URL)
    data = urllib.request.urlopen(URL, timeout=120).read()
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        for name in z.namelist():
            base = os.path.basename(name)
            if base in ("sqlite3.c", "sqlite3.h"):
                with open(os.path.join(DEST, base), "wb") as f:
                    f.write(z.read(name))
    print("SQLite listo en", DEST)
    return 0


if __name__ == "__main__":
    sys.exit(main())
