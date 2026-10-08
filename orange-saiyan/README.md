# Orange Saiyan — mod para DragonMineZ / Dragon Block Noea

Una rama nueva de transformaciones para la raza **Saiyan** de DragonMineZ 2.1.3 (Forge 1.20.1).
Es un mod independiente: solo necesita DragonMineZ (no necesita la Saga de Makuo).

## Super Saiyan Orange (primera forma de la rama)

- Como el SSJ1, pero con **pelo, ojos y aura naranjas**.
- **Grietas de lava** que suben de las manos a los hombros y **palpitan** entre lava oscura y naranja brillante.
- Poder **×2.1**: entre el SSJ1 (×1.5) y el SSJ2 (×2.25), casi parejo al SSJ2.
- Se desbloquea con el **nivel 4** de la skill de transformaciones (`superforms`) y un **25 % de maestría en el SSJ1**.

## Instalación

1. Pon `orangesaiyan-1.0.0.jar` en la carpeta `mods`, junto a DragonMineZ.
2. Abre el juego: el mod crea `config/dragonminez/races/saiyan/forms/orangesaiyan.json` él solo.
   Si lo editas, el mod no lo vuelve a sobrescribir.

DragonMineZ solo muestra en el menú de transformaciones (tecla X) las ramas que ya tienes desbloqueadas.
Para probarla rápido (con trucos):

```
/dmzform set superforms 4
/dmzmastery set <tu nombre> ssgrades supersaiyan 25
```

## Por qué es un `.jar` y no solo un JSON

La forma en sí es un JSON de DragonMineZ, como cualquier otra. El `.jar` añade la textura de las grietas y el
código que las hace palpitar (Minecraft no permite animar texturas de personajes).

## Compilar

GitHub Actions (workflow *Compilar Orange Saiyan*) compila el mod y deja el jar en `orange-saiyan/dist/`.
En tu PC (Java 17): pon el jar de DragonMineZ 2.1.3 en `libs/dragonminez-2.1.3.jar` y ejecuta `gradlew build`.

## Licencia

GPL-3.0, igual que DragonMineZ.
