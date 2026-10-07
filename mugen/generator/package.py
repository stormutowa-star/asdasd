"""Normaliza los archivos de texto (CRLF, ASCII en cns/cmd/def) y crea el ZIP
listo para copiar en mugen/chars/."""
import os
import unicodedata
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
CH = os.path.normpath(os.path.join(HERE, '..', 'HornedBlade'))
ZIP = os.path.normpath(os.path.join(HERE, '..', 'HornedBlade_MUGEN_JUS.zip'))


def ascii_fold(s):
    return unicodedata.normalize('NFKD', s).encode('ascii', 'ignore').decode('ascii')


def main():
    for fn in sorted(os.listdir(CH)):
        p = os.path.join(CH, fn)
        ext = fn.rsplit('.', 1)[-1].lower()
        if ext in ('cns', 'cmd', 'def', 'air', 'txt'):
            raw = open(p, 'rb').read()
            try:
                txt = raw.decode('utf-8')
            except UnicodeDecodeError:
                txt = raw.decode('cp1252')
            txt = txt.replace('\r\n', '\n')
            if ext != 'txt':
                txt = ascii_fold(txt)
                data = txt.replace('\n', '\r\n').encode('ascii')
            else:
                data = txt.replace('\n', '\r\n').encode('cp1252', 'replace')
            open(p, 'wb').write(data)
    with zipfile.ZipFile(ZIP, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as z:
        for fn in sorted(os.listdir(CH)):
            z.write(os.path.join(CH, fn), 'HornedBlade/' + fn)
    print(ZIP, os.path.getsize(ZIP) // 1024, 'KB')


if __name__ == '__main__':
    main()
