const BASE_URL = 'http://' + window.location.hostname + ':8082/api/auth';
const LOGIN_PAGE_URL = 'http://' + window.location.hostname + ':8081/';

let pendingEmail = null;

function showMessage(elementId, text, type) {
    const el = document.getElementById(elementId);
    el.innerHTML = `<div class="message ${type}">${text}</div>`;
    if (type !== 'success') {
        setTimeout(() => el.innerHTML = '', 4000);
    }
}

function goToLogin() {
    window.location.href = LOGIN_PAGE_URL;
}

async function register() {
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('password-confirm').value;

    if (!email) { showMessage('register-message', 'Введите email', 'error'); return; }
    if (password.length < 6) { showMessage('register-message', 'Пароль должен быть не короче 6 символов', 'error'); return; }
    if (password !== passwordConfirm) { showMessage('register-message', 'Пароли не совпадают', 'error'); return; }

    try {
        const response = await fetch(`${BASE_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            showMessage('register-message', data.message || 'Ошибка регистрации', 'error');
            return;
        }

        pendingEmail = data.email;
        showTotpSetup(data.totpSecret, data.qrCodeUri);

    } catch (e) {
        showMessage('register-message', 'Не удалось подключиться к серверу', 'error');
    }
}

function showTotpSetup(secret, qrUri) {
    document.getElementById('register-step').style.display = 'none';
    document.getElementById('totp-step').style.display = 'block';
    document.getElementById('totp-secret-text').textContent = secret;

    const container = document.getElementById('qrcode-container');
    container.innerHTML = '';
    new QRCode(container, { text: qrUri, width: 200, height: 200 });
}

async function confirmTotp() {
    const code = document.getElementById('totp-code').value.trim();

    if (code.length !== 6 || !/^\d+$/.test(code)) {
        showMessage('totp-message', 'Введите 6-значный цифровой код', 'error');
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/2fa/confirm`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: pendingEmail, code })
        });
        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            showMessage('totp-message', data.message || 'Неверный код', 'error');
            return;
        }

        showMessage('totp-message', '2FA активирована! Перенаправляем на страницу входа...', 'success');
        setTimeout(() => { window.location.href = LOGIN_PAGE_URL; }, 1500);

    } catch (e) {
        showMessage('totp-message', 'Ошибка подключения', 'error');
    }
}

document.addEventListener('keydown', (e) => {
    if (e.key !== 'Enter') return;
    if (document.getElementById('totp-step').style.display !== 'none') {
        confirmTotp();
    } else {
        register();
    }
});
