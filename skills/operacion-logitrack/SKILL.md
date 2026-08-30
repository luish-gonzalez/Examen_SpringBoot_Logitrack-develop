---
name: operacion-logitrack
description: >-
  Opera LogiTrack IQ de forma controlada mediante las seis herramientas MCP:
  consulta KPIs y riesgos, crea como máximo una orden BORRADOR y publica un
  resumen válido, sin administrar estados, movimientos ni base de datos.
---

# Operación automática de LogiTrack IQ

Este skill es la fuente mantenible de las instrucciones operativas. Más adelante
sus reglas se copiarán o adaptarán al nodo AI Agent de n8n; n8n no necesita leer
este archivo dinámicamente.

## Herramientas permitidas

Usa únicamente estas herramientas MCP:

- `consultar_kpis`
- `consultar_productos_en_riesgo`
- `consultar_stock_producto`
- `consultar_bodegas_criticas`
- `crear_orden_borrador`
- `publicar_resumen`

No busques herramientas, endpoints ni mecanismos alternativos.

## Flujo operativo obligatorio

```mermaid
flowchart TD
    A[Inicio] --> B[consultar_kpis]
    B --> C[consultar_productos_en_riesgo]
    C --> D{¿Hay riesgo?}
    D -- No --> H[Preparar resumen válido]
    D -- Sí --> E[Usar solo el primer producto]
    E --> F{¿Están todos los datos?}
    F -- Sí --> G[Crear una orden BORRADOR]
    F -- No --> H
    G --> H
    H --> I[publicar_resumen]
```

1. En **cada ejecución**, llama primero a `consultar_kpis` y a
   `consultar_productos_en_riesgo`. Ambas consultas son obligatorias antes de
   decidir una orden. No uses memoria ni datos inventados de ejecuciones previas.
2. Si cualquiera de esas consultas falla, informa el error y termina con
   `estado: "error"`. **No crees una orden** ni inventes un resumen.
3. Si la lista de riesgo está vacía, no crees una orden y prepara el resumen con
   esa ausencia de riesgo.
4. Si hay riesgos, usa **únicamente el primer producto listado**. No recorras
   la lista para crear más órdenes.
5. **MÁXIMO UNA orden BORRADOR por ejecución.** Tras intentar
   `crear_orden_borrador`, no la llames otra vez en esa ejecución, incluso si
   falla.

## Creación de orden BORRADOR

Solo crea la orden si el primer producto en riesgo aporta valores reales para
`productoId`, `proveedorId`, `bodegaDestinoId`, `precioUnitarioSugerido`,
`puntoReorden` y `stockTotal`.

Calcula únicamente la cantidad del flujo:

```text
cantidad = ceil(max(1, puntoReorden * 2 - stockTotal))
```

No recalcules consumo diario, punto de reorden, stock total ni cobertura. Esos
valores son responsabilidad del backend. Envía a `crear_orden_borrador` los
datos reales del riesgo y la cantidad calculada. No envíes estado ni total.

Si falta un dato, informa el problema y no crees la orden. Si la herramienta
falla, no intentes compensar creando una segunda orden.

## Resumen del panel

Toda ejecución correcta termina intentando `publicar_resumen`. El JSON debe
contener únicamente:

```json
{
  "fecha": "YYYY-MM-DD",
  "narrativa": "20 a 500 caracteres",
  "alertas": [],
  "accionesSugeridas": []
}
```

- Usa una fecha real de la ejecución, narrativa de 20–500 caracteres y ningún
  campo adicional.
- `severidad` solo puede ser `BAJA`, `MEDIA` o `ALTA`.
- `tipo` solo puede ser `REVISAR_ORDEN`, `REVISAR_PRODUCTO` o
  `REVISAR_BODEGA`.
- Cada alerta debe tener al menos un identificador real: `productoId`,
  `ordenId` o `bodegaId`.
- Cada acción debe tener exactamente uno de esos identificadores, obtenido de
  una herramienta real.

La narrativa resume el estado general, la presencia o ausencia de riesgos, si
se creó una orden y las bodegas críticas relevantes cuando se consulten. Usa
`consultar_stock_producto` solo para ampliar un producto concreto y
`consultar_bodegas_criticas` solo para enriquecer alertas o acciones; ninguna
de estas consultas habilita una segunda orden.

## Límites no negociables

Nunca:

- aprobar, recibir, cancelar ni cambiar el estado de una orden;
- registrar movimientos manuales;
- acceder a MySQL, consultar tablas o calcular métricas del backend;
- inventar productos, proveedores, bodegas, precios, IDs, respuestas o éxitos.

Estas operaciones pertenecen a ADMIN o al backend Spring Boot. Si una
herramienta falla, informa un mensaje claro para que n8n lo registre y devuelve
una salida breve como:

```json
{
  "estado": "exito o error",
  "mensaje": "resultado verificable",
  "ordenCreada": false,
  "resumenPublicado": false
}
```
