const fs = require('fs');
const path = require('path');

/**
 * Configuration
 */
const TARGET_WORD = 'signc_in_link'; // The word you are looking for
const SEARCH_DIRECTORY = '.';            // Search starting from the current directory

/**
 * Recursive function to search for a word in files
 * @param {string} dir - The directory to search in
 */
function searchFiles(dir) {
    try {
        const files = fs.readdirSync(dir, { withFileTypes: true });

        for (const file of files) {
            const fullPath = path.join(dir, file.name);

            // Skip the script itself to avoid false positives
            if (fullPath.endsWith('search_word.js')) continue;

            if (file.isDirectory()) {
                // Recursive call for directories
                searchFiles(fullPath);
            } else if (file.isFile()) {
                // Read and check file content
                try {
                    const content = fs.readFileSync(fullPath, 'utf8');
                    if (content.includes(TARGET_WORD)) {
                        console.log(`Found in: ${fullPath}`);
                    }
                } catch (readErr) {
                    // Skip files that can't be read (e.g., binary files or permissions)
                }
            }
        }
    } catch (dirErr) {
        console.error(`Error reading directory ${dir}: ${dirErr.message}`);
    }
}

console.log(`Searching for "${TARGET_WORD}" in ${path.resolve(SEARCH_DIRECTORY)}...\n`);
searchFiles(SEARCH_DIRECTORY);
console.log('\nSearch complete.');
