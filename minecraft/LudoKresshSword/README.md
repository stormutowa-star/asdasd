# Ludo Kressh Sword — mod para Minecraft 1.21.1 (Forge)

La misma espada del mod de Skyrim (`skyrim/LudoKresshSword`), con el mismo modelo 3D y las mismas
stats.

## Instalar

1. Instala **Forge para Minecraft 1.21.1**.
2. Copia `dist/ludokressh-1.0.0.jar` en la carpeta `mods` de tu `.minecraft`.
3. La espada está en la pestaña **Combate** del modo creativo, o se puede fabricar (abajo).

## El modelo

Es exactamente la malla del `.nif` de Skyrim: los mismos 10.593 vértices y 20.400 triángulos, las
mismas UV y la misma textura de 2048×512. Se carga con el cargador de modelos OBJ que trae Forge
(`forge:obj`), así que no es una versión "pixelada": se ve igual que en Skyrim. Está colocada como
cualquier espada de Minecraft (empuñadura abajo a la izquierda en el inventario) y en la mano usa
las mismas posiciones que las espadas vanilla.

## Stats (las de Skyrim)

| | Skyrim | Minecraft |
|---|---|---|
| Daño | 24 | 24 de daño de ataque |
| Velocidad | 1.0 (normal de espada) | 1.6, la normal de cualquier espada |
| Durabilidad | las armas no se rompen | irrompible |
| Peso / valor | 14 / 3500 | se muestran en la descripción (Minecraft no tiene peso ni precio) |

**Veneno Sith**, en cada golpe:
- *Veneno de Ludo Kressh:* 12 puntos de daño por segundo durante 5 segundos (60 en total).
  Ignora la armadura, como el veneno de Skyrim.
- *Toxina Sith:* drena 10 puntos de aguante por segundo durante 5 segundos. En Minecraft el aguante
  es la barra de hambre: a los jugadores les quita 2 puntos de comida por segundo (el mismo 10 % por
  segundo que en Skyrim, donde el aguante base es 100). Los mobs no tienen hambre, así que la
  toxina los ralentiza mientras dura.
- Los no muertos (zombis, esqueletos, etc.) son inmunes, igual que los draugr en Skyrim.

## Fabricarla

Receta sin forma en la mesa de crafteo, con los equivalentes de los ingredientes de la forja de
Skyrim:

| Skyrim | Minecraft |
|---|---|
| 3 lingotes de ébano | 3 lingotes de netherita |
| 1 corazón de daedra | 1 corazón del mar |
| 2 campanillas de la muerte | 2 rosas marchitas |
| 2 tiras de cuero | 2 cueros |

## Compilar

GitHub compila el mod solo: cada cambio en esta carpeta ejecuta el workflow
**Ludo Kressh Sword (Minecraft)**, que deja el `.jar` en `dist/`. En tu PC: `gradlew build`
(Java 21) genera `build/libs/ludokressh-1.0.0.jar`.

`tools/export_obj.py` regenera el modelo OBJ a partir del `.nif` del mod de Skyrim.
