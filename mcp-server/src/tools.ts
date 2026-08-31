import { z } from "zod";
import type { JsonValue, LogiTrackClient } from "./logitrack-client.js";

export const TOOL_NAMES = [
  "consultar_stock_producto",
  "consultar_bodegas_criticas",
  "consultar_productos_en_riesgo",
  "consultar_kpis",
  "crear_orden_borrador",
  "publicar_resumen"
] as const;

const resourceLinkSchema = z.object({
  productoId: z.number().int().optional(),
  ordenId: z.number().int().optional(),
  bodegaId: z.number().int().optional()
});

const precioUnitarioSchema = z.number().finite()
  .refine((value) => String(Math.trunc(Math.abs(value))).length <= 10,
    "El precio unitario debe tener máximo 10 enteros.")
  .refine((value) => Number.isInteger(value * 100),
    "El precio unitario debe tener máximo 2 decimales.");

const alertSchema = resourceLinkSchema.extend({
  severidad: z.enum(["BAJA", "MEDIA", "ALTA"]),
  titulo: z.string().min(1).max(150),
  detalle: z.string().min(1).max(500)
}).strict().refine(
  (alert) => alert.productoId !== undefined || alert.ordenId !== undefined || alert.bodegaId !== undefined,
  "Cada alerta debe enlazar al menos un producto, orden o bodega."
);

const actionSchema = resourceLinkSchema.extend({
  tipo: z.enum(["REVISAR_ORDEN", "REVISAR_PRODUCTO", "REVISAR_BODEGA"]),
  descripcion: z.string().min(1).max(500)
}).strict().refine(
  (action) => [action.productoId, action.ordenId, action.bodegaId]
    .filter((id) => id !== undefined).length === 1,
  "Cada acción sugerida debe enlazar exactamente un recurso."
);

export const toolSchemas = {
  consultar_stock_producto: z.object({ productoId: z.number().int().min(1) }).strict(),
  consultar_bodegas_criticas: z.object({}).strict(),
  consultar_productos_en_riesgo: z.object({}).strict(),
  consultar_kpis: z.object({}).strict(),
  crear_orden_borrador: z.object({
    productoId: z.number().int(),
    proveedorId: z.number().int(),
    bodegaDestinoId: z.number().int(),
    cantidad: z.number().int().min(1),
    precioUnitario: precioUnitarioSchema
  }).strict(),
  publicar_resumen: z.object({
    resumen: z.object({
      fecha: z.string().regex(/^\d{4}-\d{2}-\d{2}$/),
      narrativa: z.string().min(20).max(500),
      alertas: z.array(alertSchema),
      accionesSugeridas: z.array(actionSchema)
    }).strict()
  }).strict()
};

export type ToolName = typeof TOOL_NAMES[number];
export type ToolHandlers = Record<ToolName, (input: never) => Promise<JsonValue>>;

export function createToolHandlers(client: LogiTrackClient) {
  return {
    consultar_stock_producto: async (input: z.infer<typeof toolSchemas.consultar_stock_producto>) =>
      client.get(`/productos/${input.productoId}/stock`),
    consultar_bodegas_criticas: async () => client.get("/bodegas/criticas"),
    consultar_productos_en_riesgo: async () => client.get("/productos/riesgo"),
    consultar_kpis: async () => client.get("/kpis"),
    crear_orden_borrador: async (input: z.infer<typeof toolSchemas.crear_orden_borrador>) =>
      client.post("/ordenes", input),
    publicar_resumen: async (input: z.infer<typeof toolSchemas.publicar_resumen>) =>
      client.post("/panel/resumen", input.resumen)
  };
}

export function toMcpResult(payload: JsonValue) {
  return {
    content: [{ type: "text" as const, text: JSON.stringify(payload) }]
  };
}
