# LogiTrack IQ MCP Server

Servidor MCP independiente del backend Spring Boot. Expone exactamente seis herramientas mediante **Streamable HTTP** para que un cliente remoto, incluido n8n en una fase posterior, pueda conectarse a `POST`/`GET /mcp`.

## Requisitos e instalación

- Node.js 20 o superior
- Backend LogiTrack IQ disponible y un usuario con rol `AGENTE`

```powershell
npm install
Copy-Item .env.example .env
npm run build
npm start
```

Configure `.env` sin versionarlo:

```text
LOGITRACK_API_BASE_URL=http://localhost:8080
LOGITRACK_AGENT_USERNAME=
LOGITRACK_AGENT_PASSWORD=
MCP_PORT=3001
MCP_BEARER_TOKEN=
```

El servidor queda disponible en `http://localhost:3001/mcp` y su salud técnica en `GET /health`.

Si `MCP_BEARER_TOKEN` está configurado, toda solicitud a `/mcp` debe incluir
`Authorization: Bearer <MCP_BEARER_TOKEN>`; `/health` continúa público. Para
exponer el servidor mediante ngrok o conectarlo desde n8n remoto, configure
este token. Es una protección del borde MCP y **no** es el JWT de LogiTrack.

## Autenticación y límites

Cada herramienta inicia sesión en `POST /auth/login` con el usuario `AGENTE`, conserva temporalmente el JWT en memoria y envía `Authorization: Bearer <jwt>` a la API. Ante una respuesta 401 invalida el token, inicia sesión de nuevo y reintenta una sola vez. n8n nunca recibe ese JWT: usa, si corresponde, solamente el `MCP_BEARER_TOKEN` para acceder al MCP.

Este componente no accede a MySQL, no consulta tablas ni calcula métricas. Tampoco puede aprobar, cancelar o recibir órdenes, cambiar estados ni registrar movimientos: esas reglas permanecen en Spring Boot.

## Herramientas

| Herramienta | Método y endpoint Spring |
|---|---|
| `consultar_stock_producto` | `GET /productos/{productoId}/stock` |
| `consultar_bodegas_criticas` | `GET /bodegas/criticas` |
| `consultar_productos_en_riesgo` | `GET /productos/riesgo` |
| `consultar_kpis` | `GET /kpis` |
| `crear_orden_borrador` | `POST /ordenes` |
| `publicar_resumen` | `POST /panel/resumen` |

`crear_orden_borrador` recibe `productoId`, `proveedorId`, `bodegaDestinoId`, `cantidad` y `precioUnitario`; no acepta estado ni total. `publicar_resumen` recibe un resumen con `fecha`, `narrativa`, `alertas` y `accionesSugeridas` con los enums del backend.

## n8n y evidencias

n8n todavía no está configurado ni terminado. Posteriormente se conectará a la URL MCP mediante Streamable HTTP. `evidencias/` queda reservado para capturas de solicitudes y respuestas reales contra Spring Boot; no contiene resultados mock.

## Scripts

```text
npm run dev
npm run build
npm test
npm start
```
