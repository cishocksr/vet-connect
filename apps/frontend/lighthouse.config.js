import lighthouse from 'lighthouse';
import * as chromeLauncher from 'chrome-launcher';
import { writeFileSync, mkdirSync } from 'fs';
import { join } from 'path';

const baseUrl = process.env.LIGHTHOUSE_URL || 'http://localhost:5173';

const pages = [
    { name: 'Home', url: `${baseUrl}/` },
    { name: 'Resources', url: `${baseUrl}/resources` },
    { name: 'Login', url: `${baseUrl}/login` },
    { name: 'Register', url: `${baseUrl}/register` },
];

async function runLighthouse() {
    const chrome = await chromeLauncher.launch({
        chromeFlags: ['--headless'],
        // Optionally specify a path to Chrome executable, e.g., using an environment variable.
        // This is useful if chrome-launcher cannot find Chrome automatically or you want to use a specific Chrome version.
        // Example for macOS: export CHROME_PATH='/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'
        // Example for Linux: export CHROME_PATH='/usr/bin/google-chrome'
        // Example for Windows: export CHROME_PATH='C:\Program Files\Google\Chrome\Application\chrome.exe'
        chromePath: process.env.CHROME_PATH,
    });

    // Check if only accessibility was requested from command line
    const onlyAccessibility = process.argv.includes('--only-categories=accessibility');

    const options = {
        logLevel: 'info',
        output: 'html',
        onlyCategories: onlyAccessibility
            ? ['accessibility']
            : ['accessibility', 'best-practices', 'seo', 'performance'],
        port: chrome.port,
    };

    // Create reports directory
    mkdirSync('lighthouse-reports', { recursive: true });

    console.log('\n🔍 Running Lighthouse tests...\n');
    if (onlyAccessibility) {
        console.log('📋 Testing: Accessibility only\n');
    }

    const results = [];

    for (const page of pages) {
        console.log(`Testing: ${page.name} (${page.url})`);

        try {
            const runnerResult = await lighthouse(page.url, options);
            const reportHtml = runnerResult.report;

            // Get scores for all categories
            const scores = {};
            Object.keys(runnerResult.lhr.categories).forEach(category => {
                scores[category] = Math.round(runnerResult.lhr.categories[category].score * 100);
            });

            // Save report
            const timestamp = Date.now();
            const fileName = `lighthouse-reports/${page.name.toLowerCase()}-${timestamp}.html`;
            writeFileSync(fileName, reportHtml);

            console.log(`✅ ${page.name}:`);
            Object.entries(scores).forEach(([category, score]) => {
                const emoji = score >= 90 ? '🟢' : score >= 50 ? '🟡' : '🔴';
                console.log(`   ${emoji} ${category}: ${score}/100`);
            });
            console.log(`   📄 Report: ${fileName}\n`);

            // Log critical accessibility issues
            if (runnerResult.lhr.categories.accessibility) {
                const audits = runnerResult.lhr.audits;
                const criticalIssues = Object.values(audits).filter(
                    audit => audit.score !== null && audit.score < 1 &&
                        (audit.id.includes('color-contrast') || audit.id.includes('aria') || audit.id.includes('label'))
                );

                if (criticalIssues.length > 0) {
                    console.log(`   ⚠️  Critical accessibility issues:`);
                    criticalIssues.forEach(issue => {
                        console.log(`      - ${issue.title}`);
                    });
                    console.log('');
                }
            }

            results.push({ page: page.name, scores });
        } catch (error) {
            console.error(`❌ Error testing ${page.name}:`, error.message);
        }
    }

    await chrome.kill();

    // Summary
    console.log('\n' + '='.repeat(50));
    console.log('📊 SUMMARY');
    console.log('='.repeat(50));
    results.forEach(({ page, scores }) => {
        console.log(`\n${page}:`);
        Object.entries(scores).forEach(([category, score]) => {
            console.log(`  ${category}: ${score}/100`);
        });
    });
    console.log('\n✨ Lighthouse testing complete! Check lighthouse-reports/ for detailed results.\n');
}

runLighthouse().catch(err => {
    console.error('Error running Lighthouse:', err);
    process.exit(1);
});