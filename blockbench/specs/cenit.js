// Cenit, la Segunda Hora (mediodia). Grande y pesado, martillo "Mediodia" y disco solar a la espalda.
const { build, ring } = require('./lib/hour');
const { box } = require('./lib/shapes');
module.exports = build({
  name: 'cenit', style: 'heavy', weapon: 'hammer',
  h: { leg: 15, torso: 14, head: 8 }, w: { torso: 10, arm: 4, leg: 4.5 }, cape: false,
  palette: {
    armor: { color: '#ECE8DC', noise: 5 }, armor_light: { color: '#D9A931', noise: 5 }, trim: { color: '#D9A931', noise: 4 },
    under: { color: '#7A6A4E', noise: 5 }, skin: { color: '#C98E62', noise: 2, edge: false }, beard: { color: '#8A5A2B', noise: 5 },
    cape: { color: '#D9A931', noise: 4 }, glow: { color: '#FFF2A8', noise: 2, edge: false }, eye: { color: '#2A1A0A', noise: 0, edge: false },
    wood: { color: '#5A3E26', noise: 6 }, steel: { color: '#B9B6AE', noise: 6 },
  },
  extraBones: [{ name: 'sun_disk', parent: 'body', pivot: [0, 24, 4.5] }],
  parts(add, d) {
    const n = d.neck;
    add(box('head', 'helm', [0, n + 6.4, 0], [8.8, 3.4, 8.8], 'armor'));
    add(box('head', 'crest', [0, n + 9, 0], [1.2, 3, 8], 'armor_light'));
    add(box('head', 'beard', [0, n + 1.3, -3.8], [6.6, 3, 1.4], 'beard'));
    add(box('head', 'eye_r', [1.8, n + 4.3, -4.05], [1.4, 1, 0.2], 'eye'));
    add(box('head', 'eye_l', [-1.8, n + 4.3, -4.05], [1.4, 1, 0.2], 'eye'));
    add(box('body', 'belly_plate', [0, d.hip + 4, -2.6], [8, 5, 1], 'armor_light'));
    add(box('body', 'buckle', [0, d.hip + 1, -2.7], [3, 2.4, 0.6], 'glow', { glow: true }));
    add(box('right_arm', 'big_pauldron_r', [d.ax + 1, d.sh + 1.4, 0], [d.aw + 3.4, 2.6, d.aw + 3.4], 'armor'));
    add(box('left_arm', 'big_pauldron_l', [-d.ax - 1, d.sh + 1.4, 0], [d.aw + 3.4, 2.6, d.aw + 3.4], 'armor', { mirror: true }));
    // disco solar a la espalda
    add(box('sun_disk', 'disk', [0, 24, 4.5], [10, 10, 0.8], 'armor_light', { rotation: [0, 0, 45] }));
    add(box('sun_disk', 'disk2', [0, 24, 4.6], [10, 10, 0.8], 'armor_light'));
    add(box('sun_disk', 'disk_core', [0, 24, 4.0], [4, 4, 0.6], 'glow', { glow: true, rotation: [0, 0, 45] }));
    add(ring('sun_disk', 'disk_ring', [0, 24, 5.1], 7.5, 12, 0.8, 'glow', true));
    // martillo: mango hacia -Z desde la mano, cabeza en la punta
    const y = d.hip - 1;
    add(box('weapon', 'handle', [d.ax, y, -10], [1.6, 1.6, 22], 'wood'));
    add(box('weapon', 'head_h', [d.ax, y, -21], [6, 7, 9], 'steel'));
    add(box('weapon', 'head_band', [d.ax, y, -21], [6.4, 7.4, 2], 'armor_light'));
    add(box('weapon', 'head_sun_r', [d.ax + 3.3, y, -21], [0.4, 4, 4], 'glow', { glow: true, rotation: [45, 0, 0] }));
    add(box('weapon', 'head_sun_l', [d.ax - 3.3, y, -21], [0.4, 4, 4], 'glow', { glow: true, rotation: [45, 0, 0] }));
    add(box('weapon', 'pommel', [d.ax, y, 1.6], [2.4, 2.4, 1.6], 'armor_light'));
    add(ring('halo', 'halo', [0, n + 5, 6], 5.5, 12, 0.8, 'glow', true));
  },
  specials(list, h) {
    const { R0, L0, base, kf } = h;
    list.push({ name: 'special_1', loop: false, length: 1.3, bones: base({
      root: { position: kf([[0, [0, 0, 0]], [0.45, [0, 14, 0]], [0.75, [0, 0, 0]], [1.3, [0, 0, 0]]]) },
      right_arm: { rotation: kf([[0, R0], [0.45, [175, 0, 10]], [0.75, [20, 0, 0]], [1.3, R0]]) },
      left_arm: { rotation: kf([[0, L0], [0.45, [175, 0, -10]], [0.75, [30, 0, -5]], [1.3, L0]]) },
      body: { rotation: kf([[0, [0, 0, 0]], [0.45, [15, 0, 0]], [0.75, [-35, 0, 0]], [1.3, [0, 0, 0]]]) },
      right_leg: { rotation: kf([[0, [0, 0, 0]], [0.45, [30, 0, 0]], [0.75, [-20, 0, 0]], [1.3, [0, 0, 0]]]) },
      left_leg: { rotation: kf([[0, [0, 0, 0]], [0.45, [30, 0, 0]], [0.75, [20, 0, 0]], [1.3, [0, 0, 0]]]) },
    }) });
    list.push({ name: 'special_2', loop: false, length: 1.0, bones: base({
      left_arm: { rotation: kf([[0, L0], [0.35, [-40, 0, -20]], [0.55, [110, 0, 0]], [1, L0]]) },
      body: { rotation: kf([[0, [0, 0, 0]], [0.35, [0, -30, 0]], [0.55, [0, 25, 0]], [1, [0, 0, 0]]]) },
      sun_disk: { rotation: kf([[0, [0, 0, 0]], [1, [0, 0, 360]]], 'linear') },
    }) });
  },
});
