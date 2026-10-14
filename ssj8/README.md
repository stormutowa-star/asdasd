# SSJ8: fix de transformación progresiva, pelo y carga de ki

Este directorio contiene los fuentes de las clases que cambié del mod `ssj8` (1.0.2).
El resto del proyecto no está aquí: copia estos archivos sobre tu proyecto y mantén el resto igual.
El JAR ya compilado está en el mensaje de la sesión.

## Qué cambió

| Problema | Causa | Cambio |
|---|---|---|
| La transformación es instantánea | El rayo, sonidos y partículas (`transformEffects`) salían al activar la forma, y el pelo crecía después | El destello sale al **terminar** la fase de carga (modo 9, 60 ticks). El pelo crece durante esos 60 ticks (`ClientState.growth`, curva suave, ya no 45 ticks) y llega a su tamaño justo en el destello. |
| Al mirar hacia abajo, el plateado se separa del dorado | `Ssj8HairLayer` aplicaba una rotación extra `-0.8 * head.xRot` solo al pelo plateado | Ambos pelos usan el mismo transform de cabeza. |
| El plateado tiene zonas más oscuras | `HairModel.quadMetal` oscurecía cada cara según su normal y el mechón se oscurecía de la raíz a la punta | `HairModel.quadSolid`: color plateado uniforme en todo el mechón. El dorado no cambia. |
| La carga de ki suena a "pitidos" y no tiene la animación de DMZ | Un disparo de `ki_charge_loop` cada 45 ticks, y el brazo en pose lateral | Mismo sonido en **loop** que DMZ (`KiChargeSound`, volumen 0.5, sigue al jugador, se para al soltar Z). La pose es la de `base.ki_charge` de DMZ (`Ssj8PlayerModel.applyKiCharge`), con entrada y salida suaves de 5 ticks, y solo cuando el jugador está quieto (igual que `!isMoving` en DMZ). |

Referencias en DragonMine Z (commit `2d348fa`):
- Animación: `src/main/resources/assets/dragonminez/animations/entity/races/movement.animation.json`, clave `base.ki_charge`.
- Loop de sonido: `common/init/sounds/AuraLoopSound.java` (volumen 0.5, looping). El archivo `ki_charge_loop.ogg` ya era idéntico en SSJ8 (SHA-1 `67b7299a…` en ambos).

## Cambios en `Ssj8Logic.java` (no incluido aquí)

`Ssj8Logic` no tenía fuente en esta sesión, así que su `.class` se parcheó a nivel de bytecode. Estos son los cuatro cambios equivalentes en Java:

1. `setForm(...)`, rama `if (on)`: **quitar** la llamada `transformEffects(player, true);`. Dejar el `setHealth` y el mensaje.
2. `setMode(...)`: **agregar** al inicio del método:
   ```java
   if (s.mode == 9 && mode != 9) {
       transformEffects(p, true); // destello de transformacion al terminar la carga
   }
   ```
3. `handleKey(...)`, caso `0` (Z): **quitar** `SsjPlay.at(p, SsjSounds.KI_CHARGE, SoundSource.PLAYERS, 1.2f, 1.0f);`.
4. `onPlayerTick(...)`, caso `1`: **quitar** el bloque `if (p.tickCount % 45 == 0) { SoundUtil.play(... SsjSounds.KI_CHARGE ...); }`.

## Verificación y límites

- **No se pudo compilar contra Minecraft, Forge ni GeckoLib.** Desde esta sesión esos hosts no son alcanzables, así que no hay un entorno donde construir o ejecutar el mod.
- Lo que sí se comprobó:
  - Las 8 clases cambiadas compilan contra stubs generados desde las referencias del bytecode original.
  - El verificador de la JVM (`-Xverify:all`) acepta las clases cambiadas y el `Ssj8Logic` parcheado.
  - El diff de bytecode de `Ssj8Logic` cambia solo los cuatro puntos anteriores.
  - 21 pruebas de la lógica con matemática real y tiempo simulado: color plateado uniforme, crecimiento monótono, curva de 60 ticks, mezcla de carga, pose DMZ y ausencia de pose al caminar.
- **No probado en juego.** Hay que revisar en cliente: el timing real del destello, la apariencia del pelo, el sonido en loop con varios jugadores, y que la pose se vea bien con la cámara de tercera persona.
- Lo que no cambié: el pelo plateado sigue moviéndose más con el viento que el dorado (0.35 frente a 0.08, a propósito). Si al correr sigues viendo separación, esa es la siguiente pista. La cola tampoco crece durante la transformación.

## Licencia

`mods.toml` declara `license="MIT"`, pero los sonidos vienen de DragonMine Z (GPL-3.0), según `NOTICE-SONIDOS.txt`. La animación de carga que se copió también son valores de DMZ. Si distribuyes el mod, la licencia debe ser GPL-3.0, como indica ese NOTICE. No cambié `mods.toml`.
