# Resumen diario de inventario

`resumen-diario-inventario.json` es un único workflow de n8n, inicialmente
inactivo, llamado **Resumen diario de inventario**. Está preparado para n8n
actual con los nodos `Schedule Trigger`, `AI Agent`, `OpenAI Chat Model` y
`MCP Client Tool` (versión 1.4, transporte Streamable HTTP).

## Importación y configuración mínima

1. En n8n, importe `resumen-diario-inventario.json` y mantenga su zona horaria
   de workflow en `America/Bogota`.
2. Seleccione en **OpenAI Chat Model** una credencial de OpenAI de n8n. El
   export no contiene credenciales ni API keys.
3. Cree o seleccione en **MCP Client Tool** una credencial **Bearer Auth** cuyo
   token sea el valor local de `MCP_BEARER_TOKEN`. No use el JWT del usuario
   AGENTE en n8n.
4. Configure la variable de entorno de n8n `LOGITRACK_MCP_URL` con la URL
   pública o accesible del MCP, terminada en `/mcp`; por ejemplo, la URL
   conceptual `<MCP_PUBLIC_URL>/mcp`. No guarde una URL real de ngrok en este
   repositorio.
5. Verifique que el horario quede diario a las **06:00** en `America/Bogota`.
   Active el workflow solo después de comprobar las credenciales y la
   conectividad del MCP.

El MCP usa Streamable HTTP en `/mcp`. El nodo MCP descubre las herramientas y
expone de forma seleccionada únicamente las seis autorizadas por el workflow:
`consultar_stock_producto`, `consultar_bodegas_criticas`,
`consultar_productos_en_riesgo`, `consultar_kpis`,
`crear_orden_borrador` y `publicar_resumen`.

## Ejecución y verificación

Para una demostración manual, use **Execute workflow** sobre este mismo
workflow; el nodo `Ejecutar manualmente` alimenta al mismo AI Agent y no existe
un workflow manual secundario. Las ejecuciones programadas nacen del `Schedule
Trigger` diario a las 06:00.

La salida final normalizada contiene `estado`, `mensaje`, `ordenCreada` y
`resumenPublicado`. Una ejecución correcta debe registrar `EXITO`; si no se
puede conectar al MCP, falla una herramienta, falla la autenticación o el
backend responde con error, el camino de error devuelve `ERROR` sin crear una
orden basada en datos parciales.

Para obtener posteriormente una evidencia de error controlado sin alterar datos
ni la base, detenga temporalmente el MCP o use temporalmente una credencial
Bearer incorrecta. Restaure el servicio o la credencial inmediatamente después
de la captura.
## Validacion del export

El export fue validado mediante una instancia temporal real de **n8n 2.36.8**:

```text
Successfully imported 1 workflow.
```

Despues de importar, configure en este orden:

1. `LOGITRACK_MCP_URL` con una URL accesible del MCP terminada en `/mcp`.
2. Una credencial **Bearer Auth** de n8n con el valor local de
   `MCP_BEARER_TOKEN`.
3. La credencial de OpenAI en **OpenAI Chat Model**.
4. La zona horaria `America/Bogota` y el horario diario de las **06:00**.
5. Una ejecucion manual del mismo workflow.
6. El Schedule solo despues de una prueba exitosa.

Si el acceso a `$env` esta bloqueado por la configuracion de n8n, habilite el
acceso apropiado o configure la URL manualmente sin incluir secretos en el JSON
versionado. No guarde una URL real de ngrok en este repositorio.
