# consultar_stock_producto

Resultado: SUCCESS

## Entrada

```json
{
  "productoId": 1
}
```

## Respuesta real

```json
{
  "productoId": 1,
  "nombreProducto": "Filtro industrial",
  "stockTotal": 10,
  "stockPorBodega": [
    {
      "bodegaId": 1,
      "nombreBodega": "Bodega Central",
      "stock": 10
    },
    {
      "bodegaId": 2,
      "nombreBodega": "Bodega Norte",
      "stock": 0
    },
    {
      "bodegaId": 3,
      "nombreBodega": "Bodega Occidente",
      "stock": 0
    }
  ]
}
```
