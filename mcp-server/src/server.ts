import "dotenv/config";
import express from "express";
import { randomUUID } from "node:crypto";
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import { loadConfig, type McpConfig } from "./config.js";
import { LogiTrackClient } from "./logitrack-client.js";
import { createToolHandlers, TOOL_NAMES, toolSchemas, toMcpResult } from "./tools.js";

function createMcpServer(config: McpConfig) {
  const server = new McpServer({ name: "logitrack-iq-mcp", version: "1.0.0" });
  const handlers = createToolHandlers(new LogiTrackClient(config));

  server.registerTool("consultar_stock_producto", {
    description: "Consulta el stock derivado de un producto en LogiTrack IQ.",
    inputSchema: toolSchemas.consultar_stock_producto.shape
  }, async (input) => toMcpResult(await handlers.consultar_stock_producto(input)));
  server.registerTool("consultar_bodegas_criticas", {
    description: "Consulta las bodegas críticas calculadas por LogiTrack IQ.",
    inputSchema: toolSchemas.consultar_bodegas_criticas.shape
  }, async () => toMcpResult(await handlers.consultar_bodegas_criticas()));
  server.registerTool("consultar_productos_en_riesgo", {
    description: "Consulta productos en riesgo sin recalcular métricas.",
    inputSchema: toolSchemas.consultar_productos_en_riesgo.shape
  }, async () => toMcpResult(await handlers.consultar_productos_en_riesgo()));
  server.registerTool("consultar_kpis", {
    description: "Consulta los KPIs calculados por LogiTrack IQ.",
    inputSchema: toolSchemas.consultar_kpis.shape
  }, async () => toMcpResult(await handlers.consultar_kpis()));
  server.registerTool("crear_orden_borrador", {
    description: "Crea una orden BORRADOR; el backend calcula total y estado.",
    inputSchema: toolSchemas.crear_orden_borrador.shape
  }, async (input) => toMcpResult(await handlers.crear_orden_borrador(input)));
  server.registerTool("publicar_resumen", {
    description: "Publica un resumen de panel validado por el backend.",
    inputSchema: toolSchemas.publicar_resumen.shape
  }, async (input) => toMcpResult(await handlers.publicar_resumen(input)));

  return server;
}

export function createHttpApp(config: McpConfig) {
  const app = express();
  app.use(express.json());
  const transports = new Map<string, StreamableHTTPServerTransport>();

  app.get("/health", (_request, response) => response.status(200).json({ status: "ok" }));

  app.use("/mcp", (request, response, next) => {
    if (!config.MCP_BEARER_TOKEN) {
      next();
      return;
    }

    if (request.header("authorization") !== `Bearer ${config.MCP_BEARER_TOKEN}`) {
      response.status(401).json({ error: "MCP bearer token is required." });
      return;
    }

    next();
  });

  app.all("/mcp", async (request, response) => {
    const header = request.headers["mcp-session-id"];
    const sessionId = Array.isArray(header) ? header[0] : header;
    let transport = sessionId ? transports.get(sessionId) : undefined;

    if (!transport && request.method === "POST") {
      const server = createMcpServer(config);
      transport = new StreamableHTTPServerTransport({
        sessionIdGenerator: () => randomUUID(),
        onsessioninitialized: (id) => {
          transports.set(id, transport!);
        }
      });
      transport.onclose = () => {
        if (transport?.sessionId) transports.delete(transport.sessionId);
      };
      await server.connect(transport);
    }

    if (!transport) {
      response.status(400).json({ error: "MCP session is required for this request." });
      return;
    }

    await transport.handleRequest(request, response, request.body);
  });

  return app;
}

export { TOOL_NAMES };

if (process.argv[1]?.endsWith("server.js")) {
  const config = loadConfig();
  createHttpApp(config).listen(config.MCP_PORT, () => {
    console.log(`LogiTrack IQ MCP disponible en http://localhost:${config.MCP_PORT}/mcp`);
  });
}
