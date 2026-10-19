# El Leñador del Bosque

Mod de Minecraft 1.21.1 (Forge 52.x) con una historia de terror corta. Tomás, un leñador, vive en una cabaña en medio de una arboleda otoñal. Los animales del bosque aparecen muertos, y en el claro del ciervo empieza a descubrirse por qué.

## Compilar

Con GitHub Actions: cada `push` compila el mod y sube el `.jar` como artefacto (pestaña **Actions**).

En local (requiere Java 21 y Gradle 8.8):

    gradle build

El `.jar` queda en `build/libs/`.

## Cómo jugar

1. Usa la **Carta de Tomás** (pestaña de herramientas y utilidades del modo creativo). Te llevará a la arboleda.
2. Las decisiones aparecen en el chat como enlaces. Haz clic en la opción que quieras.
3. Hay tres decisiones y tres finales:
   - **El Pacto**: respetas al ciervo, guardas el brote de ámbar y lo plantas en el tocón.
   - **Cenizas**: profanas al ciervo y prendes fuego a la cabaña.
   - **El Relevo**: tomas el hacha de Tomás y el bosque sigue siendo tuyo.

## Comandos

- `/lenadorbosque elegir <id>`: lo usan los enlaces del chat. Solo acepta decisiones válidas en la etapa actual.
- `/lenadorbosque reiniciar`: borra el progreso de la historia (requiere permiso de operador, para pruebas).

## Estructura

- `story/`: la máquina de estados de la historia, decisiones y finales.
- `entity/`: Tomás (animado, invulnerable) y el ciervo caído.
- `world/GroveBuilder.java`: construye la arboleda, la cabaña y el tocón.
- `client/ClientSetup.java`: conecta las entidades con sus modelos de GeckoLib.
- `assets/`: modelos `.geo.json`, animaciones `.animation.json`, texturas, idioma.

## Limitaciones conocidas

- Los modelos y animaciones son bases escritas a mano en formato GeckoLib. Para una versión final conviene exportar los modelos desde MineAnimator o Blockbench y reemplazar los archivos de `geo/` y `animations/` manteniendo los mismos nombres.
- Los textos de la historia están en español fijo; el idioma de la interfaz solo cambia los nombres de objetos y entidades.
- La arboleda se genera donde estés al usar la carta, así que el terreno puede quedar irregular.
- Las versiones de Forge (52.0.40) y GeckoLib (4.5.7) en `gradle.properties` deben verificarse antes de la primera compilación.
