# FRUTIGER AERO CITY — Plan de implementación

> **Para agentes:** SUB-SKILL REQUERIDA: usa superpowers:subagent-driven-development (recomendado) o superpowers:executing-plans para ejecutar este plan tarea por tarea. Si no tienes esas skills, ejecuta las tareas **en orden**, marcando cada paso (`- [ ]` → `- [x]`). Los pasos usan casillas para el seguimiento.

**Objetivo:** un mod de Minecraft 1.21.1 Forge que añade el tipo de mundo **FRUTIGER AERO CITY**: una ciudad infinita con estética Frutiger Aero/Y2K (cristal, agua, blanco, cian y verde lima), ciudadanos NPC y coches burbuja Y2K que se pueden conducir y que circulan solos por los carriles.

**Arquitectura:** toda la ciudad es una **función pura** `(semilla, x, z) → columna de bloques` en el paquete `core` (Java puro, sin Minecraft, probado con JUnit). Un `ChunkGenerator` propio se limita a traducir esa columna a `BlockState`. Los NPCs y coches consultan la misma función pura para saber dónde hay aceras y carriles. Un *world preset* en JSON registra el tipo de mundo y lo añade al botón "Tipo de mundo".

**Tech Stack:** Minecraft 1.21.1 · Forge 52.1.0 · ForgeGradle 7 · Gradle 9.3.0 · Java 21 · JUnit 5 · Python 3 (solo librería estándar, para generar texturas).

**Spec:** la sección 0 de este mismo documento (visión del usuario más las decisiones de diseño del arquitecto). Léela entera antes de la tarea 1.

---

## 0. Especificación

### 0.1 Lo que pidió el usuario
- Un nuevo **world type** llamado exactamente `FRUTIGER AERO CITY`.
- El mundo entero es una **ciudad muy grande**.
- **NPCs** que viven en la ciudad.
- **Vehículos Y2K**.
- Estética **Frutiger Aero / Y2K**: cielo azul brillante, cristal, agua, burbujas, blanco limpio, cian, verde lima, cromo y naturaleza integrada con la tecnología.

### 0.2 Decisiones de diseño (cerradas; no las cambies)
| Tema | Decisión |
|---|---|
| Ubicación | Proyecto Gradle **independiente** en `frutiger-aero-city/` dentro de este repo. **No toques** el mod Akagane de la raíz. |
| modid / paquete | `frutigeraero` / `com.ricardo.frutigeraero` |
| Mundo | Infinito, plano a `ground_y = 64`, cuadrícula de manzanas de **64×64** bloques (12 de calle + 52 de solar). |
| Distritos | `PLAZA` (manzana 0,0 con fuente-orbe), `DOWNTOWN` (torres de cristal de 72 a 200 bloques), `COMMERCIAL` (centros comerciales y torres pequeñas), `RESIDENTIAL` (condominios blancos con jardín en la azotea), `PARK` (lago, caminos y árboles redondos). Hay un "centro" cada 32 manzanas (2048 bloques). |
| Calles | Doble sentido con circulación por la derecha, aceras, línea central discontinua, pasos de cebra, farolas cromadas con *sea lantern* y monorraíl elevado decorativo cada 4 calles. |
| Bioma | Uno solo, `frutigeraero:aero_city`, con cielo, agua y hierba en colores Frutiger. Sin monstruos ni estructuras vanilla. |
| NPCs | `frutigeraero:citizen`: humanoide con modelo de jugador, 8 skins Y2K generadas por script, nombre, frases al hacer clic derecho, pasea por aceras. Aparecen al generarse cada chunk y no desaparecen. |
| Vehículos | `frutigeraero:aero_car`: coche burbuja de 2 plazas y 7 colores estilo iMac G3. Se conduce con WASD. Sin conductor, va en piloto automático por su carril y gira en los cruces. Aparecen al generarse el chunk. Se obtienen como ítem. |
| Texturas | Generadas por `tools/gen_textures.py` (Python con la librería estándar). No se dibuja nada a mano. |
| Idiomas | `en_us` y `es_es`. |

---

## Cómo ejecutar este plan (léelo, ejecutor)
1. **Ejecutor previsto: Haiku 5.5.** Implementa tú cada tarea. Si una tarea falla **dos veces** después de aplicar el Apéndice B, escala según el `CLAUDE.md` del repo y pásale al siguiente agente lo que ya probaste.
2. Trabaja siempre dentro de `frutiger-aero-city/` (salvo `.github/`). Todos los comandos `./gradlew` se lanzan desde esa carpeta.
3. **TDD:** escribe el test, comprueba que falla, implementa y comprueba que pasa. No te saltes la comprobación del fallo.
4. **No inventes APIs de Minecraft.** Las firmas verificadas están en el **Apéndice A**. Si algo no compila, consulta el Apéndice A y, si no está ahí, ejecuta el comando `javap` del Apéndice A. Nunca adivines.
5. El paquete `core` **no puede importar** nada de `net.minecraft` ni de `net.minecraftforge`. Si lo necesitas, el código va en otro paquete.
6. Al terminar cada tarea, haz commit. Al terminar cada fase (tareas 4, 6 y 8), haz push a la rama que te asigne el entorno.
7. Los valores numéricos de este plan (tamaños, probabilidades, alturas y colores) son **exactos**: los tests dependen de ellos.

---

## Global Constraints
- Minecraft `1.21.1`, Forge `52.1.0`, plugin `net.minecraftforge.gradle` versión `[7.0.3,8)`, Java `21`.
- Wrapper de Gradle **9.3.0** (ForgeGradle 7 exige Gradle ≥ 9.3.0; el wrapper 8.12.1 de la raíz **no compila**).
- Mappings oficiales de Mojang (nombres como `ResourceLocation.fromNamespaceAndPath`, `getMinBuildHeight`).
- modid `frutigeraero`; paquete raíz `com.ricardo.frutigeraero`; jar `frutigeraerocity-0.1.0.jar`.
- Clave de idioma del tipo de mundo: `generator.frutigeraero.frutiger_aero_city` = `FRUTIGER AERO CITY` (igual en ambos idiomas).
- `core/*` es Java puro (cero imports de Minecraft o Forge) y está cubierto por JUnit 5.
- Determinismo: misma semilla de mundo = misma ciudad. Nada en `core` puede depender del orden de consulta ni de estado mutable compartido.
- `ground_y` por defecto `64`; ningún bloque generado puede superar `ground_y + 247`.
- Sin dependencias en runtime aparte de Forge.
- No modificar nada fuera de `frutiger-aero-city/`, `.github/workflows/frutiger-aero-city.yml` y este plan.

## Review Focus
Riesgos que ningún test de tarea cubre de forma natural, cada uno con el test que lo fija:
1. **Coordenadas negativas:** usar `%` en vez de `Math.floorMod`/`Math.floorDiv` rompe la ciudad en x<0 o z<0. Lo fijan `streetKinds`, `laneHeadings` y `monorail` (Tarea 2) y `laneCoord` (Tarea 7).
2. **Costuras entre chunks:** árboles, farolas y edificios que cruzan el borde de un chunk tienen que encajar. Lo fija `orderIndependence` (Tarea 3): la columna solo depende de `(seed, x, z)`.
3. **Límite de altura del mundo:** torre más aguja más jardín ≤ `ground_y + 247` ≤ 319. Lo fija `heightLimit` (Tarea 3).
4. **Coches que chocan con farolas o pilares del monorraíl:** el hitbox de 1.8 bloques centrado en x = 4.0 u 8.0 tiene que caber. Lo fija `carLaneClearance` (Tarea 2).
5. **Huevo de ciudadano o coche usados en un mundo normal (que no es ciudad):** `CityChunkGenerator.layoutOf` devuelve `null`; el ciudadano pasea con el paseo vanilla y el coche desactiva el piloto automático **sin crashear**. Se comprueba en el checklist manual de la Tarea 9 (no se puede probar con JUnit).

---

## Mapa de archivos
```
.github/workflows/frutiger-aero-city.yml          CI: compila y ejecuta tests, sube el jar
frutiger-aero-city/
  build.gradle, settings.gradle, gradle.properties, gradlew, gradlew.bat, gradle/wrapper/*
  .gitignore
  README.md                                       uso del mod (Tarea 9)
  tools/gen_textures.py                           genera TODAS las PNG (Tareas 6 y 8)
  tools/smoke.sh                                  arranca un servidor real y comprueba la ciudad
  tools/smoke/expected.txt                        líneas SMOKE_OK_* obligatorias
  tools/smoke/datapack/...                        datapack de comprobación (load/summon/check)
  src/main/java/com/ricardo/frutigeraero/
    FrutigerAeroMod.java                          @Mod: registra los DeferredRegister
    core/                                         ── JAVA PURO, probado con JUnit ──
      Hash.java                                   hashing determinista y value noise
      CityBlock.java                              enum de bloques abstractos
      Heading.java                                NORTH/SOUTH/EAST/WEST (+yaw)
      StreetKind.java                             LOT/NS_STREET/EW_STREET/INTERSECTION
      ColumnPlan.java                             columna pintada (y → CityBlock)
      CityLayout.java                             API pública: calles, distritos, columnas
      District.java, BuildingStyle.java, Building.java
      LotPainter.java                             pinta solares: edificios, parques, plaza, árboles
      Spawns.java                                 dónde aparecen ciudadanos y coches en cada chunk
      PedestrianTargets.java                      destino de paseo para un NPC
      Autopilot.java                              decisiones de carril y giro de los coches
      CarColor.java                               7 colores Y2K (ARGB)
    world/
      ModWorldgen.java                            DeferredRegister del ChunkGenerator
      CityChunkGenerator.java                     ChunkGenerator → usa CityLayout
      BlockPalette.java                           CityBlock → BlockState
    entity/
      ModEntities.java                            EntityTypes + atributos
      CitizenEntity.java, WalkSidewalkGoal.java
      AeroCarEntity.java
    item/
      ModItems.java                               ítems + pestaña creativa
      AeroCarItem.java
    client/
      ClientSetup.java                            registro de renderers y capas
      CitizenRenderer.java
      AeroCarModel.java, AeroCarRenderer.java
  src/main/resources/
    META-INF/mods.toml, pack.mcmeta
    data/frutigeraero/worldgen/world_preset/frutiger_aero_city.json
    data/frutigeraero/worldgen/biome/aero_city.json
    data/minecraft/tags/worldgen/world_preset/normal.json
    data/minecraft/tags/worldgen/biome/without_patrol_spawns.json
    assets/frutigeraero/lang/en_us.json, es_es.json
    assets/frutigeraero/models/item/*.json
    assets/frutigeraero/textures/...              (generadas por script)
  src/test/java/com/ricardo/frutigeraero/core/*Test.java
```

---

# FASE 1 — Tipo de mundo y ciudad

### Task 1: Esqueleto del proyecto, CI y `Hash`

**Files:**
- Create: `frutiger-aero-city/{settings.gradle, gradle.properties, build.gradle, .gitignore, gradlew, gradlew.bat, gradle/wrapper/*}`
- Create: `frutiger-aero-city/src/main/resources/META-INF/mods.toml`, `frutiger-aero-city/src/main/resources/pack.mcmeta`
- Create: `frutiger-aero-city/src/main/java/com/ricardo/frutigeraero/FrutigerAeroMod.java`
- Create: `frutiger-aero-city/src/main/java/com/ricardo/frutigeraero/core/Hash.java`
- Test: `frutiger-aero-city/src/test/java/com/ricardo/frutigeraero/core/HashTest.java`
- Create: `.github/workflows/frutiger-aero-city.yml`

**Interfaces:**
- Produces: `Hash.mix(long) -> long`; `Hash.of(long seed, int a)`, `Hash.of(long seed, int a, int b)`, `Hash.of(long seed, int a, int b, int c)` y `Hash.of(long seed, int a, int b, int c, int d)`, todas `-> long`; `Hash.unit(long h) -> double` en `[0,1)`; `Hash.noise01(long seed, double x, double z) -> double` en `[0,1]`.
- Produces: `FrutigerAeroMod.MODID = "frutigeraero"`.

- [ ] **Step 1: Copiar el wrapper y los archivos de Gradle desde la raíz**
```bash
cd "$(git rev-parse --show-toplevel)"   # raíz del repo
mkdir -p frutiger-aero-city
cp -r gradlew gradlew.bat gradle gradle.properties frutiger-aero-city/
chmod +x frutiger-aero-city/gradlew
sed -i 's#gradle-8.12.1-bin.zip#gradle-9.3.0-bin.zip#' frutiger-aero-city/gradle/wrapper/gradle-wrapper.properties
grep distributionUrl frutiger-aero-city/gradle/wrapper/gradle-wrapper.properties
```
Esperado: `distributionUrl=https\://services.gradle.org/distributions/gradle-9.3.0-bin.zip`.

- [ ] **Step 2: `settings.gradle`, `build.gradle` y `.gitignore`**

`settings.gradle`: igual que el de la raíz, pero con `rootProject.name = 'frutigeraerocity'`.

`build.gradle`: copia exacta del de la raíz con estos cambios:
- `version = '0.1.0'`, `group = 'com.ricardo.frutigeraero'`, `base { archivesName = 'frutigeraerocity' }`.
- Añade a `dependencies`:
```groovy
    testImplementation platform('org.junit:junit-bom:5.11.4')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```
- Añade al final:
```groovy
tasks.named('test', Test) {
    useJUnitPlatform()
}
```
`.gitignore`: `build/`, `.gradle/`, `run/`, `out/`, `.idea/`, `*.iml`.

- [ ] **Step 3: `mods.toml` y `pack.mcmeta`**

`mods.toml`: misma estructura que `src/main/resources/META-INF/mods.toml` de la raíz, con `modId="frutigeraero"`, `version="0.1.0"`, `displayName="Frutiger Aero City"`, `authors="Ricardo"`, `description='''Nuevo tipo de mundo: una ciudad infinita Frutiger Aero / Y2K con ciudadanos y coches burbuja.'''`, y los dos bloques `[[dependencies.frutigeraero]]` (forge `[52,)`, minecraft `[1.21.1,1.22)`).

`pack.mcmeta`: igual que el de la raíz, con `"description": "Frutiger Aero City"` (pack_format 34, forge:server_data_pack_format 48).

- [ ] **Step 4: `FrutigerAeroMod` mínimo**

Sigue el patrón de `AkaganeMod`: clase `@Mod(FrutigerAeroMod.MODID)`, `public static final String MODID = "frutigeraero";` y un constructor `public FrutigerAeroMod(FMLJavaModLoadingContext context)` que obtiene `IEventBus modEventBus = context.getModEventBus();` (los registros se añaden en tareas posteriores).

- [ ] **Step 5: Escribir el test que falla**
```java
package com.ricardo.frutigeraero.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class HashTest {
    @Test void sameInputsSameOutput() {
        assertEquals(Hash.of(42L, 1, 2, 3), Hash.of(42L, 1, 2, 3));
    }
    @Test void differentInputsDiffer() {
        assertNotEquals(Hash.of(42L, 1, 2, 3), Hash.of(42L, 1, 2, 4));
        assertNotEquals(Hash.of(42L, 1, 2, 3), Hash.of(43L, 1, 2, 3));
        assertNotEquals(Hash.of(42L, 1, 2), Hash.of(42L, 2, 1));
    }
    @Test void unitInRangeAndUniform() {
        double sum = 0;
        for (int i = 0; i < 10000; i++) {
            double u = Hash.unit(Hash.of(7L, i));
            assertTrue(u >= 0.0 && u < 1.0);
            sum += u;
        }
        double mean = sum / 10000;
        assertTrue(mean > 0.45 && mean < 0.55, "mean=" + mean);
    }
    @Test void noiseInRangeAndSmooth() {
        for (int i = 0; i < 1000; i++) {
            double x = -50 + (i * 0.1037) % 100, z = -50 + (i * 0.0713) % 100;
            double n = Hash.noise01(99L, x, z);
            assertTrue(n >= 0.0 && n <= 1.0);
            assertTrue(Math.abs(n - Hash.noise01(99L, x + 0.01, z)) < 0.05);
        }
        assertEquals(Hash.noise01(5L, -3.5, -7.25), Hash.noise01(5L, -3.5, -7.25));
    }
}
```

- [ ] **Step 6: Ejecutar el test y comprobar que falla**

Run: `./gradlew test --console=plain`
Esperado: FAIL de compilación, `cannot find symbol: class Hash`. La primera ejecución descarga Gradle 9.3.0 y Minecraft (puede tardar 5–10 min; usa un timeout de 600000 ms).

- [ ] **Step 7: Implementar `Hash` en `core/Hash.java`**

Clase `final` con constructor privado.
- `mix`: finalizador SplitMix64: `z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L; z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL; return z ^ (z >>> 31);`
- `of(seed, a, …)`: `long h = mix(seed);` y para cada entero `h = mix(h + p * 0x9E3779B97F4A7C15L);` (cuatro sobrecargas explícitas, **sin varargs**, para no reservar memoria en cada llamada).
- `unit(h) = (h >>> 11) * 0x1.0p-53`.
- `noise01(seed, x, z)`: value noise bilineal. `x0 = floor(x)`, `z0 = floor(z)`, valores de esquina `unit(of(seed, xi, zi))` y suavizado `t*t*(3-2t)` en ambos ejes.

- [ ] **Step 8: Ejecutar los tests y la compilación completa**

Run: `./gradlew test --console=plain && ./gradlew build --console=plain`
Esperado: `BUILD SUCCESSFUL` y existe `build/libs/frutigeraerocity-0.1.0.jar`.

- [ ] **Step 9: Workflow de CI `.github/workflows/frutiger-aero-city.yml`**
```yaml
name: Frutiger Aero City
on:
  push:
    paths: ['frutiger-aero-city/**', '.github/workflows/frutiger-aero-city.yml']
  workflow_dispatch:
jobs:
  build:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: frutiger-aero-city
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '21' }
      - uses: gradle/actions/setup-gradle@v4
      - run: chmod +x gradlew && ./gradlew build --console=plain
      - uses: actions/upload-artifact@v4
        with:
          name: frutiger-aero-city-mod
          path: frutiger-aero-city/build/libs/*.jar
```

- [ ] **Step 10: Commit**
```bash
git add frutiger-aero-city .github/workflows/frutiger-aero-city.yml
git status --short   # comprueba que NO aparecen build/, .gradle/ ni run/
git commit -m "feat(frutiger): esqueleto del mod Frutiger Aero City, CI y Hash determinista"
```

---

### Task 2: Calles (`core`, Java puro)

**Files:**
- Create: `core/CityBlock.java`, `core/Heading.java`, `core/StreetKind.java`, `core/ColumnPlan.java`, `core/CityLayout.java`
- Test: `core/StreetLayoutTest.java`

**Interfaces:**
- Consumes: `Hash` (Tarea 1).
- Produces:
  - `enum CityBlock { AIR, STONE, DIRT, ASPHALT, ROAD_LINE, SIDEWALK, PLAZA_TILE, PATH, GRASS, WATER, POND_FLOOR, RIM, TRUNK, LEAVES, LAMP_POLE, LIGHT, GLASS_CYAN, GLASS_AQUA, GLASS_CLEAR, TRIM, WALL, FLOOR, ROOF, ACCENT_CYAN, ACCENT_LIME, ACCENT_SKY, SPIRE, BEAM, PILLAR }`. El bedrock **no** está aquí: lo pone el generador en `minY`.
  - `enum Heading { NORTH(0,-1,180f), SOUTH(0,1,0f), EAST(1,0,-90f), WEST(-1,0,90f) }` con `int dx()`, `int dz()`, `float yaw()`, `Heading right()` (N→E→S→W→N), `Heading left()`, `String id()` (nombre en minúsculas) y `static Heading byId(String)` (valor desconocido → `SOUTH`).
  - `enum StreetKind { LOT, NS_STREET, EW_STREET, INTERSECTION }`.
  - `final class ColumnPlan`: `public static final int BELOW = 2, MAX_ABOVE = 247;` · constructor de paquete `ColumnPlan(int groundY)` (todo `AIR` desde `groundY` hacia arriba y `STONE` en `groundY-2..groundY-1`) · `void set(int y, CityBlock b)` (ignora `y` fuera de `[groundY-2, groundY+MAX_ABOVE]`) · `void setIfAir(int y, CityBlock b)` · `public CityBlock blockAt(int y)` (`y < groundY-2` → `STONE`; por encima de lo pintado → `AIR`) · `public int topY()` (la y más alta que no es `AIR`) · `public int groundY()`. Internamente usa un array `CityBlock[BELOW + MAX_ABOVE + 1]`.
  - `final class CityLayout`:
    - `public static final int BLOCK = 64, STREET = 12, FLOOR = 4, MONORAIL = 24;`
    - `public CityLayout(long seed, int groundY)`, `long seed()`, `int groundY()`
    - `public static int cellOf(int c)` = `Math.floorDiv(c, 64)`; `public static int local(int c)` = `Math.floorMod(c, 64)`
    - `public StreetKind streetKind(int x, int z)`
    - `public Heading laneHeading(int x, int z)` (devuelve `null` si no es carril)
    - `public static double laneCenter(Heading h, int cell)` = `cell*64 + (h == NORTH || h == EAST ? 8.0 : 4.0)`
    - `public ColumnPlan column(int x, int z)`
    - `public boolean isPedestrianSurface(int x, int z)`

**Reglas de calles** (`lx = local(x)`, `lz = local(z)`):

| Condición | Resultado |
|---|---|
| `lx<12 && lz<12` | `INTERSECTION` |
| `lx<12` | `NS_STREET`, `across = lx`, `along = lz` |
| `lz<12` | `EW_STREET`, `across = lz`, `along = lx` |
| en otro caso | `LOT` |

Superficie en `y = groundY` (prioridad de arriba abajo):
1. `INTERSECTION`: si `lx ∈ {0,1,10,11}` **y** `lz ∈ {0,1,10,11}` → `SIDEWALK`; si no → `ASPHALT`.
2. Calle: `across ∈ {0,1,10,11}` → `SIDEWALK`.
3. Calle: `along ∈ {12,13,14,61,62,63}` → paso de cebra: `across` par → `ROAD_LINE`, impar → `ASPHALT`.
4. Calle: `across ∈ {5,6}` y `floorMod(along, 8) < 4` → `ROAD_LINE`.
5. En otro caso → `ASPHALT`.

Encima de la superficie:
- **Farola** (solo en calle): `across ∈ {1,10}` y `along % 16 == 8` → `LAMP_POLE` en `groundY+1..groundY+4` y `LIGHT` en `groundY+5`.
- **Monorraíl:** `nsLine = floorMod(cellOf(x),4)==0 && lx ∈ {5,6}` y `ewLine = floorMod(cellOf(z),4)==0 && lz ∈ {5,6}`. Si se cumple alguno → `BEAM` en `groundY+23` y `groundY+24`. Pilar: (`NS_STREET` con `nsLine`, o `EW_STREET` con `ewLine`) y `along % 32 == 16` → `PILLAR` en `groundY+1..groundY+22`.
- **Carriles** (solo en calle; en cruces y aceras `null`): `NS_STREET`: `across 2..5` → `SOUTH`, `across 6..9` → `NORTH`. `EW_STREET`: `across 2..5` → `WEST`, `across 6..9` → `EAST`.
- **Solar (`LOT`)** en esta tarea: superficie `GRASS` provisional (la Tarea 3 lo sustituye).
- `isPedestrianSurface(x,z)`: superficie ∈ {`SIDEWALK`, `PLAZA_TILE`, `PATH`, `GRASS`} y `AIR` en `groundY+1` y `groundY+2`.

- [ ] **Step 1: Escribir el test que falla `StreetLayoutTest`**
```java
package com.ricardo.frutigeraero.core;

import static com.ricardo.frutigeraero.core.CityBlock.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class StreetLayoutTest {
    final CityLayout L = new CityLayout(42L, 64);
    CityBlock at(int x, int y, int z) { return L.column(x, z).blockAt(y); }

    @Test void streetKinds() {
        assertEquals(StreetKind.INTERSECTION, L.streetKind(4, 4));
        assertEquals(StreetKind.NS_STREET, L.streetKind(4, 30));
        assertEquals(StreetKind.EW_STREET, L.streetKind(30, 4));
        assertEquals(StreetKind.LOT, L.streetKind(30, 30));
        assertEquals(StreetKind.LOT, L.streetKind(-1, 30));
        assertEquals(StreetKind.NS_STREET, L.streetKind(-60, 30));
        assertEquals(StreetKind.INTERSECTION, L.streetKind(-64, -64));
    }
    @Test void intersectionSurface() {
        assertEquals(ASPHALT, at(4, 64, 4));
        assertEquals(SIDEWALK, at(0, 64, 0));
        assertEquals(SIDEWALK, at(11, 64, 11));
        assertEquals(ASPHALT, at(0, 64, 5));
    }
    @Test void northSouthCrossSection() {
        CityBlock[] expected = {SIDEWALK, SIDEWALK, ASPHALT, ASPHALT, ASPHALT, ASPHALT,
                                ASPHALT, ASPHALT, ASPHALT, ASPHALT, SIDEWALK, SIDEWALK};
        for (int x = 0; x < 12; x++) assertEquals(expected[x], at(x, 64, 30), "x=" + x);
        assertEquals(ROAD_LINE, at(5, 64, 33));
        assertEquals(ROAD_LINE, at(6, 64, 33));
    }
    @Test void crosswalk() {
        assertEquals(ROAD_LINE, at(2, 64, 13));
        assertEquals(ASPHALT, at(3, 64, 13));
        assertEquals(ROAD_LINE, at(4, 64, 13));
        assertEquals(ASPHALT, at(5, 64, 13));
        assertEquals(ROAD_LINE, at(2, 64, 62));
    }
    @Test void eastWestMirrorsNorthSouth() {
        for (int across = 0; across < 12; across++)
            for (int along : new int[]{13, 30, 33, 62})
                assertEquals(at(across, 64, along), at(along, 64, across));
    }
    @Test void belowGroundIsStone() {
        assertEquals(STONE, at(4, 63, 30));
        assertEquals(STONE, at(4, 0, 30));
    }
    @Test void lamps() {
        for (int y = 65; y <= 68; y++) assertEquals(LAMP_POLE, at(1, y, 24));
        assertEquals(LIGHT, at(1, 69, 24));
        assertEquals(AIR, at(1, 70, 24));
        assertEquals(LIGHT, at(10, 69, 40));
        assertEquals(AIR, at(1, 65, 25));
    }
    @Test void monorail() {
        assertEquals(BEAM, at(5, 87, 30));
        assertEquals(BEAM, at(5, 88, 30));
        assertEquals(AIR, at(5, 86, 30));
        assertEquals(AIR, at(5, 70, 30));
        assertEquals(PILLAR, at(5, 65, 16));
        assertEquals(PILLAR, at(5, 86, 16));
        assertEquals(PILLAR, at(5, 70, 48));
        assertEquals(AIR, at(69, 88, 30));      // manzana x=1: sin monorraíl
        assertEquals(BEAM, at(261, 88, 30));    // manzana x=4
        assertEquals(BEAM, at(-251, 88, 30));   // manzana x=-4
        assertEquals(BEAM, at(30, 88, 5));      // línea este-oeste en bz=0
    }
    @Test void laneHeadings() {
        assertEquals(Heading.SOUTH, L.laneHeading(3, 30));
        assertEquals(Heading.NORTH, L.laneHeading(8, 30));
        assertEquals(Heading.WEST, L.laneHeading(30, 3));
        assertEquals(Heading.EAST, L.laneHeading(30, 8));
        assertNull(L.laneHeading(1, 30));
        assertNull(L.laneHeading(4, 4));
        assertNull(L.laneHeading(30, 30));
        assertEquals(Heading.SOUTH, L.laneHeading(-61, 30));
    }
    @Test void laneCenters() {
        assertEquals(8.0, CityLayout.laneCenter(Heading.NORTH, 0));
        assertEquals(4.0, CityLayout.laneCenter(Heading.SOUTH, 0));
        assertEquals(-56.0, CityLayout.laneCenter(Heading.EAST, -1));
        assertEquals(132.0, CityLayout.laneCenter(Heading.WEST, 2));
    }
    @Test void carLaneClearance() {               // Review Focus 4
        for (int across : new int[]{3, 4, 7, 8})
            for (int along = 12; along < 64; along++)
                for (int y = 65; y <= 66; y++) {
                    assertEquals(AIR, at(across, y, along), "NS across=" + across + " along=" + along);
                    assertEquals(AIR, at(along, y, across), "EW across=" + across + " along=" + along);
                }
    }
    @Test void topYConsistency() {
        for (int x = -70; x <= 70; x += 7)
            for (int z = -70; z <= 70; z += 5) {
                ColumnPlan p = L.column(x, z);
                assertNotEquals(AIR, p.blockAt(p.topY()));
                assertEquals(AIR, p.blockAt(p.topY() + 1));
                assertEquals(AIR, p.blockAt(p.topY() + 50));
            }
    }
    @Test void pedestrianSurface() {
        assertTrue(L.isPedestrianSurface(0, 30));
        assertFalse(L.isPedestrianSurface(4, 30));
        assertFalse(L.isPedestrianSurface(1, 24));
    }
}
```

- [ ] **Step 2: Ejecutar y comprobar que falla**
Run: `./gradlew test --console=plain --tests '*StreetLayoutTest'`
Esperado: FAIL de compilación (`CityLayout` no existe).

- [ ] **Step 3: Implementar `CityBlock`, `Heading`, `StreetKind`, `ColumnPlan` y la parte de calles de `CityLayout`**
`column(x,z)` crea `new ColumnPlan(groundY)` y, según `streetKind`, pinta la calle (superficie, farola, monorraíl) o el solar provisional. Mete la lógica de calle en un método privado `paintStreet(ColumnPlan p, int x, int z)`. Usa solo `Math.floorMod` y `Math.floorDiv`, **nunca `%` con coordenadas**.

- [ ] **Step 4: Ejecutar y comprobar que pasa**
Run: `./gradlew test --console=plain`
Esperado: PASS (HashTest + StreetLayoutTest).

- [ ] **Step 5: Commit**
```bash
git add frutiger-aero-city/src
git commit -m "feat(frutiger): trazado puro de calles, carriles, farolas y monorraíl"
```

---

### Task 3: Distritos, edificios, parques, plaza y árboles (`core`, Java puro)

**Files:**
- Create: `core/District.java`, `core/BuildingStyle.java`, `core/Building.java`, `core/LotPainter.java`
- Modify: `core/CityLayout.java` (añadir `district`, `buildings`, y delegar los solares en `LotPainter`)
- Test: `core/DistrictTest.java`, `core/BuildingTest.java`, `core/LotTest.java`

**Interfaces:**
- Consumes: todo lo de la Tarea 2.
- Produces:
  - `enum District { PLAZA, DOWNTOWN, COMMERCIAL, RESIDENTIAL, PARK }`
  - `enum BuildingStyle { GLASS_TOWER, CONDO, MALL }`
  - `record Building(BuildingStyle style, int minU, int minV, int maxU, int maxV, int cornerRadius, int height, int setbackY, int setbackInset, int spire, CityBlock accent)` con `boolean inside(int u, int v, int inset)`, `boolean edge(int u, int v, int inset)`, `int topRel(int u, int v)`, `int midU()`, `int midV()` y `boolean isEntrance(int u, int v)`.
  - `CityLayout.district(int bx, int bz) -> District`, `CityLayout.buildings(int bx, int bz) -> List<Building>` (vacía en PLAZA y PARK), `CityLayout.hubDistance(int bx, int bz) -> double`.
  - `final class LotPainter` (de paquete): `static void paint(CityLayout L, int bx, int bz, int u, int v, ColumnPlan p)`.
- Coordenadas de solar: `u = local(x) - 12`, `v = local(z) - 12` (0..51). Para pasar de solar a mundo: `x = bx*64 + 12 + u`.
- Sales de hash (constantes privadas en `CityLayout`/`LotPainter`): `SALT_DISTRICT=11, SALT_PARK=12, SALT_HEIGHT=13, SALT_SPIRE=14, SALT_STYLE=15, SALT_EMPTY=16, SALT_ACCENT=17, SALT_TREE=18`.

**Distrito** `district(bx,bz)`:
1. `bx==0 && bz==0` → `PLAZA`.
2. `unit(of(seed,bx,bz,SALT_PARK)) < 0.12` → `PARK`.
3. `hubDistance`: distancia euclídea al punto más cercano de la red `(32·i, 32·j)` (`dx = bx - 32*Math.round(bx/32.0)`; igual para z).
4. `core = max(0, 1 - hubDistance/16)`; `s = 0.65*noise01(seed+SALT_DISTRICT, bx/8.0, bz/8.0) + 0.35*core`.
5. `s ≥ 0.60` → `DOWNTOWN`; `s ≥ 0.42` → `COMMERCIAL`; en otro caso → `RESIDENTIAL`.

**Edificios** `buildings(bx,bz)` (`ACCENTS = {ACCENT_CYAN, ACCENT_LIME, ACCENT_SKY}`, `accent = ACCENTS[(int)(unit(of(...,SALT_ACCENT))*3)]`, `setbackY = (floors*65/100)*4`):

| Distrito | Parcelas | Regla |
|---|---|---|
| DOWNTOWN | 1 | `GLASS_TOWER`, límites `[8,43]×[8,43]`, r=6, `floors = 18 + (int)(h*23) + (int)(10*core)` con `h=unit(of(seed,bx,bz,SALT_HEIGHT))`, `height=floors*4`, setback a 65% con inset 4, `spire = 8 + (int)(unit(of(seed,bx,bz,SALT_SPIRE))*13)` |
| COMMERCIAL | 4 (`p=0..3`, `ou=(p%2)*26`, `ov=(p/2)*26`) | `r=unit(of(seed,bx,bz,p,SALT_STYLE))`, `f=unit(of(seed,bx,bz,p,SALT_HEIGHT))`. Si `r<0.6` → `MALL` `[ou+2,ou+23]×[ov+2,ov+23]`, r=0, `floors = 2 + (f<0.5?0:1)`, sin setback ni aguja, accent `ACCENT_LIME`. Si no → `GLASS_TOWER` `[ou+3,ou+22]×[ov+3,ov+22]`, r=3, `floors = 8+(int)(f*9)`, setback a 65% con inset 2, `spire = 4+(int)(unit(of(seed,bx,bz,p,SALT_SPIRE))*5)` |
| RESIDENTIAL | 4 | Si `unit(of(seed,bx,bz,p,SALT_EMPTY)) < 0.15` → parcela vacía (jardín). Si no → `CONDO` `[ou+3,ou+22]×[ov+3,ov+22]`, r=2, `floors = 4+(int)(f*7)`, sin setback ni aguja |
| PLAZA, PARK | 0 | `List.of()` |

**Métodos de `Building`:**
- `inside(u,v,inset)`: `a0=minU+inset, a1=maxU-inset, c0=minV+inset, c1=maxV-inset, rr=max(0,cornerRadius-inset)`. Fuera del rectángulo → `false`. Si no: `dx=max(a0+rr-u, u-(a1-rr), 0)`, `dz=max(c0+rr-v, v-(c1-rr), 0)` y devuelve `dx*dx+dz*dz <= rr*rr+rr`.
- `edge(u,v,inset)`: `inside(u,v,inset)` y alguno de sus 4 vecinos no está `inside`.
- `topRel(u,v)`: `(setbackY>0 && !inside(u,v,setbackInset)) ? setbackY : height`.
- `midU()=(minU+maxU)/2`; `isEntrance(u,v)`: `u==midU || u==midU+1 || v==midV || v==midV+1`.

**Pintado de un solar** `LotPainter.paint` (orden obligatorio):
1. **Suelo base** en `groundY`: DOWNTOWN, COMMERCIAL y PLAZA → `PLAZA_TILE`; RESIDENTIAL y PARK → `GRASS` (con `DIRT` en `groundY-1` y `groundY-2`).
2. **PARK** (`d2=(u-25)²+(v-25)²`): `d2≤81` → `WATER` en `groundY` y `groundY-1` y `POND_FLOOR` en `groundY-2`; `81<d2≤110` → `RIM`; si no, y `|u-25|≤1 || |v-25|≤1` → `PATH`.
3. **PLAZA** (mismo `d2`): `d2≤16` → `WATER` en `groundY` y `groundY+1` y `POND_FLOOR` en `groundY-1`; `16<d2≤25` → `RIM` en `groundY` y `groundY+1`; `49<d2≤64` → `ACCENT_SKY` en `groundY`. Columna `(25,25)` → `TRIM` en `groundY..groundY+4`. Orbe: para cada `y` con `dy=y-(groundY+7)`: `d2+dy² ≤ 2` → `LIGHT`; si no, `≤ 9` → `GLASS_CYAN` (solo con `setIfAir`).
4. **Edificio** (si alguno cumple `inside(u,v,0)`): para `rel` de `0` a `top=b.topRel(u,v)`, con `y=groundY+rel`, `inset = (b.setbackY>0 && rel>b.setbackY) ? b.setbackInset : 0` y `isEdge=b.edge(u,v,inset)`:
   - `rel==top` → `MALL` sin borde y `floorMod(u,4)<2` → `GLASS_CLEAR`; en otro caso `ROOF`.
   - `rel%4==0` → si es borde: `CONDO` → `b.accent()`, resto → `TRIM`. Si es interior: `floorMod(u,6)==3 && floorMod(v,6)==3` → `LIGHT`; si no, `FLOOR`.
   - interior → `AIR`.
   - borde, `rel≤2`, `inset==0` y `isEntrance` → `AIR` (puertas).
   - fachada: `GLASS_TOWER`: `(u+v)%4==0` → `TRIM`, si no `GLASS_CYAN`. `CONDO`: `rel%4==3` → `WALL`; si no, `(u+v)%3==0` → `WALL`, y si tampoco `GLASS_AQUA`. `MALL`: `rel<4` → `GLASS_CYAN`; si no, `(u+v)%2==0` → `GLASS_CYAN`, y si tampoco `WALL`.
   - Azotea (`y0=groundY+top`, borde calculado con el inset de `top`): `CONDO`: `y0+1` = borde ? `GLASS_CLEAR` : `GRASS`. `MALL`: borde → `ACCENT_LIME` en `y0+1`. `GLASS_TOWER` con `top==height`: `(u,v)==(midU,midV)` → `SPIRE` en `y0+1..y0+spire-1` y `LIGHT` en `y0+spire`; si es borde → `GLASS_CLEAR` en `y0+1`.
5. **Árboles** (en DOWNTOWN, RESIDENTIAL, PARK y PLAZA; nunca en COMMERCIAL):
   - Rejilla de centros: DOWNTOWN `{3,48}`; RESIDENTIAL `{5,15,25,35,45}`; PARK y PLAZA `{5,15,35,45}`. `(gu,gv)` = valor de la rejilla más cercano a `u` y a `v`.
   - El árbol existe si: `unit(of(seed, bx*64+12+gu, bz*64+12+gv, SALT_TREE)) < densidad` (DOWNTOWN 1.0, PLAZA 1.0, PARK 0.8, RESIDENTIAL 0.9), **y** el suelo base del centro es `GRASS` o `PLAZA_TILE`, **y** ningún edificio cumple `gu ∈ [minU-2,maxU+2] && gv ∈ [minV-2,maxV+2]`, **y** en PARK/PLAZA `(gu-25)²+(gv-25)² > 156`.
   - Pintado con `du=u-gu` y `dv=v-gv`: si `du==dv==0` → `TRUNK` en `groundY+1..groundY+5`. Hojas: `y ∈ [groundY+3, groundY+9]` con `dy=y-(groundY+6)` y `du²+dv²+dy² ≤ 7` → `LEAVES`. Todo con `setIfAir`.

- [ ] **Step 1: Escribir los tests que fallan** (helper compartido: `static int[] find(CityLayout L, District d)` recorre `bx,bz ∈ [-20,20]` en espiral y devuelve la primera manzana de ese distrito, **excluyendo (0,0)**; `wx(bx,u)=bx*64+12+u`)

`DistrictTest`:
```java
@Test void plazaAtOrigin()       { for (long s : new long[]{1, 42, -7}) assertEquals(District.PLAZA, new CityLayout(s, 64).district(0, 0)); }
@Test void deterministic()       { /* dos CityLayout(42) coinciden en district() en [-20,20]² */ }
@Test void seedsDiffer()         { /* CityLayout(1) y CityLayout(2) difieren en al menos una celda de [-20,20]² */ }
@Test void allDistrictsAppear()  { /* semilla 42, [-40,40]²: DOWNTOWN, COMMERCIAL, RESIDENTIAL y PARK con recuento > 0 */ }
@Test void downtownNearHubs()    { /* semilla 42, [-64,64]²: fracción de DOWNTOWN con hubDistance ≤ 4  >  fracción con hubDistance ≥ 12 */ }
@Test void parkFraction()        { /* semilla 42, [-64,64]²: 0.05 < fracción de PARK < 0.25 */ }
@Test void negativeCells()       { assertNotNull(new CityLayout(42, 64).district(-33, -100)); }
```
`BuildingTest` (edificio de prueba `t = new Building(GLASS_TOWER, 8,8,43,43, 6, 100, 64, 4, 10, CityBlock.ACCENT_SKY)`):
```java
@Test void roundedCorners() {
    assertFalse(t.inside(8, 8, 0));  assertTrue(t.inside(25, 8, 0));
    assertTrue(t.inside(14, 14, 0)); assertFalse(t.inside(7, 25, 0));
    assertFalse(t.inside(12, 12, 4)); assertTrue(t.inside(14, 14, 4)); assertTrue(t.inside(25, 12, 4));
}
@Test void edges() { assertTrue(t.edge(25, 8, 0)); assertFalse(t.edge(25, 9, 0)); assertFalse(t.edge(25, 25, 0)); }
@Test void setbackTop() { assertEquals(64, t.topRel(9, 25)); assertEquals(100, t.topRel(25, 25)); }
```
`LotTest` (semilla 42, `groundY 64`; helper `CityBlock at(int[] c, int u, int y, int v) = L.column(wx(c[0],u), wx(c[1],v)).blockAt(y)`; `find` y `wx` son `static` de paquete para reutilizarlos en la Tarea 5):
```java
@Test void downtownTower() {
    int[] c = find(L, District.DOWNTOWN); Building b = L.buildings(c[0], c[1]).get(0);
    assertEquals(BuildingStyle.GLASS_TOWER, b.style()); assertEquals(8, b.minU()); assertEquals(43, b.maxU());
    assertTrue(b.height() >= 72 && b.height() <= 200 && b.height() % 4 == 0);
    assertTrue(b.spire() >= 8 && b.spire() <= 20);
    assertEquals(GLASS_CYAN, at(c, 21, 65, 8));   assertEquals(TRIM, at(c, 21, 68, 8));
    assertEquals(AIR, at(c, 25, 65, 8));          assertEquals(AIR, at(c, 25, 66, 8));   // entrada
    assertEquals(GLASS_CYAN, at(c, 25, 67, 8));
    assertEquals(FLOOR, at(c, 25, 64, 25));       assertEquals(AIR, at(c, 25, 65, 25));
    assertEquals(ROOF, at(c, 25, 64 + b.height(), 25));
    assertEquals(SPIRE, at(c, 25, 65 + b.height(), 25));
    assertEquals(LIGHT, at(c, 25, 64 + b.height() + b.spire(), 25));
    assertEquals(64 + b.height() + b.spire(), L.column(wx(c[0], 25), wx(c[1], 25)).topY());
    assertEquals(LIGHT, at(c, 27, 64, 27));       assertEquals(LIGHT, at(c, 27, 68, 27));
    assertEquals(PLAZA_TILE, at(c, 0, 64, 30));   assertEquals(AIR, at(c, 0, 65, 30));
    assertEquals(TRUNK, at(c, 3, 65, 3)); assertEquals(TRUNK, at(c, 3, 69, 3));
    assertEquals(LEAVES, at(c, 3, 70, 3)); assertEquals(PLAZA_TILE, at(c, 3, 64, 3));
}
@Test void condoRoofGarden() { /* primera RESIDENTIAL con buildings no vacío; b = get(0): CONDO, r=2, 16 ≤ height ≤ 40;
    (b.midU()+2, b.midV()+2): ROOF en 64+h y GRASS en 65+h; (b.midU()+2, b.minV()): GLASS_CLEAR en 65+h;
    blockAt(65) en (b.midU()+2, b.minV()) ∈ {WALL, GLASS_AQUA} */ }
@Test void mallSkylight() { /* primera COMMERCIAL con algún MALL b: (b.minU()+3, b.minV()) GLASS_CYAN en 65 y ACCENT_LIME en 65+h;
    para u en (minU, maxU) exclusivo, en v=b.midV(): y=64+h → floorMod(u,4)<2 ? GLASS_CLEAR : ROOF */ }
@Test void parkPond() { /* primer PARK: (25,25): WATER en 64 y 63, POND_FLOOR en 62; (25,15): RIM en 64; (25,2): PATH en 64; (2,10): GRASS en 64 */ }
@Test void plazaFountain() {   // manzana (0,0): mundo = 12 + u
    for (int y = 64; y <= 68; y++) assertEquals(TRIM, L.column(37, 37).blockAt(y));
    assertEquals(GLASS_CYAN, L.column(37, 37).blockAt(69));
    for (int y = 70; y <= 72; y++) assertEquals(LIGHT, L.column(37, 37).blockAt(y));
    assertEquals(GLASS_CYAN, L.column(37, 37).blockAt(73)); assertEquals(GLASS_CYAN, L.column(37, 37).blockAt(74));
    assertEquals(AIR, L.column(37, 37).blockAt(75));
    assertEquals(WATER, L.column(37, 34).blockAt(64)); assertEquals(WATER, L.column(37, 34).blockAt(65));
    assertEquals(POND_FLOOR, L.column(37, 34).blockAt(63));
    assertEquals(RIM, L.column(37, 32).blockAt(64)); assertEquals(RIM, L.column(37, 32).blockAt(65));
    assertEquals(ACCENT_SKY, L.column(37, 29).blockAt(64)); assertEquals(PLAZA_TILE, L.column(37, 30).blockAt(64));
    assertEquals(TRUNK, L.column(27, 27).blockAt(65));
}
@Test void heightLimit() {      // Review Focus 3
    for (int bx = -30; bx <= 30; bx++) for (int bz = -30; bz <= 30; bz++)
        for (Building b : L.buildings(bx, bz)) assertTrue(b.height() + b.spire() + 1 <= ColumnPlan.MAX_ABOVE);
}
@Test void orderIndependence() { // Review Focus 2: 50 coords (x=i*37-900, z=i*53-1300) en orden directo con L1 y en orden inverso con L2 (misma semilla): blockAt idéntico para y ∈ [60,300] */ }
@Test void topYConsistencyInCity() { /* igual que en la Tarea 2, pero en [-200,200]² con paso 3 */ }
```
(Las líneas `/* … */` describen exactamente las aserciones que tienes que escribir; ese es el contrato.)

- [ ] **Step 2: Ejecutar y comprobar que falla**
Run: `./gradlew test --console=plain`
Esperado: FAIL de compilación (`District`, `Building` y `LotPainter` no existen).

- [ ] **Step 3: Implementar `District`, `BuildingStyle`, `Building`, `LotPainter` y las ampliaciones de `CityLayout`**
`CityLayout.column` delega en `LotPainter.paint(this, cellOf(x), cellOf(z), local(x)-12, local(z)-12, plan)` cuando `streetKind == LOT`. Calcula `district` y `buildings` en cada llamada (son baratos; **no** los guardes en caché).

- [ ] **Step 4: Ejecutar y comprobar que pasa**
Run: `./gradlew test --console=plain`
Esperado: PASS de todos los tests. Si falla `allDistrictsAppear` o `downtownNearHubs`, revisa que uses `Math.round` y `floorDiv` como en las reglas. **No cambies los umbrales.**

- [ ] **Step 5: Commit**
```bash
git add frutiger-aero-city/src
git commit -m "feat(frutiger): distritos, torres de cristal, condos, malls, parques, plaza-fuente y árboles"
```

---

### Task 4: Integración con Minecraft: ChunkGenerator, world preset y prueba de humo

**Files:**
- Create: `world/ModWorldgen.java`, `world/BlockPalette.java`, `world/CityChunkGenerator.java`
- Modify: `FrutigerAeroMod.java`
- Create: `src/main/resources/data/frutigeraero/worldgen/world_preset/frutiger_aero_city.json`
- Create: `src/main/resources/data/frutigeraero/worldgen/biome/aero_city.json`
- Create: `src/main/resources/data/minecraft/tags/worldgen/world_preset/normal.json`
- Create: `src/main/resources/data/minecraft/tags/worldgen/biome/without_patrol_spawns.json`
- Create: `src/main/resources/assets/frutigeraero/lang/en_us.json`, `es_es.json`
- Create: `tools/smoke.sh`, `tools/smoke/expected.txt`, `tools/smoke/datapack/**`

**Interfaces:**
- Consumes: `CityLayout`, `ColumnPlan`, `CityBlock` y `District` (Tareas 2 y 3).
- Produces:
  - `CityChunkGenerator(BiomeSource biomeSource, int groundY)`; `public static final MapCodec<CityChunkGenerator> CODEC`; `public CityLayout layout(RandomState rs)`; `public static @Nullable CityLayout layoutOf(ServerLevel level)`; `public int groundY()`.
  - `BlockPalette.state(CityBlock b) -> BlockState`.
  - Tipo de generador `frutigeraero:city`. Preset `frutigeraero:frutiger_aero_city`. Bioma `frutigeraero:aero_city`.

- [ ] **Step 1: Arnés de la prueba de humo (servidor real); es el test que falla**

`tools/smoke/datapack/pack.mcmeta`: `{"pack":{"pack_format":48,"description":"smoke"}}`
`tools/smoke/datapack/data/minecraft/tags/function/load.json`: `{"values":["smoke:load"]}`
`tools/smoke/datapack/data/smoke/function/load.mcfunction`:
```
forceload add 0 0 63 63
schedule function smoke:check 240t
```
`tools/smoke/datapack/data/smoke/function/check.mcfunction`:
```
execute if block 4 64 4 minecraft:gray_concrete run say SMOKE_OK_road
execute unless block 4 64 4 minecraft:gray_concrete run say SMOKE_FAIL_road
execute if block 0 64 30 minecraft:smooth_stone run say SMOKE_OK_sidewalk
execute unless block 0 64 30 minecraft:smooth_stone run say SMOKE_FAIL_sidewalk
execute if block 1 69 24 minecraft:sea_lantern run say SMOKE_OK_lamp
execute unless block 1 69 24 minecraft:sea_lantern run say SMOKE_FAIL_lamp
execute if block 37 64 34 minecraft:water run say SMOKE_OK_fountain
execute unless block 37 64 34 minecraft:water run say SMOKE_FAIL_fountain
say SMOKE_DONE
stop
```
`tools/smoke/expected.txt` (una por línea): `SMOKE_OK_road`, `SMOKE_OK_sidewalk`, `SMOKE_OK_lamp`, `SMOKE_OK_fountain`, `SMOKE_DONE`.

`tools/smoke.sh` (ejecutable, se lanza desde `frutiger-aero-city/`):
```bash
#!/usr/bin/env bash
set -u
cd "$(dirname "$0")/.."
mkdir -p run && rm -rf run/smoke && mkdir -p run/smoke/datapacks
cp -r tools/smoke/datapack run/smoke/datapacks/smoke
echo "eula=true" > run/eula.txt
cat > run/server.properties <<'EOF'
level-name=smoke
level-seed=12345
level-type=frutigeraero\:frutiger_aero_city
online-mode=false
function-permission-level=4
spawn-protection=0
view-distance=4
simulation-distance=4
EOF
timeout 900 ./gradlew runServer --console=plain > run/smoke.log 2>&1
grep -E "SMOKE_|ERROR|Exception" run/smoke.log | head -40
status=0
while read -r want; do
  [ -z "$want" ] && continue
  grep -q "$want" run/smoke.log || { echo "FALTA: $want"; status=1; }
done < tools/smoke/expected.txt
grep -q "SMOKE_FAIL" run/smoke.log && status=1
[ $status -eq 0 ] && echo "SMOKE PASSED" || echo "SMOKE FAILED (ver run/smoke.log)"
exit $status
```

- [ ] **Step 2: Ejecutar la prueba de humo y comprobar que falla**
Run: `./gradlew build --console=plain && bash tools/smoke.sh`
Esperado: `FALTA: SMOKE_OK_road` (el preset aún no existe, así que el servidor genera un mundo normal).

- [ ] **Step 3: `BlockPalette`**: `switch` exhaustivo **sin `default`** (así el compilador obliga a cubrir cada valor del enum), guardado en caché en un `BlockState[]` construido en el primer uso:

| CityBlock | Bloque vanilla |
|---|---|
| AIR | `AIR` |
| STONE | `STONE` |
| DIRT | `DIRT` |
| ASPHALT | `GRAY_CONCRETE` |
| ROAD_LINE | `WHITE_CONCRETE` |
| SIDEWALK | `SMOOTH_STONE` |
| PLAZA_TILE | `SMOOTH_QUARTZ` |
| PATH | `CALCITE` |
| GRASS | `GRASS_BLOCK` |
| WATER | `WATER` |
| POND_FLOOR | `PRISMARINE` |
| RIM | `WHITE_CONCRETE` |
| TRUNK | `BIRCH_LOG` |
| LEAVES | `OAK_LEAVES` con `.setValue(LeavesBlock.PERSISTENT, true)` (si no, se caen) |
| LAMP_POLE | `IRON_BARS` |
| LIGHT | `SEA_LANTERN` |
| GLASS_CYAN | `LIGHT_BLUE_STAINED_GLASS` |
| GLASS_AQUA | `CYAN_STAINED_GLASS` |
| GLASS_CLEAR | `GLASS` |
| TRIM | `QUARTZ_PILLAR` |
| WALL | `WHITE_CONCRETE` |
| FLOOR | `SMOOTH_QUARTZ` |
| ROOF | `WHITE_CONCRETE` |
| ACCENT_CYAN | `CYAN_CONCRETE` |
| ACCENT_LIME | `LIME_CONCRETE` |
| ACCENT_SKY | `LIGHT_BLUE_CONCRETE` |
| SPIRE | `IRON_BLOCK` |
| BEAM | `SMOOTH_QUARTZ` |
| PILLAR | `WHITE_CONCRETE` |

- [ ] **Step 4: `CityChunkGenerator`**. Firmas exactas en el Apéndice A. Comportamiento:
  - `CODEC = RecordCodecBuilder.mapCodec(i -> i.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource), Codec.intRange(0, 100).optionalFieldOf("ground_y", 64).forGetter(CityChunkGenerator::groundY)).apply(i, CityChunkGenerator::new))`.
  - `layout(rs)`: `long seed = rs.getOrCreateRandomFactory(ResourceLocation.fromNamespaceAndPath(MODID, "city")).at(0, 0, 0).nextLong();` y caché en un campo `volatile CityLayout cached` (se recrea si cambia la semilla).
  - `layoutOf(level)`: si `level.getChunkSource().getGenerator() instanceof CityChunkGenerator g` → `g.layout(level.getChunkSource().randomState())`; si no, `null`.
  - `fillFromNoise`: para cada una de las 16×16 columnas, `ColumnPlan col = layout.column(minBlockX+lx, minBlockZ+lz)`; recorre `y` desde `chunk.getMinBuildHeight()` hasta `min(col.topY(), chunk.getMaxBuildHeight()-1)`. En `y == minBuildHeight` pon `BEDROCK`; si no, `BlockPalette.state(col.blockAt(y))`. Salta el aire. Para cada bloque: `chunk.setBlockState(mutable.set(lx, y, lz), state, false)` y actualiza los heightmaps `OCEAN_FLOOR_WG` y `WORLD_SURFACE_WG` (obtenidos con `chunk.getOrCreateHeightmapUnprimed`) con `update(lx, y, lz, state)`. Devuelve `CompletableFuture.completedFuture(chunk)`. Es el mismo patrón que `FlatLevelSource#fillFromNoise`.
  - `getBaseHeight(...)` → `min(col.topY()+1, height.getMaxBuildHeight())`. `getBaseColumn(...)` → `new NoiseColumn(minY, states)` con los estados desde `minY` hasta `topY` (bedrock en `minY`).
  - `getSeaLevel()` → `groundY-1`; `getMinY()` → `-64`; `getGenDepth()` → `384`; `getSpawnHeight(...)` → `groundY+1`.
  - `applyCarvers` y `buildSurface` vacíos. `spawnOriginalMobs` vacío por ahora (lo rellenan las Tareas 6 y 8).
  - `createState(...)` → `ChunkGeneratorStructureState.createForFlat(randomState, seed, biomeSource, Stream.empty())` (sin estructuras vanilla ni cálculo de fortalezas).
  - `addDebugScreenInfo` → añade `"Aero City: " + district + " / " + streetKind` (aparece en F3).
- [ ] **Step 5: `ModWorldgen`**: `DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID)` y `CITY = CHUNK_GENERATORS.register("city", () -> CityChunkGenerator.CODEC)`. Regístralo en el bus del mod desde `FrutigerAeroMod`.

- [ ] **Step 6: JSON de datos (copia exacta)**

`worldgen/world_preset/frutiger_aero_city.json`:
```json
{
  "dimensions": {
    "minecraft:overworld": {
      "type": "minecraft:overworld",
      "generator": {
        "type": "frutigeraero:city",
        "biome_source": { "type": "minecraft:fixed", "biome": "frutigeraero:aero_city" },
        "ground_y": 64
      }
    },
    "minecraft:the_nether": {
      "type": "minecraft:the_nether",
      "generator": {
        "type": "minecraft:noise",
        "biome_source": { "type": "minecraft:multi_noise", "preset": "minecraft:nether" },
        "settings": "minecraft:nether"
      }
    },
    "minecraft:the_end": {
      "type": "minecraft:the_end",
      "generator": {
        "type": "minecraft:noise",
        "biome_source": { "type": "minecraft:the_end" },
        "settings": "minecraft:end"
      }
    }
  }
}
```
`worldgen/biome/aero_city.json` (los colores son enteros decimales: cielo `#7FCBFF`, niebla `#D4F1FF`, agua `#3FD8F2`, niebla del agua `#1A9FC4`, hierba `#7CE33B` y follaje `#8BEA4A`):
```json
{
  "has_precipitation": true,
  "temperature": 0.8,
  "downfall": 0.4,
  "effects": {
    "sky_color": 8375295,
    "fog_color": 13955583,
    "water_color": 4184306,
    "water_fog_color": 1744836,
    "grass_color": 8184635,
    "foliage_color": 9169482
  },
  "spawners": {
    "monster": [], "creature": [], "ambient": [], "axolotls": [],
    "underground_water_creature": [], "water_creature": [], "water_ambient": [], "misc": []
  },
  "spawn_costs": {},
  "carvers": {},
  "features": []
}
```
`tags/worldgen/world_preset/normal.json`: `{ "replace": false, "values": ["frutigeraero:frutiger_aero_city"] }`
`tags/worldgen/biome/without_patrol_spawns.json`: `{ "replace": false, "values": ["frutigeraero:aero_city"] }`

Lang (`en_us` / `es_es`): `generator.frutigeraero.frutiger_aero_city` = `FRUTIGER AERO CITY` / `FRUTIGER AERO CITY`; `biome.frutigeraero.aero_city` = `Aero City` / `Ciudad Aero`.

- [ ] **Step 7: Compilar y pasar la prueba de humo**
Run: `./gradlew build --console=plain && bash tools/smoke.sh`
Esperado: `BUILD SUCCESSFUL` y `SMOKE PASSED`. Si el log contiene `Exception` con `frutigeraero` en la traza, corrígelo antes de seguir (Apéndice B).

- [ ] **Step 8: Commit y push (fin de la Fase 1)**
```bash
git add frutiger-aero-city
git commit -m "feat(frutiger): world type FRUTIGER AERO CITY con generador de ciudad y prueba de humo"
git push -u origin <tu-rama>
```

---

# FASE 2 — Ciudadanos (NPCs)

### Task 5: Puntos de aparición de ciudadanos y destinos de paseo (`core`, Java puro)

**Files:**
- Create: `core/Spawns.java`, `core/PedestrianTargets.java`
- Test: `core/SpawnsTest.java`

**Interfaces:**
- Consumes: `CityLayout.isPedestrianSurface`, `CityLayout.groundY`, `Hash`.
- Produces:
  - `Spawns.CitizenSpawn` = `record CitizenSpawn(int x, int y, int z, float yaw)` (anidado en `Spawns`).
  - `static List<CitizenSpawn> Spawns.citizens(CityLayout L, int chunkX, int chunkZ)`.
  - `static int[] PedestrianTargets.pick(CityLayout L, int x, int z, long seed)`: devuelve `{tx, tz}` o `null`.
- Sales: `SALT_CITIZEN = 31` (y `SALT_VEHICLE = 32` para la Tarea 7).

**Reglas:**
- `citizens`: `rng = new SplittableRandom(Hash.of(L.seed(), chunkX, chunkZ, SALT_CITIZEN))`; `target = rng.nextDouble() < 0.30 ? (rng.nextDouble() < 0.33 ? 2 : 1) : 0`. Hasta 24 intentos: `x = chunkX*16 + rng.nextInt(16)`, `z = chunkZ*16 + rng.nextInt(16)`. Si `isPedestrianSurface(x,z)`, se añade `(x, groundY+1, z, rng.nextInt(360))`. Para al llegar a `target`.
- `pick`: `rng = new SplittableRandom(seed)`; 12 intentos de `tx = x + rng.nextInt(-12, 13)` y `tz = z + rng.nextInt(-12, 13)`. Devuelve el primero que sea distinto de `(x,z)` y cumpla `isPedestrianSurface`; si ninguno lo cumple, `null`.

- [ ] **Step 1: Escribir el test que falla `SpawnsTest`**
```java
final CityLayout L = new CityLayout(42L, 64);
@Test void citizensDeterministic() { assertEquals(Spawns.citizens(L, 3, -2), Spawns.citizens(L, 3, -2)); }
@Test void citizensValid() {
    int total = 0;
    for (int cx = -8; cx <= 8; cx++) for (int cz = -8; cz <= 8; cz++) {
        List<Spawns.CitizenSpawn> list = Spawns.citizens(L, cx, cz);
        assertTrue(list.size() <= 2);
        for (Spawns.CitizenSpawn s : list) {
            assertTrue(s.x() >= cx * 16 && s.x() < cx * 16 + 16);
            assertTrue(s.z() >= cz * 16 && s.z() < cz * 16 + 16);
            assertEquals(65, s.y());
            assertTrue(L.isPedestrianSurface(s.x(), s.z()));
        }
        total += list.size();
    }
    assertTrue(total >= 20 && total <= 200, "total=" + total);
}
@Test void pickNearSidewalk() {
    int found = 0;
    for (long seed = 0; seed < 200; seed++) {
        int[] t = PedestrianTargets.pick(L, 0, 30, seed);
        if (t == null) continue;
        found++;
        assertTrue(Math.abs(t[0]) <= 12 && Math.abs(t[1] - 30) <= 12);
        assertFalse(t[0] == 0 && t[1] == 30);
        assertTrue(L.isPedestrianSurface(t[0], t[1]));
    }
    assertTrue(found >= 150, "found=" + found);
}
@Test void pickNullInsideTower() {
    int[] c = LotTest.find(L, District.DOWNTOWN);          // reutiliza el helper de la Tarea 3 (hazlo static de paquete)
    int x = c[0] * 64 + 12 + 25, z = c[1] * 64 + 12 + 25;
    for (long seed = 0; seed < 50; seed++) assertNull(PedestrianTargets.pick(L, x, z, seed));
}
```
- [ ] **Step 2: Run** `./gradlew test --console=plain --tests '*SpawnsTest'` → Esperado: FAIL (`Spawns` no existe).
- [ ] **Step 3: Implementar `Spawns.citizens` y `PedestrianTargets.pick`** según las reglas.
- [ ] **Step 4: Run** `./gradlew test --console=plain` → Esperado: PASS.
- [ ] **Step 5: Commit** `git commit -am "feat(frutiger): puntos de aparición y destinos de paseo de ciudadanos"` (antes, `git add` de los archivos nuevos).

---

### Task 6: `CitizenEntity`, render, skins y aparición al generar chunks

**Files:**
- Create: `entity/ModEntities.java`, `entity/CitizenEntity.java`, `entity/WalkSidewalkGoal.java`
- Create: `item/ModItems.java`
- Create: `client/ClientSetup.java`, `client/CitizenRenderer.java`
- Create: `tools/gen_textures.py` (parte de skins) → genera `assets/frutigeraero/textures/entity/citizen/citizen_0.png` … `citizen_7.png`
- Create: `assets/frutigeraero/models/item/citizen_spawn_egg.json` = `{"parent": "minecraft:item/template_spawn_egg"}`
- Modify: `FrutigerAeroMod.java`, `world/CityChunkGenerator.java` (`spawnOriginalMobs`), lang, `tools/smoke/datapack/data/smoke/function/check.mcfunction`, `tools/smoke/expected.txt`

**Interfaces:**
- Consumes: `Spawns.citizens`, `PedestrianTargets.pick`, `CityChunkGenerator.layoutOf`.
- Produces:
  - `ModEntities.ENTITY_TYPES` (`DeferredRegister<EntityType<?>>`, `ForgeRegistries.ENTITY_TYPES`) y `ModEntities.CITIZEN` (`RegistryObject<EntityType<CitizenEntity>>`, id `citizen`, `MobCategory.CREATURE`, `sized(0.6F, 1.8F)`, `clientTrackingRange(10)`). Más `static void onAttributes(EntityAttributeCreationEvent e)`.
  - `CitizenEntity extends PathfinderMob`: `public static final int VARIANTS = 8, LINES = 12;`, `public int variant()`, `public static AttributeSupplier.Builder createAttributes()` (`MAX_HEALTH 20`, `MOVEMENT_SPEED 0.5`).
  - `ModItems.ITEMS`, `ModItems.TABS`, `ModItems.CITIZEN_SPAWN_EGG` (`ForgeSpawnEggItem`, colores `0x7FDBFF` / `0xB6FF5C`) y `ModItems.TAB` (id `main`, título `itemGroup.frutigeraero`, icono = huevo).

**Comportamiento de `CitizenEntity`:**
- Dato sincronizado `DATA_VARIANT` (`EntityDataSerializers.INT`, valor por defecto 0). NBT `"Variant"`.
- `finalizeSpawn`: `variant = random.nextInt(8)` y nombre `Component.literal(NAMES[random.nextInt(16)])` con `setCustomNameVisible(false)`. `NAMES = {"Aqua","Skye","Nova","Kai","Luma","Pixel","Bubbles","Orion","Mika","Zephyr","Coral","Echo","Juno","Breeze","Halo","Marina"}`.
- Objetivos (`registerGoals`, por prioridad): `0 FloatGoal`, `1 PanicGoal(this, 0.75)`, `2 WalkSidewalkGoal(this, 0.55)`, `3 WaterAvoidingRandomStrollGoal(this, 0.5)`, `4 LookAtPlayerGoal(this, Player.class, 6F)`, `5 RandomLookAroundGoal(this)`.
- `mobInteract(player, hand)`: solo en servidor. Mira al jugador (`getLookControl().setLookAt(player)`), le envía `Component.literal("<" + nombre + "> ").append(Component.translatable("npc.frutigeraero.line." + random.nextInt(12)))` con `player.sendSystemMessage` y reproduce `SoundEvents.NOTE_BLOCK_CHIME` (volumen 0.6, tono 1.5). Devuelve `InteractionResult.sidedSuccess(level().isClientSide)`.
- `removeWhenFarAway(double)` → `false` (no desaparecen, como los aldeanos).
- `WalkSidewalkGoal` (flag `MOVE`): `canUse()` → `random.nextInt(60)==0`, el nivel es `ServerLevel`, `layoutOf` no es `null` y `pick(layout, blockX, blockZ, random.nextLong())` no es `null`. `start()` → `getNavigation().moveTo(tx+0.5, groundY+1, tz+0.5, speed)`. `canContinueToUse()` → `!getNavigation().isDone()`. Fuera de la ciudad (`layoutOf == null`) nunca se activa: Review Focus 5.
- `CityChunkGenerator.spawnOriginalMobs(region)`: `layout = layout(region.getLevel().getChunkSource().randomState())`. Para cada `CitizenSpawn` de `Spawns.citizens(layout, center.x, center.z)`: `create(region.getLevel())`, `moveTo(x+0.5, y, z+0.5, yaw, 0)`, `finalizeSpawn(region, region.getCurrentDifficultyAt(pos), MobSpawnType.CHUNK_GENERATION, null)` y `region.addFreshEntityWithPassengers(c)`.
- `CitizenRenderer extends HumanoidMobRenderer<CitizenEntity, PlayerModel<CitizenEntity>>`: `super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5F)`. La textura es `textures/entity/citizen/citizen_<variant>.png`.
- `ClientSetup`: `@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)` con `@SubscribeEvent static void registerRenderers(EntityRenderersEvent.RegisterRenderers e)`.

**Skins (`tools/gen_textures.py`)**: Python con la librería estándar; escribe PNG RGBA con `zlib` y `struct` (byte de filtro 0 por fila). Es idempotente y se ejecuta desde `frutiger-aero-city/` con `python3 tools/gen_textures.py`. Cada skin mide 64×64 y empieza **totalmente transparente**:
- Cabeza `x0..31, y0..15` = piel; pelo en la cara superior `(8..15, 0..7)` y en las filas `y 8..9` de toda la franja `x0..31`; ojos en `(9,12)`, `(10,12)`, `(13,12)` y `(14,12)` = `#1B3B5A`.
- Torso `x16..39, y16..31` = camiseta; brazo derecho `x40..55, y16..31` = camiseta (filas `y28..31` = piel); brazo izquierdo `x32..47, y48..63` = camiseta (filas `y60..63` = piel).
- Pierna derecha `x0..15, y16..31` = pantalón (filas `y29..31` = `#FFFFFF`, zapatillas); pierna izquierda `x16..31, y48..63` = pantalón (filas `y61..63` = `#FFFFFF`).
- El resto (capas exteriores) se queda en alpha 0.
- Paletas (índice = número de variante):
  - piel `[F2D1B3, D9A066, 8D5524, C68642, FFDBAC, 6B4423, E0AC69, F1C27D]`
  - camiseta `[00B4D8, 7CD62E, F2F6FA, 2E6FE0, FF8C1A, E8336B, 9AD9EA, C9D3DB]`
  - pantalón `[E8EEF2, 3A6EA5, C9D3DB, 1B3B5A, F2F6FA, 5A7D9A, DDE6EE, 2B4C6F]`
  - pelo `[2B1B0E, E6C35C, 5A3825, 111111, B5651D, 00B4D8, 111111, F5F5F5]`

**Lang** (`en_us` / `es_es`): `entity.frutigeraero.citizen` = `Citizen` / `Ciudadano`; `item.frutigeraero.citizen_spawn_egg` = `Citizen Spawn Egg` / `Huevo de ciudadano`; `itemGroup.frutigeraero` = `Frutiger Aero City`; frases `npc.frutigeraero.line.0..11`:
0 `What a beautiful blue sky today!` / `¡Qué cielo tan azul hoy!` · 1 `Have you tried the new bubble cars?` / `¿Ya probaste los nuevos coches burbuja?` · 2 `The fountain in the main plaza is so relaxing.` / `La fuente de la plaza principal es muy relajante.` · 3 `I just downloaded a new ringtone!` / `¡Acabo de descargar un tono nuevo!` · 4 `Everything is so clean and shiny here.` / `Aquí todo está tan limpio y brillante.` · 5 `Let's meet at the mall later, OK?` / `Nos vemos luego en el centro comercial, ¿vale?` · 6 `The monorail looks amazing from below.` / `El monorraíl se ve increíble desde abajo.` · 7 `Have you seen the glass towers at night? They glow!` / `¿Viste las torres de cristal de noche? ¡Brillan!` · 8 `Nature and technology, living in harmony.` / `Naturaleza y tecnología en armonía.` · 9 `My new MP3 player holds 500 songs!` / `¡Mi reproductor MP3 nuevo tiene 500 canciones!` · 10 `Welcome to Frutiger Aero City!` / `¡Bienvenido a Frutiger Aero City!` · 11 `Don't forget to drink water and stay fresh.` / `No olvides beber agua y mantenerte fresco.`

- [ ] **Step 1: Ampliar la prueba de humo (la que falla)**: añade a `check.mcfunction`, **antes de** `say SMOKE_DONE`:
```
execute if entity @e[type=frutigeraero:citizen] run say SMOKE_OK_citizens
execute unless entity @e[type=frutigeraero:citizen] run say SMOKE_FAIL_citizens
```
y añade `SMOKE_OK_citizens` a `expected.txt`.
- [ ] **Step 2: Run** `bash tools/smoke.sh` → Esperado: `SMOKE FAILED` (falta `SMOKE_OK_citizens`; el tipo de entidad aún no existe).
- [ ] **Step 3: Generar las skins.** Run: `python3 tools/gen_textures.py && file src/main/resources/assets/frutigeraero/textures/entity/citizen/citizen_0.png` → Esperado: `PNG image data, 64 x 64, 8-bit/color RGBA`.
- [ ] **Step 4: Implementar `ModEntities`, `CitizenEntity`, `WalkSidewalkGoal`, `ModItems`, `ClientSetup`, `CitizenRenderer` y `spawnOriginalMobs`.** Registra en `FrutigerAeroMod`: `ModEntities.ENTITY_TYPES`, `ModItems.ITEMS`, `ModItems.TABS` y `modEventBus.addListener(ModEntities::onAttributes)`.
- [ ] **Step 5: Run** `./gradlew build --console=plain && bash tools/smoke.sh` → Esperado: `BUILD SUCCESSFUL` y `SMOKE PASSED`.
- [ ] **Step 6: Commit y push (fin de la Fase 2)**
```bash
git add frutiger-aero-city
git commit -m "feat(frutiger): ciudadanos NPC con skins Y2K, frases y paseo por aceras"
git push -u origin <tu-rama>
```

---

# FASE 3 — Vehículos Y2K

### Task 7: `Autopilot`, `CarColor` y puntos de aparición de coches (`core`, Java puro)

**Files:**
- Create: `core/Autopilot.java`, `core/CarColor.java`
- Modify: `core/Spawns.java` (añadir `vehicles`)
- Test: `core/AutopilotTest.java`, `core/CarColorTest.java`, `core/SpawnsTest.java` (ampliar)

**Interfaces:**
- Consumes: `CityLayout.streetKind`, `laneHeading`, `laneCenter`, `cellOf`; `Heading`.
- Produces:
  - `enum CarColor { BONDI_BLUE(0xFF0095B6), BLUEBERRY(0xFF2E6FE0), GRAPE(0xFF7E4FC6), TANGERINE(0xFFFF8C1A), LIME(0xFF7CD62E), STRAWBERRY(0xFFE8336B), SNOW(0xFFF2F6FA) }` con `int argb()`, `String id()` (minúsculas), `static CarColor byId(String)` (valor desconocido → `BONDI_BLUE`) y `static CarColor byIndex(int)` (`floorMod`).
  - `final class Autopilot`: `Autopilot(Heading initial)`, `Heading heading()`, `boolean onRoad()`, `void update(CityLayout L, double x, double z, double r)`, `double laneCoord(double x, double z)`, `static Heading chooseTurn(Heading current, double r)`.
  - `Spawns.VehicleSpawn` = `record VehicleSpawn(double x, int y, double z, Heading heading, CarColor color)`; `static List<VehicleSpawn> Spawns.vehicles(CityLayout L, int chunkX, int chunkZ)`.

**Algoritmo de `Autopilot`** (estado: `heading`, `pending` (que puede ser `null`), `pendingCoord`, `wasInIntersection` y `onRoad`):
```
chooseTurn(h, r): r < 0.60 → h ; r < 0.85 → h.right() ; else → h.left()
reached(h, x, z, c): NORTH → z <= c ; SOUTH → z >= c ; EAST → x >= c ; WEST → x <= c
update(L, x, z, r):
  bx = floor(x); bz = floor(z); kind = L.streetKind(bx, bz); onRoad = kind != LOT
  if kind == INTERSECTION && !wasInIntersection:
      next = chooseTurn(heading, r)
      if next != heading: pending = next
          pendingCoord = (next is EAST or WEST) ? laneCenter(next, cellOf(bz)) : laneCenter(next, cellOf(bx))
  if pending != null && reached(heading, x, z, pendingCoord): heading = pending; pending = null
  if kind == NS_STREET || kind == EW_STREET:
      pending = null
      h = L.laneHeading(bx, bz); if h != null: heading = h
  wasInIntersection = (kind == INTERSECTION)
laneCoord(x, z): heading NORTH/SOUTH → laneCenter(heading, cellOf(floor(x)))
                 heading EAST/WEST   → laneCenter(heading, cellOf(floor(z)))
```
**`Spawns.vehicles`**: `rng = new SplittableRandom(Hash.of(seed, chunkX, chunkZ, SALT_VEHICLE))`. Si `rng.nextDouble() >= 0.20` → lista vacía. Si no, hasta 16 intentos con una columna aleatoria del chunk: si `h = laneHeading(x,z)` no es `null` → un único coche. Para NORTH/SOUTH: `x = laneCenter(h, cellOf(x))` y `z = z + 0.5`. Para EAST/WEST: `z = laneCenter(h, cellOf(z))` y `x = x + 0.5`. Además `y = groundY+1` y `color = CarColor.byIndex(rng.nextInt(7))`.

- [ ] **Step 1: Escribir los tests que fallan**
```java
// AutopilotTest
final CityLayout L = new CityLayout(42L, 64);
@Test void chooseTurnTable() {
    assertEquals(Heading.NORTH, Autopilot.chooseTurn(Heading.NORTH, 0.10));
    assertEquals(Heading.EAST,  Autopilot.chooseTurn(Heading.NORTH, 0.70));
    assertEquals(Heading.WEST,  Autopilot.chooseTurn(Heading.NORTH, 0.90));
    assertEquals(Heading.SOUTH, Autopilot.chooseTurn(Heading.EAST, 0.70));
    assertEquals(Heading.WEST,  Autopilot.chooseTurn(Heading.SOUTH, 0.70));
    assertEquals(Heading.NORTH, Autopilot.chooseTurn(Heading.WEST, 0.70));
    assertEquals(Heading.EAST,  Autopilot.chooseTurn(Heading.SOUTH, 0.90));
}
Heading driveNorth(double r, double untilZ) {           // x = 8.0 (carril NORTH de la manzana 0)
    Autopilot ap = new Autopilot(Heading.NORTH);
    for (double z = 20.5; z >= untilZ; z -= 0.25) ap.update(L, 8.0, z, r);
    return ap.heading();
}
@Test void rightTurn()   { assertEquals(Heading.NORTH, driveNorth(0.70, 9.0)); assertEquals(Heading.EAST, driveNorth(0.70, 8.0)); }
@Test void leftTurn()    { assertEquals(Heading.NORTH, driveNorth(0.90, 4.25)); assertEquals(Heading.WEST, driveNorth(0.90, 4.0)); }
@Test void straight()    { assertEquals(Heading.NORTH, driveNorth(0.30, -5.0)); }
@Test void resyncToLane() { Autopilot ap = new Autopilot(Heading.NORTH); ap.update(L, 3.0, 30.5, 0.5); assertEquals(Heading.SOUTH, ap.heading()); }
@Test void laneCoord() {                              // Review Focus 1 (negativos)
    assertEquals(8.0, new Autopilot(Heading.NORTH).laneCoord(8.7, 40.0));
    assertEquals(4.0, new Autopilot(Heading.SOUTH).laneCoord(3.2, 40.0));
    assertEquals(8.0, new Autopilot(Heading.EAST).laneCoord(30.0, 8.6));
    assertEquals(-60.0, new Autopilot(Heading.SOUTH).laneCoord(-60.3, 30.0));
}
@Test void strandedInLot() { Autopilot ap = new Autopilot(Heading.EAST); ap.update(L, 30.5, 30.5, 0.5); assertFalse(ap.onRoad()); assertEquals(Heading.EAST, ap.heading()); }

// CarColorTest
@Test void roundTrip() { for (CarColor c : CarColor.values()) assertEquals(c, CarColor.byId(c.id())); }
@Test void defaults()  { assertEquals(CarColor.BONDI_BLUE, CarColor.byId("nope")); assertEquals(CarColor.SNOW, CarColor.byIndex(-1)); }
@Test void opaque()    { for (CarColor c : CarColor.values()) assertEquals(0xFF, c.argb() >>> 24); }

// SpawnsTest (añadir)
@Test void vehiclesOnLaneCenters() {
    int total = 0;
    for (int cx = -8; cx <= 8; cx++) for (int cz = -8; cz <= 8; cz++) {
        List<Spawns.VehicleSpawn> list = Spawns.vehicles(L, cx, cz);
        assertTrue(list.size() <= 1);
        for (Spawns.VehicleSpawn v : list) {
            int bx = (int) Math.floor(v.x()), bz = (int) Math.floor(v.z());
            assertEquals(v.heading(), L.laneHeading(bx, bz));
            boolean ns = v.heading() == Heading.NORTH || v.heading() == Heading.SOUTH;
            double lateral = ns ? v.x() : v.z();
            assertEquals(CityLayout.laneCenter(v.heading(), CityLayout.cellOf(ns ? bx : bz)), lateral);
            assertTrue(bx >= cx * 16 && bx < cx * 16 + 16 && bz >= cz * 16 && bz < cz * 16 + 16);
            assertEquals(65, v.y()); assertNotNull(v.color());
        }
        total += list.size();
    }
    assertTrue(total > 0);
}
```
- [ ] **Step 2: Run** `./gradlew test --console=plain` → Esperado: FAIL de compilación.
- [ ] **Step 3: Implementar `CarColor`, `Autopilot` y `Spawns.vehicles`.**
- [ ] **Step 4: Run** `./gradlew test --console=plain` → Esperado: PASS.
- [ ] **Step 5: Commit** `git add frutiger-aero-city/src && git commit -m "feat(frutiger): piloto automático de carriles, colores Y2K y puntos de aparición de coches"`

---

### Task 8: `AeroCarEntity`, ítems, modelo, render y aparición al generar chunks

**Files:**
- Create: `entity/AeroCarEntity.java`, `item/AeroCarItem.java`, `client/AeroCarModel.java`, `client/AeroCarRenderer.java`
- Modify: `entity/ModEntities.java`, `item/ModItems.java`, `client/ClientSetup.java`, `world/CityChunkGenerator.java`, `tools/gen_textures.py`, lang
- Create: `assets/frutigeraero/models/item/aero_car_<color>.json` (7 archivos: `{"parent":"minecraft:item/generated","textures":{"layer0":"frutigeraero:item/aero_car_<color>"}}`)
- Modify: `tools/smoke/datapack/data/smoke/function/load.mcfunction`, añade `summon_car.mcfunction` y `check_car.mcfunction`, y modifica `expected.txt`

**Interfaces:**
- Consumes: `Autopilot`, `CarColor`, `Heading`, `Spawns.vehicles`, `CityChunkGenerator.layoutOf`.
- Produces:
  - `ModEntities.AERO_CAR` (id `aero_car`, `MobCategory.MISC`, `sized(1.8F, 1.6F)`, `clientTrackingRange(10)`).
  - `AeroCarEntity extends Entity`: `public void setup(CarColor color, Heading heading, boolean autopilot)` y `public CarColor color()`.
  - `ModItems.CARS` (`Map<CarColor, RegistryObject<Item>>`, ids `aero_car_<color.id()>`, registrados en bucle) y `static Item ModItems.carItem(CarColor)`.
  - `AeroCarItem(CarColor color, Item.Properties props)`, con `stacksTo(1)`.

**`AeroCarEntity`, comportamiento** (constantes: `CRUISE=0.25`, `MAX=0.7`, `ACCEL=0.025`, `BRAKE=0.05`, `REVERSE_MAX=0.2`, `COAST=0.96`, `TURN_DEG=4.5F`, `SEAT_Y=0.3` (ajustable), `GRAVITY=0.04`):
- Datos sincronizados: `DATA_COLOR` (INT, ordinal de `CarColor`). NBT: `"Color"` (id), `"Autopilot"` (boolean), `"Heading"` (id).
- `pilot` **nunca es `null`**: el constructor crea `new Autopilot(Heading.SOUTH)`, y `setup(...)` y `readAdditionalSaveData` lo sustituyen por `new Autopilot(Heading.byId(...))`. Así funciona también el `/summon` con NBT de la prueba de humo.
- `interact(player, hand)`: si `player.isSecondaryUseActive()` → `PASS`. En servidor: `autopilot=false` y `player.startRiding(this)`. Devuelve `InteractionResult.sidedSuccess(level().isClientSide)`.
- `hurt(source, amount)`: en cliente o si ya está eliminado → `true`. Si `source.getEntity() instanceof Player p`: si **no** está en creativo, `spawnAtLocation(new ItemStack(ModItems.carItem(color())))`; después `discard()` y `true`. Cualquier otro daño → `false`.
- `getControllingPassenger()` → el primer pasajero si es un `Player`; si no, `null`. `canAddPassenger` → `getPassengers().size() < 2`.
- `getPassengerAttachmentPoint(...)` → `new Vec3(index==0 ? 0.35 : -0.35, SEAT_Y, 0).yRot(-getYRot() * Mth.DEG_TO_RAD)`.
- `isPickable()` y `canBeCollidedWith()` → `true`; `maxUpStep()` → `1.0F`; `getPickResult()` → el ítem de su color.
- Interpolación en cliente como `Boat`: `lerpTo(...)` guarda el destino con `lerpSteps = 10`; `tickLerp()` (si `isControlledByLocalInstance()` → `lerpSteps=0` y `syncPacketPositionCodec(getX(),getY(),getZ())`; si `lerpSteps>0` → `lerpPositionAndRotationStep(lerpSteps, lerpX, lerpY, lerpZ, lerpYRot, getXRot())` y `lerpSteps--`). `lerpTargetX/Y/Z/YRot` devuelven el destino mientras `lerpSteps>0`.
- `tick()`:
```
super.tick(); tickLerp();
if (isControlledByLocalInstance()) {
    if (getControllingPassenger() instanceof Player p) drivePlayer(p.zza, p.xxa);
    else if (!level().isClientSide && autopilot) driveAutopilot();
    else speed *= COAST;
    vy = onGround() ? 0 : getDeltaMovement().y - GRAVITY;
    (vx, vz) los fija el modo de conducción; setDeltaMovement(vx, vy, vz); move(MoverType.SELF, getDeltaMovement());
} else setDeltaMovement(Vec3.ZERO);
```
- `drivePlayer(forward, strafe)`: `forward>0` → `speed=min(MAX, speed+ACCEL)`; `forward<0` → `speed=max(-REVERSE_MAX, speed-BRAKE)`; sin tecla → `speed*=COAST`. Giro: si `|speed|>0.01` → `setYRot(getYRot() - strafe*TURN_DEG*min(1,|speed|/0.2)*signum(speed))`. Dirección: `(-sin(yaw), 0, cos(yaw)) * speed`.
- `driveAutopilot()`: `layout = CityChunkGenerator.layoutOf((ServerLevel) level())`. Si es `null` → `autopilot=false` y return (Review Focus 5). Después `pilot.update(layout, getX(), getZ(), random.nextDouble())`. Si `!pilot.onRoad()` → `speed=0`. Obstáculo: `AABB ahead = getBoundingBox().move(h.dx()*2.5, 0, h.dz()*2.5)` con algún `LivingEntity` o `AeroCarEntity` distinto de sí mismo → objetivo 0 y `blockedTicks++`. Si no, objetivo `CRUISE` y `blockedTicks=0`. Si `blockedTicks>100` → ignora obstáculos 40 ticks (`creepTicks=40`, evita atascos). Ajusta `speed` hacia el objetivo con `±ACCEL`. Velocidad: avance `h.dx()*speed`, `h.dz()*speed` más una corrección lateral `clamp((pilot.laneCoord(x,z) - lateralActual)*0.2, -0.1, 0.1)` en el eje perpendicular. Yaw: `setYRot(Mth.approachDegrees(getYRot(), h.yaw(), 9F))`.
- `setup(...)`: guarda el color en `DATA_COLOR`, crea `pilot = new Autopilot(heading)` y guarda `autopilot`.

**`AeroCarItem.useOn(ctx)`**: solo en servidor. `pos = clickedPos.relative(clickedFace)`; crea el coche con `setup(color, Heading.SOUTH, false)` y `moveTo(pos.x+0.5, pos.y, pos.z+0.5, player yaw, 0)`. Si `!level.noCollision(car, car.getBoundingBox())` → `FAIL`. Si no: `addFreshEntity`, reduce el stack si no está en creativo y devuelve `CONSUME`.

**`AeroCarModel`** (clase normal con `ModelPart body, glass, wheels` y `LAYER = new ModelLayerLocation(rl("aero_car"), "main")`). `createLayer()` con textura de **256×128**. Todos los cubos usan `texOffs(0,0)` (las texturas son uniformes, así que el UV no importa). El suelo está en y=24 del espacio del modelo:
- `body`: `addBox(-14,10,-26, 28,10,52)`, `addBox(-12,-2,-6, 24,2,24)`, `addBox(-15,15,-28, 30,4,2)` y `addBox(-15,15,26, 30,4,2)`
- `glass`: `addBox(-12,0,-8, 24,10,28)`
- `wheels`: `addBox(-15,16,-21, 3,8,8)`, `addBox(12,16,-21, 3,8,8)`, `addBox(-15,16,13, 3,8,8)` y `addBox(12,16,13, 3,8,8)`

**`AeroCarRenderer extends EntityRenderer<AeroCarEntity>`** (`shadowRadius = 0.9F`). En `render`: `pushPose`; `mulPose(Axis.YP.rotationDegrees(180F - yaw))`; `scale(-1,-1,1)`; `translate(0,-1.501,0)`. Después: `body.render(..., entityCutoutNoCull(BODY), light, OverlayTexture.NO_OVERLAY, car.color().argb())`, `wheels.render(..., entityCutoutNoCull(WHEEL), ..., 0xFFFFFFFF)` y `glass.render(..., entityTranslucent(GLASS), ..., 0xFFFFFFFF)`. Termina con `popPose` y `super.render`. `getTextureLocation` → BODY. Regístralo junto a la capa en `ClientSetup` (`RegisterLayerDefinitions` → `AeroCarModel.LAYER`, `AeroCarModel::createLayer`).

**Texturas (añadir a `gen_textures.py`)**:
- `textures/entity/aero_car/body.png` 256×128 en `(245,248,250,255)`; `glass.png` 256×128 en `(170,230,255,140)`; `wheel.png` 64×64 en `(40,44,52,255)`.
- `textures/item/aero_car_<id>.png` 16×16, fondo transparente: carrocería en filas 8..11 y columnas 1..14 = color del coche; techo en fila 4, columnas 5..10 = color; cabina en filas 5..7, columnas 4..11 = `(170,230,255)`; faro en `(14,9)` = `(255,250,200)`; ruedas en filas 11..13, columnas 3..5 y 10..12 = `(40,44,52)`, con buje en `(4,12)` y `(11,12)` = `(200,205,210)`.

**Aparición al generar chunks**: en `spawnOriginalMobs`, después de los ciudadanos, para cada `VehicleSpawn`: `create`, `setup(color, heading, true)`, `moveTo(x, y, z, heading.yaw(), 0)` y `region.addFreshEntity(car)`.

**Lang**: `entity.frutigeraero.aero_car` = `Aero Car` / `Coche Aero`; `item.frutigeraero.aero_car_<id>` = `Aero Car (Bondi Blue|Blueberry|Grape|Tangerine|Lime|Strawberry|Snow)` / `Coche Aero (Azul Bondi|Arándano|Uva|Mandarina|Lima|Fresa|Nieve)`. Añade los 7 ítems a la pestaña creativa.

- [ ] **Step 1: Ampliar la prueba de humo (la que falla)**
`load.mcfunction` queda así:
```
forceload add 0 0 63 63
schedule function smoke:summon_car 40t
schedule function smoke:check_car 140t
schedule function smoke:check 240t
```
`summon_car.mcfunction`: `summon frutigeraero:aero_car 4.0 65 20.5 {Tags:["smoke"],Autopilot:1b,Heading:"south"}`
`check_car.mcfunction`:
```
execute if entity @e[type=frutigeraero:aero_car,tag=smoke,x=0,y=60,z=30,dx=12,dy=10,dz=30] run say SMOKE_OK_car_moved
execute unless entity @e[type=frutigeraero:aero_car,tag=smoke,x=0,y=60,z=30,dx=12,dy=10,dz=30] run say SMOKE_FAIL_car_moved
execute if entity @e[type=frutigeraero:aero_car,tag=!smoke] run say SMOKE_INFO_natural_cars
```
Añade `SMOKE_OK_car_moved` a `expected.txt` (`SMOKE_INFO_*` es informativo; puede no aparecer).
- [ ] **Step 2: Run** `bash tools/smoke.sh` → Esperado: `SMOKE FAILED` (falta `SMOKE_OK_car_moved`).
- [ ] **Step 3: Generar las texturas.** Run: `python3 tools/gen_textures.py && file src/main/resources/assets/frutigeraero/textures/item/aero_car_lime.png` → Esperado: `PNG image data, 16 x 16, 8-bit/color RGBA`.
- [ ] **Step 4: Implementar** `AeroCarEntity`, `AeroCarItem`, `AeroCarModel`, `AeroCarRenderer` y los registros, y añadir los coches a `spawnOriginalMobs`.
- [ ] **Step 5: Run** `./gradlew build --console=plain && bash tools/smoke.sh` → Esperado: `BUILD SUCCESSFUL` y `SMOKE PASSED`.
- [ ] **Step 6: Commit y push (fin de la Fase 3)**
```bash
git add frutiger-aero-city
git commit -m "feat(frutiger): coches burbuja Y2K conducibles con piloto automático por carriles"
git push -u origin <tu-rama>
```

---

### Task 9: README, verificación final y checklist manual

**Files:**
- Create: `frutiger-aero-city/README.md`

- [ ] **Step 1: `README.md`** (en español), con estas secciones: *Qué es* · *Cómo compilar* (`./gradlew build` con Java 21 → `build/libs/frutigeraerocity-0.1.0.jar`, o el artefacto `frutiger-aero-city-mod` de GitHub Actions) · *Crear el mundo* (Un jugador → Crear mundo → pestaña Mundo → botón **Tipo de mundo** → `FRUTIGER AERO CITY`) · *Ciudadanos* (clic derecho para hablar) · *Coches* (clic derecho sobre un bloque con el ítem para colocarlo; clic derecho para subir; W/S acelerar y frenar; A/D girar; Shift para bajar; golpear para recogerlo) · *Notas* (no hay monstruos ni estructuras vanilla; para desactivar los phantoms: `/gamerule doInsomnia false`).
- [ ] **Step 2: Verificación completa**
Run: `./gradlew clean build --console=plain && bash tools/smoke.sh`
Esperado: `BUILD SUCCESSFUL`, todos los tests en verde y `SMOKE PASSED`.
- [ ] **Step 3: Checklist manual para el usuario** (cópialo al final del README como "Prueba en el juego"): ☐ el botón Tipo de mundo muestra `FRUTIGER AERO CITY` ☐ apareces junto a la plaza con la fuente-orbe ☐ cielo azul claro, agua cian y hierba lima ☐ de noche las torres y farolas brillan ☐ los ciudadanos pasean por las aceras y hablan con clic derecho ☐ los coches circulan por su carril, giran en los cruces y frenan ante peatones ☐ puedes conducir un coche y dejarlo ☐ en un mundo **normal**, el huevo de ciudadano y el ítem de coche funcionan sin crashear (Review Focus 5) ☐ al salir y volver a entrar siguen los ciudadanos (con su skin y nombre) y los coches (con su color).
- [ ] **Step 4: Commit y push**
```bash
git add frutiger-aero-city/README.md
git commit -m "docs(frutiger): README de uso y checklist de prueba en el juego"
git push -u origin <tu-rama>
```

---

## Apéndice A — API de Minecraft 1.21.1 / Forge 52.1.0 (firmas verificadas)

Comprobadas con `javap` contra el jar real que usa la compilación (Gradle 9.3.0, primera compilación ≈ 6–7 min).

**Cómo comprobar una firma tú mismo** (después de la primera compilación de la Tarea 1):
```bash
JAR=~/.gradle/caches/minecraftforge/forgegradle/mavenizer/caches/forge/net/minecraftforge/forge/1.21.1-52.1.0/official/1.21.1/recompiled.jar
javap -cp "$JAR" -p net.minecraft.world.level.chunk.ChunkGenerator | grep -E "abstract|fillFromNoise"
# Anotaciones de FML (Mod$EventBusSubscriber):
find ~/.gradle/caches/modules-2 -name "javafmllanguage-1.21.1-52.1.0.jar"
```

**ChunkGenerator** (`net.minecraft.world.level.chunk`). Constructor `ChunkGenerator(BiomeSource)`. Métodos abstractos que hay que implementar:
```java
protected MapCodec<? extends ChunkGenerator> codec()
public void applyCarvers(WorldGenRegion, long seed, RandomState, BiomeManager, StructureManager, ChunkAccess, GenerationStep.Carving)
public void buildSurface(WorldGenRegion, StructureManager, RandomState, ChunkAccess)
public void spawnOriginalMobs(WorldGenRegion)
public int getGenDepth()
public CompletableFuture<ChunkAccess> fillFromNoise(Blender, RandomState, StructureManager, ChunkAccess)   // SIN Executor en 1.21.1
public int getSeaLevel()
public int getMinY()
public int getBaseHeight(int x, int z, Heightmap.Types, LevelHeightAccessor, RandomState)
public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor, RandomState)
public void addDebugScreenInfo(List<String>, RandomState, BlockPos)
```
Sobrescribibles: `public int getSpawnHeight(LevelHeightAccessor)`, `public ChunkGeneratorStructureState createState(HolderLookup<StructureSet>, RandomState, long seed)`, `public void applyBiomeDecoration(WorldGenLevel, ChunkAccess, StructureManager)`. Getter: `getBiomeSource()`.

**Generación del mundo, resto**
- `ChunkGeneratorStructureState.createForFlat(RandomState, long, BiomeSource, Stream<Holder<StructureSet>>)` (estático)
- `NoiseColumn(int minY, BlockState[] states)`
- `ChunkAccess`: `BlockState setBlockState(BlockPos, BlockState, boolean)`, `Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types)`, `int getMinBuildHeight()`, `int getMaxBuildHeight()`, `ChunkPos getPos()` → `getMinBlockX()` / `getMinBlockZ()`
- `Heightmap`: `boolean update(int localX, int y, int localZ, BlockState)`
- `RandomState.getOrCreateRandomFactory(ResourceLocation) -> PositionalRandomFactory`; `PositionalRandomFactory.at(int,int,int) -> RandomSource`
- `ServerLevel.getChunkSource()` (`ServerChunkCache`): `ChunkGenerator getGenerator()`, `RandomState randomState()`
- `WorldGenRegion`: `ChunkPos getCenter()`, `ServerLevel getLevel()`, `long getSeed()`, `void addFreshEntityWithPassengers(Entity)` (heredado de `ServerLevelAccessor`)
- `LeavesBlock.PERSISTENT` (`BooleanProperty`)
- `ResourceLocation.fromNamespaceAndPath(String, String)` (el constructor es privado en 1.21)
- `DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID)`; **`ForgeRegistries.CHUNK_GENERATORS` NO EXISTE**

**Entidades**
- `Entity`: `protected abstract void defineSynchedData(SynchedEntityData.Builder)`, `protected abstract void readAdditionalSaveData(CompoundTag)`, `protected abstract void addAdditionalSaveData(CompoundTag)`, `public InteractionResult interact(Player, InteractionHand)`, `public boolean hurt(DamageSource, float)`, `public LivingEntity getControllingPassenger()`, `public boolean isControlledByLocalInstance()`, `protected boolean canAddPassenger(Entity)`, `protected Vec3 getPassengerAttachmentPoint(Entity, EntityDimensions, float)`, `public boolean isPickable()`, `public boolean canBeCollidedWith()`, `public float maxUpStep()`, `public ItemStack getPickResult()`, `public void lerpTo(double,double,double,float,float,int)`, `protected void lerpPositionAndRotationStep(int,double,double,double,double,double)`, `public double lerpTargetX()` (y `Y`, `Z`, `XRot`, `YRot`), `public void syncPacketPositionCodec(double,double,double)`, `public boolean onGround()`, `public ItemEntity spawnAtLocation(ItemStack)`, `public void tick()`. `positionRider(Entity)` es `final`: **no la sobrescribas**; usa `getPassengerAttachmentPoint`.
- `Mob`: `public SpawnGroupData finalizeSpawn(ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData)`, `public boolean removeWhenFarAway(double)`, `protected InteractionResult mobInteract(Player, InteractionHand)`, `protected void registerGoals()`, `public static AttributeSupplier.Builder createMobAttributes()`
- `LivingEntity`: campos públicos `float xxa` (lateral, +izquierda) y `float zza` (avance)
- `Player`: `boolean isLocalPlayer()`, `boolean isSecondaryUseActive()`, `Abilities getAbilities()`
- `EntityType.Builder`: `static of(EntityFactory<T>, MobCategory)`, `sized(float,float)`, `build(String id)` (**recibe un String** en 1.21.1)
- `DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID)` (existe)
- `EntityAttributeCreationEvent.put(EntityType<? extends LivingEntity>, AttributeSupplier)`
- `ForgeSpawnEggItem(Supplier<? extends EntityType<? extends Mob>>, int bg, int fg, Item.Properties)`
- `Level.noCollision(Entity, AABB)` (por defecto, de `CollisionGetter`) · `InteractionResult.sidedSuccess(boolean)` · `Mth.approachDegrees(float,float,float)` · `Mth.DEG_TO_RAD`

**Cliente**
- `EntityRenderer<T>`: constructor `protected EntityRenderer(EntityRendererProvider.Context)`, `public void render(T, float yaw, float partialTick, PoseStack, MultiBufferSource, int packedLight)`, `public abstract ResourceLocation getTextureLocation(T)`
- `HumanoidMobRenderer(EntityRendererProvider.Context, M model, float shadowRadius)`
- `ModelPart.render(PoseStack, VertexConsumer, int light, int overlay, int argbColor)` (en 1.21 el color es un `int` ARGB, **no** cuatro `float`)
- `ModelLayers.PLAYER` · `com.mojang.math.Axis.YP`
- `EntityRenderersEvent.RegisterRenderers.registerEntityRenderer(EntityType<? extends T>, EntityRendererProvider<T>)`
- `EntityRenderersEvent.RegisterLayerDefinitions.registerLayerDefinition(ModelLayerLocation, Supplier<LayerDefinition>)`
- `@Mod.EventBusSubscriber(modid = ..., bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)` (existe en `javafmllanguage`)

## Apéndice B — Recuperación de errores comunes
| Síntoma | Causa | Arreglo |
|---|---|---|
| `Gradle 9.3.0 or newer is required` | wrapper antiguo | revisa el Step 1 de la Tarea 1 (`distributionUrl` 9.3.0) |
| `Permission denied: ./gradlew` | se perdió el bit de ejecución | `chmod +x gradlew` |
| `method does not override or implement a method from a supertype` | firma distinta en 1.21.1 | copia la firma del Apéndice A o de `javap`; **no** quites el `@Override` |
| Tests en rojo con coordenadas negativas | uso de `%` | `Math.floorMod` / `Math.floorDiv` |
| El servidor de humo no termina | `stop` sin permiso | comprueba `function-permission-level=4` en `run/server.properties` |
| `FALTA: SMOKE_OK_road` y en el log `Unknown/Failed ... level-type` | preset no encontrado | revisa la ruta `data/frutigeraero/worldgen/world_preset/` y que `ModWorldgen` esté registrado |
| No existe la tarea `runServer` | nombre distinto en FG7 | `./gradlew tasks --all \| grep -i run` y usa la tarea de servidor que aparezca |
| Las hojas desaparecen | sin `PERSISTENT` | revisa `BlockPalette` |
| El coche no se mueve en la prueba de humo | piloto automático desactivado o `layoutOf` es `null` | comprueba que `readAdditionalSaveData` lee `Autopilot` y `Heading`, y que `setup` crea `pilot` |
| Los ciudadanos no aparecen | `spawnOriginalMobs` vacío o `finalizeSpawn` lanza una excepción | busca `Exception` en `run/smoke.log` |
