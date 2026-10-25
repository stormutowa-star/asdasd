// Utilidades comunes de los scripts de Blockbench (Playwright + web.blockbench.net)
const { chromium } = require('playwright');

async function openBlockbench(viewport = { width: 1280, height: 800 }) {
  const browser = await chromium.launch({ executablePath: process.env.CHROME || undefined, args: ['--no-sandbox'] });
  const page = await browser.newPage({ viewport });
  page.on('pageerror', (e) => console.error('PAGEERR', e.message));
  await page.goto('https://web.blockbench.net/', { waitUntil: 'load', timeout: 120000 });
  await page.waitForFunction(() => typeof Formats !== 'undefined' && typeof Codecs !== 'undefined' && Formats.modded_entity, null, { timeout: 120000 });
  await page.waitForTimeout(3000);
  return { browser, page };
}

module.exports = { openBlockbench };
