# Checklist de entrega — LogiTrack IQ

| requisito | estado | ubicación/evidencia |
| --- | --- | --- |
| Repositorio Git con historial SDD/TDD | ✅ COMPLETO | `git log`; commits `docs: define...`, `test: define...`, `feat: implement...` |
| Datos reproducibles | ✅ COMPLETO | `src/main/resources/schema.sql`, `data.sql` y README |
| Swagger / OpenAPI JWT | ✅ COMPLETO | `/swagger-ui/index.html`, `/v3/api-docs`, `OpenApiIqIntegrationTest` |
| Documentación SDD | ✅ COMPLETO | `docs/sdd/01-propuesta.md` a `04-tareas.md` |
| TDD rojo y verde | ✅ COMPLETO | `docs/evidencias/tdd-rojo*.txt`, `tdd-verde.txt` y 35 pruebas actuales |
| MCP: seis tools | ✅ COMPLETO | `mcp-server/src/tools.ts`, `mcp-server/src/server.ts` |
| Evidencia real de las seis tools | ✅ COMPLETO | `mcp-server/evidencias/01-...` a `06-...` |
| Skill operativo | ✅ COMPLETO | `skills/operacion-logitrack/SKILL.md` |
| Export n8n | ✅ COMPLETO | `n8n/resumen-diario-inventario.json`, validado por importación en n8n 2.36.8 |
| Captura n8n de ejecución exitosa | ✅ COMPLETO | `n8n/evidencias/ejecucion-exitosa.png.png` |
| Captura n8n de error controlado | ✅ COMPLETO | `n8n/evidencias/ejecucion-error-controlado.png` y capturas auxiliares |
| Frontend torre de control | ✅ COMPLETO | `src/main/resources/static/`, validación manual registrada |
| PDF de orden | ✅ COMPLETO | `PdfOrdenService`, `OrdenCompraPdfIntegrationTest` |
| Arquitectura | ✅ COMPLETO | `docs/arquitectura-logitrack-iq.md` |
| Video de 4–6 min | ⚠️ PENDIENTE | Grabar y adjuntar/enlazar fuera del repositorio si la entrega lo requiere |
| GitHub / push | ⚠️ PENDIENTE | Configurar remoto y publicar solo con autorización explícita |
| Capturas frontend | ✅ COMPLETO | `docs/evidencias/frontend-admin-aprobacion.png`, `frontend-admin-dashboard.png`, `frontend-agente-dashboard.png` y `frontend-pdf-borrador.png` |
