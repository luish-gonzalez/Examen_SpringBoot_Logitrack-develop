# publicar_resumen

Resultado: SUCCESS

## Entrada

```json
{
  "resumen": {
    "fecha": "2026-08-31",
    "narrativa": "El producto en riesgo requiere seguimiento de la orden BORRADOR creada durante la integraci?n.",
    "alertas": [
      {
        "productoId": 1,
        "severidad": "ALTA",
        "titulo": "Producto en riesgo",
        "detalle": "El stock derivado est? por debajo del punto de reorden calculado."
      }
    ],
    "accionesSugeridas": [
      {
        "ordenId": 1,
        "tipo": "REVISAR_ORDEN",
        "descripcion": "Revisar la orden BORRADOR creada para atender el riesgo de inventario."
      }
    ]
  }
}
```

## Respuesta real

```json
{
  "fecha": "2026-08-31",
  "narrativa": "El producto en riesgo requiere seguimiento de la orden BORRADOR creada durante la integraci?n.",
  "alertas": [
    {
      "severidad": "ALTA",
      "titulo": "Producto en riesgo",
      "detalle": "El stock derivado est? por debajo del punto de reorden calculado.",
      "productoId": 1,
      "ordenId": null,
      "bodegaId": null
    }
  ],
  "accionesSugeridas": [
    {
      "tipo": "REVISAR_ORDEN",
      "descripcion": "Revisar la orden BORRADOR creada para atender el riesgo de inventario.",
      "ordenId": 1,
      "productoId": null,
      "bodegaId": null
    }
  ]
}
```
