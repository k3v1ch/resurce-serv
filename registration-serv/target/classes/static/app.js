const REGISTER_URL = '/api/auth/register';
const LOGIN_PAGE_URL = 'http://' + window.location.hostname + ':8081/';

function showMessage(text, type) {
    const el = document.getElementById('register-message');
    el.innerHTML = `<div class="message ${type}">${text}</div>`;
    if (type !== 'success') {
        setTimeout(() => el.innerHTML = '', 4000);
    }
}

async function register() {
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('password-confirm').value;

    if (!email) {
        showMessage('Введите email', 'error');
        return;
    }
    if (password.length < 6) {
        showMessage('Пароль должен быть не короче 6 символов', 'error');
        return;
    }
    if (password !== passwordConfirm) {
        showMessage('Пароли не совпадают', 'error');
        return;
    }

    try {
        const response = await fetch(REGISTER_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            showMessage(data.message || 'Ошибка регистрации', 'error');
            return;
        }

        showMessage(
            `Аккаунт ${data.email} создан. Перенаправляем на страницу входа...`,
            'success'
        );
        document.getElementById('email').value = '';
        document.getElementById('password').value = '';
        document.getElementById('password-confirm').value = '';

        setTimeout(() => { window.location.href = LOGIN_PAGE_URL; }, 1500);

    } catch (e) {
        showMessage('Не удалось подключиться к серверу регистрации', 'error');
    }
}

document.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') register();
});
