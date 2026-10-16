# Mario — mod para Minecraft 1.21.1 (Forge)

Personaje Mario como entidad propia, con modelo detallado (gorra con la M, bigote, overol azul con botones, guantes blancos y zapatos marrones).

## Obtenerlo

Pestaña creativa **Mario** → *Huevo de Mario*. Úsalo sobre cualquier bloque para invocarlo.

## Comportamiento

- Camina por el mundo, mira a los jugadores y da saltitos.
- Si hay un monstruo cerca, levanta los brazos y le lanza **bolas de fuego**.
- No ataca a los jugadores.

## Compilar

**En GitHub:** pestaña *Actions* → *Compilar Mario* → cuando termine, baja el artefacto `mario-mod` (contiene `mario-1.0.0.jar`). Ponlo en tu carpeta `mods` (Forge 1.21.1).

**En tu PC:** `gradlew build` (necesita Java 21) → `build/libs/mario-1.0.0.jar`.
