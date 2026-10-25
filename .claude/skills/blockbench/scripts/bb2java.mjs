// Uso: node bb2java.mjs <modelo.bbmodel> <ClaseBase> [paquete=com.keinloree.client.model.gen] [dir_raiz_repo]
// Genera <Clase>Model.java y <Clase>Anims.java a partir del .bbmodel (conversion de ejes en SKILL.md).
import fs from 'node:fs';
import path from 'node:path';

const [, , file, cls, pkg = 'com.keinloree.client.model.gen', rootArg] = process.argv;
if (!file || !cls) { console.error('uso: bb2java.mjs <bbmodel> <ClaseBase> [paquete] [raiz]'); process.exit(1); }
const model = JSON.parse(fs.readFileSync(file, 'utf8'));
const repoRoot = path.resolve(rootArg || path.join(path.dirname(path.resolve(file)), '..'));
const outDir = path.join(repoRoot, 'src/main/java', ...pkg.split('.'));
const modId = 'keinloree';
const texName = model.kl_name || path.basename(file, '.bbmodel');

// ---------- jerarquia ----------
const groupInfo = new Map(); // uuid -> grupo
for (const g of model.groups || []) groupInfo.set(g.uuid, g);
const elements = new Map(model.elements.map((e) => [e.uuid, e]));
const bones = []; // {name, parent, origin, rotation, cubes:[]}
function walk(nodes, parent) {
  for (const n of nodes) {
    if (typeof n === 'string') {
      const el = elements.get(n);
      if (!el) throw new Error('elemento desconocido ' + n);
      if (!parent) throw new Error('cubo ' + el.name + ' fuera de un hueso');
      parent.cubes.push(el);
    } else {
      const g = { ...(groupInfo.get(n.uuid) || {}), ...n };
      const bone = { uuid: g.uuid, name: g.name, parent, origin: g.origin || [0, 0, 0], rotation: g.rotation || [0, 0, 0], cubes: [], children: [] };
      bones.push(bone);
      if (parent) parent.children.push(bone);
      walk(g.children || [], bone);
    }
  }
}
walk(model.outliner, null);

const JAVA_KW = new Set(['root', 'abstract', 'class', 'new', 'this', 'default', 'case', 'final', 'package', 'static', 'switch', 'layerRoot']);
const camel = (s) => s.replace(/[^A-Za-z0-9]+(.)?/g, (_, c) => (c ? c.toUpperCase() : ''));
const field = (n) => { const c = camel(n); return JAVA_KW.has(c) ? c + 'Bone' : c; };
const constName = (n) => n.replace(/[^A-Za-z0-9]+/g, '_').toUpperCase();
const f = (v) => { const r = Math.round(v * 10000) / 10000; const s = (Object.is(r, -0) ? 0 : r).toString(); return (s.includes('.') ? s : s + '.0') + 'F'; };
const rad = (deg) => (deg === 0 ? '0.0F' : `(float) Math.toRadians(${f(deg)})`);
const neg0 = (v) => (v === 0 ? 0 : v);

// ---------- Model ----------
function poseOf(b) {
  const o = b.origin, r = b.rotation;
  let off;
  if (!b.parent) off = [-o[0], 24 - o[1], o[2]];
  else { const p = b.parent.origin; off = [-(o[0] - p[0]), -(o[1] - p[1]), o[2] - p[2]]; }
  return { off, rot: [-r[0], -r[1], r[2]] };
}
function poseExpr({ off, rot }) {
  const hasRot = rot.some((v) => v !== 0);
  return hasRot
    ? `PartPose.offsetAndRotation(${f(neg0(off[0]))}, ${f(neg0(off[1]))}, ${f(off[2])}, ${rad(rot[0])}, ${rad(rot[1])}, ${rad(rot[2])})`
    : `PartPose.offset(${f(neg0(off[0]))}, ${f(neg0(off[1]))}, ${f(off[2])})`;
}
function cubeCall(el, pivot, uvRef) {
  const w = el.to[0] - el.from[0], h = el.to[1] - el.from[1], d = el.to[2] - el.from[2];
  const x = -(el.to[0] - pivot[0]), y = -(el.to[1] - pivot[1]), z = el.from[2] - pivot[2];
  const infl = el.inflate ? `, new CubeDeformation(${f(el.inflate)})` : '';
  const uv = el.uv_offset || [0, 0];
  const mir = el.mirror_uv ? '.mirror()' : '';
  const mirOff = el.mirror_uv ? '.mirror(false)' : '';
  return `CubeListBuilder.create().texOffs(${uv[0]}, ${uv[1]})${mir}.addBox(${f(neg0(x))}, ${f(neg0(y))}, ${f(z)}, ${f(w)}, ${f(h)}, ${f(d)}${infl})${mirOff}`;
}

let defs = '';
const varOf = new Map();
function emitBone(b, parentVar, indent) {
  const v = 'pd_' + field(b.name);
  varOf.set(b, v);
  const plain = b.cubes.filter((c) => !c.rotation || c.rotation.every((x) => x === 0));
  const rotated = b.cubes.filter((c) => !plain.includes(c));
  let cl = 'CubeListBuilder.create()';
  const calls = plain.map((c) => cubeCall(c, b.origin).replace('CubeListBuilder.create()', ''));
  cl += calls.join('');
  defs += `${indent}PartDefinition ${v} = ${parentVar}.addOrReplaceChild("${b.name}", ${cl}, ${poseExpr(poseOf(b))});\n`;
  for (const c of rotated) {
    const o = c.origin || c.from.map((x, i) => (x + c.to[i]) / 2);
    const rel = [-(o[0] - b.origin[0]), -(o[1] - b.origin[1]), o[2] - b.origin[2]];
    const rot = [-c.rotation[0], -c.rotation[1], c.rotation[2]];
    defs += `${indent}${v}.addOrReplaceChild("cube_${c.name}", ${cubeCall(c, o)}, ${poseExpr({ off: rel, rot })});\n`;
  }
  for (const ch of b.children) emitBone(ch, v, indent);
}
for (const b of bones.filter((x) => !x.parent)) emitBone(b, 'partdefinition', '        ');

let fields = '', ctor = '';
function emitFields(b, parentExpr) {
  const n = field(b.name);
  fields += `    public final ModelPart ${n};\n`;
  ctor += `        this.${n} = ${parentExpr}.getChild("${b.name}");\n`;
  for (const ch of b.children) emitFields(ch, 'this.' + n);
}
for (const b of bones.filter((x) => !x.parent)) emitFields(b, 'layerRoot');

const tw = model.resolution.width, th = model.resolution.height;
const javaModel = `package ${pkg};

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

/** GENERADO por bb2java.mjs desde blockbench/${path.basename(file)}. No editar a mano: edita la spec y regenera. */
public class ${cls}Model<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("${modId}", "${texName}"), "main");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("${modId}", "textures/entity/${texName}.png");
    public static final ResourceLocation GLOW_TEXTURE = ResourceLocation.fromNamespaceAndPath("${modId}", "textures/entity/${texName}_glow.png");
    private static final Vector3f CACHE = new Vector3f();

    private final ModelPart layerRoot;
${fields}
    public ${cls}Model(ModelPart layerRoot) {
        this.layerRoot = layerRoot;
${ctor}    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
${defs}        return LayerDefinition.create(meshdefinition, ${tw}, ${th});
    }

    @Override
    public ModelPart root() {
        return this.layerRoot;
    }

    /** Vuelve todas las partes a su pose de reposo (llamar antes de aplicar animaciones). */
    public void resetPose() {
        this.layerRoot.getAllParts().forEach(ModelPart::resetPose);
    }

    /** Aplica una animacion en el instante dado (ms); acumula sobre la pose actual. */
    public void applyAnim(AnimationDefinition definition, long timeMs) {
        KeyframeAnimations.animate(this, definition, timeMs, 1.0F, CACHE);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // El renderer concreto decide que animacion aplicar
    }
}
`;

// ---------- Anims ----------
const boneByUuid = new Map(bones.map((b) => [b.uuid, b]));
let anims = '';
for (const a of model.animations || []) {
  const looping = a.loop === 'loop';
  let body = `        AnimationDefinition.Builder.withLength(${f(a.length)})${looping ? '\n            .looping()' : ''}`;
  for (const [uuid, an] of Object.entries(a.animators || {})) {
    const b = boneByUuid.get(uuid);
    if (!b || !an.keyframes) continue;
    for (const ch of ['rotation', 'position', 'scale']) {
      const kfs = an.keyframes.filter((k) => k.channel === ch).sort((p, q) => p.time - q.time);
      if (!kfs.length) continue;
      const target = { rotation: 'ROTATION', position: 'POSITION', scale: 'SCALE' }[ch];
      const items = kfs.map((k) => {
        const dp = k.data_points[0];
        const [x, y, z] = [dp.x, dp.y, dp.z].map((v) => { const n = parseFloat(v); if (Number.isNaN(n)) { console.warn(`AVISO ${a.name}/${b.name}: valor no numerico '${v}'`); return 0; } return n; });
        const interp = k.interpolation === 'catmullrom' ? 'CATMULLROM' : 'LINEAR';
        if (k.interpolation && !['linear', 'catmullrom'].includes(k.interpolation)) console.warn(`AVISO ${a.name}/${b.name}: interpolacion ${k.interpolation} -> LINEAR`);
        let vec;
        if (ch === 'rotation') vec = `KeyframeAnimations.degreeVec(${f(neg0(-x))}, ${f(neg0(-y))}, ${f(z)})`;
        else if (ch === 'position') vec = `new Vector3f(${f(neg0(-x))}, ${f(neg0(-y))}, ${f(z)})`;
        else vec = `KeyframeAnimations.scaleVec(${f(x)}, ${f(y)}, ${f(z)})`;
        return `new Keyframe(${f(k.time)}, ${vec}, AnimationChannel.Interpolations.${interp})`;
      });
      body += `\n            .addAnimation("${b.name}", new AnimationChannel(AnimationChannel.Targets.${target},\n                ${items.join(',\n                ')}))`;
    }
  }
  anims += `    public static final AnimationDefinition ${constName(a.name)} = ${body.trimStart()}\n            .build();\n\n`;
}
const javaAnims = `package ${pkg};

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3f;

/** GENERADO por bb2java.mjs desde blockbench/${path.basename(file)}. No editar a mano. */
public final class ${cls}Anims {
${anims}    private ${cls}Anims() {
    }
}
`;

fs.mkdirSync(outDir, { recursive: true });
fs.writeFileSync(path.join(outDir, cls + 'Model.java'), javaModel);
fs.writeFileSync(path.join(outDir, cls + 'Anims.java'), javaAnims);
console.log(`bb2java: ${outDir}/${cls}Model.java + ${cls}Anims.java (${bones.length} huesos, ${(model.animations || []).length} animaciones)`);
