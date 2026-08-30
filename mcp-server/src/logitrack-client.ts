import type { McpConfig } from "./config.js";

export type JsonValue = Record<string, unknown> | unknown[];
export type FetchLike = typeof fetch;

type LoginResponse = { token?: unknown };

export class BackendHttpError extends Error {
  constructor(
    public readonly status: number,
    message: string
  ) {
    super(`LogiTrack API respondió ${status}: ${message}`);
    this.name = "BackendHttpError";
  }
}

export class LogiTrackClient {
  private token?: string;

  constructor(
    private readonly config: McpConfig,
    private readonly fetchImplementation: FetchLike = fetch
  ) {}

  async get(path: string): Promise<JsonValue> {
    return this.request("GET", path);
  }

  async post(path: string, body: JsonValue): Promise<JsonValue> {
    return this.request("POST", path, body);
  }

  private async request(
    method: "GET" | "POST",
    path: string,
    body?: JsonValue
  ): Promise<JsonValue> {
    const firstResponse = await this.send(method, path, body);
    if (firstResponse.status !== 401) {
      return this.readResponse(firstResponse);
    }

    this.token = undefined;
    const retryResponse = await this.send(method, path, body);
    return this.readResponse(retryResponse);
  }

  private async send(
    method: "GET" | "POST",
    path: string,
    body?: JsonValue
  ): Promise<Response> {
    const token = await this.getToken();
    const headers: Record<string, string> = {
      Authorization: `Bearer ${token}`
    };

    if (body !== undefined) {
      headers["Content-Type"] = "application/json";
    }

    return this.fetchImplementation(this.url(path), {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body)
    });
  }

  private async getToken(): Promise<string> {
    if (this.token) {
      return this.token;
    }

    const response = await this.fetchImplementation(this.url("/auth/login"), {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        username: this.config.LOGITRACK_AGENT_USERNAME,
        password: this.config.LOGITRACK_AGENT_PASSWORD
      })
    });

    const payload = await this.readResponse(response) as LoginResponse;
    if (typeof payload.token !== "string" || payload.token.length === 0) {
      throw new Error("LogiTrack API no devolvió un JWT válido al autenticar AGENTE.");
    }

    this.token = payload.token;
    return this.token;
  }

  private async readResponse(response: Response): Promise<JsonValue> {
    const payload = await this.readPayload(response);
    if (!response.ok) {
      throw new BackendHttpError(response.status, this.messageFrom(payload));
    }
    return payload;
  }

  private async readPayload(response: Response): Promise<JsonValue> {
    const text = await response.text();
    if (!text) {
      return {};
    }

    try {
      return JSON.parse(text) as JsonValue;
    } catch {
      return { message: text };
    }
  }

  private messageFrom(payload: JsonValue): string {
    if (typeof payload === "object" && payload !== null && !Array.isArray(payload)) {
      const value = payload.message ?? payload.error;
      if (typeof value === "string" && value.length > 0) {
        return value;
      }
    }
    return "Solicitud rechazada por el backend.";
  }

  private url(path: string): string {
    return new URL(path, `${this.config.LOGITRACK_API_BASE_URL.replace(/\/$/, "")}/`).toString();
  }
}
