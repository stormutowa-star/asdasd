# Jugador 2 — diseño

- **Fecha:** 2026-10-15
- **Repo:** stormutowa-star/asdasd
- **Rama:** claude/gracious-mayer-yfybjh
- **Estado:** diseño aprobado en conversación; pendiente de revisión de este documento

## 1. Resumen

Mod para Minecraft 1.21.1 con Forge 52.1.0 (`jugador2`). Añade un **jugador real del servidor**, el Jugador 2, controlado por una IA integrada que decide por sí misma. Aparece en la lista de jugadores, tiene inventario, vida, hambre y XP, puede morir y reaparecer, y actúa con las mismas APIs que el juego usa para un jugador. No es un mob ni un `FakePlayer` de Forge.

## 2. Decisiones

| Tema | Decisión | Origen |
|---|---|---|
| Ubicación | Mod separado en `jugador2/`, con su propio build y su jar. Akagane no cambia. | Usuario |
| Tipo de IA | Integrada, sin LLM ni API externa en v1. | Usuario |
| Enfoque | Jugador falso real: `ServerPlayer` con conexión falsa, como hace Carpet. | Usuario |
| Carácter | Un "player 2" literal: piensa por sí mismo y puede morir. | Usuario |
| Alcance v1 | Supervivencia, crafteo básico, cofres, camas, chat y comandos. | Recomendación, aprobada en el diseño |
| Muerte | Suelta su inventario donde murió y reaparece en su spawn o cama, con inventario vacío, a los 5 s. | Interpretación de "si me muero sin items", aprobada en el diseño |
| Nombre | Se genera la primera vez que el mod arranca en un mundo: único en ese mundo y de 3 a 16 caracteres válidos para un usuario de Minecraft. | Usuario |
| Persistencia | Como cualquier jugador (`playerdata` por UUID), más un `SavedData` con nombre, UUID y estado. | Recomendación |
| Entrega | Jar como artefacto de GitHub Actions (`jugador2-mod`). | Usuario |

## 3. Fuera de alcance (v1)

- Pesca, comercio con aldeanos, encantamientos, Nether y End.
- Construcciones complejas o copiar estructuras.
- LLM o cualquier servicio externo.
- Skin propia del NPC: usa la skin por defecto de Minecraft.
- Interfaz gráfica propia: la interacción es por comandos y chat.
- Corregir el workflow "Compilar Akagane" que menciona el README de Akagane. No existe en el repo y queda fuera de esta entrega.

## 4. Requisitos funcionales

- **RF-1.** Al activarse, el NPC aparece en la lista de jugadores y el chat muestra su mensaje de unión.
- **RF-2.** Camina con búsqueda de rutas: sube 1 bloque, baja hasta 3 y salta. Evita lava y agua.
- **RF-3.** Mina bloques con la herramienta de su inventario. Los objetos caen y los recoge igual que un jugador.
- **RF-4.** Coloca bloques desde su inventario.
- **RF-5.** Come cuando el hambre baja de 14 y tiene comida.
- **RF-6.** Ataca mobs hostiles a menos de 6 bloques. Huye cuando la vida baja de 6 puntos con enemigos cerca.
- **RF-7.** Crea herramientas y bloques básicos cuando le faltan, con la mesa de crafteo y las recetas del juego.
- **RF-8.** Guarda el excedente de objetos en un cofre a menos de 16 bloques.
- **RF-9.** Duerme en una cama de noche si tiene una.
- **RF-10.** Muere y reaparece según la sección 8.
- **RF-11.** Obedece órdenes por chat y por comandos (sección 7.2).
- **RF-12.** Sobrevive a reinicios: si estaba activo al guardar, reaparece al arrancar el servidor.

## 5. Arquitectura

Proyecto Gradle separado en `jugador2/`, con `build.gradle`, `settings.gradle`, `gradle.properties` y wrapper propios. Usa las mismas versiones que Akagane: ForgeGradle 7, Minecraft 1.21.1, Forge 52.1.0 y Java 21. Paquete raíz `com.ricardo.jugador2`. Produce `jugador2-1.0.0.jar`.

La lógica pura (generador de nombres, A*, selección de tareas y parser de órdenes) no depende de tipos de Minecraft, para probarla con JUnit.

| Unidad | Responsabilidad | Depende de |
|---|---|---|
| `Jugador2Mod` | Punto de entrada. Registra eventos, comandos y configuración. | Todas |
| `Jugador2Config` | Valores configurables (sección 10). | — |
| `npc.NpcPlayer` | `ServerPlayer` con conexión falsa. Expone acciones de bajo nivel. | `npc.FakeConnection` |
| `npc.FakeConnection` | Conexión de red que no envía nada al cliente. | — |
| `npc.NpcManager` | Un NPC por mundo: crear, reaparecer, retirar y tick. | `NpcPlayer`, `NpcSavedData`, `NpcNameGenerator`, `ai.NpcBrain` |
| `npc.NpcSavedData` | Nombre, UUID y estado del NPC, guardados en el mundo. | — |
| `npc.NpcNameGenerator` | Genera nombres únicos y válidos. | — |
| `ai.NpcBrain` | Elige la tarea activa por prioridad. | `ai.task.*`, `nav.NpcNavigator`, `actions.NpcActions` |
| `ai.task.*` | `Survive`, `Defend`, `Order`, `Gather`, `Craft` y `Sleep`. Cada una implementa `tick` y devuelve RUNNING, SUCCESS o FAILURE. | `actions.NpcActions`, `nav.NpcNavigator`, `ai.WorldView` |
| `ai.WorldView` | Consultas al mundo que usan las tareas: bloques cercanos y entidades cercanas. | — |
| `nav.NpcNavigator` | A* sobre bloques. Devuelve la ruta o `null`. | `nav.BlockAccess` |
| `nav.BlockAccess` | Interfaz sobre el mundo. Permite probar el A* sin Minecraft. | — |
| `actions.NpcActions` | Minar, colocar, atacar, comer, cambiar de ranura, crafteo, cofre y cama. | `npc.NpcPlayer` |
| `commands.Jugador2Command` | Comandos `/jugador2`, solo para operadores. | `npc.NpcManager` |
| `chat.OrderParser` | Interpreta órdenes de chat. Función pura. | — |
| `selftest.SelfTestScenario` | Escenario de integración. Solo se activa con `-Djugador2.selftest=true`. | `npc.NpcManager` |

## 6. Flujo por tick

1. El evento de fin de tick del servidor llama a `NpcManager.tick()`.
2. Para cada NPC activo: si está muerto, pasa a la sección 8. Si no, continúa.
3. Cada `aiDecisionIntervalTicks` (10), `NpcBrain` reevalúa prioridades y puede cambiar de tarea.
4. Cada tick, la tarea activa decide su entrada: ruta a seguir, dirección de mirada y acción (minar, atacar, colocar o comer).
5. `NpcNavigator` calcula la ruta cuando la tarea la pide, con un presupuesto de nodos.
6. El movimiento aplica velocidad y salto con las físicas de jugador de Minecraft: gravedad y colisiones.
7. `NpcActions` ejecuta la acción elegida.
8. El resto del tick (hambre, regeneración, recogida de objetos, progreso de minado) lo procesa el juego como para cualquier jugador.

## 7. Comportamiento

### 7.1 Prioridades

De mayor a menor:

1. **Sobrevivir.** Come si el hambre baja de 14 y tiene comida. Huye si la vida baja de 6 puntos con enemigos a menos de 6 bloques.
2. **Defenderse.** Ataca al enemigo hostil más cercano a menos de 6 bloques.
3. **Órdenes.** Ejecuta la orden activa de un jugador.
4. **Autónomo**, en este orden:
   - Reunir madera si tiene menos de 16 troncos, luego piedra si tiene menos de 32, y después carbón.
   - Si no encuentra el recurso cerca, explora en un radio de 64 bloques.
   - Crear herramientas de piedra o de hierro cuando le falten.
   - Guardar el excedente en un cofre a menos de 16 bloques.
   - Dormir de noche si tiene una cama.

### 7.2 Órdenes

- **Por chat**, escribiendo su nombre al inicio del mensaje:
  - `Nombre, ven`: va hacia quien dio la orden.
  - `Nombre, sigue a <jugador>`
  - `Nombre, ve a <x> <y> <z>`
  - `Nombre, mina <bloque>`
  - `Nombre, para`
- El NPC responde en el chat como un jugador normal.
- Las órdenes no distinguen mayúsculas. Una orden no reconocida recibe una respuesta corta con la lista de órdenes.
- **Por comandos**, con `/jugador2` y nivel de permiso 2: `spawn`, `remove`, `status`, `follow <jugador>`, `goto <x> <y> <z>` y `stop`.

## 8. Vida, muerte, nombre y persistencia

- **Nombre.** `NpcNameGenerator` combina sílabas de una lista original, que no incluye nombres de personajes conocidos ni de cuentas reales. Reintenta si el nombre ya existe en el mundo o coincide con un jugador conectado. El nombre y el UUID se generan una sola vez y se guardan en `NpcSavedData`. Por eso no cambian dentro de un mundo, y cada mundo nuevo tiene nombre nuevo.
- **Persistencia.** Inventario, vida, XP y posición se guardan en el `playerdata` estándar, indexado por UUID.
- **Muerte.** Al morir, el juego suelta el inventario (con `keepInventory` desactivado) y el NPC sale de la lista de jugadores. El chat muestra el aviso de muerte como para cualquier jugador. Si `keepInventory` está activado, conserva el inventario, igual que un jugador.
- **Reaparición.** Tras `respawnDelayTicks` (100 ticks, 5 s) se crea un `NpcPlayer` nuevo con el mismo UUID y perfil. Aparece en su punto de spawn: la cama válida o el spawn del mundo. Como el juego ya vació el inventario, reaparece con inventario vacío.
- **Reinicio del servidor.** Con `spawnOnServerStart = true`, el NPC reaparece si estaba activo al guardar.

## 9. Errores y límites

- **Sin ruta o presupuesto agotado.** La tarea falla. `NpcBrain` elige otra, y el aviso en el log se limita a uno cada 30 s.
- **Excepción dentro de la IA.** Se captura por NPC y por tick. Ese NPC queda inactivo 200 ticks, se registra el error con su stack y el servidor sigue funcionando.
- **Inventario lleno.** Deja de recoger. Guarda en el cofre o descarta tierra y grava.
- **Sin herramienta adecuada.** Mina con la mano, como un jugador.
- **Rendimiento.** `pathSearchNodeLimit` acota cada búsqueda y solo se hace una búsqueda por tick.
- **Chunks.** El NPC mantiene chunks cargados como un jugador real. Se documenta en el README como coste de rendimiento.

## 10. Configuración

Archivo `jugador2-common.toml`:

| Clave | Valor por defecto | Significado |
|---|---|---|
| `enabled` | `true` | Activa el mod. |
| `spawnOnServerStart` | `true` | Reaparece el NPC al arrancar si estaba activo. |
| `aiDecisionIntervalTicks` | `10` | Cada cuántos ticks reevalúa prioridades. |
| `hostileDetectionRadius` | `6` | Radio para detectar enemigos, en bloques. |
| `eatHungerThreshold` | `14` | Come si el hambre baja de este valor. Escala de 0 a 20. |
| `fleeHealthThreshold` | `6` | Huye si la vida baja de este valor. 20 puntos son 10 corazones. |
| `respawnDelayTicks` | `100` | Espera antes de reaparecer. |
| `pathSearchNodeLimit` | `2000` | Nodos máximos por búsqueda de ruta. |

## 11. Pruebas

### 11.1 Unitarias (JUnit 5, `src/test/java`)

- **`NpcNameGeneratorTest`.** Longitud de 3 a 16 caracteres. Solo `[A-Za-z0-9_]`. Nunca vacío. Misma semilla, mismo nombre. Si el nombre ya está tomado, genera otro.
- **`NpcNavigatorTest`**, con un `BlockAccess` de prueba. Ruta en llano. Subida de 1 bloque. Bajada de 3 bloques. Rodeo de un muro. Sin ruta si está cerrado. Respeta `pathSearchNodeLimit`. Evita lava.
- **`NpcBrainTest`**, con estados simulados. Con vida baja y enemigo cerca, huye. Con hambre, come. Con una orden activa y sin amenazas, sigue la orden. Sin nada más, ejecuta tareas autónomas.
- **`OrderParserTest`.** Formas válidas. Ignora mayúsculas. Ignora órdenes con otro nombre. Una orden desconocida devuelve la ayuda.

### 11.2 En juego (`selftest`)

Servidor de desarrollo con `-Djugador2.selftest=true`. El escenario construye una plataforma plana de 20×20 en una zona despejada, con la dificultad en fácil. Desactiva el spawn natural de mobs con `doMobSpawning=false`; solo aparece el zombi del escenario. Cada paso tiene un tiempo límite:

1. El NPC aparece en la lista de jugadores. Límite: 40 ticks.
2. Camina 8 bloques hasta un punto marcado, y termina a 1.5 bloques o menos. Límite: 400 ticks.
3. Mina una piedra con pico de hierro. El bloque desaparece y entra piedra en el inventario. Límite: 200 ticks.
4. Coloca una piedra en un hueco. El bloque aparece. Límite: 100 ticks.
5. Con hambre en 6 y pan en el inventario, come y el hambre sube. Límite: 100 ticks.
6. Ataca a un zombi, y la vida del zombi baja o muere. Límite: 300 ticks.
7. Muere con `kill`. Suelta los objetos y reaparece con el mismo nombre y UUID, con inventario vacío, en `respawnDelayTicks + 40` ticks como máximo.

El escenario imprime `[JUGADOR2-SELFTEST] RESULT PASS` o `RESULT FAIL <paso>`, y detiene el servidor con código 0 o 1.

### 11.3 Manuales (lo prueba el usuario)

- Instalar el jar en `mods/` de un servidor o de un mundo con Forge 1.21.1.
- Comprobar que el NPC aparece en la lista de jugadores y que el mensaje de unión sale en el chat.
- Probar órdenes por chat y comandos, y ver que muere y reaparece.

## 12. Entrega

- `jugador2/` contiene el proyecto Gradle, `src/` y `README.md`. El README explica la instalación, los comandos y el coste de rendimiento.
- `.github/workflows/jugador2.yml` se ejecuta con cada push a `claude/gracious-mayer-yfybjh` que cambie `jugador2/**` o el propio workflow, y también a mano. Pasos:
  1. Instalar Java 21 (Temurin).
  2. Ejecutar `gradlew test` en `jugador2/`.
  3. Ejecutar `gradlew runSelftest` con `eula=true` en `run/selftest` y comprobar que aparece `RESULT PASS`.
  4. Ejecutar `gradlew build`.
  5. Subir `jugador2/build/libs/jugador2-1.0.0.jar` como artefacto `jugador2-mod`.
- Si el escenario falla, el workflow falla y no sube el artefacto.

## 13. Riesgos

- Las APIs internas de Minecraft (`ServerPlayer`, `PlayerList`, `ServerPlayerGameMode`) se usan como las usa Carpet. Los nombres y firmas exactos se verifican contra 1.21.1 al implementar. El escenario de CI es la prueba.
- Otros mods pueden asumir que todo jugador es humano. Se documenta en el README.
- Un jugador falso mantiene chunks cargados. En servidores grandes puede costar rendimiento.
- Sin skin propia: el NPC aparece con la skin por defecto.

## 14. Criterios de aceptación

- El workflow queda en verde: pruebas unitarias, escenario en juego en `PASS` y build.
- El artefacto `jugador2-mod` contiene `jugador2-1.0.0.jar`.
- La prueba manual de la sección 11.3 funciona en un mundo con Forge 1.21.1.
