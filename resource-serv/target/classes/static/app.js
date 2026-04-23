const AUTH_URL = 'http://' + window.location.hostname + ':8082/api/auth';
const NOTES_URL = '/api/notes';

let accessToken = null;
let editingNoteId = null;

function showMessage(elementId, text, type) {
    const el = document.getElementById(elementId);
    el.innerHTML = `<div class="message ${type}">${text}</div>`;
    setTimeout(() => el.innerHTML = '', 3000);
}

async function login() {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch(`${AUTH_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (!response.ok) {
            showMessage('auth-message', data.message || 'Ошибка входа', 'error');
            return;
        }

        accessToken = data.token;
        document.getElementById('user-email').textContent = email;
        document.getElementById('auth-section').style.display = 'none';
        document.getElementById('notes-section').style.display = 'block';

        loadNotes();

    } catch (e) {
        showMessage('auth-message', 'Не удалось подключиться к серверу', 'error');
    }
}

function logout() {
    accessToken = null;
    document.getElementById('auth-section').style.display = 'block';
    document.getElementById('notes-section').style.display = 'none';
    document.getElementById('notes-list').innerHTML = '';
    document.getElementById('email').value = '';
    document.getElementById('password').value = '';
}

async function loadNotes() {
    try {
        const response = await fetch(NOTES_URL, {
            headers: { 'Authorization': `Bearer ${accessToken}` }
        });

        const notes = await response.json();
        renderNotes(notes);

    } catch (e) {
        console.error('Ошибка загрузки заметок', e);
    }
}

function escapeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

const noteStore = {};

function renderNotes(notes) {
    const list = document.getElementById('notes-list');

    if (notes.length === 0) {
        list.innerHTML = '<p style="color:#999; text-align:center;">Заметок пока нет</p>';
        return;
    }

    notes.forEach(note => { noteStore[note.id] = note; });

    list.innerHTML = notes.map(note => `
        <div class="note-card">
            <div class="note-header">
                <span class="note-title">${escapeHtml(note.title)}</span>
                <div class="note-actions">
                    <button class="btn-edit" onclick="openEdit(${note.id})">✏️</button>
                    <button class="btn-danger" onclick="deleteNote(${note.id})">🗑️</button>
                </div>
            </div>
            <div class="note-content">${escapeHtml(note.content)}</div>
            <div class="note-date">
                Создана: ${new Date(note.createdAt).toLocaleString('ru')}
            </div>
        </div>
    `).join('');
}

async function createNote() {
    const title = document.getElementById('new-title').value;
    const content = document.getElementById('new-content').value;

    if (!title.trim()) {
        showMessage('create-message', 'Введите заголовок', 'error');
        return;
    }

    try {
        const response = await fetch(
            `${NOTES_URL}?title=${encodeURIComponent(title)}&content=${encodeURIComponent(content)}`,
            {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );

        if (response.ok) {
            document.getElementById('new-title').value = '';
            document.getElementById('new-content').value = '';
            showMessage('create-message', 'Заметка добавлена!', 'success');
            loadNotes();
        }

    } catch (e) {
        showMessage('create-message', 'Ошибка создания заметки', 'error');
    }
}

function openEdit(id) {
    editingNoteId = id;
    const note = noteStore[id];
    document.getElementById('edit-title').value = note.title;
    document.getElementById('edit-content').value = note.content;
    document.getElementById('edit-modal').classList.add('active');
}

function closeModal() {
    document.getElementById('edit-modal').classList.remove('active');
    editingNoteId = null;
}

async function saveEdit() {
    const title = document.getElementById('edit-title').value;
    const content = document.getElementById('edit-content').value;

    try {
        const response = await fetch(
            `${NOTES_URL}/${editingNoteId}?title=${encodeURIComponent(title)}&content=${encodeURIComponent(content)}`,
            {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${accessToken}` }
            }
        );

        if (response.ok) {
            closeModal();
            loadNotes();
        }

    } catch (e) {
        console.error('Ошибка обновления', e);
    }
}

async function deleteNote(id) {
    if (!confirm('Удалить заметку?')) return;

    try {
        await fetch(`${NOTES_URL}/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${accessToken}` }
        });

        loadNotes();

    } catch (e) {
        console.error('Ошибка удаления', e);
    }
}
