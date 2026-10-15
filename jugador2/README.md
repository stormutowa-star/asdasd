# Jugador 2

Mod para Minecraft 1.21.1 con Forge 52.1.0. Añade un **jugador real del servidor** controlado por una IA integrada que decide por sí misma. Aparece en la lista de jugadores, tiene inventario, vida, hambre y experiencia, puede morir y reaparecer, y actúa con las mismas APIs que usa el juego para un jugador.

No es un mob ni un `FakePlayer` de Forge: es un `ServerPlayer` con una conexión falsa, como hacen los jugadores falsos de Carpet.

## Instalación

1. Instala Forge 52.1.0 para Minecraft 1.21.1.
2. Copia `jugador2-1.0.0.jar` en la carpeta `mods/` del servidor o del mundo.
3. Arranca el servidor. El NPC aparece en la lista de jugadores y su mensaje de unión sale en el chat.

Java 21 es necesario para compilar y ejecutar el mod.

## Nombre

El NPC recibe un nombre distinto en cada mundo nuevo. Se genera una sola vez y se guarda, así que no cambia dentro de ese mundo. Para saber cuál es, usa `/jugador2 status`.

## Órdenes

Por el chat, empieza el mensaje con el nombre del NPC:

| Orden | Qué hace |
|---|---|
| `Nombre, ven` | Va hacia quien dio la orden. |
| `Nombre, sigue a <jugador>` | Sigue a un jugador conectado. |
| `Nombre, ve a <x> <y> <z>` | Va a un punto del mundo. |
| `Nombre, mina <bloque>` | Mina el bloque más cercano de ese tipo, por ejemplo `mina stone`. |
| `Nombre, para` | Cancela la orden activa. |

Las órdenes no distinguen mayúsculas. Una orden no reconocida recibe una respuesta corta con la lista. El NPC responde en el chat con su nombre entre corchetes angulares.

Por comandos, con permiso de operador de nivel 2:

- `/jugador2 spawn`: activa el NPC en tu posición.
- `/jugador2 remove`: retira el NPC y no reaparece al arrancar.
- `/jugador2 status`: muestra el estado y la posición.
- `/jugador2 follow <jugador>`: sigue a un jugador conectado.
- `/jugador2 goto <x> <y> <z>`: va a un punto.
- `/jugador2 stop`: cancela la orden activa.

## Qué hace solo

En orden de prioridad:

1. **Sobrevivir.** Come cuando el hambre baja de 14 y tiene comida. Huye corriendo si la vida baja de 6 puntos con enemigos a menos de 6 bloques.
2. **Defenderse.** Ataca al enemigo hostil más cercano a menos de 6 bloques.
3. **Órdenes.** Cumple la orden activa.
4. **Por su cuenta:** recoge madera de árboles (troncos con hojas cerca, no la madera de una construcción) hasta 16 troncos, piedra hasta 32 y carbón hasta 16. Si no tiene ningún pico, craftea uno de madera con tres troncos antes que nada; después, un pico de piedra cuando le falta una herramienta. Pone él mismo la mesa. Guarda en un cofre cercano lo que no son herramientas, armadura ni comida. De noche duerme en una cama si tiene una. Si no le queda nada que hacer, se queda quieto.

Recoge lo que sueltan los bloques que rompe, camina con búsqueda de rutas (sube un bloque, baja hasta tres y salta; evita lava y agua) y mina con la herramienta de su inventario, o con la mano si no tiene una adecuada. Si un bloque no le sirve porque no llega a él, pasa al siguiente. Come también la comida que lleva fuera de la barra, pasándola a la barra antes.

## Muerte y reaparición

Al morir, el NPC suelta su inventario donde murió y sale de la lista de jugadores, con el aviso de muerte de siempre. Reaparece a los 5 segundos, en un sitio libre junto a su cama si la tiene y sigue existiendo, o en el spawn del mundo. Como el juego ya vació su inventario, reaparece con el inventario vacío. Si `keepInventory` está activado, conserva el inventario igual que un jugador.

Reaparece con el mismo UUID y el mismo nombre.

Si el servidor se cierra con el NPC vivo, al arrancar de nuevo vuelve al mismo sitio y con la misma vida (si `spawnOnServerStart` está activado).

## Configuración

Archivo `config/jugador2-common.toml`:

| Clave | Valor por defecto | Significado |
|---|---|---|
| `enabled` | `true` | Activa el mod. |
| `spawnOnServerStart` | `true` | Reaparece el NPC al arrancar si estaba activo. |
| `aiDecisionIntervalTicks` | `10` | Cada cuántos ticks reevalúa prioridades. |
| `hostileDetectionRadius` | `6` | Radio para detectar enemigos, en bloques. |
| `eatHungerThreshold` | `14` | Come si el hambre baja de este valor (escala de 0 a 20). |
| `fleeHealthThreshold` | `6` | Huye si la vida baja de este valor (20 puntos son 10 corazones). |
| `respawnDelayTicks` | `100` | Espera antes de reaparecer, en ticks. |
| `pathSearchNodeLimit` | `2000` | Nodos máximos por búsqueda de ruta. |

## Límites

- No pesca, no comercia con aldeanos, no encanta, no entra al Nether ni al End.
- No tiene skin propia: aparece con la skin por defecto de Minecraft.
- No tiene interfaz gráfica: se controla con comandos y chat.
- Solo anda sobre bloques completos. No sabe pisar losas, escaleras ni tierra labrada: un camino que pase por ellos puede fallar, o el NPC puede quedarse corto.
- Cuenta como jugador para dormir. Si no está durmiendo (por ejemplo, de noche sin cama cerca), la noche no pasa mientras los demás duermen.

## Coste de rendimiento

El NPC mantiene cargados los chunks donde está, igual que un jugador real. En servidores grandes eso cuesta memoria y tiempo de servidor. Cada búsqueda de ruta está acotada por `pathSearchNodeLimit`, y solo se hace una búsqueda por tick.

## Compatibilidad

Otros mods pueden asumir que todo jugador es humano: por ejemplo, contar jugadores para dormir de noche, o enviar avisos solo a jugadores que tienen cliente. El NPC no tiene cliente, así que esos mods pueden comportarse de forma distinta cuando está activo.

## Compilar

Con GitHub Actions: la ejecución `Compilar Jugador 2` compila el mod, pasa las pruebas unitarias y el escenario en juego, y sube el jar como artefacto `jugador2-mod`. Un escenario fallido detiene la ejecución y no sube el artefacto.

En local, desde `jugador2/`:

```bash
./gradlew test                 # pruebas unitarias
JUGADOR2_SELFTEST=1 ./gradlew runSelftest   # escenario en juego, en un servidor de desarrollo
./gradlew build                # produce build/libs/jugador2-1.0.0.jar
```

El escenario en juego construye una plataforma de prueba en `run/selftest` y comprueba, en orden, que el NPC se añade a la lista de jugadores, camina, mina, coloca, come, ataca, muere y reaparece, obedece órdenes de chat, defiende, huye, recoge madera, craftea, guarda en un cofre, fabrica el pico de madera sin herramientas, reaparece junto a su cama, come la comida que lleva fuera de la barra, no insiste en un árbol al que no llega, no corta madera de construcciones y va a un punto en el aire quedándose en el suelo. Imprime `RESULT PASS` o `RESULT FAIL` y detiene el servidor en el primer paso que falla.
