# Evidencia progresiva de SDD y TDD

Este documento se completa únicamente con ejecuciones y hashes reales. `PENDIENTE` significa que la actividad todavía no ocurrió.

## 1. Documentos de diseño

- [Propuesta](01-propuesta.md)
- [Especificación](02-especificacion.md)
- [Diseño](03-diseno.md)
- [Plan de tareas](04-tareas.md)

## 2. Baseline anterior al proceso obligatorio

| Hash | Commit | Alcance |
|---|---|---|
| `f899bc5` | `chore: prepare LogiTrack IQ baseline` | Preparación segura del proyecto heredado. **No es uno de los tres commits obligatorios de SDD/TDD.** |

## 3. Commits obligatorios

| Orden | Commit requerido | Hash | Estado |
|---:|---|---|---|
| 1 | `docs: define LogiTrack IQ scope` | `PENDIENTE` | Documentos creados, commit no autorizado aún |
| 2 | `test: define reorder and order-state rules` | `PENDIENTE` | Pruebas todavía no escritas |
| 3 | `feat: implement LogiTrack IQ rules` | `PENDIENTE` | Implementación todavía no iniciada |

## 4. Trazabilidad inicial: regla → prueba prevista

| Regla | Prueba prevista | Nivel |
|---|---|---|
| ENTRADA suma destino; SALIDA resta origen; TRANSFERENCIA hace ambos efectos | casos con una y varias bodegas | Unitaria de service |
| Todos los detalles participan | movimiento con varios `DetalleMovimiento` y productos | Unitaria de service |
| No existe stock negativo | salida/transferencia insuficiente no persiste | Unitaria + integración |
| Ventana de consumo de 30 días calendario, incluido hoy | bordes con `Clock` fijo y tipos distintos | Unitaria |
| Consumo 0 | cobertura `null`, `SIN_CONSUMO` | Unitaria obligatoria |
| Consumo mayor que 0 | `CON_CONSUMO`, reorden y cobertura exactos | Unitaria |
| Riesgo usa `<`, no `<=`, antes de redondear | stock igual y valores decimales cercanos | Unitaria obligatoria |
| Producto sin proveedor principal queda excluido | producto con consumo/saldo de riesgo sin proveedor | Unitaria |
| Bodega sugerida usa menor stock y luego menor id | empate entre bodegas | Unitaria |
| Ocupación crítica es `>= 90 %` | 89.99, 90 y más de 90 | Unitaria/MockMvc |
| Quiebre es stock total 0 | stock cero, positivo y saldo inválido rechazado | Unitaria |
| Órdenes por aprobar cuenta/suma solo `BORRADOR` | mezcla de estados y totales | Unitaria |
| Movimientos de ayer se cuentan por tipo | límites del día anterior en Bogotá | Unitaria |
| Cantidad de orden debe ser positiva | cero/negativa produce HTTP 400 | MockMvc obligatoria |
| Máquina de estados cerrada | cuatro transiciones válidas y restantes inválidas | Unitaria parametrizada |
| Cancelada no vuelve a aprobada | PATCH devuelve 400 | MockMvc obligatoria |
| Recibir aprobada crea ENTRADA atómicamente | éxito y rollback ante fallo | Integración obligatoria |
| `AGENTE` no cambia estados ni registra movimientos | intento autenticado devuelve 403 | Seguridad/MockMvc obligatoria |
| Resumen tiene contrato exacto e IDs existentes | severidad inválida, ID inexistente y campo extra | MockMvc obligatoria |
| Resumen inválido conserva el anterior | publicar válido y después inválido | Integración |
| Solo un resumen por fecha | dos publicaciones válidas actualizan la misma fila | Integración |
| PDF borrador tiene marca y se invalida al cambiar estado | inspección de bytes/texto, GET antes/después | Unitaria + MockMvc obligatoria |
| Endpoints protegidos distinguen 401, 403 y 404 | matriz de sesión, rol y recurso | Seguridad/MockMvc |

## 5. Evidencia roja

- Fecha/hora: `2026-08-28T13:48:12-05:00`.
- Commit o árbol de trabajo probado: árbol de trabajo sin commit, basado en `cf8f55e`, con la infraestructura y las pruebas TDD nuevas.
- Comando ejecutado: `.\mvnw.cmd test`.
- Pruebas que fallaron por ausencia de implementación: 21 ejecutadas, 21 fallos, 0 errores y 0 omitidas. Entre los fallos representativos están la ausencia de la calculadora de métricas, las rutas de órdenes, stock, riesgo y resumen todavía no implementadas, el uso actual de `Inventario` para validar movimientos y la falta de restricción de `AGENTE` sobre movimientos manuales.
- Resultado reproducible o enlace a evidencia: `BUILD FAILURE` de Surefire por fallos de aserción; el código de producción y las 11 fuentes de prueba compilaron, el perfil `test` inició correctamente con H2 en memoria y no hubo fallos de contexto ni de base de datos. Informes locales en `target/surefire-reports/`.

No se registrará como evidencia roja una falla de compilación accidental o de infraestructura; debe demostrar una regla aún no implementada.

## 6. Evidencia verde

- Fecha/hora: `PENDIENTE`.
- Commit o árbol de trabajo probado: `PENDIENTE`.
- Comando ejecutado: `PENDIENTE`.
- Resumen de pruebas aprobadas: `PENDIENTE`.
- Evidencia de integración y ausencia de regresiones: `PENDIENTE`.

## 7. Reflexión final (máximo 150 palabras)

`PENDIENTE: se redactará al finalizar. Si especificación e implementación no cambian, se consignará “No hubo cambios”.`
