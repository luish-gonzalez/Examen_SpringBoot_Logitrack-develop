# Tareas de LogiTrack IQ

Las tareas son consecutivas, pequeñas y verificables. Una casilla solo se marca cuando existe evidencia. Las pruebas de cada regla están planificadas antes de su implementación.

## Fase 1. SDD

- [x] T001 Preparar y compilar el baseline seguro; evidencia: commit `f899bc5`.
- [x] T002 Leer `AGENTS.md`, el PDF oficial y la arquitectura heredada relacionada.
- [x] T003 Crear `docs/sdd/01-propuesta.md` con problema, objetivos, alcance y actores.
- [x] T004 Crear `docs/sdd/02-especificacion.md` con reglas, API y aceptación.
- [x] T005 Crear `docs/sdd/03-diseno.md` con arquitectura, entidades y decisiones.
- [x] T006 Crear este plan y `docs/sdd/evidencia-sdd.md`.
- [x] T007 Revisar en conjunto los cinco documentos y corregir solo inconsistencias SDD.
- [ ] T008 Con autorización, crear el commit `docs: define LogiTrack IQ scope` y registrar su hash.

## Fase 2. Preparación TDD

- [ ] T009 Inspeccionar dependencias y configuración de pruebas existentes en `pom.xml` y `src/test`.
- [ ] T010 Diseñar el perfil aislado de pruebas en `src/test/resources` sin base externa.
- [ ] T011 Incorporar únicamente dependencias de prueba imprescindibles en `pom.xml`.
- [ ] T012 Crear fábricas/fixtures reutilizables para productos, bodegas, movimientos, usuarios y órdenes.
- [ ] T013 Configurar un `Clock` fijo sustituible en pruebas.
- [ ] T014 Ejecutar las pruebas heredadas y registrar su estado previo.

## Fase 3. Pruebas en rojo

- [ ] T015 Escribir pruebas unitarias de stock para ENTRADA, SALIDA y TRANSFERENCIA en `src/test/.../services`.
- [ ] T016 Probar que todos los `DetalleMovimiento` participan y que el total suma todas las bodegas.
- [ ] T017 Probar rechazo de SALIDA y TRANSFERENCIA que producirían stock negativo.
- [ ] T018 Probar ventana de 30 días, inclusión de hoy y exclusión de movimientos no `SALIDA`.
- [ ] T019 Probar consumo cero: cobertura `null` y `SIN_CONSUMO`.
- [ ] T020 Probar consumo positivo: `CON_CONSUMO`, punto de reorden y cobertura.
- [ ] T021 Probar que la comparación de riesgo ocurre antes del redondeo y que igualdad no implica riesgo.
- [ ] T022 Probar exclusión de producto sin proveedor principal y sugerencia de bodega con desempate por id.
- [ ] T023 Probar ocupación, límite crítico de 90 %, quiebre y saldos cero.
- [ ] T024 Probar órdenes `BORRADOR`: conteo, suma monetaria, cantidad inválida y total calculado.
- [ ] T025 Probar movimientos de ayer por tipo y límites de `America/Bogota`.
- [ ] T026 Probar todas las transiciones válidas e inválidas, incluida cancelada → aprobada.
- [ ] T027 Probar que `APROBADA → RECIBIDA` crea una ENTRADA con el detalle correcto.
- [ ] T028 Probar rollback conjunto si falla la recepción transaccional.
- [ ] T029 Probar generación/reemplazo del PDF, marca `BORRADOR` e invalidación tras transición.
- [ ] T030 Probar contrato exacto del resumen, enumeraciones, longitudes, IDs y ausencia de campos extra.
- [ ] T031 Probar que un resumen inválido conserva el último válido y que publicar hoy actualiza una fila.
- [ ] T032 Escribir pruebas de seguridad: no autenticado 401, `AGENTE` al aprobar 403 y matriz de permisos.
- [ ] T033 Escribir pruebas MockMvc de los endpoints y códigos 400/404 principales.
- [ ] T034 Escribir al menos una integración Spring Boot/MockMvc para PATCH de estado o POST de resumen.
- [ ] T035 Ejecutar la suite nueva, conservar evidencia roja y autorizar el commit `test: define reorder and order-state rules`.

## Fase 4. Modelo y persistencia

- [ ] T036 Agregar enums de estado de orden y cobertura en `com.logitrack.enums`.
- [ ] T037 Crear `Proveedor` y su repository con validación de `diasEntrega`.
- [ ] T038 Relacionar opcionalmente `Producto.proveedorPrincipal`.
- [ ] T039 Crear `OrdenCompra`, relaciones, dinero y `@Lob` del PDF.
- [ ] T040 Crear `ResumenPanel` con unicidad por fecha y su repository.
- [ ] T041 Añadir repositories y consultas de orden requeridas.

## Fase 5. Stock derivado

- [ ] T042 Implementar agregación de stock por bodega y total desde `Movimiento`/`DetalleMovimiento`.
- [ ] T043 Integrar la validación de no negatividad en `MovimientoService` dentro de transacción.
- [ ] T044 Exponer `GET /productos/{id}/stock` mediante DTO, service y controller.
- [ ] T045 Adaptar posteriormente el endpoint heredado de inventario para reflejar saldos derivados.

## Fase 6. KPIs y productos en riesgo

- [ ] T046 Implementar el `Clock` de `America/Bogota` en `com.logitrack.config`.
- [ ] T047 Implementar consumo, reorden, cobertura y estados sin redondear antes de comparar.
- [ ] T048 Implementar producto en riesgo, proveedor principal y bodega sugerida determinista.
- [ ] T049 Implementar ocupación, bodegas críticas, productos en quiebre y movimientos de ayer.
- [ ] T050 Implementar órdenes por aprobar y redondeo de presentación.
- [ ] T051 Exponer `GET /kpis`, `GET /productos/riesgo` y `GET /bodegas/criticas`.
- [ ] T052 Exponer `GET /proveedores`.

## Fase 7. Órdenes

- [ ] T053 Crear DTOs de orden y PATCH exacto en `com.logitrack.dto`.
- [ ] T054 Implementar creación `BORRADOR`, usuario, fecha, cantidad y total en `OrdenCompraService`.
- [ ] T055 Implementar consulta/listado y filtro opcional por estado.
- [ ] T056 Implementar la máquina de estados y mensajes de transición inválida.
- [ ] T057 Exponer GET/POST de órdenes y PATCH de estado en `OrdenCompraController`.

## Fase 8. Recepción transaccional

- [ ] T058 Reutilizar `MovimientoService` para construir la ENTRADA automática con un detalle.
- [ ] T059 Unir `APROBADA → RECIBIDA` y la ENTRADA en un método `@Transactional`.
- [ ] T060 Verificar manualmente rollback, auditoría y stock derivado tras recepción.

## Fase 9. PDF

- [ ] T061 Incorporar Apache PDFBox en `pom.xml`.
- [ ] T062 Implementar contenido, marca diagonal semitransparente y almacenamiento `@Lob`.
- [ ] T063 Implementar reemplazo e invalidación de PDF/fecha al cambiar estado.
- [ ] T064 Exponer POST/GET `/ordenes/{id}/pdf` como `application/pdf` y 404 cuando corresponda.

## Fase 10. Resumen del panel

- [ ] T065 Crear DTOs cerrados para fecha, narrativa, alertas y `accionesSugeridas`.
- [ ] T066 Implementar validación de estructura, longitudes, enums e IDs existentes.
- [ ] T067 Implementar publicación transaccional insert/update por fecha sin perder el último válido ante error.
- [ ] T068 Implementar consulta del resumen más reciente.
- [ ] T069 Exponer POST/GET `/panel/resumen` y auditar publicación/reemplazo.

## Fase 11. Seguridad y auditoría

- [ ] T070 Agregar el rol `AGENTE` sin habilitarlo en registro público.
- [ ] T071 Configurar permisos JWT/Spring Security para todos los endpoints nuevos y movimientos.
- [ ] T072 Registrar en auditoría creación de orden, resumen, transición y recepción.
- [ ] T073 Completar respuestas globales 400, 401, 403 y 404 sin filtrar detalles internos.

## Fase 12. Datos reproducibles y Swagger

- [ ] T074 Adaptar scripts PostgreSQL heredados al destino MySQL.
- [ ] T075 Crear datos reproducibles de proveedores, productos, bodegas, usuarios y movimientos iniciales.
- [ ] T076 Documentar contratos protegidos, DTOs y errores en Swagger/OpenAPI.
- [ ] T077 Verificar arranque limpio y carga repetible en MySQL.

## Fase 13. Pruebas verdes

- [ ] T078 Ejecutar unitarias y corregir solo la implementación hasta obtener verde.
- [ ] T079 Ejecutar pruebas de seguridad y MockMvc hasta obtener verde.
- [ ] T080 Ejecutar integración transaccional y suite heredada completa.
- [ ] T081 Registrar evidencia verde, cobertura de aceptación y ausencia de regresiones.
- [ ] T082 Con autorización, crear `feat: implement LogiTrack IQ rules` y registrar su hash.

## Fase 14. MCP

- [ ] T083 Crear `mcp-server/` con configuración por variables de entorno y autenticación `AGENTE`.
- [ ] T084 Implementar exactamente las cuatro herramientas de consulta definidas.
- [ ] T085 Implementar `crear_orden_borrador` y `publicar_resumen` mediante REST.
- [ ] T086 Verificar seis herramientas, respuestas, errores y ausencia de acceso directo a MySQL.

## Fase 15. Skill

- [ ] T087 Crear `skills/operacion-logitrack/SKILL.md` con orden de consultas y límites operativos.
- [ ] T088 Verificar que prohíbe aprobar/cancelar/recibir, limita una orden y exige JSON válido.

## Fase 16. n8n

- [ ] T089 Crear un único flujo `Resumen diario de inventario` con cron 06:00 `America/Bogota`.
- [ ] T090 Conectar AI Agent a MCP y adaptar las instrucciones de la skill.
- [ ] T091 Implementar selección del primer riesgo y cantidad `ceil(max(1, puntoReorden × 2 - stockTotal))`.
- [ ] T092 Publicar el resumen y registrar salidas controladas de éxito y error.
- [ ] T093 Exportar `n8n/resumen-diario-inventario.json` y probar ejecución manual.

## Fase 17. Dashboard

- [ ] T094 Crear `frontend/` en HTML, CSS y JavaScript sin framework.
- [ ] T095 Implementar login JWT con almacenamiento exclusivo en `sessionStorage`.
- [ ] T096 Mostrar KPIs, movimientos, ocupación, resumen, riesgos y borradores desde API real.
- [ ] T097 Implementar generación/visualización del PDF `BORRADOR`.
- [ ] T098 Mostrar el botón Aprobar solo a `ADMIN` y refrescar la tabla tras aprobar.

## Fase 18. Evidencias, README y video

- [ ] T099 Completar `evidencia-sdd.md` con hashes reales y evidencia roja/verde.
- [ ] T100 Añadir evidencias MCP, n8n, seguridad, PDF y flujo completo sin secretos.
- [ ] T101 Actualizar README con instalación MySQL, ejecución, usuarios de prueba y rutas.
- [ ] T102 Verificar Swagger, estructura y entregables contra el PDF.
- [ ] T103 Grabar video de 4 a 6 minutos sin mostrar código, demostrando el flujo completo.
- [ ] T104 Escribir la reflexión final de máximo 150 palabras y hacer revisión final reproducible.
