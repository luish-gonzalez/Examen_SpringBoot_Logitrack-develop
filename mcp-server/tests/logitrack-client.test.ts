import { describe, expect, it, vi } from "vitest";
import type { McpConfig } from "../src/config.js";
import { LogiTrackClient } from "../src/logitrack-client.js";
import { createToolHandlers, TOOL_NAMES, toolSchemas } from "../src/tools.js";

const config: McpConfig = {
  LOGITRACK_API_BASE_URL: "http://api.test",
  LOGITRACK_AGENT_USERNAME: "agente",
  LOGITRACK_AGENT_PASSWORD: "no-se-guarda-en-el-test",
  MCP_PORT: 3001
};

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}

describe("LogiTrack MCP tools", () => {
  it("expone exactamente las seis herramientas permitidas", () => {
    expect(TOOL_NAMES).toEqual([
      "consultar_stock_producto", "consultar_bodegas_criticas", "consultar_productos_en_riesgo",
      "consultar_kpis", "crear_orden_borrador", "publicar_resumen"
    ]);
    expect(TOOL_NAMES).not.toContain("aprobar_orden");
    expect(TOOL_NAMES).not.toContain("registrar_movimiento");
  });

  it("envía GET autenticados para las cuatro consultas", async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(jsonResponse({ token: "jwt-de-prueba" }))
      .mockImplementation(() => Promise.resolve(jsonResponse({ ok: true })));
    const handlers = createToolHandlers(new LogiTrackClient(config, fetchMock));

    await handlers.consultar_stock_producto({ productoId: 1 });
    await handlers.consultar_bodegas_criticas({});
    await handlers.consultar_productos_en_riesgo({});
    await handlers.consultar_kpis({});

    expect(fetchMock.mock.calls.slice(1).map(([url, options]) => [url, options.method])).toEqual([
      ["http://api.test/productos/1/stock", "GET"], ["http://api.test/bodegas/criticas", "GET"],
      ["http://api.test/productos/riesgo", "GET"], ["http://api.test/kpis", "GET"]
    ]);
    expect(fetchMock.mock.calls[1][1].headers.Authorization).toBe("Bearer jwt-de-prueba");
  });

  it("envía los cuerpos reales para orden BORRADOR y resumen", async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(jsonResponse({ token: "jwt-de-prueba" }))
      .mockImplementation(() => Promise.resolve(jsonResponse({ id: 1 })));
    const handlers = createToolHandlers(new LogiTrackClient(config, fetchMock));
    const orden = { productoId: 1, proveedorId: 2, bodegaDestinoId: 3, cantidad: 4, precioUnitario: 12.5 };
    const resumen = { fecha: "2026-08-30", narrativa: "Narrativa de prueba con longitud suficiente.", alertas: [{ severidad: "ALTA" as const, titulo: "Riesgo", detalle: "Detalle de alerta.", productoId: 1 }], accionesSugeridas: [{ tipo: "REVISAR_PRODUCTO" as const, descripcion: "Revisar producto en riesgo.", productoId: 1 }] };

    await handlers.crear_orden_borrador(orden);
    await handlers.publicar_resumen({ resumen });

    expect(fetchMock.mock.calls[1]).toMatchObject(["http://api.test/ordenes", { method: "POST", body: JSON.stringify(orden) }]);
    expect(fetchMock.mock.calls[2]).toMatchObject(["http://api.test/panel/resumen", { method: "POST", body: JSON.stringify(resumen) }]);
    expect(toolSchemas.publicar_resumen.parse({ resumen })).toEqual({ resumen });
  });

  it("reautentica y reintenta una sola vez tras un 401", async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(jsonResponse({ token: "jwt-uno" }))
      .mockResolvedValueOnce(jsonResponse({ message: "Expirado" }, 401))
      .mockResolvedValueOnce(jsonResponse({ token: "jwt-dos" }))
      .mockResolvedValueOnce(jsonResponse({ ok: true }));
    const client = new LogiTrackClient(config, fetchMock);

    await client.get("/kpis");

    expect(fetchMock).toHaveBeenCalledTimes(4);
    expect(fetchMock.mock.calls[3][1].headers.Authorization).toBe("Bearer jwt-dos");
  });

  it("no acepta estado y refleja @Digits sin exigir precio positivo", () => {
    const orden = { productoId: 1, proveedorId: 2, bodegaDestinoId: 3, cantidad: 1, precioUnitario: 0 };

    expect(toolSchemas.crear_orden_borrador.parse(orden)).toEqual(orden);
    expect(() => toolSchemas.crear_orden_borrador.parse({ ...orden, estado: "APROBADA" })).toThrow();
    expect(() => toolSchemas.crear_orden_borrador.parse({ ...orden, precioUnitario: 1.234 })).toThrow();
  });
});
