// IDE Tab Functionality
function initIde() {
    initEditor();
    loadExamples();
    setupIdeActions();
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initIde);
} else {
    initIde();
}

// Initialize CodeMirror Editor
function initEditor() {
    const textarea = document.getElementById('code-editor');
    window.editor = CodeMirror.fromTextArea(textarea, {
        lineNumbers: true,
        theme: 'dracula',
        mode: 'javascript', // JavaScript mode highlights let, if, else, while, fn, print, return perfectly
        indentUnit: 4,
        tabSize: 4,
        lineWrapping: true,
        autofocus: true,
        extraKeys: {
            "Ctrl-Enter": runCode,
            "Cmd-Enter": runCode
        }
    });

    // Default code to load
    window.editor.setValue('print "Welcome to NovaLang!";\nlet name = "Developer";\nprint "Hello, " + name + "!";\n');
}

// Load Examples from API
async function loadExamples() {
    const container = document.getElementById('examples-list');
    
    try {
        const response = await fetch(`${App.apiBase}/examples`);
        if (!response.ok) throw new Error('Failed to retrieve examples');
        
        const examples = await response.json();
        container.innerHTML = ''; // clear skeletons

        // Map short descriptions for visual cards
        const descriptions = {
            "Hello World": "Traditional first program printing a message.",
            "Variables & Math": "Declaring variables, reassigning, and basic operations.",
            "FizzBuzz": "Classic counting program demonstrating loops and conditionals.",
            "Fibonacci": "Recursively computes Fibonacci numbers using functions.",
            "Factorial": "Recursive calculation demonstrating function return values.",
            "Calculator": "Multi-operation calculator utilizing functional execution flow."
        };

        examples.forEach((ex, index) => {
            const card = document.createElement('div');
            card.className = `example-card ${index === 0 ? 'active' : ''}`;
            card.innerHTML = `
                <div class="example-name">${ex.name}</div>
                <div class="example-desc">${descriptions[ex.name] || 'Demonstration program in NovaLang.'}</div>
            `;

            card.addEventListener('click', () => {
                // Clear active states
                document.querySelectorAll('.example-card').forEach(c => c.classList.remove('active'));
                card.classList.add('active');

                // Load code
                window.editor.setValue(ex.code);
                
                // Animate editor refresh
                window.editor.refresh();

                // Automatically run loaded example code
                runCode();
            });

            container.appendChild(card);
        });

        // Load the first example as initial value
        if (examples.length > 0) {
            window.editor.setValue(examples[0].code);
        }

    } catch (err) {
        container.innerHTML = '<div class="example-card" style="border-color: var(--color-error); cursor: default;"><div class="example-name" style="color: var(--color-error);">Failed to load examples</div><div class="example-desc">Please check server status.</div></div>';
        console.error('Error loading examples:', err);
    }
}

// Hook Action Buttons
function setupIdeActions() {
    document.getElementById('btn-run').addEventListener('click', runCode);
    document.getElementById('btn-validate').addEventListener('click', validateCode);
}

// Execute NovaLang code
async function runCode() {
    const runBtn = document.getElementById('btn-run');
    const validateBtn = document.getElementById('btn-validate');
    const outputConsole = document.getElementById('console-output');
    const execTimeLabel = document.getElementById('exec-time');

    const codeValue = window.editor.getValue();
    if (!codeValue.trim()) {
        outputConsole.className = 'console-log error';
        outputConsole.textContent = 'Error: Cannot run empty source code.';
        execTimeLabel.textContent = '';
        return;
    }

    // Update UI states
    runBtn.disabled = true;
    validateBtn.disabled = true;
    runBtn.innerHTML = 'Running...';
    outputConsole.className = 'console-log';
    outputConsole.textContent = 'Executing on backend engine...';
    execTimeLabel.textContent = '';

    try {
        const response = await fetch(`${App.apiBase}/execute`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ code: codeValue })
        });

        if (!response.ok) {
            // Check if HTTP 500 triggered by GlobalExceptionHandler
            const errData = await response.json();
            outputConsole.className = 'console-log error';
            outputConsole.textContent = errData.error || 'Server error occurred during execution.';
            return;
        }

        const data = await response.json();

        if (data.success) {
            outputConsole.className = 'console-log success';
            outputConsole.textContent = data.output || 'Program ran successfully with no stdout output.';
            execTimeLabel.textContent = `Completed in ${data.executionTimeMs}ms`;
        } else {
            outputConsole.className = 'console-log error';
            outputConsole.textContent = data.error || 'Unknown runtime compilation error.';
            execTimeLabel.textContent = `Failed in ${data.executionTimeMs}ms`;
        }

    } catch (err) {
        outputConsole.className = 'console-log error';
        outputConsole.textContent = `Network error: Failed to connect to NovaLang engine. (${err.message})`;
        console.error(err);
    } finally {
        runBtn.disabled = false;
        validateBtn.disabled = false;
        runBtn.innerHTML = `
            <svg class="run-icon" viewBox="0 0 24 24"><path fill="currentColor" d="M8,5.14V19.14L19,12.14L8,5.14Z"/></svg>
            Run Code
        `;
    }
}

// Validate NovaLang syntax without evaluating
async function validateCode() {
    const runBtn = document.getElementById('btn-run');
    const validateBtn = document.getElementById('btn-validate');
    const outputConsole = document.getElementById('console-output');
    const execTimeLabel = document.getElementById('exec-time');

    const codeValue = window.editor.getValue();
    if (!codeValue.trim()) {
        outputConsole.className = 'console-log error';
        outputConsole.textContent = 'Error: Cannot validate empty source code.';
        execTimeLabel.textContent = '';
        return;
    }

    validateBtn.disabled = true;
    runBtn.disabled = true;
    validateBtn.innerHTML = 'Validating...';
    outputConsole.className = 'console-log';
    outputConsole.textContent = 'Validating lexer and parser structure...';
    execTimeLabel.textContent = '';

    try {
        const response = await fetch(`${App.apiBase}/validate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ code: codeValue })
        });

        if (!response.ok) {
            const errData = await response.json();
            outputConsole.className = 'console-log error';
            outputConsole.textContent = errData.error || 'Server error occurred during validation.';
            return;
        }

        const data = await response.json();

        if (data.success) {
            outputConsole.className = 'console-log success';
            outputConsole.textContent = 'Syntax is valid! Ready to execute.';
            execTimeLabel.textContent = `Validated in ${data.executionTimeMs}ms`;
        } else {
            outputConsole.className = 'console-log error';
            outputConsole.textContent = data.error || 'Syntax validation failed.';
            execTimeLabel.textContent = `Validation failed in ${data.executionTimeMs}ms`;
        }

    } catch (err) {
        outputConsole.className = 'console-log error';
        outputConsole.textContent = `Network error: Failed to connect to validation API. (${err.message})`;
    } finally {
        validateBtn.disabled = false;
        runBtn.disabled = false;
        validateBtn.textContent = 'Validate Syntax';
    }
}
