# Especificación funcional de LogiTrack IQ

## 1. Propósito y carácter verificable

Este documento define las reglas observables de LogiTrack IQ. Una implementación es conforme cuando satisface estos contratos y sus criterios de aceptación mediante pruebas o evidencia funcional.

- **REQ:** requisito obligatorio del enunciado.
- **DEC:** decisión técnica del proyecto que completa un detalle sin contradecir el enunciado.

## 2. Convenciones globales

- **REQ:** backend, n8n y datos de prueba operan en `America/Bogota`.
- **DEC:** un `Clock` inyectable será la única referencia del “ahora” en servicios IQ.
- **DEC:** un intervalo diario se expresa como inicio inclusivo y final exclusivo para evitar duplicidad en los límites.
- **DEC:** dinero usa `BigDecimal`, escala 2 y `RoundingMode.HALF_UP`.
- **DEC:** las métricas conservan precisión interna suficiente; solo los valores presentados se redondean a escala 2 con `HALF_UP`.
- **DEC:** la comparación de riesgo se realiza con valores sin redondear para presentación.
- **REQ:** la capacidad de bodega se mide en unidades y debe ser mayor que cero.

## 3. Modelo funcional mínimo

### 3.1 Proveedor

| Campo | Regla |
|---|---|
| `id` | Identificador persistente. |
| `nombre` | **DEC:** texto no vacío. |
| `contacto` | Información de contacto. |
| `diasEntrega` | Entero entre 1 y 90, ambos incluidos. |

**REQ:** los proveedores deben cargarse con `data.sql` o un mecanismo equivalente y reproducible.

### 3.2 Producto

Se conservan los campos heredados y se agrega `proveedorPrincipal`, relación `ManyToOne` opcional con `Proveedor`.

- **REQ:** un producto sin proveedor principal no aparece en `/productos/riesgo`.
- **REQ:** un producto sin proveedor principal no genera una orden automática.

### 3.3 OrdenCompra

| Campo | Regla |
|---|---|
| `id` | Identificador persistente. |
| `producto` | Obligatorio; exactamente un producto por orden. |
| `proveedor` | Obligatorio y existente. |
| `bodegaDestino` | Obligatoria y existente. |
| `cantidad` | Entero mayor que cero. |
| `precioUnitario` | Dinero con la escala y redondeo globales; el PDF no fija un rango adicional. |
| `total` | Calculado por el servidor: `cantidad × precioUnitario`. |
| `fechaCreacion` | Instante de creación según el reloj del backend. |
| `estado` | Inicialmente `BORRADOR`. |
| `creadoPor` | Usuario autenticado que creó la orden. |
| `pdf` | Binario opcional asociado a la orden. |
| `fechaGeneracionPdf` | Fecha/hora opcional de generación. |

### 3.4 ResumenPanel

| Campo | Regla |
|---|---|
| `id` | Identificador persistente. |
| `fecha` | Fecha de negocio en Bogotá, única. |
| `contenidoJson` | Representación del resumen estructurado válido. |
| `autor` | Usuario autenticado que publica o reemplaza. |

- **REQ:** existe como máximo un registro por fecha.
- **REQ:** una publicación válida para una fecha ya existente actualiza ese registro y genera auditoría.
- **REQ:** una publicación inválida no altera el último resumen válido.

## 4. Reglas de inventario

### 4.1 Fuente de verdad

- **REQ:** el stock se calcula exclusivamente desde `Movimiento` y todos sus `DetalleMovimiento`.
- **REQ:** un movimiento puede tener uno o varios detalles y ninguno puede omitirse.
- **REQ:** los datos iniciales deben incluir movimientos `ENTRADA` que representen el inventario inicial.
- **DEC:** `Inventario.stock` no participa en cálculos ni validaciones IQ, aunque la entidad heredada permanezca temporalmente.

### 4.2 Efecto por tipo

Para un detalle de producto `p`, cantidad `q` y bodega `b`:

- `ENTRADA`: suma `q` a la bodega destino.
- `SALIDA`: resta `q` de la bodega origen.
- `TRANSFERENCIA`: resta `q` de la bodega origen y suma `q` a la bodega destino.

```text
stock(p,b) = entradasDestino(p,b)
           + transferenciasDestino(p,b)
           - salidasOrigen(p,b)
           - transferenciasOrigen(p,b)

stockTotal(p) = Σ stock(p,b) para todas las bodegas b
```

### 4.3 Prohibición de stock negativo

- **REQ:** una `SALIDA` se rechaza si algún detalle deja negativo el producto en la bodega origen.
- **REQ:** una `TRANSFERENCIA` se rechaza bajo la misma condición en origen.
- **REQ:** el movimiento completo se rechaza; no se guardan efectos parciales de otros detalles.
- **DEC:** una combinación producto-bodega sin movimientos previos tiene saldo cero.

## 5. Ventanas temporales y métricas

Sea `hoy` la fecha obtenida del `Clock` en `America/Bogota`.

### 5.1 Consumo diario promedio

```text
inicio = hoy - 29 días a las 00:00
fin = mañana a las 00:00
consumoDiarioPromedio = unidades SALIDA en [inicio, fin) / 30
```

- **REQ:** se incluyen 30 días calendario, incluida la fecha de consulta.
- **REQ:** solo cuentan unidades de movimientos `SALIDA`; las transferencias no son consumo.

### 5.2 Punto de reorden

```text
puntoReorden = consumoDiarioPromedio × proveedorPrincipal.diasEntrega × 1.5
```

Solo se calcula riesgo para productos con proveedor principal.

### 5.3 Días y estado de cobertura

```text
si consumo > 0:
    diasCobertura = stockTotal / consumoDiarioPromedio
    estadoCobertura = CON_CONSUMO
si consumo == 0:
    diasCobertura = null
    estadoCobertura = SIN_CONSUMO
```

`CON_CONSUMO` es una decisión técnica; `SIN_CONSUMO` es exigido por el enunciado.

### 5.4 Producto en riesgo

```text
proveedorPrincipal != null AND stockTotal < puntoReorden
```

Si `stockTotal == puntoReorden`, no está en riesgo.

### 5.5 Bodega destino sugerida

- **REQ:** se elige la bodega con menor stock del producto.
- **REQ:** si hay empate, se elige la de menor `id`.
- **DEC:** se consideran todas las bodegas registradas; una bodega sin movimientos para el producto tiene stock cero.

### 5.6 Ocupación y bodega crítica

```text
unidadesAlmacenadas(b) = Σ stock(p,b) para todos los productos p
ocupacion(b) = unidadesAlmacenadas(b) / capacidad(b) × 100
```

Una bodega es crítica cuando `ocupacion >= 90`.

### 5.7 Productos en quiebre

Cantidad de productos cuyo `stockTotal == 0`.

### 5.8 Órdenes por aprobar

Cantidad de órdenes en `BORRADOR` y suma de sus campos `total`.

### 5.9 Movimientos de ayer

Se usa el intervalo desde ayer a las 00:00 hasta hoy a las 00:00 en Bogotá. Se cuenta cada movimiento una vez y se informa por separado `ENTRADA`, `SALIDA` y `TRANSFERENCIA`.

## 6. Estados y recepción de órdenes

Estados: `BORRADOR`, `APROBADA`, `RECIBIDA`, `CANCELADA`.

| Estado actual | Estados siguientes válidos |
|---|---|
| `BORRADOR` | `APROBADA`, `CANCELADA` |
| `APROBADA` | `RECIBIDA`, `CANCELADA` |
| `RECIBIDA` | Ninguno |
| `CANCELADA` | Ninguno |

- Toda transición no listada responde 400 con mensaje claro.
- Al cambiar cualquier estado, `pdf = null` y `fechaGeneracionPdf = null`.
- Al ejecutar `APROBADA → RECIBIDA`, se crea un movimiento `ENTRADA` con producto, cantidad y bodega destino de la orden.
- La orden y el movimiento se guardan en una sola transacción: ambos se completan o ambos se revierten.
- **DEC de trazabilidad:** el usuario responsable del movimiento automático es el `ADMIN` que recibe la orden.

## 7. Contratos de API

Los endpoints heredados se mantienen. Los endpoints IQ se agregan en las rutas exactas indicadas.

### 7.1 `GET /kpis`

Autorización: `ADMIN` o `AGENTE`.

```json
{
  "calculadoEn": "2026-08-24T06:00:00-05:00",
  "ocupacionPorBodega": [
    { "bodegaId": 1, "nombre": "Bogota", "porcentaje": 92.50 }
  ],
  "productosEnQuiebre": 1,
  "productosEnRiesgo": 2,
  "ordenesPorAprobar": { "cantidad": 1, "montoTotal": 45000.00 },
  "movimientosAyer": { "entrada": 2, "salida": 3, "transferencia": 1 }
}
```

### 7.2 `GET /productos/{id}/stock`

Autorización: `ADMIN` o `AGENTE`.

- Devuelve stock total y desglose por bodega calculados desde movimientos.
- **DEC de DTO:** incluye `productoId`, `nombreProducto`, `stockTotal` y `stockPorBodega`; cada desglose contiene `bodegaId`, `nombreBodega` y `stock`.
- Producto inexistente: 404.

### 7.3 `GET /productos/riesgo`

Autorización: `ADMIN` o `AGENTE`. Cada elemento conserva:

```text
productoId, nombreProducto, proveedorId, stockTotal,
consumoDiarioPromedio, puntoReorden, diasCobertura,
estadoCobertura, bodegaDestinoId
```

**DEC de extensión:** añade `precioUnitarioSugerido`, obtenido de `Producto.precio`, para que MCP pueda enviar `precioUnitario` al crear la orden.

**DEC de orden:** la lista se ordena por `productoId` ascendente para que “el primer producto” de n8n sea determinista.

### 7.4 `GET /bodegas/criticas`

Autorización: `ADMIN` o `AGENTE`.

- Lista solo bodegas con ocupación mayor o igual a 90 %.
- **DEC de DTO:** incluye `bodegaId`, `nombre`, `capacidad`, `unidadesAlmacenadas` y `porcentaje`.

### 7.5 `GET /proveedores`

Autorización: `ADMIN` o `AGENTE`. Devuelve `id`, `nombre`, `contacto` y `diasEntrega`.

### 7.6 Órdenes

#### `GET /ordenes`

- `ADMIN` o `AGENTE`.
- Acepta el filtro opcional `estado`; **DEC:** un valor inválido responde 400.

#### `POST /ordenes`

- `ADMIN` o `AGENTE`.
- Entrada:

```json
{
  "productoId": 12,
  "proveedorId": 4,
  "bodegaDestinoId": 3,
  "cantidad": 10,
  "precioUnitario": 4500.00
}
```

- El servidor fuerza `BORRADOR`, asigna fecha y usuario autenticado y calcula `total`.
- Cantidad cero o negativa: 400. Referencias inexistentes: 404.
- **DEC HTTP:** creación válida: 201.

#### `GET /ordenes/{id}`

- `ADMIN` o `AGENTE`.
- Devuelve los campos funcionales, excepto el binario PDF; inexistente: 404.

#### `PATCH /ordenes/{id}/estado`

- Solo `ADMIN`.
- Acepta exactamente `{ "estado": "APROBADA" }`.
- **DEC HTTP:** una transición válida responde 200 con la orden actualizada.
- Propiedades adicionales, estado desconocido o transición inválida: 400; orden inexistente: 404.

### 7.7 PDF de la orden

#### `POST /ordenes/{id}/pdf`

- `ADMIN` o `AGENTE` autenticado (**DEC de autorización**, pues el PDF no asigna este endpoint a un rol concreto).
- Genera o reemplaza el PDF mediante Apache PDFBox, lo almacena y registra su fecha de generación.
- **DEC HTTP:** responde 200 con el PDF como `application/pdf`; orden inexistente: 404.
- En estado `BORRADOR`, el documento lleva una marca de agua visible.

#### `GET /ordenes/{id}/pdf`

- `ADMIN` o `AGENTE` autenticado, por la misma decisión anterior.
- Devuelve `application/pdf`; orden inexistente o PDF no generado: 404.

### 7.8 Resumen del panel

#### `POST /panel/resumen`

- `ADMIN` o `AGENTE`.
- Valida el contrato exacto de la sección 8 y publica el resumen de la fecha actual.
- Si ya existe uno para esa fecha, lo actualiza y conserva la auditoría.
- Respuesta válida: 200 (**DEC HTTP**). Un incumplimiento: 400 sin reemplazar el resumen válido anterior.

#### `GET /panel/resumen`

- `ADMIN` o `AGENTE`.
- Devuelve el último resumen válido; si no existe ninguno: 404.

## 8. Contrato exacto del resumen diario

No se admiten propiedades adicionales. El cuerpo contiene exactamente `fecha`, `narrativa`, `alertas` y `accionesSugeridas`.

```json
{
  "fecha": "2026-08-28",
  "narrativa": "Hay productos en riesgo y una orden pendiente de aprobación.",
  "alertas": [{
    "severidad": "ALTA",
    "titulo": "Producto en riesgo",
    "detalle": "Producto X está por debajo de su punto de reorden.",
    "productoId": 12,
    "ordenId": null,
    "bodegaId": 3
  }],
  "accionesSugeridas": [{
    "tipo": "REVISAR_ORDEN",
    "descripcion": "Revisar la orden 14 antes de aprobarla.",
    "ordenId": 14,
    "productoId": null,
    "bodegaId": null
  }]
}
```

- `fecha` usa `YYYY-MM-DD` y debe ser hoy según el `Clock` de `America/Bogota`.
- `narrativa` tiene entre 20 y 500 caracteres.
- `alertas` y `accionesSugeridas` son arreglos obligatorios, aunque estén vacíos.
- `severidad` admite `BAJA`, `MEDIA` o `ALTA`; cada alerta tiene título, detalle y al menos un ID.
- `tipo` admite `REVISAR_ORDEN`, `REVISAR_PRODUCTO` o `REVISAR_BODEGA`; cada acción tiene descripción y exactamente un ID.
- Cada identificador informado debe existir.
- No se valida el significado de la narrativa en lenguaje natural.
- Se valida antes de escribir. Un cuerpo inválido responde 400 y no altera el resumen válido anterior.
- La persistencia garantiza una fila por fecha; otra publicación válida del mismo día actualiza esa fila.

## 9. Documento PDF de una orden

Incluye como mínimo identificación de la orden, fecha, estado, producto, proveedor, bodega destino, cantidad, precio unitario y total. Apache PDFBox genera el documento; el binario se almacena como `@Lob`, junto con `fechaGeneracionPdf`.

- Generar de nuevo reemplaza el binario y su fecha.
- Toda transición válida invalida el documento: `pdf = null` y `fechaGeneracionPdf = null`.
- En `BORRADOR` se agrega una marca de agua visible.
- El contenido se entrega como `application/pdf`.

## 10. Seguridad y auditoría

Se reutilizan JWT, Spring Security, usuarios y auditoría heredados. El registro público no acepta selección de `ADMIN` ni `AGENTE`; las cuentas de demostración se crean con datos reproducibles o una operación administrativa existente.

| Operación | AGENTE | ADMIN |
|---|---:|---:|
| Consultar KPIs, stock, riesgos y bodegas críticas | Sí | Sí |
| Consultar proveedores y órdenes (**DEC**) | Sí | Sí |
| Crear orden `BORRADOR` | Sí | Sí |
| Publicar resumen | Sí | Sí |
| Generar/consultar PDF (**DEC**) | Sí | Sí |
| Aprobar, recibir o cancelar una orden | No | Sí |
| Registrar movimientos manualmente | No | Sí |

Se auditan creación de orden, publicación o reemplazo de resumen, transición de orden y recepción. Las consultas no requieren auditoría.

## 11. Errores HTTP

- 400: datos o enumeraciones inválidos, stock negativo, cantidad no positiva, transición no permitida o contrato de resumen incumplido.
- 401: credencial ausente, expirada o inválida en una ruta protegida.
- 403: usuario autenticado sin el rol requerido.
- 404: recurso solicitado inexistente; también PDF aún no generado y ausencia de resúmenes válidos.

Los errores pasan por el manejador global heredado y ofrecen un mensaje claro, sin exponer secretos ni detalles internos.

## 12. MCP, skill y n8n

El servidor MCP usa la API REST con un usuario `AGENTE`, no accede a MySQL ni repite reglas. Tendrá exactamente seis herramientas:

1. `consultar_stock_producto(productoId)` → `GET /productos/{id}/stock`.
2. `consultar_bodegas_criticas()` → `GET /bodegas/criticas`.
3. `consultar_productos_en_riesgo()` → `GET /productos/riesgo`.
4. `consultar_kpis()` → `GET /kpis`.
5. `crear_orden_borrador(productoId, proveedorId, bodegaDestinoId, cantidad, precioUnitario)` → `POST /ordenes`.
6. `publicar_resumen(resumen)` → `POST /panel/resumen`.

No habrá herramienta para aprobar, cancelar o recibir órdenes. La skill ordena consultar primero riesgos y KPIs, crear como máximo una orden en borrador por ejecución, publicar solo el JSON válido y comunicar los errores de herramientas.

El único flujo n8n se llamará **Resumen diario de inventario**, se programará a las 06:00 en `America/Bogota` y podrá ejecutarse manualmente para demostrarlo. Su AI Agent usa MCP, consulta KPIs y riesgos, y si hay riesgo crea como máximo una orden para el primer producto listado. La cantidad es `ceil(max(1, puntoReorden × 2 - stockTotal))`. Luego publica el resumen y registra éxito o error.

## 13. Dashboard

`frontend/` usará HTML, CSS y JavaScript sin framework y consumirá endpoints reales. Mostrará los cuatro indicadores, movimientos de ayer, ocupación por bodega, narrativa, alertas, acciones sugeridas, productos en riesgo y órdenes `BORRADOR`. Reutilizará el login JWT y guardará el token solo en `sessionStorage`.

Permitirá generar y visualizar el PDF de una orden en borrador. El botón **Aprobar** aparecerá únicamente para un `ADMIN` autenticado y, al aprobar, la tabla se actualizará. No se requieren animaciones, diseño móvil ni diseño avanzado.

## 14. Criterios de aceptación verificables

- El stock y sus desgloses se reconstruyen exclusivamente desde todos los detalles de movimientos.
- Una salida o transferencia que produciría saldo negativo responde 400 y no persiste cambios parciales.
- Con consumo cero, cobertura es `null` y estado `SIN_CONSUMO`; con consumo positivo, el estado es `CON_CONSUMO`.
- Un stock igual al punto de reorden no aparece en riesgo; uno estrictamente menor sí, siempre que tenga proveedor principal.
- KPIs, bodegas críticas, quiebres, borradores y movimientos de ayer siguen las fórmulas y límites temporales documentados.
- Una cantidad de orden cero o negativa responde 400.
- Solo se aceptan las cuatro transiciones listadas; una orden cancelada no se puede aprobar.
- Recibir una orden aprobada cambia su estado y crea la `ENTRADA` en una sola transacción.
- Un `AGENTE` consulta, crea borradores y publica resúmenes, pero recibe 403 al intentar transiciones o movimientos manuales.
- Un resumen inválido responde 400 y conserva el último válido; publicar nuevamente hoy actualiza una sola fila y deja auditoría.
- El PDF en borrador se guarda con marca de agua `BORRADOR`; cambiar el estado lo invalida y su GET responde 404 hasta regenerarlo.
- Existe al menos una prueba de integración con Spring Boot/MockMvc para el cambio de estado o la publicación del resumen.
- MCP expone exactamente seis herramientas y n8n crea como máximo una orden por ejecución sin aprobarla.
- El flujo extremo a extremo concluye con el movimiento `ENTRADA` y el dashboard mostrando el stock actualizado.
