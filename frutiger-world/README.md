# Frutiger World (Minecraft 1.21.1, Forge)

Tipo de mundo con estética Frutiger Aero: cielo aqua, césped verde-lima, agua turquesa,
cristal azul, blanco brillante y estructuras limpias y luminosas.

## Cómo se usa

Al crear un mundo nuevo, en **Tipo de mundo** elige **Frutiger World** (en el selector de mundos también aparece con ese nombre).

## Estructuras

| Estructura | Qué es | Rareza |
|---|---|---|
| Pabellón de burbuja | Cúpula de cristal aqua con franjas de brillo blancas, suelo de cuarzo, farolas marinas. Entrada de 3 de ancho por 3 de alto. | 1 de cada 40 chunks |
| Fuente | Plaza de cuarzo con estanque de agua enmarcado y cuatro farolas. | 1 de cada 30 |
| Torre mirador | Torre blanca de 12 bloques con ventanas aqua, escalera de mano interior y pasarela en lo alto. | 1 de cada 36 |
| Aerogenerador | Torre blanca de 17 bloques con rotor de tres aspas. | 1 de cada 55 |
| Racimo de burbujas | Esferas de cristal flotando a 12-19 bloques del suelo, con una luz en el centro. | 1 de cada 22 |
| Palmeras | Grupo de hasta tres palmeras tropicales. | 1 de cada 10 |

Todas las estructuras de suelo se levantan sobre una plataforma plana con la puerta a un escalón del terreno y no se generan sobre agua ni con más de 4 bloques de desnivel, para que nunca tapen el paso.

## Compilar

El workflow `.github/workflows/frutiger-world.yml` compila el mod, arranca un servidor Forge con él para comprobar que el mundo genera sin errores, y deja el jar en `frutiger-world/dist/` y en los artefactos de la ejecución.

En tu PC: `./gradlew build` (necesita Java 21) → `build/libs/frutigerworld-1.0.0.jar`.
