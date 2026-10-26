// Alba, la Primera Hora (amanecer). Agil, dos espadas cortas de luz, diadema solar y halo de rayos.
const { build, ring, C } = require('./lib/hour');
const { box } = require('./lib/shapes');
module.exports = build({
  name: 'alba', style: 'fast', weapon: 'swords',
  h: { leg: 13, torso: 12, head: 8 }, w: { torso: 7, arm: 3, leg: 3.5 }, capeLen: 12,
  palette: {
    armor: { color: '#E8B04A', noise: 5 }, armor_light: { color: '#F2C66A', noise: 4 }, trim: { color: '#E98A7A', noise: 4 },
    under: { color: '#F4E3C3', noise: 3 }, skin: { color: '#F1C9A5', noise: 2, edge: false }, hair: { color: '#D8A35A', noise: 5 },
    cape: { color: '#E98A7A', noise: 4, edge: false }, glow: { color: '#FFD27A', noise: 2, edge: false },
    eye: { color: '#5A3A1A', noise: 0, edge: false }, blade: { color: '#FFE7A8', noise: 2, edge: false },
  },
  extraBones: [{ name: 'weapon_l', parent: 'left_arm', pivot: [-5, 12, 0] }],
  parts(add, d) {
    const n = d.neck;
    add(box('head', 'hair', [0, n + 6.5, 0.6], [8.6, 3.4, 8.6], 'hair'));
    add(box('head', 'hair_back', [0, n + 3.5, 3.4], [8.4, 5, 1.6], 'hair'));
    add(box('head', 'eye_r', [1.8, n + 4, -4.05], [1.2, 1, 0.2], 'eye'));
    add(box('head', 'eye_l', [-1.8, n + 4, -4.05], [1.2, 1, 0.2], 'eye'));
    add(box('head', 'diadem', [0, n + 6.2, -4.3], [8.4, 1, 0.6], 'armor'));
    [-30, -15, 0, 15, 30].forEach((a, i) => add(box('head', `ray_${i}`, [Math.sin(a * Math.PI / 180) * 3.5, n + 8.2, -4.4],
      [0.8, 3 + (i === 2 ? 1.5 : 0), 0.5], 'glow', { glow: true, rotation: [0, 0, -a] })));
    add(box('body', 'skirt_f', [0, d.hip - 1.5, -2.2], [7.4, 4, 0.8], 'armor_light', { rotation: [-12, 0, 0] }));
    add(box('body', 'skirt_b', [0, d.hip - 1.5, 2.2], [7.4, 4, 0.8], 'armor_light', { rotation: [12, 0, 0] }));
    add(box('body', 'sash', [0, d.hip + 3, -2.6], [2, 6, 0.5], 'trim'));
    // espadas de luz (apuntan a -Z desde la mano)
    add(box('weapon', 'grip_r', [d.ax, d.hip - 1, -1], [1.2, 1.2, 3], 'trim'));
    add(box('weapon', 'guard_r', [d.ax, d.hip - 1, -2.8], [3.2, 0.8, 0.8], 'armor'));
    add(box('weapon', 'blade_r', [d.ax, d.hip - 1, -9.5], [1, 1.6, 12], 'blade', { glow: true }));
    add(box('weapon_l', 'grip_l', [-d.ax, d.hip - 1, -1], [1.2, 1.2, 3], 'trim'));
    add(box('weapon_l', 'guard_l', [-d.ax, d.hip - 1, -2.8], [3.2, 0.8, 0.8], 'armor'));
    add(box('weapon_l', 'blade_l', [-d.ax, d.hip - 1, -9.5], [1, 1.6, 12], 'blade', { glow: true }));
    add(ring('halo', 'halo', [0, n + 5, 6], 6, 10, 0.7, 'glow', true));
    [0, 45, 90, 135, 180, 225, 270, 315].forEach((a, i) => {
      const r = a * Math.PI / 180;
      add(box('halo', `hray_${i}`, [Math.cos(r) * 8, n + 5 + Math.sin(r) * 8, 6], [0.6, 2.5, 0.6], 'glow', { glow: true, rotation: [0, 0, a - 90] }));
    });
  },
  specials(list, h) {
    const { R0, L0, base, kf } = h;
    list.push({ name: 'special_1', loop: false, length: 0.8, bones: base({
      body: { rotation: kf([[0, [0, 0, 0]], [0.2, [-35, 0, 0]], [0.6, [-35, 0, 0]], [0.8, [0, 0, 0]]]) },
      right_arm: { rotation: kf([[0, R0], [0.2, [-60, 0, 30]], [0.6, [-60, 0, 30]], [0.8, R0]]) },
      left_arm: { rotation: kf([[0, L0], [0.2, [-60, 0, -30]], [0.6, [-60, 0, -30]], [0.8, L0]]) },
      right_leg: { rotation: kf([[0, [0, 0, 0]], [0.2, [40, 0, 0]], [0.6, [40, 0, 0]], [0.8, [0, 0, 0]]]) },
      left_leg: { rotation: kf([[0, [0, 0, 0]], [0.2, [-40, 0, 0]], [0.6, [-40, 0, 0]], [0.8, [0, 0, 0]]]) },
      cape: { rotation: kf([[0, [-6, 0, 0]], [0.2, [-70, 0, 0]], [0.8, [-6, 0, 0]]]) },
    }) });
    list.push({ name: 'special_2', loop: false, length: 0.9, bones: base({
      root: { position: kf([[0, [0, 0, 0]], [0.3, [0, 10, 0]], [0.6, [0, 10, 0]], [0.9, [0, 0, 0]]]) },
      body: { rotation: [[0, [0, 0, 0], 'linear'], [0.75, [0, 720, 0], 'linear'], [0.9, [0, 720, 0], 'linear']] },
      right_arm: { rotation: kf([[0, R0], [0.2, [90, 0, 90]], [0.7, [90, 0, 90]], [0.9, R0]]) },
      left_arm: { rotation: kf([[0, L0], [0.2, [90, 0, -90]], [0.7, [90, 0, -90]], [0.9, L0]]) },
    }) });
  },
});
