# La Saga de Makuo — addon para DragonMineZ / Dragon Block Noea

Una saga nueva de unas 2 horas para **DragonMineZ 2.1.3** (Forge 1.20.1), el mod base de Dragon Block Noea.
Makuo, un namekiano desterrado hace 600 años, vuelve con su ejército de Zhar, sus tres Heraldos
y su forma final: **Alas del Vacío**, piel violeta y alas de cuchillas de luz al estilo del Heilig Flügel.

La historia completa, los personajes y las 15 misiones están en [HISTORIA.md](HISTORIA.md).

## Qué añade

- **Saga de Makuo**: 15 misiones en el árbol de sagas de DragonMineZ. Se desbloquea al terminar la Saga de Buu.
- **9 enemigos nuevos**: Guerrero Zhar, Zhar de Élite, Garuk, Seiryn, Vokkar, Drakhul (con transformación en gigante)
  y Makuo (con transformación a Alas del Vacío). Usan la IA, los ataques de ki, los combos y las transformaciones
  en mitad del combate de los villanos de DragonMineZ.
- **Planeta Vharos**: dimensión nueva con hierba morada, mares de amatista y cielo violeta. Tiene el campamento de Saien
  donde aterriza la nave y la **Ciudadela de Makuo**, unos 320 bloques al norte.
- **Destino nuevo en la nave espacial**: Vharos. Se desbloquea al completar la misión 5.
- **Fragmento del Vacío**: lo sueltan los Zhar de Élite.
- **Saien**: NPC aliado en Vharos, con diálogos propios.

## Instalación

1. Descarga `makuosaga-1.0.0.jar` (ver «Compilar» abajo).
2. Ponlo en la carpeta `mods` de tu modpack de Noea, junto a DragonMineZ. No hace falta nada más.
3. Abre el mundo. Al arrancar, el addon copia la saga a `<mundo>/dragonminez/sagas` y `<mundo>/dragonminez/quests/saga_makuo`.

En un servidor, el `.jar` va en el servidor **y** en los jugadores (añade modelos y texturas).

## Probarla sin jugar todo DragonMineZ

Con trucos activados (u OP):

```
/dmzquest startsaga makuo_saga
```

Las misiones piden nivel de personaje (de 2340 a 2540, como las sagas oficiales después de Buu).
Para probar rápido, usa los comandos de DragonMineZ de stats o `/dmzquest finish <misión>` para saltar misiones.

## Compilar

El `.jar` se compila con **GitHub Actions** (workflow *Compilar La Saga de Makuo*):
pestaña **Actions** → la última ejecución en verde → artefacto **makuosaga-mod**.

En tu PC (Java 17): pon el jar de DragonMineZ 2.1.3 en `libs/dragonminez-2.1.3.jar` y ejecuta `gradlew build`.
El resultado queda en `build/libs/`.

## Cómo está hecho

- `src/main/java/.../entity/MakuoSagaEntities.java`: los villanos, que extienden la clase de enemigo de saga de DragonMineZ.
- `src/main/java/.../world/SagaInstaller.java`: copia los JSON de la saga al mundo antes de que DragonMineZ los cargue.
- `src/main/java/.../world/VharosBuilder.java`: construye el campamento y la Ciudadela la primera vez que alguien llega a Vharos.
- `src/main/resources/makuosaga_story/`: los JSON de la saga (mismo formato que las sagas oficiales).
- `src/main/resources/data/makuosaga/`: dimensión, bioma, destino de la nave y botín.
- `tools/`: scripts que generan las texturas, el modelo con alas, las misiones y los idiomas. Si cambias algo, vuelve a ejecutarlos:

```
python3 tools/gen_textures.py <assets de dragonminez> src/main/resources/assets/dragonminez/textures/entity/sagas
python3 tools/gen_wings_model.py <assets de dragonminez> src/main/resources/assets/dragonminez/geo/entity/sagas
python3 tools/gen_story.py src/main/resources
python3 tools/gen_icons.py src/main/resources
```

## Licencia

GPL-3.0, igual que DragonMineZ. Los modelos (`saga_piccolo`, `saga_slug`) y las animaciones son de DragonMineZ.
Las texturas de este addon son recoloreados de las suyas, y el modelo con alas parte de su modelo de Piccolo.
