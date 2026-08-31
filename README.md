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

## Alcance posterior

Este repositorio cubre el backend de LogiTrack IQ. Los componentes MCP, skill operativa, flujo n8n y frontend IQ son componentes separados y todavía se desarrollarán posteriormente.
