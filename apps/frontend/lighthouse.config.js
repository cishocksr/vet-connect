import lighthouse from 'lighthouse';
import * as chromeLauncher from 'chrome-launcher';
import { writeFileSync, mkdirSync } from 'fs';
import { join } from 'path';

const pages = [
  { name: 'Home', url: 'http://localhost:5173/' },
  { name: 'Resources', url: 'http://localhost:5173/resources' },
  { name: 'Login', url: 'http://localhost:5173/login' },
  { name: 'Register', url: 'http://localhost:5173/register' },
];

async function runLighthouse() {
  const chrome = await chromeLauncher.launch({ chromeFlags: ['--headless'] });
  
  const options = {
    logLevel: 'info',
    output: 'html',
    onlyCategories: ['accessibility', 'best-practices', 'seo'],
    port: chrome.port,
  };

  // Create reports directory
  mkdirSync('lighthouse-reports', { recursive: true });

  console.log('\n🔍 Running Lighthouse accessibility tests...\n');

  for (const page of pages) {
    console.log(`Testing: ${page.name} (${page.url})`);
    
    try {
      const runnerResult = await lighthouse(page.url, options);
      const reportHtml = runnerResult.report;
      const score = runnerResult.lhr.categories.accessibility.score * 100;

      // Save report
      const fileName = `lighthouse-reports/${page.name.toLowerCase()}-${Date.now()}.html`;
      writeFileSync(fileName, reportHtml);

      console.log(`✅ ${page.name}: Accessibility Score = ${score}/100`);
      console.log(`   Report saved: ${fileName}\n`);

      // Log critical issues
      const audits = runnerResult.lhr.audits;
      const criticalIssues = Object.values(audits).filter(
        audit => audit.score !== null && audit.score < 1 && audit.id.includes('color-contrast')
      );

      if (criticalIssues.length > 0) {
        console.log(`   ⚠️  Color contrast issues found:`);
        criticalIssues.forEach(issue => {
          console.log(`      - ${issue.title}`);
        });
        console.log('');
      }
    } catch (error) {
      console.error(`❌ Error testing ${page.name}:`, error.message);
    }
  }

  await chrome.kill();
  console.log('✨ Lighthouse testing complete! Check lighthouse-reports/ for detailed results.\n');
}

runLighthouse().catch(err => {
  console.error('Error running Lighthouse:', err);
  process.exit(1);
});
