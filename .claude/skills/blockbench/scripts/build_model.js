// Uso: NODE_PATH="$(npm root -g)" node build_model.js <spec.js> [salida.bbmodel]
// Spec -> .bbmodel construido en Blockbench web (formato modded_entity, box UV). Ver SKILL.md.
const fs = require('fs');
const path = require('path');
const { openBlockbench } = require('./bblib');

const specPath = path.resolve(process.argv[2]);
const spec = require(specPath);
const out = path.resolve(process.argv[3] || path.join(__dirname, '../../../../blockbench', spec.name + '.bbmodel'));
const [TW, TH] = spec.texture;

// Empaquetado de UV (estantes). Huella de caja: 2(w+d) x (d+h)
function packUV(cubes) {
  const items = cubes.map((c, i) => {
    const w = Math.ceil(c.to[0] - c.from[0] - 1e-6), h = Math.ceil(c.to[1] - c.from[1] - 1e-6), d = Math.ceil(c.to[2] - c.from[2] - 1e-6);
    return { i, W: 2 * (w + d), H: d + h };
  }).sort((a, b) => b.H - a.H || b.W - a.W);
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

(async () => {
  const names = new Set();
  spec.cubes.forEach((c, i) => { c.name = c.name || `cube_${i}`; if (names.has(c.name)) c.name += '_' + i; names.add(c.name); });
  const uvs = packUV(spec.cubes);
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
