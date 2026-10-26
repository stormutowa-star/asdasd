// Uso: NODE_PATH="$(npm root -g)" node build_model.js <spec.js> [salida.bbmodel]
// Spec -> .bbmodel construido en Blockbench web (formato modded_entity, box UV). Ver SKILL.md.
const fs = require('fs');
const path = require('path');
const { openBlockbench } = require('./bblib');

const specPath = path.resolve(process.argv[2]);
const spec = require(specPath);
const out = path.resolve(process.argv[3] || path.join(__dirname, '../../../../blockbench', spec.name + '.bbmodel'));
const [TW, TH] = spec.texture;

// Empaquetado de UV (estantes). Huella de caja: 2(w+d) x (d+h).
// Se prueba primero el orden clasico (alto desc.); si no cabe se prueban otros ordenes (sin cambiar los modelos que ya caben).
function packWith(cubes, cmp) {
  const items = cubes.map((c, i) => {
    const w = Math.ceil(c.to[0] - c.from[0] - 1e-6), h = Math.ceil(c.to[1] - c.from[1] - 1e-6), d = Math.ceil(c.to[2] - c.from[2] - 1e-6);
    return { i, W: 2 * (w + d), H: d + h };
  }).sort(cmp);
  const uv = new Array(cubes.length);
  let x = 0, y = 0, rowH = 0;
  for (const it of items) {
    if (x + it.W > TW) { x = 0; y += rowH; rowH = 0; }
    if (it.W > TW) throw new Error(`cubo ${cubes[it.i].name} no cabe en la textura`);
    if (y + it.H > TH) throw new Error(`textura ${TW}x${TH} llena empaquetando ${cubes[it.i].name}`);
    uv[it.i] = [x, y];
    x += it.W; rowH = Math.max(rowH, it.H);
  }
  return uv;
}
// MaxRects (mejor ajuste por lado corto) como ultimo recurso: aprovecha mucho mejor la textura.
function packMaxRects(cubes, cmp) {
  const items = cubes.map((c, i) => {
    const w = Math.ceil(c.to[0] - c.from[0] - 1e-6), h = Math.ceil(c.to[1] - c.from[1] - 1e-6), d = Math.ceil(c.to[2] - c.from[2] - 1e-6);
    return { i, W: 2 * (w + d), H: d + h };
  }).sort(cmp);
  let free = [{ x: 0, y: 0, w: TW, h: TH }];
  const uv = new Array(cubes.length);
  for (const it of items) {
    let best = null, bs = 1e9, bl = 1e9;
    for (const f of free) {
      if (it.W <= f.w && it.H <= f.h) {
        const ss = Math.min(f.w - it.W, f.h - it.H), ls = Math.max(f.w - it.W, f.h - it.H);
        if (ss < bs || (ss === bs && ls < bl)) { best = f; bs = ss; bl = ls; }
      }
    }
    if (!best) throw new Error(`textura ${TW}x${TH} llena empaquetando ${cubes[it.i].name}`);
    const r = { x: best.x, y: best.y, w: it.W, h: it.H };
    uv[it.i] = [r.x, r.y];
    const next = [];
    for (const f of free) {
      if (r.x >= f.x + f.w || r.x + r.w <= f.x || r.y >= f.y + f.h || r.y + r.h <= f.y) { next.push(f); continue; }
      if (r.x > f.x) next.push({ x: f.x, y: f.y, w: r.x - f.x, h: f.h });
      if (r.x + r.w < f.x + f.w) next.push({ x: r.x + r.w, y: f.y, w: f.x + f.w - r.x - r.w, h: f.h });
      if (r.y > f.y) next.push({ x: f.x, y: f.y, w: f.w, h: r.y - f.y });
      if (r.y + r.h < f.y + f.h) next.push({ x: f.x, y: r.y + r.h, w: f.w, h: f.y + f.h - r.y - r.h });
    }
    free = next.filter((a, ia) => !next.some((b, ib) => ib !== ia && a.x >= b.x && a.y >= b.y && a.x + a.w <= b.x + b.w && a.y + a.h <= b.y + b.h && (ia > ib || a.x !== b.x || a.y !== b.y || a.w !== b.w || a.h !== b.h)));
  }
  return uv;
}
function packUV(cubes) {
  const orders = [
    (a, b) => b.H - a.H || b.W - a.W,
    (a, b) => b.W * b.H - a.W * a.H,
    (a, b) => b.W - a.W || b.H - a.H,
    (a, b) => Math.floor(b.H / 4) - Math.floor(a.H / 4) || b.W - a.W,
    (a, b) => Math.floor(b.H / 8) - Math.floor(a.H / 8) || b.W - a.W,
  ];
  let err;
  for (const cmp of orders) { try { return packWith(cubes, cmp); } catch (e) { err = err || e; } }
  for (const cmp of [orders[1], orders[0], orders[2]]) { try { return packMaxRects(cubes, cmp); } catch (e) { err = err || e; } }
  throw err;
}

(async () => {
  const names = new Set();
  spec.cubes.forEach((c, i) => { c.name = c.name || `cube_${i}`; if (names.has(c.name)) c.name += '_' + i; names.add(c.name); });
  // `uvFrom: '<cubo>'` reutiliza la zona de textura de otro cubo (mismas dimensiones): ahorra textura en piezas simetricas.
  const dimsOf = (c) => [0, 1, 2].map((k) => Math.ceil(c.to[k] - c.from[k] - 1e-6)).join('x');
  const own = spec.cubes.map((c, i) => i).filter((i) => !spec.cubes[i].uvFrom);
  const packed = packUV(own.map((i) => spec.cubes[i]));
  const uvs = new Array(spec.cubes.length);
  own.forEach((i, k) => { uvs[i] = packed[k]; });
  spec.cubes.forEach((c, i) => {
    if (!c.uvFrom) return;
    const j = spec.cubes.findIndex((o) => o.name === c.uvFrom);
    if (j < 0 || spec.cubes[j].uvFrom) throw new Error(`uvFrom invalido en ${c.name}: ${c.uvFrom}`);
    if (dimsOf(c) !== dimsOf(spec.cubes[j])) throw new Error(`uvFrom ${c.name} <- ${c.uvFrom}: dimensiones distintas`);
    uvs[i] = uvs[j];
  });
  const { browser, page } = await openBlockbench();
  const result = await page.evaluate(({ spec, uvs }) => {
    const warn = [];
    newProject(Formats.modded_entity);
    Project.name = spec.name;
    Project.texture_width = spec.texture[0];
    Project.texture_height = spec.texture[1];
    Project.box_uv = true;
    const groups = {};
    // Los huesos pueden venir en cualquier orden: crea padres primero
    const pending = spec.bones.slice();
    let guard = 0;
    while (pending.length && guard++ < 10000) {
      const b = pending.shift();
      if (b.parent && !groups[b.parent]) { pending.push(b); continue; }
      const g = new Group({ name: b.name, origin: b.pivot || [0, 0, 0], rotation: b.rotation || [0, 0, 0] });
      if (b.parent) g.addTo(groups[b.parent]);
      g.init();
      groups[b.name] = g;
    }
    if (pending.length) throw new Error('huesos con padre inexistente: ' + pending.map((b) => b.name));
    spec.cubes.forEach((c, i) => {
      if (!groups[c.bone]) throw new Error('cubo ' + c.name + ' con hueso inexistente ' + c.bone);
      const o = { name: c.name, from: c.from, to: c.to, uv_offset: uvs[i], inflate: c.inflate || 0, mirror_uv: !!c.mirror, box_uv: true };
      if (c.rotation) { o.rotation = c.rotation; o.origin = c.origin || c.from.map((v, k) => (v + c.to[k]) / 2); }
      new Cube(o).addTo(groups[c.bone]).init();
    });
    for (const a of spec.animations || []) {
      const loop = a.loop === true || a.loop === 'loop' ? 'loop' : a.loop === 'hold' ? 'hold' : 'once';
      const anim = new Animation({ name: a.name, loop, length: a.length, snapping: 24 }).add(true);
      for (const [bn, chans] of Object.entries(a.bones || {})) {
        const g = groups[bn];
        if (!g) { warn.push('animacion ' + a.name + ': hueso inexistente ' + bn); continue; }
        const ba = new BoneAnimator(g.uuid, anim, g.name);
        anim.animators[g.uuid] = ba;
        for (const [ch, kfs] of Object.entries(chans)) {
          for (const [t, v, interp] of kfs) {
            ba.addKeyframe({ channel: ch, time: t, interpolation: interp || 'linear', data_points: [{ x: v[0], y: v[1], z: v[2] }] });
          }
        }
      }
    }
    return { json: Codecs.project.compile(), warn };
  }, { spec, uvs });
  await browser.close();
  result.warn.forEach((w) => console.warn('AVISO', w));
  const model = JSON.parse(result.json);
  // Metadatos propios (materiales/glow/paleta) para paint.py; Blockbench los ignora al abrir
  if (model.elements.length !== spec.cubes.length) throw new Error('numero de elementos distinto');
  model.elements.forEach((e, i) => {
    if (e.name !== spec.cubes[i].name) throw new Error('orden de elementos distinto en ' + e.name);
    e.kl_mat = spec.cubes[i].mat || 'default';
    e.kl_glow = !!spec.cubes[i].glow;
  });
  model.kl_palette = spec.palette || {};
  model.kl_name = spec.name;
  fs.mkdirSync(path.dirname(out), { recursive: true });
  fs.writeFileSync(out, JSON.stringify(model));
  console.log(`build_model: ${out} (${model.elements.length} cubos, ${(model.animations || []).length} animaciones)`);
})().catch((e) => { console.error(e); process.exit(1); });
