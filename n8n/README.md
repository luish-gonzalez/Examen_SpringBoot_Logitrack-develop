# Resumen diario de inventario

`resumen-diario-inventario.json` contiene un unico workflow de n8n, inicialmente
inactivo, llamado **Resumen diario de inventario**. Fue probado con
**Google Gemini Chat Model** y el modelo **gemini-2.5-flash**, junto con
`Schedule Trigger`, `AI Agent` y `MCP Client Tool` (Streamable HTTP).

## Importacion y configuracion minima

1. Importe `resumen-diario-inventario.json` y conserve la zona horaria
   `America/Bogota`.
2. Configure una credencial Gemini en **Google Gemini Chat Model**. El export
   no contiene credenciales ni API keys.
3. Cree o seleccione en **MCP Client Tool** una credencial **Bearer Auth** con
   el valor local de `MCP_BEARER_TOKEN`. No use el JWT de AGENTE en n8n.
4. Configure `LOGITRACK_MCP_URL` con una URL accesible del MCP que termine
   en `/mcp`. El localhost probado fue `http://localhost:3001/mcp`.
5. Verifique el horario diario a las **06:00** en `America/Bogota`, ejecute
   manualmente primero y active el Schedule solo tras una prueba exitosa.

El endpoint versionado es `={{ $env.LOGITRACK_MCP_URL }}`. En n8n 2.36.8,
el acceso a `$env` puede requerir
`N8N_BLOCK_ENV_ACCESS_IN_NODE=false`. Si la UI no puede resolverlo para
cargar opciones, configure temporalmente la URL manualmente sin guardar una
URL privada ni secretos en el JSON versionado.

El MCP usa Streamable HTTP y Bearer Auth en `/mcp`. El nodo selecciona
exclusivamente: `consultar_stock_producto`, `consultar_bodegas_criticas`,
`consultar_productos_en_riesgo`, `consultar_kpis`,
`crear_orden_borrador` y `publicar_resumen`.

## Reglas del Agent

El Agent consulta primero KPIs y riesgos; si hay riesgos usa exclusivamente el
primer producto y puede crear como maximo una orden BORRADOR con
`ceil(max(1, puntoReorden * 2 - stockTotal))`. No usa fallback a un segundo
producto, no aprueba, recibe o cancela ordenes, no registra movimientos
manuales ni accede a MySQL.

El resumen debe ser valido y reportar errores sin inventar datos. Cada elemento
de `accionesSugeridas` enlaza exactamente un recurso real: `REVISAR_ORDEN`
usa solo `ordenId`, `REVISAR_PRODUCTO` solo `productoId` y
`REVISAR_BODEGA` solo `bodegaId`; los campos opcionales restantes se
omiten.

## Validacion del export

El export fue importado en una instancia temporal de **n8n 2.36.8**:

```text
Successfully imported 1 workflow.
```

El workflow permanece inactivo. No guarde API keys, valores de
`MCP_BEARER_TOKEN`, JWTs ni URLs privadas en este repositorio.
