// Uso: NODE_PATH="$(npm root -g)" node ship_shots.js <bbmodel> <dir> '<json>'  vistas: {nombre:{pos:[x,y,z],target:[x,y,z],fov:45,hide:["hueso"]}} (camaras libres, p. ej. dentro de una cabina)
const fs = require('fs'), path = require('path');
const { openBlockbench } = require('./bblib');
const [file, outDir, viewsJson] = process.argv.slice(2);
const views = JSON.parse(viewsJson);
fs.mkdirSync(outDir, { recursive: true });
(async () => {
  const { browser, page } = await openBlockbench({ width: 2000, height: 1000 });
  await page.evaluate((m) => { loadModelFile({ name: 'm.bbmodel', content: m, path: '/x/m.bbmodel' }); }, fs.readFileSync(file, 'utf8'));
  await page.waitForTimeout(2000);
  for (const [name, v] of Object.entries(views)) {
    await page.evaluate(({ v }) => {
      unselectAll();
      const pv = Preview.selected;
      try { if (v.fov) pv.setFOV(v.fov); } catch (e) {}
      { const hide = v.hide || []; Group.all.forEach((g) => { const vis = !hide.includes(g.name); g.visibility = vis; g.forEachChild((c) => { if (c instanceof Cube) { c.visibility = vis; if (c.mesh) c.mesh.visible = vis; } }, Cube); }); Canvas.updateVisibility && Canvas.updateVisibility(); }
      pv.camera.position.set(...v.pos);
      pv.controls.target.set(...v.target);
      pv.camera.lookAt(...v.target);
      pv.controls.update();
      pv.render();
    }, { v });
    await page.waitForTimeout(400);
    const box = await page.evaluate(() => { const r = Preview.selected.canvas.getBoundingClientRect(); return { x: r.x, y: r.y, width: r.width, height: r.height }; });
    await page.screenshot({ path: path.join(outDir, name + '.png'), clip: box });
  }
  await browser.close();
  console.log('ok', fs.readdirSync(outDir).join(' '));
})().catch((e) => { console.error(e); process.exit(1); });
