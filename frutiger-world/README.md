# Frutiger World (Minecraft 1.21.1, Forge)

Tipo de mundo con estética Frutiger Aero: cielo aqua, césped verde-lima, agua turquesa,
cristal azul, blanco brillante y burbujas que suben por el aire.

## Cómo se usa

Al crear un mundo nuevo, en **Tipo de mundo** elige **Frutiger World** (en el selector de mundos también aparece con ese nombre).

## Atmósfera

- Cielo aqua, niebla blanca-azulada, agua turquesa y césped lima.
- Burbujas que suben despacio por el aire.
- Conejos, loros y abejas además de los animales de la llanura.

Los shaders (luz cálida y brillos tipo solarpunk) no vienen con el mod: son un pack aparte que se activa en el cliente con un cargador de shaders como Iris u OptiFine.

## Estructuras

| Estructura | Qué es | Rareza |
|---|---|---|
| Pabellón de burbuja | Cúpula de cristal aqua de 9×9 con franjas de brillo, suelo de damero cian y cuarzo, estanque central y faroles en las esquinas. Entrada de 3×3. | 1 de cada 40 chunks |
| Fuente | Plaza de 9×9 con borde cian, estanque de 5×5, surtidor con burbuja de cristal y bancos. | 1 de cada 30 |
| Torre mirador | Torre blanca de 14 bloques con ventanas aqua en bandas, escalera interior, barandilla de cristal y faro en lo alto. | 1 de cada 36 |
| Aerogenerador | Torre de 15 bloques con góndola, cubo cian y tres aspas de 9 bloques. | 1 de cada 55 |
| Torre de burbujas | Pilar blanco anclado al suelo que sostiene una burbuja grande con luz y dos burbujas laterales unidas por brazos. | 1 de cada 30 |
| Palmeras | Grupo de hasta tres palmeras tropicales. | 1 de cada 10 |

Todas las estructuras se levantan sobre una plataforma plana con la puerta a un escalón del terreno. No se generan sobre agua ni con más de 4 bloques de desnivel, y nada flota: cada torre y cada burbuja están unidas a su base por bloques.

## Compilar

El workflow `.github/workflows/frutiger-world.yml` compila el mod, arranca un servidor Forge con él para comprobar que el mundo genera sin errores, y deja el jar en `frutiger-world/dist/` y en los artefactos de la ejecución.

En tu PC: `./gradlew build` (necesita Java 21) → `build/libs/frutigerworld-1.0.0.jar`.
