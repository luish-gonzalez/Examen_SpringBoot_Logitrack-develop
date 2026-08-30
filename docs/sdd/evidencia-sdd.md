# Evidencia progresiva de SDD y TDD

Este documento registra únicamente commits y ejecuciones reales de LogiTrack IQ.

## Documentos de diseño

- [Propuesta](01-propuesta.md)
- [Especificación](02-especificacion.md)
- [Diseño](03-diseno.md)
- [Plan de tareas](04-tareas.md)

## Baseline previo

| Hash | Commit | Alcance |
|---|---|---|
| `f899bc5` | `chore: prepare LogiTrack IQ baseline` | Preparación segura del proyecto heredado. **No forma parte de los tres commits obligatorios.** |

## Commits SDD/TDD obligatorios

| Orden | Commit | Hash | Estado |
|---:|---|---|---|
| 1 | `docs: define LogiTrack IQ scope` | `cf8f55e` | Completado |
| 2 | `test: define reorder and order-state rules` | `8ad2efd` | Completado |
| 3 | `feat: implement LogiTrack IQ rules` | `4732fc6` | Completado |

Commit complementario de TDD: `81de3c3 test: cover remaining LogiTrack IQ rules`. Complementa la segunda ronda de pruebas y no sustituye ninguno de los tres commits obligatorios.

## Evidencia TDD

| Fase | Fuente | Resultado real |
|---|---|---|
| Rojo inicial | [`../evidencias/tdd-rojo.txt`](../evidencias/tdd-rojo.txt) | `Tests run: 21`, `Failures: 21`, `Errors: 0`, `Skipped: 0`, `BUILD FAILURE` |
| Rojo fase 2 | [`../evidencias/tdd-rojo-fase2.txt`](../evidencias/tdd-rojo-fase2.txt) | `Tests run: 33`, `Failures: 8`, `Errors: 0`, `Skipped: 0`, `BUILD FAILURE` |
| Verde final | [`../evidencias/tdd-verde.txt`](../evidencias/tdd-verde.txt) | `Tests run: 34`, `Failures: 0`, `Errors: 0`, `Skipped: 0`, `BUILD SUCCESS`; `./mvnw.cmd -DskipTests package`: `BUILD SUCCESS` |

## Trazabilidad: regla → prueba

| Regla comprobada | Clase y prueba real |
|---|---|
| Consumo 0 → cobertura `null` y `SIN_CONSUMO` | `CalculadoraMetricasInventarioTest#consumoCeroProduceCoberturaNulaYEstadoSinConsumo` |
| `stockTotal == puntoReorden` no está en riesgo | `ProductoRiesgoIntegrationTest#stockIgualAlPuntoDeReordenNoApareceEnRiesgo` |
| Cantidad de orden 0 o negativa → 400 | `OrdenCompraIntegrationTest#cantidadNoPositivaRespondeBadRequest` |
| CANCELADA → APROBADA → 400 | `OrdenCompraIntegrationTest#ordenCanceladaNoPuedeVolverAprobada` |
| APROBADA → RECIBIDA crea movimiento ENTRADA | `OrdenCompraIntegrationTest#recibirOrdenAprobadaCreaMovimientoEntrada` |
| AGENTE intenta aprobar → 403 | `OrdenCompraSecurityIntegrationTest#agenteNoPuedeAprobarOrden` |
| Resumen inválido → 400 y conserva el anterior | `ResumenPanelIntegrationTest#severidadInvalidaRespondeBadRequestYConservaResumenAnterior`; `#identificadorInexistenteRespondeBadRequestYConservaResumenAnterior` |
| PDF BORRADOR: marca, almacenamiento e invalidación | `OrdenCompraPdfIntegrationTest#pdfBorradorSeGuardaContieneMarcaYSeInvalidaAlCambiarEstado` |
| Stock derivado, todos los tipos y sin usar Inventario como verdad | `ProductoStockIntegrationTest#calculaStockDesdeTodosLosDetallesYTiposSinUsarInventario` |
| Múltiples detalles se validan antes de persistir | `MovimientoStockValidationIntegrationTest#validaTodosLosDetallesAntesDePersistirElMovimiento` |
| Stock negativo en salida y transferencia se rechaza | `MovimientoStockValidationIntegrationTest#salidaRechazaSaldoDerivadoInsuficienteAunqueInventarioIndiqueCien`; `#transferenciaRechazaSaldoDerivadoInsuficiente` |
| Precisión de `1/30` sin redondeo intermedio | `ProductoRiesgoVentanaPrecisionIntegrationTest#salidaUnitariaCalculaUnTreintavoSinErrorNiRedondeoIntermedio` |
| Ventana de 30 días: hoy y hace 29 incluidos; hace 30 excluido | `ProductoRiesgoVentanaPrecisionIntegrationTest#ventanaIncluyeHoyYHaceVeintinueveDiasPeroExcluyeHaceTreintaDias` |
| KPIs calculados desde movimientos | `KpisIntegrationTest#devuelveEstructuraYMetricasCalculadasDesdeMovimientos` |
| Bodegas críticas con ocupación ≥ 90 % | `BodegaCriticaIntegrationTest#soloIncluyeOcupacionIgualOMayorAlNoventaPorCiento` |
| Matriz ADMIN/AGENTE/EMPLEADO en consultas IQ | `ConsultaIqSecurityIntegrationTest#stockRespetaMatrizDeConsultaIq`; `#riesgoRespetaMatrizDeConsultaIq`; `#proveedoresRespetaMatrizDeConsultaIq`; `#kpisExigeRutaFuncionalAntesDePoderValidarSuMatriz`; `#bodegasCriticasExigeRutaFuncionalAntesDePoderValidarSuMatriz` |
| Reemplazo del ResumenPanel y auditoría | `ResumenPanelReemplazoAuditoriaIntegrationTest#segundoResumenValidoReemplazaLaUnicaFilaYDejaAuditoria` |
| Rollback de recepción | `OrdenCompraRecepcionAtomicidadAuditoriaIntegrationTest#falloDuranteEntradaAutomaticaRevierteEstadoYNoDejaPersistenciaParcial` |
| Auditoría de recepción exitosa | `OrdenCompraRecepcionAtomicidadAuditoriaIntegrationTest#recepcionExitosaCreaEntradaYCreaAuditoriaDeMovimientoYOrden` |
| OpenAPI expone rutas IQ y bearer JWT | `OpenApiIqIntegrationTest#exponeRutasIqYBearerJwt` |

## Reflexión final

El backend heredado usaba `Inventario.stock` como referencia operativa. LogiTrack IQ cambió los cálculos nuevos a movimientos y detalles, conservando Inventario solo como compatibilidad. La primera ronda TDD produjo 21 fallos funcionales sin errores de infraestructura. Durante la auditoría posterior apareció el caso de precisión `1/30`; por ello se añadió una segunda ronda TDD para ventana temporal, KPIs, bodegas críticas, seguridad, reemplazo de resumen y recepción atómica. La implementación hizo verdes las 34 pruebas sin reescribir su intención. Finalmente, los scripts heredados de PostgreSQL se adaptaron a MySQL para la ejecución de producción, mientras el perfil de pruebas quedó aislado con H2 en modo MySQL.
