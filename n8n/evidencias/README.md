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

## Error controlado verificado

- Prueba: error controlado.
- Condicion: MCP detenido temporalmente.
- Ejecucion: Manual Trigger.
- Resultado: fallo controlado.
- Orden nueva: no.
- Resumen reemplazado: no.
- BORRADOR final: 1.

La captura real principal es `ejecucion-error-controlado.png`. No se
inventan detalles visuales adicionales.

### Capturas auxiliares del flujo de error

Las siguientes capturas reales complementan la evidencia principal y documentan
los nodos visibles durante el fallo controlado:

- `ejecucion-error-controlado-nodoAiAgent.png` ? AI Agent.
- `ejecucion-error-controlado-nodoMcpClientTool.png` ? MCP Client Tool.
- `ejecucion-error-controlado-nodoCodeRegistrarErrorControlado.png` ? Registrar error controlado.
