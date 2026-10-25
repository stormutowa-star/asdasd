---
name: blockbench
description: Crea y edita modelos y animaciones de Blockbench (archivos .bbmodel) para el mod Kein Loree (naves, Kein, Razako, los 5 Calendarios, soldados) controlando la app web oficial de Blockbench con Playwright y Chromium. Úsalo cuando la tarea pida modelos 3D, animaciones o VFX hechos en Blockbench para este mod.
---

# Blockbench desde Claude

Blockbench no tiene CLI. Esta skill lo controla con Playwright: abre la app web oficial (`https://web.blockbench.net/`, versión 5.2.2 comprobada) y llama a su API de JavaScript para crear el modelo, sus animaciones y compilar el archivo `.bbmodel`.

## Requisitos

- **Node.js** y **Playwright** (paquete global). En la nube ya están en el contenedor.
- **Chromium**. En la nube está en `/opt/pw-browsers`. En local, `npx playwright install chromium`.
- **Red** hacia `web.blockbench.net`.

Comprueba antes de empezar que Node, Playwright y Chromium estén disponibles.

## Cómo correr un script

Escribe el script en el scratchpad o en `.claude/skills/blockbench/scripts/`, nunca dentro del código del mod:

```bash
NODE_PATH="$(npm root -g)" node mi-script.js
```

Plantilla verificada en Blockbench 5.2.2:

```js
const { chromium } = require('playwright');
const fs = require('fs');

(async () => {
  const browser = await chromium.launch({
    executablePath: process.env.CHROME || undefined,
    args: ['--no-sandbox'],
  });
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  await page.goto('https://web.blockbench.net/', { waitUntil: 'load', timeout: 120000 });
  await page.waitForTimeout(8000);

  const bbmodel = await page.evaluate(() => {
    newProject(Formats.free);
    const cube = new Cube({ name: 'cuerpo', from: [0, 0, 0], to: [16, 16, 16] }).init();
    const anim = new Animation({ name: 'idle', loop: 'loop', length: 1 }).add(true);
    const ba = new BoneAnimator(cube.uuid, anim, cube.name);
    anim.animators[cube.uuid] = ba;
    ba.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: 0, y: 0, z: 0 }] });
    ba.addKeyframe({ channel: 'rotation', time: 1, data_points: [{ x: 0, y: 45, z: 0 }] });
    return Codecs.project.compile();
  });

  fs.writeFileSync(process.env.OUT || 'modelo.bbmodel', bbmodel);
  await browser.close();
})();
```

En la nube, `CHROME` no hace falta si Playwright ya encuentra su Chromium. Si no lo encuentra, usa `CHROME=/opt/pw-browsers/chromium`.

## Qué está verificado

- La app carga sin errores de página.
- Se crea un cubo, una animación con dos keyframes de rotación, y `Codecs.project.compile()` devuelve JSON `.bbmodel` (formato 5.0).
- `page.screenshot` funciona. Puedes abrir la captura con Read para revisar el modelo.

## Qué no está verificado

- Formatos de Minecraft Java y Bedrock (`Formats.java_block`, etc.). Usa `Formats.free` hasta comprobarlo.
- Exportar a formatos de juego (modelo de entidad Java o GeckoLib). Depende de la decisión sobre cómo entran las animaciones al juego.
- Posiciones, UV y texturas.

## Reglas

- **No inventes nombres de API.** Si algo falla, lee el error, haz una sonda pequeña y consulta la [wiki de plugins](https://www.blockbench.net/wiki/docs/plugin).
- **Guarda los `.bbmodel` en la ruta que indique la tarea**, no en cualquier carpeta.
- **Revisa visualmente con `page.screenshot`** cuando el cambio sea visual.
- **No hagas `npm install` sobre código de repos externos** sin permiso explícito.
- **La prueba en el juego y en Blockbench de escritorio la confirma el usuario.** Esta skill no la reemplaza.
