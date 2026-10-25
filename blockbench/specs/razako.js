// Razako, el Ejecutor. 3 bloques de alto (48 px), textura 256x256.
// Convencion: mira a -Z, brazo derecho en +X, pies en y=0, 16 px = 1 bloque.
// Rotaciones de Blockbench: +rx = el brazo/pierna que cuelga va hacia ADELANTE (el torso, +rx = se echa hacia atras);
// +rz en brazo derecho = separarlo del cuerpo; +ry = girar hacia su izquierda.
// Las manos de las animaciones a dos manos se calculan con una IK numerica (ver grip()) para que agarren la espada.

const P = {
  armor_black: { color: '#1B1A1F', noise: 4 },
  armor_cracked: { color: '#1B1A1F', noise: 4 },
  armor_crimson: { color: '#6E0F16', noise: 5 },
  armor_edge: { color: '#3A3640', noise: 5 },
  leather: { color: '#2B1E17', noise: 5 },
  cloth: { color: '#4A0C12', noise: 4, edge: false },
  iron: { color: '#585B62', noise: 7 },
  blade: { color: '#2E2F35', noise: 5 },
  blade_edge: { color: '#9AA0A6', noise: 9 },
  glow_red: { color: '#FF2A2A', noise: 4, edge: false },
  glow_green: { color: '#4CFF7A', noise: 3, edge: false },
  cape: { color: '#3D0A0E', noise: 5, edge: false },
};

const bones = [
  { name: 'root', parent: null, pivot: [0, 0, 0] },
  { name: 'body', parent: 'root', pivot: [0, 20, 0] },
  { name: 'head', parent: 'body', pivot: [0, 40, 0] },
  { name: 'right_arm', parent: 'body', pivot: [12, 37, 0] },
  { name: 'left_arm', parent: 'body', pivot: [-12, 37, 0] },
  { name: 'weapon', parent: 'right_arm', pivot: [12, 15.5, 0] },
  { name: 'chain', parent: 'left_arm', pivot: [-12, 26, 0] },
  { name: 'chain_tail', parent: 'chain', pivot: [-12, 13.5, 0] },
  { name: 'right_leg', parent: 'root', pivot: [5, 20, 0] },
  { name: 'left_leg', parent: 'root', pivot: [-5, 20, 0] },
  { name: 'right_shin', parent: 'right_leg', pivot: [5, 10, 0] },
  { name: 'left_shin', parent: 'left_leg', pivot: [-5, 10, 0] },
  { name: 'cape', parent: 'body', pivot: [0, 38, 6.5] },
  { name: 'cape_low', parent: 'cape', pivot: [0, 24, 7.5] },
];

const cubes = [];
const C = (bone, name, from, to, mat, o = {}) => cubes.push({ bone, name, from, to, mat, ...o });
// Pareja simetrica: el cubo se define a la derecha (+X) y se espeja a la izquierda
const LR = (bone, name, from, to, mat, o = {}) => {
  C(bone, name + '_r', from, to, mat, o);
  const m = { ...o };
  if (o.rotation) m.rotation = [o.rotation[0], -o.rotation[1], -o.rotation[2]];
  if (o.origin) m.origin = [-o.origin[0], o.origin[1], o.origin[2]];
  C(bone.replace('right_', 'left_'), name + '_l', [-to[0], from[1], from[2]], [-from[0], to[1], to[2]], mat, m);
};

// ---- Piernas (muslo en right_leg, pantorrilla y bota en right_shin) ----
LR('right_leg', 'thigh', [1.5, 10, -4.5], [8.5, 21, 4.5], 'armor_black');
LR('right_leg', 'tasset', [2, 12.5, -5.6], [8, 21.5, -4.5], 'armor_crimson');
LR('right_leg', 'knee', [2, 8.5, -6.2], [8, 12.5, -4.5], 'armor_edge');
LR('right_shin', 'shin', [1.5, 3, -4], [8.5, 10, 4], 'armor_black');
LR('right_shin', 'shin_plate', [2, 3.5, -5], [8, 9.5, -4], 'armor_edge');
LR('right_shin', 'boot', [1, 0, -6.5], [9, 4, 5], 'leather');
LR('right_shin', 'toe', [1.5, 0, -8], [8.5, 3, -6.5], 'armor_edge');

// ---- Torso ----
C('body', 'pelvis', [-7, 17, -4.5], [7, 24, 4.5], 'armor_black');
C('body', 'belt', [-7.6, 22, -5], [7.6, 25, 5], 'leather');
C('body', 'buckle', [-2.5, 21.5, -5.8], [2.5, 25.5, -5], 'armor_crimson');
C('body', 'tabard', [-2, 11, -6.2], [2, 22, -5.7], 'cloth');
C('body', 'abdomen', [-6, 24, -3.5], [6, 30, 3.5], 'armor_edge');
C('body', 'chest', [-9, 29, -5], [9, 40, 5], 'armor_black');
C('body', 'chest_plate', [-7.5, 30, -6.3], [7.5, 38.5, -5], 'armor_cracked');
C('body', 'core', [-2, 33, -6.9], [2, 37, -6.3], 'glow_green', { glow: true });
C('body', 'crack_a', [2.5, 34.5, -6.65], [6.8, 35.4, -6.3], 'glow_green', { glow: true, rotation: [0, 0, 28] });
C('body', 'crack_b', [-6.8, 31.5, -6.65], [-2.5, 32.4, -6.3], 'glow_green', { glow: true, rotation: [0, 0, -32] });
C('body', 'crack_c', [-0.4, 27.5, -6.65], [0.4, 33, -6.3], 'glow_green', { glow: true });
C('body', 'back_plate', [-8, 30, 5], [8, 39, 6.5], 'armor_cracked');
C('body', 'back_crack', [-1, 31, 6.4], [1, 38, 6.8], 'glow_green', { glow: true, rotation: [0, 0, 8] });
C('body', 'gorget', [-5.5, 38.5, -5], [5.5, 41, 5], 'armor_edge');

// ---- Cabeza: yelmo con cuernos y visera de ranura roja ----
C('head', 'helm', [-4.5, 40.5, -4.5], [4.5, 48, 4.5], 'armor_black');
C('head', 'visor_top', [-4.8, 45.2, -5.4], [4.8, 47.5, -4.5], 'armor_edge');
C('head', 'visor_bot', [-4.8, 41.5, -5.4], [4.8, 44.2, -4.5], 'armor_edge');
C('head', 'visor_slit', [-3.8, 44.2, -5.1], [3.8, 45.2, -4.5], 'glow_red', { glow: true });
C('head', 'crest', [-0.8, 48, -4.5], [0.8, 50.5, 4.5], 'armor_crimson');
C('head', 'neck_guard', [-4, 41, 4.5], [4, 46.5, 5.6], 'armor_edge');
LR('head', 'cheek', [4.5, 41, -3.5], [5.5, 45.5, 3], 'armor_edge');
// Cuerno de 5 tramos que sale hacia fuera y se curva hacia atras y arriba
LR('head', 'horn1', [4.5, 45, -1.5], [8.5, 48.5, 2], 'armor_edge');
LR('head', 'horn2', [7.5, 46, 1.2], [10.5, 49.2, 5], 'armor_edge');
LR('head', 'horn3', [9.5, 47.8, 4.2], [12, 50.6, 8], 'armor_crimson');
LR('head', 'horn4', [10.6, 50, 7], [12.6, 53, 9.8], 'armor_crimson');
LR('head', 'horn5', [11, 52.4, 8.8], [12.2, 55.5, 11], 'armor_edge');

// ---- Brazos: hombreras enormes, 32 px de ancho total ----
LR('right_arm', 'pauldron_top', [8, 39.5, -6.5], [16, 42, 6.5], 'armor_crimson');
LR('right_arm', 'pauldron_mid', [9, 36.5, -6], [16.8, 39.5, 6], 'armor_cracked');
LR('right_arm', 'pauldron_low', [10, 33.5, -5.5], [16.5, 36.5, 5.5], 'armor_edge');
LR('right_arm', 'spike', [11.5, 42, -1.2], [13.8, 47.5, 1.2], 'armor_edge', { rotation: [0, 0, -14] });
LR('right_arm', 'upper_arm', [9.5, 27, -3.5], [14.5, 36, 3.5], 'armor_black');
LR('right_arm', 'elbow', [9, 25.5, -4.3], [15, 29.5, 4.3], 'armor_edge');
LR('right_arm', 'forearm', [9.5, 17.5, -3.5], [14.5, 26, 3.5], 'leather');
LR('right_arm', 'fist', [9.5, 13.5, -3.5], [14.5, 17.5, 3.5], 'leather');
C('right_arm', 'vambrace_r', [9, 18, -4.3], [15, 25, 4.3], 'armor_cracked');
C('right_arm', 'arm_crack_r', [11.4, 19, -4.65], [12.6, 24, -4.25], 'glow_green', { glow: true, rotation: [0, 0, 12] });

// ---- Cadena enrollada en el antebrazo izquierdo + cola colgante ----
for (let i = 0; i < 4; i++) {
  C('chain', 'coil_' + i, [-15.2, 18.2 + i * 2, -4.4], [-8.8, 19.3 + i * 2, 4.4], 'iron', { rotation: [0, i % 2 ? 9 : -9, 0] });
}
C('chain_tail', 'link_1', [-13, 10.5, -0.8], [-11, 13.5, 0.8], 'iron');
C('chain_tail', 'link_2', [-12.8, 7.5, -1], [-11.2, 10.5, 1], 'iron');
C('chain_tail', 'link_3', [-13, 5, -0.8], [-11, 8, 0.8], 'iron');
C('chain_tail', 'ball', [-14, 1.5, -2], [-10, 5.5, 2], 'armor_edge');
C('chain_tail', 'ball_spike_x', [-15.6, 3, -0.6], [-8.4, 4, 0.6], 'iron');
C('chain_tail', 'ball_spike_z', [-12.6, 3, -3.6], [-11.4, 4, 3.6], 'iron');

// ---- Sentencia: espadon-cuchilla. Hoja de 40 px (y 24..64) en el sistema local del arma (hoja hacia +Y) ----
C('weapon', 'handle', [10.8, 6.5, -1.2], [13.2, 22.5, 1.2], 'leather');
C('weapon', 'pommel', [10.2, 3.8, -1.8], [13.8, 6.5, 1.8], 'armor_edge');
C('weapon', 'guard', [4.5, 22.5, -2], [19.5, 24.5, 2], 'armor_edge');
C('weapon', 'guard_core', [10, 21.8, -2.7], [14, 25, 2.7], 'armor_crimson');
C('weapon', 'blade_spine', [8.5, 24.5, -1.45], [10.5, 56, 1.45], 'blade');
C('weapon', 'blade_mid', [10.5, 24.5, -1], [14, 56, 1], 'blade');
C('weapon', 'blade_edge', [14, 24.5, -0.6], [15.5, 56, 0.6], 'blade_edge');
C('weapon', 'tip_1', [10.3, 56, -0.9], [15.5, 58, 0.9], 'blade');
C('weapon', 'tip_2', [12.1, 58, -0.8], [15.5, 60, 0.8], 'blade');
C('weapon', 'tip_3', [13.9, 60, -0.6], [15.5, 62.2, 0.6], 'blade_edge');
C('weapon', 'blade_crack_a', [11, 28, -1.25], [11.8, 38, 1.25], 'glow_green', { glow: true, rotation: [0, 0, 6] });
C('weapon', 'blade_crack_b', [12.2, 38.5, -1.25], [13, 50, 1.25], 'glow_green', { glow: true, rotation: [0, 0, -5] });
C('weapon', 'blade_crack_c', [10.7, 46, -1.25], [11.3, 53, 1.25], 'glow_green', { glow: true });

// ---- Capa rasgada en dos segmentos ----
C('cape', 'cape_top', [-8.5, 24, 6.6], [8.5, 38.5, 7.8], 'cape');
C('cape_low', 'cape_low_l', [-8.5, 13, 7.1], [-3, 24, 8.3], 'cape');
C('cape_low', 'cape_low_m', [-3, 7, 7.1], [3, 24, 8.3], 'cape');
C('cape_low', 'cape_low_r', [3, 11, 7.1], [8.5, 24, 8.3], 'cape');

// =====================================================================================
// IK de las dos manos: la mano izquierda debe agarrar el mango de la espada (6 px por debajo de la derecha).
// =====================================================================================
const D = Math.PI / 180;
function Rm(r) {
  const [a, b, c] = r.map((v) => v * D);
  const ca = Math.cos(a), sa = Math.sin(a), cb = Math.cos(b), sb = Math.sin(b), cc = Math.cos(c), sc = Math.sin(c);
  return [[cc * cb, cc * sb * sa - sc * ca, cc * sb * ca + sc * sa], [sc * cb, sc * sb * sa + cc * ca, sc * sb * ca - cc * sa], [-sb, cb * sa, cb * ca]];
}
const mv = (R, v) => [0, 1, 2].map((i) => R[i][0] * v[0] + R[i][1] * v[1] + R[i][2] * v[2]);
const add = (a, b) => a.map((v, i) => v + b[i]);
const sub = (a, b) => a.map((v, i) => v - b[i]);
const PIV_R = [12, 37, 0], HAND_R = [12, 15.5, 0], PIV_L = [-12, 37, 0], HAND_L = [-12, 15.5, 0];
const GRIP = 6; // distancia entre manos sobre el mango
function gripTarget(ra, w) {
  const local = add(HAND_R, mv(Rm(w), [0, -GRIP, 0]));
  return add(PIV_R, mv(Rm(ra), sub(local, PIV_R)));
}
function leftHand(l) { return add(PIV_L, mv(Rm(l), sub(HAND_L, PIV_L))); }
function solve3(A, b) {
  const det = (m) => m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
  const d = det(A);
  if (Math.abs(d) < 1e-12) return [0, 0, 0];
  return [0, 1, 2].map((c) => det(A.map((row, i) => row.map((v, j) => (j === c ? b[i] : v)))) / d);
}
function ikLeft(target, seed) {
  let th = seed.slice();
  const reg = 0.01;
  for (let it = 0; it < 80; it++) {
    const f = (t) => { const p = leftHand(t); return [...sub(p, target), ...t.map((v, i) => reg * (v - seed[i]))]; };
    const r = f(th);
    const J = [];
    for (let k = 0; k < 3; k++) {
      const t2 = th.slice(); t2[k] += 0.05;
      const r2 = f(t2);
      J.push(r2.map((v, i) => (v - r[i]) / 0.05));
    }
    // J[k][i] = d r_i / d th_k ; normal equations
    const A = [0, 1, 2].map((a) => [0, 1, 2].map((b) => J[a].reduce((s, v, i) => s + v * J[b][i], 0) + (a === b ? 0.05 : 0)));
    const g = [0, 1, 2].map((a) => -J[a].reduce((s, v, i) => s + v * r[i], 0));
    const dth = solve3(A, g);
    th = th.map((v, i) => v + Math.max(-15, Math.min(15, dth[i])));
  }
  const err = Math.hypot(...sub(leftHand(th), target));
  return { th, err };
}
let lastLeft = [50, 0, 20];
const gripLog = [];
// frames: [t, ra, w, interp]  ->  { right_arm, weapon, left_arm } con la mano izquierda calculada
function grip(name, frames) {
  const out = { right_arm: [], weapon: [], left_arm: [] };
  let seed = lastLeft;
  for (const [t, ra, w, i] of frames) {
    const tgt = gripTarget(ra, w);
    let best = null;
    for (const s of [seed, [60, 0, 25], [20, 0, 10], [100, 0, 30], [-30, 0, 20]]) {
      const r = ikLeft(tgt, s);
      if (!best || r.err < best.err) best = r;
    }
    seed = best.th;
    if (best.err > 1.2) gripLog.push(`${name} t=${t}: error de agarre ${best.err.toFixed(1)} px`);
    out.right_arm.push([t, ra, i || 'catmullrom']);
    out.weapon.push([t, w, i || 'catmullrom']);
    out.left_arm.push([t, best.th.map((v) => +v.toFixed(1)), i || 'catmullrom']);
  }
  lastLeft = seed;
  return out;
}
const CR = 'catmullrom', LN = 'linear';
// Suma de canales: {hueso:{rotation:[...]}} (los dos primeros argumentos son fusionados por hueso)
function merge(...parts) {
  const res = {};
  for (const p of parts) for (const [b, ch] of Object.entries(p)) res[b] = { ...(res[b] || {}), ...ch };
  return res;
}
const rot = (arr) => ({ rotation: arr });
const pos = (arr) => ({ position: arr });
function wrapGrip(g) { return { right_arm: rot(g.right_arm), weapon: rot(g.weapon), left_arm: rot(g.left_arm) }; }
// Poses base reutilizadas
const READY = [[30, 0, -32], [-62, 0, 0]]; // espada en guardia, dos manos
const SWEEP = [[50, 0, -15], [-140, 0, 0]]; // hoja horizontal hacia delante
const HIGH = [[160, 0, -15], [-70, 0, 0]]; // espada sobre la cabeza, hoja atras
const ground = [[40, 0, -25], [-150, 0, 0]]; // tajo vertical al suelo

const animations = [];

// ---------------- idle (2 s, bucle) ----------------
{
  const g = grip('idle', [
    [0, [30, 0, -32], [-62, 0, 0]], [0.5, [31.5, 0, -31], [-61, 0, 0]], [1, [34, 0, -30], [-59, 0, 0]],
    [1.5, [31.5, 0, -31], [-61, 0, 0]], [2, [30, 0, -32], [-62, 0, 0]],
  ]);
  animations.push({
    name: 'idle', loop: true, length: 2,
    bones: merge(wrapGrip(g), {
      root: pos([[0, [0, 0, 0], CR], [1, [0, -0.7, 0], CR], [2, [0, 0, 0], CR]]),
      body: rot([[0, [0, 0, 0], CR], [1, [2.5, 0, 0.8], CR], [2, [0, 0, 0], CR]]),
      head: rot([[0, [0, 0, 0], CR], [0.5, [1, 5, 0], CR], [1, [-2, 0, 0], CR], [1.5, [1, -5, 0], CR], [2, [0, 0, 0], CR]]),
      right_leg: rot([[0, [-6, 0, 3], CR], [2, [-6, 0, 3], CR]]),
      left_leg: rot([[0, [8, 0, -3], CR], [2, [8, 0, -3], CR]]),
      right_shin: rot([[0, [4, 0, 0], CR], [2, [4, 0, 0], CR]]),
      left_shin: rot([[0, [-8, 0, 0], CR], [2, [-8, 0, 0], CR]]),
      cape: rot([[0, [-5, 0, 0], CR], [0.7, [-10, 0, 1.5], CR], [1.4, [-6, 0, -1.5], CR], [2, [-5, 0, 0], CR]]),
      cape_low: rot([[0, [-3, 0, 0], CR], [0.9, [-14, 0, 2], CR], [1.6, [-5, 0, -2], CR], [2, [-3, 0, 0], CR]]),
      chain: rot([[0, [0, 0, 2], CR], [1, [0, 0, -2], CR], [2, [0, 0, 2], CR]]),
      chain_tail: rot([[0, [4, 0, 3], CR], [1, [-6, 0, -4], CR], [2, [4, 0, 3], CR]]),
    }),
  });
}

// ---------------- walk (1 s, bucle) ----------------
{
  const g = grip('walk', [
    [0, [22, 0, -28], [-50, 0, 0]], [0.25, [26, 0, -28], [-50, 0, 0]], [0.5, [22, 0, -28], [-50, 0, 0]],
    [0.75, [26, 0, -28], [-50, 0, 0]], [1, [22, 0, -28], [-50, 0, 0]],
  ]);
  animations.push({
    name: 'walk', loop: true, length: 1,
    bones: merge(wrapGrip(g), {
      root: pos([[0, [0, -2, 0], CR], [0.25, [0, 0, 0], CR], [0.5, [0, -2, 0], CR], [0.75, [0, 0, 0], CR], [1, [0, -2, 0], CR]]),
      body: rot([[0, [-3, -6, 2], CR], [0.25, [-1, 0, 0], CR], [0.5, [-3, 6, -2], CR], [0.75, [-1, 0, 0], CR], [1, [-3, -6, 2], CR]]),
      head: rot([[0, [3, 5, -1], CR], [0.5, [3, -5, 1], CR], [1, [3, 5, -1], CR]]),
      right_leg: rot([[0, [26, 0, 1], CR], [0.25, [0, 0, 1], CR], [0.5, [-26, 0, 1], CR], [0.75, [0, 0, 1], CR], [1, [26, 0, 1], CR]]),
      left_leg: rot([[0, [-26, 0, -1], CR], [0.25, [0, 0, -1], CR], [0.5, [26, 0, -1], CR], [0.75, [0, 0, -1], CR], [1, [-26, 0, -1], CR]]),
      right_shin: rot([[0, [0, 0, 0], CR], [0.25, [-6, 0, 0], CR], [0.5, [-10, 0, 0], CR], [0.75, [-42, 0, 0], CR], [1, [0, 0, 0], CR]]),
      left_shin: rot([[0, [-10, 0, 0], CR], [0.25, [-42, 0, 0], CR], [0.5, [0, 0, 0], CR], [0.75, [-6, 0, 0], CR], [1, [-10, 0, 0], CR]]),
      cape: rot([[0, [-12, 0, 2], CR], [0.5, [-20, 0, -2], CR], [1, [-12, 0, 2], CR]]),
      cape_low: rot([[0, [-14, 0, 3], CR], [0.35, [-30, 0, -3], CR], [0.85, [-20, 0, 3], CR], [1, [-14, 0, 3], CR]]),
      chain: rot([[0, [0, 0, 4], CR], [0.5, [0, 0, -4], CR], [1, [0, 0, 4], CR]]),
      chain_tail: rot([[0, [10, 0, 6], CR], [0.5, [-10, 0, -6], CR], [1, [10, 0, 6], CR]]),
    }),
  });
}

// ---------------- run (0.6 s, bucle) ----------------
{
  const T = [0, 0.15, 0.3, 0.45, 0.6];
  animations.push({
    name: 'run', loop: true, length: 0.6,
    bones: {
      root: pos(T.map((t, i) => [t, [0, i % 2 ? 3 : -2, 0], CR])),
      body: rot([[0, [-16, -8, 2], CR], [0.3, [-16, 8, -2], CR], [0.6, [-16, -8, 2], CR]]),
      head: rot([[0, [16, 6, 0], CR], [0.3, [16, -6, 0], CR], [0.6, [16, 6, 0], CR]]),
      right_leg: rot([[0, [52, 0, 0], CR], [0.15, [10, 0, 0], CR], [0.3, [-48, 0, 0], CR], [0.45, [10, 0, 0], CR], [0.6, [52, 0, 0], CR]]),
      left_leg: rot([[0, [-48, 0, 0], CR], [0.15, [10, 0, 0], CR], [0.3, [52, 0, 0], CR], [0.45, [10, 0, 0], CR], [0.6, [-48, 0, 0], CR]]),
      right_shin: rot([[0, [-4, 0, 0], CR], [0.15, [-30, 0, 0], CR], [0.3, [-20, 0, 0], CR], [0.45, [-95, 0, 0], CR], [0.6, [-4, 0, 0], CR]]),
      left_shin: rot([[0, [-20, 0, 0], CR], [0.15, [-95, 0, 0], CR], [0.3, [-4, 0, 0], CR], [0.45, [-30, 0, 0], CR], [0.6, [-20, 0, 0], CR]]),
      // espada a un lado, arrastrando la hoja hacia atras
      right_arm: rot([[0, [-30, 0, -6], CR], [0.3, [-42, 0, -6], CR], [0.6, [-30, 0, -6], CR]]),
      weapon: rot([[0, [90, 0, 0], CR], [0.6, [90, 0, 0], CR]]),
      left_arm: rot([[0, [50, 0, -10], CR], [0.3, [-45, 0, -10], CR], [0.6, [50, 0, -10], CR]]),
      cape: rot([[0, [-42, 0, 3], CR], [0.3, [-52, 0, -3], CR], [0.6, [-42, 0, 3], CR]]),
      cape_low: rot([[0, [-55, 0, 4], CR], [0.2, [-72, 0, -4], CR], [0.45, [-60, 0, 4], CR], [0.6, [-55, 0, 4], CR]]),
      chain: rot([[0, [0, 0, 6], CR], [0.3, [0, 0, -6], CR], [0.6, [0, 0, 6], CR]]),
      chain_tail: rot([[0, [30, 0, 8], CR], [0.3, [-20, 0, -8], CR], [0.6, [30, 0, 8], CR]]),
    },
  });
}

// ---------------- attack_1 (0.85 s): tajo vertical desde arriba ----------------
{
  const g = grip('attack_1', [
    [0, ...READY, CR], [0.1, [95, 0, -25], [-65, 0, 0], CR], [0.26, ...HIGH, CR], [0.34, [165, 0, -15], [-72, 0, 0], LN],
    [0.4, [90, 0, -20], [-110, 0, 0], LN], [0.45, ...ground, LN], [0.52, [42, 0, -25], [-148, 0, 0], CR],
    [0.7, [36, 0, -28], [-120, 0, 0], CR], [0.85, ...READY, CR],
  ]);
  animations.push({
    name: 'attack_1', loop: false, length: 0.85,
    bones: merge(wrapGrip(g), {
      root: pos([[0, [0, 0, 0], CR], [0.26, [0, -3, 3], CR], [0.34, [0, -3, 3], LN], [0.45, [0, -5, -4], LN], [0.6, [0, -4, -3], CR], [0.85, [0, 0, 0], CR]]),
      body: rot([[0, [0, 0, 0], CR], [0.26, [16, 0, 0], CR], [0.34, [18, 0, 0], LN], [0.45, [-26, 0, 0], LN], [0.6, [-18, 0, 0], CR], [0.85, [0, 0, 0], CR]]),
      head: rot([[0, [0, 0, 0], CR], [0.26, [-10, 0, 0], CR], [0.45, [20, 0, 0], LN], [0.85, [0, 0, 0], CR]]),
      right_leg: rot([[0, [-6, 0, 3], CR], [0.26, [-18, 0, 5], CR], [0.45, [30, 0, 4], LN], [0.6, [28, 0, 4], CR], [0.85, [-6, 0, 3], CR]]),
      left_leg: rot([[0, [8, 0, -3], CR], [0.26, [14, 0, -6], CR], [0.45, [-26, 0, -4], LN], [0.6, [-24, 0, -4], CR], [0.85, [8, 0, -3], CR]]),
      right_shin: rot([[0, [4, 0, 0], CR], [0.26, [-6, 0, 0], CR], [0.45, [-10, 0, 0], LN], [0.85, [4, 0, 0], CR]]),
      left_shin: rot([[0, [-8, 0, 0], CR], [0.26, [-10, 0, 0], CR], [0.45, [-36, 0, 0], LN], [0.6, [-30, 0, 0], CR], [0.85, [-8, 0, 0], CR]]),
      cape: rot([[0, [-5, 0, 0], CR], [0.26, [-4, 0, 0], CR], [0.45, [-34, 0, 0], LN], [0.62, [-16, 0, 0], CR], [0.85, [-5, 0, 0], CR]]),
      cape_low: rot([[0, [-3, 0, 0], CR], [0.26, [4, 0, 0], CR], [0.5, [-50, 0, 0], CR], [0.7, [-20, 0, 0], CR], [0.85, [-3, 0, 0], CR]]),
      chain_tail: rot([[0, [4, 0, 3], CR], [0.26, [-12, 0, 0], CR], [0.5, [30, 0, 0], CR], [0.85, [4, 0, 3], CR]]),
    }),
  });
}

// ---------------- attack_2 (0.75 s): barrido horizontal de derecha a izquierda ----------------
{
  const g = grip('attack_2', [
    [0, ...READY, CR], [0.1, [45, 0, -22], [-100, 0, 0], CR], [0.24, ...SWEEP, CR], [0.4, [52, 0, -12], [-142, 0, 0], LN],
    [0.5, [55, 0, -10], [-145, 0, 0], CR], [0.75, ...READY, CR],
  ]);
  animations.push({
    name: 'attack_2', loop: false, length: 0.75,
    bones: merge(wrapGrip(g), {
      root: pos([[0, [0, 0, 0], CR], [0.24, [0, -3, 2], CR], [0.4, [0, -3, -3], LN], [0.55, [0, -2, -2], CR], [0.75, [0, 0, 0], CR]]),
      body: rot([[0, [0, 0, 0], CR], [0.24, [4, -48, 3], CR], [0.28, [4, -48, 3], LN], [0.4, [-8, 62, -3], LN], [0.55, [-6, 52, -2], CR], [0.75, [0, 0, 0], CR]]),
      head: rot([[0, [0, 0, 0], CR], [0.24, [0, 30, 0], CR], [0.4, [4, -40, 0], LN], [0.55, [3, -34, 0], CR], [0.75, [0, 0, 0], CR]]),
      right_leg: rot([[0, [-6, 0, 3], CR], [0.24, [-10, 0, 8], CR], [0.4, [10, 0, 7], LN], [0.75, [-6, 0, 3], CR]]),
      left_leg: rot([[0, [8, 0, -3], CR], [0.24, [14, 0, -9], CR], [0.4, [-6, 0, -8], LN], [0.75, [8, 0, -3], CR]]),
      right_shin: rot([[0, [4, 0, 0], CR], [0.24, [-14, 0, 0], CR], [0.4, [-12, 0, 0], LN], [0.75, [4, 0, 0], CR]]),
      left_shin: rot([[0, [-8, 0, 0], CR], [0.24, [-22, 0, 0], CR], [0.4, [-14, 0, 0], LN], [0.75, [-8, 0, 0], CR]]),
      cape: rot([[0, [-5, 0, 0], CR], [0.24, [-6, 0, -8], CR], [0.42, [-26, 0, 22], LN], [0.6, [-14, 0, 8], CR], [0.75, [-5, 0, 0], CR]]),
      cape_low: rot([[0, [-3, 0, 0], CR], [0.24, [-8, 0, -14], CR], [0.46, [-30, 0, 38], CR], [0.62, [-14, 0, 12], CR], [0.75, [-3, 0, 0], CR]]),
      chain_tail: rot([[0, [4, 0, 3], CR], [0.24, [0, 0, 24], CR], [0.46, [0, 0, -40], CR], [0.75, [4, 0, 3], CR]]),
    }),
  });
}

// ---------------- attack_3 (1.2 s): agacharse y girar 360 grados con la hoja extendida ----------------
{
  const g = grip('attack_3', [
    [0, ...READY, CR], [0.18, [8, 0, -26], [-24, 0, 0], CR], [0.4, [10, 0, -28], [-10, 0, 0], CR], [0.5, ...SWEEP, LN],
    [0.8, [52, 0, -14], [-142, 0, 0], LN], [0.92, [44, 0, -22], [-132, 0, 0], CR], [1.2, ...READY, CR],
  ]);
  animations.push({
    name: 'attack_3', loop: false, length: 1.2,
    bones: merge(wrapGrip(g), {
      root: Object.assign({},
        pos([[0, [0, 0, 0], CR], [0.4, [0, -6, 2], CR], [0.5, [0, -3, 0], LN], [0.8, [0, -3, 0], LN], [0.95, [0, -4, 0], CR], [1.2, [0, 0, 0], CR]]),
        rot([[0, [0, 0, 0], CR], [0.4, [0, -45, 0], CR], [0.5, [0, -40, 0], LN], [0.82, [0, 320, 0], LN], [1.05, [0, 358, 0], CR], [1.2, [0, 360, 0], CR]]),
      ),
      body: rot([[0, [0, 0, 0], CR], [0.4, [-10, -12, 4], CR], [0.5, [-14, 0, 8], LN], [0.8, [-14, 0, 8], LN], [1.0, [-8, 0, 2], CR], [1.2, [0, 0, 0], CR]]),
      head: rot([[0, [0, 0, 0], CR], [0.4, [2, 20, 0], CR], [0.8, [6, 0, 0], LN], [1.2, [0, 0, 0], CR]]),
      right_leg: rot([[0, [-6, 0, 3], CR], [0.4, [38, 0, 8], CR], [0.5, [26, 0, 10], LN], [0.8, [16, 0, 10], LN], [1.2, [-6, 0, 3], CR]]),
      left_leg: rot([[0, [8, 0, -3], CR], [0.4, [-30, 0, -9], CR], [0.5, [-18, 0, -10], LN], [0.8, [-12, 0, -10], LN], [1.2, [8, 0, -3], CR]]),
      right_shin: rot([[0, [4, 0, 0], CR], [0.4, [-40, 0, 0], CR], [0.8, [-20, 0, 0], LN], [1.2, [4, 0, 0], CR]]),
      left_shin: rot([[0, [-8, 0, 0], CR], [0.4, [-50, 0, 0], CR], [0.8, [-20, 0, 0], LN], [1.2, [-8, 0, 0], CR]]),
      cape: rot([[0, [-5, 0, 0], CR], [0.4, [-6, 0, 0], CR], [0.55, [-50, 0, 0], LN], [0.85, [-60, 0, 0], LN], [1.2, [-5, 0, 0], CR]]),
      cape_low: rot([[0, [-3, 0, 0], CR], [0.4, [2, 0, 0], CR], [0.6, [-70, 0, 0], CR], [0.9, [-80, 0, 0], CR], [1.2, [-3, 0, 0], CR]]),
      chain_tail: rot([[0, [4, 0, 3], CR], [0.5, [60, 0, 0], CR], [0.85, [75, 0, 0], CR], [1.2, [4, 0, 3], CR]]),
    }),
  });
}

// ---------------- charge (0.5 s, bucle): embestida con la hoja al frente ----------------
{
  const g = grip('charge', [
    [0, [38, 0, -22], [-128, 0, 0]], [0.25, [42, 0, -22], [-132, 0, 0]], [0.5, [38, 0, -22], [-128, 0, 0]],
  ]);
  animations.push({
    name: 'charge', loop: true, length: 0.5,
    bones: merge(wrapGrip(g), {
      root: pos([[0, [0, -3, 0], CR], [0.125, [0, 2, 0], CR], [0.25, [0, -3, 0], CR], [0.375, [0, 2, 0], CR], [0.5, [0, -3, 0], CR]]),
      body: rot([[0, [-30, -6, 2], CR], [0.25, [-30, 6, -2], CR], [0.5, [-30, -6, 2], CR]]),
      head: rot([[0, [24, 0, 0], CR], [0.5, [24, 0, 0], CR]]),
      right_leg: rot([[0, [58, 0, 3], CR], [0.125, [8, 0, 3], CR], [0.25, [-54, 0, 3], CR], [0.375, [8, 0, 3], CR], [0.5, [58, 0, 3], CR]]),
      left_leg: rot([[0, [-54, 0, -3], CR], [0.125, [8, 0, -3], CR], [0.25, [58, 0, -3], CR], [0.375, [8, 0, -3], CR], [0.5, [-54, 0, -3], CR]]),
      right_shin: rot([[0, [-4, 0, 0], CR], [0.125, [-30, 0, 0], CR], [0.25, [-20, 0, 0], CR], [0.375, [-100, 0, 0], CR], [0.5, [-4, 0, 0], CR]]),
      left_shin: rot([[0, [-20, 0, 0], CR], [0.125, [-100, 0, 0], CR], [0.25, [-4, 0, 0], CR], [0.375, [-30, 0, 0], CR], [0.5, [-20, 0, 0], CR]]),
      cape: rot([[0, [-58, 0, 3], CR], [0.25, [-66, 0, -3], CR], [0.5, [-58, 0, 3], CR]]),
      cape_low: rot([[0, [-70, 0, 4], CR], [0.2, [-86, 0, -4], CR], [0.5, [-70, 0, 4], CR]]),
      chain_tail: rot([[0, [70, 0, 6], CR], [0.25, [60, 0, -6], CR], [0.5, [70, 0, 6], CR]]),
    }),
  });
}

// ---------------- leap (1.2 s): agacharse, saltar alto y caer con tajo ----------------
{
  const g = grip('leap', [
    [0, ...READY, CR], [0.3, [60, 0, -28], [-70, 0, 0], CR], [0.5, [120, 0, -20], [-70, 0, 0], CR], [0.65, ...HIGH, CR],
    [0.78, [150, 0, -15], [-72, 0, 0], LN], [0.88, ...ground, LN], [0.96, [42, 0, -25], [-148, 0, 0], CR], [1.2, ...READY, CR],
  ]);
  animations.push({
    name: 'leap', loop: false, length: 1.2,
    bones: merge(wrapGrip(g), {
      root: pos([[0, [0, 0, 0], CR], [0.3, [0, -6, 2], CR], [0.42, [0, 8, -2], LN], [0.62, [0, 20, -4], CR], [0.78, [0, 18, -3], LN], [0.88, [0, -4, -4], LN], [0.96, [0, -5, -2], CR], [1.2, [0, 0, 0], CR]]),
      body: rot([[0, [0, 0, 0], CR], [0.3, [-14, 0, 0], CR], [0.45, [8, 0, 0], CR], [0.65, [10, 0, 0], CR], [0.78, [14, 0, 0], LN], [0.88, [-26, 0, 0], LN], [1.0, [-14, 0, 0], CR], [1.2, [0, 0, 0], CR]]),
      head: rot([[0, [0, 0, 0], CR], [0.3, [14, 0, 0], CR], [0.62, [-8, 0, 0], CR], [0.88, [22, 0, 0], LN], [1.2, [0, 0, 0], CR]]),
      right_leg: rot([[0, [-6, 0, 3], CR], [0.3, [44, 0, 8], CR], [0.45, [-20, 0, 6], CR], [0.65, [38, 0, 4], CR], [0.78, [30, 0, 4], CR], [0.88, [24, 0, 4], LN], [1.2, [-6, 0, 3], CR]]),
      left_leg: rot([[0, [8, 0, -3], CR], [0.3, [-44, 0, -8], CR], [0.45, [-30, 0, -6], CR], [0.65, [8, 0, -4], CR], [0.78, [-6, 0, -4], CR], [0.88, [-22, 0, -4], LN], [1.2, [8, 0, -3], CR]]),
      right_shin: rot([[0, [4, 0, 0], CR], [0.3, [-70, 0, 0], CR], [0.45, [-10, 0, 0], CR], [0.65, [-60, 0, 0], CR], [0.88, [-12, 0, 0], LN], [1.2, [4, 0, 0], CR]]),
      left_shin: rot([[0, [-8, 0, 0], CR], [0.3, [-30, 0, 0], CR], [0.45, [-10, 0, 0], CR], [0.65, [-90, 0, 0], CR], [0.88, [-30, 0, 0], LN], [1.2, [-8, 0, 0], CR]]),
      cape: rot([[0, [-5, 0, 0], CR], [0.3, [-4, 0, 0], CR], [0.5, [-50, 0, 0], CR], [0.7, [-24, 0, 0], CR], [0.9, [-60, 0, 0], LN], [1.2, [-5, 0, 0], CR]]),
      cape_low: rot([[0, [-3, 0, 0], CR], [0.3, [6, 0, 0], CR], [0.55, [-70, 0, 0], CR], [0.8, [-20, 0, 0], CR], [1.0, [-70, 0, 0], CR], [1.2, [-3, 0, 0], CR]]),
      chain_tail: rot([[0, [4, 0, 3], CR], [0.3, [-10, 0, 0], CR], [0.55, [40, 0, 0], CR], [0.8, [-20, 0, 0], CR], [1.2, [4, 0, 3], CR]]),
    }),
  });
}

// ---------------- roar (1.6 s): ruge con la espada al cielo ----------------
{
  const shake = [];
  for (let i = 0; i < 14; i++) shake.push([0.55 + i * 0.06, [0, 0, i % 2 ? 1.6 : -1.6], LN]);
  animations.push({
    name: 'roar', loop: false, length: 1.6,
    bones: {
      root: pos([[0, [0, 0, 0], CR], [0.45, [0, -2, 0], CR], [0.55, [0, -1, 0], CR], [1.4, [0, -1, 0], CR], [1.6, [0, 0, 0], CR]]),
      body: rot([[0, [0, 0, 0], CR], [0.2, [-8, 0, 0], CR], [0.5, [14, 0, 0], CR], [1.4, [12, 0, 0], CR], [1.6, [0, 0, 0], CR]]),
      head: rot([[0, [0, 0, 0], CR], [0.2, [-12, 0, 0], CR], [0.5, [32, 0, 0], CR], [1.4, [30, 0, 0], CR], [1.6, [0, 0, 0], CR]]),
      right_arm: rot([[0, [30, 0, -32], CR], [0.2, [10, 0, 0], CR], [0.5, [0, 0, 150], CR], [1.4, [0, 0, 150], CR], [1.6, [30, 0, -32], CR]]),
      weapon: rot([[0, [-62, 0, 0], CR], [0.2, [-20, 0, 0], CR], [0.5, [0, 0, -150], CR], [1.4, [0, 0, -150], CR], [1.6, [-62, 0, 0], CR]]),
      left_arm: rot([[0, [30, 0, 20], CR], [0.2, [-10, 0, -10], CR], [0.5, [-10, 0, -105], CR], [1.4, [-10, 0, -105], CR], [1.6, [30, 0, 20], CR]]),
      right_leg: rot([[0, [-6, 0, 3], CR], [0.5, [-4, 0, 14], CR], [1.4, [-4, 0, 14], CR], [1.6, [-6, 0, 3], CR]]),
      left_leg: rot([[0, [8, 0, -3], CR], [0.5, [4, 0, -14], CR], [1.4, [4, 0, -14], CR], [1.6, [8, 0, -3], CR]]),
      right_shin: rot([[0, [4, 0, 0], CR], [0.5, [-8, 0, 0], CR], [1.6, [4, 0, 0], CR]]),
      left_shin: rot([[0, [-8, 0, 0], CR], [0.5, [-8, 0, 0], CR], [1.6, [-8, 0, 0], CR]]),
      cape: rot([[0, [-5, 0, 0], CR], [0.2, [4, 0, 0], CR], [0.5, [-42, 0, 0], CR], [1.0, [-30, 0, 3], CR], [1.4, [-40, 0, -3], CR], [1.6, [-5, 0, 0], CR]]),
      cape_low: rot([[0, [-3, 0, 0], CR], [0.2, [8, 0, 0], CR], [0.55, [-60, 0, 0], CR], [1.1, [-35, 0, 4], CR], [1.4, [-55, 0, -4], CR], [1.6, [-3, 0, 0], CR]]),
      chain_tail: rot([[0, [4, 0, 3], CR], [0.5, [-30, 0, 8], CR], [1.0, [20, 0, -8], CR], [1.6, [4, 0, 3], CR]]),
    },
  });
  // temblor del rugido en el torso
  animations[animations.length - 1].bones.body.rotation = [
    [0, [0, 0, 0], CR], [0.2, [-8, 0, 0], CR], [0.5, [14, 0, 0], CR],
    ...shake.map(([t, v]) => [t, [13, 0, v[2]], LN]), [1.4, [12, 0, 0], CR], [1.6, [0, 0, 0], CR],
  ];
}

// ---------------- block (hold): guardia con la espada vertical delante ----------------
{
  const B = [[78, 0, -36], [-78, 0, 0]];
  const g = grip('block', [[0, ...READY, CR], [0.15, ...B, CR], [0.3, [80, 0, -35], [-80, 0, 0], CR], [0.5, [78, 0, -36], [-78, 0, 0], CR]]);
  animations.push({
    name: 'block', loop: 'hold', length: 0.5,
    bones: merge(wrapGrip(g), {
      root: pos([[0, [0, 0, 0], CR], [0.15, [0, -3, 0], CR], [0.5, [0, -3, 0], CR]]),
      body: rot([[0, [0, 0, 0], CR], [0.15, [-8, 8, 0], CR], [0.5, [-8, 8, 0], CR]]),
      head: rot([[0, [0, 0, 0], CR], [0.15, [8, -8, 0], CR], [0.5, [8, -8, 0], CR]]),
      right_leg: rot([[0, [-6, 0, 3], CR], [0.15, [24, 0, 10], CR], [0.5, [24, 0, 10], CR]]),
      left_leg: rot([[0, [8, 0, -3], CR], [0.15, [-16, 0, -10], CR], [0.5, [-16, 0, -10], CR]]),
      right_shin: rot([[0, [4, 0, 0], CR], [0.15, [-40, 0, 0], CR], [0.5, [-40, 0, 0], CR]]),
      left_shin: rot([[0, [-8, 0, 0], CR], [0.15, [-26, 0, 0], CR], [0.5, [-26, 0, 0], CR]]),
      cape: rot([[0, [-5, 0, 0], CR], [0.15, [-26, 0, 0], CR], [0.5, [-12, 0, 0], CR]]),
      cape_low: rot([[0, [-3, 0, 0], CR], [0.15, [-40, 0, 0], CR], [0.5, [-16, 0, 0], CR]]),
      chain_tail: rot([[0, [4, 0, 3], CR], [0.15, [-30, 0, 0], CR], [0.5, [6, 0, 2], CR]]),
    }),
  });
}

// ---------------- horizon (2 s): carga larga y tajo horizontal de media vuelta ----------------
{
  const g = grip('horizon', [
    [0, ...READY, CR], [0.3, [20, 0, -26], [-30, 0, 0], CR], [0.9, [-22, 0, -22], [100, 0, 0], CR], [1.2, [-26, 0, -22], [104, 0, 0], CR],
    [1.45, [-26, 0, -22], [104, 0, 0], CR], [1.52, [30, 0, -20], [-110, 0, 0], LN], [1.7, ...SWEEP, LN], [2, ...READY, CR],
  ]);
  const trem = [];
  for (let i = 0; i < 6; i++) trem.push([1.2 + i * 0.04, [-10, -88 + (i % 2 ? 2 : -2), i % 2 ? 2.5 : -2.5], LN]);
  animations.push({
    name: 'horizon', loop: false, length: 2,
    bones: merge(wrapGrip(g), {
      root: pos([[0, [0, 0, 0], CR], [0.3, [0, -1, 0], CR], [0.9, [0, -7, 2], CR], [1.2, [0, -8, 2], CR], [1.45, [0, -8, 2], CR], [1.7, [0, -3, -3], LN], [2, [0, 0, 0], CR]]),
      body: rot([[0, [0, 0, 0], CR], [0.3, [2, -10, 0], CR], [0.9, [-10, -88, 0], CR], [1.2, [-10, -88, 0], CR], ...trem, [1.45, [-10, -88, 0], CR], [1.7, [-8, 88, 0], LN], [1.85, [-4, 82, 0], CR], [2, [0, 0, 0], CR]]),
      head: rot([[0, [0, 0, 0], CR], [0.9, [4, 40, 0], CR], [1.45, [4, 42, 0], CR], [1.7, [3, -50, 0], LN], [2, [0, 0, 0], CR]]),
      right_leg: rot([[0, [-6, 0, 3], CR], [0.9, [38, 0, 12], CR], [1.45, [38, 0, 12], CR], [1.7, [20, 0, 12], LN], [2, [-6, 0, 3], CR]]),
      left_leg: rot([[0, [8, 0, -3], CR], [0.9, [-34, 0, -12], CR], [1.45, [-34, 0, -12], CR], [1.7, [-14, 0, -12], LN], [2, [8, 0, -3], CR]]),
      right_shin: rot([[0, [4, 0, 0], CR], [0.9, [-56, 0, 0], CR], [1.45, [-56, 0, 0], CR], [1.7, [-30, 0, 0], LN], [2, [4, 0, 0], CR]]),
      left_shin: rot([[0, [-8, 0, 0], CR], [0.9, [-40, 0, 0], CR], [1.45, [-40, 0, 0], CR], [1.7, [-24, 0, 0], LN], [2, [-8, 0, 0], CR]]),
      cape: rot([[0, [-5, 0, 0], CR], [0.9, [-14, 0, 0], CR], [1.45, [-10, 0, 0], CR], [1.7, [-40, 0, 20], LN], [2, [-5, 0, 0], CR]]),
      cape_low: rot([[0, [-3, 0, 0], CR], [0.9, [-24, 0, 4], CR], [1.45, [-18, 0, -4], CR], [1.78, [-50, 0, 40], CR], [2, [-3, 0, 0], CR]]),
      chain_tail: rot([[0, [4, 0, 3], CR], [0.9, [40, 0, 0], CR], [1.7, [70, 0, 0], LN], [2, [4, 0, 3], CR]]),
    }),
  });
  const h = animations[animations.length - 1];
  // la espada vibra y crece un poco mientras se carga
  h.bones.weapon.scale = [[0, [1, 1, 1], LN], [0.9, [1, 1, 1], LN], [1.0, [1.04, 1.04, 1.04], LN], [1.1, [1, 1, 1], LN], [1.2, [1.06, 1.06, 1.06], LN], [1.3, [1, 1, 1], LN], [1.4, [1.08, 1.08, 1.08], LN], [1.5, [1, 1, 1], LN], [2, [1, 1, 1], LN]];
}

// ---------------- chain (1 s): lanza la cadena con la izquierda ----------------
{
  animations.push({
    name: 'chain', loop: false, length: 1,
    bones: {
      root: pos([[0, [0, 0, 0], CR], [0.3, [0, -2, 1], CR], [0.42, [0, -2, -2], LN], [0.75, [0, -1, -1], CR], [1, [0, 0, 0], CR]]),
      body: rot([[0, [0, 0, 0], CR], [0.3, [4, -34, 0], CR], [0.42, [-8, 32, 0], LN], [0.75, [-4, 20, 0], CR], [1, [0, 0, 0], CR]]),
      head: rot([[0, [0, 0, 0], CR], [0.3, [0, 20, 0], CR], [0.42, [3, -28, 0], LN], [0.75, [0, -16, 0], CR], [1, [0, 0, 0], CR]]),
      // la espada queda sujeta con la derecha
      right_arm: rot([[0, [30, 0, -32], CR], [0.3, [30, 0, -10], CR], [0.42, [36, 0, -14], CR], [1, [30, 0, -32], CR]]),
      weapon: rot([[0, [-62, 0, 0], CR], [0.3, [-100, 0, 0], CR], [0.42, [-110, 0, 0], CR], [1, [-62, 0, 0], CR]]),
      left_arm: rot([[0, [30, 0, 20], CR], [0.12, [-40, 0, -40], CR], [0.3, [-110, 0, -50], CR], [0.42, [90, 0, -6], LN], [0.75, [88, 0, -6], CR], [1, [30, 0, 20], CR]]),
      chain_tail: {
        rotation: [[0, [4, 0, 3], CR], [0.3, [360, 0, 0], LN], [0.42, [0, 0, 0], LN], [0.8, [0, 0, 0], CR], [1, [4, 0, 3], CR]],
        scale: [[0, [1, 1, 1], LN], [0.3, [1, 1, 1], LN], [0.45, [1, 4.2, 1], LN], [0.75, [1, 4.2, 1], LN], [0.95, [1, 1, 1], LN]],
      },
      right_leg: rot([[0, [-6, 0, 3], CR], [0.42, [12, 0, 8], CR], [1, [-6, 0, 3], CR]]),
      left_leg: rot([[0, [8, 0, -3], CR], [0.42, [-14, 0, -8], CR], [1, [8, 0, -3], CR]]),
      right_shin: rot([[0, [4, 0, 0], CR], [0.42, [-20, 0, 0], CR], [1, [4, 0, 0], CR]]),
      left_shin: rot([[0, [-8, 0, 0], CR], [0.42, [-20, 0, 0], CR], [1, [-8, 0, 0], CR]]),
      cape: rot([[0, [-5, 0, 0], CR], [0.42, [-26, 0, 8], CR], [1, [-5, 0, 0], CR]]),
      cape_low: rot([[0, [-3, 0, 0], CR], [0.46, [-40, 0, 14], CR], [1, [-3, 0, 0], CR]]),
    },
  });
}

// ---------------- kneel (1 s, hold): arrodillado, espada clavada en el suelo ----------------
{
  const g = grip('kneel', [[0, ...READY, CR], [0.5, [60, 0, -24], [100, 0, 0], CR], [1, [58, 0, -22], [122, 0, 0], CR]]);
  animations.push({
    name: 'kneel', loop: 'hold', length: 1,
    bones: merge(wrapGrip(g), {
      root: pos([[0, [0, 0, 0], CR], [0.7, [0, -10, -3], CR], [1, [0, -10, -3], CR]]),
      body: rot([[0, [0, 0, 0], CR], [0.7, [-16, 0, 0], CR], [1, [-18, 0, 0], CR]]),
      head: rot([[0, [0, 0, 0], CR], [0.7, [30, 0, 0], CR], [1, [34, 0, 0], CR]]),
      right_leg: rot([[0, [-6, 0, 3], CR], [0.7, [-4, 0, 6], CR], [1, [-4, 0, 6], CR]]),
      right_shin: rot([[0, [4, 0, 0], CR], [0.7, [-86, 0, 0], CR], [1, [-86, 0, 0], CR]]),
      left_leg: rot([[0, [8, 0, -3], CR], [0.7, [86, 0, -4], CR], [1, [86, 0, -4], CR]]),
      left_shin: rot([[0, [-8, 0, 0], CR], [0.7, [-86, 0, 0], CR], [1, [-86, 0, 0], CR]]),
      cape: rot([[0, [-5, 0, 0], CR], [0.5, [-30, 0, 0], CR], [1, [-8, 0, 0], CR]]),
      cape_low: rot([[0, [-3, 0, 0], CR], [0.6, [-34, 0, 0], CR], [1, [-10, 0, 0], CR]]),
      chain_tail: rot([[0, [4, 0, 3], CR], [0.5, [-20, 0, 0], CR], [1, [6, 0, 2], CR]]),
    }),
  });
}

// ---------------- hurt (0.35 s): retroceso ----------------
{
  const g = grip('hurt', [[0, ...READY, CR], [0.07, [10, 0, -26], [-46, 0, 0], LN], [0.2, [22, 0, -30], [-54, 0, 0], CR], [0.35, ...READY, CR]]);
  animations.push({
    name: 'hurt', loop: false, length: 0.35,
    bones: merge(wrapGrip(g), {
      root: pos([[0, [0, 0, 0], CR], [0.07, [0, -1.5, 3], LN], [0.35, [0, 0, 0], CR]]),
      body: rot([[0, [0, 0, 0], CR], [0.07, [16, 0, 4], LN], [0.2, [8, 0, 1], CR], [0.35, [0, 0, 0], CR]]),
      head: rot([[0, [0, 0, 0], CR], [0.07, [14, 8, 2], LN], [0.2, [6, 3, 0], CR], [0.35, [0, 0, 0], CR]]),
      right_leg: rot([[0, [-6, 0, 3], CR], [0.07, [-14, 0, 4], LN], [0.35, [-6, 0, 3], CR]]),
      left_leg: rot([[0, [8, 0, -3], CR], [0.07, [14, 0, -4], LN], [0.35, [8, 0, -3], CR]]),
      cape: rot([[0, [-5, 0, 0], CR], [0.07, [14, 0, 0], LN], [0.25, [-18, 0, 0], CR], [0.35, [-5, 0, 0], CR]]),
      cape_low: rot([[0, [-3, 0, 0], CR], [0.1, [22, 0, 0], LN], [0.28, [-24, 0, 0], CR], [0.35, [-3, 0, 0], CR]]),
      chain_tail: rot([[0, [4, 0, 3], CR], [0.07, [-40, 0, 10], LN], [0.35, [4, 0, 3], CR]]),
    }),
  });
}

// ---------------- pose (2 s, hold): presentacion, espada vertical ante el pecho ----------------
{
  const g = grip('pose', [[0, ...READY, CR], [0.8, [72, 0, -34], [-72, 0, 0], CR], [2, [70, 0, -34], [-70, 0, 0], CR]]);
  animations.push({
    name: 'pose', loop: 'hold', length: 2,
    bones: merge(wrapGrip(g), {
      root: pos([[0, [0, 0, 0], CR], [0.8, [0, 0, 0], CR], [2, [0, -0.5, 0], CR]]),
      body: rot([[0, [0, 0, 0], CR], [0.8, [5, 0, 0], CR], [2, [6, 0, 0], CR]]),
      head: rot([[0, [0, 0, 0], CR], [0.8, [10, 0, 0], CR], [2, [12, 0, 0], CR]]),
      right_leg: rot([[0, [-6, 0, 3], CR], [0.8, [2, 0, 9], CR], [2, [2, 0, 9], CR]]),
      left_leg: rot([[0, [8, 0, -3], CR], [0.8, [2, 0, -9], CR], [2, [2, 0, -9], CR]]),
      right_shin: rot([[0, [4, 0, 0], CR], [0.8, [-6, 0, 0], CR], [2, [-6, 0, 0], CR]]),
      left_shin: rot([[0, [-8, 0, 0], CR], [0.8, [-6, 0, 0], CR], [2, [-6, 0, 0], CR]]),
      cape: rot([[0, [-5, 0, 0], CR], [0.8, [-22, 0, 0], CR], [1.4, [-14, 0, 2], CR], [2, [-20, 0, -2], CR]]),
      cape_low: rot([[0, [-3, 0, 0], CR], [0.9, [-32, 0, 0], CR], [1.5, [-18, 0, 3], CR], [2, [-28, 0, -3], CR]]),
      chain_tail: rot([[0, [4, 0, 3], CR], [1, [-8, 0, 0], CR], [2, [4, 0, 3], CR]]),
    }),
  });
}

gripLog.forEach((l) => console.warn('IK', l));

module.exports = { name: 'razako', texture: [256, 256], palette: P, bones, cubes, animations };
