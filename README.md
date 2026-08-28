# Examen Spring Boot - LogiTrack

## Descripción

Este examen implementa un módulo REST de reportes para el proyecto **LogiTrack**. El módulo permite consultar movimientos de inventario y registros de auditoría utilizando filtros opcionales y consultas JPA.

El desarrollo se encuentra dentro del paquete:

```text
src/main/java/com/logitrack/examen
```

## Objetivo

Implementar endpoints REST que permitan obtener información consolidada de movimientos y auditorías mediante filtros combinables.

## Estructura del módulo

```text
src/main/java/com/logitrack/examen/
├── ExamenMovimientoRepository.java
├── ExamenAuditoriaRepository.java
├── ExamenReporteService.java
└── ExamenReporteController.java
```

## Tecnologías utilizadas

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- Maven
- Thunder Client

## Endpoints

### Consultar movimientos

```http
GET /api/reportes/movimientos
```

Parámetros opcionales:

| Parámetro | Tipo | Ejemplo |
|---|---|---|
| `bodegaId` | `Long` | `1` |
| `productoId` | `Long` | `4` |
| `tipoMovimiento` | `TipoMovimiento` | `SALIDA` |
| `fechaInicio` | `LocalDateTime` | `2026-07-01T00:00:00` |
| `fechaFin` | `LocalDateTime` | `2026-07-30T23:59:59` |

### Consultar auditorías

```http
GET /api/reportes/auditoria
```

Parámetros opcionales:

| Parámetro | Tipo | Ejemplo |
|---|---|---|
| `productoId` | `Long` | `1` |
| `fechaInicio` | `LocalDateTime` | `2026-07-01T00:00:00` |
| `fechaFin` | `LocalDateTime` | `2026-07-30T23:59:59` |
| `campoModificado` | `String` | `precio` |

## Ejecución

Desde la raíz del proyecto, donde se encuentra `pom.xml`, ejecutar:

```bash
mvn spring-boot:run
```

La aplicación utiliza la clase principal existente:

```text
src/main/java/com/logitrack/LogitrackApplication.java
```

No se requiere una clase principal adicional dentro del paquete `examen`.

## Autenticación JWT

Antes de consultar los reportes se debe obtener un token JWT.

```http
POST http://localhost:8080/auth/login
```

Cuerpo JSON:

```json
{
  "username": "admin",
  "password": "Admin123"
}
```

El valor recibido en `token` se configura en Thunder Client mediante:

```text
Auth → Bearer → Bearer Token
```

## Evidencias de pruebas en Thunder Client

### 1. Inicio de sesión y obtención del token

[Abrir imagen de la prueba](evidenciasExamen/01_login_token.png)

![Inicio de sesión y token](evidenciasExamen/01_login_token.png)

### 2. Movimientos sin filtros

```http
GET http://localhost:8080/api/reportes/movimientos
```

[Abrir imagen de la prueba](evidenciasExamen/02_movimiento_sin_filtros_b.png)

![Movimientos sin filtros](evidenciasExamen/02_movimiento_sin_filtros_b.png)

### 3. Movimientos filtrados por tipo

```http
GET http://localhost:8080/api/reportes/movimientos?tipoMovimiento=SALIDA
```

[Abrir imagen de la prueba](evidenciasExamen/03_movimientos_por_tipo_b.png)

![Movimientos por tipo](evidenciasExamen/03_movimientos_por_tipo_b.png)

### 4. Movimientos filtrados por bodega

```http
GET http://localhost:8080/api/reportes/movimientos?bodegaId=1
```

[Abrir imagen de la prueba](evidenciasExamen/04_movimientos_por_bodega_b.png)

![Movimientos por bodega](evidenciasExamen/04_movimientos_por_bodega_b.png)

### 5. Movimientos filtrados por producto

```http
GET http://localhost:8080/api/reportes/movimientos?productoId=4
```

[Abrir imagen de la prueba](evidenciasExamen/05_movimientos_por_producto_b.png)

![Movimientos por producto](evidenciasExamen/05_movimientos_por_producto_b.png)

### 6. Movimientos filtrados por tipo y rango de fechas

```http
GET http://localhost:8080/api/reportes/movimientos?tipoMovimiento=SALIDA&fechaInicio=2026-07-01T00:00:00&fechaFin=2026-07-30T23:59:59
```

[Abrir imagen de la prueba](evidenciasExamen/06_movimiento_tipo_y_fecha_b.png)

![Movimientos por tipo y fecha](evidenciasExamen/06_movimiento_tipo_y_fecha_b.png)

### 7. Auditorías sin filtros

```http
GET http://localhost:8080/api/reportes/auditoria
```

[Abrir imagen de la prueba](evidenciasExamen/07_auditorias_sin_filtro_b.png)

![Auditorías sin filtros](evidenciasExamen/07_auditorias_sin_filtro_b.png)

### 8. Auditorías filtradas por producto

```http
GET http://localhost:8080/api/reportes/auditoria?productoId=1
```

[Abrir imagen de la prueba](evidenciasExamen/08_auditorias_por_producto_b.png)

![Auditorías por producto](evidenciasExamen/08_auditorias_por_producto_b.png)

### 9. Auditorías filtradas por campo modificado

```http
GET http://localhost:8080/api/reportes/auditoria?campoModificado=precio
```

[Abrir imagen de la prueba](evidenciasExamen/09_auditorias_por_campo_b.png)

![Auditorías por campo](evidenciasExamen/09_auditorias_por_campo_b.png)

### 10. Auditorías con combinación de filtros

```http
GET http://localhost:8080/api/reportes/auditoria?productoId=1&campoModificado=precio&fechaInicio=2026-07-01T00:00:00&fechaFin=2026-07-30T23:59:59
```

[Abrir imagen de la prueba](evidenciasExamen/10_auditorias_combinacion_filtros.png)

![Auditorías con filtros combinados](evidenciasExamen/10_auditorias_combinacion_filtros.png)

## Resultados obtenidos

Las pruebas realizadas en Thunder Client muestran:

- autenticación JWT correcta
- respuestas HTTP `200 OK`
- consulta general de movimientos
- filtro por tipo de movimiento
- filtro por bodega
- filtro por producto
- filtro por rango de fechas
- consulta general de auditorías
- filtro de auditorías por producto
- filtro por campo modificado
- combinación de filtros de auditoría

## Consideraciones técnicas

- Los filtros son opcionales.
- Cuando no se envía un parámetro, ese filtro no se aplica.
- Los rangos de fecha se validan en el servicio.
- La consulta de movimientos usa `DISTINCT` para evitar registros duplicados.
- El filtro de bodega busca tanto en la bodega de origen como en la bodega de destino.
- El filtro de producto en movimientos utiliza la relación con `DetalleMovimiento`.
- El campo modificado se busca dentro de `valoresAnteriores` y `valoresNuevos`, porque la entidad `Auditoria` no posee un atributo específico llamado `campoModificado`.
- Las rutas están protegidas y requieren un token JWT válido.

## Entregables

- Código fuente del módulo de reportes.
- Repositorios con consultas JPA.
- Servicio con validaciones y filtros.
- Controlador con endpoints GET.
- Evidencias de pruebas realizadas en Thunder Client.
- Repositorio del proyecto en GitHub.
