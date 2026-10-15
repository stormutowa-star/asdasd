# Transformación (Minecraft 1.21.1 · Forge)

Mod que añade un item, **Transformación**. Al hacer clic derecho con él en la mano, el jugador
se transforma (y lo ven los demás jugadores): pelo largo con pinchos, rostro nuevo, alas
moradas detrás de la espalda y guanteletes con garras en las dos manos. Otro clic derecho la desactiva.

## Partes de la transformación

| Parte | Cómo está hecha en Minecraft |
|---|---|
| Rostro | Capa que cubre la cabeza con la textura del rostro (`head.png`) |
| Pelo | Flequillo sobre la frente, melena detrás de la cabeza y 3 pinchos (`head.png`) |
| Alas | Dos planos detrás de la espalda con movimiento de aleteo (`wings.png`) |
| Manos | Guanteletes sobre cada brazo con 3 garras en la punta (`hand_left.png`, `hand_right.png`) |

Las texturas salen de las imágenes de referencia reducidas a píxeles. Los modelos usan cajas de
Minecraft, así que se ven en tercera persona (F5) y en los demás jugadores.

## Obtenerlo

- Pestaña creativa **Transformación**, o
- Receta sin forma: 2 × membrana de phantom + 1 × estrella del Nether + 1 × rosa marchita.

Comando: `/give @p transformacion:transformacion`

## Compilar

`./gradlew build` (necesita Java 21) → `build/libs/transformacion-1.0.0.jar`.
Pon el jar en la carpeta `mods` de Forge 1.21.1 (la misma versión que Akagane).
