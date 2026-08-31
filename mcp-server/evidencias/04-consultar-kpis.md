# consultar_kpis

Resultado: SUCCESS

## Entrada

```json
{}
```

## Respuesta real

```json
{
  "calculadoEn": "2026-08-31T00:28:39.2114614-05:00",
  "ocupacionPorBodega": [
    {
      "bodegaId": 1,
      "nombre": "Bodega Central",
      "capacidad": 500,
      "unidadesAlmacenadas": 30,
      "porcentaje": 6
    },
    {
      "bodegaId": 2,
      "nombre": "Bodega Norte",
      "capacidad": 300,
      "unidadesAlmacenadas": 60,
      "porcentaje": 20
    },
    {
      "bodegaId": 3,
      "nombre": "Bodega Occidente",
      "capacidad": 250,
      "unidadesAlmacenadas": 25,
      "porcentaje": 10
    }
  ],
  "productosEnQuiebre": 0,
  "productosEnRiesgo": 1,
  "ordenesPorAprobar": {
    "cantidad": 0,
    "montoTotal": 0
  },
  "movimientosAyer": {
    "entrada": 0,
    "salida": 1,
    "transferencia": 0
  }
}
```
