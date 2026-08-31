"use strict";

const SESSION = { token: "logitrack_iq_token", username: "logitrack_iq_username", role: "logitrack_iq_role" };
const state = { token: null, username: null, role: null };

class ApiError extends Error { constructor(status, message) { super(message); this.status = status; } }
const $ = (id) => document.getElementById(id);
const text = (value, fallback = "?") => value === null || value === undefined || value === "" ? fallback : String(value);
const number = (value) => new Intl.NumberFormat("es-CO", { maximumFractionDigits: 2 }).format(Number(value ?? 0));
const currency = (value) => new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 2 }).format(Number(value ?? 0));
const dateTime = (value) => value ? new Intl.DateTimeFormat("es-CO", { dateStyle: "medium", timeStyle: "short", timeZone: "America/Bogota" }).format(new Date(value)) : "?";
const clear = (node) => { while (node.firstChild) node.removeChild(node.firstChild); };
const element = (tag, content, className) => { const node = document.createElement(tag); if (className) node.className = className; if (content !== undefined) node.textContent = text(content); return node; };

function normalizeRole(value) { return text(value, "").replace(/^ROLE_/, "").toUpperCase(); }
function decodeJwt(token) { try { const value = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/"); return JSON.parse(decodeURIComponent(atob(value + "=".repeat((4 - value.length % 4) % 4)).split("").map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0")).join(""))); } catch { return {}; } }
function clearSession() { sessionStorage.removeItem(SESSION.token); sessionStorage.removeItem(SESSION.username); sessionStorage.removeItem(SESSION.role); state.token = null; state.username = null; state.role = null; }
function show(view) { $("loginView").hidden = view !== "login"; $("dashboardView").hidden = view !== "dashboard"; }
function message(target, content, type = "info") { const node = $(target); node.textContent = content; node.className = "iq-message iq-message-" + type; node.hidden = !content; }
function setBusy(button, busy, label) { button.disabled = busy; if (label) button.textContent = busy ? "Procesando?" : label; }

async function api(path, options = {}) {
  const headers = new Headers(options.headers || {});
  headers.set("Accept", options.accept || "application/json");
  if (state.token) headers.set("Authorization", "Bearer " + state.token);
  const response = await fetch(path, { ...options, headers });
  if (response.status === 401) { clearSession(); show("login"); message("loginMessage", "La sesi?n expir?. Inicie sesi?n nuevamente.", "error"); throw new ApiError(401, "Sesi?n expirada"); }
  if (response.status === 403) throw new ApiError(403, "Operaci?n no autorizada para su rol.");
  if (!response.ok) { let body = {}; try { body = await response.json(); } catch { /* response without JSON */ } throw new ApiError(response.status, body.message || body.mensaje || body.error || "No fue posible completar la operaci?n."); }
  return response;
}
async function json(path, options) { const response = await api(path, options); return response.json(); }

async function login(event) {
  event.preventDefault();
  const button = $("loginButton"); const username = $("username").value.trim(); const password = $("password").value;
  if (!username || !password) { message("loginMessage", "Usuario y contrase?a son obligatorios.", "error"); return; }
  setBusy(button, true, "Iniciar sesi?n"); message("loginMessage", "", "info");
  try {
    const response = await fetch("/auth/login", { method: "POST", headers: { "Content-Type": "application/json", Accept: "application/json" }, body: JSON.stringify({ username, password }) });
    const body = await response.json().catch(() => ({}));
    if (!response.ok || !body.token) throw new ApiError(response.status, body.message || body.mensaje || "No fue posible iniciar sesi?n.");
    const claims = decodeJwt(body.token); const role = normalizeRole(body.rol || claims.roles?.[0] || claims.role);
    state.token = body.token; state.username = body.username || claims.sub || username; state.role = role;
    sessionStorage.setItem(SESSION.token, state.token); sessionStorage.setItem(SESSION.username, state.username); sessionStorage.setItem(SESSION.role, state.role);
    show("dashboard"); await loadDashboard();
  } catch (error) { message("loginMessage", error instanceof ApiError && error.status === 403 ? "Operaci?n no autorizada." : error.message || "Error de red. Verifique el servidor.", "error"); }
  finally { setBusy(button, false, "Iniciar sesi?n"); }
}
function restoreSession() { state.token = sessionStorage.getItem(SESSION.token); state.username = sessionStorage.getItem(SESSION.username); state.role = sessionStorage.getItem(SESSION.role); if (!state.token) { show("login"); return false; } show("dashboard"); return true; }
function resource(item) { if (item.ordenId != null) return "Orden #" + item.ordenId; if (item.productoId != null) return "Producto #" + item.productoId; if (item.bodegaId != null) return "Bodega #" + item.bodegaId; return "Sin recurso enlazado"; }
function emptyRow(target, columns, content) { const row = document.createElement("tr"); const cell = element("td", content, "iq-empty"); cell.colSpan = columns; row.append(cell); target.append(row); }

function renderKpis(kpi) {
  $("calculatedAt").textContent = "Actualizado: " + dateTime(kpi.calculadoEn);
  const movements = kpi.movimientosAyer || {};
  const cards = [["Productos en quiebre", kpi.productosEnQuiebre], ["Productos en riesgo", kpi.productosEnRiesgo], ["?rdenes por aprobar", kpi.ordenesPorAprobar?.cantidad], ["Movimientos de ayer", Number(movements.entrada || 0) + Number(movements.salida || 0) + Number(movements.transferencia || 0)]];
  clear($("kpiCards")); cards.forEach(([label, value]) => { const card = element("article", undefined, "iq-kpi-card"); card.append(element("p", label), element("strong", value)); $("kpiCards").append(card); });
  clear($("movementBreakdown")); [["Entradas", kpi.movimientosAyer?.entrada], ["Salidas", kpi.movimientosAyer?.salida], ["Transferencias", kpi.movimientosAyer?.transferencia]].forEach(([label, value]) => { const card = element("div", undefined, "iq-movement"); card.append(element("span", label), element("strong", number(value))); $("movementBreakdown").append(card); });
  clear($("occupancyList")); const occupancy = kpi.ocupacionPorBodega || []; if (!occupancy.length) { $("occupancyList").append(element("p", "Sin datos de ocupaci?n.", "iq-panel")); return; }
  occupancy.forEach((item) => { const critical = Number(item.porcentaje) >= 90; const card = element("article", undefined, "iq-occupancy" + (critical ? " iq-critical" : "")); const bar = element("div", undefined, "iq-progress"); const fill = element("div", undefined, "iq-progress-fill"); fill.style.width = Math.min(Number(item.porcentaje || 0), 100) + "%"; bar.append(fill); card.append(element("h3", item.nombre), element("p", number(item.unidadesAlmacenadas) + " unidades de " + number(item.capacidad)), element("strong", number(item.porcentaje) + "%"), bar); $("occupancyList").append(card); });
}
function renderCritical(items) { const target = $("criticalWarehouses"); clear(target); if (!items.length) { target.append(element("p", "Sin bodegas cr?ticas.")); return; } const list = element("ul", undefined, "iq-list"); items.forEach((item) => list.append(element("li", item.nombre + ": " + number(item.porcentaje) + "% (" + number(item.unidadesAlmacenadas) + "/" + number(item.capacidad) + ")"))); target.append(list); }
function renderRisks(items) { const target = $("riskRows"); clear(target); if (!items.length) return emptyRow(target, 8, "No hay productos en riesgo."); items.forEach((item) => { const row = document.createElement("tr"); [item.nombreProducto || "Producto #" + item.productoId, item.stockTotal, item.consumoDiarioPromedio, item.puntoReorden, item.diasCobertura, item.proveedorId == null ? "Sin proveedor" : "Proveedor #" + item.proveedorId, item.bodegaDestinoId == null ? "?" : "Bodega #" + item.bodegaDestinoId, currency(item.precioUnitarioSugerido)].forEach((value) => row.append(element("td", value))); target.append(row); }); }
function pdfButton(label, handler) { const button = element("button", label, "iq-button iq-button-small"); button.type = "button"; button.addEventListener("click", handler); return button; }
async function generatePdf(order, button) { setBusy(button, true, "Generar PDF"); try { await api("/ordenes/" + order.id + "/pdf", { method: "POST", accept: "application/pdf" }); message("appMessage", "PDF generado para la orden #" + order.id + ".", "success"); } catch (error) { message("appMessage", error.message, error.status === 403 ? "error" : "error"); } finally { setBusy(button, false, "Generar PDF"); } }
async function viewPdf(order, button) { setBusy(button, true, "Ver PDF"); try { const response = await api("/ordenes/" + order.id + "/pdf", { accept: "application/pdf" }); const url = URL.createObjectURL(await response.blob()); window.open(url, "_blank", "noopener"); window.setTimeout(() => URL.revokeObjectURL(url), 60000); } catch (error) { message("appMessage", error.status === 404 ? "El PDF a?n no ha sido generado." : error.message, "error"); } finally { setBusy(button, false, "Ver PDF"); } }
async function approve(order, button) { setBusy(button, true, "Aprobar"); try { await json("/ordenes/" + order.id + "/estado", { method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ estado: "APROBADA" }) }); message("appMessage", "Orden #" + order.id + " aprobada correctamente.", "success"); await loadDashboard(); } catch (error) { message("appMessage", error.message, "error"); } finally { setBusy(button, false, "Aprobar"); } }
function renderOrders(items) { const target = $("orderRows"); clear(target); if (!items.length) return emptyRow(target, 10, "No hay ?rdenes BORRADOR."); items.forEach((order) => { const row = document.createElement("tr"); [order.id, "Producto #" + order.productoId, "Proveedor #" + order.proveedorId, "Bodega #" + order.bodegaDestinoId, order.cantidad, currency(order.precioUnitario), currency(order.total), dateTime(order.fechaCreacion), order.estado].forEach((value) => row.append(element("td", value))); const actions = element("td", undefined, "iq-actions"); actions.append(pdfButton("Generar PDF", (event) => generatePdf(order, event.currentTarget)), pdfButton("Ver PDF", (event) => viewPdf(order, event.currentTarget))); if (state.role === "ADMIN") actions.append(pdfButton("Aprobar", (event) => approve(order, event.currentTarget))); row.append(actions); target.append(row); }); }
function renderSummary(summary) { const target = $("dailySummary"); clear(target); if (!summary) { target.append(element("p", "A?n no hay un resumen publicado para hoy.")); return; } target.append(element("p", "Fecha: " + summary.fecha, "iq-summary-date"), element("p", summary.narrativa)); const alerts = element("div", undefined, "iq-summary-group"); alerts.append(element("h3", "Alertas")); if (!(summary.alertas || []).length) alerts.append(element("p", "Sin alertas.")); (summary.alertas || []).forEach((alert) => { const card = element("article", undefined, "iq-alert iq-severity-" + text(alert.severidad).toLowerCase()); card.append(element("strong", text(alert.severidad) + " ? " + text(alert.titulo)), element("p", alert.detalle), element("small", resource(alert))); alerts.append(card); }); const actions = element("div", undefined, "iq-summary-group"); actions.append(element("h3", "Acciones sugeridas")); if (!(summary.accionesSugeridas || []).length) actions.append(element("p", "Sin acciones sugeridas.")); (summary.accionesSugeridas || []).forEach((action) => { const card = element("article", undefined, "iq-action"); card.append(element("strong", action.tipo), element("p", action.descripcion), element("small", resource(action))); actions.append(card); }); target.append(alerts, actions); }

async function loadDashboard() {
  $("sessionUser").textContent = state.username || "Usuario"; $("sessionRole").textContent = state.role || "ROL"; setBusy($("refreshButton"), true, "Actualizar panel"); message("appMessage", "Cargando panel?", "info");
  const [kpis, risks, orders, critical, summary] = await Promise.allSettled([json("/kpis"), json("/productos/riesgo"), json("/ordenes?estado=BORRADOR"), json("/bodegas/criticas"), json("/panel/resumen")]);
  const failed = [kpis, risks, orders, critical].find((item) => item.status === "rejected"); if (failed) { message("appMessage", failed.reason.message || "Error de red al cargar el dashboard.", "error"); setBusy($("refreshButton"), false, "Actualizar panel"); return; }
  renderKpis(kpis.value); renderRisks(risks.value); renderOrders(orders.value); renderCritical(critical.value); if (summary.status === "fulfilled") renderSummary(summary.value); else if (summary.reason.status === 404) renderSummary(null); else message("appMessage", summary.reason.message || "No fue posible cargar el resumen.", "error");
  if (summary.status === "fulfilled" || summary.reason?.status === 404) message("appMessage", "", "info"); setBusy($("refreshButton"), false, "Actualizar panel");
}

document.addEventListener("DOMContentLoaded", () => { $("loginForm").addEventListener("submit", login); $("logoutButton").addEventListener("click", () => { clearSession(); show("login"); }); $("refreshButton").addEventListener("click", loadDashboard); if (restoreSession()) loadDashboard(); });
