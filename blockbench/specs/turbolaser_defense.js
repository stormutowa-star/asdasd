// Torreta de defensa del destructor (cuadruple canon corto). Frente = -Z. Huesos: base > yaw > pitch > barrel_0..3.
// yaw: (0,5,0) centro de la base; pitch: (0,12,-2) eje de la cuna; barrel_n: pivote en su recamara.
// barrel_0 (+x,arriba), barrel_1 (-x,arriba), barrel_2 (+x,abajo), barrel_3 (-x,abajo).
const { box, oct, octPlate, octRing } = require('./lib/shapes');

const cubes = [];
const add = (...l) => l.forEach((x) => (Array.isArray(x) ? cubes.push(...x) : cubes.push(x)));

// ---------------- BASE ----------------
add(octRing('base', 'plate', [0, 1.5, 0], 14, 6, 3, 'plate'));
add(octRing('base', 'ring', [0, 4, 0], 11, 4, 2, 'hull'));
add(octRing('base', 'glowline', [0, 3.2, 0], 12.5, 1, 0.6, 'ring', { glow: true }));
for (let k = 0; k < 4; k++) {
  const a = (22.5 + 90 * k) * Math.PI / 180;
  add(box('base', `bolt_${k}`, [12.8 * Math.cos(a), 3.4, 12.8 * Math.sin(a)], [1.6, 1, 1.6], 'metal'));
}
add(box('base', 'light_e', [14.3, 1.5, 0], [0.8, 1.4, 2], 'glow_red', { glow: true }));
add(box('base', 'light_w', [-14.3, 1.5, 0], [0.8, 1.4, 2], 'glow_red', { glow: true }));
add(box('base', 'light_s', [0, 1.5, 14.3], [2, 1.4, 0.8], 'glow_red', { glow: true }));

add(box('base', 'deck', [0, 4.2, 0], [15, 2.6, 15], 'hull_dark'));

// ---------------- YAW ----------------
add(box('yaw', 'house', [0, 8.5, 3.5], [14, 7, 11], 'hull'));
add(box('yaw', 'roof', [0, 12.6, 4], [13, 1.4, 10], 'plate', { rotation: [-6, 0, 0] }));
add(box('yaw', 'hatch', [0, 13.7, 6], [5, 1, 4], 'hull_dark'));
for (const s of [1, -1]) {
  const n = s > 0 ? 'r' : 'l';
  add(box('yaw', `cheek_${n}`, [s * 6.4, 10.5, -2], [2.2, 11, 8], 'hull'));
  add(box('yaw', `cheek_cap_${n}`, [s * 6.4, 16.2, -2], [2.6, 0.6, 8.4], 'plate'));
  add(box('yaw', `stripe_${n}`, [s * 7.7, 6, 4.5], [0.4, 0.7, 8], 'trim_green'));
  add(box('yaw', `side_panel_${n}`, [s * 7.4, 8.5, 5], [0.8, 5, 8], 'plate'));
  add(box('yaw', `vent_${n}`, [s * 7.9, 8.5, 5.5], [0.3, 4, 5], 'hull_dark'));
  for (let k = 0; k < 3; k++) add(box('yaw', `slat_${n}_${k}`, [s * 8.1, 7.2 + 1.3 * k, 5.5], [0.5, 0.5, 5], 'metal'));
  add(oct('x', 'yaw', `boss_${n}`, [s * 7.8, 12, -2], 1.9, 0.8, 'metal'));
  add(box('yaw', `plight_${n}`, [s * 6.8, 12.2, 8.6], [1, 1, 1], 'glow_red', { glow: true }));
  add(box('yaw', `clight_${n}`, [s * 6.4, 16.8, -5.4], [1, 1, 1], 'glow_red', { glow: true }));
}
for (const s of [1, -1]) add(box('yaw', `rail_${s > 0 ? 'r' : 'l'}`, [s * 1.8, 9, 9.9], [0.6, 8, 0.6], 'metal'));
for (let k = 0; k < 3; k++) add(box('yaw', `rung_${k}`, [0, 6.2 + 2.2 * k, 9.9], [3.6, 0.5, 0.5], 'metal'));
add(box('yaw', 'sensor', [-2.5, 14.2, 4], [3, 1.4, 3], 'hull_dark'));
add(box('yaw', 'sensor_lens', [-2.5, 14.2, 2.4], [1.2, 0.8, 0.3], 'glow_green', { glow: true }));
add(box('yaw', 'mast_b', [-4, 15.5, 8], [0.6, 5, 0.6], 'metal'));
add(box('yaw', 'mast_b_light', [-4, 18.3, 8], [0.9, 0.9, 0.9], 'glow_red', { glow: true }));
add(box('yaw', 'mast_a', [4.5, 17.5, 7], [0.6, 8, 0.6], 'metal'));
add(box('yaw', 'mast_a_light', [4.5, 21.8, 7], [1, 1, 1], 'glow_red', { glow: true }));

// ---------------- PITCH ----------------
add(box('pitch', 'cradle', [0, 12, -1], [9, 8, 8], 'hull'));
add(box('pitch', 'cradle_top', [0, 16.2, -1], [8, 0.6, 7], 'plate'));
add(box('pitch', 'cradle_bottom', [0, 8, -1], [8, 0.6, 7], 'hull_dark'));
for (let k = 0; k < 2; k++) add(box('pitch', `coil_${k}`, [0, 16.6, -2.5 + 2.4 * k], [4, 0.4, 0.8], 'ring', { glow: true }));
add(box('pitch', 'chin', [0, 12, -5.4], [9.4, 3, 0.8], 'plate'));

// ---------------- CANONES ----------------
[[0, 1, 1], [1, -1, 1], [2, 1, -1], [3, -1, -1]].forEach(([i, sx, sy]) => {
  const bn = `barrel_${i}`, x = sx * 3.2, y = 12 + sy * 2.6;
  add(box('pitch', `${bn}_sleeve`, [x, y, -6], [4.4, 4.4, 2], 'hull_dark'));
  add(box(bn, `${bn}_core`, [x, y, -12.5], [3, 3, 15], 'plate'));
  for (const zc of [-9, -14.5]) {
    add(box(bn, `${bn}_ring_${-zc}`, [x, y, zc], [3.7, 3.7, 1.0], 'ring', { glow: true }));
  }
  add(box(bn, `${bn}_m1`, [x, y, -20.6], [3.8, 3.8, 1.2], 'plate'));
  add(box(bn, `${bn}_m2`, [x, y, -21.8], [4.8, 4.8, 1.2], 'hull_dark'));
  add(box(bn, `${bn}_tip`, [x, y, -22.5], [2.2, 2.2, 0.4], 'glow_green', { glow: true }));
});

module.exports = {
  name: 'turbolaser_defense',
  texture: [128, 128],
  palette: {
    hull: '#2C2D33', hull_dark: '#1A1B20', plate: '#44464F', trim_green: '#2E7D46', metal: '#8A8F98',
    glow_green: { color: '#4CFF7A', noise: 0, edge: false },
    glow_red: { color: '#FF3B3B', noise: 0, edge: false },
    ring: { color: '#2FA35A', noise: 2, edge: false },
  },
  bones: [
    { name: 'root', parent: null, pivot: [0, 0, 0] },
    { name: 'base', parent: 'root', pivot: [0, 0, 0] },
    { name: 'yaw', parent: 'base', pivot: [0, 5, 0] },
    { name: 'pitch', parent: 'yaw', pivot: [0, 12, -2] },
    { name: 'barrel_0', parent: 'pitch', pivot: [3.2, 14.6, -5] },
    { name: 'barrel_1', parent: 'pitch', pivot: [-3.2, 14.6, -5] },
    { name: 'barrel_2', parent: 'pitch', pivot: [3.2, 9.4, -5] },
    { name: 'barrel_3', parent: 'pitch', pivot: [-3.2, 9.4, -5] },
  ],
  cubes,
  animations: [],
};
