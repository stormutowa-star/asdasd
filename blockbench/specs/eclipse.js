// Eclipse, la Quinta Hora y lider. Armadura negra, capa blanca por dentro, yelmo cerrado, corona-halo con disco negro, lanza.
const { build, ring } = require('./lib/hour');
const { box } = require('./lib/shapes');
module.exports = build({
  name: 'eclipse', style: 'lance', weapon: 'spear',
  h: { leg: 14, torso: 13, head: 8 }, w: { torso: 8, arm: 3, leg: 3.8 }, capeLen: 20,
  palette: {
    armor: { color: '#101014', noise: 4 }, armor_light: { color: '#2A2A30', noise: 4 }, trim: { color: '#A9AAB8', noise: 5 },
    under: { color: '#1A1A20', noise: 4 }, skin: { color: '#101014', noise: 3 }, visor: { color: '#F4F4FF', noise: 0, edge: false },
    cape: { color: '#E8E8FF', noise: 3, edge: false }, cape_out: { color: '#121216', noise: 3, edge: false },
    glow: { color: '#F4F4FF', noise: 1, edge: false }, disk: { color: '#050507', noise: 0, edge: false }, steel: { color: '#A9AAB8', noise: 6 },
  },
  parts(add, d) {
    const n = d.neck;
    add(box('head', 'helm', [0, n + 4.2, 0], [8.6, 8.6, 8.6], 'armor_light'));
    add(box('head', 'visor', [0, n + 4.4, -4.4], [6, 0.8, 0.3], 'visor', { glow: true }));
    add(box('head', 'helm_ridge', [0, n + 8.8, 0], [1, 1.2, 8.8], 'trim'));
    add(box('cape', 'cape_out', [0, n - 10, 3.5], [d.tw + 1.2, 20, 0.3], 'cape_out'));
    add(box('body', 'chest_line', [0, d.hip + 9, -2.7], [0.8, 6, 0.4], 'glow', { glow: true }));
    add(box('body', 'tasset_r', [2.4, d.hip - 2, -1], [3, 5, 5], 'armor_light', { rotation: [0, 0, 8] }));
    add(box('body', 'tasset_l', [-2.4, d.hip - 2, -1], [3, 5, 5], 'armor_light', { rotation: [0, 0, -8] }));
    // lanza hacia -Z desde la mano
    const x = d.ax, y = d.hip - 1;
    add(box('weapon', 'shaft', [x, y, -8], [1.2, 1.2, 36], 'armor'));
    add(box('weapon', 'tip', [x, y, -28.5], [1.6, 3.2, 5], 'glow', { glow: true }));
    add(box('weapon', 'tip_guard', [x, y, -25.6], [3.6, 1, 1], 'steel'));
    add(box('weapon', 'butt', [x, y, 10.4], [1.8, 1.8, 1.4], 'steel'));
    // corona: aro blanco grande con disco negro
    add(ring('halo', 'halo', [0, n + 5, 6.5], 7.5, 16, 1.2, 'glow', true));
    add(box('halo', 'disk', [0, n + 5, 6.8], [10, 10, 0.4], 'disk'));
    add(box('halo', 'disk2', [0, n + 5, 6.8], [10, 10, 0.4], 'disk', { rotation: [0, 0, 45] }));
  },
  specials(list, h) {
    const { R0, L0, base, kf } = h;
    list.push({ name: 'special_1', loop: false, length: 1.2, bones: base({
      right_arm: { rotation: kf([[0, R0], [0.35, [150, 0, 10]], [0.55, [60, 0, 0]], [1.2, [60, 0, 0]]]) },
      left_arm: { rotation: kf([[0, L0], [0.55, [90, -20, -30]], [1.2, [90, -20, -30]]]) },
      body: { rotation: kf([[0, [0, 0, 0]], [0.35, [10, 0, 0]], [0.55, [-15, 0, 0]], [1.2, [-10, 0, 0]]]) },
      halo: { rotation: kf([[0, [0, 0, 0]], [1.2, [0, 0, 720]]], 'linear'), scale: kf([[0, [1, 1, 1]], [0.55, [1.4, 1.4, 1]], [1.2, [1, 1, 1]]]) },
    }) });
    list.push({ name: 'special_2', loop: false, length: 0.9, bones: base({
      body: { rotation: [[0, [0, 0, 0], 'catmullrom'], [0.6, [0, 360, 0], 'linear'], [0.9, [0, 360, 0], 'catmullrom']] },
      right_arm: { rotation: kf([[0, R0], [0.2, [90, 0, 80]], [0.6, [90, 0, 80]], [0.9, R0]]) },
    }) });
  },
});
