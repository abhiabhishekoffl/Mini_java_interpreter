// REPL Terminal Functionality
let socket = null;
let term = null;
let currentLine = '';
const history = [];
let historyIndex = -1;
let isTerminalInitialized = false;

window.initTerminalLazy = function() {
    console.log("initTerminalLazy() called. initialized status:", isTerminalInitialized);
    if (isTerminalInitialized) {
        if (window.terminalFit) {
            window.terminalFit();
        }
        if (term) {
            term.focus();
        }
        return;
    }
    isTerminalInitialized = true;

    const container = document.getElementById('terminal');
    console.log("Terminal container element:", container);
    if (!container) {
        console.warn("Terminal container element not found. Returning early.");
        isTerminalInitialized = false;
        return;
    }
    console.log("Terminal class type:", typeof Terminal);

    // Initialize xterm
    term = new Terminal({
        cursorBlink: true,
        cursorStyle: 'block',
        fontFamily: 'JetBrains Mono, Menlo, Monaco, Consolas, monospace',
        fontSize: 14,
        theme: {
            background: '#050409',
            foreground: '#e2dff5',
            cursor: '#00f5ff',
            selection: 'rgba(138, 43, 226, 0.3)',
        },
        convertEol: true
    });

    term.open(container);
    
    // Fit terminal
    window.terminalFit = () => {
        const width = container.clientWidth;
        const height = container.clientHeight;
        const charWidth = 8.5; // Approx pixel width of single character
        const charHeight = 17; // Approx pixel height of single character
        
        const cols = Math.floor(width / charWidth) - 4;
        const rows = Math.floor(height / charHeight) - 2;

        if (cols > 0 && rows > 0) {
            term.resize(cols, rows);
        }
    };
    
    window.terminalFit();
    window.addEventListener('resize', window.terminalFit);

    // Initial socket connection
    connectWebSocket();

    // Setup action buttons
    document.getElementById('btn-clear-repl').addEventListener('click', () => {
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send(':clear');
        } else {
            term.clear();
        }
        term.focus();
    });

    document.getElementById('btn-reset-repl').addEventListener('click', () => {
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send(':reset');
        }
        term.focus();
    });

    // Handle user inputs in terminal
    term.onData(data => {
        const charCode = data.charCodeAt(0);

        if (socket === null || socket.readyState !== WebSocket.OPEN) {
            // If disconnected, pressing Enter reconnects
            if (data === '\r') {
                term.write('\r\nConnecting to engine...');
                connectWebSocket();
            }
            return;
        }

        if (data === '\r') { // Enter
            // Send line
            socket.send(currentLine);
            
            // Add to history
            if (currentLine.trim()) {
                history.push(currentLine);
                historyIndex = history.length;
            }
            
            currentLine = '';
            term.write('\r\n');
        } else if (data === '\u007f' || data === '\b') { // Backspace
            if (currentLine.length > 0) {
                currentLine = currentLine.slice(0, -1);
                term.write('\b \b');
            }
        } else if (data === '\u001b[A') { // Arrow Up (history back)
            if (history.length > 0 && historyIndex > 0) {
                historyIndex--;
                clearLineInput();
                currentLine = history[historyIndex];
                term.write(currentLine);
            }
        } else if (data === '\u001b[B') { // Arrow Down (history forward)
            if (history.length > 0 && historyIndex < history.length) {
                historyIndex++;
                clearLineInput();
                if (historyIndex < history.length) {
                    currentLine = history[historyIndex];
                    term.write(currentLine);
                } else {
                    currentLine = '';
                }
            }
        } else if (charCode >= 32 && charCode < 127) { // Standard printable characters
            currentLine += data;
            term.write(data);
        }
    });
};

function clearLineInput() {
    for (let i = 0; i < currentLine.length; i++) {
        term.write('\b \b');
    }
    currentLine = '';
}

function connectWebSocket() {
    try {
        socket = new WebSocket(App.wsUrl);

        socket.onopen = () => {
            console.log('REPL WebSocket connected');
            term.focus();
        };

        socket.onmessage = event => {
            term.write(event.data);
        };

        socket.onclose = () => {
            term.write('\r\n[Disconnected from NovaLang engine. Press Enter to reconnect]\r\n');
            socket = null;
        };

        socket.onerror = err => {
            term.write(`\r\n[WebSocket connection error: ${err.message || 'unknown'}]\r\n`);
            socket = null;
        };
    } catch (e) {
        term.write(`\r\n[Failed to initiate WebSocket connection: ${e.message}]\r\n`);
        socket = null;
    }
}
