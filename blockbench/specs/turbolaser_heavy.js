// Torreta pesada del destructor (doble canon largo). Frente = -Z. Huesos: base > yaw > pitch > barrel_0/1.
// yaw: pivote en el centro de la base (0,9,0); pitch: eje de la cuna (0,23,-3); barrel_n: pivote en su recamara.
const { box, oct, octPlate, octRing } = require('./lib/shapes');

const cubes = [];
const add = (...l) => l.forEach((x) => (Array.isArray(x) ? cubes.push(...x) : cubes.push(x)));

// ---------------- BASE ----------------
add(octRing('base', 'plate', [0, 2.5, 0], 24, 10, 5, 'plate'));
add(octRing('base', 'ring', [0, 7, 0], 19, 5, 4, 'hull'));
add(octRing('base', 'glowline', [0, 5.4, 0], 21.5, 1.2, 0.8, 'ring', { glow: true }));
for (let k = 0; k < 8; k++) {
  const a = (22.5 + 45 * k) * Math.PI / 180;
  add(box('base', `bolt_${k}`, [22 * Math.cos(a), 5.8, 22 * Math.sin(a)], [2.4, 1.6, 2.4], 'metal'));
}
add(box('base', 'light_e', [24.3, 2.5, 0], [1, 2, 3], 'glow_red', { glow: true }));
add(box('base', 'light_w', [-24.3, 2.5, 0], [1, 2, 3], 'glow_red', { glow: true }));
add(box('base', 'light_s', [0, 2.5, 24.3], [3, 2, 1], 'glow_red', { glow: true }));
add(box('base', 'light_n', [0, 2.5, -24.3], [3, 2, 1], 'glow_red', { glow: true }));

add(box('base', 'deck', [0, 7.2, 0], [29, 3.6, 29], 'hull_dark'));

// ---------------- YAW (casa blindada) ----------------
add(box('yaw', 'house', [0, 15.5, 5], [26, 13, 24], 'hull'));
add(box('yaw', 'roof', [0, 23.4, 6], [24, 2.4, 22], 'plate', { rotation: [-6, 0, 0] }));
add(box('yaw', 'hatch', [0, 25.2, 9.5], [10, 1.6, 8], 'hull_dark'));
for (let k = 0; k < 3; k++) add(box('yaw', `hatch_slat_${k}`, [0, 26.2, 7.5 + 2 * k], [7, 0.5, 0.8], 'metal'));
add(box('yaw', 'glacis', [0, 14, -10.5], [22, 2, 12.2], 'plate', { rotation: [-55, 0, 0] }));
add(box('yaw', 'glacis_trim', [0, 10.3, -13.8], [22.4, 1.4, 1.4], 'hull_dark'));
for (const s of [1, -1]) {
  const n = s > 0 ? 'r' : 'l';
  add(box('yaw', `cheek_${n}`, [s * 10.8, 19, -3], [4.4, 20, 14], 'hull'));
  add(box('yaw', `cheek_cap_${n}`, [s * 10.8, 29.3, -3], [4.8, 0.8, 14.4], 'plate'));
  add(box('yaw', `cheek_plate_${n}`, [s * 13.2, 22, -3], [0.8, 12, 11], 'plate'));
  add(box('yaw', `side_panel_${n}`, [s * 13.6, 16, 7], [1.2, 10, 18], 'plate'));
  add(box('yaw', `vent_${n}`, [s * 14.3, 16, 9], [0.4, 8, 10], 'hull_dark'));
  for (let k = 0; k < 6; k++) add(box('yaw', `slat_${n}_${k}`, [s * 14.6, 12.9 + 1.25 * k, 9], [0.8, 0.7, 10], 'metal'));
  add(box('yaw', `stripe_${n}`, [s * 14.3, 11.2, 7], [0.6, 1.2, 17], 'trim_green'));
  add(box('yaw', `corner_${n}`, [s * 12.4, 14, 16.4], [2.4, 10, 1.6], 'hull_dark'));
  // luz de posicion y pernos
  add(box('yaw', `plight_${n}`, [s * 13, 22.6, 16.6], [1.6, 1.6, 1.6], 'glow_red', { glow: true }));
  add(box('yaw', `clight_${n}`, [s * 10.8, 30.1, -9.4], [1.4, 1.4, 1.4], 'glow_red', { glow: true }));
  // eje de elevacion visible (bosses y pernos)
  add(oct('x', 'yaw', `boss_${n}`, [s * 13.9, 23, -3], 3.4, 1.4, 'metal'));
  add(oct('x', 'yaw', `boss_dark_${n}`, [s * 13.6, 23, -3], 4.8, 0.8, 'hull_dark'));
  for (const [dy, dz, i] of [[5.6, 5.6, 0], [5.6, -5.6, 1], [-5.6, 5.6, 2], [-5.6, -5.6, 3]]) {
    add(box('yaw', `cbolt_${n}_${i}`, [s * 13.9, 23 + dy, -3 + dz], [1.2, 1.4, 1.4], 'metal'));
  }
}
// escalerilla trasera
add(box('yaw', 'rear_plate', [0, 14, 17.4], [10, 8, 0.8], 'plate'));
for (const s of [1, -1]) add(box('yaw', `rail_${s > 0 ? 'r' : 'l'}`, [s * 3.2, 16, 18.4], [1, 14, 1], 'metal'));
for (let k = 0; k < 6; k++) add(box('yaw', `rung_${k}`, [0, 10 + 2.4 * k, 18.4], [6.4, 0.8, 0.8], 'metal'));
// antenas y sensores
add(box('yaw', 'mast_a', [9, 29.5, 12], [0.9, 12, 0.9], 'metal'));
add(box('yaw', 'mast_b', [-10, 27.5, 13], [0.9, 8, 0.9], 'metal'));
add(box('yaw', 'mast_a_light', [9, 35.6, 12], [1.5, 1.5, 1.5], 'glow_red', { glow: true }));
add(box('yaw', 'mast_b_light', [-10, 31.6, 13], [1.5, 1.5, 1.5], 'glow_red', { glow: true }));
add(box('yaw', 'mast_a_cross', [9, 32, 12], [4.5, 0.6, 0.6], 'metal'));
add(box('yaw', 'sensor', [-6, 25.6, 10], [4, 2, 4], 'hull_dark'));
add(box('yaw', 'sensor_lens', [-6, 25.6, 7.9], [1.6, 1.2, 0.4], 'glow_green', { glow: true }));
add(box('yaw', 'dish', [3.5, 25.4, 13], [5, 0.8, 5], 'metal', { rotation: [20, 0, 0] }));
add(box('yaw', 'dish_stem', [3.5, 24.6, 13], [0.8, 2, 0.8], 'metal'));

// ---------------- PITCH (cuna) ----------------
add(box('pitch', 'cradle', [0, 23, -2.5], [16, 9, 13], 'hull'));
add(box('pitch', 'cradle_top', [0, 27.8, -2.5], [14, 1.2, 12], 'plate'));
add(box('pitch', 'cradle_bottom', [0, 18.2, -3], [12, 1, 10], 'hull_dark'));
add(box('pitch', 'chin', [0, 20.5, -9.6], [15, 3.4, 1.4], 'plate'));
for (let k = 0; k < 3; k++) add(box('pitch', `coil_${k}`, [0, 28.6, -5.5 + 3 * k], [8, 0.5, 1.2], 'ring', { glow: true }));
add(box('pitch', 'cradle_stripe_r', [8.1, 23, -2.5], [0.4, 1.2, 11], 'trim_green'));
add(box('pitch', 'cradle_stripe_l', [-8.1, 23, -2.5], [0.4, 1.2, 11], 'trim_green'));
for (const s of [1, -1]) {
  const n = s > 0 ? 'r' : 'l';
  add(box('pitch', `sleeve_${n}`, [s * 5.2, 23, -11], [7.6, 7.6, 4], 'hull_dark'));
  add(box('pitch', `sleeve_flare_${n}`, [s * 5.2, 23, -13.3], [8.6, 8.6, 0.8], 'plate'));
}
add(box('pitch', 'center_light', [0, 21, -10.5], [2, 1.2, 0.6], 'glow_green', { glow: true }));

// ---------------- CANONES ----------------
[['barrel_0', 1], ['barrel_1', -1]].forEach(([bn, s]) => {
  const x = s * 5.2, t = bn;
  add(box(bn, `${t}_core`, [x, 23, -25], [4.6, 4.6, 30], 'plate'));
  add(oct('z', bn, `${t}_breech`, [x, 23, -9.5], 3.2, 3, 'hull_dark'));
  for (const zc of [-17, -24, -31]) {
    add(oct('z', bn, `${t}_ring_${-zc}`, [x, 23, zc], 3.9, 2.4, 'ring', { glow: true }));
    add(box(bn, `${t}_clamp_${-zc}`, [x, 23, zc - 1.7], [8.4, 8.4, 0.8], 'hull_dark'));
  }
  add(oct('z', bn, `${t}_m1`, [x, 23, -40.9], 3.5, 1.8, 'plate'));
  add(oct('z', bn, `${t}_m2`, [x, 23, -42.7], 4.4, 1.8, 'hull_dark'));
  add(box(bn, `${t}_core_glow`, [x, 23, -43.6], [3.4, 3.4, 0.4], 'glow_green', { glow: true }));
});

module.exports = {
  name: 'turbolaser_heavy',
  texture: [256, 256],
  palette: {
    hull: '#2C2D33', hull_dark: '#1A1B20', plate: '#44464F', trim_green: '#2E7D46', metal: '#8A8F98',
    glow_green: { color: '#4CFF7A', noise: 0, edge: false },
    glow_red: { color: '#FF3B3B', noise: 0, edge: false },
    ring: { color: '#2FA35A', noise: 2, edge: false },
  },
  bones: [
    { name: 'root', parent: null, pivot: [0, 0, 0] },
    { name: 'base', parent: 'root', pivot: [0, 0, 0] },
    { name: 'yaw', parent: 'base', pivot: [0, 9, 0] },
    { name: 'pitch', parent: 'yaw', pivot: [0, 23, -3] },
    { name: 'barrel_0', parent: 'pitch', pivot: [5.2, 23, -9] },
    { name: 'barrel_1', parent: 'pitch', pivot: [-5.2, 23, -9] },
  ],
  cubes,
  animations: [],
};
