import { readFileSync, writeFileSync, unlinkSync } from 'fs';
import { fileURLToPath, pathToFileURL } from 'url';
import { dirname, join } from 'path';
import { marked } from 'marked';
import { execFileSync } from 'child_process';

const __dirname = dirname(fileURLToPath(import.meta.url));
const mdPath = join(__dirname, 'ERP_BUSINESS_AND_USER_STORIES_AR.md');
const cssPath = join(__dirname, 'erp-doc-pdf.css');
const htmlPath = join(__dirname, 'ERP_BUSINESS_AND_USER_STORIES_AR.tmp.html');
const pdfPath = join(__dirname, 'ERP_BUSINESS_AND_USER_STORIES_AR.pdf');

const md = readFileSync(mdPath, 'utf8');
const css = readFileSync(cssPath, 'utf8');
marked.setOptions({ gfm: true, breaks: false });
const body = marked.parse(md);

const html = `<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>CoreERP — وثيقة الأعمال ودليل الشاشات</title>
  <style>${css}</style>
</head>
<body>
<article class="doc-root">
${body}
</article>
</body>
</html>`;

writeFileSync(htmlPath, html, 'utf8');

const edge = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const fileUrl = pathToFileURL(htmlPath).href;

execFileSync(
  edge,
  [
    '--headless=new',
    '--disable-gpu',
    `--print-to-pdf=${pdfPath}`,
    '--no-pdf-header-footer',
    fileUrl
  ],
  { stdio: 'inherit' }
);

try {
  unlinkSync(htmlPath);
} catch {
  /* ignore */
}
console.log('Wrote:', pdfPath);
