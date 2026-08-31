# Evidencias reales de n8n

Hechos verificados el **2026-08-31** en una ejecucion manual real de
**Resumen diario de inventario** con **Gemini 2.5 Flash**:

- `consultar_kpis`: SUCCESS.
- `consultar_productos_en_riesgo`: SUCCESS.
- `crear_orden_borrador`: SUCCESS; se creo una unica orden, ID 1, cantidad
  80, estado `BORRADOR`.
- El primer `publicar_resumen` fue rechazado por validacion porque una accion
  no enlazaba exactamente un recurso.
- El segundo `publicar_resumen`: SUCCESS.
- Resultado final del Agent: `EXITO`.
- El resumen quedo persistido y el total final de ordenes BORRADOR es 1.

No se inventan capturas. Las capturas reales presentes en este directorio se
conservan como evidencia sin alterar su contenido.

Sigue pendiente una evidencia real separada de error controlado, obtenida sin
crear ordenes ni publicar resumenes adicionales.
