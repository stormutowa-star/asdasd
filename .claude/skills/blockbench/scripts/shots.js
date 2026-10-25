// Uso: NODE_PATH="$(npm root -g)" node shots.js <modelo.bbmodel> [dir_salida] [--anim=wave,idle]
// Capturas en Blockbench: front, side (lado DERECHO del personaje, +X), back, q34 y cada animacion a 0/25/50/75 %.
const fs = require('fs');
const path = require('path');
const { openBlockbench } = require('./bblib');

const file = path.resolve(process.argv[2]);
const name = path.basename(file, '.bbmodel');
const outDir = path.resolve(process.argv[3] && !process.argv[3].startsWith('--') ? process.argv[3] : path.join(path.dirname(file), 'shots', name));
const onlyAnims = (process.argv.find((a) => a.startsWith('--anim=')) || '').slice(7).split(',').filter(Boolean);
fs.mkdirSync(outDir, { recursive: true });

const VIEWS = {
  front: [0, 18, -70], // camara en -Z: ve la cara del personaje
  side: [70, 18, 0], // camara en +X: ve el lado derecho del personaje
  back: [0, 18, 70],
  q34: [-45, 30, -50],
};

(async () => {
  const { browser, page } = await openBlockbench();
  await page.evaluate((m) => { loadModelFile({ name: 'm.bbmodel', content: m, path: '/x/m.bbmodel' }); }, fs.readFileSync(file, 'utf8'));
  await page.waitForTimeout(1500);
  const target = await page.evaluate(() => {
    unselectAll();
    // Centro del modelo para apuntar la camara
    const b = Canvas.bounding_box ? null : null;
    return [0, 16, 0];
  });
  const shoot = async (label) => {
    await page.waitForTimeout(250);
    const box = await page.evaluate(() => { const r = Preview.selected.canvas.getBoundingClientRect(); return { x: r.x, y: r.y, width: r.width, height: r.height }; });
    await page.screenshot({ path: path.join(outDir, label + '.png'), clip: box });
  };
  const setCam = (pos) => page.evaluate(({ pos, target }) => {
    const pv = Preview.selected;
    pv.camera.position.set(...pos);
    pv.controls.target.set(...target);
    pv.camera.lookAt(...target);
    pv.controls.update();
    pv.render();
  }, { pos, target });

  for (const [v, pos] of Object.entries(VIEWS)) { await setCam(pos); await shoot(v); }

  const anims = await page.evaluate(() => Animation.all.map((a) => ({ name: a.name, length: a.length })));
  if (anims.length) {
    await page.evaluate(() => Modes.options.animate.select());
    await page.waitForTimeout(800);
    for (const a of anims) {
      if (onlyAnims.length && !onlyAnims.includes(a.name)) continue;
      for (const pct of [0, 25, 50, 75]) {
        await page.evaluate(({ n, t }) => {
          const anim = Animation.all.find((x) => x.name === n);
          anim.select();
          Timeline.setTime(t);
          Animator.preview();
        }, { n: a.name, t: (a.length * pct) / 100 });
        await setCam(VIEWS.front);
        await shoot(`${a.name}_${pct}_front`);
        if (pct === 0 || pct === 50) { await setCam(VIEWS.side); await shoot(`${a.name}_${pct}_side`); }
      }
    }
  }
  await browser.close();
  console.log('shots:', outDir, fs.readdirSync(outDir).join(' '));
})().catch((e) => { console.error(e); process.exit(1); });
