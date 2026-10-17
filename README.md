# Mario — mod para Minecraft 1.21.1 (Forge)

Personaje Mario como entidad propia, con modelo detallado (gorra con la M, bigote, overol azul con botones, guantes blancos y zapatos marrones).

## Obtenerlo

Pestaña creativa **Mario** → *Huevo de Mario*. Úsalo sobre cualquier bloque para invocarlo.

## Comportamiento

- Camina por el mundo, mira a los jugadores y da saltitos.
- Si hay un monstruo a distancia, levanta los brazos y le lanza **bolas de fuego**.
- Si el monstruo está cerca, Mario se acerca y hace el **Super Jump Punch**: se agacha, salta con un golpe ascendente y suelta **monedas** en cada impacto. El último golpe lanza al monstruo con fuerza. Entre usos pasan 5 segundos y el aterrizaje no le hace daño.
- No ataca a los jugadores.

## Compilar

**En GitHub:** pestaña *Actions* → *Compilar Mario* → cuando termine, baja el artefacto `mario-mod` (contiene `mario-1.1.0.jar`). También queda publicado en la rama `mario-jar`. Ponlo en tu carpeta `mods` (Forge 1.21.1).

**En tu PC:** `gradlew build` (necesita Java 21) → `build/libs/mario-1.1.0.jar`.
