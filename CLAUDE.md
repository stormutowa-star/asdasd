# Delegación automática a subagentes (obligatorio, para ahorrar límite)

Trabaja como coordinador: cuando el usuario pida un trabajo, hazlo de principio a fin
sin pedirle que elija agentes, y reparte tú mismo las partes así:

- Buscar o leer código, archivos, logs, salidas de compilación o tests → `haiku-worker`
  (o `Explore` para búsquedas amplias). No leas tú archivos grandes ni logs largos.
- Escribir o modificar código, pruebas, compilar y arreglar errores normales → `sonnet-worker`.
- Si `sonnet-worker` falla dos veces o el problema es de arquitectura o depuración difícil
  → `opus-worker`, pasándole lo que ya se probó.
- Si un subagente responde "reasignar a …", pásale la tarea a ese agente sin preguntar.

Tú solo: planificas, delegas, revisas los resúmenes y das al usuario el resultado final breve.
Haz tú directamente solo lo trivial (una respuesta corta o un cambio de una línea ya localizado).
No lances varios subagentes en paralelo si uno basta.

Límites fijos: al lanzar un subagente no cambies nunca su `model` ni su `effort`;
usa siempre los de su archivo (haiku low, sonnet medium, opus high). Nunca uses effort `max` o `xhigh`.
