// Ocaso, la Tercera Hora (atardecer). Capucha puntiaguda, mascara con ojos violetas, capa larga y guadana.
const { build, ring } = require('./lib/hour');
const { box } = require('./lib/shapes');
module.exports = build({
  name: 'ocaso', style: 'reap', weapon: 'scythe',
  h: { leg: 13, torso: 12, head: 8 }, w: { torso: 7, arm: 3, leg: 3.5 }, capeLen: 22,
  palette: {
    armor: { color: '#5A1426', noise: 5 }, armor_light: { color: '#5B2A86', noise: 5 }, trim: { color: '#141017', noise: 4 },
    under: { color: '#141017', noise: 4 }, skin: { color: '#141017', noise: 3, edge: false }, hood: { color: '#3A0E1C', noise: 5 },
    mask: { color: '#5E5866', noise: 5 }, cape: { color: '#2A0A14', noise: 4, edge: false }, glow: { color: '#C24CFF', noise: 2, edge: false },
    wood: { color: '#24161C', noise: 6 }, blade: { color: '#6E6874', noise: 7 },
  },
  parts(add, d) {
    const n = d.neck;
    add(box('head', 'hood', [0, n + 4.5, 0.5], [9.4, 9.4, 9.4], 'hood'));
    add(box('head', 'hood_tip', [0, n + 10, 2.5], [5, 4, 5], 'hood', { rotation: [-25, 0, 0] }));
    add(box('head', 'hood_tip2', [0, n + 12.6, 4.4], [2.6, 3, 2.6], 'hood', { rotation: [-40, 0, 0] }));
    add(box('head', 'mask', [0, n + 3.6, -4.4], [6.6, 5.6, 0.8], 'mask'));
    add(box('head', 'eye_r', [1.6, n + 4.6, -4.9], [1.6, 0.8, 0.3], 'glow', { glow: true }));
    add(box('head', 'eye_l', [-1.6, n + 4.6, -4.9], [1.6, 0.8, 0.3], 'glow', { glow: true }));
    add(box('body', 'sash', [0, d.hip + 6, -2.6], [7.4, 1.4, 0.5], 'armor_light', { rotation: [0, 0, 25] }));
    add(box('body', 'coat_f', [0, d.hip - 3, -2.2], [7.4, 7, 0.8], 'armor', { rotation: [-8, 0, 0] }));
    add(box('cape', 'cape_tatter', [0, n - 23, 3.0], [6, 3, 0.8], 'cape', { rotation: [0, 0, 8] }));
    // guadana sostenida como un jugador: el mango sale de la mano hacia delante y arriba (60 grados), con la hoja al final
    const x = d.ax, H = d.hip - 1, A = 60, dir = [0, Math.sin(A * Math.PI / 180), -Math.cos(A * Math.PI / 180)];
    const rot = [A - 90, 0, 0];
    const at = (t, dy = 0, dz = 0) => [x, H + dir[1] * t + dy, dir[2] * t + dz];
    add(box('weapon', 'shaft', at(9), [1.4, 30, 1.4], 'wood', { rotation: rot }));
    add(box('weapon', 'collar', at(0.5), [2.2, 2.2, 2.2], 'armor_light'));
    add(box('weapon', 'collar_top', at(24), [2, 2, 2], 'armor_light'));
    add(box('weapon', 'blade_1', at(24, 0, -3), [0.8, 2.6, 7], 'blade', { rotation: rot }));
    add(box('weapon', 'blade_2', at(24, 1.5, -9), [0.8, 2.6, 5], 'blade', { rotation: [A - 90 - 30, 0, 0] }));
    add(box('weapon', 'blade_3', at(24, 4.5, -11.5), [0.8, 2.2, 3.5], 'blade', { rotation: [A - 90 - 50, 0, 0] }));
    add(box('weapon', 'edge', at(24, 0.6, -5.5), [0.4, 0.6, 11], 'glow', { glow: true, rotation: rot }));
    add(ring('halo', 'halo', [0, n + 5, 6], 6, 10, 0.6, 'glow', true));
  },
  specials(list, h) {
    const { R0, L0, base, kf } = h;
    list.push({ name: 'special_1', loop: false, length: 0.7, bones: base({
      body: { rotation: kf([[0, [0, 0, 0]], [0.25, [-30, 0, 0]], [0.7, [0, 0, 0]]]) },
      root: { scale: kf([[0, [1, 1, 1]], [0.3, [0.2, 0.05, 0.2]], [0.45, [0.2, 0.05, 0.2]], [0.7, [1, 1, 1]]]) },
      cape: { rotation: kf([[0, [-6, 0, 0]], [0.3, [-80, 0, 0]], [0.7, [-6, 0, 0]]]) },
    }) });
    list.push({ name: 'special_2', loop: false, length: 0.9, bones: base({
      body: { rotation: [[0, [0, 0, 0], 'catmullrom'], [0.2, [0, 60, 0], 'catmullrom'], [0.6, [0, -300, 0], 'linear'], [0.9, [0, -360, 0], 'catmullrom']] },
      right_arm: { rotation: kf([[0, R0], [0.2, [90, 60, 70]], [0.6, [90, 0, 80]], [0.9, R0]]) },
      left_arm: { rotation: kf([[0, L0], [0.2, [80, 0, -60]], [0.9, L0]]) },
    }) });
  },
});
