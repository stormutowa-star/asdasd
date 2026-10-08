# Rama Super Saiyan AF (SSJ1 → SSJ5) para DragonMineZ / Dragon Block Noea

Grupo de transformaciones nuevo para la raza **Saiyan** de DragonMineZ 2.1.3 (Forge 1.20.1), el mod base que usa Dragon Block Noea.
No toca el `.jar` de ningún mod: es un archivo de configuración y un resource pack con los nombres.

## Instalación

1. Copia `config/dragonminez/races/saiyan/forms/afsaiyan.json` a la misma ruta dentro de tu carpeta de Minecraft
   (en un servidor, en la carpeta del **servidor**; el servidor se la envía a los jugadores).
2. Pon `resourcepack/DMZ-AF-Saiyan.zip` en `resourcepacks/` y actívalo (si no, los nombres salen como `race.dragonminez...`).
3. Reinicia el juego/servidor, o usa `/dmzreload config`.
4. En el juego, cambia el grupo de transformación a **Super Saiyan AF**.

## Progresión

Usa el mismo árbol de skill `superforms` que las formas normales del Saiyan, así que **no hay que editar nada más**.

| Forma | Nivel de skill `superforms` | Además necesita |
|---|---|---|
| SSJ 1 (AF) | 1 | — |
| SSJ 2 (AF) | 5 | 25 % de maestría en SSJ 1 (AF) |
| SSJ 3 (AF) | 6 | 25 % de maestría en SSJ 2 (AF) |
| SSJ 4 (AF) | 8 | 25 % de maestría en SSJ 3 (AF) |
| SSJ 5 (AF) | 8 | **75 %** de maestría en SSJ 4 (AF) |

En creativo todo está desbloqueado.

## Aspecto

| Forma | Pelo | Cuerpo | Ojos | Aura | Rayos | Fuerza/Ki |
|---|---|---|---|---|---|---|
| SSJ 1 | tu peinado SSJ, dorado `#FFEDB3` | normal | cian | dorada | no | ×1.5 |
| SSJ 2 | tu peinado SSJ2, dorado | normal | cian | dorada (más grande) | sí | ×2.25 |
| SSJ 3 | tu peinado SSJ3 (sin cejas), dorado | normal | cian | dorada (más grande) | sí | ×3.0 |
| SSJ 4 | peinado SSJ4 de GT, de tu color natural | modelo `ssj4gt`, pelaje rojo `#9d1e31`, cola | dorados | dorada | no | ×3.75 |
| SSJ 5 | SSJ4 alargado en melena hasta la cintura, plateado `#E9EDF2` | modelo `ssj4gt`, pelaje blanco plateado `#DCE0E6`, cola | plateados `#9FAAB8` | blanco plateada | no | ×4.5 |

- SSJ1-3 en AF son los mismos que en Z, así que copian los valores de las formas Super Saiyan de DragonMineZ 2.1.3.
  El SSJ4 copia el `supersaiyan4` (versión GT) del mod. Con eso, la rama AF está equilibrada con la normal.
- El SSJ5 sigue el diseño de Toyble/Tablos: cuerpo de SSJ4 con pelaje blanco plateado, pelo largo plateado y cola.
  Los ojos y el aura varían según la versión fan; aquí son plateados.
- Los nombres internos (`afsupersaiyan2`, `afsupersaiyan3`, `afsupersaiyan4`, `afsupersaiyan5_ssj4`) son así a propósito.
  DragonMineZ da el aura grande del SSJ2/SSJ3 a las formas cuyo nombre contiene `supersaiyan2`/`supersaiyan3`,
  y siempre enseña la cola si el nombre contiene `supersaiyan4` o `ssj4`. No los cambies o se pierden esos efectos.

`previews/pelo_ssj4_vs_ssj5.png` es una vista aproximada del pelo, dibujada con la misma geometría que usa el mod (no es una captura del juego).

## Personalizar

- **Colores y poder:** edita los campos del JSON (`hairColor`, `bodyColor2` = pelaje, `eye1Color`/`eye2Color`, `auraColor`, `strMultiplier`…).
- **Pelo del SSJ5:** diseña uno en el editor de pelo del juego, copia el código y pégalo en `forcedHairCode`.
  O cambia el largo en `tools/make_ssj5.py` y regenera con:
  ```
  python3 tools/make_ssj5.py tools/ssj4.code > tools/ssj5.code
  python3 tools/gen_forms.py tools/ssj4.code tools/ssj5.code config/dragonminez/races/saiyan/forms/afsaiyan.json
  python3 tools/preview.py tools/ssj5.code vista.png "#d8dde6" "SSJ5"   # necesita matplotlib
  ```
- **SSJ5 en un nivel de skill propio (9):** en `afsaiyan.json` pon `"unlockOnSkillLevel": 9` en el SSJ5, y en
  `config/dragonminez/races/saiyan/character.json` añade un precio más a `formSkillsCosts.superforms.prices`
  (por ejemplo `130000` al final de la lista).

## Límites

- No se pueden añadir modelos 3D ni texturas nuevas sin programar en Java (la lista de modelos está fija en el mod),
  por eso el SSJ5 reutiliza el modelo del SSJ4 y cambia los colores.
- **No está probado dentro del juego.** Está comprobado contra el código fuente de DragonMineZ 2.1.3:
  todos los campos del JSON existen, y el código de pelo del SSJ5 se decodifica bien con el mismo algoritmo que usa el mod.
- No sé si Dragon Block Noea cambia algo de las formas del Saiyan; si lo hace, este grupo sigue siendo independiente.
