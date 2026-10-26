// Caza del Nexo (estilo TIE). Frente = -Z, derecha = +X. Centro de la cabina en (0,24,0) (16 px = 1 bloque).
// Huesos: root > cockpit (bola de voxeles; renderer sin culling) > window (marco + radios; el cristal lo dibuja el
// renderer en el plano z=-10.1 del hueso), interior (asiento, palancas, consola), pilot (solo con piloto IA);
// root > engine (toberas), pylon_l/r, wing_l/r.
// Ojo del piloto (camara en primera persona): (0, 28.9, 3.4) en Blockbench.
const { box, oct, octRingZ, T } = require('./lib/shapes');

const cubes = [];
const add = (...l) => l.forEach((x) => (Array.isArray(x) ? cubes.push(...x) : cubes.push(x)));
const C = 24; // altura del centro

// ---------------- COCKPIT: esfera de capas octogonales huecas ----------------
const R = 11.2;
const bounds = [-10, -7, -3.5, 0, 3.5, 7, 10];
const layers = [];
for (let i = 0; i < bounds.length - 1; i++) {
  const y0 = bounds[i], y1 = bounds[i + 1], ym = (y0 + y1) / 2;
  layers.push({ i, y0, y1, ym, a: Math.sqrt(R * R - ym * ym) * 0.97 });
}
layers.forEach((L) => {
  // grosor: la pared llega hacia dentro hasta cubrir el borde exterior de la capa vecina hacia el polo (o la tapa)
  const pole = L.ym < 0 ? layers[L.i - 1] : layers[L.i + 1];
  const aPole = pole ? pole.a : 4.6;
  const t = Math.min(L.a - 1, Math.max(1.5, L.a - aPole + 0.9));
  const len = 2 * L.a * Math.tan(22.5 * T) + 0.3;
  const h = L.y1 - L.y0;
  const window = L.y0 >= -7 && L.y1 <= 7; // capas que cruzan la ventana: sin los 3 segmentos delanteros
  let first = null;
  for (let k = 0; k < 8; k++) {
    if (window && (k === 3 || k === 4 || k === 5)) continue;
    const phi = k * 45 * T, r = L.a - t / 2;
    const name = `shell_${L.i}_${k}`;
    add(box('cockpit', name, [r * Math.sin(phi), C + L.ym, r * Math.cos(phi)], [len, h, t],
      L.i === 0 || L.i === 5 ? 'hull_light' : 'hull', { rotation: k === 0 ? null : [0, k * 45, 0], uvFrom: first || undefined }));
    if (!first) first = name;
  }
});
add(oct('y', 'cockpit', 'cap_top', [0, C + 10.5, 0], 4.6, 1, 'hull_light'));
add(oct('y', 'cockpit', 'cap_bottom', [0, C - 10.5, 0], 4.6, 1, 'hull_dark'));
// franjas de panel horizontales (ecuador) a los lados
for (const s of [1, -1]) add(box('cockpit', `equator_${s > 0 ? 'r' : 'l'}`, [s * 10.9, C, 0], [0.6, 0.8, 8], 'frame'));
add(box('cockpit', 'equator_back', [0, C, 10.9], [8, 0.8, 0.6], 'frame'));

// ---------------- WINDOW: tambor octogonal con 8 radios ----------------
add(octRingZ('window', 'bezel', [0, C, -8.05], 11, 4, 8.5, 'frame'));
add(octRingZ('window', 'lip', [0, C, -12.3], 7.6, 1, 1, 'hull_light'));
for (let k = 0; k < 8; k++) {
  const a = k * 45 * T;
  add(box('window', `spoke_${k}`, [-4.6 * Math.sin(a), C + 4.6 * Math.cos(a), -9.4], [0.9, 7.2, 0.9], 'frame',
    { rotation: k === 0 ? null : [0, 0, k * 45], uvFrom: k === 0 ? undefined : 'spoke_0' }));
}
add(oct('z', 'window', 'hub', [0, C, -9.4], 1.6, 1.4, 'hull_light'));

// ---------------- INTERIOR ----------------
add(oct('y', 'interior', 'floor', [0, C - 6.6, 0], 7.4, 0.8, 'seat'));
add(box('interior', 'floor_light_r', [3.9, C - 6.1, -3], [0.6, 0.3, 5], 'glow_green', { glow: true }));
add(box('interior', 'floor_light_l', [-3.9, C - 6.1, -3], [0.6, 0.3, 5], 'glow_green', { glow: true, uvFrom: 'floor_light_r' }));
// asiento
add(box('interior', 'seat_base', [0, C - 5.7, 3.6], [5, 1, 5], 'frame'));
add(box('interior', 'seat_cushion', [0, C - 4.8, 3.2], [6.5, 2.4, 6.5], 'seat'));
add(box('interior', 'seat_back', [0, C, 6.4], [6.5, 9.5, 1.6], 'seat', { rotation: [10, 0, 0] }));
add(box('interior', 'seat_head', [0, C + 5.8, 7.4], [4.4, 3.4, 1.4], 'seat'));
add(box('interior', 'seat_frame', [0, C, 7.4], [5, 8, 0.6], 'frame', { rotation: [10, 0, 0] }));
for (const s of [1, -1]) {
  const n = s > 0 ? 'r' : 'l', u = s < 0 ? (x) => ({ uvFrom: x }) : () => ({});
  add(box('interior', `armrest_${n}`, [s * 3.9, C - 2.7, 3.8], [1.4, 1.2, 5.5], 'frame', u('armrest_r')));
  // palancas
  add(box('interior', `lever_base_${n}`, [s * 4.8, C - 4.4, -1.5], [1.8, 1.6, 1.8], 'frame', u('lever_base_r')));
  add(box('interior', `lever_shaft_${n}`, [s * 4.8, C - 1.7, -2.4], [0.8, 5, 0.8], 'metal', { rotation: [-20, 0, 0], ...u('lever_shaft_r') }));
  add(box('interior', `lever_grip_${n}`, [s * 4.8, C + 0.6, -3.3], [1.6, 2.2, 1.6], 'seat', u('lever_grip_r')));
  add(box('interior', `lever_btn_${n}`, [s * 4.8, C + 1.8, -3.8], [0.6, 0.5, 0.5], 'glow_red', { glow: true }));
  // paneles laterales con indicadores
  add(box('interior', `side_panel_${n}`, [s * 7.0, C - 1.4, -1.5], [0.8, 5.5, 8.5], 'frame', u('side_panel_r')));
  add(box('interior', `side_screen_${n}`, [s * 6.5, C - 0.6, -2.5], [0.3, 2.2, 3.2], 'screen', { glow: true }));
  for (let k = 0; k < 3; k++) {
    add(box('interior', `side_led_${n}_${k}`, [s * 6.5, C - 3, -4.5 + 2.2 * k], [0.3, 0.7, 1], k === 1 ? 'glow_red' : 'glow_green', { glow: true }));
  }
}
// consola delantera inclinada con 3 pantallas
const th = 22 * T, pc = [0, C - 4.2, -6.8];
const onPlate = (dx, dz, lift) => [pc[0] + dx, pc[1] + dz * Math.sin(-th) + lift * Math.cos(th), pc[2] + dz * Math.cos(th) + lift * Math.sin(th)];
add(box('interior', 'console_body', [0, C - 5.6, -6.6], [13, 2.6, 5], 'frame'));
add(box('interior', 'console_top', pc, [12.4, 0.8, 5.6], 'panel', { rotation: [22, 0, 0] }));
add(box('interior', 'screen_c', onPlate(0, -0.6, 0.55), [4.4, 0.4, 3.2], 'screen', { glow: true, rotation: [22, 0, 0] }));
add(box('interior', 'screen_r', onPlate(4.1, -0.6, 0.55), [3.2, 0.4, 2.8], 'screen', { glow: true, rotation: [22, 0, 0] }));
add(box('interior', 'screen_l', onPlate(-4.1, -0.6, 0.55), [3.2, 0.4, 2.8], 'screen', { glow: true, rotation: [22, 0, 0], uvFrom: 'screen_r' }));
for (let k = 0; k < 5; k++) {
  add(box('interior', `btn_${k}`, onPlate(-4 + 2 * k, 2.1, 0.5), [0.9, 0.4, 0.7], k % 2 ? 'glow_red' : 'glow_green', { glow: true, rotation: [22, 0, 0] }));
}
add(box('interior', 'dash_hood', [0, C - 2.2, -8.6], [10, 0.8, 1.6], 'frame'));
// panel del techo
add(box('interior', 'overhead', [0, C + 8.4, -2.5], [6, 0.8, 4], 'frame'));
for (let k = 0; k < 3; k++) add(box('interior', `overhead_led_${k}`, [-2 + 2 * k, C + 7.9, -2.5], [0.8, 0.3, 0.8], k === 1 ? 'glow_red' : 'screen', { glow: true }));

// ---------------- PILOT (Legion, simplificado) ----------------
add(box('pilot', 'torso', [0, C - 1, 3.6], [5.6, 6.4, 3.4], 'suit'));
add(box('pilot', 'chest', [0, C + 0.2, 1.8], [4.4, 3.4, 0.6], 'hull_light'));
add(box('pilot', 'chest_light', [1.2, C + 0.6, 1.4], [0.8, 0.6, 0.3], 'glow_green', { glow: true }));
add(box('pilot', 'belt', [0, C - 3.2, 3.6], [5.8, 0.8, 3.6], 'trim'));
add(box('pilot', 'helmet', [0, C + 4.9, 3.4], [5.2, 5, 5.2], 'hull_light'));
add(box('pilot', 'visor', [0, C + 4.7, 0.7], [4.4, 1.6, 0.6], 'glow_green', { glow: true }));
add(box('pilot', 'crest', [0, C + 7.6, 3.4], [1, 0.8, 4.6], 'hull'));
for (const s of [1, -1]) {
  const n = s > 0 ? 'r' : 'l', u = s < 0 ? (x) => ({ uvFrom: x }) : () => ({});
  add(box('pilot', `cheek_${n}`, [s * 2.8, C + 4, 2.4], [0.6, 2.6, 2.2], 'hull', u('cheek_r')));
  // brazo: hombro (y 25.4, z 3.2) -> codo (22.6, 1.4) -> mano en la palanca (24.3, -2.8)
  const limb = (nm, p0, p1, tk, mat, uv) => {
    const dy = p1[1] - p0[1], dz = p1[2] - p0[2], len = Math.hypot(dy, dz);
    const ang = Math.atan2(-dy, dz) / T; // gira el eje Z local hacia (dy,dz)
    add(box('pilot', nm, [s * 4.2, (p0[1] + p1[1]) / 2, (p0[2] + p1[2]) / 2], [tk, tk, len], mat, { rotation: [ang, 0, 0], ...u(uv) }));
  };
  limb(`upper_arm_${n}`, [0, C + 1.4, 3.2], [0, C - 1.4, 1.2], 1.8, 'suit', 'upper_arm_r');
  limb(`forearm_${n}`, [0, C - 1.4, 1.2], [0, C + 0.3, -2.8], 1.7, 'suit', 'forearm_r');
  add(box('pilot', `hand_${n}`, [s * 4.6, C + 0.4, -3.1], [1.8, 1.8, 1.8], 'hull', u('hand_r')));
  add(box('pilot', `thigh_${n}`, [s * 1.7, C - 2.9, 1.7], [2.2, 2.2, 5], 'suit', u('thigh_r')));
  add(box('pilot', `shin_${n}`, [s * 1.7, C - 4.7, -0.9], [2.2, 3.6, 2.2], 'suit', u('shin_r')));
  add(box('pilot', `boot_${n}`, [s * 1.7, C - 6.1, -1.3], [2.4, 1, 3.2], 'hull', u('boot_r')));
}

// ---------------- ENGINE: toberas traseras ----------------
add(box('engine', 'engine_block', [0, C, 11.6], [8, 5, 2.4], 'frame'));
add(box('engine', 'fin_top', [0, C + 4.4, 12.4], [0.8, 3.6, 3], 'hull'));
add(box('engine', 'fin_bottom', [0, C - 4.4, 12.4], [0.8, 3.6, 3], 'hull', { uvFrom: 'fin_top' }));
for (const s of [1, -1]) {
  const n = s > 0 ? 'r' : 'l', x = s * 5.2;
  add(oct('z', 'engine', `throat_${n}`, [x, C, 12.4], 2.6, 3, 'hull_light'));
  add(oct('z', 'engine', `bell_${n}`, [x, C, 14.6], 3.6, 2.2, 'hull_dark'));
  add(octRingZ('engine', `rim_${n}`, [x, C, 16], 4.2, 1.2, 1, 'hull_light'));
  add(oct('z', 'engine', `flame_${n}`, [x, C, 15.95], 3.2, 0.5, 'glow_green', { glow: true }));
}

// ---------------- PYLONS ----------------
for (const s of [1, -1]) {
  const n = s > 0 ? 'r' : 'l', bn = `pylon_${n}`;
  add(oct('x', bn, `collar_${n}`, [s * 11.4, C, 0], 4.4, 1.6, 'hull_light'));
  add(oct('x', bn, `strut_${n}`, [s * 18, C, 0], 3, 12.4, 'hull'));
  for (const xx of [15, 20.5]) add(oct('x', bn, `strut_ring_${n}_${xx}`, [s * xx, C, 0], 3.6, 1, 'frame'));
  add(oct('x', bn, `flange_${n}`, [s * 25.4, C, 0], 6, 2.6, 'hull_light'));
  add(box(bn, `arm_${n}`, [s * 31, C, 0], [8.6, 5, 5], 'hull', s < 0 ? { uvFrom: 'arm_r' } : {}));
  add(box(bn, `conduit_${n}`, [s * 18, C + 3.05, 0], [10, 0.5, 1], 'glow_green', s < 0 ? { glow: true, uvFrom: 'conduit_r' } : { glow: true }));
}

// ---------------- WINGS: hexagono vertical (punta arriba/abajo), R=23 ----------------
const RW = 23, AW = RW * Math.cos(30 * T), FW = 2.6; // radio, apotema, ancho del marco
for (const s of [1, -1]) {
  const n = s > 0 ? 'r' : 'l', bn = `wing_${n}`, x = s * 37.5;
  const u = (nm) => (s < 0 ? { uvFrom: nm.replace(/_l(_|$)/, '_r$1') } : {});
  // marco: 6 barras (eje largo local en Y => textura barata)
  for (let k = 0; k < 6; k++) {
    const al = k * 60; // normal exterior, desde +Z hacia +Y
    const A = AW - FW / 2;
    const nm = `frame_${n}_${k}`;
    add(box(bn, nm, [x, C + A * Math.sin(al * T), A * Math.cos(al * T)], [3.6, RW + 0.58 * FW, FW], 'frame',
      { rotation: al % 180 === 0 ? null : [-al, 0, 0], ...(s < 0 ? { uvFrom: `frame_r_${k}` } : {}) }));
  }
  // panel solar: tiras verticales que forman el hexagono interior
  const AI = AW - FW + 0.4, RI = AI / Math.cos(30 * T), NS = 9, SW = (2 * AI) / NS;
  for (let k = 0; k < NS; k++) {
    const zc = -AI + SW * (k + 0.5);
    const hh = Math.min(RI, RI - Math.tan(30 * T) * (Math.abs(zc) - SW / 2)) + 0.2;
    add(box(bn, `cell_${n}_${k}`, [x, C, zc], [2.8, 2 * Math.min(hh, RI), SW + 0.02], 'panel', u(`cell_${n}_${k}`)));
  }
  // nervios radiales (vertice a vertice) y barras horizontales
  [[0, 'v'], [60, 'a'], [-60, 'b']].forEach(([rx, t]) => {
    add(box(bn, `rib_${n}_${t}`, [x, C, 0], [3.4, 2 * RI - 1, 1.1], 'frame', { rotation: rx ? [rx, 0, 0] : null, ...u(`rib_${n}_${t}`) }));
  });
  for (const dy of [-12, -6, 6, 12]) {
    const half = Math.min(AI, (RI - Math.abs(dy)) / Math.tan(30 * T)) - 0.6;
    const nm = `bar_${n}_${dy < 0 ? 'm' + -dy : dy}`;
    add(box(bn, nm, [x, C + dy, 0], [3.2, 2 * half, 0.7], 'frame', { rotation: [90, 0, 0], ...u(nm) }));
  }
  // cubo central (union con el pilon) y luces en las puntas
  add(oct('x', bn, `hub_${n}`, [x, C, 0], 4.6, 5.6, 'hull_light'));
  add(oct('x', bn, `hub_cap_${n}`, [x + s * 3, C, 0], 2.6, 0.8, 'frame'));
  add(box(bn, `tip_top_${n}`, [x, C + RW - 1.2, 0], [4, 1.4, 1.4], 'glow_green', { glow: true, ...u(`tip_top_${n}`) }));
  add(box(bn, `tip_bottom_${n}`, [x, C - RW + 1.2, 0], [4, 1.4, 1.4], 'glow_green', { glow: true, uvFrom: `tip_top_r` }));
}

module.exports = {
  name: 'fighter',
  texture: [256, 256],
  palette: {
    hull: '#2A2B31', hull_light: '#4A4C55', hull_dark: '#1A1B20', panel: { color: '#121318', noise: 3, edge: true },
    frame: '#3A3C44', seat: '#1C1C22', metal: '#8A8F98', suit: '#26272D', trim: '#2E7D46',
    glow_green: { color: '#4CFF7A', noise: 0, edge: false },
    glow_red: { color: '#FF3B3B', noise: 0, edge: false },
    screen: { color: '#7CFFB0', noise: 10, edge: true },
  },
  bones: [
    { name: 'root', parent: null, pivot: [0, 0, 0] },
    { name: 'cockpit', parent: 'root', pivot: [0, C, 0] },
    { name: 'window', parent: 'cockpit', pivot: [0, C, -10.1] },
    { name: 'interior', parent: 'cockpit', pivot: [0, C, 0] },
    { name: 'pilot', parent: 'cockpit', pivot: [0, C, 0] },
    { name: 'engine', parent: 'root', pivot: [0, C, 11] },
    { name: 'pylon_r', parent: 'root', pivot: [11, C, 0] },
    { name: 'pylon_l', parent: 'root', pivot: [-11, C, 0] },
    { name: 'wing_r', parent: 'root', pivot: [37.5, C, 0] },
    { name: 'wing_l', parent: 'root', pivot: [-37.5, C, 0] },
  ],
  cubes,
  animations: [],
};
