"use client";

import { useEffect, useState } from "react";

const initialContact = {
  name: "",
  email: "",
  phone: "",
  company: "",
  status: "LEAD"
};

const initialDeal = {
  title: "",
  value: "",
  stage: "PROSPECTING",
  expectedCloseDate: "",
  contactId: ""
};

const initialActivity = {
  type: "CALL",
  notes: "",
  dueAt: "",
  contactId: ""
};

const contactStatusLabels = {
  LEAD: "Lead",
  QUALIFIED: "Qualificado",
  CUSTOMER: "Cliente"
};

const dealStageLabels = {
  PROSPECTING: "Prospeccao",
  PROPOSAL: "Proposta",
  NEGOTIATION: "Negociacao",
  WON: "Ganho",
  LOST: "Perdido"
};

const activityTypeLabels = {
  CALL: "Ligacao",
  EMAIL: "E-mail",
  MEETING: "Reuniao",
  TASK: "Tarefa"
};

const stageColors = {
  PROSPECTING: "#38bdf8",
  PROPOSAL: "#8b5cf6",
  NEGOTIATION: "#14b8a6",
  WON: "#22c55e",
  LOST: "#f97316"
};

const activityColors = {
  CALL: "#38bdf8",
  EMAIL: "#14b8a6",
  MEETING: "#a855f7",
  TASK: "#f97316"
};

let csrfState = null;

async function extractErrorMessage(response) {
  const contentType = response.headers.get("content-type") || "";

  if (contentType.includes("application/json")) {
    const payload = await response.json().catch(() => null);
    if (payload?.error) {
      return payload.error;
    }
  }

  const text = await response.text().catch(() => "");
  return text || "Falha na requisicao.";
}

async function fetchJson(path, options = {}) {
  const { headers: extraHeaders, ...restOptions } = options;
  const response = await fetch(path, {
    ...restOptions,
    headers: {
      "Content-Type": "application/json",
      ...(extraHeaders ?? {})
    }
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response));
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

async function ensureCsrfState() {
  if (csrfState) {
    return csrfState;
  }

  csrfState = await fetchJson("/api/security/csrf");
  return csrfState;
}

function formatCurrency(value) {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
    maximumFractionDigits: 0
  }).format(Number(value || 0));
}

function formatNumber(value) {
  return new Intl.NumberFormat("pt-BR").format(Number(value || 0));
}

function formatPercent(value) {
  return `${Number(value || 0).toFixed(1).replace(".", ",")}%`;
}

function formatDate(value) {
  if (!value) {
    return "-";
  }

  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: value.includes("T") ? "short" : undefined
  }).format(new Date(value));
}

function safeEntries(record) {
  return Object.entries(record || {}).filter(([, value]) => Number(value || 0) > 0);
}

function linePath(points) {
  if (!points.length) {
    return "";
  }

  return points
    .map((point, index) => `${index === 0 ? "M" : "L"} ${point.x} ${point.y}`)
    .join(" ");
}

function buildLinePoints(values, width, height) {
  const max = Math.max(...values, 1);

  return values.map((value, index) => ({
    x: (index / Math.max(values.length - 1, 1)) * width,
    y: height - (value / max) * height
  }));
}

function contactLabel(contact) {
  return `${contact.name}${contact.company ? ` • ${contact.company}` : ""}`;
}

function SectionHeader({ eyebrow, title, description }) {
  return (
    <div className="mb-5">
      {eyebrow ? <p className="panel-eyebrow">{eyebrow}</p> : null}
      <h2 className="panel-title">{title}</h2>
      {description ? <p className="panel-description">{description}</p> : null}
    </div>
  );
}

function Panel({ eyebrow, title, description, children, className = "" }) {
  return (
    <section className={`panel ${className}`.trim()}>
      <SectionHeader eyebrow={eyebrow} title={title} description={description} />
      {children}
    </section>
  );
}

function MetricCard({ label, value, detail, tone = "cyan" }) {
  return (
    <article className={`metric-card metric-card-${tone}`}>
      <p className="metric-label">{label}</p>
      <p className="metric-value">{value}</p>
      <p className="metric-detail">{detail}</p>
    </article>
  );
}

function TrendChart({ items }) {
  const width = 520;
  const height = 220;
  const values = items.map((item) => Number(item.leads || 0));
  const points = buildLinePoints(values, width, height);
  const path = linePath(points);

  return (
    <div className="chart-block">
      <svg viewBox={`0 0 ${width} ${height + 24}`} className="chart-svg" role="img" aria-label="Grafico de leads por periodo">
        <defs>
          <linearGradient id="lead-line" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#38bdf8" />
            <stop offset="100%" stopColor="#8b5cf6" />
          </linearGradient>
        </defs>
        {[0.25, 0.5, 0.75].map((line) => (
          <line key={line} x1="0" y1={height * line} x2={width} y2={height * line} className="chart-grid-line" />
        ))}
        <path d={path} fill="none" stroke="url(#lead-line)" strokeWidth="4" strokeLinecap="round" />
        {points.map((point, index) => (
          <g key={items[index].date}>
            <circle cx={point.x} cy={point.y} r="6" fill="#0f172a" stroke="#67e8f9" strokeWidth="3" />
            <text x={point.x} y={height + 18} textAnchor="middle" className="chart-axis-label">
              {items[index].label}
            </text>
          </g>
        ))}
      </svg>
    </div>
  );
}

function RecordsBarChart({ items }) {
  const max = Math.max(...items.map((item) => Number(item.total || 0)), 1);

  return (
    <div className="bar-chart">
      {items.map((item) => {
        const total = Number(item.total || 0);
        const height = `${Math.max((total / max) * 100, 6)}%`;
        return (
          <div key={item.date} className="bar-column">
            <span className="bar-value">{formatNumber(total)}</span>
            <div className="bar-track">
              <div className="bar-fill" style={{ height }} />
            </div>
            <span className="bar-label">{item.label}</span>
          </div>
        );
      })}
    </div>
  );
}

function DonutChart({ entries, labels, colors, totalLabel }) {
  const size = 208;
  const radius = 76;
  const circumference = 2 * Math.PI * radius;
  const total = entries.reduce((sum, [, value]) => sum + Number(value || 0), 0);
  let offset = 0;

  return (
    <div className="donut-layout">
      <div className="donut-shell">
        <svg viewBox={`0 0 ${size} ${size}`} className="donut-svg" role="img">
          <circle cx={size / 2} cy={size / 2} r={radius} className="donut-base" />
          {entries.map(([key, value]) => {
            const rawValue = Number(value || 0);
            const segment = total === 0 ? 0 : (rawValue / total) * circumference;
            const circle = (
              <circle
                key={key}
                cx={size / 2}
                cy={size / 2}
                r={radius}
                className="donut-segment"
                stroke={colors[key] || "#38bdf8"}
                strokeDasharray={`${segment} ${circumference - segment}`}
                strokeDashoffset={-offset}
              />
            );
            offset += segment;
            return circle;
          })}
        </svg>
        <div className="donut-center">
          <span className="donut-total">{formatNumber(total)}</span>
          <span className="donut-label">{totalLabel}</span>
        </div>
      </div>
      <div className="legend-list">
        {entries.length ? (
          entries.map(([key, value]) => (
            <div key={key} className="legend-row">
              <span className="legend-dot" style={{ backgroundColor: colors[key] || "#38bdf8" }} />
              <span className="legend-text">{labels[key] || key}</span>
              <span className="legend-value">{formatNumber(value)}</span>
            </div>
          ))
        ) : (
          <p className="empty-state">Sem dados suficientes para este grafico.</p>
        )}
      </div>
    </div>
  );
}

export default function HomePage() {
  const [metrics, setMetrics] = useState({
    contacts: 0,
    deals: 0,
    activities: 0,
    totalRecords: 0,
    projectedRevenue: 0,
    closedRevenue: 0,
    conversionRate: 0,
    overdueActivities: 0,
    scheduledActivities: 0,
    pipelineByStage: {},
    leadStatusCounts: {},
    activityTypeCounts: {},
    recordsTimeline: []
  });
  const [contacts, setContacts] = useState([]);
  const [deals, setDeals] = useState([]);
  const [activities, setActivities] = useState([]);
  const [contactForm, setContactForm] = useState(initialContact);
  const [dealForm, setDealForm] = useState(initialDeal);
  const [activityForm, setActivityForm] = useState(initialActivity);
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState({
    contact: false,
    deal: false,
    activity: false
  });

  const contactOptions = [...contacts].sort((left, right) => left.name.localeCompare(right.name, "pt-BR"));

  async function loadDashboard() {
    setLoading(true);

    try {
      const [metricsResponse, contactsResponse, dealsResponse, activitiesResponse] = await Promise.all([
        fetchJson("/api/dashboard/metrics"),
        fetchJson("/api/contacts"),
        fetchJson("/api/deals"),
        fetchJson("/api/activities")
      ]);

      setMetrics(metricsResponse);
      setContacts(contactsResponse);
      setDeals(dealsResponse);
      setActivities(activitiesResponse);
      setFeedback({ type: "", message: "" });
    } catch (error) {
      setFeedback({
        type: "error",
        message: error.message || "Nao foi possivel carregar o dashboard."
      });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDashboard();
  }, []);

  useEffect(() => {
    if (contactOptions.length && !dealForm.contactId) {
      setDealForm((current) => ({ ...current, contactId: String(contactOptions[0].id) }));
    }

    if (contactOptions.length && !activityForm.contactId) {
      setActivityForm((current) => ({ ...current, contactId: String(contactOptions[0].id) }));
    }
  }, [contactOptions, dealForm.contactId, activityForm.contactId]);

  async function handleSubmit(kind, path, payload, resetForm) {
    setSubmitting((current) => ({ ...current, [kind]: true }));

    try {
      const csrf = await ensureCsrfState();
      await fetchJson(path, {
        method: "POST",
        headers: {
          [csrf.headerName]: csrf.token
        },
        body: JSON.stringify(payload)
      });
      resetForm();
      await loadDashboard();
      setFeedback({
        type: "success",
        message: "Registro salvo com sucesso e dashboard atualizado."
      });
    } catch (error) {
      setFeedback({
        type: "error",
        message: error.message || "Nao foi possivel concluir a operacao."
      });
    } finally {
      setSubmitting((current) => ({ ...current, [kind]: false }));
    }
  }

  const timeline = metrics.recordsTimeline || [];
  const pipelineEntries = safeEntries(metrics.pipelineByStage);
  const activityEntries = safeEntries(metrics.activityTypeCounts);
  const hottestStage = [...pipelineEntries].sort((left, right) => Number(right[1]) - Number(left[1]))[0];
  const hasContacts = contactOptions.length > 0;

  return (
    <main className="dashboard-shell">
      <div className="dashboard-grid">
        <header className="hero-panel">
          <div className="hero-copy">
            <p className="hero-kicker">Central de inteligencia comercial</p>
            <h1 className="hero-title">Visao executiva da operacao de vendas em tempo real</h1>
            <p className="hero-description">
              Monitore leads, negocios e atividades com leitura rapida de volume, conversao,
              receita e ritmo operacional. Tudo em uma interface pensada para decisao comercial.
            </p>
          </div>
          <div className="hero-highlights">
            <div className="hero-chip">
              <span className="hero-chip-label">Receita projetada</span>
              <span className="hero-chip-value">{loading ? "..." : formatCurrency(metrics.projectedRevenue)}</span>
            </div>
            <div className="hero-chip">
              <span className="hero-chip-label">Taxa de conversao</span>
              <span className="hero-chip-value">{loading ? "..." : formatPercent(metrics.conversionRate)}</span>
            </div>
            <div className="hero-chip">
              <span className="hero-chip-label">Registros no banco</span>
              <span className="hero-chip-value">{loading ? "..." : formatNumber(metrics.totalRecords)}</span>
            </div>
          </div>
        </header>

        {feedback.message ? (
          <div className={`feedback-banner ${feedback.type === "error" ? "feedback-banner-error" : ""}`}>
            {feedback.message}
          </div>
        ) : null}

        <section className="metric-grid">
          <MetricCard
            label="Leads e clientes"
            value={loading ? "..." : formatNumber(metrics.contacts)}
            detail="Base ativa para relacionamento e qualificacao."
          />
          <MetricCard
            label="Negocios em andamento"
            value={loading ? "..." : formatNumber(metrics.deals)}
            detail="Oportunidades abertas no funil de vendas."
            tone="violet"
          />
          <MetricCard
            label="Atividades registradas"
            value={loading ? "..." : formatNumber(metrics.activities)}
            detail="Interacoes e tarefas em acompanhamento."
            tone="teal"
          />
          <MetricCard
            label="Receita fechada"
            value={loading ? "..." : formatCurrency(metrics.closedRevenue)}
            detail="Total ganho em negocios convertidos."
            tone="green"
          />
        </section>

        <section className="insight-grid">
          <Panel eyebrow="Leads" title="Leads por periodo" description="Leitura dos cadastros recentes para acompanhar entrada de demanda.">
            {timeline.length ? <TrendChart items={timeline} /> : <p className="empty-state">Sem historico suficiente.</p>}
          </Panel>

          <Panel eyebrow="Banco de dados" title="Registros criados por dia" description="Volume combinado de leads, oportunidades e atividades gravadas.">
            {timeline.length ? <RecordsBarChart items={timeline} /> : <p className="empty-state">Sem historico suficiente.</p>}
          </Panel>
        </section>

        <section className="analytics-grid">
          <Panel eyebrow="Funil" title="Distribuicao do pipeline" description="Valor por etapa para identificar onde a receita esta concentrada.">
            <DonutChart entries={pipelineEntries} labels={dealStageLabels} colors={stageColors} totalLabel="valor no funil" />
          </Panel>

          <Panel eyebrow="Atividades" title="Mix operacional" description="Tipos de contato e acompanhamento que sustentam a operacao.">
            <DonutChart entries={activityEntries} labels={activityTypeLabels} colors={activityColors} totalLabel="atividades" />
          </Panel>
        </section>

        <section className="operations-grid">
          <Panel eyebrow="Execucao" title="Prioridades do dia" description="Sinais imediatos para orientar a equipe comercial." className="panel-secondary">
            <ul className="priority-list">
              <li>
                <strong>Atividades atrasadas:</strong> {formatNumber(metrics.overdueActivities)} pendencias exigem acao imediata.
              </li>
              <li>
                <strong>Proximas atividades:</strong> {formatNumber(metrics.scheduledActivities)} compromissos previstos.
              </li>
              <li>
                <strong>Melhor etapa:</strong> {hottestStage ? dealStageLabels[hottestStage[0]] : "Sem dados"} lidera o volume do pipeline.
              </li>
              <li>
                <strong>Base qualificada:</strong> {formatNumber(metrics.leadStatusCounts?.QUALIFIED)} contatos prontos para avancar.
              </li>
            </ul>
          </Panel>

          <Panel eyebrow="Leads" title="Base comercial" description="Distribuicao atual entre leads, contatos qualificados e clientes.">
            <div className="status-grid">
              {Object.entries(contactStatusLabels).map(([status, label]) => (
                <div key={status} className="status-row">
                  <span>{label}</span>
                  <strong>{formatNumber(metrics.leadStatusCounts?.[status])}</strong>
                </div>
              ))}
            </div>
          </Panel>

          <Panel eyebrow="Cadastro" title="Novo contato" description="Adicione leads e clientes para fortalecer a base comercial.">
            <form
              className="form-stack"
              onSubmit={(event) => {
                event.preventDefault();
                handleSubmit("contact", "/api/contacts", contactForm, () => setContactForm(initialContact));
              }}
            >
              <input className="field" placeholder="Nome" value={contactForm.name} onChange={(event) => setContactForm({ ...contactForm, name: event.target.value })} required />
              <input className="field" type="email" placeholder="E-mail" value={contactForm.email} onChange={(event) => setContactForm({ ...contactForm, email: event.target.value })} required />
              <input className="field" placeholder="Telefone" value={contactForm.phone} onChange={(event) => setContactForm({ ...contactForm, phone: event.target.value })} />
              <input className="field" placeholder="Empresa" value={contactForm.company} onChange={(event) => setContactForm({ ...contactForm, company: event.target.value })} />
              <select className="field" value={contactForm.status} onChange={(event) => setContactForm({ ...contactForm, status: event.target.value })}>
                {Object.entries(contactStatusLabels).map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </select>
              <button className="primary-button" type="submit" disabled={submitting.contact}>
                {submitting.contact ? "Salvando contato..." : "Salvar contato"}
              </button>
            </form>
          </Panel>

          <Panel eyebrow="Pipeline" title="Nova oportunidade" description="Registre um negocio e acompanhe sua evolucao no funil.">
            <form
              className="form-stack"
              onSubmit={(event) => {
                event.preventDefault();
                handleSubmit(
                  "deal",
                  "/api/deals",
                  {
                    ...dealForm,
                    value: Number(dealForm.value),
                    expectedCloseDate: dealForm.expectedCloseDate || null,
                    contactId: Number(dealForm.contactId)
                  },
                  () => setDealForm((current) => ({ ...initialDeal, contactId: current.contactId }))
                );
              }}
            >
              <input className="field" placeholder="Nome da oportunidade" value={dealForm.title} onChange={(event) => setDealForm({ ...dealForm, title: event.target.value })} required />
              <input className="field" type="number" min="0" step="0.01" placeholder="Valor previsto" value={dealForm.value} onChange={(event) => setDealForm({ ...dealForm, value: event.target.value })} required />
              <select className="field" value={dealForm.stage} onChange={(event) => setDealForm({ ...dealForm, stage: event.target.value })}>
                {Object.entries(dealStageLabels).map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </select>
              <input className="field" type="date" value={dealForm.expectedCloseDate} onChange={(event) => setDealForm({ ...dealForm, expectedCloseDate: event.target.value })} />
              <select className="field" value={dealForm.contactId} onChange={(event) => setDealForm({ ...dealForm, contactId: event.target.value })} disabled={!hasContacts} required>
                {hasContacts ? (
                  contactOptions.map((contact) => (
                    <option key={contact.id} value={contact.id}>{contactLabel(contact)}</option>
                  ))
                ) : (
                  <option value="">Cadastre um contato antes de criar oportunidades</option>
                )}
              </select>
              <button className="primary-button" type="submit" disabled={submitting.deal || !hasContacts}>
                {submitting.deal ? "Salvando oportunidade..." : "Salvar oportunidade"}
              </button>
            </form>
          </Panel>
        </section>

        <section className="table-grid">
          <Panel eyebrow="Agenda" title="Nova atividade" description="Planeje contatos, reunioes e tarefas para manter a cadencia comercial.">
            <form
              className="form-stack"
              onSubmit={(event) => {
                event.preventDefault();
                handleSubmit(
                  "activity",
                  "/api/activities",
                  {
                    ...activityForm,
                    contactId: Number(activityForm.contactId),
                    dueAt: activityForm.dueAt ? new Date(activityForm.dueAt).toISOString() : null
                  },
                  () => setActivityForm((current) => ({ ...initialActivity, contactId: current.contactId || initialActivity.contactId }))
                );
              }}
            >
              <select className="field" value={activityForm.type} onChange={(event) => setActivityForm({ ...activityForm, type: event.target.value })}>
                {Object.entries(activityTypeLabels).map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </select>
              <input className="field" placeholder="Observacao" value={activityForm.notes} onChange={(event) => setActivityForm({ ...activityForm, notes: event.target.value })} required />
              <input className="field" type="datetime-local" value={activityForm.dueAt} onChange={(event) => setActivityForm({ ...activityForm, dueAt: event.target.value })} />
              <select className="field" value={activityForm.contactId} onChange={(event) => setActivityForm({ ...activityForm, contactId: event.target.value })} disabled={!hasContacts} required>
                {hasContacts ? (
                  contactOptions.map((contact) => (
                    <option key={contact.id} value={contact.id}>{contactLabel(contact)}</option>
                  ))
                ) : (
                  <option value="">Cadastre um contato antes de criar atividades</option>
                )}
              </select>
              <button className="primary-button" type="submit" disabled={submitting.activity || !hasContacts}>
                {submitting.activity ? "Salvando atividade..." : "Salvar atividade"}
              </button>
            </form>
          </Panel>

          <Panel eyebrow="Relacionamento" title="Ultimos contatos" description="Consulta rapida da base comercial para atendimento e acompanhamento.">
            <div className="table-shell">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Contato</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {contacts.map((contact) => (
                    <tr key={contact.id}>
                      <td>{contact.id}</td>
                      <td>
                        <div className="table-title">{contact.name}</div>
                        <div className="table-caption">{contact.email}</div>
                      </td>
                      <td>{contactStatusLabels[contact.status] || contact.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Panel>

          <Panel eyebrow="Receita" title="Negocios recentes" description="Oportunidades com valor e etapa para apoiar a tomada de decisao.">
            <div className="table-shell">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Oportunidade</th>
                    <th>Contato</th>
                    <th>Etapa</th>
                  </tr>
                </thead>
                <tbody>
                  {deals.map((deal) => (
                    <tr key={deal.id}>
                      <td>{deal.id}</td>
                      <td>
                        <div className="table-title">{deal.title}</div>
                        <div className="table-caption">{formatCurrency(deal.value)}</div>
                      </td>
                      <td>{deal.contact?.name || "-"}</td>
                      <td>{dealStageLabels[deal.stage] || deal.stage}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Panel>

          <Panel eyebrow="Agenda" title="Atividades da equipe" description="Historico recente para manter contexto nas proximas interacoes.">
            <div className="table-shell">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Tipo</th>
                    <th>Contato</th>
                    <th>Prazo</th>
                  </tr>
                </thead>
                <tbody>
                  {activities.map((activity) => (
                    <tr key={activity.id}>
                      <td>{activity.id}</td>
                      <td>
                        <div className="table-title">{activityTypeLabels[activity.type] || activity.type}</div>
                        <div className="table-caption">{activity.notes}</div>
                      </td>
                      <td>{activity.contact?.name || "-"}</td>
                      <td>{formatDate(activity.dueAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Panel>
        </section>
      </div>
    </main>
  );
}
