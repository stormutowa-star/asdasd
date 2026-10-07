"""Write LudoKresshSword.esp (Skyrim SE/AE, ESL-flagged ESP).

Record layouts follow xEdit's wbDefinitionsTES5.pas; vanilla FormIDs come from
Mutagen.Bethesda.FormKeys (generated from Skyrim.esm).
"""
import struct
import sys

OUT = sys.argv[1]
MESH = 'Weapons\\LudoKressh\\LudoKresshSword.nif'
FORM_VERSION = 44          # Skyrim SE form version
OWN = 0x01000000           # our records: master count (1) in the high byte

# ---------------------------------------------------------------- vanilla Skyrim.esm forms
WeapTypeSword = 0x0001E711
WeapMaterialEbony = 0x0001E71E
VendorItemWeapon = 0x0008F958
MagicDisallowEnchanting = 0x000C27BD
EitherHand = 0x00013F44
WPNBashBladeImpactSet = 0x000183FF
MaterialBlockBlade1Hand = 0x000774C2
WPNzBlade1HandImpactSet = 0x00013CAC
ITMGenericWeaponUpSD = 0x0003C7BE
ITMGenericWeaponDownSD = 0x0003C7C0
WPNSwingBladeMediumSD = 0x0003C730
WPNBlade1HandDrawSD = 0x0003C72E
WPNBlade1HandSheatheSD = 0x0003C72F
EnchGreenFXShader = 0x0005D606
ChaurusPoisonFXShader = 0x0010CC64
CraftingSmithingForge = 0x00088105
CraftingSmithingSharpeningWheel = 0x00088108
IngotEbony = 0x0005AD9D
DaedraHeart = 0x0003AD5B
deathBell = 0x000516C8
LeatherStrips = 0x000800E4

AV_HEALTH, AV_STAMINA, AV_POISON_RESIST, AV_ONE_HANDED, AV_NONE = 24, 26, 40, 6, -1

# ---------------------------------------------------------------- our forms (ESL range)
MGEF_POISON = OWN | 0x800
MGEF_WEAKEN = OWN | 0x801
ENCH_POISON = OWN | 0x802
STAT_1ST = OWN | 0x803
WEAP_SWORD = OWN | 0x804
COBJ_FORGE = OWN | 0x805
COBJ_TEMPER = OWN | 0x806
NEXT_ID = 0x807


def zs(text):
    return text.encode('cp1252') + b'\0'


def sub(sig, data):
    assert len(data) < 0x10000, sig
    return sig.encode() + struct.pack('<H', len(data)) + data


def record(sig, formid, subs, flags=0):
    data = b''.join(subs)
    return sig.encode() + struct.pack('<IIIIHH', len(data), flags, formid, 0, FORM_VERSION, 0) + data


def group(sig, records):
    data = b''.join(records)
    return b'GRUP' + struct.pack('<I4sIHHHH', 24 + len(data), sig.encode(), 0, 0, 0, 0, 0) + data


def fid(x):
    return struct.pack('<I', x)


def obnd(x1, y1, z1, x2, y2, z2):
    return sub('OBND', struct.pack('<6h', x1, y1, z1, x2, y2, z2))


def keywords(kws):
    return [sub('KSIZ', struct.pack('<I', len(kws))), sub('KWDA', b''.join(fid(k) for k in kws))]


def mgef_data(*, flags, base_cost, resist, hit_shader, enchant_shader, actor_value,
              casting_type=1, delivery=1, skill=AV_NONE, sound_level=1):
    d = struct.pack('<I f I i i H H I f I I I I f f f f I i I I I I i I I I f I f I I I I I I I f f',
                    flags, base_cost, 0, skill, resist, 0, 0,
                    0, 0.0, hit_shader, enchant_shader, 0,
                    0, 0.0,                 # spellmaking area / casting time
                    0.0, 0.0, 0.0,          # taper curve / duration, second AV weight
                    0,                      # archetype: Value Modifier
                    actor_value,
                    0, 0,                   # projectile, explosion
                    casting_type, delivery,
                    AV_NONE,                # second actor value
                    0, 0, 0,                # casting art, hit effect art, impact data
                    0.0,                    # skill usage mult
                    0, 1.0,                 # dual casting art, scale
                    0, 0, 0, 0, 0, 0,       # enchant art, hit/enchant visuals, equip ability, IMAD, perk
                    sound_level,
                    0.0, 0.0)               # script effect AI score / delay
    assert len(d) == 152, len(d)
    return sub('DATA', d)


HOSTILE, DETRIMENTAL, FX_PERSIST = 0x1, 0x4, 0x1000

mgef_poison = record('MGEF', MGEF_POISON, [
    sub('EDID', zs('LK_PoisonDamageHealth')),
    sub('FULL', zs('Veneno de Ludo Kressh')),
    mgef_data(flags=HOSTILE | DETRIMENTAL | FX_PERSIST, base_cost=2.0, resist=AV_POISON_RESIST,
              hit_shader=ChaurusPoisonFXShader, enchant_shader=EnchGreenFXShader,
              actor_value=AV_HEALTH),
    sub('DNAM', zs('Envenena al objetivo: <mag> puntos de daño por segundo durante <dur> segundos.')),
])

mgef_weaken = record('MGEF', MGEF_WEAKEN, [
    sub('EDID', zs('LK_PoisonDamageStamina')),
    sub('FULL', zs('Toxina Sith')),
    mgef_data(flags=HOSTILE | DETRIMENTAL, base_cost=1.0, resist=AV_POISON_RESIST,
              hit_shader=0, enchant_shader=0, actor_value=AV_STAMINA),
    sub('DNAM', zs('La toxina drena <mag> puntos de aguante por segundo durante <dur> segundos.')),
])


def effect(mgef, magnitude, area, duration):
    return [sub('EFID', fid(mgef)), sub('EFIT', struct.pack('<fII', magnitude, area, duration))]


NO_AUTOCALC = 0x1
enit = struct.pack('<iIIiIIfII',
                   0,            # enchantment cost 0 -> hits never drain the charge
                   NO_AUTOCALC,
                   1,            # Fire and Forget
                   0,            # enchantment amount
                   1,            # Touch (contact)
                   6,            # Enchantment
                   0.0,          # charge time
                   0, 0)         # base enchantment, worn restrictions
assert len(enit) == 36
ench = record('ENCH', ENCH_POISON, [
    sub('EDID', zs('LK_EnchLudoKresshPoison')),
    obnd(0, 0, 0, 0, 0, 0),
    sub('FULL', zs('Veneno Sith')),
    sub('ENIT', enit),
    *effect(MGEF_POISON, 12.0, 0, 5),     # 12 poison damage / s for 5 s
    *effect(MGEF_WEAKEN, 10.0, 0, 5),     # 10 stamina / s for 5 s
])

BOUNDS = (-13, -15, -3, 13, 84, 3)
stat = record('STAT', STAT_1ST, [
    sub('EDID', zs('LK_1stPersonLudoKresshSword')),
    obnd(*BOUNDS),
    sub('MODL', zs(MESH)),
    sub('DNAM', struct.pack('<fIB3s', 90.0, 0, 0, b'\0\0\0')),
])

dnam = struct.pack('<B3sffHHf4sBBBBffIIfffffff4si8si4sf',
                   1, b'\0\0\0',      # OneHandSword
                   1.0, 1.0,          # speed, reach
                   0, 0,              # flags, unused
                   0.0, b'\0' * 4,    # sight FOV, unknown
                   0, 255, 1, 0,      # VATS, attack anim (default), projectiles, embedded AV
                   0.0, 0.0,          # range min / max
                   0, 0,              # on hit, flags2
                   1.0, 0.0,          # animation attack mult, fire rate
                   0.0, 0.0, 0.0,     # rumble
                   0.0, 0.0,          # override damage-to-weapon mult, attack shots/sec
                   b'\0' * 4,
                   AV_ONE_HANDED,
                   b'\0' * 8,
                   AV_NONE,           # resist
                   b'\0' * 4,
                   0.85)              # stagger
assert len(dnam) == 100, len(dnam)
crdt = struct.pack('<HHfB7sI4s', 12, 0, 1.0, 0, b'\0' * 7, 0, b'\0' * 4)
assert len(crdt) == 24

weap = record('WEAP', WEAP_SWORD, [
    sub('EDID', zs('LudoKresshSword')),
    obnd(*BOUNDS),
    sub('FULL', zs('Ludo Kressh Sword')),
    sub('MODL', zs(MESH)),
    sub('EITM', fid(ENCH_POISON)),
    sub('EAMT', struct.pack('<H', 3000)),
    sub('ETYP', fid(EitherHand)),
    sub('BIDS', fid(WPNBashBladeImpactSet)),
    sub('BAMT', fid(MaterialBlockBlade1Hand)),
    sub('YNAM', fid(ITMGenericWeaponUpSD)),
    sub('ZNAM', fid(ITMGenericWeaponDownSD)),
    *keywords([WeapTypeSword, WeapMaterialEbony, VendorItemWeapon, MagicDisallowEnchanting]),
    sub('DESC', zs('')),
    sub('INAM', fid(WPNzBlade1HandImpactSet)),
    sub('WNAM', fid(STAT_1ST)),
    sub('SNAM', fid(WPNSwingBladeMediumSD)),
    sub('NAM9', fid(WPNBlade1HandDrawSD)),
    sub('NAM8', fid(WPNBlade1HandSheatheSD)),
    sub('DATA', struct.pack('<IfH', 3500, 14.0, 24)),
    sub('DNAM', dnam),
    sub('CRDT', crdt),
    sub('VNAM', struct.pack('<I', 1)),     # detection sound level: normal
])


def cobj(formid, edid, items, bench):
    return record('COBJ', formid, [
        sub('EDID', zs(edid)),
        sub('COCT', struct.pack('<I', len(items))),
        *[sub('CNTO', struct.pack('<Ii', item, count)) for item, count in items],
        sub('CNAM', fid(WEAP_SWORD)),
        sub('BNAM', fid(bench)),
        sub('NAM1', struct.pack('<H', 1)),
    ])


forge = cobj(COBJ_FORGE, 'LK_RecipeLudoKresshSword',
             [(IngotEbony, 3), (DaedraHeart, 1), (deathBell, 2), (LeatherStrips, 2)],
             CraftingSmithingForge)
temper = cobj(COBJ_TEMPER, 'LK_TemperLudoKresshSword', [(IngotEbony, 1)],
              CraftingSmithingSharpeningWheel)

groups = [group('MGEF', [mgef_poison, mgef_weaken]),
          group('ENCH', [ench]),
          group('STAT', [stat]),
          group('WEAP', [weap]),
          group('COBJ', [forge, temper])]
num_records = 7 + len(groups)

ESL_FLAG = 0x200
tes4 = record('TES4', 0, [
    sub('HEDR', struct.pack('<fiI', 1.7, num_records, NEXT_ID)),
    sub('CNAM', zs('Claude Code')),
    sub('SNAM', zs('Ludo Kressh Sword - espada de un mano con ataques imbuidos en veneno.')),
    sub('MAST', zs('Skyrim.esm')),
    sub('DATA', struct.pack('<Q', 0)),
], flags=ESL_FLAG)

with open(OUT, 'wb') as f:
    f.write(tes4 + b''.join(groups))
print('wrote', OUT)
