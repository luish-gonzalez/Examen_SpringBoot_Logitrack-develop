# Propuesta de LogiTrack IQ

## 1. Propósito del documento

Este documento define el problema, los objetivos y los límites de LogiTrack IQ. Junto con la especificación, el diseño y el plan de tareas, será la fuente funcional y verificable para el desarrollo y las pruebas.

Para evitar confusiones se emplean dos etiquetas:

- **Requisito del enunciado (REQ):** comportamiento obligatorio tomado del PDF oficial.
- **Decisión técnica (DEC):** elección de implementación adoptada para integrar el requisito con el proyecto heredado.

## 2. Problema actual

LogiTrack ya registra productos, bodegas y movimientos de inventario, pero la revisión es principalmente manual. El sistema no ofrece todavía una vista diaria que calcule el stock real desde los movimientos, detecte productos por debajo de su punto de reorden, prepare compras ni publique un resumen estructurado para un dashboard.

Además, el backend heredado mantiene una entidad `Inventario` con saldos almacenados. Para LogiTrack IQ esos saldos no pueden ser la fuente de verdad: las entradas, salidas, transferencias y todos sus detalles deben explicar el stock disponible.

## 3. Contexto del sistema heredado

El repositorio contiene una aplicación Spring Boot existente, organizada en entidades, repositories, services, controllers, DTOs, seguridad, auditoría y manejo global de excepciones. Ya dispone de:

- autenticación JWT y Spring Security;
- usuarios con roles;
- `Producto`, `Bodega`, `Movimiento` y `DetalleMovimiento`;
- persistencia JPA;
- auditoría y excepciones comunes;
- endpoints y una interfaz web del reto anterior;
- reportes de movimientos y auditoría.

**REQ:** LogiTrack IQ extiende este backend. No se crea un backend independiente ni se reemplazan innecesariamente las funciones existentes.

## 4. Objetivo general

Extender LogiTrack con una torre de control de inventario que calcule existencias desde los movimientos, identifique riesgo de desabastecimiento, gestione órdenes de compra, registre su recepción y publique información verificable para automatización y visualización.

## 5. Objetivos específicos

1. Calcular stock por bodega y stock total usando `Movimiento` y `DetalleMovimiento`.
2. Evitar salidas o transferencias que produzcan stock negativo.
3. Calcular consumo diario promedio, punto de reorden y días de cobertura en `America/Bogota`.
4. Identificar productos en riesgo y sugerir la bodega destino según el menor stock.
5. Incorporar proveedores, órdenes de compra y resúmenes del panel.
6. Controlar las transiciones de orden y recibir una orden en una única transacción.
7. Generar y conservar un PDF coherente con el estado actual de la orden.
8. Aplicar permisos diferenciados para `ADMIN` y `AGENTE` reutilizando JWT.
9. Exponer contratos REST documentados y comprobables.
10. Construir posteriormente MCP, skill, n8n y dashboard como consumidores de la API.
11. Producir evidencia verificable de SDD y TDD mediante documentos, pruebas rojas y verdes y commits trazables.

## 6. Actores

### 6.1 ADMIN

Usuario humano responsable de la administración. Puede consultar los indicadores IQ, crear borradores, publicar resúmenes, registrar movimientos manuales y aprobar, recibir o cancelar órdenes.

### 6.2 AGENTE

Usuario técnico usado por MCP y n8n. Puede consultar KPIs, stock, riesgos y bodegas críticas; crear órdenes en `BORRADOR`; y publicar el resumen. No puede aprobar, recibir o cancelar órdenes ni registrar movimientos manuales.

## 7. Flujo funcional de extremo a extremo

```text
producto en riesgo
→ n8n/MCP crea BORRADOR
→ ADMIN aprueba
→ ADMIN recibe
→ movimiento ENTRADA
→ stock actualizado
→ dashboard actualizado
```

El flujo demuestra que la recomendación se origina en datos reales, que la automatización tiene permisos limitados y que la recepción modifica el inventario mediante el mismo registro de movimientos usado como fuente de verdad.

## 8. Alcance

### 8.1 Backend

- Adaptar el modelo heredado e incorporar `Proveedor`, `OrdenCompra` y `ResumenPanel`.
- Agregar `proveedorPrincipal` opcional a `Producto`.
- Calcular stock y métricas desde movimientos con múltiples detalles.
- Implementar KPIs, productos en riesgo y bodegas críticas.
- Gestionar órdenes, estados, recepción transaccional y PDF.
- Validar y almacenar un resumen estructurado por fecha.
- Agregar el rol `AGENTE`, su matriz de permisos y las auditorías obligatorias.
- Mantener los endpoints heredados y agregar los endpoints IQ.
- Adaptar posteriormente persistencia y datos reproducibles a MySQL.
- Crear pruebas unitarias, de seguridad y de integración antes de implementar las reglas.

### 8.2 Integraciones posteriores

- Servidor MCP con exactamente seis herramientas y acceso exclusivo por API REST.
- Skill operativa para restringir la automatización.
- Un flujo n8n diario a las 6:00 a. m. en `America/Bogota`.
- Dashboard HTML, CSS y JavaScript sin framework, conectado a la API real.
- Evidencias, README actualizado y video de demostración de 4 a 6 minutos.

## 9. Fuera de alcance

- Crear un segundo backend para LogiTrack IQ.
- Permitir que MCP, n8n o el dashboard consulten o modifiquen MySQL directamente.
- Aprobar, recibir o cancelar órdenes desde MCP o desde el rol `AGENTE`.
- Crear órdenes con varios productos; cada `OrdenCompra` contiene exactamente uno.
- Usar `Producto.stock` o `Inventario.stock` como fuente de verdad de los cálculos IQ.
- Analizar semánticamente la narrativa del resumen mediante IA; solo se valida su contrato.
- Desarrollar una aplicación móvil, un framework frontend o animaciones avanzadas.
- Implementar los componentes MCP, skill, n8n o dashboard antes de completar y probar el backend.

## 10. Decisiones técnicas de alcance

- **DEC-01:** la versión objetivo utilizará MySQL; los scripts PostgreSQL heredados se adaptarán en una fase posterior.
- **DEC-02:** `Movimiento` y `DetalleMovimiento` serán la única fuente de verdad para stock y validaciones IQ.
- **DEC-03:** `Inventario` podrá permanecer temporalmente por compatibilidad, pero su endpoint deberá devolver saldos derivados y ningún servicio IQ dependerá de `Inventario.stock`.
- **DEC-04:** se inyectará un `Clock` configurado con `America/Bogota` para hacer deterministas los límites diarios y las pruebas.
- **DEC-05:** el dinero usará `BigDecimal`, escala 2 y `HALF_UP`. Las métricas conservarán precisión interna suficiente y se presentarán con escala 2; el riesgo se comparará antes del redondeo de presentación.
- **DEC-06:** el PDF se generará con Apache PDFBox y se almacenará como `@Lob` en la orden.
- **DEC-07:** `estadoCobertura` tendrá `SIN_CONSUMO` cuando el consumo sea cero y `CON_CONSUMO` cuando sea mayor que cero.
- **DEC-08:** `/productos/riesgo` añadirá `precioUnitarioSugerido`, tomado de `Producto.precio`, sin eliminar ninguno de los campos exigidos.

## 11. Criterios generales de aceptación

- El backend heredado sigue compilando y conserva sus responsabilidades principales.
- Todo stock mostrado por IQ puede reconstruirse desde movimientos y detalles.
- Ninguna salida o transferencia aceptada deja saldo negativo.
- Un producto aparece en riesgo solo si tiene proveedor principal y `stockTotal < puntoReorden`.
- El empate `stockTotal == puntoReorden` no genera riesgo.
- La recepción aprobada crea una entrada y actualiza la orden atómicamente.
- `AGENTE` recibe 403 al intentar operaciones reservadas a `ADMIN`.
- Los endpoints devuelven 400, 401, 403 y 404 conforme al contrato.
- El PDF `BORRADOR` contiene la marca de agua y se invalida al cambiar el estado.
- Un resumen inválido no reemplaza el último resumen válido.
- Las reglas nuevas cuentan primero con pruebas en rojo y finalmente con pruebas verdes.
- MCP, n8n y dashboard consumen la API; no duplican reglas de negocio.

## 12. Resultado esperado

El proyecto permitirá demostrar con datos reproducibles un producto en riesgo, la creación limitada de una orden en borrador, su aprobación y recepción por un administrador, el movimiento de entrada resultante y la actualización del dashboard. La trazabilidad entre requisito, prueba, implementación y evidencia quedará registrada en `docs/sdd/`.
