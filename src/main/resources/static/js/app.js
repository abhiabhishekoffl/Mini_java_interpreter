// Global App Configurations & State
const App = {
    apiBase: '/api',
    wsUrl: (() => {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        return `${protocol}//${window.location.host}/ws/repl`;
    })(),
    activeTab: 'tab-ide',
};

// Initialize Application UI
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        initTabs();
        checkHealth();
    });
} else {
    initTabs();
    checkHealth();
}

// Setup tab navigation
function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.getAttribute('data-tab');
            if (targetTab === App.activeTab) return;

            // Update buttons active class
            tabButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // Update content active class
            tabContents.forEach(content => {
                if (content.id === targetTab) {
                    content.classList.add('active');
                } else {
                    content.classList.remove('active');
                }
            });

            App.activeTab = targetTab;

            // Trigger sizing adjustments for code editor or xterm
            if (targetTab === 'tab-ide' && window.editor) {
                window.editor.refresh();
            } else if (targetTab === 'tab-repl') {
                if (window.initTerminalLazy) {
                    window.initTerminalLazy();
                } else if (window.terminalFit) {
                    window.terminalFit();
                }
            }
        });
    });
}

// Quick status indicator using server health check
async function checkHealth() {
    const indicator = document.querySelector('.status-indicator');
    const label = document.querySelector('.status-label');

    try {
        const response = await fetch(`${App.apiBase}/health`);
        if (response.ok) {
            const data = await response.json();
            indicator.className = 'status-indicator online';
            label.textContent = `Engine Ready (v${data.version})`;
        } else {
            throw new Error('Unhealthy status code');
        }
    } catch (err) {
        indicator.className = 'status-indicator';
        indicator.style.backgroundColor = '#ff2a5f';
        indicator.style.boxShadow = '0 0 10px #ff2a5f';
        label.textContent = 'Engine Offline';
        console.error('System health check failed:', err);
    }
}
