import { z } from "zod";

const environmentSchema = z.object({
  LOGITRACK_API_BASE_URL: z.string().url(),
  LOGITRACK_AGENT_USERNAME: z.string().min(1),
  LOGITRACK_AGENT_PASSWORD: z.string().min(1),
  MCP_PORT: z.coerce.number().int().positive().default(3001),
  MCP_BEARER_TOKEN: z.preprocess(
    (value) => typeof value === "string" && value.trim() === "" ? undefined : value,
    z.string().min(1).optional()
  )
});

export type McpConfig = z.infer<typeof environmentSchema>;

export function loadConfig(environment: NodeJS.ProcessEnv = process.env): McpConfig {
  return environmentSchema.parse(environment);
}
