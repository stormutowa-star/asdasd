"""Generates the Makuo saga quest files (DragonMineZ 2.1.3 quest schema) and the addon's language files.

Usage: python3 gen_story.py <src/main/resources dir>
"""
import json, os, sys

RES = sys.argv[1]
STORY = os.path.join(RES, 'makuosaga_story')
LANG = os.path.join(RES, 'assets/makuosaga/lang')
SAGA = 'makuo_saga'
FOLDER = 'saga_makuo'
NAMEK, VHAROS = 'dragonminez:namek', 'makuosaga:vharos'
FORTRESS = (0, 101, -300)


def kill(entity, count, hp, melee, ki, **extra):
    o = {'type': 'KILL', 'entity': 'makuosaga:' + entity, 'count': count, 'health': hp, 'meleeDamage': melee,
         'kiDamage': ki, 'spawn': 'QUEST', 'count_mode': 'QUEST_SPAWNED_ONLY'}
    o.update(extra)
    return o


def item(item_id, count):
    return {'type': 'ITEM', 'item': item_id, 'count': count}


def talk(npc):
    return {'type': 'TALK_TO', 'npcId': npc}


def dim(d):
    return {'type': 'DIMENSION', 'dimension': d}


def coords(x, y, z, r):
    return {'type': 'COORDS', 'x': x, 'y': y, 'z': z, 'radius': r}


def tps(n):
    return {'type': 'TPS', 'amount': n}


def give(item_id, n):
    return {'type': 'ITEM', 'item': item_id, 'count': n}


def req(level, dimension=None):
    conds = [{'type': 'LEVEL', 'minLevel': level}]
    if dimension:
        conds.append({'type': 'DIMENSION', 'dimension': dimension})
    return {'operator': 'AND', 'conditions': conds}


# (id, slug, level, dimension requirement, objectives, rewards, es title, es desc, en title, en desc)
Q = [
    (1, 'eclipse_violeta', 2340, None, [dim(NAMEK)], [tps(120000)],
     'Eclipse Violeta',
     'Una sombra violeta cubre el sol de Namek. Los namekianos dicen que el cielo «llora cristal»: algo cae del espacio sobre sus aldeas. Viaja a Namek.',
     'Violet Eclipse',
     'A violet shadow covers Namek\'s sun. The Namekians say the sky is "weeping crystal": something is falling from space onto their villages. Travel to Namek.'),
    (2, 'palabras_del_patriarca', 2340, NAMEK, [talk('guru')], [tps(150000)],
     'Palabras del Patriarca',
     'El Gran Patriarca ha sentido el eclipse antes que nadie. Habla con él: «Ese color… lo vi en los recuerdos de mis antepasados. Hubo un namekiano al que borramos de la historia. Se llamaba Makuo».',
     'Words of the Grand Elder',
     'The Grand Elder felt the eclipse before anyone else. Talk to him: "That color... I saw it in my ancestors\' memories. There was a Namekian we erased from history. His name was Makuo."'),
    (3, 'huevos_de_cristal', 2350, NAMEK, [kill('saga_zhar', 12, 52000, 2300, 2100)], [tps(230000)],
     'Huevos de cristal',
     'Los huevos violetas se abren y de ellos salen guerreros de armadura negra: los Zhar, hijos de Makuo. Buscan las Esferas del Dragón de Namek. Derrota a 12 Guerreros Zhar.',
     'Crystal Eggs',
     'The violet eggs hatch into black-armored warriors: the Zhar, children of Makuo. They are hunting Namek\'s Dragon Balls. Defeat 12 Zhar Warriors.'),
    (4, 'el_puno_de_cristal', 2370, NAMEK, [kill('saga_garuk', 1, 820000, 33600, 29000)],
     [tps(300000), give('dragonminez:senzu_bean', 3)],
     'El Puño de Cristal',
     'Garuk, el Primer Heraldo, aterriza entre los restos de los Zhar: «El Padre no quiere a Namek. Solo quiere lo que le quitasteis. Apártate y vivirás». Derrótalo.',
     'The Crystal Fist',
     'Garuk, the First Herald, lands among the fallen Zhar: "Father does not want Namek. Only what you took from him. Step aside and live." Defeat him.'),
    (5, 'preparar_el_salto', 2370, None,
     [item('minecraft:amethyst_shard', 16), item('minecraft:iron_ingot', 12), item('minecraft:glowstone_dust', 8)],
     [tps(180000)],
     'Preparar el salto',
     'Vharos está en el borde de la galaxia y el eclipse distorsiona el espacio. La nave necesita 16 fragmentos de amatista para estabilizar el salto, 12 lingotes de hierro para el casco y 8 de polvo de piedra luminosa para orientarse. Al terminar, la nave podrá viajar a Vharos.',
     'Preparing the Jump',
     'Vharos lies at the edge of the galaxy and the eclipse warps space. The space pod needs 16 amethyst shards to stabilize the jump, 12 iron ingots for the hull and 8 glowstone dust to navigate. Once done, the pod can travel to Vharos.'),
    (6, 'rumbo_a_vharos', 2370, None, [dim(VHAROS)], [tps(150000)],
     'Rumbo a Vharos',
     'Sube a la nave espacial y elige el destino nuevo: Vharos. Un planeta de hierba violeta y mares de amatista, sin sol.',
     'Course for Vharos',
     'Board the space pod and choose the new destination: Vharos. A planet of violet grass and amethyst seas, with no sun.'),
    (7, 'el_ultimo_guardian', 2380, VHAROS, [talk('saien')], [tps(150000)],
     'El último Guardián',
     'Entre las ruinas del campamento namekiano, cerca de donde aterrizaste, te espera un anciano: Saien, el último Guardián de la Cadena. Habla con él.',
     'The Last Guardian',
     'Among the ruins of the Namekian camp, near your landing site, an old man waits for you: Saien, the last Guardian of the Chain. Talk to him.'),
    (8, 'ceniza_y_cristal', 2400, VHAROS, [kill('saga_zhar', 16, 60000, 2600, 2400)], [tps(280000)],
     'Ceniza y cristal',
     'Los Zhar han olido tu ki y rodean el campamento de Saien. Derrota a 16 Guerreros Zhar.',
     'Ash and Crystal',
     'The Zhar have sensed your ki and surround Saien\'s camp. Defeat 16 Zhar Warriors.'),
    (9, 'la_voz_del_vacio', 2420, VHAROS, [kill('saga_seiryn', 1, 900000, 34000, 38000)], [tps(340000)],
     'La Voz del Vacío',
     'Seiryn, el Segundo Heraldo, flota sobre el mar de amatista: «Tu ki es tan… brillante. El Padre lo disfrutará». Es un hechicero: no le dejes atacar de lejos.',
     'The Voice of the Void',
     'Seiryn, the Second Herald, floats above the amethyst sea: "Your ki is so... bright. Father will enjoy it." He is a sorcerer: don\'t let him fight from afar.'),
    (10, 'fragmentos_del_vacio', 2440, VHAROS,
     [kill('saga_zhar_elite', 6, 160000, 7000, 6500), item('makuosaga:void_shard', 4)],
     [tps(320000), give('dragonminez:senzu_bean', 3)],
     'Fragmentos del Vacío',
     'Saien sabe cómo debilitar las alas de Makuo: un sello hecho con su propio cristal. Los Zhar de Élite llevan fragmentos del Corazón del Vacío incrustados. Derrota a 6 y reúne 4 Fragmentos del Vacío.',
     'Void Shards',
     'Saien knows how to weaken Makuo\'s wings: a seal made from his own crystal. Zhar Elites carry shards of the Void Heart. Defeat 6 of them and gather 4 Void Shards.'),
    (11, 'la_sombra', 2460, VHAROS, [kill('saga_vokkar', 1, 980000, 41000, 36000)], [tps(380000)],
     'La Sombra',
     'Algo te sigue sin hacer ruido: Vokkar, el Tercer Heraldo. «Garuk era fuerza. Seiryn era poder. Yo soy lo que no ves venir». Derrótalo.',
     'The Shadow',
     'Something follows you without a sound: Vokkar, the Third Herald. "Garuk was strength. Seiryn was power. I am what you never see coming." Defeat him.'),
    (12, 'las_puertas_de_la_ciudadela', 2470, VHAROS, [coords(*FORTRESS, 40)], [tps(200000)],
     'Las puertas de la Ciudadela',
     'Saien graba el sello en tus manos: «Cuando despliegue las alas, el sello las hará visibles para tu ki». Ve al norte, unos 300 bloques desde el campamento (X 0, Z -300): allí se alza la Ciudadela de Makuo.',
     'The Citadel Gates',
     'Saien engraves the seal on your hands: "When he spreads his wings, the seal will make them visible to your ki." Head north, about 300 blocks from the camp (X 0, Z -300): Makuo\'s Citadel stands there.'),
    (13, 'drakhul_el_coloso', 2500, VHAROS,
     [kill('saga_drakhul', 1, 1100000, 45000, 40000, TransformHealth=1600000, TransformMeleeDamage=56000,
           TransformKiDamage=50000, TransformTriggerPercent=0.4)],
     [tps(450000), give('dragonminez:senzu_bean', 3)],
     'Drakhul, el Coloso',
     'Ante el trono vacío te espera Drakhul, el primer hijo de Makuo: «Mis hermanos eran Heraldos. Yo soy su escudo. No pasarás». Cuidado: los namekianos pueden volverse gigantes.',
     'Drakhul, the Colossus',
     'Before the empty throne stands Drakhul, Makuo\'s firstborn: "My brothers were Heralds. I am their shield. You shall not pass." Careful: Namekians can grow giant.'),
    (14, 'alas_del_vacio', 2540, VHAROS,
     [kill('saga_makuo', 1, 1350000, 52000, 52000, TransformHealth=2100000, TransformMeleeDamage=66000,
           TransformKiDamage=68000, TransformTriggerPercent=0.5)],
     [tps(650000)],
     'Alas del Vacío',
     'Makuo baja del Corazón del Vacío sin prisa: «Seiscientos años mirando las estrellas… y la primera luz que viene a verme eres tú». Cuando lo dejes a la mitad, desplegará las Alas del Vacío. Derrótalo.',
     'Wings of the Void',
     'Makuo descends from the Void Heart, unhurried: "Six hundred years watching the stars... and the first light that comes to see me is you." At half health he will spread the Wings of the Void. Defeat him.'),
    (15, 'amanecer', 2540, None, [dim(NAMEK), talk('guru')],
     [tps(300000), give('dragonminez:senzu_bean', 5)],
     'Amanecer',
     'El Corazón del Vacío se apaga y, por primera vez en seiscientos años, sale un sol pálido sobre Vharos. Saien se queda: «Alguien tiene que contarles a las estrellas que la cadena ya no hace falta». Vuelve a Namek y habla con el Gran Patriarca.',
     'Dawn',
     'The Void Heart goes dark and, for the first time in six hundred years, a pale sun rises over Vharos. Saien stays behind: "Someone has to tell the stars the chain is no longer needed." Return to Namek and talk to the Grand Elder.'),
]

os.makedirs(os.path.join(STORY, 'sagas'), exist_ok=True)
os.makedirs(os.path.join(STORY, 'quests', FOLDER), exist_ok=True)
index = []

saga = {'id': SAGA, 'name': 'dmz.saga.' + SAGA, 'requirements': {'previousSaga': 'buu_saga'}, 'questFolder': FOLDER}
with open(os.path.join(STORY, 'sagas', SAGA + '.json'), 'w') as f:
    json.dump(saga, f, indent=2, ensure_ascii=False)
index.append('sagas/' + SAGA + '.json')

es, en = {'dmz.saga.' + SAGA: 'Saga de Makuo'}, {'dmz.saga.' + SAGA: 'Makuo Saga'}
for qid, slug, level, dimension, objectives, rewards, es_t, es_d, en_t, en_d in Q:
    key = 'dmz.quest.makuo%d' % qid
    quest = {
        'id': qid, 'title': key + '.name', 'description': key + '.desc', 'type': 'SAGA', 'category': FOLDER,
        'parallel_objectives': False, 'party_scaling': True, 'secret': False, 'claim_mode': 'TREE_OR_NPC',
        'quest_giver': None, 'turn_in': None,
    }
    if qid > 1:
        quest['prerequisites'] = {'operator': 'AND',
                                  'conditions': [{'type': 'SAGA_QUEST', 'sagaId': SAGA, 'questId': qid - 1}]}
    quest['requirements'] = req(level, dimension)
    quest['objectives'] = objectives
    quest['rewards'] = rewards
    name = '%02d_%s.json' % (qid, slug)
    with open(os.path.join(STORY, 'quests', FOLDER, name), 'w') as f:
        json.dump(quest, f, indent=2, ensure_ascii=False)
    index.append('quests/%s/%s' % (FOLDER, name))
    es[key + '.name'], es[key + '.desc'] = es_t, es_d
    en[key + '.name'], en[key + '.desc'] = en_t, en_d

with open(os.path.join(STORY, 'index.txt'), 'w') as f:
    f.write('\n'.join(index) + '\n')

ENTITIES = {
    'saga_makuo': ('Makuo, el Exiliado', 'Makuo, the Exile'),
    'saga_makuo_wings': ('Makuo — Alas del Vacío', 'Makuo — Wings of the Void'),
    'saga_drakhul': ('Drakhul, el Coloso', 'Drakhul, the Colossus'),
    'saga_drakhul_giant': ('Drakhul Gigante', 'Giant Drakhul'),
    'saga_garuk': ('Garuk, el Puño de Cristal', 'Garuk, the Crystal Fist'),
    'saga_seiryn': ('Seiryn, la Voz del Vacío', 'Seiryn, the Voice of the Void'),
    'saga_vokkar': ('Vokkar, la Sombra', 'Vokkar, the Shadow'),
    'saga_zhar': ('Guerrero Zhar', 'Zhar Warrior'),
    'saga_zhar_elite': ('Zhar de Élite', 'Zhar Elite'),
}
for ent, (e_s, e_n) in ENTITIES.items():
    es['entity.makuosaga.' + ent], en['entity.makuosaga.' + ent] = e_s, e_n

extra_es = {
    'item.makuosaga.void_shard': 'Fragmento del Vacío',
    'gui.makuosaga.spacepod.vharos': 'Vharos',
    'entity.dragonminez.questnpc.saien': 'Saien, el último Guardián',
    'dialogue.dragonminez.story.sidequest.saien.idle': 'Seiscientos años vigilando una cadena… Si has venido a romperla del todo, no pierdas el tiempo conmigo. Ve al norte cuando estés listo.',
    'dialogue.dragonminez.story.sidequest.saien.offer': 'Éramos doce. Makuo nos dejó vivir para que viéramos cómo se rompía la cadena. Los demás se convirtieron en cristal hace siglos.',
    'dialogue.dragonminez.story.sidequest.saien.in_progress': 'No lo derrotarás mientras sus Heraldos lo alimenten con luz. Primero ellos. Después, él.',
    'dialogue.dragonminez.story.sidequest.saien.complete': 'Has hecho lo que doce guardianes no pudimos en seiscientos años. Gracias.',
    'message.makuosaga.vharos.arrival': '§5Vharos§r — Sientes un ki antiguo y frío al norte. Entre las ruinas cercanas, alguien te observa.',
    'message.makuosaga.vharos.citadel': '§5La Ciudadela de Makuo§r se alza al norte (X 0, Z -300).',
}
extra_en = {
    'item.makuosaga.void_shard': 'Void Shard',
    'gui.makuosaga.spacepod.vharos': 'Vharos',
    'entity.dragonminez.questnpc.saien': 'Saien, the Last Guardian',
    'dialogue.dragonminez.story.sidequest.saien.idle': 'Six hundred years guarding a chain... If you came to break it for good, don\'t waste your time with me. Head north when you are ready.',
    'dialogue.dragonminez.story.sidequest.saien.offer': 'There were twelve of us. Makuo let us live so we could watch the chain break. The others turned to crystal centuries ago.',
    'dialogue.dragonminez.story.sidequest.saien.in_progress': 'You will not defeat him while his Heralds feed him light. Them first. Then him.',
    'dialogue.dragonminez.story.sidequest.saien.complete': 'You did what twelve guardians could not in six hundred years. Thank you.',
    'message.makuosaga.vharos.arrival': '§5Vharos§r — You sense an ancient, cold ki to the north. Someone is watching you from the nearby ruins.',
    'message.makuosaga.vharos.citadel': '§5Makuo\'s Citadel§r rises to the north (X 0, Z -300).',
}
es.update(extra_es)
en.update(extra_en)

os.makedirs(LANG, exist_ok=True)
for code in ('es_es', 'es_mx', 'es_ar', 'es_cl', 'es_ve', 'es_uy'):
    with open(os.path.join(LANG, code + '.json'), 'w') as f:
        json.dump(es, f, indent=2, ensure_ascii=False)
with open(os.path.join(LANG, 'en_us.json'), 'w') as f:
    json.dump(en, f, indent=2, ensure_ascii=False)
print('story: %d quests, %d lang keys' % (len(Q), len(es)))
