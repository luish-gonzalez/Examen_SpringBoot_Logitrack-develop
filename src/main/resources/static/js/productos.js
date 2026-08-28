"use strict";

/*
 * LogiTrack - Gestión de productos
 * Ruta: src/main/resources/static/js/productos.js
 */

document.addEventListener("DOMContentLoaded", function () {
    /*
     * ASUNCIÓN TÉCNICA:
     * ProductoController utiliza el prefijo /api y expone su CRUD en
     * /api/productos. Si el controlador generado anteriormente utiliza
     * /productos, solo debe cambiarse esta constante.
     */
    const PRODUCTOS_ENDPOINT = "/api/productos";
    const LOGIN_PAGE = "login.html";

    let productos = [];
    let inventarios = [];
    let enviandoFormulario = false;
    let eliminandoProducto = false;

    const productoForm = document.getElementById("productoForm");
    const productoIdInput = document.getElementById("productoId");
    const nombreInput = document.getElementById("nombre");
    const categoriaInput = document.getElementById("categoria");
    const precioInput = document.getElementById("precio");
    const precioFormateadoInput =
        document.getElementById("precioFormateado");

    const nombreError = document.getElementById("nombreError");
    const categoriaError = document.getElementById("categoriaError");
    const precioError = document.getElementById("precioError");

    const formTitle = document.getElementById("formTitle");
    const saveProductoButton =
        document.getElementById("saveProductoButton");
    const cancelEditButton =
        document.getElementById("cancelEditButton");
    const clearFormButton =
        document.getElementById("clearFormButton");
    const newProductoButton =
        document.getElementById("newProductoButton");

    const refreshProductosButton =
        document.getElementById("refreshProductosButton");
    const searchProductoInput =
        document.getElementById("searchProducto");
    const categoryFilterSelect =
        document.getElementById("categoryFilter");
    const minimumPriceFilterInput =
        document.getElementById("minimumPriceFilter");
    const maximumPriceFilterInput =
        document.getElementById("maximumPriceFilter");

    const productosTableBody =
        document.getElementById("productosTableBody");
    const productosCounter =
        document.getElementById("productosCounter");

    const summaryTotalProductos =
        document.getElementById("summaryTotalProductos");
    const summaryTotalCategorias =
        document.getElementById("summaryTotalCategorias");
    const summaryAveragePrice =
        document.getElementById("summaryAveragePrice");

    const pageMessage = document.getElementById("pageMessage");
    const logoutButton = document.getElementById("logoutButton");

    const deleteModal = document.getElementById("deleteModal");
    const deleteProductoIdInput =
        document.getElementById("deleteProductoId");
    const deleteProductoName =
        document.getElementById("deleteProductoName");
    const closeDeleteModalButton =
        document.getElementById("closeDeleteModalButton");
    const cancelDeleteButton =
        document.getElementById("cancelDeleteButton");
    const confirmDeleteButton =
        document.getElementById("confirmDeleteButton");

    inicializarPagina();

    async function inicializarPagina() {
        if (!verificarAutenticacion()) {
            return;
        }

        configurarEventos();
        actualizarVistaPreviaPrecio();
        await cargarProductos();
    }

    function configurarEventos() {
        if (productoForm) {
            productoForm.addEventListener(
                "submit",
                guardarProducto
            );

            productoForm.addEventListener(
                "reset",
                function () {
                    window.setTimeout(function () {
                        limpiarErroresFormulario();
                        limpiarModoEdicion();
                        actualizarVistaPreviaPrecio();
                    }, 0);
                }
            );
        }

        if (newProductoButton) {
            newProductoButton.addEventListener(
                "click",
                prepararNuevoProducto
            );
        }

        if (cancelEditButton) {
            cancelEditButton.addEventListener(
                "click",
                prepararNuevoProducto
            );
        }

        if (clearFormButton) {
            clearFormButton.addEventListener(
                "click",
                limpiarErroresFormulario
            );
        }

        if (refreshProductosButton) {
            refreshProductosButton.addEventListener(
                "click",
                cargarProductos
            );
        }

        if (nombreInput) {
            nombreInput.addEventListener(
                "input",
                function () {
                    limpiarErrorCampo(nombreInput, nombreError);
                }
            );
        }

        if (categoriaInput) {
            categoriaInput.addEventListener(
                "input",
                function () {
                    limpiarErrorCampo(
                        categoriaInput,
                        categoriaError
                    );
                }
            );
        }

        if (precioInput) {
            precioInput.addEventListener(
                "input",
                function () {
                    limpiarErrorCampo(precioInput, precioError);
                    actualizarVistaPreviaPrecio();
                }
            );
        }

        if (searchProductoInput) {
            searchProductoInput.addEventListener(
                "input",
                aplicarFiltros
            );
        }

        if (categoryFilterSelect) {
            categoryFilterSelect.addEventListener(
                "change",
                aplicarFiltros
            );
        }

        if (minimumPriceFilterInput) {
            minimumPriceFilterInput.addEventListener(
                "input",
                aplicarFiltros
            );
        }

        if (maximumPriceFilterInput) {
            maximumPriceFilterInput.addEventListener(
                "input",
                aplicarFiltros
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
                eliminarProductoConfirmado
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
    }

    async function cargarProductos() {
        cambiarEstadoBoton(
            refreshProductosButton,
            true,
            "Cargando..."
        );

        mostrarFilaInformativa("Cargando productos...");

        try {
            const response = await realizarPeticion(
                PRODUCTOS_ENDPOINT,
                {
                    method: "GET"
                }
            );

            const datos = await leerRespuesta(response);
            productos = normalizarLista(datos);

            // Cargar inventarios para mostrar stock por producto
            try {
                const invResp = await realizarPeticion('/api/inventarios', { method: 'GET' });
                const invData = await leerRespuesta(invResp);
                inventarios = normalizarLista(invData);
            } catch (invErr) {
                inventarios = [];
                console.warn('No se pudo cargar inventarios:', invErr);
            }

            actualizarFiltroCategorias(productos);
            aplicarFiltros();
            actualizarResumen(productos);
            ocultarMensaje();
        } catch (error) {
            productos = [];
            actualizarFiltroCategorias([]);
            renderizarProductos([]);
            actualizarResumen([]);

            if (error.status === 401 || error.status === 403) {
                manejarSesionNoValida(error.status);
                return;
            }

            mostrarFilaInformativa(
                "No fue posible cargar los productos."
            );

            mostrarMensaje(
                error.message ||
                    "Ocurrió un error al consultar los productos.",
                "error",
                false
            );

            console.error(
                "Error al consultar los productos:",
                error
            );
        } finally {
            cambiarEstadoBoton(
                refreshProductosButton,
                false,
                "Actualizar lista"
            );
        }
    }

    async function guardarProducto(event) {
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
            productoIdInput ? productoIdInput.value : null
        );

        const editando = id !== null;
        const endpoint = editando
            ? PRODUCTOS_ENDPOINT + "/" + id
            : PRODUCTOS_ENDPOINT;

        const metodo = editando ? "PUT" : "POST";
        const producto = construirProductoPayload();

        enviandoFormulario = true;
        cambiarEstadoFormulario(true);

        try {
            const response = await realizarPeticion(
                endpoint,
                {
                    method: metodo,
                    body: JSON.stringify(producto)
                }
            );

            await leerRespuesta(response);

            mostrarMensaje(
                editando
                    ? "El producto fue actualizado correctamente."
                    : "El producto fue registrado correctamente.",
                "success",
                true
            );

            prepararNuevoProducto();
            await cargarProductos();
        } catch (error) {
            if (error.status === 401 || error.status === 403) {
                manejarSesionNoValida(error.status);
                return;
            }

            mostrarMensaje(
                error.message ||
                    "No fue posible guardar el producto.",
                "error",
                false
            );

            console.error(
                "Error al guardar el producto:",
                error
            );
        } finally {
            enviandoFormulario = false;
            cambiarEstadoFormulario(false);
        }
    }

    function construirProductoPayload() {
        return {
            nombre: nombreInput.value.trim(),
            categoria: categoriaInput.value.trim(),
            precio: Number(precioInput.value)
        };
    }

    function validarFormulario() {
        let formularioValido = true;

        const nombre = nombreInput
            ? nombreInput.value.trim()
            : "";

        const categoria = categoriaInput
            ? categoriaInput.value.trim()
            : "";

        const precioTexto = precioInput
            ? precioInput.value.trim()
            : "";

        const precio = Number(precioTexto);

        if (!nombre) {
            mostrarErrorCampo(
                nombreInput,
                nombreError,
                "El nombre del producto es obligatorio."
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

        if (!categoria) {
            mostrarErrorCampo(
                categoriaInput,
                categoriaError,
                "La categoría es obligatoria."
            );

            formularioValido = false;
        } else if (categoria.length < 2) {
            mostrarErrorCampo(
                categoriaInput,
                categoriaError,
                "La categoría debe tener al menos 2 caracteres."
            );

            formularioValido = false;
        } else if (categoria.length > 100) {
            mostrarErrorCampo(
                categoriaInput,
                categoriaError,
                "La categoría no puede superar 100 caracteres."
            );

            formularioValido = false;
        }

        if (!precioTexto) {
            mostrarErrorCampo(
                precioInput,
                precioError,
                "El precio es obligatorio."
            );

            formularioValido = false;
        } else if (
            !Number.isFinite(precio) ||
            precio <= 0
        ) {
            mostrarErrorCampo(
                precioInput,
                precioError,
                "El precio debe ser un número mayor que cero."
            );

            formularioValido = false;
        }

        return formularioValido;
    }

    function editarProducto(producto) {
        const id = obtenerIdProducto(producto);

        if (id === null) {
            mostrarMensaje(
                "No fue posible identificar el producto seleccionado.",
                "error",
                false
            );

            return;
        }

        productoIdInput.value = id;
        nombreInput.value = obtenerTextoSeguro(producto.nombre);
        categoriaInput.value = obtenerTextoSeguro(
            producto.categoria
        );
        precioInput.value = obtenerPrecio(producto);

        if (formTitle) {
            formTitle.textContent = "Editar producto";
        }

        if (saveProductoButton) {
            saveProductoButton.textContent =
                "Actualizar producto";
        }

        if (cancelEditButton) {
            cancelEditButton.hidden = false;
        }

        limpiarErroresFormulario();
        ocultarMensaje();
        actualizarVistaPreviaPrecio();

        if (nombreInput) {
            nombreInput.focus();
        }

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    }

    function prepararNuevoProducto() {
        if (productoForm) {
            productoForm.reset();
        }

        limpiarModoEdicion();
        limpiarErroresFormulario();
        actualizarVistaPreviaPrecio();

        if (nombreInput) {
            nombreInput.focus();
        }
    }

    function limpiarModoEdicion() {
        if (productoIdInput) {
            productoIdInput.value = "";
        }

        if (formTitle) {
            formTitle.textContent = "Registrar producto";
        }

        if (saveProductoButton) {
            saveProductoButton.textContent =
                "Guardar producto";
        }

        if (cancelEditButton) {
            cancelEditButton.hidden = true;
        }
    }

    function solicitarEliminacion(producto) {
        const id = obtenerIdProducto(producto);

        if (id === null) {
            mostrarMensaje(
                "No fue posible identificar el producto seleccionado.",
                "error",
                false
            );

            return;
        }

        if (deleteProductoIdInput) {
            deleteProductoIdInput.value = id;
        }

        if (deleteProductoName) {
            deleteProductoName.textContent =
                obtenerTextoSeguro(producto.nombre) ||
                "seleccionado";
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
        if (!deleteModal || eliminandoProducto) {
            return;
        }

        deleteModal.hidden = true;
        document.body.style.overflow = "";

        if (deleteProductoIdInput) {
            deleteProductoIdInput.value = "";
        }

        if (deleteProductoName) {
            deleteProductoName.textContent = "";
        }
    }

    async function eliminarProductoConfirmado() {
        if (eliminandoProducto) {
            return;
        }

        const id = obtenerNumeroEntero(
            deleteProductoIdInput
                ? deleteProductoIdInput.value
                : null
        );

        if (id === null) {
            cerrarModalEliminacion();

            mostrarMensaje(
                "No fue posible identificar el producto que desea eliminar.",
                "error",
                false
            );

            return;
        }

        eliminandoProducto = true;

        cambiarEstadoBoton(
            confirmDeleteButton,
            true,
            "Eliminando..."
        );

        try {
            const response = await realizarPeticion(
                PRODUCTOS_ENDPOINT + "/" + id,
                {
                    method: "DELETE"
                }
            );

            await leerRespuesta(response);

            if (deleteModal) {
                deleteModal.hidden = true;
            }

            document.body.style.overflow = "";

            mostrarMensaje(
                "El producto fue eliminado correctamente.",
                "success",
                true
            );

            if (
                obtenerNumeroEntero(productoIdInput.value) === id
            ) {
                prepararNuevoProducto();
            }

            await cargarProductos();
        } catch (error) {
            if (error.status === 401 || error.status === 403) {
                manejarSesionNoValida(error.status);
                return;
            }

            mostrarMensaje(
                error.message ||
                    "No fue posible eliminar el producto. Puede estar relacionado con inventarios o movimientos.",
                "error",
                false
            );

            console.error(
                "Error al eliminar el producto:",
                error
            );
        } finally {
            eliminandoProducto = false;

            cambiarEstadoBoton(
                confirmDeleteButton,
                false,
                "Eliminar"
            );
        }
    }

    function aplicarFiltros() {
        const textoBusqueda = normalizarTexto(
            searchProductoInput
                ? searchProductoInput.value
                : ""
        );

        const categoriaSeleccionada = normalizarTexto(
            categoryFilterSelect
                ? categoryFilterSelect.value
                : ""
        );

        const precioMinimo = obtenerNumeroDecimalOpcional(
            minimumPriceFilterInput
                ? minimumPriceFilterInput.value
                : ""
        );

        const precioMaximo = obtenerNumeroDecimalOpcional(
            maximumPriceFilterInput
                ? maximumPriceFilterInput.value
                : ""
        );

        const productosFiltrados = productos.filter(
            function (producto) {
                const nombre = normalizarTexto(producto.nombre);
                const categoria = normalizarTexto(
                    producto.categoria
                );
                const precio = obtenerPrecio(producto);

                const coincideTexto =
                    !textoBusqueda ||
                    nombre.includes(textoBusqueda) ||
                    categoria.includes(textoBusqueda);

                const coincideCategoria =
                    !categoriaSeleccionada ||
                    categoria === categoriaSeleccionada;

                const coincidePrecioMinimo =
                    precioMinimo === null ||
                    precio >= precioMinimo;

                const coincidePrecioMaximo =
                    precioMaximo === null ||
                    precio <= precioMaximo;

                return (
                    coincideTexto &&
                    coincideCategoria &&
                    coincidePrecioMinimo &&
                    coincidePrecioMaximo
                );
            }
        );

        renderizarProductos(productosFiltrados);
    }

    function renderizarProductos(lista) {
        if (!productosTableBody) {
            return;
        }

        productosTableBody.innerHTML = "";

        if (!Array.isArray(lista) || lista.length === 0) {
            mostrarFilaInformativa(
                productos.length === 0
                    ? "No hay productos registrados."
                    : "No se encontraron productos con los filtros seleccionados."
            );

            actualizarContador(0);
            return;
        }

        lista.forEach(function (producto) {
            const fila = document.createElement("tr");

            fila.appendChild(
                crearCelda(obtenerIdProducto(producto) ?? "")
            );

            fila.appendChild(
                crearCelda(
                    obtenerTextoSeguro(producto.nombre)
                )
            );

            fila.appendChild(
                crearCelda(
                    obtenerTextoSeguro(producto.categoria)
                )
            );

            fila.appendChild(
                crearCelda(
                    formatearMoneda(
                        obtenerPrecio(producto)
                    )
                )
            );

            // Calcular stock total por producto sumando inventarios
            const productoId = obtenerIdProducto(producto);
            const stockTotal = inventarios.reduce((acc, inv) => {
                const pid = inv.productoId ?? inv.producto?.id;
                if (Number(pid) === Number(productoId)) {
                    return acc + (Number(inv.stock) || 0);
                }
                return acc;
            }, 0);

            fila.appendChild(
                crearCelda(stockTotal)
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
                    editarProducto(producto);
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
                    solicitarEliminacion(producto);
                }
            );

            celdaAcciones.appendChild(botonEditar);
            celdaAcciones.appendChild(botonEliminar);

            fila.appendChild(celdaAcciones);
            productosTableBody.appendChild(fila);
        });

        actualizarContador(lista.length);
    }

    function crearCelda(valor) {
        const celda = document.createElement("td");
        celda.textContent = obtenerTextoSeguro(valor);
        return celda;
    }

    function mostrarFilaInformativa(mensaje) {
        if (!productosTableBody) {
            return;
        }

        productosTableBody.innerHTML = "";

        const fila = document.createElement("tr");
        const celda = document.createElement("td");

        celda.colSpan = 5;
        celda.className = "no-data";
        celda.textContent = mensaje;

        fila.appendChild(celda);
        productosTableBody.appendChild(fila);
    }

    function actualizarContador(cantidadVisible) {
        if (!productosCounter) {
            return;
        }

        productosCounter.textContent =
            "Productos mostrados: " +
            cantidadVisible +
            " de " +
            productos.length;
    }

    function actualizarFiltroCategorias(lista) {
        if (!categoryFilterSelect) {
            return;
        }

        const categoriaSeleccionada =
            categoryFilterSelect.value;

        const categorias = Array.from(
            new Set(
                lista
                    .map(function (producto) {
                        return obtenerTextoSeguro(
                            producto.categoria
                        ).trim();
                    })
                    .filter(function (categoria) {
                        return categoria !== "";
                    })
            )
        ).sort(function (categoriaA, categoriaB) {
            return categoriaA.localeCompare(
                categoriaB,
                "es",
                {
                    sensitivity: "base"
                }
            );
        });

        categoryFilterSelect.innerHTML = "";

        const opcionTodas =
            document.createElement("option");

        opcionTodas.value = "";
        opcionTodas.textContent =
            "Todas las categorías";

        categoryFilterSelect.appendChild(opcionTodas);

        categorias.forEach(function (categoria) {
            const opcion =
                document.createElement("option");

            opcion.value = categoria;
            opcion.textContent = categoria;

            categoryFilterSelect.appendChild(opcion);
        });

        const seleccionTodaviaExiste =
            categorias.some(function (categoria) {
                return categoria === categoriaSeleccionada;
            });

        categoryFilterSelect.value =
            seleccionTodaviaExiste
                ? categoriaSeleccionada
                : "";
    }

    function actualizarResumen(lista) {
        const cantidad = Array.isArray(lista)
            ? lista.length
            : 0;

        const categorias = new Set(
            Array.isArray(lista)
                ? lista
                    .map(function (producto) {
                        return normalizarTexto(
                            producto.categoria
                        );
                    })
                    .filter(function (categoria) {
                        return categoria !== "";
                    })
                : []
        );

        const precioTotal = Array.isArray(lista)
            ? lista.reduce(
                function (acumulado, producto) {
                    return (
                        acumulado +
                        obtenerPrecio(producto)
                    );
                },
                0
            )
            : 0;

        const promedio =
            cantidad > 0
                ? precioTotal / cantidad
                : 0;

        if (summaryTotalProductos) {
            summaryTotalProductos.textContent =
                formatearNumero(cantidad);
        }

        if (summaryTotalCategorias) {
            summaryTotalCategorias.textContent =
                formatearNumero(categorias.size);
        }

        if (summaryAveragePrice) {
            summaryAveragePrice.textContent =
                formatearMoneda(promedio);
        }
    }

    function actualizarVistaPreviaPrecio() {
        if (!precioFormateadoInput) {
            return;
        }

        const precio = precioInput
            ? Number(precioInput.value)
            : 0;

        precioFormateadoInput.value =
            formatearMoneda(
                Number.isFinite(precio) && precio > 0
                    ? precio
                    : 0
            );
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
                ...(opciones && opciones.headers
                    ? opciones.headers
                    : {})
            }
        };

        let response;

        try {
            response = await fetch(url, configuracion);
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
                return "No se encontró el producto solicitado.";

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
            datos.productos
        ];

        return posiblesListas.find(Array.isArray) || [];
    }

    function obtenerIdProducto(producto) {
        if (!producto || typeof producto !== "object") {
            return null;
        }

        return obtenerNumeroEntero(
            producto.id ?? producto.productoId
        );
    }

    function obtenerPrecio(producto) {
        const precio = Number(
            producto && producto.precio
        );

        return Number.isFinite(precio)
            ? precio
            : 0;
    }

    function mostrarErrorCampo(
        campo,
        elementoError,
        mensaje
    ) {
        if (campo) {
            campo.classList.add("input-error");
            campo.setAttribute("aria-invalid", "true");
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
            categoriaInput,
            categoriaError
        );
        limpiarErrorCampo(precioInput, precioError);
    }

    function enfocarPrimerCampoInvalido() {
        const campoInvalido = document.querySelector(
            "#productoForm .input-error"
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
            categoriaInput,
            precioInput,
            clearFormButton,
            cancelEditButton,
            newProductoButton
        ];

        controles.forEach(function (control) {
            if (control) {
                control.disabled = cargando;
            }
        });

        cambiarEstadoBoton(
            saveProductoButton,
            cargando,
            cargando
                ? "Guardando..."
                : productoIdInput.value
                    ? "Actualizar producto"
                    : "Guardar producto"
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

    function crearErrorPeticion(mensaje, status) {
        const error = new Error(mensaje);
        error.status = status;
        return error;
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

    function obtenerNumeroDecimalOpcional(valor) {
        if (
            valor === null ||
            valor === undefined ||
            String(valor).trim() === ""
        ) {
            return null;
        }

        const numero = Number(valor);

        return Number.isFinite(numero)
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

    function formatearMoneda(valor) {
        const numero = Number(valor);

        return new Intl.NumberFormat(
            "es-CO",
            {
                style: "currency",
                currency: "COP",
                minimumFractionDigits: 0,
                maximumFractionDigits: 2
            }
        ).format(
            Number.isFinite(numero)
                ? numero
                : 0
        );
    }
});