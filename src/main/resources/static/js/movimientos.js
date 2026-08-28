"use strict";

/*
 * LogiTrack - Gestión de movimientos de inventario
 * Ruta: src/main/resources/static/js/movimientos.js
 */

document.addEventListener("DOMContentLoaded", function () {
    /*
     * ASUNCIÓN TÉCNICA:
     * Los controladores utilizan el prefijo /api.
     *
     * Si los controladores generados anteriormente usan rutas como
     * /movimientos, /bodegas y /productos, solamente deben cambiarse
     * estas tres constantes.
     */
    const MOVIMIENTOS_ENDPOINT = "/api/movimientos";
    const BODEGAS_ENDPOINT = "/api/bodegas";
    const PRODUCTOS_ENDPOINT = "/api/productos";
    const LOGIN_PAGE = "login.html";

    let bodegas = [];
    let productos = [];
    let movimientos = [];
    let detallesMovimiento = [];

    let enviandoMovimiento = false;

    const movimientoForm =
        document.getElementById("movimientoForm");

    const tipoMovimientoSelect =
        document.getElementById("tipoMovimiento");

    const fechaMovimientoInput =
        document.getElementById("fechaMovimiento");

    const bodegaOrigenGroup =
        document.getElementById("bodegaOrigenGroup");

    const bodegaDestinoGroup =
        document.getElementById("bodegaDestinoGroup");

    const bodegaOrigenSelect =
        document.getElementById("bodegaOrigenId");

    const bodegaDestinoSelect =
        document.getElementById("bodegaDestinoId");

    const bodegaOrigenRequired =
        document.getElementById("bodegaOrigenRequired");

    const bodegaDestinoRequired =
        document.getElementById("bodegaDestinoRequired");

    const tipoMovimientoError =
        document.getElementById("tipoMovimientoError");

    const bodegaOrigenError =
        document.getElementById("bodegaOrigenError");

    const bodegaDestinoError =
        document.getElementById("bodegaDestinoError");

    const movementHelp =
        document.getElementById("movementHelp");

    const productoSelect =
        document.getElementById("productoId");

    const cantidadInput =
        document.getElementById("cantidad");

    const productoError =
        document.getElementById("productoError");

    const cantidadError =
        document.getElementById("cantidadError");

    const detallesError =
        document.getElementById("detallesError");

    const addDetailButton =
        document.getElementById("addDetailButton");

    const clearDetailButton =
        document.getElementById("clearDetailButton");

    const movementDetailsTableBody =
        document.getElementById("movementDetailsTableBody");

    const detailsCounter =
        document.getElementById("detailsCounter");

    const saveMovementButton =
        document.getElementById("saveMovementButton");

    const cancelMovementButton =
        document.getElementById("cancelMovementButton");

    const clearMovementButton =
        document.getElementById("clearMovementButton");

    const movementFiltersForm =
        document.getElementById("movementFiltersForm");

    const typeFilterSelect =
        document.getElementById("typeFilter");

    const warehouseFilterSelect =
        document.getElementById("warehouseFilter");

    const startDateFilterInput =
        document.getElementById("startDateFilter");

    const endDateFilterInput =
        document.getElementById("endDateFilter");

    const productSearchFilterInput =
        document.getElementById("productSearchFilter");

    const responsibleSearchFilterInput =
        document.getElementById("responsibleSearchFilter");

    const refreshMovementsButton =
        document.getElementById("refreshMovementsButton");

    const clearFiltersButton =
        document.getElementById("clearFiltersButton");

    const movementsTableBody =
        document.getElementById("movementsTableBody");

    const movementsCounter =
        document.getElementById("movementsCounter");

    const summaryEntries =
        document.getElementById("summaryEntries");

    const summaryOutputs =
        document.getElementById("summaryOutputs");

    const summaryTransfers =
        document.getElementById("summaryTransfers");

    const pageMessage =
        document.getElementById("pageMessage");

    const currentUsername =
        document.getElementById("currentUsername");

    const logoutButton =
        document.getElementById("logoutButton");

    const movementDetailModal =
        document.getElementById("movementDetailModal");

    const closeMovementDetailModalButton =
        document.getElementById(
            "closeMovementDetailModalButton"
        );

    const closeMovementDetailButton =
        document.getElementById(
            "closeMovementDetailButton"
        );

    const detailMovementId =
        document.getElementById("detailMovementId");

    const detailMovementDate =
        document.getElementById("detailMovementDate");

    const detailMovementType =
        document.getElementById("detailMovementType");

    const detailMovementResponsible =
        document.getElementById(
            "detailMovementResponsible"
        );

    const detailMovementOrigin =
        document.getElementById("detailMovementOrigin");

    const detailMovementDestination =
        document.getElementById(
            "detailMovementDestination"
        );

    const movementDetailModalTableBody =
        document.getElementById(
            "movementDetailModalTableBody"
        );

    inicializarPagina();

    async function inicializarPagina() {
        if (!verificarAutenticacion()) {
            return;
        }

        mostrarUsuarioActual();
        configurarEventos();
        establecerFechaActual();
        actualizarCamposPorTipo();
        renderizarDetallesMovimiento();

        await cargarDatosIniciales();
    }

    function configurarEventos() {
        if (movimientoForm) {
            movimientoForm.addEventListener(
                "submit",
                registrarMovimiento
            );
        }

        if (tipoMovimientoSelect) {
            tipoMovimientoSelect.addEventListener(
                "change",
                function () {
                    limpiarErrorCampo(
                        tipoMovimientoSelect,
                        tipoMovimientoError
                    );

                    actualizarCamposPorTipo();
                }
            );
        }

        if (bodegaOrigenSelect) {
            bodegaOrigenSelect.addEventListener(
                "change",
                function () {
                    limpiarErrorCampo(
                        bodegaOrigenSelect,
                        bodegaOrigenError
                    );

                    validarBodegasDiferentes();
                }
            );
        }

        if (bodegaDestinoSelect) {
            bodegaDestinoSelect.addEventListener(
                "change",
                function () {
                    limpiarErrorCampo(
                        bodegaDestinoSelect,
                        bodegaDestinoError
                    );

                    validarBodegasDiferentes();
                }
            );
        }

        if (productoSelect) {
            productoSelect.addEventListener(
                "change",
                function () {
                    limpiarErrorCampo(
                        productoSelect,
                        productoError
                    );
                }
            );
        }

        if (cantidadInput) {
            cantidadInput.addEventListener(
                "input",
                function () {
                    limpiarErrorCampo(
                        cantidadInput,
                        cantidadError
                    );
                }
            );

            cantidadInput.addEventListener(
                "keydown",
                function (event) {
                    if (event.key === "Enter") {
                        event.preventDefault();
                        agregarDetalle();
                    }
                }
            );
        }

        if (addDetailButton) {
            addDetailButton.addEventListener(
                "click",
                agregarDetalle
            );
        }

        if (clearDetailButton) {
            clearDetailButton.addEventListener(
                "click",
                limpiarFormularioDetalle
            );
        }

        if (clearMovementButton) {
            clearMovementButton.addEventListener(
                "click",
                limpiarMovimiento
            );
        }

        if (cancelMovementButton) {
            cancelMovementButton.addEventListener(
                "click",
                limpiarMovimiento
            );
        }

        if (refreshMovementsButton) {
            refreshMovementsButton.addEventListener(
                "click",
                cargarMovimientos
            );
        }

        if (movementFiltersForm) {
            movementFiltersForm.addEventListener(
                "submit",
                function (event) {
                    event.preventDefault();
                    aplicarFiltros();
                }
            );

            movementFiltersForm.addEventListener(
                "reset",
                function () {
                    window.setTimeout(function () {
                        aplicarFiltros();
                    }, 0);
                }
            );
        }

        if (typeFilterSelect) {
            typeFilterSelect.addEventListener(
                "change",
                aplicarFiltros
            );
        }

        if (warehouseFilterSelect) {
            warehouseFilterSelect.addEventListener(
                "change",
                aplicarFiltros
            );
        }

        if (startDateFilterInput) {
            startDateFilterInput.addEventListener(
                "change",
                aplicarFiltros
            );
        }

        if (endDateFilterInput) {
            endDateFilterInput.addEventListener(
                "change",
                aplicarFiltros
            );
        }

        if (productSearchFilterInput) {
            productSearchFilterInput.addEventListener(
                "input",
                aplicarFiltros
            );
        }

        if (responsibleSearchFilterInput) {
            responsibleSearchFilterInput.addEventListener(
                "input",
                aplicarFiltros
            );
        }

        if (clearFiltersButton) {
            clearFiltersButton.addEventListener(
                "click",
                function () {
                    window.setTimeout(
                        aplicarFiltros,
                        0
                    );
                }
            );
        }

        if (closeMovementDetailModalButton) {
            closeMovementDetailModalButton.addEventListener(
                "click",
                cerrarModalDetalle
            );
        }

        if (closeMovementDetailButton) {
            closeMovementDetailButton.addEventListener(
                "click",
                cerrarModalDetalle
            );
        }

        if (movementDetailModal) {
            movementDetailModal.addEventListener(
                "click",
                function (event) {
                    if (
                        event.target ===
                        movementDetailModal
                    ) {
                        cerrarModalDetalle();
                    }
                }
            );
        }

        document.addEventListener(
            "keydown",
            function (event) {
                if (
                    event.key === "Escape" &&
                    movementDetailModal &&
                    !movementDetailModal.hidden
                ) {
                    cerrarModalDetalle();
                }
            }
        );

        if (logoutButton) {
            logoutButton.addEventListener(
                "click",
                cerrarSesion
            );
        }
    }

    async function cargarDatosIniciales() {
        mostrarMensaje(
            "Cargando bodegas, productos y movimientos...",
            "info",
            false
        );

        mostrarFilaMovimientos(
            "Cargando movimientos..."
        );

        const resultados = await Promise.allSettled([
            cargarBodegas(),
            cargarProductos(),
            cargarMovimientos()
        ]);

        const errores = resultados.filter(
            function (resultado) {
                return resultado.status === "rejected";
            }
        );

        if (errores.length === 0) {
            ocultarMensaje();
            return;
        }

        if (errores.length < resultados.length) {
            mostrarMensaje(
                "Parte de la información no pudo cargarse. Revise la conexión y los endpoints.",
                "warning",
                false
            );
        }
    }

    async function cargarBodegas() {
        try {
            const response = await realizarPeticion(
                BODEGAS_ENDPOINT,
                {
                    method: "GET"
                }
            );

            const datos = await leerRespuesta(response);
            bodegas = normalizarLista(
                datos,
                "bodegas"
            );

            cargarSelectBodegas(
                bodegaOrigenSelect,
                "Seleccione la bodega de origen"
            );

            cargarSelectBodegas(
                bodegaDestinoSelect,
                "Seleccione la bodega de destino"
            );

            cargarSelectBodegas(
                warehouseFilterSelect,
                "Todas las bodegas"
            );
        } catch (error) {
            bodegas = [];

            cargarSelectBodegas(
                bodegaOrigenSelect,
                "No hay bodegas disponibles"
            );

            cargarSelectBodegas(
                bodegaDestinoSelect,
                "No hay bodegas disponibles"
            );

            cargarSelectBodegas(
                warehouseFilterSelect,
                "Todas las bodegas"
            );

            manejarErrorAutenticacion(error);
            throw error;
        }
    }

    async function cargarProductos() {
        try {
            const response = await realizarPeticion(
                PRODUCTOS_ENDPOINT,
                {
                    method: "GET"
                }
            );

            const datos = await leerRespuesta(response);
            productos = normalizarLista(
                datos,
                "productos"
            );

            cargarSelectProductos();
        } catch (error) {
            productos = [];
            cargarSelectProductos();

            manejarErrorAutenticacion(error);
            throw error;
        }
    }

    async function cargarMovimientos() {
        cambiarEstadoBoton(
            refreshMovementsButton,
            true,
            "Cargando..."
        );

        mostrarFilaMovimientos(
            "Cargando movimientos..."
        );

        try {
            const response = await realizarPeticion(
                MOVIMIENTOS_ENDPOINT,
                {
                    method: "GET"
                }
            );

            const datos = await leerRespuesta(response);

            movimientos = normalizarLista(
                datos,
                "movimientos"
            );

            aplicarFiltros();
            actualizarResumen(movimientos);
        } catch (error) {
            movimientos = [];
            renderizarMovimientos([]);
            actualizarResumen([]);

            manejarErrorAutenticacion(error);

            mostrarFilaMovimientos(
                "No fue posible cargar los movimientos."
            );

            if (
                error.status !== 401 &&
                error.status !== 403
            ) {
                mostrarMensaje(
                    error.message ||
                        "Ocurrió un error al consultar los movimientos.",
                    "error",
                    false
                );
            }

            throw error;
        } finally {
            cambiarEstadoBoton(
                refreshMovementsButton,
                false,
                "Actualizar lista"
            );
        }
    }

    function cargarSelectBodegas(
        select,
        textoInicial
    ) {
        if (!select) {
            return;
        }

        const valorAnterior = select.value;

        select.innerHTML = "";

        const opcionInicial =
            document.createElement("option");

        opcionInicial.value = "";
        opcionInicial.textContent = textoInicial;

        select.appendChild(opcionInicial);

        bodegas.forEach(function (bodega) {
            const id = obtenerIdBodega(bodega);

            if (id === null) {
                return;
            }

            const opcion =
                document.createElement("option");

            opcion.value = String(id);
            opcion.textContent =
                obtenerNombreBodega(bodega);

            select.appendChild(opcion);
        });

        const valorExiste = Array.from(
            select.options
        ).some(function (opcion) {
            return opcion.value === valorAnterior;
        });

        if (valorExiste) {
            select.value = valorAnterior;
        }
    }

    function cargarSelectProductos() {
        if (!productoSelect) {
            return;
        }

        const valorAnterior =
            productoSelect.value;

        productoSelect.innerHTML = "";

        const opcionInicial =
            document.createElement("option");

        opcionInicial.value = "";
        opcionInicial.textContent =
            productos.length > 0
                ? "Seleccione un producto"
                : "No hay productos disponibles";

        productoSelect.appendChild(opcionInicial);

        productos.forEach(function (producto) {
            const id = obtenerIdProducto(producto);

            if (id === null) {
                return;
            }

            const opcion =
                document.createElement("option");

            opcion.value = String(id);
            opcion.textContent =
                obtenerNombreProducto(producto);

            productoSelect.appendChild(opcion);
        });

        const valorExiste = Array.from(
            productoSelect.options
        ).some(function (opcion) {
            return opcion.value === valorAnterior;
        });

        if (valorExiste) {
            productoSelect.value = valorAnterior;
        }
    }

    function actualizarCamposPorTipo() {
        const tipo = tipoMovimientoSelect
            ? tipoMovimientoSelect.value
            : "";

        limpiarErrorCampo(
            bodegaOrigenSelect,
            bodegaOrigenError
        );

        limpiarErrorCampo(
            bodegaDestinoSelect,
            bodegaDestinoError
        );

        switch (tipo) {
            case "ENTRADA":
                configurarCampoBodega(
                    bodegaOrigenGroup,
                    bodegaOrigenSelect,
                    bodegaOrigenRequired,
                    false
                );

                configurarCampoBodega(
                    bodegaDestinoGroup,
                    bodegaDestinoSelect,
                    bodegaDestinoRequired,
                    true
                );

                bodegaOrigenSelect.value = "";

                actualizarAyudaMovimiento(
                    "La entrada aumenta el stock en la bodega de destino."
                );
                break;

            case "SALIDA":
                configurarCampoBodega(
                    bodegaOrigenGroup,
                    bodegaOrigenSelect,
                    bodegaOrigenRequired,
                    true
                );

                configurarCampoBodega(
                    bodegaDestinoGroup,
                    bodegaDestinoSelect,
                    bodegaDestinoRequired,
                    false
                );

                bodegaDestinoSelect.value = "";

                actualizarAyudaMovimiento(
                    "La salida disminuye el stock de la bodega de origen."
                );
                break;

            case "TRANSFERENCIA":
                configurarCampoBodega(
                    bodegaOrigenGroup,
                    bodegaOrigenSelect,
                    bodegaOrigenRequired,
                    true
                );

                configurarCampoBodega(
                    bodegaDestinoGroup,
                    bodegaDestinoSelect,
                    bodegaDestinoRequired,
                    true
                );

                actualizarAyudaMovimiento(
                    "La transferencia resta stock en la bodega de origen y lo suma en una bodega de destino diferente."
                );
                break;

            default:
                configurarCampoBodega(
                    bodegaOrigenGroup,
                    bodegaOrigenSelect,
                    bodegaOrigenRequired,
                    false
                );

                configurarCampoBodega(
                    bodegaDestinoGroup,
                    bodegaDestinoSelect,
                    bodegaDestinoRequired,
                    false
                );

                bodegaOrigenSelect.value = "";
                bodegaDestinoSelect.value = "";

                actualizarAyudaMovimiento(
                    "Seleccione el tipo de movimiento para conocer las bodegas requeridas."
                );
        }
    }

    function configurarCampoBodega(
        grupo,
        select,
        indicadorRequerido,
        requerido
    ) {
        if (grupo) {
            grupo.hidden = !requerido;
        }

        if (select) {
            select.required = requerido;
            select.disabled = !requerido;
        }

        if (indicadorRequerido) {
            indicadorRequerido.hidden = !requerido;
        }
    }

    function actualizarAyudaMovimiento(mensaje) {
        if (movementHelp) {
            movementHelp.textContent = mensaje;
        }
    }

    function agregarDetalle() {
        limpiarErroresDetalle();

        const productoId = obtenerNumeroEntero(
            productoSelect
                ? productoSelect.value
                : null
        );

        const cantidad = obtenerNumeroEntero(
            cantidadInput
                ? cantidadInput.value
                : null
        );

        let detalleValido = true;

        if (productoId === null) {
            mostrarErrorCampo(
                productoSelect,
                productoError,
                "Debe seleccionar un producto."
            );

            detalleValido = false;
        }

        if (cantidad === null || cantidad <= 0) {
            mostrarErrorCampo(
                cantidadInput,
                cantidadError,
                "La cantidad debe ser un número entero mayor que cero."
            );

            detalleValido = false;
        }

        if (!detalleValido) {
            enfocarPrimerCampoInvalido(
                "#productoId, #cantidad"
            );
            return;
        }

        const producto = productos.find(
            function (elemento) {
                return (
                    obtenerIdProducto(elemento) ===
                    productoId
                );
            }
        );

        if (!producto) {
            mostrarErrorCampo(
                productoSelect,
                productoError,
                "El producto seleccionado no está disponible."
            );
            return;
        }

        const detalleExistente =
            detallesMovimiento.find(
                function (detalle) {
                    return (
                        detalle.productoId ===
                        productoId
                    );
                }
            );

        if (detalleExistente) {
            detalleExistente.cantidad += cantidad;
        } else {
            detallesMovimiento.push({
                productoId: productoId,
                productoNombre:
                    obtenerNombreProducto(producto),
                productoCategoria:
                    obtenerTextoSeguro(
                        producto.categoria
                    ),
                cantidad: cantidad
            });
        }

        limpiarFormularioDetalle();
        limpiarErrorDetalles();
        renderizarDetallesMovimiento();
    }

    function renderizarDetallesMovimiento() {
        if (!movementDetailsTableBody) {
            return;
        }

        movementDetailsTableBody.innerHTML = "";

        if (detallesMovimiento.length === 0) {
            const fila = document.createElement("tr");
            const celda = document.createElement("td");

            celda.colSpan = 4;
            celda.className = "no-data";
            celda.textContent =
                "No se han agregado productos al movimiento.";

            fila.appendChild(celda);
            movementDetailsTableBody.appendChild(fila);

            actualizarContadorDetalles();
            return;
        }

        detallesMovimiento.forEach(
            function (detalle, indice) {
                const fila =
                    document.createElement("tr");

                fila.appendChild(
                    crearCelda(
                        detalle.productoNombre
                    )
                );

                fila.appendChild(
                    crearCelda(
                        detalle.productoCategoria ||
                            "Sin categoría"
                    )
                );

                const celdaCantidad =
                    document.createElement("td");

                const inputCantidad =
                    document.createElement("input");

                inputCantidad.type = "number";
                inputCantidad.min = "1";
                inputCantidad.step = "1";
                inputCantidad.value =
                    detalle.cantidad;

                inputCantidad.setAttribute(
                    "aria-label",
                    "Cantidad de " +
                        detalle.productoNombre
                );

                inputCantidad.addEventListener(
                    "change",
                    function () {
                        actualizarCantidadDetalle(
                            indice,
                            inputCantidad.value
                        );
                    }
                );

                celdaCantidad.appendChild(
                    inputCantidad
                );

                fila.appendChild(celdaCantidad);

                const celdaAcciones =
                    document.createElement("td");

                celdaAcciones.className =
                    "table-actions";

                const botonEliminar =
                    document.createElement("button");

                botonEliminar.type = "button";
                botonEliminar.className =
                    "danger-button";
                botonEliminar.textContent =
                    "Quitar";

                botonEliminar.addEventListener(
                    "click",
                    function () {
                        eliminarDetalle(indice);
                    }
                );

                celdaAcciones.appendChild(
                    botonEliminar
                );

                fila.appendChild(celdaAcciones);
                movementDetailsTableBody.appendChild(
                    fila
                );
            }
        );

        actualizarContadorDetalles();
    }

    function actualizarCantidadDetalle(
        indice,
        nuevoValor
    ) {
        const cantidad =
            obtenerNumeroEntero(nuevoValor);

        if (cantidad === null || cantidad <= 0) {
            mostrarMensaje(
                "La cantidad debe ser un número entero mayor que cero.",
                "error",
                false
            );

            renderizarDetallesMovimiento();
            return;
        }

        detallesMovimiento[indice].cantidad =
            cantidad;

        ocultarMensaje();
    }

    function eliminarDetalle(indice) {
        detallesMovimiento.splice(indice, 1);
        renderizarDetallesMovimiento();
    }

    function actualizarContadorDetalles() {
        if (!detailsCounter) {
            return;
        }

        const totalUnidades =
            detallesMovimiento.reduce(
                function (acumulado, detalle) {
                    return (
                        acumulado +
                        detalle.cantidad
                    );
                },
                0
            );

        detailsCounter.textContent =
            "Productos agregados: " +
            detallesMovimiento.length +
            " | Unidades totales: " +
            totalUnidades;
    }

    function limpiarFormularioDetalle() {
        if (productoSelect) {
            productoSelect.value = "";
        }

        if (cantidadInput) {
            cantidadInput.value = "";
        }

        limpiarErroresDetalle();

        if (productoSelect) {
            productoSelect.focus();
        }
    }

    async function registrarMovimiento(event) {
        event.preventDefault();

        if (enviandoMovimiento) {
            return;
        }

        limpiarErroresMovimiento();
        ocultarMensaje();

        if (!validarMovimiento()) {
            mostrarMensaje(
                "Revise los datos del movimiento antes de continuar.",
                "error",
                false
            );

            enfocarPrimerCampoInvalido(
                "#movimientoForm .input-error"
            );
            return;
        }

        const payload =
            construirMovimientoPayload();

        enviandoMovimiento = true;
        cambiarEstadoMovimiento(true);

        try {
            const response = await realizarPeticion(
                MOVIMIENTOS_ENDPOINT,
                {
                    method: "POST",
                    body: JSON.stringify(payload)
                }
            );

            await leerRespuesta(response);

            mostrarMensaje(
                "El movimiento fue registrado correctamente.",
                "success",
                true
            );

            limpiarMovimiento();
            await cargarMovimientos();
        } catch (error) {
            if (
                error.status === 401 ||
                error.status === 403
            ) {
                manejarSesionNoValida(
                    error.status
                );
                return;
            }

            mostrarMensaje(
                error.message ||
                    "No fue posible registrar el movimiento.",
                "error",
                false
            );

            console.error(
                "Error al registrar el movimiento:",
                error
            );
        } finally {
            enviandoMovimiento = false;
            cambiarEstadoMovimiento(false);
        }
    }

    function construirMovimientoPayload() {
        const tipo =
            tipoMovimientoSelect.value;

        const payload = {
            tipo: tipo,
            bodegaOrigenId: null,
            bodegaDestinoId: null,
            detalles: detallesMovimiento.map(
                function (detalle) {
                    return {
                        productoId:
                            detalle.productoId,
                        cantidad:
                            detalle.cantidad
                    };
                }
            )
        };

        if (
            tipo === "SALIDA" ||
            tipo === "TRANSFERENCIA"
        ) {
            payload.bodegaOrigenId =
                obtenerNumeroEntero(
                    bodegaOrigenSelect.value
                );
        }

        if (
            tipo === "ENTRADA" ||
            tipo === "TRANSFERENCIA"
        ) {
            payload.bodegaDestinoId =
                obtenerNumeroEntero(
                    bodegaDestinoSelect.value
                );
        }

        return payload;
    }

    function validarMovimiento() {
        let movimientoValido = true;

        const tipo = tipoMovimientoSelect
            ? tipoMovimientoSelect.value
            : "";

        const bodegaOrigenId =
            obtenerNumeroEntero(
                bodegaOrigenSelect
                    ? bodegaOrigenSelect.value
                    : null
            );

        const bodegaDestinoId =
            obtenerNumeroEntero(
                bodegaDestinoSelect
                    ? bodegaDestinoSelect.value
                    : null
            );

        if (!tipo) {
            mostrarErrorCampo(
                tipoMovimientoSelect,
                tipoMovimientoError,
                "Debe seleccionar un tipo de movimiento."
            );

            movimientoValido = false;
        }

        if (
            tipo === "SALIDA" ||
            tipo === "TRANSFERENCIA"
        ) {
            if (bodegaOrigenId === null) {
                mostrarErrorCampo(
                    bodegaOrigenSelect,
                    bodegaOrigenError,
                    "Debe seleccionar una bodega de origen."
                );

                movimientoValido = false;
            }
        }

        if (
            tipo === "ENTRADA" ||
            tipo === "TRANSFERENCIA"
        ) {
            if (bodegaDestinoId === null) {
                mostrarErrorCampo(
                    bodegaDestinoSelect,
                    bodegaDestinoError,
                    "Debe seleccionar una bodega de destino."
                );

                movimientoValido = false;
            }
        }

        if (
            tipo === "TRANSFERENCIA" &&
            bodegaOrigenId !== null &&
            bodegaDestinoId !== null &&
            bodegaOrigenId === bodegaDestinoId
        ) {
            mostrarErrorCampo(
                bodegaOrigenSelect,
                bodegaOrigenError,
                "La bodega de origen y destino deben ser diferentes."
            );

            mostrarErrorCampo(
                bodegaDestinoSelect,
                bodegaDestinoError,
                "La bodega de destino debe ser diferente de la bodega de origen."
            );

            movimientoValido = false;
        }

        if (detallesMovimiento.length === 0) {
            mostrarErrorDetalles(
                "Debe agregar al menos un producto al movimiento."
            );

            movimientoValido = false;
        }

        const detalleInvalido =
            detallesMovimiento.some(
                function (detalle) {
                    return (
                        obtenerNumeroEntero(
                            detalle.productoId
                        ) === null ||
                        !Number.isInteger(
                            detalle.cantidad
                        ) ||
                        detalle.cantidad <= 0
                    );
                }
            );

        if (detalleInvalido) {
            mostrarErrorDetalles(
                "Todos los productos deben tener una cantidad entera mayor que cero."
            );

            movimientoValido = false;
        }

        return movimientoValido;
    }

    function validarBodegasDiferentes() {
        const tipo = tipoMovimientoSelect
            ? tipoMovimientoSelect.value
            : "";

        if (tipo !== "TRANSFERENCIA") {
            return true;
        }

        const origenId = obtenerNumeroEntero(
            bodegaOrigenSelect.value
        );

        const destinoId = obtenerNumeroEntero(
            bodegaDestinoSelect.value
        );

        if (
            origenId !== null &&
            destinoId !== null &&
            origenId === destinoId
        ) {
            mostrarErrorCampo(
                bodegaDestinoSelect,
                bodegaDestinoError,
                "La bodega de destino debe ser diferente de la bodega de origen."
            );

            return false;
        }

        limpiarErrorCampo(
            bodegaDestinoSelect,
            bodegaDestinoError
        );

        return true;
    }

    function limpiarMovimiento() {
        if (movimientoForm) {
            movimientoForm.reset();
        }

        detallesMovimiento = [];

        establecerFechaActual();
        limpiarErroresMovimiento();
        limpiarErroresDetalle();
        actualizarCamposPorTipo();
        renderizarDetallesMovimiento();
        ocultarMensaje();

        if (tipoMovimientoSelect) {
            tipoMovimientoSelect.focus();
        }
    }

    function aplicarFiltros() {
        const tipo = typeFilterSelect
            ? typeFilterSelect.value
            : "";

        const bodegaId =
            obtenerNumeroEntero(
                warehouseFilterSelect
                    ? warehouseFilterSelect.value
                    : null
            );

        const fechaInicial =
            obtenerFechaFiltro(
                startDateFilterInput
                    ? startDateFilterInput.value
                    : ""
            );

        const fechaFinal =
            obtenerFechaFiltro(
                endDateFilterInput
                    ? endDateFilterInput.value
                    : ""
            );

        const productoBuscado =
            normalizarTexto(
                productSearchFilterInput
                    ? productSearchFilterInput.value
                    : ""
            );

        const responsableBuscado =
            normalizarTexto(
                responsibleSearchFilterInput
                    ? responsibleSearchFilterInput.value
                    : ""
            );

        if (
            fechaInicial &&
            fechaFinal &&
            fechaInicial > fechaFinal
        ) {
            mostrarMensaje(
                "La fecha inicial no puede ser posterior a la fecha final.",
                "warning",
                false
            );

            renderizarMovimientos([]);
            return;
        }

        const movimientosFiltrados =
            movimientos.filter(
                function (movimiento) {
                    const coincideTipo =
                        !tipo ||
                        obtenerTipoMovimiento(
                            movimiento
                        ) === tipo;

                    const coincideBodega =
                        bodegaId === null ||
                        obtenerIdBodegaOrigen(
                            movimiento
                        ) === bodegaId ||
                        obtenerIdBodegaDestino(
                            movimiento
                        ) === bodegaId;

                    const fechaMovimiento =
                        obtenerFechaMovimiento(
                            movimiento
                        );

                    const coincideFechaInicial =
                        !fechaInicial ||
                        (
                            fechaMovimiento &&
                            fechaMovimiento >=
                                fechaInicial
                        );

                    const coincideFechaFinal =
                        !fechaFinal ||
                        (
                            fechaMovimiento &&
                            fechaMovimiento <=
                                fechaFinal
                        );

                    const coincideProducto =
                        !productoBuscado ||
                        obtenerDetallesMovimiento(
                            movimiento
                        ).some(
                            function (detalle) {
                                return normalizarTexto(
                                    obtenerNombreProductoDetalle(
                                        detalle
                                    )
                                ).includes(
                                    productoBuscado
                                );
                            }
                        );

                    const responsable =
                        normalizarTexto(
                            obtenerNombreResponsable(
                                movimiento
                            )
                        );

                    const coincideResponsable =
                        !responsableBuscado ||
                        responsable.includes(
                            responsableBuscado
                        );

                    return (
                        coincideTipo &&
                        coincideBodega &&
                        coincideFechaInicial &&
                        coincideFechaFinal &&
                        coincideProducto &&
                        coincideResponsable
                    );
                }
            );

        renderizarMovimientos(
            movimientosFiltrados
        );
    }

    function renderizarMovimientos(lista) {
        if (!movementsTableBody) {
            return;
        }

        movementsTableBody.innerHTML = "";

        if (!Array.isArray(lista) || lista.length === 0) {
            mostrarFilaMovimientos(
                movimientos.length === 0
                    ? "No hay movimientos registrados."
                    : "No se encontraron movimientos con los filtros seleccionados."
            );

            actualizarContadorMovimientos(0);
            return;
        }

        lista.forEach(function (movimiento) {
            const fila = document.createElement("tr");

            fila.appendChild(
                crearCelda(
                    obtenerIdMovimiento(
                        movimiento
                    ) ?? ""
                )
            );

            fila.appendChild(
                crearCelda(
                    formatearFecha(
                        obtenerFechaMovimiento(
                            movimiento
                        )
                    )
                )
            );

            const celdaTipo =
                document.createElement("td");

            const badgeTipo =
                document.createElement("span");

            const tipo =
                obtenerTipoMovimiento(
                    movimiento
                );

            badgeTipo.className =
                obtenerClaseTipo(tipo);

            badgeTipo.textContent =
                formatearTipo(tipo);

            celdaTipo.appendChild(badgeTipo);
            fila.appendChild(celdaTipo);

            fila.appendChild(
                crearCelda(
                    obtenerNombreResponsable(
                        movimiento
                    )
                )
            );

            fila.appendChild(
                crearCelda(
                    obtenerNombreBodegaOrigen(
                        movimiento
                    )
                )
            );

            fila.appendChild(
                crearCelda(
                    obtenerNombreBodegaDestino(
                        movimiento
                    )
                )
            );

            const detalles =
                obtenerDetallesMovimiento(
                    movimiento
                );

            fila.appendChild(
                crearCelda(
                    resumirDetalles(detalles)
                )
            );

            const celdaAcciones =
                document.createElement("td");

            celdaAcciones.className =
                "table-actions";

            const botonDetalle =
                document.createElement("button");

            botonDetalle.type = "button";
            botonDetalle.className =
                "action-button";
            botonDetalle.textContent =
                "Ver detalle";

            botonDetalle.addEventListener(
                "click",
                function () {
                    abrirModalDetalle(
                        movimiento
                    );
                }
            );

            celdaAcciones.appendChild(
                botonDetalle
            );

            fila.appendChild(celdaAcciones);
            movementsTableBody.appendChild(fila);
        });

        actualizarContadorMovimientos(
            lista.length
        );
    }

    function mostrarFilaMovimientos(mensaje) {
        if (!movementsTableBody) {
            return;
        }

        movementsTableBody.innerHTML = "";

        const fila = document.createElement("tr");
        const celda = document.createElement("td");

        celda.colSpan = 8;
        celda.className = "no-data";
        celda.textContent = mensaje;

        fila.appendChild(celda);
        movementsTableBody.appendChild(fila);
    }

    function actualizarContadorMovimientos(
        cantidadVisible
    ) {
        if (!movementsCounter) {
            return;
        }

        movementsCounter.textContent =
            "Movimientos mostrados: " +
            cantidadVisible +
            " de " +
            movimientos.length;
    }

    function actualizarResumen(lista) {
        const entradas = lista.filter(
            function (movimiento) {
                return (
                    obtenerTipoMovimiento(
                        movimiento
                    ) === "ENTRADA"
                );
            }
        ).length;

        const salidas = lista.filter(
            function (movimiento) {
                return (
                    obtenerTipoMovimiento(
                        movimiento
                    ) === "SALIDA"
                );
            }
        ).length;

        const transferencias = lista.filter(
            function (movimiento) {
                return (
                    obtenerTipoMovimiento(
                        movimiento
                    ) === "TRANSFERENCIA"
                );
            }
        ).length;

        if (summaryEntries) {
            summaryEntries.textContent =
                String(entradas);
        }

        if (summaryOutputs) {
            summaryOutputs.textContent =
                String(salidas);
        }

        if (summaryTransfers) {
            summaryTransfers.textContent =
                String(transferencias);
        }
    }

    function abrirModalDetalle(movimiento) {
        if (!movementDetailModal) {
            return;
        }

        if (detailMovementId) {
            detailMovementId.textContent =
                obtenerTextoSeguro(
                    obtenerIdMovimiento(
                        movimiento
                    ) ?? "--"
                );
        }

        if (detailMovementDate) {
            detailMovementDate.textContent =
                formatearFecha(
                    obtenerFechaMovimiento(
                        movimiento
                    )
                );
        }

        if (detailMovementType) {
            detailMovementType.textContent =
                formatearTipo(
                    obtenerTipoMovimiento(
                        movimiento
                    )
                );
        }

        if (detailMovementResponsible) {
            detailMovementResponsible.textContent =
                obtenerNombreResponsable(
                    movimiento
                );
        }

        if (detailMovementOrigin) {
            detailMovementOrigin.textContent =
                obtenerNombreBodegaOrigen(
                    movimiento
                );
        }

        if (detailMovementDestination) {
            detailMovementDestination.textContent =
                obtenerNombreBodegaDestino(
                    movimiento
                );
        }

        renderizarDetallesModal(
            obtenerDetallesMovimiento(
                movimiento
            )
        );

        movementDetailModal.hidden = false;
        document.body.style.overflow = "hidden";

        if (closeMovementDetailButton) {
            closeMovementDetailButton.focus();
        }
    }

    function renderizarDetallesModal(detalles) {
        if (!movementDetailModalTableBody) {
            return;
        }

        movementDetailModalTableBody.innerHTML = "";

        if (!detalles || detalles.length === 0) {
            const fila = document.createElement("tr");
            const celda = document.createElement("td");

            celda.colSpan = 2;
            celda.className = "no-data";
            celda.textContent =
                "Sin productos para mostrar.";

            fila.appendChild(celda);
            movementDetailModalTableBody.appendChild(
                fila
            );
            return;
        }

        detalles.forEach(function (detalle) {
            const fila = document.createElement("tr");

            fila.appendChild(
                crearCelda(
                    obtenerNombreProductoDetalle(
                        detalle
                    )
                )
            );

            fila.appendChild(
                crearCelda(
                    obtenerCantidadDetalle(
                        detalle
                    )
                )
            );

            movementDetailModalTableBody.appendChild(
                fila
            );
        });
    }

    function cerrarModalDetalle() {
        if (!movementDetailModal) {
            return;
        }

        movementDetailModal.hidden = true;
        document.body.style.overflow = "";
    }

    function resumirDetalles(detalles) {
        if (!detalles || detalles.length === 0) {
            return "Sin productos";
        }

        const totalUnidades = detalles.reduce(
            function (acumulado, detalle) {
                return (
                    acumulado +
                    obtenerCantidadDetalle(
                        detalle
                    )
                );
            },
            0
        );

        return (
            detalles.length +
            (
                detalles.length === 1
                    ? " producto"
                    : " productos"
            ) +
            " / " +
            totalUnidades +
            " unidades"
        );
    }

    function obtenerDetallesMovimiento(
        movimiento
    ) {
        if (
            !movimiento ||
            typeof movimiento !== "object"
        ) {
            return [];
        }

        if (Array.isArray(movimiento.detalles)) {
            return movimiento.detalles;
        }

        if (
            Array.isArray(
                movimiento.detallesMovimiento
            )
        ) {
            return movimiento.detallesMovimiento;
        }

        if (Array.isArray(movimiento.productos)) {
            return movimiento.productos;
        }

        return [];
    }

    function obtenerNombreProductoDetalle(
        detalle
    ) {
        if (!detalle || typeof detalle !== "object") {
            return "Producto";
        }

        if (detalle.productoNombre) {
            return String(detalle.productoNombre);
        }

        if (detalle.nombreProducto) {
            return String(detalle.nombreProducto);
        }

        if (
            detalle.producto &&
            typeof detalle.producto === "object"
        ) {
            return obtenerNombreProducto(
                detalle.producto
            );
        }

        const producto = productos.find(
            function (elemento) {
                return (
                    obtenerIdProducto(elemento) ===
                    obtenerNumeroEntero(
                        detalle.productoId
                    )
                );
            }
        );

        return producto
            ? obtenerNombreProducto(producto)
            : "Producto";
    }

    function obtenerCantidadDetalle(detalle) {
        const cantidad = Number(
            detalle && detalle.cantidad
        );

        return Number.isFinite(cantidad)
            ? cantidad
            : 0;
    }

    function obtenerIdMovimiento(movimiento) {
        if (
            !movimiento ||
            typeof movimiento !== "object"
        ) {
            return null;
        }

        return obtenerNumeroEntero(
            movimiento.id ??
                movimiento.movimientoId
        );
    }

    function obtenerTipoMovimiento(movimiento) {
        if (
            !movimiento ||
            typeof movimiento !== "object"
        ) {
            return "";
        }

        return obtenerTextoSeguro(
            movimiento.tipo ??
                movimiento.tipoMovimiento
        ).toUpperCase();
    }

    function obtenerFechaMovimiento(movimiento) {
        if (
            !movimiento ||
            typeof movimiento !== "object"
        ) {
            return null;
        }

        const valor =
            movimiento.fecha ??
            movimiento.fechaHora ??
            movimiento.fechaMovimiento;

        if (!valor) {
            return null;
        }

        const fecha = new Date(valor);

        return Number.isNaN(fecha.getTime())
            ? null
            : fecha;
    }

    function obtenerNombreResponsable(
        movimiento
    ) {
        if (
            !movimiento ||
            typeof movimiento !== "object"
        ) {
            return "Sin responsable";
        }

        if (movimiento.usuarioResponsableNombre) {
            return String(
                movimiento.usuarioResponsableNombre
            );
        }

        if (movimiento.nombreResponsable) {
            return String(
                movimiento.nombreResponsable
            );
        }

        if (movimiento.usernameResponsable) {
            return String(
                movimiento.usernameResponsable
            );
        }

        const usuario =
            movimiento.usuarioResponsable ??
            movimiento.usuario;

        if (
            usuario &&
            typeof usuario === "object"
        ) {
            if (usuario.nombre && usuario.username) {
                return (
                    usuario.nombre +
                    " (" +
                    usuario.username +
                    ")"
                );
            }

            return obtenerTextoSeguro(
                usuario.nombre ??
                    usuario.username
            ) || "Sin responsable";
        }

        return obtenerTextoSeguro(usuario) ||
            "Sin responsable";
    }

    function obtenerIdBodegaOrigen(
        movimiento
    ) {
        return obtenerIdBodegaRelacionada(
            movimiento,
            "origen"
        );
    }

    function obtenerIdBodegaDestino(
        movimiento
    ) {
        return obtenerIdBodegaRelacionada(
            movimiento,
            "destino"
        );
    }

    function obtenerIdBodegaRelacionada(
        movimiento,
        tipo
    ) {
        if (
            !movimiento ||
            typeof movimiento !== "object"
        ) {
            return null;
        }

        const esOrigen = tipo === "origen";

        const idDirecto = esOrigen
            ? movimiento.bodegaOrigenId
            : movimiento.bodegaDestinoId;

        const id = obtenerNumeroEntero(
            idDirecto
        );

        if (id !== null) {
            return id;
        }

        const bodega = esOrigen
            ? movimiento.bodegaOrigen
            : movimiento.bodegaDestino;

        return obtenerIdBodega(bodega);
    }

    function obtenerNombreBodegaOrigen(
        movimiento
    ) {
        return obtenerNombreBodegaRelacionada(
            movimiento,
            "origen"
        );
    }

    function obtenerNombreBodegaDestino(
        movimiento
    ) {
        return obtenerNombreBodegaRelacionada(
            movimiento,
            "destino"
        );
    }

    function obtenerNombreBodegaRelacionada(
        movimiento,
        tipo
    ) {
        if (
            !movimiento ||
            typeof movimiento !== "object"
        ) {
            return "No aplica";
        }

        const esOrigen = tipo === "origen";

        const nombreDirecto = esOrigen
            ? (
                movimiento.bodegaOrigenNombre ??
                movimiento.nombreBodegaOrigen
            )
            : (
                movimiento.bodegaDestinoNombre ??
                movimiento.nombreBodegaDestino
            );

        if (nombreDirecto) {
            return String(nombreDirecto);
        }

        const bodega = esOrigen
            ? movimiento.bodegaOrigen
            : movimiento.bodegaDestino;

        if (
            bodega &&
            typeof bodega === "object"
        ) {
            return obtenerNombreBodega(bodega);
        }

        const id = esOrigen
            ? obtenerIdBodegaOrigen(movimiento)
            : obtenerIdBodegaDestino(movimiento);

        if (id !== null) {
            const bodegaEncontrada =
                bodegas.find(function (elemento) {
                    return (
                        obtenerIdBodega(elemento) === id
                    );
                });

            if (bodegaEncontrada) {
                return obtenerNombreBodega(
                    bodegaEncontrada
                );
            }
        }

        return "No aplica";
    }

    function obtenerIdBodega(bodega) {
        if (!bodega || typeof bodega !== "object") {
            return null;
        }

        return obtenerNumeroEntero(
            bodega.id ?? bodega.bodegaId
        );
    }

    function obtenerNombreBodega(bodega) {
        if (!bodega || typeof bodega !== "object") {
            return "Bodega";
        }

        const nombre =
            obtenerTextoSeguro(bodega.nombre);

        const ubicacion =
            obtenerTextoSeguro(bodega.ubicacion);

        if (nombre && ubicacion) {
            return nombre + " - " + ubicacion;
        }

        return nombre ||
            ubicacion ||
            (
                "Bodega " +
                (
                    obtenerIdBodega(bodega) ??
                    ""
                )
            );
    }

    function obtenerIdProducto(producto) {
        if (
            !producto ||
            typeof producto !== "object"
        ) {
            return null;
        }

        return obtenerNumeroEntero(
            producto.id ?? producto.productoId
        );
    }

    function obtenerNombreProducto(producto) {
        if (
            !producto ||
            typeof producto !== "object"
        ) {
            return "Producto";
        }

        const nombre =
            obtenerTextoSeguro(producto.nombre);

        const categoria =
            obtenerTextoSeguro(producto.categoria);

        if (nombre && categoria) {
            return nombre + " - " + categoria;
        }

        return nombre ||
            (
                "Producto " +
                (
                    obtenerIdProducto(producto) ??
                    ""
                )
            );
    }

    function obtenerClaseTipo(tipo) {
        switch (tipo) {
            case "ENTRADA":
                return "badge badge-success";

            case "SALIDA":
                return "badge badge-danger";

            case "TRANSFERENCIA":
                return "badge badge-info";

            default:
                return "badge";
        }
    }

    function formatearTipo(tipo) {
        switch (tipo) {
            case "ENTRADA":
                return "Entrada";

            case "SALIDA":
                return "Salida";

            case "TRANSFERENCIA":
                return "Transferencia";

            default:
                return tipo || "Sin tipo";
        }
    }

    function establecerFechaActual() {
        if (!fechaMovimientoInput) {
            return;
        }

        const fecha = new Date();

        const fechaLocal = new Date(
            fecha.getTime() -
                fecha.getTimezoneOffset() *
                60000
        );

        fechaMovimientoInput.value =
            fechaLocal
                .toISOString()
                .slice(0, 16);
    }

    function obtenerFechaFiltro(valor) {
        if (!valor) {
            return null;
        }

        const fecha = new Date(valor);

        return Number.isNaN(fecha.getTime())
            ? null
            : fecha;
    }

    function formatearFecha(fecha) {
        if (!(fecha instanceof Date)) {
            return "Sin fecha";
        }

        return new Intl.DateTimeFormat(
            "es-CO",
            {
                year: "numeric",
                month: "2-digit",
                day: "2-digit",
                hour: "2-digit",
                minute: "2-digit"
            }
        ).format(fecha);
    }

    function crearCelda(valor) {
        const celda = document.createElement("td");

        celda.textContent =
            obtenerTextoSeguro(valor);

        return celda;
    }

    async function realizarPeticion(
        url,
        opciones
    ) {
        const token =
            localStorage.getItem("token");

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
                "Authorization":
                    "Bearer " + token,
                ...(
                    opciones &&
                    opciones.body
                        ? {
                            "Content-Type":
                                "application/json"
                        }
                        : {}
                ),
                ...(
                    opciones &&
                    opciones.headers
                        ? opciones.headers
                        : {}
                )
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
            const cuerpoError =
                await leerCuerpoRespuesta(
                    response
                );

            throw crearErrorPeticion(
                obtenerMensajeError(
                    response.status,
                    cuerpoError
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

    async function leerCuerpoRespuesta(
        response
    ) {
        const contentType =
            response.headers.get(
                "content-type"
            ) || "";

        try {
            if (
                contentType.includes(
                    "application/json"
                )
            ) {
                return await response.json();
            }

            const texto =
                await response.text();

            return texto
                ? { message: texto }
                : null;
        } catch (error) {
            return null;
        }
    }

    function obtenerMensajeError(
        status,
        datos
    ) {
        const mensajeServidor =
            extraerMensajeServidor(datos);

        if (mensajeServidor) {
            return mensajeServidor;
        }

        switch (status) {
            case 400:
                return "Los datos del movimiento no son válidos.";

            case 401:
                return "La sesión no es válida o el token ha vencido.";

            case 403:
                return "No tiene permisos para realizar esta operación.";

            case 404:
                return "No se encontró una bodega, producto o movimiento solicitado.";

            case 409:
                return "El movimiento genera un conflicto con el inventario actual.";

            case 422:
                return "El movimiento no cumple las reglas del inventario.";

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
            const errores =
                Array.isArray(datos.errors)
                    ? datos.errors
                    : Object.values(
                        datos.errors
                    );

            if (errores.length > 0) {
                const primerError =
                    errores[0];

                if (
                    typeof primerError ===
                    "string"
                ) {
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

    function normalizarLista(
        datos,
        nombrePropiedad
    ) {
        if (Array.isArray(datos)) {
            return datos;
        }

        if (
            !datos ||
            typeof datos !== "object"
        ) {
            return [];
        }

        const posiblesListas = [
            datos.content,
            datos.data,
            datos.resultado,
            datos[nombrePropiedad]
        ];

        return posiblesListas.find(
            Array.isArray
        ) || [];
    }

    function crearErrorPeticion(
        mensaje,
        status
    ) {
        const error = new Error(mensaje);
        error.status = status;
        return error;
    }

    function manejarErrorAutenticacion(
        error
    ) {
        if (
            error.status === 401 ||
            error.status === 403
        ) {
            manejarSesionNoValida(
                error.status
            );
        }
    }

    function mostrarErrorCampo(
        campo,
        elementoError,
        mensaje
    ) {
        if (campo) {
            campo.classList.add(
                "input-error"
            );

            campo.setAttribute(
                "aria-invalid",
                "true"
            );
        }

        if (elementoError) {
            elementoError.textContent =
                mensaje;
        }
    }

    function limpiarErrorCampo(
        campo,
        elementoError
    ) {
        if (campo) {
            campo.classList.remove(
                "input-error"
            );

            campo.removeAttribute(
                "aria-invalid"
            );
        }

        if (elementoError) {
            elementoError.textContent = "";
        }
    }

    function mostrarErrorDetalles(mensaje) {
        if (detallesError) {
            detallesError.textContent = mensaje;
        }
    }

    function limpiarErrorDetalles() {
        if (detallesError) {
            detallesError.textContent = "";
        }
    }

    function limpiarErroresDetalle() {
        limpiarErrorCampo(
            productoSelect,
            productoError
        );

        limpiarErrorCampo(
            cantidadInput,
            cantidadError
        );
    }

    function limpiarErroresMovimiento() {
        limpiarErrorCampo(
            tipoMovimientoSelect,
            tipoMovimientoError
        );

        limpiarErrorCampo(
            bodegaOrigenSelect,
            bodegaOrigenError
        );

        limpiarErrorCampo(
            bodegaDestinoSelect,
            bodegaDestinoError
        );

        limpiarErrorDetalles();
    }

    function enfocarPrimerCampoInvalido(
        selector
    ) {
        const campo = document.querySelector(
            selector
        );

        if (campo) {
            campo.focus();
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
                    pageMessage.textContent ===
                    mensaje
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

    function cambiarEstadoMovimiento(
        cargando
    ) {
        const controles = [
            tipoMovimientoSelect,
            bodegaOrigenSelect,
            bodegaDestinoSelect,
            productoSelect,
            cantidadInput,
            addDetailButton,
            clearDetailButton,
            clearMovementButton,
            cancelMovementButton
        ];

        controles.forEach(function (control) {
            if (control) {
                control.disabled = cargando;
            }
        });

        cambiarEstadoBoton(
            saveMovementButton,
            cargando,
            cargando
                ? "Registrando..."
                : "Registrar movimiento"
        );

        if (!cargando) {
            actualizarCamposPorTipo();
        }
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

    function mostrarUsuarioActual() {
        if (!currentUsername) {
            return;
        }

        const username =
            localStorage.getItem("username");

        currentUsername.textContent =
            username ||
            "Usuario autenticado";
    }

    function verificarAutenticacion() {
        const token =
            localStorage.getItem("token");

        if (!token) {
            limpiarSesion();
            window.location.replace(
                LOGIN_PAGE
            );
            return false;
        }

        if (tokenExpirado(token)) {
            limpiarSesion();
            window.location.replace(
                LOGIN_PAGE
            );
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
        const payload =
            decodificarPayloadJwt(token);

        if (!payload.exp) {
            return false;
        }

        const fechaActual =
            Math.floor(Date.now() / 1000);

        return (
            payload.exp <= fechaActual
        );
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
                            4 -
                                (
                                    base64.length %
                                    4
                                )
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
            .replace(
                /[\u0300-\u036f]/g,
                ""
            )
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
});