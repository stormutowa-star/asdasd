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

## Pipeline (scripts/ en esta carpeta; todo verificado con el modelo `bbtest`)

`scripts/all.sh [modelo...]` regenera todo (`SHOTS=1` añade capturas). Por modelo:
1. Spec `blockbench/specs/<m>.js` (CommonJS) -> `build_model.js <spec> <salida.bbmodel>` (Blockbench web).
2. `python3 -I paint.py <bbmodel>` -> `src/main/resources/assets/keinloree/textures/entity/<m>.png` y `<m>_glow.png`, incrustadas en el .bbmodel.
3. `shots.js <bbmodel> [dir]` -> `blockbench/shots/<m>/` (front, side = lado DERECHO, back, q34, `<anim>_<0|25|50|75>_front/side`). Revisa con Read.
4. `bb2java.mjs <bbmodel> <Clase> [paquete]` -> `client/model/gen/<Clase>Model.java` + `<Clase>Anims.java` (clase = PascalCase del nombre: `bbtest` -> `Bbtest`).

### Formato de spec
`{ name, texture:[w,h], palette:{mat:'#hex' | {color,noise,edge}}, bones:[{name,parent,pivot,rotation}],
cubes:[{bone,name,from,to,inflate,mat,glow,mirror,rotation,origin}], animations:[{name, loop:true|false|'hold', length,
bones:{hueso:{rotation|position|scale:[[t,[x,y,z],'linear'|'catmullrom'],...]}}}] }`.
Unidades de Blockbench (Y arriba, px, 16 = 1 bloque, pies en y=0). `rotation`/`origin` de un cubo = rotacion propia (origen por defecto: su centro).
El empaquetado de UV es automatico (box UV, estantes). `bb2java` solo entiende interpolacion linear/catmullrom.
Huesos y nombres estandar: ver `scratchpad/docs/41-modelos.md` (root, body, head, right_arm, left_arm, ...).

### Convencion de ejes (probada en el juego, capturas coinciden con Blockbench)
- El personaje mira a **-Z** de Blockbench; su brazo derecho esta en **+X**. En Blockbench, camara en -Z = de frente; camara en +X = lado derecho.
- En el juego, entidad con yaw 180 mira a una camara que mira al sur (yaw 0); yaw 90 ensena su lado derecho.
- Conversion: posicion (x,y,z) -> (-x,-y,z); rotacion (rx,ry,rz) -> (-rx,-ry,rz); hueso raiz `offset=(-px,24-py,pz)`; hijos `-(p-pPadre)` en x,y y `+` en z;
  cubo `addBox(-(to.x-p.x), -(to.y-p.y), from.z-p.z, w,h,d, inflate)`; cubo rotado -> hijo `cube_<nombre>` con pivote en `origin` y rotacion convertida.
  Animacion: rotacion `degreeVec(-rx,-ry,rz)`, posicion `new Vector3f(-x,-y,z)` (NO `posVec`), escala `scaleVec`. Positivo Z en rotacion de Blockbench = mismo sentido visual en el juego.
- `KeyframeAnimations.animate` SUMA a la pose: llama a `model.resetPose()` y luego `model.applyAnim(DEF, ms)` (o `animate(AnimationState,...)` en subclases).
- Render fuera de entidades (como `client/test/BbTestRender`): en `RenderLevelStageEvent` AFTER_ENTITIES usa un `PoseStack` NUEVO (la matriz de vista ya esta en RenderSystem; no hagas `mulPose(event.getPoseStack())`),
  traslada a `pos - camara`, luego como LivingEntityRenderer: `Axis.YP.rotationDegrees(180-yaw)`, `scale(-1,-1,1)`, `translate(0,-1.501,0)`. Glow: `RenderType.eyes(<m>_glow.png)`.
- Verificar en juego: `tools/client_shot.sh bbtest` (escenario "bbtest" registrado en `ClientAutoTest`; capturas en `run/screenshots/bbtest_*.png`). Nubes: `mc.options.cloudStatus().set(CloudStatus.OFF)` en el escenario.

### APIs de Blockbench 5.2.2 que funcionan (verificadas)
- Formato `Formats.modded_entity` (con `Project.box_uv = true`; `Project.texture_width/height`).
- `new Group({name, origin, rotation}).addTo(padre).init()`; `new Cube({name, from, to, uv_offset, inflate, mirror_uv, rotation, origin}).addTo(grupo).init()`.
- `new Animation({name, loop:'loop'|'once'|'hold', length, snapping}).add(true)`; `new BoneAnimator(group.uuid, anim, group.name)`; `anim.animators[group.uuid] = ba`;
  `ba.addKeyframe({channel:'rotation'|'position'|'scale', time, interpolation, data_points:[{x,y,z}]})`.
- `Codecs.project.compile()` -> JSON (jerarquia en `outliner`, datos de grupo en `groups`, animaciones en `animations`).
- Cargar: `loadModelFile({name, content, path:'/x/m.bbmodel'})` (el `path` con extension es obligatorio). Texturas embebidas en `textures[].source` (data URL); caras con `texture:0`.
- Camara: `Preview.selected.camera.position.set()`, `.controls.target.set()`, `.controls.update()`; captura con `page.screenshot({clip: canvas rect})`.
  Animacion: `Modes.options.animate.select(); anim.select(); Timeline.setTime(t); Animator.preview()`.
- Campos propios `kl_mat`, `kl_glow`, `kl_palette`, `kl_name` los anade build_model.js al JSON (Blockbench los ignora).

## Pendiente de verificar
- `mirror:true` en cubos (UV espejo) no se ha comparado visualmente; `bb2java` solo traduce `mirror_uv` a `.mirror()`.
- Interpolaciones `step`/`bezier`, expresiones en keyframes (se avisa y se usa linear/0).
- Abrir el .bbmodel en Blockbench de escritorio lo confirma el usuario.

## Reglas

- **No inventes nombres de API.** Si algo falla, lee el error, haz una sonda pequeña y consulta la [wiki de plugins](https://www.blockbench.net/wiki/docs/plugin).
- **Guarda los `.bbmodel` en la ruta que indique la tarea**, no en cualquier carpeta.
- **Revisa visualmente con `page.screenshot`** cuando el cambio sea visual.
- **No hagas `npm install` sobre código de repos externos** sin permiso explícito.
- **La prueba en el juego y en Blockbench de escritorio la confirma el usuario.** Esta skill no la reemplaza.
