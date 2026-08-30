import { once } from "node:events";
import type { Server } from "node:http";
import { afterEach, describe, expect, it } from "vitest";
import type { McpConfig } from "../src/config.js";
import { createHttpApp } from "../src/server.js";

const config: McpConfig = {
  LOGITRACK_API_BASE_URL: "http://localhost:8080",
  LOGITRACK_AGENT_USERNAME: "agente",
  LOGITRACK_AGENT_PASSWORD: "no-se-guarda-en-el-test",
  MCP_PORT: 3001,
  MCP_BEARER_TOKEN: "mcp-test-token"
};

let server: Server | undefined;

afterEach(async () => {
  if (server) {
    server.close();
    await once(server, "close");
    server = undefined;
  }
});

async function startServer() {
  server = createHttpApp(config).listen(0);
  await once(server, "listening");
  const address = server.address();
  if (!address || typeof address === "string") throw new Error("No se obtuvo puerto de prueba.");
  return `http://127.0.0.1:${address.port}`;
}

function initializeRequest(token?: string) {
  return {
    method: "POST",
    headers: {
      Accept: "application/json, text/event-stream",
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify({
      jsonrpc: "2.0",
      id: 1,
      method: "initialize",
      params: { protocolVersion: "2025-03-26", capabilities: {}, clientInfo: { name: "test", version: "1.0" } }
    })
  };
}

describe("protección MCP opcional", () => {
  it("rechaza /mcp sin token y con token incorrecto", async () => {
    const baseUrl = await startServer();
    expect((await fetch(`${baseUrl}/mcp`, initializeRequest())).status).toBe(401);
    expect((await fetch(`${baseUrl}/mcp`, initializeRequest("incorrecto"))).status).toBe(401);
  });

  it("permite protocolo MCP con token correcto y deja /health público", async () => {
    const baseUrl = await startServer();
    const health = await fetch(`${baseUrl}/health`);
    const initialized = await fetch(`${baseUrl}/mcp`, initializeRequest("mcp-test-token"));

    expect(health.status).toBe(200);
    expect(await health.json()).toEqual({ status: "ok" });
    expect(initialized.status).toBe(200);
    expect(initialized.headers.get("mcp-session-id")).toBeTruthy();
  });
});
