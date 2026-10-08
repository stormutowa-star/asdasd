# YGO Duel — Godot 4 + GDScript + ocgcore

Prototipo jugable de un simulador de duelos de Yu-Gi-Oh! al estilo **EDOPro**, hecho con
**Godot 4.4** y **GDScript**, que usa el motor de reglas real de EDOPro:
[**ocgcore**](https://github.com/edo9300/ygopro-core) (con sus scripts Lua de
[ProjectIgnis/CardScripts](https://github.com/ProjectIgnis/CardScripts)).

- Duelo completo contra la CPU con reglas Master Rule 5 (todas las resuelve ocgcore).
- Tablero estilo EDOPro: zonas de monstruos y M/T, Zonas Extra, Campo, Cementerio, Desterradas, Deck y Extra Deck.
- Panel de información de la carta, registro del duelo, cadena actual, LP y fases.
- Acciones con clic: Invocar, Colocar, Activar, Invocación Especial, cambiar posición, atacar…
- Diálogos para todas las peticiones del core: elegir cartas/objetivos, sacrificios, cadenas, sí/no,
  opciones, posición, zonas, contadores, declarar carta/tipo/atributo/número, piedra-papel-tijera.
- IA rival sencilla en GDScript (invoca, activa magias, coloca trampas, encadena y ataca con cabeza).
- Mazos `.ydk` de EDOPro y bases de datos `.cdb` de ProjectIgnis.
- Imágenes de cartas descargadas al vuelo (como EDOPro) y guardadas en caché; si no hay red se dibuja
  una carta genérica con nombre, nivel y ATK/DEF.

## Jugar

**Opción rápida (GitHub Actions):** pestaña *Actions* → *YGO Duel (Godot + ocgcore)* → la última
ejecución → descarga el artefacto `ygo-duel-windows` (o `ygo-duel-linux`), descomprímelo y ejecuta
`YGODuel.exe`.

**Desde el editor:** abre la carpeta `game/` con **Godot 4.4 o superior**. La extensión ya viene
compilada para Windows y Linux en `game/bin/`. Pulsa F5.

### Controles

| Acción | Cómo |
|---|---|
| Ver una carta | Pasa el ratón por encima (panel izquierdo) |
| Acciones de una carta | Clic en ella (las que tienen acciones brillan en azul) |
| Cementerio / Desterradas / Extra | Clic en el montón |
| Cambiar de fase | Botones *Batalla*, *Main 2* y *Fin de turno* |
| Atacar | En la Fase de Batalla, clic en tu monstruo → *Atacar* |

Opciones en el panel derecho: colocar en zona automáticamente, no preguntar por cadenas y animaciones rápidas.

## Estructura

```
ygo-duel-godot/
├── SConstruct                 # compila la GDExtension (godot-cpp + ocgcore + Lua + SQLite)
├── extension/src/             # C++: envoltorio fino de la API de ocgcore para Godot
│   ├── ygo_duel.*             #   clase YGODuel (crear duelo, process, mensajes, respuestas, consultas)
│   └── ygo_card_database.*    #   clase YGOCardDatabase (lee .cdb con SQLite)
├── thirdparty/
│   ├── godot-cpp/             # submódulo (rama 4.4)
│   ├── ygopro-core/           # submódulo: ocgcore de EDOPro (+ Lua 5.4)
│   └── sqlite/                # se descarga con tools/get_sqlite.py
├── tools/
│   ├── setup.sh / setup.ps1   # submódulos + SQLite + scons
│   ├── get_sqlite.py
│   └── build_data.py          # genera game/data a partir de CardScripts y BabelCDB
└── game/                      # proyecto de Godot
    ├── bin/                   # .gdextension + bibliotecas compiladas
    ├── data/                  # cards.cdb, scripts Lua, strings.conf (es/en)
    ├── decks/                 # Yugi.ydk, Kaiba.ydk
    ├── scenes/                # main_menu.tscn, duel.tscn
    ├── scripts/
    │   ├── ocg.gd             #   constantes del core y textos
    │   ├── message_parser.gd  #   mensajes binarios del core → Dictionary
    │   ├── response.gd        #   construcción de respuestas binarias
    │   ├── duel_controller.gd #   bucle del duelo, estado del campo, registro
    │   ├── ai_player.gd       #   IA rival
    │   ├── duel_board.gd      #   interfaz del tablero
    │   ├── prompt_dialog.gd   #   diálogos de selección
    │   ├── card_view.gd       #   dibujo de una carta
    │   ├── card_db.gd         #   autoload: base de datos, strings y mazos
    │   └── card_images.gd     #   autoload: descarga/caché de imágenes
    └── tests/                 # pruebas IA vs IA (sin interfaz) y de la interfaz
```

### Cómo funciona

1. `YGODuel.create_duel()` crea el duelo en ocgcore; el core pide datos de cartas (se leen del `.cdb`)
   y scripts Lua (se buscan en `data/script/`) mediante callbacks.
2. `DuelController` llama a `process()`, recoge los mensajes binarios y los traduce con
   `OCGMessageParser`.
3. Cuando un mensaje pide una decisión (`MSG_SELECT_*`), responde la IA o la interfaz, y la respuesta
   se envía con `set_response()` (formato en `response.gd`).
4. Tras cada lote de mensajes se consulta el estado completo del campo (`query_location`) para
   redibujar el tablero, así la interfaz nunca se desincroniza del core.

## Compilar la extensión

Requisitos: Python 3, SCons, un compilador C++17 (GCC/Clang, MSVC o MinGW) y git.

```bash
cd ygo-duel-godot
./tools/setup.sh                 # o tools\setup.ps1 en Windows
scons platform=linux target=template_debug
scons platform=linux target=template_release
# Windows: scons platform=windows target=template_debug   (MSVC)
#          scons platform=windows use_mingw=yes target=template_debug   (MinGW / desde Linux)
```

Las bibliotecas se generan en `game/bin/`.

## Añadir más cartas

El juego incluye sólo los datos de las cartas de los mazos de ejemplo. Para usar el pool completo:

```bash
git clone --depth 1 https://github.com/ProjectIgnis/CardScripts
git clone --depth 1 https://github.com/ProjectIgnis/BabelCDB
python3 tools/build_data.py --scripts CardScripts --cdb BabelCDB/cards.cdb --all
```

O sin tocar el proyecto: copia `cards.cdb` y la carpeta `script/` dentro de `user://expansions/`
(en Windows `%APPDATA%\Godot\app_userdata\YGO Duel (ocgcore)\expansions\`). Los mazos `.ydk` creados
con EDOPro pueden ponerse en `game/decks/` o en `user://decks/`.

## Pruebas

```bash
godot --headless --path game --import
godot --headless --path game res://tests/ai_vs_ai.tscn -- 6 2024      # 6 duelos IA vs IA
xvfb-run godot --path game res://tests/ui_smoke.tscn -- /tmp/capturas # interfaz + capturas
```

## Limitaciones del prototipo

- La IA es heurística (no es WindBot): no evalúa combos ni efectos complejos.
- Sin modo en línea, sin editor de mazos, sin animaciones elaboradas ni sonido.
- Ordenar cartas (`SORT_CARD`) se deja en el orden por defecto.

## Licencias

ocgcore y los scripts de cartas de ProjectIgnis se distribuyen bajo **AGPL-3.0**; como la extensión
enlaza con ocgcore, este proyecto debe distribuirse también bajo AGPL-3.0. SQLite es de dominio público.
Yu-Gi-Oh! es una marca de Konami; este es un proyecto de fans sin ánimo de lucro.
