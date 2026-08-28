"use strict";

/*
 * LogiTrack - Gestión de bodegas
 * Ruta: src/main/resources/static/js/bodegas.js
 */

document.addEventListener("DOMContentLoaded", function () {
    /*
     * ASUNCIÓN TÉCNICA:
     * Los controladores utilizan el prefijo /api de forma consistente.
     * Si BodegaController o UsuarioController no utilizan este prefijo,
     * deben modificarse únicamente estas constantes.
     */
    const BODEGAS_ENDPOINT = "/api/bodegas";
    const USUARIOS_ENDPOINT = "/api/usuarios";
    const LOGIN_PAGE = "login.html";

    let bodegas = [];
    let usuarios = [];
    let productos = [];
    let enviandoFormulario = false;
    let eliminandoBodega = false;

    const bodegaForm = document.getElementById("bodegaForm");
    const bodegaIdInput = document.getElementById("bodegaId");
    const nombreInput = document.getElementById("nombre");
    const ubicacionInput = document.getElementById("ubicacion");
    const capacidadInput = document.getElementById("capacidad");
    const encargadoSelect = document.getElementById("encargadoId");

    const nombreError = document.getElementById("nombreError");
    const ubicacionError = document.getElementById("ubicacionError");
    const capacidadError = document.getElementById("capacidadError");
    const encargadoError = document.getElementById("encargadoError");

    const formTitle = document.getElementById("formTitle");
    const saveBodegaButton = document.getElementById(
        "saveBodegaButton"
    );
    const cancelEditButton = document.getElementById(
        "cancelEditButton"
    );
    const clearFormButton = document.getElementById(
        "clearFormButton"
    );
    const newBodegaButton = document.getElementById(
        "newBodegaButton"
    );

    const refreshBodegasButton = document.getElementById(
        "refreshBodegasButton"
    );
    const searchBodegaInput = document.getElementById(
        "searchBodega"
    );
    const capacityFilterInput = document.getElementById(
        "capacityFilter"
    );

    const bodegasTableBody = document.getElementById(
        "bodegasTableBody"
    );
    const bodegasCounter = document.getElementById(
        "bodegasCounter"
    );

    const pageMessage = document.getElementById("pageMessage");
    const logoutButton = document.getElementById("logoutButton");
    const stockFilterSelect = document.getElementById("bodegaStockFilter");
    const stockTableBody = document.getElementById("stockTableBody");

    const summaryTotalBodegas = document.getElementById(
        "summaryTotalBodegas"
    );
    const summaryTotalCapacity = document.getElementById(
        "summaryTotalCapacity"
    );
    const summaryAverageCapacity = document.getElementById(
        "summaryAverageCapacity"
    );

    const deleteModal = document.getElementById("deleteModal");
    const deleteBodegaIdInput = document.getElementById(
        "deleteBodegaId"
    );
    const deleteBodegaName = document.getElementById(
        "deleteBodegaName"
    );
    const closeDeleteModalButton = document.getElementById(
        "closeDeleteModalButton"
    );
    const cancelDeleteButton = document.getElementById(
        "cancelDeleteButton"
    );
    const confirmDeleteButton = document.getElementById(
        "confirmDeleteButton"
    );

    inicializarPagina();

    async function inicializarPagina() {
        if (!verificarAutenticacion()) {
            return;
        }

        configurarEventos();
        await cargarDatosIniciales();
    }

    function configurarEventos() {
        if (bodegaForm) {
            bodegaForm.addEventListener(
                "submit",
                guardarBodega
            );

            bodegaForm.addEventListener(
                "reset",
                function () {
                    window.setTimeout(function () {
                        limpiarErroresFormulario();
                        limpiarModoEdicion();
                    }, 0);
                }
            );
        }

        if (newBodegaButton) {
            newBodegaButton.addEventListener(
                "click",
                prepararNuevaBodega
            );
        }

        if (cancelEditButton) {
            cancelEditButton.addEventListener(
                "click",
                prepararNuevaBodega
            );
        }

        if (clearFormButton) {
            clearFormButton.addEventListener(
                "click",
                function () {
                    limpiarErroresFormulario();
                }
            );
        }

        if (refreshBodegasButton) {
            refreshBodegasButton.addEventListener(
                "click",
                cargarBodegas
            );
        }

        if (searchBodegaInput) {
            searchBodegaInput.addEventListener(
                "input",
                aplicarFiltros
            );
        }

        if (capacityFilterInput) {
            capacityFilterInput.addEventListener(
                "input",
                aplicarFiltros
            );
        }

        if (nombreInput) {
            nombreInput.addEventListener(
                "input",
                function () {
                    limpiarErrorCampo(
                        nombreInput,
                        nombreError
                    );
                }
            );
        }

        if (ubicacionInput) {
            ubicacionInput.addEventListener(
                "input",
                function () {
                    limpiarErrorCampo(
                        ubicacionInput,
                        ubicacionError
                    );
                }
            );
        }

        if (capacidadInput) {
            capacidadInput.addEventListener(
                "input",
                function () {
                    limpiarErrorCampo(
                        capacidadInput,
                        capacidadError
                    );
                }
            );
        }

        if (encargadoSelect) {
            encargadoSelect.addEventListener(
                "change",
                function () {
                    limpiarErrorCampo(
                        encargadoSelect,
                        encargadoError
                    );
                }
            );
        }

        if (closeDeleteModalButton) {
            closeDeleteModalButton.addEventListener(
                "click",
                cerrarModalEliminacion
            );
        }

        if (cancelDeleteButton) {
            cancelDeleteButton.addEventListener(
                "click",
                cerrarModalEliminacion
            );
        }

        if (confirmDeleteButton) {
            confirmDeleteButton.addEventListener(
                "click",
                eliminarBodegaConfirmada
            );
        }

        if (deleteModal) {
            deleteModal.addEventListener(
                "click",
                function (event) {
                    if (event.target === deleteModal) {
                        cerrarModalEliminacion();
                    }
                }
            );
        }

        document.addEventListener(
            "keydown",
            function (event) {
                if (
                    event.key === "Escape" &&
                    deleteModal &&
                    !deleteModal.hidden
                ) {
                    cerrarModalEliminacion();
                }
            }
        );

        if (logoutButton) {
            logoutButton.addEventListener(
                "click",
                cerrarSesion
            );
        }

        if (stockFilterSelect) {
            stockFilterSelect.addEventListener(
                "change",
                cargarStockPorBodega
            );
        }
    }

    async function cargarDatosIniciales() {
        mostrarMensaje(
            "Cargando información de bodegas...",
            "info",
            false
        );

        mostrarFilaInformativa("Cargando bodegas...");

        /*
         * Las dos solicitudes no dependen entre sí, por lo que se realizan
         * al mismo tiempo.
         */
        const resultados = await Promise.allSettled([
            cargarUsuarios(),
            cargarBodegas(),
            cargarProductos()
        ]);

        await cargarStockPorBodega();

        const usuariosFallaron =
            resultados[0].status === "rejected";

        const bodegasFallaron =
            resultados[1].status === "rejected";

        if (!usuariosFallaron && !bodegasFallaron) {
            ocultarMensaje();
            return;
        }

        if (usuariosFallaron && !bodegasFallaron) {
            mostrarMensaje(
                "Las bodegas se cargaron, pero no fue posible consultar los usuarios encargados.",
                "warning",
                false
            );
        }
    }

    async function cargarUsuarios() {
        try {
            const response = await realizarPeticion(
                USUARIOS_ENDPOINT,
                {
                    method: "GET"
                }
            );

            const datos = await leerRespuesta(response);
            usuarios = normalizarLista(datos);

            cargarOpcionesEncargados(usuarios);
        } catch (error) {
            usuarios = [];
            cargarOpcionesEncargados([]);

            if (error.status === 401 || error.status === 403) {
                manejarSesionNoValida(error.status);
                throw error;
            }

            console.error(
                "Error al consultar los usuarios:",
                error
            );

            throw error;
        }
    }

    async function cargarStockPorBodega() {
        if (!stockTableBody) {
            return;
        }

        const selectedBodegaId = stockFilterSelect?.value || "";

        try {
            const endpoint = selectedBodegaId
                ? `/api/inventarios/bodega/${selectedBodegaId}`
                : '/api/inventarios';

            const response = await realizarPeticion(endpoint, {
                method: 'GET'
            });

            const datos = await leerRespuesta(response);
            const registros = normalizarLista(datos);

            if (stockFilterSelect) {
                const bodegasDisponibles = bodegas.length ? bodegas : [];
                stockFilterSelect.innerHTML = '<option value="">Todas las bodegas</option>' + bodegasDisponibles.map((bodega) => `<option value="${bodega.id}">${bodega.nombre}</option>`).join('');
                stockFilterSelect.value = selectedBodegaId;
            }

            renderizarStock(registros);
        } catch (error) {
            console.error('Error al consultar el stock:', error);
            renderizarStock([]);
        }
    }

    function renderizarStock(registros) {
        if (!stockTableBody) {
            return;
        }

        if (!registros.length) {
            stockTableBody.innerHTML = '<tr><td colspan="3" class="no-data">No hay stock registrado.</td></tr>';
            return;
        }

        stockTableBody.innerHTML = registros.map((registro) => {
            const bodega = bodegas.find((item) => item.id === registro.bodegaId);
            const producto = productos.find((item) => (item.id ?? item.productoId) === registro.productoId);
            return `
                <tr>
                    <td>${bodega ? bodega.nombre : 'Sin bodega'}</td>
                    <td>${producto ? producto.nombre : 'Sin producto'}</td>
                    <td><span class="badge ${registro.stock > 0 ? 'badge-success' : 'badge-warning'}">${registro.stock ?? 0}</span></td>
                </tr>`;
        }).join('');
    }

    async function cargarProductos() {
        try {
            const response = await realizarPeticion('/api/productos', { method: 'GET' });

            const datos = await leerRespuesta(response);
            productos = normalizarLista(datos);
        } catch (error) {
            productos = [];
            console.error('Error al cargar productos para stock:', error);
        }
    }

    async function cargarBodegas() {
        cambiarEstadoBoton(
            refreshBodegasButton,
            true,
            "Cargando..."
        );

        mostrarFilaInformativa("Cargando bodegas...");

        try {
            const response = await realizarPeticion(
                BODEGAS_ENDPOINT,
                {
                    method: "GET"
                }
            );

            const datos = await leerRespuesta(response);
            bodegas = normalizarLista(datos);

            aplicarFiltros();
            actualizarResumen(bodegas);
        } catch (error) {
            bodegas = [];
            renderizarBodegas([]);
            actualizarResumen([]);

            if (error.status === 401 || error.status === 403) {
                manejarSesionNoValida(error.status);
                throw error;
            }

            mostrarFilaInformativa(
                "No fue posible cargar las bodegas."
            );

            mostrarMensaje(
                error.message ||
                    "Ocurrió un error al consultar las bodegas.",
                "error",
                false
            );

            console.error(
                "Error al consultar las bodegas:",
                error
            );

            throw error;
        } finally {
            cambiarEstadoBoton(
                refreshBodegasButton,
                false,
                "Actualizar lista"
            );
        }
    }

    async function guardarBodega(event) {
        event.preventDefault();

        if (enviandoFormulario) {
            return;
        }

        limpiarErroresFormulario();
        ocultarMensaje();

        if (!validarFormulario()) {
            mostrarMensaje(
                "Revise los campos indicados antes de continuar.",
                "error",
                false
            );
            enfocarPrimerCampoInvalido();
            return;
        }

        const id = obtenerNumeroEntero(
            bodegaIdInput ? bodegaIdInput.value : null
        );

        const encargadoId = obtenerNumeroEntero(
            encargadoSelect.value
        );

        const payload = construirBodegaPayload(encargadoId);

        const editando = id !== null;
        const endpoint = editando
            ? BODEGAS_ENDPOINT + "/" + id
            : BODEGAS_ENDPOINT;

        const metodo = editando ? "PUT" : "POST";

        enviandoFormulario = true;
        cambiarEstadoFormulario(true);

        try {
            const response = await realizarPeticion(
                endpoint,
                {
                    method: metodo,
                    body: JSON.stringify(payload)
                }
            );

            await leerRespuesta(response);

            mostrarMensaje(
                editando
                    ? "La bodega fue actualizada correctamente."
                    : "La bodega fue registrada correctamente.",
                "success",
                true
            );

            prepararNuevaBodega();
            await cargarBodegas();
        } catch (error) {
            if (error.status === 401 || error.status === 403) {
                manejarSesionNoValida(error.status);
                return;
            }

            mostrarMensaje(
                error.message ||
                    "No fue posible guardar la bodega.",
                "error",
                false
            );

            console.error(
                "Error al guardar la bodega:",
                error
            );
        } finally {
            enviandoFormulario = false;
            cambiarEstadoFormulario(false);
        }
    }

    function construirBodegaPayload(encargadoId) {
        /*
         * BodegaDto puede representar el encargado mediante encargadoId.
         * También se agrega encargado con su id para facilitar la adaptación
         * cuando el controlador recibe una entidad Bodega.
         */
        return {
            nombre: nombreInput.value.trim(),
            ubicacion: ubicacionInput.value.trim(),
            capacidad: Number.parseInt(
                capacidadInput.value,
                10
            ),
            encargadoId: encargadoId,
            encargado: {
                id: encargadoId
            }
        };
    }

    function validarFormulario() {
        let formularioValido = true;

        const nombre = nombreInput
            ? nombreInput.value.trim()
            : "";

        const ubicacion = ubicacionInput
            ? ubicacionInput.value.trim()
            : "";

        const capacidadTexto = capacidadInput
            ? capacidadInput.value.trim()
            : "";

        const capacidad = Number(capacidadTexto);

        const encargadoId = encargadoSelect
            ? obtenerNumeroEntero(encargadoSelect.value)
            : null;

        if (!nombre) {
            mostrarErrorCampo(
                nombreInput,
                nombreError,
                "El nombre de la bodega es obligatorio."
            );
            formularioValido = false;
        } else if (nombre.length < 2) {
            mostrarErrorCampo(
                nombreInput,
                nombreError,
                "El nombre debe tener al menos 2 caracteres."
            );
            formularioValido = false;
        } else if (nombre.length > 100) {
            mostrarErrorCampo(
                nombreInput,
                nombreError,
                "El nombre no puede superar 100 caracteres."
            );
            formularioValido = false;
        }

        if (!ubicacion) {
            mostrarErrorCampo(
                ubicacionInput,
                ubicacionError,
                "La ubicación es obligatoria."
            );
            formularioValido = false;
        } else if (ubicacion.length < 2) {
            mostrarErrorCampo(
                ubicacionInput,
                ubicacionError,
                "La ubicación debe tener al menos 2 caracteres."
            );
            formularioValido = false;
        } else if (ubicacion.length > 150) {
            mostrarErrorCampo(
                ubicacionInput,
                ubicacionError,
                "La ubicación no puede superar 150 caracteres."
            );
            formularioValido = false;
        }

        if (!capacidadTexto) {
            mostrarErrorCampo(
                capacidadInput,
                capacidadError,
                "La capacidad es obligatoria."
            );
            formularioValido = false;
        } else if (
            !Number.isInteger(capacidad) ||
            capacidad <= 0
        ) {
            mostrarErrorCampo(
                capacidadInput,
                capacidadError,
                "La capacidad debe ser un número entero mayor que cero."
            );
            formularioValido = false;
        }

        if (encargadoId === null) {
            mostrarErrorCampo(
                encargadoSelect,
                encargadoError,
                "Debe seleccionar un encargado."
            );
            formularioValido = false;
        }

        return formularioValido;
    }

    function editarBodega(bodega) {
        const id = obtenerIdBodega(bodega);

        if (id === null) {
            mostrarMensaje(
                "No fue posible identificar la bodega seleccionada.",
                "error",
                false
            );
            return;
        }

        const encargadoId = obtenerEncargadoId(bodega);

        bodegaIdInput.value = id;
        nombreInput.value = obtenerTextoSeguro(
            bodega.nombre
        );
        ubicacionInput.value = obtenerTextoSeguro(
            bodega.ubicacion
        );
        capacidadInput.value = obtenerCapacidad(bodega);

        if (encargadoId !== null) {
            asegurarOpcionEncargado(bodega, encargadoId);
            encargadoSelect.value = String(encargadoId);
        } else {
            encargadoSelect.value = "";
        }

        if (formTitle) {
            formTitle.textContent = "Editar bodega";
        }

        if (saveBodegaButton) {
            saveBodegaButton.textContent =
                "Actualizar bodega";
        }

        if (cancelEditButton) {
            cancelEditButton.hidden = false;
        }

        limpiarErroresFormulario();
        ocultarMensaje();

        if (nombreInput) {
            nombreInput.focus();
        }

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    }

    function prepararNuevaBodega() {
        if (bodegaForm) {
            bodegaForm.reset();
        }

        limpiarModoEdicion();
        limpiarErroresFormulario();

        if (nombreInput) {
            nombreInput.focus();
        }
    }

    function limpiarModoEdicion() {
        if (bodegaIdInput) {
            bodegaIdInput.value = "";
        }

        if (formTitle) {
            formTitle.textContent = "Registrar bodega";
        }

        if (saveBodegaButton) {
            saveBodegaButton.textContent =
                "Guardar bodega";
        }

        if (cancelEditButton) {
            cancelEditButton.hidden = true;
        }
    }

    function solicitarEliminacion(bodega) {
        const id = obtenerIdBodega(bodega);

        if (id === null) {
            mostrarMensaje(
                "No fue posible identificar la bodega seleccionada.",
                "error",
                false
            );
            return;
        }

        if (deleteBodegaIdInput) {
            deleteBodegaIdInput.value = id;
        }

        if (deleteBodegaName) {
            deleteBodegaName.textContent =
                obtenerTextoSeguro(bodega.nombre) ||
                "seleccionada";
        }

        abrirModalEliminacion();
    }

    function abrirModalEliminacion() {
        if (!deleteModal) {
            return;
        }

        deleteModal.hidden = false;
        document.body.style.overflow = "hidden";

        if (confirmDeleteButton) {
            confirmDeleteButton.focus();
        }
    }

    function cerrarModalEliminacion() {
        if (!deleteModal || eliminandoBodega) {
            return;
        }

        deleteModal.hidden = true;
        document.body.style.overflow = "";

        if (deleteBodegaIdInput) {
            deleteBodegaIdInput.value = "";
        }

        if (deleteBodegaName) {
            deleteBodegaName.textContent = "";
        }
    }

    async function eliminarBodegaConfirmada() {
        if (eliminandoBodega) {
            return;
        }

        const id = obtenerNumeroEntero(
            deleteBodegaIdInput
                ? deleteBodegaIdInput.value
                : null
        );

        if (id === null) {
            cerrarModalEliminacion();

            mostrarMensaje(
                "No fue posible identificar la bodega que desea eliminar.",
                "error",
                false
            );
            return;
        }

        eliminandoBodega = true;

        cambiarEstadoBoton(
            confirmDeleteButton,
            true,
            "Eliminando..."
        );

        try {
            const response = await realizarPeticion(
                BODEGAS_ENDPOINT + "/" + id,
                {
                    method: "DELETE"
                }
            );

            await leerRespuesta(response);

            deleteModal.hidden = true;
            document.body.style.overflow = "";

            mostrarMensaje(
                "La bodega fue eliminada correctamente.",
                "success",
                true
            );

            if (
                obtenerNumeroEntero(bodegaIdInput.value) === id
            ) {
                prepararNuevaBodega();
            }

            await cargarBodegas();
        } catch (error) {
            if (error.status === 401 || error.status === 403) {
                manejarSesionNoValida(error.status);
                return;
            }

            mostrarMensaje(
                error.message ||
                    "No fue posible eliminar la bodega. Puede estar relacionada con inventarios o movimientos.",
                "error",
                false
            );

            console.error(
                "Error al eliminar la bodega:",
                error
            );
        } finally {
            eliminandoBodega = false;

            cambiarEstadoBoton(
                confirmDeleteButton,
                false,
                "Eliminar"
            );
        }
    }

    function aplicarFiltros() {
        const textoBusqueda = normalizarTexto(
            searchBodegaInput
                ? searchBodegaInput.value
                : ""
        );

        const capacidadMinimaTexto = capacityFilterInput
            ? capacityFilterInput.value.trim()
            : "";

        const capacidadMinima =
            capacidadMinimaTexto === ""
                ? null
                : Number(capacidadMinimaTexto);

        const bodegasFiltradas = bodegas.filter(
            function (bodega) {
                const nombre = normalizarTexto(
                    bodega.nombre
                );

                const ubicacion = normalizarTexto(
                    bodega.ubicacion
                );

                const coincideTexto =
                    !textoBusqueda ||
                    nombre.includes(textoBusqueda) ||
                    ubicacion.includes(textoBusqueda);

                const capacidad = obtenerCapacidad(bodega);

                const coincideCapacidad =
                    capacidadMinima === null ||
                    Number.isNaN(capacidadMinima) ||
                    capacidad >= capacidadMinima;

                return (
                    coincideTexto &&
                    coincideCapacidad
                );
            }
        );

        renderizarBodegas(bodegasFiltradas);
    }

    function renderizarBodegas(lista) {
        if (!bodegasTableBody) {
            return;
        }

        bodegasTableBody.innerHTML = "";

        if (!Array.isArray(lista) || lista.length === 0) {
            mostrarFilaInformativa(
                bodegas.length === 0
                    ? "No hay bodegas registradas."
                    : "No se encontraron bodegas con los filtros seleccionados."
            );

            actualizarContador(0);
            return;
        }

        lista.forEach(function (bodega) {
            const fila = document.createElement("tr");

            fila.appendChild(
                crearCelda(obtenerIdBodega(bodega) ?? "")
            );

            fila.appendChild(
                crearCelda(
                    obtenerTextoSeguro(bodega.nombre)
                )
            );

            fila.appendChild(
                crearCelda(
                    obtenerTextoSeguro(bodega.ubicacion)
                )
            );

            fila.appendChild(
                crearCelda(
                    formatearNumero(
                        obtenerCapacidad(bodega)
                    )
                )
            );

            fila.appendChild(
                crearCelda(
                    obtenerNombreEncargado(bodega)
                )
            );

            const celdaAcciones =
                document.createElement("td");

            celdaAcciones.className = "table-actions";

            const botonEditar =
                document.createElement("button");

            botonEditar.type = "button";
            botonEditar.className = "action-button";
            botonEditar.textContent = "Editar";
            botonEditar.addEventListener(
                "click",
                function () {
                    editarBodega(bodega);
                }
            );

            const botonEliminar =
                document.createElement("button");

            botonEliminar.type = "button";
            botonEliminar.className = "danger-button";
            botonEliminar.textContent = "Eliminar";
            botonEliminar.addEventListener(
                "click",
                function () {
                    solicitarEliminacion(bodega);
                }
            );

            celdaAcciones.appendChild(botonEditar);
            celdaAcciones.appendChild(botonEliminar);

            fila.appendChild(celdaAcciones);
            bodegasTableBody.appendChild(fila);
        });

        actualizarContador(lista.length);
    }

    function crearCelda(valor) {
        const celda = document.createElement("td");
        celda.textContent = obtenerTextoSeguro(valor);
        return celda;
    }

    function mostrarFilaInformativa(mensaje) {
        if (!bodegasTableBody) {
            return;
        }

        bodegasTableBody.innerHTML = "";

        const fila = document.createElement("tr");
        const celda = document.createElement("td");

        celda.colSpan = 6;
        celda.className = "no-data";
        celda.textContent = mensaje;

        fila.appendChild(celda);
        bodegasTableBody.appendChild(fila);
    }

    function actualizarContador(cantidadVisible) {
        if (!bodegasCounter) {
            return;
        }

        bodegasCounter.textContent =
            "Bodegas mostradas: " +
            cantidadVisible +
            " de " +
            bodegas.length;
    }

    function actualizarResumen(lista) {
        const cantidad = Array.isArray(lista)
            ? lista.length
            : 0;

        const capacidadTotal = Array.isArray(lista)
            ? lista.reduce(
                function (acumulado, bodega) {
                    return (
                        acumulado +
                        obtenerCapacidad(bodega)
                    );
                },
                0
            )
            : 0;

        const promedio =
            cantidad > 0
                ? capacidadTotal / cantidad
                : 0;

        if (summaryTotalBodegas) {
            summaryTotalBodegas.textContent =
                formatearNumero(cantidad);
        }

        if (summaryTotalCapacity) {
            summaryTotalCapacity.textContent =
                formatearNumero(capacidadTotal);
        }

        if (summaryAverageCapacity) {
            summaryAverageCapacity.textContent =
                formatearNumero(
                    Math.round(promedio)
                );
        }
    }

    function cargarOpcionesEncargados(listaUsuarios) {
        if (!encargadoSelect) {
            return;
        }

        const valorSeleccionado =
            encargadoSelect.value;

        encargadoSelect.innerHTML = "";

        const opcionInicial =
            document.createElement("option");

        opcionInicial.value = "";
        opcionInicial.textContent =
            "Seleccione un encargado";

        encargadoSelect.appendChild(opcionInicial);

        const usuariosActivos = listaUsuarios.filter(
            function (usuario) {
                return usuario.activo !== false;
            }
        );

        usuariosActivos.forEach(function (usuario) {
            const id = obtenerIdUsuario(usuario);

            if (id === null) {
                return;
            }

            const opcion =
                document.createElement("option");

            opcion.value = id;
            opcion.textContent =
                obtenerNombreUsuario(usuario);

            encargadoSelect.appendChild(opcion);
        });

        if (
            valorSeleccionado &&
            Array.from(encargadoSelect.options).some(
                function (opcion) {
                    return (
                        opcion.value ===
                        valorSeleccionado
                    );
                }
            )
        ) {
            encargadoSelect.value =
                valorSeleccionado;
        }
    }

    function asegurarOpcionEncargado(
        bodega,
        encargadoId
    ) {
        const opcionExiste = Array.from(
            encargadoSelect.options
        ).some(function (opcion) {
            return (
                opcion.value === String(encargadoId)
            );
        });

        if (opcionExiste) {
            return;
        }

        const opcion =
            document.createElement("option");

        opcion.value = encargadoId;
        opcion.textContent =
            obtenerNombreEncargado(bodega);

        encargadoSelect.appendChild(opcion);
    }

    function obtenerNombreEncargado(bodega) {
        if (!bodega || typeof bodega !== "object") {
            return "Sin encargado";
        }

        if (bodega.encargadoNombre) {
            return String(bodega.encargadoNombre);
        }

        if (bodega.nombreEncargado) {
            return String(bodega.nombreEncargado);
        }

        const encargado = bodega.encargado;

        if (encargado && typeof encargado === "object") {
            return obtenerNombreUsuario(encargado);
        }

        return "Sin encargado";
    }

    function obtenerNombreUsuario(usuario) {
        if (!usuario || typeof usuario !== "object") {
            return "Usuario";
        }

        if (usuario.nombre && usuario.username) {
            return (
                usuario.nombre +
                " (" +
                usuario.username +
                ")"
            );
        }

        return (
            usuario.nombre ||
            usuario.username ||
            usuario.nombreUsuario ||
            "Usuario " +
                (obtenerIdUsuario(usuario) ?? "")
        );
    }

    function obtenerEncargadoId(bodega) {
        if (!bodega || typeof bodega !== "object") {
            return null;
        }

        const idDirecto = obtenerNumeroEntero(
            bodega.encargadoId
        );

        if (idDirecto !== null) {
            return idDirecto;
        }

        if (
            bodega.encargado &&
            typeof bodega.encargado === "object"
        ) {
            return obtenerIdUsuario(
                bodega.encargado
            );
        }

        return null;
    }

    function obtenerIdBodega(bodega) {
        if (!bodega || typeof bodega !== "object") {
            return null;
        }

        return obtenerNumeroEntero(
            bodega.id ?? bodega.bodegaId
        );
    }

    function obtenerIdUsuario(usuario) {
        if (!usuario || typeof usuario !== "object") {
            return null;
        }

        return obtenerNumeroEntero(
            usuario.id ?? usuario.usuarioId
        );
    }

    function obtenerCapacidad(bodega) {
        const capacidad = Number(
            bodega && bodega.capacidad
        );

        return Number.isFinite(capacidad)
            ? capacidad
            : 0;
    }

    async function realizarPeticion(url, opciones) {
        const token = localStorage.getItem("token");

        if (!token) {
            limpiarSesion();
            window.location.replace(LOGIN_PAGE);

            throw crearErrorPeticion(
                "No existe una sesión activa.",
                401
            );
        }

        const configuracion = {
            method: "GET",
            ...opciones,
            headers: {
                "Accept": "application/json",
                "Authorization": "Bearer " + token,
                ...(opciones && opciones.body
                    ? {
                        "Content-Type":
                            "application/json"
                    }
                    : {}),
                ...(opciones &&
                opciones.headers
                    ? opciones.headers
                    : {})
            }
        };

        let response;

        try {
            response = await fetch(
                url,
                configuracion
            );
        } catch (error) {
            throw crearErrorPeticion(
                "No fue posible conectar con el servidor.",
                0
            );
        }

        if (!response.ok) {
            const datosError =
                await leerCuerpoRespuesta(response);

            throw crearErrorPeticion(
                obtenerMensajeError(
                    response.status,
                    datosError
                ),
                response.status
            );
        }

        return response;
    }

    async function leerRespuesta(response) {
        if (
            response.status === 204 ||
            response.status === 205
        ) {
            return null;
        }

        return leerCuerpoRespuesta(response);
    }

    async function leerCuerpoRespuesta(response) {
        const contentType =
            response.headers.get("content-type") || "";

        try {
            if (
                contentType.includes(
                    "application/json"
                )
            ) {
                return await response.json();
            }

            const texto = await response.text();

            return texto
                ? { message: texto }
                : null;
        } catch (error) {
            return null;
        }
    }

    function obtenerMensajeError(status, datos) {
        const mensajeServidor =
            extraerMensajeServidor(datos);

        if (mensajeServidor) {
            return mensajeServidor;
        }

        switch (status) {
            case 400:
                return "Los datos enviados no son válidos.";

            case 401:
                return "La sesión no es válida o el token ha vencido.";

            case 403:
                return "No tiene permisos para realizar esta operación.";

            case 404:
                return "No se encontró el recurso solicitado.";

            case 409:
                return "La operación genera un conflicto con los datos existentes.";

            case 500:
                return "Ocurrió un error interno en el servidor.";

            default:
                return "No fue posible completar la solicitud.";
        }
    }

    function extraerMensajeServidor(datos) {
        if (!datos) {
            return null;
        }

        if (typeof datos === "string") {
            return datos;
        }

        const mensaje =
            datos.message ||
            datos.mensaje ||
            datos.error ||
            datos.detail;

        if (mensaje) {
            return String(mensaje);
        }

        if (
            datos.errors &&
            typeof datos.errors === "object"
        ) {
            const errores = Array.isArray(datos.errors)
                ? datos.errors
                : Object.values(datos.errors);

            if (errores.length > 0) {
                const primerError = errores[0];

                if (typeof primerError === "string") {
                    return primerError;
                }

                if (
                    primerError &&
                    primerError.message
                ) {
                    return String(
                        primerError.message
                    );
                }
            }
        }

        return null;
    }

    function crearErrorPeticion(mensaje, status) {
        const error = new Error(mensaje);
        error.status = status;
        return error;
    }

    function normalizarLista(datos) {
        if (Array.isArray(datos)) {
            return datos;
        }

        if (!datos || typeof datos !== "object") {
            return [];
        }

        const posiblesListas = [
            datos.content,
            datos.data,
            datos.resultado,
            datos.bodegas,
            datos.usuarios
        ];

        const listaEncontrada =
            posiblesListas.find(Array.isArray);

        return listaEncontrada || [];
    }

    function mostrarErrorCampo(
        campo,
        elementoError,
        mensaje
    ) {
        if (campo) {
            campo.classList.add("input-error");
            campo.setAttribute(
                "aria-invalid",
                "true"
            );
        }

        if (elementoError) {
            elementoError.textContent = mensaje;
        }
    }

    function limpiarErrorCampo(
        campo,
        elementoError
    ) {
        if (campo) {
            campo.classList.remove("input-error");
            campo.removeAttribute("aria-invalid");
        }

        if (elementoError) {
            elementoError.textContent = "";
        }
    }

    function limpiarErroresFormulario() {
        limpiarErrorCampo(nombreInput, nombreError);
        limpiarErrorCampo(
            ubicacionInput,
            ubicacionError
        );
        limpiarErrorCampo(
            capacidadInput,
            capacidadError
        );
        limpiarErrorCampo(
            encargadoSelect,
            encargadoError
        );
    }

    function enfocarPrimerCampoInvalido() {
        const campoInvalido = document.querySelector(
            "#bodegaForm .input-error"
        );

        if (campoInvalido) {
            campoInvalido.focus();
        }
    }

    function mostrarMensaje(
        mensaje,
        tipo,
        ocultarAutomaticamente
    ) {
        if (!pageMessage) {
            return;
        }

        pageMessage.textContent = mensaje;
        pageMessage.className = "alert";

        switch (tipo) {
            case "success":
                pageMessage.classList.add(
                    "alert-success"
                );
                break;

            case "warning":
                pageMessage.classList.add(
                    "alert-warning"
                );
                break;

            case "info":
                pageMessage.classList.add(
                    "alert-info"
                );
                break;

            default:
                pageMessage.classList.add(
                    "alert-error"
                );
        }

        pageMessage.hidden = false;

        if (ocultarAutomaticamente) {
            window.setTimeout(function () {
                if (
                    pageMessage.textContent === mensaje
                ) {
                    ocultarMensaje();
                }
            }, 5000);
        }
    }

    function ocultarMensaje() {
        if (!pageMessage) {
            return;
        }

        pageMessage.hidden = true;
        pageMessage.textContent = "";
        pageMessage.className = "alert";
    }

    function cambiarEstadoFormulario(cargando) {
        const controles = [
            nombreInput,
            ubicacionInput,
            capacidadInput,
            encargadoSelect,
            clearFormButton,
            cancelEditButton,
            newBodegaButton
        ];

        controles.forEach(function (control) {
            if (control) {
                control.disabled = cargando;
            }
        });

        cambiarEstadoBoton(
            saveBodegaButton,
            cargando,
            cargando
                ? "Guardando..."
                : bodegaIdInput.value
                    ? "Actualizar bodega"
                    : "Guardar bodega"
        );
    }

    function cambiarEstadoBoton(
        boton,
        deshabilitado,
        texto
    ) {
        if (!boton) {
            return;
        }

        boton.disabled = deshabilitado;
        boton.textContent = texto;
    }

    function verificarAutenticacion() {
        const token = localStorage.getItem("token");

        if (!token) {
            limpiarSesion();
            window.location.replace(LOGIN_PAGE);
            return false;
        }

        if (tokenExpirado(token)) {
            limpiarSesion();
            window.location.replace(LOGIN_PAGE);
            return false;
        }

        return true;
    }

    function manejarSesionNoValida(status) {
        limpiarSesion();

        if (status === 403) {
            window.alert(
                "La sesión no tiene permisos para acceder a este recurso."
            );
        }

        window.location.replace(LOGIN_PAGE);
    }

    function cerrarSesion() {
        limpiarSesion();
        window.location.replace(LOGIN_PAGE);
    }

    function limpiarSesion() {
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        localStorage.removeItem("rol");
    }

    function tokenExpirado(token) {
        const payload = decodificarPayloadJwt(token);

        if (!payload.exp) {
            return false;
        }

        const fechaActual = Math.floor(
            Date.now() / 1000
        );

        return payload.exp <= fechaActual;
    }

    function decodificarPayloadJwt(token) {
        try {
            const partes = token.split(".");

            if (partes.length !== 3) {
                return {};
            }

            const base64 = partes[1]
                .replace(/-/g, "+")
                .replace(/_/g, "/");

            const relleno =
                base64.length % 4 === 0
                    ? base64
                    : base64 +
                        "=".repeat(
                            4 - (base64.length % 4)
                        );

            const texto = decodeURIComponent(
                window
                    .atob(relleno)
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

            return JSON.parse(texto);
        } catch (error) {
            return {};
        }
    }

    function obtenerNumeroEntero(valor) {
        if (
            valor === null ||
            valor === undefined ||
            String(valor).trim() === ""
        ) {
            return null;
        }

        const numero = Number(valor);

        return Number.isInteger(numero)
            ? numero
            : null;
    }

    function normalizarTexto(valor) {
        return obtenerTextoSeguro(valor)
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .toLowerCase()
            .trim();
    }

    function obtenerTextoSeguro(valor) {
        if (
            valor === null ||
            valor === undefined
        ) {
            return "";
        }

        return String(valor);
    }

    function formatearNumero(valor) {
        const numero = Number(valor);

        if (!Number.isFinite(numero)) {
            return "0";
        }

        return new Intl.NumberFormat(
            "es-CO",
            {
                maximumFractionDigits: 0
            }
        ).format(numero);
    }
});