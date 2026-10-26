// Medianoche, la Cuarta Hora (noche). Tunica indigo con estrellas, hombreras de media luna, baculo con astrolabio. Levita.
const { build, ring } = require('./lib/hour');
const { box } = require('./lib/shapes');
module.exports = build({
  name: 'medianoche', style: 'mage', weapon: 'staff', float: true,
  h: { leg: 13, torso: 12, head: 8 }, w: { torso: 7, arm: 3, leg: 3.5 }, capeLen: 18,
  palette: {
    armor: { color: '#1E2457', noise: 5 }, armor_light: { color: '#C9CED8', noise: 4 }, trim: { color: '#0E1030', noise: 4 },
    under: { color: '#1E2457', noise: 5 }, skin: { color: '#D9C2B0', noise: 2, edge: false }, hair: { color: '#C9CED8', noise: 5 },
    robe: { color: '#151A44', noise: 5 }, cape: { color: '#0E1030', noise: 4, edge: false }, glow: { color: '#9FA8FF', noise: 2, edge: false },
    star: { color: '#FFFFFF', noise: 0, edge: false }, eye: { color: '#9FA8FF', noise: 0, edge: false }, wood: { color: '#2A2A3A', noise: 6 },
  },
  extraBones: [
    { name: 'ring_1', parent: 'weapon', pivot: [5, 34, 0] },
    { name: 'ring_2', parent: 'weapon', pivot: [5, 34, 0] },
    { name: 'ring_3', parent: 'weapon', pivot: [5, 34, 0] },
  ],
  parts(add, d) {
    const n = d.neck;
    add(box('head', 'hair', [0, n + 6.4, 0.8], [8.6, 3.4, 8.6], 'hair'));
    add(box('head', 'hair_long', [0, n - 1, 3.6], [8, 13, 1.6], 'hair'));
    add(box('head', 'hood', [0, n + 7.6, 1.6], [9.2, 2, 7.4], 'robe'));
    add(box('head', 'eye_r', [1.8, n + 4.2, -4.05], [1.4, 0.8, 0.2], 'eye', { glow: true }));
    add(box('head', 'eye_l', [-1.8, n + 4.2, -4.05], [1.4, 0.8, 0.2], 'eye', { glow: true }));
    add(box('body', 'robe_low', [0, d.hip - 6, 0], [8.4, 12, 5.4], 'robe'));
    add(box('body', 'robe_hem', [0, d.hip - 11.6, 0], [9.4, 1.4, 6.4], 'armor_light'));
    [[-2.5, 4], [2, 7], [-1, 10], [3, 2], [0.5, -4], [-3, -7], [2.5, -9]].forEach(([x, y], i) =>
      add(box('body', `star_${i}`, [x, d.hip + y, -2.85], [0.8, 0.8, 0.3], 'star', { glow: true, rotation: [0, 0, 45] })));
    add(box('right_arm', 'moon_r', [d.ax + 1.6, d.sh + 1, 0], [1.2, 4, 6], 'armor_light', { rotation: [0, 0, -20] }));
    add(box('left_arm', 'moon_l', [-d.ax - 1.6, d.sh + 1, 0], [1.2, 4, 6], 'armor_light', { rotation: [0, 0, 20], mirror: true }));
    // baculo vertical en la mano con astrolabio de 3 aros
    const x = d.ax, y = d.hip - 1;
    add(box('weapon', 'staff', [x, y + 8, 0], [1.4, 28, 1.4], 'wood'));
    add(box('weapon', 'orb', [x, 34, 0], [2.6, 2.6, 2.6], 'star', { glow: true }));
    add(ring('ring_1', 'r1', [x, 34, 0], 3.2, 10, 0.5, 'armor_light', false));
    add(ring('ring_2', 'r2', [x, 34, 0], 4.2, 12, 0.5, 'glow', true));
    add(ring('ring_3', 'r3', [x, 34, 0], 5.2, 14, 0.5, 'armor_light', false));
    add(ring('halo', 'halo', [0, n + 5, 6], 6, 12, 0.6, 'glow', true));
  },
  specials(list, h) {
    const { R0, L0, base, kf, rootFloat } = h;
    const spin = (len) => ({
      ring_1: { rotation: kf([[0, [0, 0, 0]], [len, [0, 360, 0]]], 'linear') },
      ring_2: { rotation: kf([[0, [0, 0, 0]], [len, [360, 0, 0]]], 'linear') },
      ring_3: { rotation: kf([[0, [0, 0, 0]], [len, [0, 0, 360]]], 'linear') },
    });
    // aros girando tambien en reposo
    const idle = list.find((a) => a.name === 'idle');
    Object.assign(idle.bones, spin(2));
    list.push({ name: 'special_1', loop: false, length: 1.4, bones: base(Object.assign({
      root: rootFloat(1.4),
      right_arm: { rotation: kf([[0, R0], [0.4, [170, 0, 15]], [1.0, [170, 0, 15]], [1.4, R0]]) },
      left_arm: { rotation: kf([[0, L0], [0.4, [170, 0, -15]], [1.0, [170, 0, -15]], [1.4, L0]]) },
      head: { rotation: kf([[0, [0, 0, 0]], [0.4, [25, 0, 0]], [1.0, [25, 0, 0]], [1.4, [0, 0, 0]]]) },
    }, spin(0.7))) });
    list.push({ name: 'special_2', loop: false, length: 1.0, bones: base(Object.assign({
      root: rootFloat(1),
      right_arm: { rotation: kf([[0, R0], [0.3, [-10, 0, 10]], [0.5, [100, 0, 0]], [1, R0]]) },
      body: { rotation: kf([[0, [0, 0, 0]], [0.3, [10, 0, 0]], [0.5, [-15, 0, 0]], [1, [0, 0, 0]]]) },
    }, spin(0.5))) });
    list.push({ name: 'float', loop: true, length: 2, bones: base(Object.assign({ root: rootFloat(2) }, spin(2))) });
  },
});
