# AGENTS.md — LogiTrack IQ

## Contexto del proyecto

Este repositorio contiene un proyecto académico existente llamado LogiTrack,
desarrollado con Spring Boot.

El nuevo proyecto "LogiTrack IQ" NO debe construirse como un backend nuevo.

Debe extenderse el proyecto existente respetando su arquitectura, entidades,
seguridad, auditoría, convenciones y funcionalidades anteriores siempre que
sea posible.

El enunciado oficial del nuevo proyecto está en:

docs/Proyecto IA2 - LogiTrack IQ.pdf

Ese documento es la fuente principal de requisitos.

---

## Forma de trabajo

Este proyecto será desarrollado por etapas.

No implementar todo el proyecto de una sola vez.

Antes de modificar archivos:

1. Inspeccionar primero la implementación existente relacionada.
2. Explicar qué archivos se necesitarían modificar.
3. Explicar por qué se requiere el cambio.
4. Esperar autorización del estudiante cuando se solicite explícitamente.

Realizar cambios pequeños y verificables.

No reemplazar componentes existentes innecesariamente.

No reorganizar completamente el proyecto salvo que sea estrictamente necesario.

---

## SDD y TDD

El proyecto exige evidencia verificable de SDD y TDD.

Por lo tanto, respetar estrictamente este orden:

1. Crear documentación SDD.
2. Commit de documentación.
3. Crear pruebas de las reglas nuevas.
4. Ejecutar las pruebas y obtener evidencia inicial fallando.
5. Commit de pruebas.
6. Implementar las reglas.
7. Ejecutar las pruebas hasta obtener resultado verde.
8. Commit de implementación.

NO implementar las reglas de LogiTrack IQ antes de crear las pruebas
correspondientes.

Los commits obligatorios son, en este orden:

1. docs: define LogiTrack IQ scope
2. test: define reorder and order-state rules
3. feat: implement LogiTrack IQ rules

NO crear commits automáticamente.

NO ejecutar `git commit`, `git push`, rebase, reset, force push ni modificar
el historial de Git sin autorización explícita del estudiante.

---

## Backend existente

Antes de crear nuevas clases, verificar si ya existe una implementación que
pueda reutilizarse.

Se deben reutilizar especialmente cuando corresponda:

- Spring Security
- JWT
- usuarios
- roles
- auditoría
- manejo global de excepciones
- Producto
- Bodega
- Movimiento
- DetalleMovimiento
- repositories
- services
- controllers existentes

---

## Regla fundamental de inventario

Para las nuevas funciones de LogiTrack IQ:

El stock se calcula desde los movimientos registrados.

No utilizar Producto.stock ni un stock almacenado como fuente de verdad para
los cálculos nuevos.

Reglas:

ENTRADA:
suma unidades a la bodega destino.

SALIDA:
resta unidades de la bodega origen.

TRANSFERENCIA:
resta unidades de la bodega origen y suma la misma cantidad a la bodega destino.

Cada movimiento puede contener varios detalles y todos deben ser considerados.

El stock total de un producto es la suma de sus existencias en todas las
bodegas.

No debe permitirse una salida o transferencia que produzca stock negativo.

---

## Reglas de LogiTrack IQ

Zona horaria:

America/Bogota

Consumo diario promedio:

unidades SALIDA de los últimos 30 días calendario / 30

Punto de reorden:

consumoDiarioPromedio * diasEntrega * 1.5

Días de cobertura:

stockTotal / consumoDiarioPromedio

Si consumoDiarioPromedio es 0:

diasCobertura = null
estadoCobertura = SIN_CONSUMO

Un producto está en riesgo solamente cuando:

stockTotal < puntoReorden

Si stockTotal == puntoReorden, NO está en riesgo.

Un producto sin proveedor principal no puede aparecer en riesgo ni generar
una orden automática.

---

## Nuevas entidades principales

El proyecto deberá incorporar o adaptar:

- Proveedor
- OrdenCompra
- ResumenPanel

Producto deberá tener una relación opcional con:

proveedorPrincipal

Estados de una OrdenCompra:

- BORRADOR
- APROBADA
- RECIBIDA
- CANCELADA

Transiciones válidas:

BORRADOR -> APROBADA
BORRADOR -> CANCELADA
APROBADA -> RECIBIDA
APROBADA -> CANCELADA

RECIBIDA y CANCELADA son estados finales.

Una transición inválida debe responder HTTP 400.

---

## Recepción de órdenes

Al pasar:

APROBADA -> RECIBIDA

el backend debe crear automáticamente un movimiento ENTRADA para:

- producto de la orden
- cantidad de la orden
- bodegaDestino de la orden

El cambio de estado y el movimiento deben ejecutarse en una única transacción.

---

## Seguridad

Mantener JWT y Spring Security del proyecto anterior.

Agregar rol:

AGENTE

AGENTE puede:

- consultar KPIs
- consultar stock
- consultar productos en riesgo
- consultar bodegas críticas
- crear órdenes BORRADOR
- publicar resumen del panel

AGENTE NO puede:

- aprobar órdenes
- recibir órdenes
- cancelar órdenes
- registrar movimientos manuales

Estas operaciones requieren ADMIN.

---

## API requerida

El proyecto deberá incorporar:

GET /kpis

GET /productos/{id}/stock

GET /productos/riesgo

GET /bodegas/criticas

GET /proveedores

GET /ordenes
POST /ordenes
GET /ordenes/{id}

POST /ordenes/{id}/pdf
GET /ordenes/{id}/pdf

PATCH /ordenes/{id}/estado

POST /panel/resumen
GET /panel/resumen

---

## Componentes posteriores

Después de completar y probar el backend se desarrollarán:

- mcp-server/
- skills/operacion-logitrack/SKILL.md
- n8n/resumen-diario-inventario.json
- frontend/

No comenzar estos componentes hasta que se complete la etapa correspondiente.

---

## Objetivo educativo

El estudiante debe comprender lo que se modifica.

Al proponer un cambio importante:

- explicar el concepto
- mostrar qué parte del proyecto existente se reutiliza
- explicar el motivo de la implementación
- evitar generar grandes cantidades de código sin explicación
