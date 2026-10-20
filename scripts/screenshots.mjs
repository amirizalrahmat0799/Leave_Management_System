// Takes the README screenshots from a running app (docker compose up) with Playwright.
// Usage: node scripts/screenshots.mjs [base-url] [output-folder]
import { chromium } from 'playwright';

const base = process.argv[2] ?? 'http://localhost:8080';
const out = process.argv[3] ?? 'docs/images';

const browser = await chromium.launch();

async function as(user, shots) {
  const context = await browser.newContext({ viewport: { width: 1280, height: 800 }, locale: 'en-GB' });
  const page = await context.newPage();
  if (user) {
    await page.goto(`${base}/login.action`);
    await page.fill('#username', user);
    await page.fill('#password', 'demo1234');
    await Promise.all([page.waitForNavigation(), page.click('button[type=submit]')]);
  }
  for (const [path, file] of shots) {
    await page.goto(`${base}/${path}`);
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: `${out}/${file}`, fullPage: true });
    console.log(`saved ${out}/${file}`);
  }
  await context.close();
}

await as(null, [['login.action', 'login.png']]);
await as('daniel', [['dashboard.action', 'dashboard.png'], ['review.action', 'approvals.png']]);
await as('hafiz', [['apply.action', 'apply.png'], ['my-leave.action', 'my-leave.png']]);
await as('aina', [['hr/employees.action', 'employees.png'], ['hr/report.action', 'report.png']]);

await browser.close();
