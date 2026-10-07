"""Verificador del personaje: comprueba que todo lo que referencian el
CNS/CMD/AIR existe (estados, animaciones, sprites, sonidos, comandos) y
que cada controlador tiene tipo y triggers. Uso: python3 lint.py"""
import os
import re
import sys
import mugenfmt as MF

HERE = os.path.dirname(os.path.abspath(__file__))
CH = os.path.normpath(os.path.join(HERE, '..', 'HornedBlade'))
N = 'HornedBlade'
errors, warns = [], []

# estados que aporta common1.cns
COMMON = {0, 10, 11, 12, 20, 40, 45, 47, 50, 52, 100, 105, 106, 120, 130, 131, 132, 140, 150, 151, 152,
          153, 154, 155, 5000, 5001, 5002, 5010, 5011, 5020, 5030, 5035, 5040, 5050, 5070, 5071, 5080,
          5081, 5100, 5101, 5110, 5120, 5150, 5200, 5201, 5210, 5500, 5900}
CUSTOM_P2 = {820, 821, 3290, 3291}      # estados donde el rival usa sus propias anims


def parse_sections(text):
    secs = []
    cur = None
    for ln, raw in enumerate(text.splitlines(), 1):
        line = raw.split(';', 1)[0].strip()
        if not line:
            continue
        m = re.match(r'^\[(.+?)\]$', line)
        if m:
            cur = dict(head=m.group(1).strip(), kv=[], line=ln)
            secs.append(cur)
            continue
        if cur is None:
            continue
        if '=' in line:
            k, v = line.split('=', 1)
            cur['kv'].append((k.strip().lower(), v.strip(), ln))
        else:
            cur['kv'].append((line.lower(), '', ln))
    return secs


def parse_air(text):
    acts = {}
    cur = None
    for ln, raw in enumerate(text.splitlines(), 1):
        line = raw.split(';', 1)[0].strip()
        if not line:
            continue
        m = re.match(r'^\[begin action\s+(-?\d+)\]$', line, re.I)
        if m:
            cur = int(m.group(1))
            if cur in acts:
                errors.append('AIR: accion %d duplicada' % cur)
            acts[cur] = []
            continue
        if cur is None:
            continue
        if re.match(r'^(clsn[12](default)?:|clsn[12]\[|loopstart)', line, re.I):
            m = re.match(r'^clsn([12])\[\d+\]\s*=\s*(.+)$', line, re.I)
            if m:
                v = [x.strip() for x in m.group(2).split(',')]
                if len(v) != 4 or not all(re.match(r'^-?\d+$', x) for x in v):
                    errors.append('AIR linea %d: caja mal formada' % ln)
            continue
        parts = [p.strip() for p in line.split(',')]
        if len(parts) < 5:
            errors.append('AIR linea %d: cuadro mal formado: %s' % (ln, line))
            continue
        acts[cur].append((int(parts[0]), int(parts[1]), int(parts[4]), ln))
    return acts


def main():
    sff = MF.read_sff(os.path.join(CH, N + '.sff'))
    sprites = {(s['group'], s['no']) for s in sff}
    for need in ((9000, 0), (9000, 1)):
        if need not in sprites:
            errors.append('SFF: falta el retrato %s' % (need,))
    snd = {(g, n) for g, n, _ in MF.read_snd(os.path.join(CH, N + '.snd'))}
    air = parse_air(open(os.path.join(CH, N + '.air'), encoding='latin-1').read())
    for a, frs in air.items():
        if not frs:
            errors.append('AIR: accion %d sin cuadros' % a)
        for g, n, t, ln in frs:
            if g >= 0 and (g, n) not in sprites:
                errors.append('AIR linea %d: sprite %d,%d no existe en el SFF' % (ln, g, n))
    for req in (0, 5, 6, 10, 11, 12, 20, 21, 40, 41, 42, 43, 47, 100, 105, 120, 121, 122, 130, 131, 132,
                140, 141, 142, 150, 151, 152, 5000, 5001, 5002, 5005, 5006, 5007, 5010, 5011, 5012, 5015,
                5016, 5017, 5020, 5021, 5022, 5025, 5026, 5027, 5030, 5035, 5040, 5050, 5060, 5070, 5080,
                5090, 5100, 5110, 5120, 5150, 5160, 5170, 5200, 5210, 5300):
        if req not in air:
            errors.append('AIR: falta la animacion estandar %d' % req)

    cmd_text = open(os.path.join(CH, N + '.cmd'), encoding='latin-1').read()
    cns_text = open(os.path.join(CH, N + '.cns'), encoding='latin-1').read()
    cmd = parse_sections(cmd_text)
    cns = parse_sections(cns_text)
    commands = {v.strip('"') for s in cmd if s['head'].lower() == 'command' for k, v, _ in s['kv'] if k == 'name'}
    for req in ('holdfwd', 'holdback', 'holdup', 'holddown', 'recovery', 'FF', 'BB'):
        if req not in commands:
            errors.append('CMD: falta el comando requerido %s' % req)

    statedefs = {}
    for src, secs in (('cns', cns), ('cmd', cmd)):
        for s in secs:
            m = re.match(r'^statedef\s+(-?\d+)', s['head'], re.I)
            if m:
                n = int(m.group(1))
                if n in statedefs:
                    errors.append('%s: Statedef %d duplicado' % (src, n))
                statedefs[n] = s
    mine = set(statedefs)

    def check_cmd_refs(v, ln, src):
        for c in re.findall(r'command\s*!?=\s*"([^"]+)"', v, re.I):
            if c not in commands:
                errors.append('%s linea %d: comando "%s" no definido' % (src, ln, c))

    cur_state = None
    for src, secs in (('cns', cns), ('cmd', cmd)):
        for s in secs:
            m = re.match(r'^statedef\s+(-?\d+)', s['head'], re.I)
            if m:
                cur_state = int(m.group(1))
                for k, v, ln in s['kv']:
                    if k == 'anim' and cur_state not in CUSTOM_P2:
                        if re.match(r'^\d+$', v) and int(v) not in air:
                            errors.append('%s linea %d: anim %s no existe' % (src, ln, v))
                continue
            if not s['head'].lower().startswith('state '):
                continue
            kv = {}
            trig = False
            for k, v, ln in s['kv']:
                kv.setdefault(k, (v, ln))
                if k.startswith('trigger'):
                    trig = True
                    check_cmd_refs(v, ln, src)
                    if v.count('(') != v.count(')'):
                        errors.append('%s linea %d: parentesis desbalanceados' % (src, ln))
            if 'type' not in kv:
                errors.append('%s linea %d: controlador sin type' % (src, s['line']))
                continue
            if not trig:
                errors.append('%s linea %d: controlador sin triggers' % (src, s['line']))
            if not any(k == 'trigger1' or k == 'triggerall' for k in kv):
                errors.append('%s linea %d: falta trigger1' % (src, s['line']))
            typ = kv['type'][0].lower()
            if typ in ('changestate', 'selfstate', 'targetstate'):
                v, ln = kv.get('value', ('', s['line']))
                nums = [int(x) for x in re.findall(r'(?<![\w(])(\d{2,4})(?![\w)])', v)]
                if re.match(r'^\d+$', v):
                    n = int(v)
                    if n not in mine and n not in COMMON:
                        errors.append('%s linea %d: estado %d no existe' % (src, ln, n))
                elif nums:
                    for n in nums:
                        if n >= 100 and n not in mine and n not in COMMON and n not in (600, 610, 620):
                            warns.append('%s linea %d: revisar estado %d en expresion "%s"' % (src, ln, n, v))
            if typ in ('changeanim', 'changeanim2') and cur_state not in CUSTOM_P2:
                v, ln = kv['value']
                if re.match(r'^\d+$', v) and int(v) not in air:
                    errors.append('%s linea %d: ChangeAnim %s no existe' % (src, ln, v))
            if typ == 'explod' or typ == 'projectile':
                for key in ('anim', 'projanim', 'projhitanim', 'projremanim', 'projcancelanim'):
                    if key in kv:
                        v, ln = kv[key]
                        for n in re.findall(r'\d{3,4}', v):
                            if int(n) not in air:
                                errors.append('%s linea %d: %s %s no existe' % (src, ln, key, n))
            if typ == 'playsnd':
                v, ln = kv['value']
                if not v.upper().startswith('F'):
                    g, n = [int(x) for x in v.split(',')]
                    if (g, n) not in snd:
                        errors.append('%s linea %d: sonido %d,%d no existe' % (src, ln, g, n))
            for key in ('hitsound', 'guardsound', 'sound'):
                if key in kv:
                    v, ln = kv[key]
                    if v.upper().startswith('S'):
                        g, n = [int(x) for x in v[1:].split(',')]
                        if (g, n) not in snd:
                            errors.append('%s linea %d: %s %s no existe' % (src, ln, key, v))
            for key in ('sparkno', 'guard.sparkno'):
                if key in kv:
                    v, ln = kv[key]
                    if v.upper().startswith('S') and int(v[1:]) not in air:
                        errors.append('%s linea %d: %s %s no existe' % (src, ln, key, v))
            if typ == 'hitdef' or typ == 'projectile':
                if 'attr' not in kv:
                    errors.append('%s linea %d: HitDef sin attr' % (src, s['line']))
                for key in ('p1stateno', 'p2stateno'):
                    if key in kv and int(kv[key][0]) not in mine:
                        errors.append('%s linea %d: %s no existe' % (src, kv[key][1], key))
    # anims que usan los estados comunes y deberian existir
    for a in (44, 45, 46, 106, 170, 175, 181, 190, 195, 5051, 5061, 5101):
        if a not in air:
            warns.append('AIR: (opcional) falta la animacion %d' % a)
    print('sprites:', len(sprites), 'acciones AIR:', len(air), 'sonidos:', len(snd),
          'estados propios:', len(mine), 'comandos:', len(commands))
    for w in warns:
        print('AVISO:', w)
    for e in errors:
        print('ERROR:', e)
    print('OK' if not errors else '%d errores' % len(errors))
    return 1 if errors else 0


if __name__ == '__main__':
    sys.exit(main())
