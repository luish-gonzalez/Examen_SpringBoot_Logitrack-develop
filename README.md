# LogiTrack IQ

Backend académico Spring Boot que extiende LogiTrack con stock derivado desde movimientos, alertas de riesgo, proveedores, órdenes de compra, KPIs, resumen de panel y auditoría.

## Requisitos

- Java 17
- MySQL 8+
- Maven Wrapper incluido en el repositorio

## Configuración local

Defina estas variables de entorno antes de iniciar la aplicación:

```text
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
```

No se versionan credenciales locales. El archivo `application.properties` consume esas variables.

## Base de datos de demostración

En una base MySQL vacía, cargue los scripts manualmente y en este orden:

1. `src/main/resources/schema.sql`
2. `src/main/resources/data.sql`

Las semillas usan fechas relativas e incluyen usuarios académicos de prueba `admin` (ADMIN), `agente` (AGENTE) y `empleado` (EMPLEADO), además de proveedores, bodegas, productos y movimientos. No contienen credenciales de infraestructura.

Las tres cuentas demo usan la contraseña académica `LogiTrackIQ2026!`.

## Ejecución

Desde la raíz del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

## API y pruebas

- Swagger UI: `/swagger-ui/index.html`
- OpenAPI: `/v3/api-docs`
- Rutas IQ principales: `/kpis`, `/productos/{id}/stock`, `/productos/riesgo`, `/bodegas/criticas`, `/proveedores`, `/ordenes` y `/panel/resumen`.

Para ejecutar la suite aislada, que usa H2 en memoria en modo MySQL y no requiere una base externa:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

## Frontend LogiTrack IQ

El frontend est?tico se sirve desde el mismo Spring Boot en `http://localhost:8080/`. No requiere Node, Vite ni un servidor adicional.

El inicio de sesi?n usa `POST /auth/login` y guarda el JWT ?nicamente en `sessionStorage`. Las cuentas acad?micas de demostraci?n son `admin` (ADMIN), `agente` (AGENTE) y `empleado` (EMPLEADO).

El panel IQ consulta KPIs, ocupaci?n, riesgos, ?rdenes BORRADOR y resumen diario. ADMIN puede aprobar una orden BORRADOR; AGENTE puede consultar y crear BORRADOR mediante las rutas permitidas, pero no aprobar. Ocultar una acci?n en la interfaz es solo UX: Spring Security mantiene la autorizaci?n real.

## MCP, Skill y automatización

- El servidor [MCP](mcp-server/README.md) expone seis herramientas sobre Streamable HTTP y opera la API con el rol `AGENTE`; no accede directamente a MySQL ni administra estados de órdenes.
- El [Skill de operación](skills/operacion-logitrack/SKILL.md) define el flujo seguro: consulta KPIs y riesgos, crea como máximo una orden BORRADOR y publica un resumen válido.
- El export [n8n](n8n/resumen-diario-inventario.json) contiene el workflow inactivo **Resumen diario de inventario**. Sus instrucciones de importación y configuración segura están en [n8n/README.md](n8n/README.md).

## Arquitectura y evidencias

- Diagrama y responsabilidades: [docs/arquitectura-logitrack-iq.md](docs/arquitectura-logitrack-iq.md).
- Estado verificable de entrega: [docs/checklist-entrega.md](docs/checklist-entrega.md).
- Evidencia SDD/TDD: [docs/evidencias](docs/evidencias/) y [docs/sdd](docs/sdd/).
- Evidencias reales MCP: [mcp-server/evidencias](mcp-server/evidencias/).
- Evidencias reales n8n: [n8n/evidencias](n8n/evidencias/).