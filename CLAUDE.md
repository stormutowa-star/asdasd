# Uso de subagentes (para ahorrar límite)

- Búsquedas, lectura de archivos, logs y salidas largas de compilación → `haiku-worker` (o `Explore`). Pide solo el resumen.
- Implementar o modificar código, pruebas, errores normales → `sonnet-worker`.
- `opus-worker` solo si `sonnet-worker` falló o hay un problema de arquitectura o depuración difícil.
- Si un subagente responde "reasignar a …", pásale la tarea a ese agente con lo ya averiguado.
- No lances varios subagentes en paralelo si uno basta.
