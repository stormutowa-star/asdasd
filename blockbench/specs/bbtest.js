// Modelo de prueba del pipeline: humanoide sencillo, colores distintos por pieza.
// Convencion: mira a -Z, brazo derecho en +X, pies en y=0, 16 px = 1 bloque.
module.exports = {
  name: 'bbtest',
  texture: [64, 64],
  palette: {
    body: '#7F7F8C', head: '#D32020', nose: '#FF8A1F', arm_r: '#2050E0', arm_l: '#20B040',
    leg: '#E8D020', leg_dark: '#B8A410', belt: '#8A4A1A', gem: '#E020E0', weapon: '#F4F4F4',
  },
  bones: [
    { name: 'root', parent: null, pivot: [0, 0, 0] },
    { name: 'body', parent: 'root', pivot: [0, 12, 0] },
    { name: 'head', parent: 'body', pivot: [0, 24, 0] },
    { name: 'right_arm', parent: 'body', pivot: [6, 22, 0], rotation: [0, 0, 30] },
    { name: 'left_arm', parent: 'body', pivot: [-6, 22, 0] },
    { name: 'right_leg', parent: 'root', pivot: [2, 12, 0] },
    { name: 'left_leg', parent: 'root', pivot: [-2, 12, 0] },
    { name: 'weapon', parent: 'right_arm', pivot: [6, 12, 0] },
  ],
  cubes: [
    { bone: 'body', name: 'torso', from: [-4, 12, -2], to: [4, 24, 2], mat: 'body' },
    { bone: 'body', name: 'belt', from: [-4, 12, -2], to: [4, 14, 2], inflate: 0.5, mat: 'belt' },
    { bone: 'body', name: 'gem', from: [-2, 16, -4], to: [2, 20, -2], rotation: [0, 45, 0], mat: 'gem' },
    { bone: 'head', name: 'head', from: [-4, 24, -4], to: [4, 32, 4], mat: 'head' },
    { bone: 'head', name: 'nose', from: [-1, 26, -5], to: [1, 28, -4], mat: 'nose' },
    { bone: 'right_arm', name: 'arm_r', from: [4, 12, -2], to: [8, 24, 2], mat: 'arm_r' },
    { bone: 'left_arm', name: 'arm_l', from: [-8, 12, -2], to: [-4, 24, 2], mat: 'arm_l', mirror: true },
    { bone: 'right_leg', name: 'leg_r', from: [0, 0, -2], to: [4, 12, 2], mat: 'leg' },
    { bone: 'left_leg', name: 'leg_l', from: [-4, 0, -2], to: [0, 12, 2], mat: 'leg_dark' },
    { bone: 'weapon', name: 'blade', from: [5, 11.5, -14], to: [7, 12.5, -2], mat: 'weapon', glow: true },
  ],
  animations: [
    {
      name: 'wave', loop: true, length: 2,
      bones: {
        right_arm: {
          rotation: [
            [0, [0, 0, 0], 'catmullrom'], [0.5, [-20, 0, 100], 'catmullrom'], [1, [20, 0, 110], 'catmullrom'],
            [1.5, [-20, 0, 100], 'catmullrom'], [2, [0, 0, 0], 'catmullrom'],
          ],
        },
        head: { position: [[0, [0, 0, 0], 'linear'], [1, [0, 2, 0], 'linear'], [2, [0, 0, 0], 'linear']] },
      },
    },
  ],
};
