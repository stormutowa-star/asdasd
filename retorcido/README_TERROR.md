# El Retorcido 2.0.0 - Director de terror

Todo ocurre en el SERVIDOR (funciona en dedicado). El Retorcido ya no aparece solo: solo lo trae el director, el huevo o comandos.

## Historia (tiempo jugado en el overworld)
- Fase 1 (0 min): pasos, susurros, puertas, antorchas apagadas, Dani con dudas.
- Fase 2 (10 min): + Observador lejano (45-60 bloques), oscuridad con latidos, Dani mira fijamente la nada. Dani pasa a mood 1 (inquieto).
- Fase 3 (25 min): observador mas cerca (30-40) y detras, Dani dice frases siniestras.
- Fase 4 (40 min, de noche): SECUESTRO de Dani (si esta a <20 bloques).
- Fase 5 (tras el secuestro): un Cazador cada noche; de dia huyen. Al amanecer Dani regresa (mood 2, "no se acuerda de nada").
- Si Dani muere antes del secuestro, reaparece a los 60 s ("Volví. Me caí en una cueva.").
- Si matas a un cazador: 2 dias sin cacerias.

## Sucesos
pasos, susurro, puerta, antorcha, oscuridad, observador, dani_dice, dani_mira, secuestro, cazador, regreso.

## Comandos (permiso 2, sobre quien los escribe)
- `/terror fase <0-5>` fija la fase (0 = automatica, tiempo a 0)
- `/terror evento <nombre>` ejecuta un suceso ignorando la hora
- `/terror reset` borra el progreso y calma a Dani
- `/terror estado` muestra fase, tiempo y si fue secuestrado

## Ajustes
Todo en `src/main/java/com/ricardo/retorcido/terror/TerrorConfig.java` (tiempos en ticks, distancias en bloques, cooldowns, probabilidades). Frases en `TerrorLines.java`.
