// Ayudantes para specs de naves (CommonJS). Unidades de Blockbench (px), Y arriba, frente = -Z.
// Todas devuelven una lista de cubos de spec: {bone,name,from,to,mat,glow,rotation,origin}.
const T = Math.PI / 180;

/** Cubo por centro y tamano. o: {glow, rotation:[rx,ry,rz], origin, inflate, mirror} */
function box(bone, name, c, s, mat, o = {}) {
  const cube = {
    bone, name, mat,
    from: [c[0] - s[0] / 2, c[1] - s[1] / 2, c[2] - s[2] / 2],
    to: [c[0] + s[0] / 2, c[1] + s[1] / 2, c[2] + s[2] / 2],
  };
  if (o.glow) cube.glow = true;
  if (o.rotation) { cube.rotation = o.rotation; cube.origin = o.origin || c.slice(); }
  if (o.uvFrom) cube.uvFrom = o.uvFrom;
  if (o.inflate) cube.inflate = o.inflate;
  if (o.mirror) cube.mirror = true;
  return cube;
}

/**
 * Prisma octogonal regular (4 rectangulos solapados, esquinas sobre los vertices) de apotema a y largo l
 * a lo largo del eje axis ('x'|'y'|'z'), centrado en c. Las alturas varian 0.04 para evitar z-fighting.
 */
function oct(axis, bone, name, c, a, l, mat, o = {}) {
  const long = 2 * a, short = 2 * a * Math.tan(22.5 * T);
  const out = [];
  [0, 90, 45, -45].forEach((ang, i) => {
    const ll = l + 0.04 * i;
    let size, rot = null;
    const A = long, B = short;
    const r = ang;
    if (axis === 'y') { size = [A, ll, B]; if (r) rot = [0, r, 0]; }
    else if (axis === 'z') { size = [A, B, ll]; if (r) rot = [0, 0, r]; }
    else { size = [ll, A, B]; if (r) rot = [r, 0, 0]; }
    out.push(box(bone, `${name}_${i}`, c, size, mat, { glow: o.glow, rotation: rot }));
  });
  return out;
}
const octPlate = (bone, name, c, a, h, mat, o) => oct('y', bone, name, c, a, h, mat, o);
const octTubeZ = (bone, name, c, a, l, mat, o) => oct('z', bone, name, c, a, l, mat, o);
const octBarX = (bone, name, c, a, l, mat, o) => oct('x', bone, name, c, a, l, mat, o);

/**
 * Aro octogonal hueco en el plano XZ (8 segmentos): apotema exterior a, grosor t, alto h. Mucho mas barato en
 * textura que oct() cuando no hace falta que sea macizo.
 */
function octRing(bone, name, c, a, t, h, mat, o = {}) {
  const out = [];
  const L = 2 * a * Math.tan(22.5 * T) + 0.3;
  for (let k = 0; k < 8; k++) {
    const phi = (k * 45) * T; // direccion de la normal exterior: (sin phi, cos phi) en (x,z)
    const r = a - t / 2;
    out.push(box(bone, `${name}_${k}`, [c[0] + r * Math.sin(phi), c[1], c[2] + r * Math.cos(phi)], [L, h, t], mat,
      { glow: o.glow, rotation: k === 0 ? null : [0, k * 45, 0] }));
  }
  return out;
}

/** Aro octogonal hueco alrededor del eje Z (8 segmentos): apotema exterior a, grosor t, profundidad d (en Z). */
function octRingZ(bone, name, c, a, t, d, mat, o = {}) {
  const out = [];
  const L = 2 * a * Math.tan(22.5 * T) + 0.3;
  for (let k = 0; k < 8; k++) {
    const psi = k * 45 * T; // normal exterior (-sin psi, cos psi) en (x,y); psi=0 => arriba
    const r = a - t / 2;
    out.push(box(bone, `${name}_${k}`, [c[0] - r * Math.sin(psi), c[1] + r * Math.cos(psi), c[2]], [L, t, d], mat,
      { glow: o.glow, rotation: k === 0 ? null : [0, 0, k * 45] }));
  }
  return out;
}

module.exports = { box, oct, octRing, octRingZ, octPlate, octTubeZ, octBarX, T };
