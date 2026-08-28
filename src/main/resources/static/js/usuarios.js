document.addEventListener('DOMContentLoaded', () => {
    const USUARIOS_ENDPOINT = '/api/usuarios';
    const logoutButton = document.getElementById('logoutButton');
    const refreshButton = document.getElementById('refreshUsersButton');
    const form = document.getElementById('userForm');
    const tableBody = document.getElementById('usersTableBody');
    const pageMessage = document.getElementById('pageMessage');

    verificarAutenticacion();
    if (!esAdmin()) {
        mostrarMensaje('Acceso denegado: solo el administrador puede ver y gestionar usuarios.', 'error');
        tableBody.innerHTML = '<tr><td colspan="5" class="no-data">No tienes permiso para ver esta sección.</td></tr>';
        window.location.replace('index.html');
        return;
    }
    configurarEventos();
    cargarUsuarios();

    function configurarEventos() {
        if (logoutButton) {
            logoutButton.addEventListener('click', cerrarSesion);
        }
        if (refreshButton) {
            refreshButton.addEventListener('click', cargarUsuarios);
        }
        if (form) {
            form.addEventListener('submit', guardarUsuario);
        }
    }

    async function cargarUsuarios() {
        if (!esAdmin()) {
            mostrarMensaje('Acceso denegado: solo el administrador puede ver y gestionar usuarios.', 'error');
            esconderAccionesDeAdministracion();
            tableBody.innerHTML = '<tr><td colspan="5" class="no-data">No tienes permiso para gestionar usuarios.</td></tr>';
            return;
        }

        try {
            const response = await fetch(USUARIOS_ENDPOINT, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    'Accept': 'application/json'
                }
            });
            if (!response.ok) {
                if (response.status === 403) {
                    mostrarMensaje('No tiene permisos para ver esta información.', 'error');
                    window.location.replace('index.html');
                    return;
                }
                throw new Error('No se pudieron cargar los usuarios');
            }
            const data = await response.json();
            renderizarTabla(data || []);
            ocultarMensaje();
        } catch (error) {
            console.error(error);
            mostrarMensaje('No fue posible cargar los usuarios.', 'error');
        }
    }

    async function guardarUsuario(event) {
        event.preventDefault();
        if (!esAdmin()) {
            mostrarMensaje('Solo los administradores pueden crear usuarios.', 'error');
            return;
        }

        const payload = {
            nombre: document.getElementById('nombre').value.trim(),
            username: document.getElementById('username').value.trim(),
            password: document.getElementById('password').value,
            rol: document.getElementById('rol').value
        };

        try {
            const response = await fetch(USUARIOS_ENDPOINT, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errorBody = await response.json().catch(() => ({}));
                throw new Error(errorBody.message || 'No fue posible crear el usuario');
            }

            form.reset();
            mostrarMensaje('Usuario creado correctamente.', 'success');
            await cargarUsuarios();
        } catch (error) {
            mostrarMensaje(error.message || 'No fue posible guardar el usuario.', 'error');
        }
    }

    async function eliminarUsuario(id) {
        if (!id) return;

        const confirmado = window.confirm('¿Desea eliminar este usuario?');
        if (!confirmado) return;

        try {
            const response = await fetch(`${USUARIOS_ENDPOINT}/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                }
            });

            if (!response.ok) {
                throw new Error('No fue posible eliminar el usuario');
            }

            mostrarMensaje('Usuario eliminado correctamente.', 'success');
            await cargarUsuarios();
        } catch (error) {
            console.error(error);
            mostrarMensaje(error.message || 'No fue posible eliminar el usuario.', 'error');
        }
    }

    function renderizarTabla(usuarios) {
        if (!tableBody) return;
        if (!usuarios.length) {
            tableBody.innerHTML = '<tr><td colspan="5" class="no-data">No hay usuarios registrados.</td></tr>';
            return;
        }

        tableBody.innerHTML = usuarios.map((usuario) => `
            <tr>
                <td>${usuario.username || '-'}</td>
                <td>${usuario.nombre || '-'}</td>
                <td><span class="badge ${usuario.rol === 'ADMIN' ? 'badge-info' : 'badge-success'}">${usuario.rol || 'EMPLEADO'}</span></td>
                <td><span class="badge ${usuario.activo ? 'badge-success' : 'badge-warning'}">${usuario.activo ? 'Activo' : 'Inactivo'}</span></td>
                <td>
                    ${esAdmin() ? `<button type="button" class="danger-button delete-user-button" data-id="${usuario.id || ''}">Eliminar</button>` : '<span class="text-muted">Sin acceso</span>'}
                </td>
            </tr>
        `).join('');

        if (esAdmin()) {
            tableBody.querySelectorAll('.delete-user-button').forEach((boton) => {
                boton.addEventListener('click', () => eliminarUsuario(boton.getAttribute('data-id')));
            });
        }
    }

    function mostrarMensaje(mensaje, tipo) {
        if (!pageMessage) return;
        pageMessage.hidden = false;
        pageMessage.className = 'alert';
        pageMessage.classList.add(tipo === 'error' ? 'alert-error' : tipo === 'success' ? 'alert-success' : 'alert-info');
        pageMessage.textContent = mensaje;
    }

    function ocultarMensaje() {
        if (pageMessage) {
            pageMessage.hidden = true;
            pageMessage.textContent = '';
        }
    }

    function verificarAutenticacion() {
        const token = localStorage.getItem('token');
        if (!token) {
            window.location.href = 'login.html';
        }
    }

    function esAdmin() {
        return (localStorage.getItem('rol') || '').toUpperCase() === 'ADMIN';
    }

    function esconderAccionesDeAdministracion() {
        const botones = document.querySelectorAll('.delete-user-button');
        botones.forEach((boton) => boton.remove());
    }

    function cerrarSesion() {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('rol');
        window.location.href = 'login.html';
    }
});
