// JWT Attack Tool frontend
// talks to /api/login and /api/attack/{n} on the same WAR

const $ = (sel) => document.querySelector(sel);

const state = {
    token: null,
    secret: null,
    baseUrl: 'http://localhost:8080/rva/api',
};

const ATTACK_NAMES = {
    1: 'Token Decoder',
    2: 'Token Forgery',
    3: 'Brute Force Secret',
    4: 'alg:none Bypass',
    5: 'Expired Replay',
    6: 'Privilege Escalation',
    7: 'Algorithm Confusion',
    8: 'kid Header Injection',
    9: 'No-Revocation Test',
    10: 'Token Surface Audit',
};

const ATTACK_ORDER = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

const STEP_DELAY = 80;

document.addEventListener('DOMContentLoaded', () => {
    $('#loginBtn').addEventListener('click', login);
    $('#runAllBtn').addEventListener('click', runAll);
    document.querySelectorAll('.btn-attack').forEach(btn => {
        btn.addEventListener('click', () => runAttack(parseInt(btn.dataset.attack, 10)));
    });
    setCmd('awaiting input...');
});

function setCmd(text) {
    $('#cmdLine').textContent = text;
}

function setStatus(connected) {
    const el = $('#status');
    if (connected) {
        el.textContent = 'token captured';
        el.className = 'status connected';
    } else {
        el.textContent = 'disconnected';
        el.className = 'status disconnected';
    }
}

async function login() {
    const baseUrl = $('#baseUrl').value.trim();
    const email = $('#email').value.trim();
    const password = $('#password').value;

    state.baseUrl = baseUrl;
    setCmd(`POST /api/login -> ${email}@${baseUrl}`);

    try {
        const res = await fetch('api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ baseUrl, email, password }),
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || `HTTP ${res.status}`);
        state.token = data.jwt;
        $('#tokenDisplay').value = data.jwt;
        setStatus(true);
        setCmd(`token captured for ${email}`);

        const block = appendAttackBlock('Login - Token Captured');
        appendStep(block, { type: 'OK', text: `Token successfully captured for ${email}` });
        appendStep(block, { type: 'CODE', text: 'JWT', code: data.jwt });
        appendStep(block, { type: 'INFO', text: 'You can now run any of the 10 attacks.' });
    } catch (e) {
        const block = appendAttackBlock('Login - FAILED');
        appendStep(block, { type: 'FAIL', text: e.message });
        setStatus(false);
        setCmd('login failed');
    }
}

async function runAttack(n) {
    if (!state.token && n !== 0) {
        const block = appendAttackBlock(`Attack ${n} - ${ATTACK_NAMES[n]}`);
        appendStep(block, { type: 'FAIL', text: 'No token. Log in first.' });
        return;
    }

    const btn = document.querySelector(`.btn-attack[data-attack="${n}"]`);
    if (btn) btn.classList.add('running');
    setCmd(`POST /api/attack/${n} ${ATTACK_NAMES[n]} ...`);

    const block = appendAttackBlock(`Attack ${n} - ${ATTACK_NAMES[n]}`);
    const spinner = document.createElement('div');
    spinner.className = 'step info';
    spinner.innerHTML = '<span class="spinner">&#9656; executing ...</span>';
    block.appendChild(spinner);
    scrollToBottom();

    try {
        const ctx = {
            token: state.token,
            secret: state.secret,
            baseUrl: state.baseUrl,
        };
        const res = await fetch(`api/attack/${n}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ctx),
        });
        const result = await res.json();
        spinner.remove();

        block.querySelector('.attack-title').textContent = result.title || `Attack ${n}`;

        for (const step of (result.steps || [])) {
            await sleep(STEP_DELAY);
            appendStep(block, step);
            scrollToBottom();
        }

        if (result.conclusion) {
            await sleep(STEP_DELAY * 2);
            const c = document.createElement('div');
            c.className = 'conclusion';
            c.textContent = ' ' + result.conclusion;
            block.appendChild(c);
            scrollToBottom();
        }

        // capture the cracked secret from A3
        if (n === 3) {
            const secretStep = (result.steps || []).find(s => s.type === 'CODE' && (s.text === 'Cracked secret'));
            if (secretStep) {
                state.secret = secretStep.code;
                $('#secretDisplay').value = secretStep.code;
            }
        }

        setCmd(`attack ${n} complete`);
    } catch (e) {
        spinner.remove();
        appendStep(block, { type: 'FAIL', text: 'Network error: ' + e.message });
        setCmd(`attack ${n} failed`);
    } finally {
        if (btn) btn.classList.remove('running');
    }
}

async function runAll() {
    for (const n of ATTACK_ORDER) {
        await runAttack(n);
        await sleep(300);
    }
}

function appendAttackBlock(title) {
    const body = $('#terminalBody');
    // hide the banner after the first attack
    const banner = body.querySelector('.banner');
    if (banner) banner.style.display = 'none';

    const block = document.createElement('div');
    block.className = 'attack-block';
    const h = document.createElement('div');
    h.className = 'attack-title';
    h.textContent = title;
    block.appendChild(h);
    body.appendChild(block);
    scrollToBottom();
    return block;
}

function appendStep(block, step) {
    const type = (step.type || 'INFO').toLowerCase();
    const el = document.createElement('div');
    el.className = 'step ' + type;
    if (type === 'code') {
        if (step.text) {
            const label = document.createElement('span');
            label.className = 'code-label';
            label.textContent = step.text;
            el.appendChild(label);
        }
        const code = document.createElement('div');
        code.textContent = step.code || '';
        el.appendChild(code);
    } else {
        el.textContent = step.text || '';
    }
    block.appendChild(el);
}

function scrollToBottom() {
    const body = $('#terminalBody');
    body.scrollTop = body.scrollHeight;
}

function sleep(ms) {
    return new Promise(r => setTimeout(r, ms));
}
