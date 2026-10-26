// Generador de los Cinco Calendarios (humanoides con armadura, arma, capa y halo) y sus animaciones.
// Convencion: mira a -Z, brazo derecho en +X, pies en y=0, 16 px = 1 bloque.
// Rotaciones de Blockbench: +rx = brazo/pierna que cuelga va hacia ADELANTE (torso: +rx se echa atras),
// +rz en el brazo derecho = separarlo del cuerpo, +ry = girar hacia su izquierda.
const { box } = require('./shapes');

const C = 'catmullrom';
const L = 'linear';

/**
 * o: {name, palette, h: {leg, torso, head}, w: {torso, arm, leg}, parts: fn(add, dims) que añade cubos propios,
 *     extraBones: [...], weapon: 'swords'|'hammer'|'scythe'|'staff'|'spear', float: bool, style: 'fast'|'heavy'|'reap'|'mage'|'lance'}
 */
function build(o) {
  const leg = o.h.leg, torso = o.h.torso, head = o.h.head;
  const tw = o.w.torso, aw = o.w.arm, lw = o.w.leg;
  const hip = leg, neck = leg + torso, top = neck + head;
  const sh = neck - 1.5; // altura del hombro
  const ax = tw / 2 + aw / 2; // centro del brazo en x
  const d = { leg, torso, head, tw, aw, lw, hip, neck, top, sh, ax };
  const bones = [
    { name: 'root', parent: null, pivot: [0, 0, 0] },
    { name: 'body', parent: 'root', pivot: [0, hip, 0] },
    { name: 'head', parent: 'body', pivot: [0, neck, 0] },
    { name: 'right_arm', parent: 'body', pivot: [ax, sh, 0] },
    { name: 'left_arm', parent: 'body', pivot: [-ax, sh, 0] },
    { name: 'right_leg', parent: 'root', pivot: [lw / 2, hip, 0] },
    { name: 'left_leg', parent: 'root', pivot: [-lw / 2, hip, 0] },
    { name: 'weapon', parent: 'right_arm', pivot: [ax, hip - 1, 0] },
    { name: 'cape', parent: 'body', pivot: [0, neck, 2.6] },
    { name: 'halo', parent: 'head', pivot: [0, neck + head / 2 + 1, 6] },
  ].concat(o.extraBones || []);
  const cubes = [];
  const add = (c) => { if (Array.isArray(c)) cubes.push(...c); else cubes.push(c); };
  // Cuerpo base
  add(box('body', 'torso', [0, hip + torso / 2, 0], [tw, torso, 4.5], 'armor'));
  add(box('body', 'chest', [0, hip + torso * 0.68, -0.4], [tw - 1, torso * 0.42, 5], 'armor_light', { inflate: 0.25 }));
  add(box('body', 'belt', [0, hip + 1, 0], [tw + 0.4, 2, 4.9], 'trim'));
  add(box('body', 'gem', [0, hip + torso * 0.66, -2.9], [2, 2, 1], 'glow', { glow: true, rotation: [0, 0, 45] }));
  add(box('head', 'head', [0, neck + head / 2, 0], [head, head, head], 'skin'));
  add(box('right_arm', 'arm_r', [ax, sh - 5.5, 0], [aw, 12, aw], 'armor'));
  add(box('right_arm', 'glove_r', [ax, sh - 10, 0], [aw + 0.2, 3, aw + 0.2], 'trim', { inflate: 0.2 }));
  add(box('right_arm', 'pauldron_r', [ax + 0.5, sh + 0.5, 0], [aw + 2, 3, aw + 2], 'armor_light'));
  add(box('left_arm', 'arm_l', [-ax, sh - 5.5, 0], [aw, 12, aw], 'armor', { mirror: true }));
  add(box('left_arm', 'glove_l', [-ax, sh - 10, 0], [aw + 0.2, 3, aw + 0.2], 'trim', { inflate: 0.2, mirror: true }));
  add(box('left_arm', 'pauldron_l', [-ax - 0.5, sh + 0.5, 0], [aw + 2, 3, aw + 2], 'armor_light', { mirror: true }));
  add(box('right_leg', 'leg_r', [lw / 2, leg / 2, 0], [lw, leg, 4], 'under'));
  add(box('right_leg', 'greave_r', [lw / 2, leg * 0.35, -0.3], [lw + 0.4, leg * 0.5, 4.4], 'armor'));
  add(box('right_leg', 'boot_r', [lw / 2, 1.5, -0.4], [lw + 0.6, 3, 4.8], 'trim'));
  add(box('left_leg', 'leg_l', [-lw / 2, leg / 2, 0], [lw, leg, 4], 'under', { mirror: true }));
  add(box('left_leg', 'greave_l', [-lw / 2, leg * 0.35, -0.3], [lw + 0.4, leg * 0.5, 4.4], 'armor', { mirror: true }));
  add(box('left_leg', 'boot_l', [-lw / 2, 1.5, -0.4], [lw + 0.6, 3, 4.8], 'trim', { mirror: true }));
  if (o.cape !== false) {
    add(box('cape', 'cape', [0, neck - (o.capeLen || 16) / 2, 3.0], [tw + 1, o.capeLen || 16, 0.8], 'cape'));
  }
  o.parts(add, d);
  return {
    name: o.name, texture: o.texture || [128, 128], palette: o.palette, bones, cubes,
    animations: anims(o, d),
  };
}

/** Aro de n segmentos en el plano XY (detras de la cabeza), centrado en el pivote del hueso. */
function ring(bone, name, c, r, n, thick, mat, glow) {
  const out = [];
  const seg = 2 * r * Math.tan(Math.PI / n) + 0.3;
  for (let i = 0; i < n; i++) {
    const a = (i / n) * 2 * Math.PI;
    const x = c[0] + Math.cos(a) * r, y = c[1] + Math.sin(a) * r;
    out.push(box(bone, `${name}_${i}`, [x, y, c[2]], [thick, seg, thick], mat,
      { glow, rotation: [0, 0, (a * 180) / Math.PI], origin: [x, y, c[2]] }));
  }
  return out;
}

const kf = (pairs, interp = C) => pairs.map(([t, v]) => [t, v, interp]);

function anims(o, d) {
  const heavy = o.style === 'heavy';
  const fast = o.style === 'fast';
  const k = heavy ? 1.4 : fast ? 0.8 : 1.0; // escala de tiempo de los ataques
  const R0 = [22, 0, 8], L0 = o.weapon === 'swords' ? [22, 0, -8] : [10, 0, -6];
  const list = [];
  const floatY = o.float ? 3 : 0;
  const halo = { rotation: kf([[0, [0, 0, 0]], [1, [0, 0, 180]], [2, [0, 0, 360]]], L) };
  const base = (extra = {}) => Object.assign({ halo }, extra);
  const rootFloat = (len) => (o.float ? { position: kf([[0, [0, floatY, 0]], [len / 2, [0, floatY + 1.5, 0]], [len, [0, floatY, 0]]]) } : undefined);
  const legsFloat = o.float ? { rotation: kf([[0, [12, 0, 0]], [1, [18, 0, 0]], [2, [12, 0, 0]]]) } : undefined;

  // idle
  list.push({ name: 'idle', loop: true, length: 2, bones: base({
    root: rootFloat(2),
    body: { rotation: kf([[0, [0, 0, 0]], [1, [-2, 0, 0]], [2, [0, 0, 0]]]) },
    head: { rotation: kf([[0, [0, 0, 0]], [1, [2, -4, 0]], [2, [0, 0, 0]]]) },
    right_arm: { rotation: kf([[0, R0], [1, [R0[0] + 4, 0, R0[2] + 2]], [2, R0]]) },
    left_arm: { rotation: kf([[0, L0], [1, [L0[0] + 4, 0, L0[2] - 2]], [2, L0]]) },
    cape: { rotation: kf([[0, [-6, 0, 0]], [1, [-11, 0, 0]], [2, [-6, 0, 0]]]) },
    right_leg: legsFloat, left_leg: legsFloat,
  }) });
  // walk / run
  const gait = (name, len, legA, armA, lean) => list.push({ name, loop: true, length: len, bones: base({
    root: rootFloat(len),
    body: { rotation: kf([[0, [-lean, 0, 0]], [len / 2, [-lean, 0, 0]], [len, [-lean, 0, 0]]]),
            position: kf([[0, [0, 0, 0]], [len / 4, [0, 0.6, 0]], [len / 2, [0, 0, 0]], [len * 0.75, [0, 0.6, 0]], [len, [0, 0, 0]]]) },
    // los que levitan no dan zancadas: las piernas oscilan poco bajo la tunica y el cuerpo se mece al flotar
    right_leg: o.float ? { rotation: kf([[0, [12 + legA * 0.3, 0, 0]], [len / 2, [12 - legA * 0.3, 0, 0]], [len, [12 + legA * 0.3, 0, 0]]]) }
      : { rotation: kf([[0, [legA, 0, 0]], [len / 2, [-legA, 0, 0]], [len, [legA, 0, 0]]]) },
    left_leg: o.float ? { rotation: kf([[0, [12 - legA * 0.3, 0, 0]], [len / 2, [12 + legA * 0.3, 0, 0]], [len, [12 - legA * 0.3, 0, 0]]]) }
      : { rotation: kf([[0, [-legA, 0, 0]], [len / 2, [legA, 0, 0]], [len, [-legA, 0, 0]]]) },
    right_arm: { rotation: kf([[0, [R0[0] - armA, 0, R0[2]]], [len / 2, [R0[0] + armA, 0, R0[2]]], [len, [R0[0] - armA, 0, R0[2]]]]) },
    left_arm: { rotation: kf([[0, [L0[0] + armA, 0, L0[2]]], [len / 2, [L0[0] - armA, 0, L0[2]]], [len, [L0[0] + armA, 0, L0[2]]]]) },
    cape: { rotation: kf([[0, [-18 - lean, 0, 0]], [len / 2, [-24 - lean, 0, 0]], [len, [-18 - lean, 0, 0]]]) },
  }) });
  gait('walk', 1, 28, 14, 3);
  gait('run', 0.6, 48, 34, 14);

  // Ataques: cada uno con anticipacion (30%), golpe (20%) y recuperacion (50%).
  const atk = (name, len, rWind, rHit, bodyWind, bodyHit, extra = {}) => {
    const tw = len * 0.3, th = len * 0.5;
    const b = base({
      root: rootFloat(len),
      right_arm: { rotation: [[0, R0, C], [tw, rWind, C], [th, rHit, L], [len, R0, C]] },
      body: { rotation: [[0, [0, 0, 0], C], [tw, bodyWind, C], [th, bodyHit, L], [len, [0, 0, 0], C]] },
      left_arm: { rotation: kf([[0, L0], [tw, [L0[0] - 20, 0, L0[2] - 20]], [th, [L0[0] + 30, 0, L0[2] - 10]], [len, L0]]) },
      right_leg: o.float ? legsFloat : { rotation: kf([[0, [0, 0, 0]], [tw, [-10, 0, 0]], [th, [-25, 0, 0]], [len, [0, 0, 0]]]) },
      left_leg: o.float ? legsFloat : { rotation: kf([[0, [0, 0, 0]], [tw, [10, 0, 0]], [th, [20, 0, 0]], [len, [0, 0, 0]]]) },
    });
    if (o.weapon === 'swords') {
      b.left_arm = { rotation: [[0, L0, C], [tw, [rWind[0], -rWind[1], -rWind[2]], C], [th, [rHit[0], -rHit[1], -rHit[2]], L], [len, L0, C]] };
    }
    Object.assign(b, extra);
    list.push({ name, loop: false, length: len, bones: b });
  };
  if (o.style === 'heavy') {
    atk('attack_1', 0.9, [175, 0, 15], [35, 0, 5], [12, 0, 0], [-22, 0, 0]);
    atk('attack_2', 0.85, [95, 70, 60], [80, -60, -15], [0, 35, 0], [0, -40, 0]);
    atk('attack_3', 1.1, [170, 20, 20], [20, 0, 0], [15, 20, 0], [-30, 0, 0],
      { root: { position: kf([[0, [0, 0, 0]], [0.33, [0, 6, 0]], [0.55, [0, 0, -3]], [1.1, [0, 0, 0]]]) } });
  } else if (o.style === 'lance') {
    atk('attack_1', 0.6 * k, [-20, 0, 10], [95, 0, 0], [8, 10, 0], [-14, -12, 0],
      { root: { position: kf([[0, [0, 0, 0]], [0.18, [0, 0, 1]], [0.3, [0, 0, -4]], [0.6, [0, 0, 0]]]) } });
    atk('attack_2', 0.7 * k, [100, 70, 70], [95, -70, -10], [0, 40, 0], [0, -45, 0]);
    atk('attack_3', 0.9 * k, [160, 0, 10], [70, 0, 0], [15, 0, 0], [-25, 0, 0],
      { root: { position: kf([[0, [0, 0, 0]], [0.27, [0, 4, 0]], [0.45, [0, 0, -5]], [0.9, [0, 0, 0]]]) } });
  } else {
    atk('attack_1', 0.6 * k, [150, 10, 40], [40, -30, -10], [0, 25, 0], [-8, -25, 0]);
    atk('attack_2', 0.6 * k, [95, 65, 60], [90, -65, -15], [0, 35, 0], [0, -40, 0]);
    atk('attack_3', 0.75 * k, [-30, 0, 15], [100, 0, 0], [10, 0, 0], [-15, 0, 0],
      { root: { position: kf([[0, [0, floatY, 0]], [0.22, [0, floatY, 1]], [0.37, [0, floatY, -4]], [0.75, [0, floatY, 0]]]) } });
  }
  // Especiales y otras
  o.specials(list, { R0, L0, base, kf, rootFloat, legsFloat, floatY });
  list.push({ name: 'cast', loop: false, length: 1, bones: base({
    root: rootFloat(1),
    left_arm: { rotation: kf([[0, L0], [0.25, [95, 0, -5]], [0.75, [100, 0, -5]], [1, L0]]) },
    right_arm: { rotation: kf([[0, R0], [0.25, [40, 0, 25]], [0.75, [40, 0, 25]], [1, R0]]) },
    body: { rotation: kf([[0, [0, 0, 0]], [0.25, [6, -10, 0]], [0.75, [6, -10, 0]], [1, [0, 0, 0]]]) },
    head: { rotation: kf([[0, [0, 0, 0]], [0.25, [5, -10, 0]], [1, [0, 0, 0]]]) },
  }) });
  list.push({ name: 'pose', loop: 'hold', length: 2, bones: base({
    root: rootFloat(2),
    right_arm: { rotation: kf([[0, R0], [0.6, [160, 0, 20]], [2, [165, 0, 18]]]) },
    left_arm: { rotation: kf([[0, L0], [0.6, [-10, 0, -35]], [2, [-10, 0, -35]]]) },
    body: { rotation: kf([[0, [0, 0, 0]], [0.6, [5, 15, 0]], [2, [5, 15, 0]]]) },
    head: { rotation: kf([[0, [0, 0, 0]], [0.6, [10, -12, 0]], [2, [10, -12, 0]]]) },
    right_leg: o.float ? legsFloat : { rotation: kf([[0, [0, 0, 0]], [0.6, [-10, 0, 10]], [2, [-10, 0, 10]]]) },
    left_leg: o.float ? legsFloat : { rotation: kf([[0, [0, 0, 0]], [0.6, [10, 0, -8]], [2, [10, 0, -8]]]) },
    cape: { rotation: kf([[0, [-6, 0, 0]], [0.6, [-30, 0, 0]], [2, [-26, 0, 0]]]) },
  }) });
  list.push({ name: 'kneel', loop: 'hold', length: 1, bones: base({
    root: { position: kf([[0, [0, 0, 0]], [0.6, [0, -6, 0]], [1, [0, -6, 0]]]) },
    body: { rotation: kf([[0, [0, 0, 0]], [0.6, [-18, 0, 0]], [1, [-18, 0, 0]]]) },
    head: { rotation: kf([[0, [0, 0, 0]], [0.6, [-20, 0, 0]], [1, [-20, 0, 0]]]) },
    right_leg: { rotation: kf([[0, [0, 0, 0]], [0.6, [80, 0, 0]], [1, [80, 0, 0]]]) },
    left_leg: { rotation: kf([[0, [0, 0, 0]], [0.6, [-30, 0, 0]], [1, [-30, 0, 0]]]) },
    right_arm: { rotation: kf([[0, R0], [0.6, [-10, 0, 10]], [1, [-10, 0, 10]]]) },
    left_arm: { rotation: kf([[0, L0], [0.6, [40, 0, 10]], [1, [40, 0, 10]]]) },
  }) });
  list.push({ name: 'hurt', loop: false, length: 0.3, bones: base({
    body: { rotation: kf([[0, [0, 0, 0]], [0.1, [14, 0, 0]], [0.3, [0, 0, 0]]]) },
    head: { rotation: kf([[0, [0, 0, 0]], [0.1, [16, 0, 0]], [0.3, [0, 0, 0]]]) },
  }) });
  // Limpia huesos sin datos
  for (const a of list) for (const k2 of Object.keys(a.bones)) if (!a.bones[k2]) delete a.bones[k2];
  return list;
}

module.exports = { build, ring, kf, C, L };
