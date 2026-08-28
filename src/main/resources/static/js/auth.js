"use strict";

/*
 * LogiTrack - Autenticación del frontend
 * Ruta: src/main/resources/static/js/auth.js
 */

document.addEventListener("DOMContentLoaded", function () {
    const LOGIN_ENDPOINT = "/auth/login";
    const HOME_PAGE = "index.html";

    const loginForm = document.getElementById("loginForm");
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");

    const usernameError = document.getElementById("usernameError");
    const passwordError = document.getElementById("passwordError");
    const loginMessage = document.getElementById("loginMessage");

    const loginButton = document.getElementById("loginButton");
    const loginButtonText = document.getElementById("loginButtonText");
    const loginLoading = document.getElementById("loginLoading");

    const togglePasswordButton = document.getElementById("togglePassword");

    verificarSesionExistente();
    configurarMostrarPassword();

    if (!loginForm) {
        console.error(
            "No se encontró el formulario con el identificador loginForm."
        );
        return;
    }

    loginForm.addEventListener("submit", iniciarSesion);

    if (usernameInput) {
        usernameInput.addEventListener("input", function () {
            limpiarErrorCampo(usernameInput, usernameError);
            ocultarMensajeGeneral();
        });
    }

    if (passwordInput) {
        passwordInput.addEventListener("input", function () {
            limpiarErrorCampo(passwordInput, passwordError);
            ocultarMensajeGeneral();
        });
    }

    async function iniciarSesion(event) {
        event.preventDefault();

        limpiarErrores();

        if (!validarFormulario()) {
            return;
        }

        const credenciales = {
            username: usernameInput.value.trim(),
            password: passwordInput.value
        };

        cambiarEstadoCarga(true);

        try {
            const response = await fetch(LOGIN_ENDPOINT, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(credenciales)
            });

            const responseBody = await obtenerCuerpoRespuesta(response);

            if (!response.ok) {
                const mensajeError = obtenerMensajeError(
                    response.status,
                    responseBody
                );

                mostrarMensaje(mensajeError, "error");
                passwordInput.value = "";
                passwordInput.focus();
                return;
            }

            const token = obtenerToken(responseBody);

            if (!token) {
                mostrarMensaje(
                    "El servidor autenticó al usuario, pero no devolvió un token JWT.",
                    "error"
                );
                return;
            }

            guardarSesion(responseBody, token, credenciales.username);

            mostrarMensaje(
                "Inicio de sesión correcto. Redirigiendo...",
                "success"
            );

            window.location.replace(HOME_PAGE);
        } catch (error) {
            console.error("Error durante el inicio de sesión:", error);

            mostrarMensaje(
                "No fue posible conectar con el servidor. Verifique que LogiTrack esté en ejecución.",
                "error"
            );
        } finally {
            cambiarEstadoCarga(false);
        }
    }

    function validarFormulario() {
        let formularioValido = true;

        const username = usernameInput
            ? usernameInput.value.trim()
            : "";

        const password = passwordInput
            ? passwordInput.value
            : "";

        if (!username) {
            mostrarErrorCampo(
                usernameInput,
                usernameError,
                "El nombre de usuario es obligatorio."
            );

            formularioValido = false;
        } else if (username.length < 3) {
            mostrarErrorCampo(
                usernameInput,
                usernameError,
                "El nombre de usuario debe tener al menos 3 caracteres."
            );

            formularioValido = false;
        }

        if (!password) {
            mostrarErrorCampo(
                passwordInput,
                passwordError,
                "La contraseña es obligatoria."
            );

            formularioValido = false;
        } else if (password.length < 6) {
            mostrarErrorCampo(
                passwordInput,
                passwordError,
                "La contraseña debe tener al menos 6 caracteres."
            );

            formularioValido = false;
        }

        if (!formularioValido) {
            mostrarMensaje(
                "Revise los campos indicados antes de continuar.",
                "error"
            );

            enfocarPrimerCampoInvalido();
        }

        return formularioValido;
    }

    function guardarSesion(responseBody, token, usernameIngresado) {
        const datosToken = decodificarPayloadJwt(token);

        const username =
            responseBody.username ||
            responseBody.nombreUsuario ||
            datosToken.sub ||
            datosToken.username ||
            usernameIngresado;

        const rol =
            responseBody.rol ||
            responseBody.role ||
            obtenerRolDesdeToken(datosToken);

        localStorage.setItem("token", token);
        localStorage.setItem("username", username);

        if (rol) {
            localStorage.setItem("rol", limpiarPrefijoRol(rol));
        } else {
            localStorage.removeItem("rol");
        }
    }

    function obtenerToken(responseBody) {
        if (!responseBody || typeof responseBody !== "object") {
            return null;
        }

        return (
            responseBody.token ||
            responseBody.accessToken ||
            responseBody.jwt ||
            null
        );
    }

    async function obtenerCuerpoRespuesta(response) {
        const contentType = response.headers.get("content-type") || "";

        if (contentType.includes("application/json")) {
            try {
                return await response.json();
            } catch (error) {
                console.error(
                    "La respuesta indicó JSON, pero no pudo procesarse:",
                    error
                );

                return {};
            }
        }

        try {
            const texto = await response.text();

            return texto
                ? { message: texto }
                : {};
        } catch (error) {
            console.error(
                "No fue posible leer la respuesta del servidor:",
                error
            );

            return {};
        }
    }

    function obtenerMensajeError(status, responseBody) {
        const mensajeServidor =
            responseBody.message ||
            responseBody.mensaje ||
            responseBody.error ||
            responseBody.detail;

        if (mensajeServidor) {
            return mensajeServidor;
        }

        if (responseBody.errors) {
            const mensajeValidacion = obtenerPrimerErrorValidacion(
                responseBody.errors
            );

            if (mensajeValidacion) {
                return mensajeValidacion;
            }
        }

        switch (status) {
            case 400:
                return "Los datos enviados no son válidos.";

            case 401:
                return "El nombre de usuario o la contraseña son incorrectos.";

            case 403:
                return "El usuario no tiene permiso para ingresar o está inactivo.";

            case 404:
                return "No se encontró el endpoint de inicio de sesión.";

            case 500:
                return "Ocurrió un error interno durante la autenticación.";

            default:
                return "No fue posible iniciar sesión. Intente nuevamente.";
        }
    }

    function obtenerPrimerErrorValidacion(errors) {
        if (Array.isArray(errors) && errors.length > 0) {
            const primerError = errors[0];

            if (typeof primerError === "string") {
                return primerError;
            }

            if (primerError && primerError.message) {
                return primerError.message;
            }
        }

        if (errors && typeof errors === "object") {
            const mensajes = Object.values(errors);

            if (mensajes.length > 0) {
                return String(mensajes[0]);
            }
        }

        return null;
    }

    function mostrarErrorCampo(input, errorElement, mensaje) {
        if (input) {
            input.classList.add("input-error");
            input.setAttribute("aria-invalid", "true");
        }

        if (errorElement) {
            errorElement.textContent = mensaje;
        }
    }

    function limpiarErrorCampo(input, errorElement) {
        if (input) {
            input.classList.remove("input-error");
            input.removeAttribute("aria-invalid");
        }

        if (errorElement) {
            errorElement.textContent = "";
        }
    }

    function limpiarErrores() {
        limpiarErrorCampo(usernameInput, usernameError);
        limpiarErrorCampo(passwordInput, passwordError);
        ocultarMensajeGeneral();
    }

    function enfocarPrimerCampoInvalido() {
        const primerCampoInvalido = document.querySelector(
            ".input-error"
        );

        if (primerCampoInvalido) {
            primerCampoInvalido.focus();
        }
    }

    function mostrarMensaje(mensaje, tipo) {
        if (!loginMessage) {
            return;
        }

        loginMessage.textContent = mensaje;
        loginMessage.className = "login-message";

        if (tipo === "success") {
            loginMessage.classList.add("success");
        } else {
            loginMessage.classList.add("error");
        }

        loginMessage.hidden = false;
    }

    function ocultarMensajeGeneral() {
        if (!loginMessage) {
            return;
        }

        loginMessage.textContent = "";
        loginMessage.className = "login-message";
        loginMessage.hidden = true;
    }

    function cambiarEstadoCarga(cargando) {
        if (loginButton) {
            loginButton.disabled = cargando;
        }

        if (usernameInput) {
            usernameInput.disabled = cargando;
        }

        if (passwordInput) {
            passwordInput.disabled = cargando;
        }

        if (togglePasswordButton) {
            togglePasswordButton.disabled = cargando;
        }

        if (loginButtonText) {
            loginButtonText.hidden = cargando;
        }

        if (loginLoading) {
            loginLoading.hidden = !cargando;
        }
    }

    function configurarMostrarPassword() {
        if (!togglePasswordButton || !passwordInput) {
            return;
        }

        togglePasswordButton.addEventListener("click", function () {
            const passwordVisible =
                passwordInput.type === "text";

            passwordInput.type = passwordVisible
                ? "password"
                : "text";

            togglePasswordButton.textContent = passwordVisible
                ? "Mostrar"
                : "Ocultar";

            togglePasswordButton.setAttribute(
                "aria-label",
                passwordVisible
                    ? "Mostrar contraseña"
                    : "Ocultar contraseña"
            );

            togglePasswordButton.setAttribute(
                "aria-pressed",
                String(!passwordVisible)
            );

            passwordInput.focus();
        });
    }

    function verificarSesionExistente() {
        const token = localStorage.getItem("token");

        if (!token) {
            return;
        }

        if (tokenExpirado(token)) {
            limpiarSesion();
            return;
        }

        window.location.replace(HOME_PAGE);
    }

    function tokenExpirado(token) {
        const datosToken = decodificarPayloadJwt(token);

        if (!datosToken.exp) {
            return false;
        }

        const fechaActualEnSegundos = Math.floor(
            Date.now() / 1000
        );

        return datosToken.exp <= fechaActualEnSegundos;
    }

    function decodificarPayloadJwt(token) {
        try {
            const partesToken = token.split(".");

            if (partesToken.length !== 3) {
                return {};
            }

            const payloadBase64 = partesToken[1]
                .replace(/-/g, "+")
                .replace(/_/g, "/");

            const payloadConRelleno = agregarRellenoBase64(
                payloadBase64
            );

            const payloadDecodificado = decodeURIComponent(
                window
                    .atob(payloadConRelleno)
                    .split("")
                    .map(function (caracter) {
                        return (
                            "%" +
                            (
                                "00" +
                                caracter
                                    .charCodeAt(0)
                                    .toString(16)
                            ).slice(-2)
                        );
                    })
                    .join("")
            );

            return JSON.parse(payloadDecodificado);
        } catch (error) {
            console.warn(
                "No fue posible leer la información interna del JWT:",
                error
            );

            return {};
        }
    }

    function agregarRellenoBase64(valor) {
        const resto = valor.length % 4;

        if (resto === 0) {
            return valor;
        }

        return valor + "=".repeat(4 - resto);
    }

    function obtenerRolDesdeToken(datosToken) {
        if (!datosToken || typeof datosToken !== "object") {
            return null;
        }

        if (datosToken.rol) {
            return datosToken.rol;
        }

        if (datosToken.role) {
            return datosToken.role;
        }

        if (
            Array.isArray(datosToken.roles) &&
            datosToken.roles.length > 0
        ) {
            return datosToken.roles[0];
        }

        if (
            Array.isArray(datosToken.authorities) &&
            datosToken.authorities.length > 0
        ) {
            const primeraAutoridad = datosToken.authorities[0];

            if (typeof primeraAutoridad === "string") {
                return primeraAutoridad;
            }

            if (
                primeraAutoridad &&
                primeraAutoridad.authority
            ) {
                return primeraAutoridad.authority;
            }
        }

        return null;
    }

    function limpiarPrefijoRol(rol) {
        return String(rol).replace(/^ROLE_/, "");
    }

    function limpiarSesion() {
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        localStorage.removeItem("rol");
    }
});