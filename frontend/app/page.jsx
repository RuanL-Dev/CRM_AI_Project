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

const metricCards = [
  { key: "contacts", label: "Contatos" },
  { key: "deals", label: "Deals" },
  { key: "activities", label: "Atividades" }
];

let csrfState = null;

async function fetchJson(path, options = {}) {
  const response = await fetch(path, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers ?? {})
    },
    ...options
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "Falha na requisicao.");
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
    currency: "BRL"
  }).format(Number(value || 0));
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

function SectionCard({ title, description, children }) {
  return (
    <section className="rounded-[28px] border border-white/70 bg-white/90 p-6 shadow-panel backdrop-blur">
      <div className="mb-5">
        <h2 className="font-display text-xl font-bold text-ink">{title}</h2>
        <p className="mt-1 text-sm text-slate-500">{description}</p>
      </div>
      {children}
    </section>
  );
}

export default function HomePage() {
  const [metrics, setMetrics] = useState({
    contacts: 0,
    deals: 0,
    activities: 0,
    pipelineByStage: {}
  });
  const [contacts, setContacts] = useState([]);
  const [deals, setDeals] = useState([]);
  const [activities, setActivities] = useState([]);
  const [contactForm, setContactForm] = useState(initialContact);
  const [dealForm, setDealForm] = useState(initialDeal);
  const [activityForm, setActivityForm] = useState(initialActivity);
  const [feedback, setFeedback] = useState("");
  const [loading, setLoading] = useState(true);

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
      setFeedback("");
    } catch (error) {
      setFeedback(error.message || "Nao foi possivel carregar o dashboard.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDashboard();
  }, []);

  async function handleSubmit(path, payload, resetForm) {
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
      setFeedback("Operacao concluida com sucesso.");
    } catch (error) {
      setFeedback(error.message || "Nao foi possivel concluir a operacao.");
    }
  }

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(15,118,110,0.2),_transparent_36%),linear-gradient(180deg,_#f8fafc_0%,_#dbeafe_100%)] text-slate-900">
      <div className="mx-auto flex min-h-screen max-w-7xl flex-col gap-8 px-4 py-8 sm:px-6 lg:px-8">
        <header className="overflow-hidden rounded-[36px] bg-ink px-6 py-8 text-white shadow-panel sm:px-8">
          <div className="flex flex-col gap-8 lg:flex-row lg:items-end lg:justify-between">
            <div className="max-w-3xl">
              <p className="inline-flex rounded-full border border-white/15 bg-white/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.3em] text-cyan-200">
                Next.js + Spring Boot + PostgreSQL
              </p>
              <h1 className="mt-5 font-display text-4xl font-bold tracking-tight sm:text-5xl">
                CRM AI Project
              </h1>
              <p className="mt-4 max-w-2xl text-sm leading-6 text-slate-300 sm:text-base">
                Frontend Next.js com React e Tailwind CSS consumindo a API autenticada do CRM.
                O backend Java segue como nucleo de dominio e integracao com N8N, com PostgreSQL
                como runtime principal e H2 reservado aos testes.
              </p>
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <div className="rounded-3xl border border-white/10 bg-white/10 p-4">
                <p className="text-xs uppercase tracking-[0.25em] text-slate-300">Frontend</p>
                <p className="mt-2 font-display text-2xl font-semibold text-white">Next.js</p>
              </div>
              <div className="rounded-3xl border border-white/10 bg-white/10 p-4">
                <p className="text-xs uppercase tracking-[0.25em] text-slate-300">Persistencia</p>
                <p className="mt-2 font-display text-2xl font-semibold text-white">PostgreSQL</p>
              </div>
            </div>
          </div>
        </header>

        {feedback ? (
          <div className="rounded-2xl border border-cyanbrand/20 bg-cyanbrand/10 px-4 py-3 text-sm text-cyanbrand">
            {feedback}
          </div>
        ) : null}

        <section className="grid gap-4 md:grid-cols-3">
          {metricCards.map((card) => (
            <article
              key={card.key}
              className="rounded-[28px] border border-white/70 bg-white/80 p-5 shadow-panel backdrop-blur"
            >
              <p className="text-sm font-medium uppercase tracking-[0.22em] text-slate-400">{card.label}</p>
              <p className="mt-3 font-display text-4xl font-bold text-ink">
                {loading ? "..." : metrics[card.key]}
              </p>
            </article>
          ))}
        </section>

        <section className="grid gap-6 lg:grid-cols-[1.4fr_1fr]">
          <SectionCard
            title="Pipeline por Estagio"
            description="Visao financeira consolidada por etapa do funil comercial."
          >
            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
              {Object.entries(metrics.pipelineByStage || {}).map(([stage, amount]) => (
                <div key={stage} className="rounded-3xl bg-slate-50 p-4 ring-1 ring-slate-200">
                  <p className="text-xs uppercase tracking-[0.2em] text-slate-400">{stage}</p>
                  <p className="mt-3 text-2xl font-semibold text-ink">{formatCurrency(amount)}</p>
                </div>
              ))}
            </div>
          </SectionCard>

          <SectionCard
            title="Contexto da Arquitetura"
            description="Evolucao brownfield preservando o backend existente."
          >
            <ul className="space-y-3 text-sm leading-6 text-slate-600">
              <li>Frontend migrado de Thymeleaf para Next.js + React.</li>
              <li>Tailwind CSS aplicado na interface para padrao visual moderno.</li>
              <li>Flyway gerencia schema no runtime PostgreSQL.</li>
              <li>H2 permanece apenas para testes unitarios e de integracao.</li>
            </ul>
          </SectionCard>
        </section>

        <section className="grid gap-6 xl:grid-cols-3">
          <SectionCard title="Novo Contato" description="Cadastro rapido de leads e clientes no CRM.">
            <form
              className="space-y-3"
              onSubmit={(event) => {
                event.preventDefault();
                handleSubmit("/api/contacts", contactForm, () => setContactForm(initialContact));
              }}
            >
              <input
                className="field"
                placeholder="Nome"
                value={contactForm.name}
                onChange={(event) => setContactForm({ ...contactForm, name: event.target.value })}
                required
              />
              <input
                className="field"
                type="email"
                placeholder="Email"
                value={contactForm.email}
                onChange={(event) => setContactForm({ ...contactForm, email: event.target.value })}
                required
              />
              <input
                className="field"
                placeholder="Telefone"
                value={contactForm.phone}
                onChange={(event) => setContactForm({ ...contactForm, phone: event.target.value })}
              />
              <input
                className="field"
                placeholder="Empresa"
                value={contactForm.company}
                onChange={(event) => setContactForm({ ...contactForm, company: event.target.value })}
              />
              <select
                className="field"
                value={contactForm.status}
                onChange={(event) => setContactForm({ ...contactForm, status: event.target.value })}
              >
                <option value="LEAD">Lead</option>
                <option value="QUALIFIED">Qualified</option>
                <option value="CUSTOMER">Customer</option>
              </select>
              <button className="primary-button" type="submit">Criar contato</button>
            </form>
          </SectionCard>

          <SectionCard title="Novo Deal" description="Movimente o funil mantendo a API atual.">
            <form
              className="space-y-3"
              onSubmit={(event) => {
                event.preventDefault();
                handleSubmit(
                  "/api/deals",
                  {
                    ...dealForm,
                    value: Number(dealForm.value),
                    contactId: Number(dealForm.contactId)
                  },
                  () => setDealForm(initialDeal)
                );
              }}
            >
              <input
                className="field"
                placeholder="Titulo do deal"
                value={dealForm.title}
                onChange={(event) => setDealForm({ ...dealForm, title: event.target.value })}
                required
              />
              <input
                className="field"
                type="number"
                min="0"
                step="0.01"
                placeholder="Valor"
                value={dealForm.value}
                onChange={(event) => setDealForm({ ...dealForm, value: event.target.value })}
                required
              />
              <select
                className="field"
                value={dealForm.stage}
                onChange={(event) => setDealForm({ ...dealForm, stage: event.target.value })}
              >
                <option value="PROSPECTING">Prospecting</option>
                <option value="PROPOSAL">Proposal</option>
                <option value="NEGOTIATION">Negotiation</option>
                <option value="WON">Won</option>
                <option value="LOST">Lost</option>
              </select>
              <input
                className="field"
                type="date"
                value={dealForm.expectedCloseDate}
                onChange={(event) => setDealForm({ ...dealForm, expectedCloseDate: event.target.value })}
              />
              <input
                className="field"
                type="number"
                min="1"
                placeholder="ID do contato"
                value={dealForm.contactId}
                onChange={(event) => setDealForm({ ...dealForm, contactId: event.target.value })}
                required
              />
              <button className="primary-button" type="submit">Criar deal</button>
            </form>
          </SectionCard>

          <SectionCard title="Nova Atividade" description="Registre a interacao operacional no CRM.">
            <form
              className="space-y-3"
              onSubmit={(event) => {
                event.preventDefault();
                handleSubmit(
                  "/api/activities",
                  {
                    ...activityForm,
                    contactId: Number(activityForm.contactId),
                    dueAt: activityForm.dueAt ? new Date(activityForm.dueAt).toISOString() : null
                  },
                  () => setActivityForm(initialActivity)
                );
              }}
            >
              <select
                className="field"
                value={activityForm.type}
                onChange={(event) => setActivityForm({ ...activityForm, type: event.target.value })}
              >
                <option value="CALL">Call</option>
                <option value="EMAIL">Email</option>
                <option value="MEETING">Meeting</option>
                <option value="TASK">Task</option>
              </select>
              <input
                className="field"
                placeholder="Notas"
                value={activityForm.notes}
                onChange={(event) => setActivityForm({ ...activityForm, notes: event.target.value })}
                required
              />
              <input
                className="field"
                type="datetime-local"
                value={activityForm.dueAt}
                onChange={(event) => setActivityForm({ ...activityForm, dueAt: event.target.value })}
              />
              <input
                className="field"
                type="number"
                min="1"
                placeholder="ID do contato"
                value={activityForm.contactId}
                onChange={(event) => setActivityForm({ ...activityForm, contactId: event.target.value })}
                required
              />
              <button className="primary-button" type="submit">Criar atividade</button>
            </form>
          </SectionCard>
        </section>

        <section className="grid gap-6 xl:grid-cols-3">
          <SectionCard title="Contatos" description="Lista atual consumida diretamente da API do CRM.">
            <div className="table-shell">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Nome</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {contacts.map((contact) => (
                    <tr key={contact.id}>
                      <td>{contact.id}</td>
                      <td>
                        <div className="font-medium text-ink">{contact.name}</div>
                        <div className="text-xs text-slate-500">{contact.email}</div>
                      </td>
                      <td>{contact.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </SectionCard>

          <SectionCard title="Deals" description="Pipeline e relacionamento mantidos no backend atual.">
            <div className="table-shell">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Titulo</th>
                    <th>Estagio</th>
                  </tr>
                </thead>
                <tbody>
                  {deals.map((deal) => (
                    <tr key={deal.id}>
                      <td>{deal.id}</td>
                      <td>
                        <div className="font-medium text-ink">{deal.title}</div>
                        <div className="text-xs text-slate-500">{formatCurrency(deal.value)}</div>
                      </td>
                      <td>{deal.stage}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </SectionCard>

          <SectionCard title="Atividades" description="Historico operacional associado aos contatos do CRM.">
            <div className="table-shell">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Tipo</th>
                    <th>Prazo</th>
                  </tr>
                </thead>
                <tbody>
                  {activities.map((activity) => (
                    <tr key={activity.id}>
                      <td>{activity.id}</td>
                      <td>
                        <div className="font-medium text-ink">{activity.type}</div>
                        <div className="text-xs text-slate-500">{activity.notes}</div>
                      </td>
                      <td>{formatDate(activity.dueAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </SectionCard>
        </section>
      </div>
    </main>
  );
}
