# Akagane (赤金) — mod para Minecraft 1.21.1 (Forge)

Zanpakutō original. Un solo item: **Akagane** (pestaña creativa "Akagane", o receta: bloque de redstone / lingote de netherita / vara de blaze en columna).

## Controles

| Forma | Acción |
|---|---|
| **Sellada** | Clic derecho (o chat: `Arde, Akagane.`) → **Shikai** |
| **Shikai** | Cada corte (golpe al aire, bloque o mob) deja una **marca** roja (máx. 5) |
| | Clic derecho → el corte más antiguo reaparece en el mismo sitio |
| | Agachado + clic derecho (o chat: `Gyakuzan`) → las 5 marcas a la vez |
| | Agachado + mirar al suelo + clic derecho (o chat: `Bankai`) → **Bankai** |
| **Bankai** | Cada corte deja marca automática (máx. 40) |
| | Clic derecho → **conecta** todas las marcas sueltas en un solo corte (líneas doradas) |
| | Agachado + clic derecho → libera los cortes conectados |
| | Agachado + mirar al cielo + clic derecho (o chat: `Guren Retsudan`) → todas las marcas cortan a la vez |
| | Agachado + mirar al suelo + clic derecho (o chat: `sellar`) → vuelve a sellada |

**Debilidad:** la barra bajo el icono es el reiatsu. Cada marca consume energía continuamente; si baja de ~8 % las marcas se disipan, y si llega a 0 la espada se sella sola.

## Compilar

**En GitHub (lo más fácil):** sube todo el contenido de esta carpeta a un repositorio → pestaña *Actions* → *Compilar Akagane* → cuando termine, baja el artefacto `akagane-mod` (contiene `akagane-1.0.0.jar`). Pon el jar en tu carpeta `mods` (Forge 1.21.1).

**En tu PC:** `gradlew build` (necesita Java 21) → `build/libs/akagane-1.0.0.jar`.
