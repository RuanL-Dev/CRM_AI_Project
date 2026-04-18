"use client";

import { useEffect, useMemo, useState } from "react";

const STARTER_EMAIL_HTML = `
<div style="background:#07111f;padding:40px 24px;font-family:Arial,sans-serif;color:#e2f5ff">
  <div style="max-width:640px;margin:0 auto;background:linear-gradient(180deg,#10223e,#0a1222);border:1px solid rgba(103,232,249,.18);border-radius:28px;overflow:hidden;box-shadow:0 30px 80px rgba(0,0,0,.35)">
    <div style="padding:40px 40px 20px;background:radial-gradient(circle at top left,rgba(103,232,249,.22),transparent 34%),radial-gradient(circle at bottom right,rgba(45,212,191,.16),transparent 28%)">
      <span style="display:inline-block;padding:8px 14px;border-radius:999px;background:rgba(103,232,249,.14);color:#67e8f9;font-size:12px;letter-spacing:.2em;text-transform:uppercase">Campanha premium</span>
      <h1 style="margin:22px 0 14px;font-size:34px;line-height:1.1;color:#fff">Sua campanha de Natal com cara de marca forte</h1>
      <p style="margin:0;font-size:16px;line-height:1.7;color:#cbd5e1">Monte mensagens elegantes, segmente com precisão e entregue uma experiência que parece peça de brand, não disparo genérico.</p>
    </div>
    <div style="padding:0 40px 40px">
      <div style="padding:26px 0;border-top:1px solid rgba(148,163,184,.14);border-bottom:1px solid rgba(148,163,184,.14);color:#cbd5e1;line-height:1.8">
        <p style="margin:0 0 12px">Olá, {{nome}}.</p>
        <p style="margin:0 0 12px">Selecionamos uma curadoria especial para quem já demonstrou interesse em produtos sofisticados. Aproveite a janela promocional e ative sua próxima campanha com segmentação refinada.</p>
        <p style="margin:0">Troque este HTML para personalizar layout, copy e CTA do jeito que sua operação precisar.</p>
      </div>
      <a href="https://srv1588289.hstgr.cloud" style="display:inline-block;margin-top:26px;padding:16px 24px;border-radius:18px;background:linear-gradient(90deg,#67e8f9,#2dd4bf);color:#04101d;text-decoration:none;font-weight:700">Quero ver a coleção</a>
    </div>
  </div>
</div>
`.trim();

const starterFormQuestions = [
  { fieldKey: "name", label: "Qual e o seu nome?", questionType: "SHORT_TEXT", placeholder: "Digite seu nome", description: "Queremos falar com voce do jeito certo.", required: true, positionIndex: 0, options: [] },
  { fieldKey: "email", label: "Qual e o melhor email para te mandar ofertas?", questionType: "EMAIL", placeholder: "nome@empresa.com", description: "So enviaremos comunicacoes relevantes.", required: true, positionIndex: 1, options: [] },
  { fieldKey: "interesse", label: "Qual linha faz mais sentido para voce?", questionType: "MULTIPLE_CHOICE", placeholder: "", description: "Escolha o segmento que mais conversa com seu momento.", required: true, positionIndex: 2, options: ["Sapatos bico fino", "Sandalias premium", "Bolsas executivas"] }
];

async function readError(response) {
  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    const payload = await response.json().catch(() => null);
    if (payload?.error) {
      return payload.error;
    }
  }
  return (await response.text().catch(() => "")) || "Falha na requisicao.";
}

async function fetchJson(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    }
  });

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

let csrfState = null;

async function ensureCsrf() {
  if (csrfState) {
    return csrfState;
  }
  csrfState = await fetchJson("/api/security/csrf");
  return csrfState;
}

function buttonLabel(submitting, idle, busy) {
  return submitting ? busy : idle;
}

function MetricPill({ label, value, detail }) {
  return (
    <article className="marketing-metric">
      <span className="marketing-metric-label">{label}</span>
      <strong className="marketing-metric-value">{value}</strong>
      <span className="marketing-metric-detail">{detail}</span>
    </article>
  );
}

function formatNumber(value) {
  return new Intl.NumberFormat("pt-BR").format(Number(value || 0));
}

function formatDateTime(value) {
  if (!value) {
    return "-";
  }

  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short"
  }).format(new Date(value));
}

function updateQuestion(questions, index, nextQuestion) {
  return questions.map((question, currentIndex) => (currentIndex === index ? nextQuestion : question));
}

export default function MarketingWorkspace() {
  const [metrics, setMetrics] = useState(null);
  const [contacts, setContacts] = useState([]);
  const [providers, setProviders] = useState([]);
  const [segments, setSegments] = useState([]);
  const [campaigns, setCampaigns] = useState([]);
  const [forms, setForms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [testEmail, setTestEmail] = useState("");
  const [submitting, setSubmitting] = useState({});
  const [providerForm, setProviderForm] = useState({
    name: "Hostinger principal",
    providerType: "HOSTINGER",
    host: "smtp.hostinger.com",
    port: 587,
    username: "",
    password: "",
    fromEmail: "",
    fromName: "AIOX Campaigns",
    replyTo: "",
    tlsEnabled: true,
    active: true
  });
  const [segmentForm, setSegmentForm] = useState({
    name: "Compradores de sapatos bico fino",
    description: "Leads que demonstraram interesse em modelos premium e acabamentos sofisticados.",
    color: "#67e8f9",
    contactIds: []
  });
  const [campaignForm, setCampaignForm] = useState({
    name: "Natal elegancia premium",
    subject: "Natal sofisticado: sua selecao especial chegou",
    previewText: "Uma campanha refinada para segmentos de alto interesse.",
    senderName: "Curadoria AIOX",
    providerId: "",
    segmentIds: [],
    htmlContent: STARTER_EMAIL_HTML,
    plainTextContent: "Sua campanha de Natal premium esta pronta para disparo."
  });
  const [formBuilder, setFormBuilder] = useState({
    name: "Formulario natal premium",
    slug: "natal-premium",
    headline: "Descubra a campanha perfeita para o seu estilo",
    description: "Uma experiencia curta, elegante e orientada por segmento.",
    submitLabel: "Continuar",
    successTitle: "Perfeito. Sua curadoria esta a caminho.",
    successMessage: "Em instantes voce entra no fluxo certo da campanha.",
    targetSegmentId: "",
    active: true,
    questions: starterFormQuestions
  });

  const contactsById = useMemo(
    () => new Map(contacts.map((contact) => [contact.id, contact])),
    [contacts]
  );

  async function loadWorkspace() {
    setLoading(true);

    try {
      const [metricsResponse, contactsResponse, providersResponse, segmentsResponse, campaignsResponse, formsResponse] = await Promise.all([
        fetchJson("/api/dashboard/metrics"),
        fetchJson("/api/contacts"),
        fetchJson("/api/email-providers"),
        fetchJson("/api/segments"),
        fetchJson("/api/campaigns"),
        fetchJson("/api/forms")
      ]);

      setMetrics(metricsResponse);
      setContacts(contactsResponse);
      setProviders(providersResponse);
      setSegments(segmentsResponse);
      setCampaigns(campaignsResponse);
      setForms(formsResponse);
      setFeedback({ type: "", message: "" });
    } catch (error) {
      setFeedback({ type: "error", message: error.message || "Nao foi possivel carregar o workspace." });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadWorkspace();
  }, []);

  useEffect(() => {
    if (!campaignForm.providerId && providers.length) {
      setCampaignForm((current) => ({ ...current, providerId: String(providers[0].id) }));
    }
  }, [providers, campaignForm.providerId]);

  useEffect(() => {
    if (!formBuilder.targetSegmentId && segments.length) {
      setFormBuilder((current) => ({ ...current, targetSegmentId: String(segments[0].id) }));
    }
  }, [segments, formBuilder.targetSegmentId]);

  async function submitWithCsrf(kind, path, body, onSuccess) {
    setSubmitting((current) => ({ ...current, [kind]: true }));

    try {
      const csrf = await ensureCsrf();
      await fetchJson(path, {
        method: "POST",
        headers: {
          [csrf.headerName]: csrf.token
        },
        body: JSON.stringify(body)
      });
      await loadWorkspace();
      setFeedback({ type: "success", message: "Operacao concluida com sucesso." });
      onSuccess?.();
    } catch (error) {
      setFeedback({ type: "error", message: error.message || "Nao foi possivel concluir a operacao." });
    } finally {
      setSubmitting((current) => ({ ...current, [kind]: false }));
    }
  }

  async function testProvider(providerId) {
    if (!testEmail) {
      setFeedback({ type: "error", message: "Informe o email de teste antes de validar o provedor." });
      return;
    }

    await submitWithCsrf(
      `provider-test-${providerId}`,
      `/api/email-providers/${providerId}/test`,
      { recipientEmail: testEmail }
    );
  }

  async function dispatchCampaign(campaignId) {
    await submitWithCsrf(`dispatch-${campaignId}`, `/api/campaigns/${campaignId}/dispatch`, {});
  }

  const totalSegmentedContacts = segments.reduce((sum, segment) => sum + Number(segment.contactCount || 0), 0);
  const totalCampaignDeliveries = campaigns.reduce((sum, campaign) => sum + Number(campaign.deliveries?.length || 0), 0);

  return (
    <main className="marketing-shell">
      <div className="marketing-grid">
        <header className="marketing-hero">
          <div>
            <p className="marketing-kicker">AIOX Growth OS</p>
            <h1 className="marketing-title">Campanhas, segmentos, provedores SMTP e formularios em uma unica operacao</h1>
            <p className="marketing-copy">
              Conecte Hostinger, Gmail ou qualquer SMTP, separe leads por segmento, crie campanhas visuais com HTML premium
              e publique formularios no estilo Typeform que alimentam automaticamente seu CRM.
            </p>
          </div>

          <div className="marketing-hero-actions">
            <a className="marketing-link-button" href="/admin">Workspace admin</a>
            <a className="marketing-link-button marketing-link-button-secondary" href={`/formulario?slug=${encodeURIComponent(formBuilder.slug)}`}>Abrir formulario publico</a>
          </div>
        </header>

        {feedback.message ? (
          <div className={`marketing-feedback ${feedback.type === "error" ? "marketing-feedback-error" : ""}`}>
            {feedback.message}
          </div>
        ) : null}

        <section className="marketing-metrics-row">
          <MetricPill label="Contatos no CRM" value={loading ? "..." : formatNumber(metrics?.contacts)} detail="Base disponivel para segmentacao." />
          <MetricPill label="Contatos segmentados" value={loading ? "..." : formatNumber(totalSegmentedContacts)} detail="Volume total associado a segmentos." />
          <MetricPill label="Disparos registrados" value={loading ? "..." : formatNumber(totalCampaignDeliveries)} detail="Historico de entregas por campanha." />
          <MetricPill label="Formularios ativos" value={loading ? "..." : formatNumber(forms.filter((form) => form.active).length)} detail="Captacao publica pronta para uso." />
        </section>

        <section className="marketing-columns">
          <section className="marketing-panel">
            <div className="marketing-panel-heading">
              <p className="marketing-section-tag">SMTP e envio</p>
              <h2>Provedores de email</h2>
              <p>Cadastre Hostinger, Gmail com App Password ou qualquer outro SMTP padrao.</p>
            </div>

            <form
              className="marketing-form"
              onSubmit={(event) => {
                event.preventDefault();
                submitWithCsrf("create-provider", "/api/email-providers", providerForm, () => {
                  setProviderForm((current) => ({ ...current, username: "", password: "", fromEmail: "", replyTo: "" }));
                });
              }}
            >
              <div className="marketing-form-grid">
                <input className="marketing-field" placeholder="Nome do provedor" value={providerForm.name} onChange={(event) => setProviderForm({ ...providerForm, name: event.target.value })} required />
                <select className="marketing-field" value={providerForm.providerType} onChange={(event) => setProviderForm({ ...providerForm, providerType: event.target.value })}>
                  <option value="HOSTINGER">Hostinger</option>
                  <option value="GMAIL">Gmail</option>
                  <option value="SMTP">Outro SMTP</option>
                </select>
                <input className="marketing-field" placeholder="SMTP host" value={providerForm.host} onChange={(event) => setProviderForm({ ...providerForm, host: event.target.value })} required />
                <input className="marketing-field" type="number" placeholder="Porta" value={providerForm.port} onChange={(event) => setProviderForm({ ...providerForm, port: Number(event.target.value) })} required />
                <input className="marketing-field" placeholder="Usuario SMTP" value={providerForm.username} onChange={(event) => setProviderForm({ ...providerForm, username: event.target.value })} required />
                <input className="marketing-field" type="password" placeholder="Senha SMTP" value={providerForm.password} onChange={(event) => setProviderForm({ ...providerForm, password: event.target.value })} required />
                <input className="marketing-field" type="email" placeholder="From email" value={providerForm.fromEmail} onChange={(event) => setProviderForm({ ...providerForm, fromEmail: event.target.value })} required />
                <input className="marketing-field" placeholder="From name" value={providerForm.fromName} onChange={(event) => setProviderForm({ ...providerForm, fromName: event.target.value })} />
                <input className="marketing-field" type="email" placeholder="Reply-to" value={providerForm.replyTo} onChange={(event) => setProviderForm({ ...providerForm, replyTo: event.target.value })} />
              </div>

              <div className="marketing-toggle-row">
                <label><input type="checkbox" checked={providerForm.tlsEnabled} onChange={(event) => setProviderForm({ ...providerForm, tlsEnabled: event.target.checked })} /> TLS ativo</label>
                <label><input type="checkbox" checked={providerForm.active} onChange={(event) => setProviderForm({ ...providerForm, active: event.target.checked })} /> Provedor ativo</label>
              </div>

              <button className="marketing-primary-button" type="submit" disabled={submitting["create-provider"]}>
                {buttonLabel(submitting["create-provider"], "Salvar provedor", "Salvando provedor...")}
              </button>
            </form>

            <div className="marketing-inline-actions">
              <input className="marketing-field" type="email" placeholder="email@teste.com" value={testEmail} onChange={(event) => setTestEmail(event.target.value)} />
            </div>

            <div className="marketing-card-list">
              {providers.map((provider) => (
                <article key={provider.id} className="marketing-card">
                  <div>
                    <strong>{provider.name}</strong>
                    <p>{provider.providerType} • {provider.host}:{provider.port}</p>
                    <p>{provider.fromEmail}</p>
                  </div>
                  <button className="marketing-secondary-button" type="button" onClick={() => testProvider(provider.id)} disabled={submitting[`provider-test-${provider.id}`]}>
                    {buttonLabel(submitting[`provider-test-${provider.id}`], "Testar", "Testando...")}
                  </button>
                </article>
              ))}
            </div>
          </section>

          <section className="marketing-panel">
            <div className="marketing-panel-heading">
              <p className="marketing-section-tag">Audience ops</p>
              <h2>Segmentos de contatos</h2>
              <p>Crie grupos manuais que servem de audiencia para campanhas e formularios.</p>
            </div>

            <form
              className="marketing-form"
              onSubmit={(event) => {
                event.preventDefault();
                submitWithCsrf("create-segment", "/api/segments", segmentForm, () => {
                  setSegmentForm((current) => ({ ...current, name: "", description: "", contactIds: [] }));
                });
              }}
            >
              <input className="marketing-field" placeholder="Nome do segmento" value={segmentForm.name} onChange={(event) => setSegmentForm({ ...segmentForm, name: event.target.value })} required />
              <textarea className="marketing-field marketing-textarea" placeholder="Descricao estrategica" value={segmentForm.description} onChange={(event) => setSegmentForm({ ...segmentForm, description: event.target.value })} />
              <input className="marketing-field" placeholder="Cor" value={segmentForm.color} onChange={(event) => setSegmentForm({ ...segmentForm, color: event.target.value })} />

              <div className="marketing-check-grid">
                {contacts.map((contact) => (
                  <label key={contact.id} className="marketing-check-card">
                    <input
                      type="checkbox"
                      checked={segmentForm.contactIds.includes(contact.id)}
                      onChange={(event) => {
                        const contactIds = event.target.checked
                          ? [...segmentForm.contactIds, contact.id]
                          : segmentForm.contactIds.filter((id) => id !== contact.id);
                        setSegmentForm({ ...segmentForm, contactIds });
                      }}
                    />
                    <span>{contact.name}</span>
                    <small>{contact.email}</small>
                  </label>
                ))}
              </div>

              <button className="marketing-primary-button" type="submit" disabled={submitting["create-segment"]}>
                {buttonLabel(submitting["create-segment"], "Criar segmento", "Criando segmento...")}
              </button>
            </form>

            <div className="marketing-stack-list">
              {segments.map((segment) => (
                <article key={segment.id} className="marketing-stack-card">
                  <div className="marketing-stack-head">
                    <strong>{segment.name}</strong>
                    <span className="marketing-badge" style={{ borderColor: segment.color || "#67e8f9", color: segment.color || "#67e8f9" }}>
                      {segment.contactCount} contatos
                    </span>
                  </div>
                  <p>{segment.description || "Sem descricao."}</p>
                  <div className="marketing-chip-wrap">
                    {segment.contacts.slice(0, 5).map((contact) => (
                      <span key={contact.id} className="marketing-chip">{contact.name}</span>
                    ))}
                  </div>
                </article>
              ))}
            </div>
          </section>
        </section>

        <section className="marketing-columns">
          <section className="marketing-panel marketing-panel-large">
            <div className="marketing-panel-heading">
              <p className="marketing-section-tag">Campaign studio</p>
              <h2>Campanhas de email</h2>
              <p>Monte a mensagem, conecte segmentos e dispare com o provedor escolhido.</p>
            </div>

            <form
              className="marketing-form"
              onSubmit={(event) => {
                event.preventDefault();
                submitWithCsrf(
                  "create-campaign",
                  "/api/campaigns",
                  {
                    ...campaignForm,
                    providerId: campaignForm.providerId ? Number(campaignForm.providerId) : null,
                    segmentIds: campaignForm.segmentIds.map(Number),
                    status: "READY"
                  },
                  () => {
                    setCampaignForm((current) => ({ ...current, name: "", subject: "", previewText: "" }));
                  }
                );
              }}
            >
              <div className="marketing-form-grid">
                <input className="marketing-field" placeholder="Nome da campanha" value={campaignForm.name} onChange={(event) => setCampaignForm({ ...campaignForm, name: event.target.value })} required />
                <input className="marketing-field" placeholder="Assunto" value={campaignForm.subject} onChange={(event) => setCampaignForm({ ...campaignForm, subject: event.target.value })} required />
                <input className="marketing-field" placeholder="Preview text" value={campaignForm.previewText} onChange={(event) => setCampaignForm({ ...campaignForm, previewText: event.target.value })} />
                <input className="marketing-field" placeholder="Assinatura do remetente" value={campaignForm.senderName} onChange={(event) => setCampaignForm({ ...campaignForm, senderName: event.target.value })} />
                <select className="marketing-field" value={campaignForm.providerId} onChange={(event) => setCampaignForm({ ...campaignForm, providerId: event.target.value })} required>
                  <option value="">Selecione o provedor</option>
                  {providers.map((provider) => (
                    <option key={provider.id} value={provider.id}>{provider.name}</option>
                  ))}
                </select>
              </div>

              <div className="marketing-check-grid">
                {segments.map((segment) => (
                  <label key={segment.id} className="marketing-check-card">
                    <input
                      type="checkbox"
                      checked={campaignForm.segmentIds.includes(String(segment.id))}
                      onChange={(event) => {
                        const segmentIds = event.target.checked
                          ? [...campaignForm.segmentIds, String(segment.id)]
                          : campaignForm.segmentIds.filter((id) => id !== String(segment.id));
                        setCampaignForm({ ...campaignForm, segmentIds });
                      }}
                    />
                    <span>{segment.name}</span>
                    <small>{segment.contactCount} destinatarios potenciais</small>
                  </label>
                ))}
              </div>

              <textarea className="marketing-field marketing-textarea marketing-textarea-xl" placeholder="HTML da campanha" value={campaignForm.htmlContent} onChange={(event) => setCampaignForm({ ...campaignForm, htmlContent: event.target.value })} required />
              <textarea className="marketing-field marketing-textarea" placeholder="Versao texto puro" value={campaignForm.plainTextContent} onChange={(event) => setCampaignForm({ ...campaignForm, plainTextContent: event.target.value })} />

              <button className="marketing-primary-button" type="submit" disabled={submitting["create-campaign"]}>
                {buttonLabel(submitting["create-campaign"], "Salvar campanha", "Salvando campanha...")}
              </button>
            </form>

            <div className="marketing-email-preview" dangerouslySetInnerHTML={{ __html: campaignForm.htmlContent }} />

            <div className="marketing-stack-list">
              {campaigns.map((campaign) => (
                <article key={campaign.id} className="marketing-stack-card">
                  <div className="marketing-stack-head">
                    <div>
                      <strong>{campaign.name}</strong>
                      <p>{campaign.subject}</p>
                    </div>
                    <span className="marketing-badge">{campaign.status}</span>
                  </div>
                  <p>Segmentos: {campaign.segments.map((segment) => segment.name).join(", ") || "Nenhum"}</p>
                  <p>Entregas registradas: {campaign.deliveries.length}</p>
                  <div className="marketing-inline-actions">
                    <button className="marketing-secondary-button" type="button" onClick={() => dispatchCampaign(campaign.id)} disabled={submitting[`dispatch-${campaign.id}`]}>
                      {buttonLabel(submitting[`dispatch-${campaign.id}`], "Disparar agora", "Disparando...")}
                    </button>
                  </div>
                  {campaign.deliveries.length ? (
                    <div className="marketing-delivery-list">
                      {campaign.deliveries.slice(0, 4).map((delivery) => (
                        <div key={delivery.id} className="marketing-delivery-row">
                          <span>{delivery.contact.name}</span>
                          <span>{delivery.status}</span>
                          <small>{formatDateTime(delivery.sentAt || delivery.createdAt)}</small>
                        </div>
                      ))}
                    </div>
                  ) : null}
                </article>
              ))}
            </div>
          </section>

          <section className="marketing-panel">
            <div className="marketing-panel-heading">
              <p className="marketing-section-tag">Typeform-like capture</p>
              <h2>Construtor de formulario</h2>
              <p>Uma pergunta por vez, lead enrichment e roteamento automatico para segmento.</p>
            </div>

            <form
              className="marketing-form"
              onSubmit={(event) => {
                event.preventDefault();
                submitWithCsrf(
                  "create-form",
                  "/api/forms",
                  {
                    ...formBuilder,
                    targetSegmentId: formBuilder.targetSegmentId ? Number(formBuilder.targetSegmentId) : null,
                    questions: formBuilder.questions.map((question, index) => ({
                      ...question,
                      positionIndex: index,
                      options: (question.options || []).filter(Boolean)
                    }))
                  }
                );
              }}
            >
              <input className="marketing-field" placeholder="Nome interno" value={formBuilder.name} onChange={(event) => setFormBuilder({ ...formBuilder, name: event.target.value })} required />
              <input className="marketing-field" placeholder="Slug publico" value={formBuilder.slug} onChange={(event) => setFormBuilder({ ...formBuilder, slug: event.target.value.toLowerCase().replace(/\s+/g, "-") })} required />
              <input className="marketing-field" placeholder="Headline" value={formBuilder.headline} onChange={(event) => setFormBuilder({ ...formBuilder, headline: event.target.value })} required />
              <textarea className="marketing-field marketing-textarea" placeholder="Descricao" value={formBuilder.description} onChange={(event) => setFormBuilder({ ...formBuilder, description: event.target.value })} />
              <input className="marketing-field" placeholder="Texto do botao final" value={formBuilder.submitLabel} onChange={(event) => setFormBuilder({ ...formBuilder, submitLabel: event.target.value })} required />
              <input className="marketing-field" placeholder="Titulo de sucesso" value={formBuilder.successTitle} onChange={(event) => setFormBuilder({ ...formBuilder, successTitle: event.target.value })} required />
              <textarea className="marketing-field marketing-textarea" placeholder="Mensagem de sucesso" value={formBuilder.successMessage} onChange={(event) => setFormBuilder({ ...formBuilder, successMessage: event.target.value })} required />
              <select className="marketing-field" value={formBuilder.targetSegmentId} onChange={(event) => setFormBuilder({ ...formBuilder, targetSegmentId: event.target.value })}>
                <option value="">Sem segmento automatico</option>
                {segments.map((segment) => (
                  <option key={segment.id} value={segment.id}>{segment.name}</option>
                ))}
              </select>

              <div className="marketing-question-list">
                {formBuilder.questions.map((question, index) => (
                  <article key={`${question.fieldKey}-${index}`} className="marketing-question-card">
                    <div className="marketing-question-toolbar">
                      <strong>Pergunta {index + 1}</strong>
                      <button
                        className="marketing-text-button"
                        type="button"
                        onClick={() => setFormBuilder({ ...formBuilder, questions: formBuilder.questions.filter((_, currentIndex) => currentIndex !== index) })}
                      >
                        remover
                      </button>
                    </div>

                    <input
                      className="marketing-field"
                      placeholder="fieldKey"
                      value={question.fieldKey}
                      onChange={(event) => setFormBuilder({
                        ...formBuilder,
                        questions: updateQuestion(formBuilder.questions, index, { ...question, fieldKey: event.target.value })
                      })}
                    />
                    <input
                      className="marketing-field"
                      placeholder="Pergunta"
                      value={question.label}
                      onChange={(event) => setFormBuilder({
                        ...formBuilder,
                        questions: updateQuestion(formBuilder.questions, index, { ...question, label: event.target.value })
                      })}
                    />
                    <textarea
                      className="marketing-field marketing-textarea"
                      placeholder="Descricao"
                      value={question.description}
                      onChange={(event) => setFormBuilder({
                        ...formBuilder,
                        questions: updateQuestion(formBuilder.questions, index, { ...question, description: event.target.value })
                      })}
                    />
                    <input
                      className="marketing-field"
                      placeholder="Placeholder"
                      value={question.placeholder}
                      onChange={(event) => setFormBuilder({
                        ...formBuilder,
                        questions: updateQuestion(formBuilder.questions, index, { ...question, placeholder: event.target.value })
                      })}
                    />
                    <select
                      className="marketing-field"
                      value={question.questionType}
                      onChange={(event) => setFormBuilder({
                        ...formBuilder,
                        questions: updateQuestion(formBuilder.questions, index, { ...question, questionType: event.target.value })
                      })}
                    >
                      <option value="SHORT_TEXT">Texto curto</option>
                      <option value="EMAIL">Email</option>
                      <option value="PHONE">Telefone</option>
                      <option value="LONG_TEXT">Texto longo</option>
                      <option value="MULTIPLE_CHOICE">Multipla escolha</option>
                    </select>
                    <label className="marketing-inline-check">
                      <input
                        type="checkbox"
                        checked={question.required}
                        onChange={(event) => setFormBuilder({
                          ...formBuilder,
                          questions: updateQuestion(formBuilder.questions, index, { ...question, required: event.target.checked })
                        })}
                      />
                      Resposta obrigatoria
                    </label>
                    {question.questionType === "MULTIPLE_CHOICE" ? (
                      <textarea
                        className="marketing-field marketing-textarea"
                        placeholder="Uma opcao por linha"
                        value={(question.options || []).join("\n")}
                        onChange={(event) => setFormBuilder({
                          ...formBuilder,
                          questions: updateQuestion(formBuilder.questions, index, { ...question, options: event.target.value.split("\n") })
                        })}
                      />
                    ) : null}
                  </article>
                ))}
              </div>

              <button
                className="marketing-secondary-button"
                type="button"
                onClick={() => setFormBuilder({
                  ...formBuilder,
                  questions: [
                    ...formBuilder.questions,
                    {
                      fieldKey: `campo_${formBuilder.questions.length + 1}`,
                      label: "Nova pergunta",
                      questionType: "SHORT_TEXT",
                      placeholder: "",
                      description: "",
                      required: true,
                      positionIndex: formBuilder.questions.length,
                      options: []
                    }
                  ]
                })}
              >
                Adicionar pergunta
              </button>

              <button className="marketing-primary-button" type="submit" disabled={submitting["create-form"]}>
                {buttonLabel(submitting["create-form"], "Publicar formulario", "Publicando formulario...")}
              </button>
            </form>

            <div className="marketing-stack-list">
              {forms.map((form) => (
                <article key={form.id} className="marketing-stack-card">
                  <div className="marketing-stack-head">
                    <strong>{form.name}</strong>
                    <span className="marketing-badge">{form.active ? "ATIVO" : "RASCUNHO"}</span>
                  </div>
                  <p>{form.headline}</p>
                  <p>Slug: /formulario?slug={form.slug}</p>
                  <p>Respostas: {form.responses.length}</p>
                  <div className="marketing-inline-actions">
                    <a className="marketing-secondary-button marketing-link-button-secondary" href={`/formulario?slug=${encodeURIComponent(form.slug)}`} target="_blank">
                      Ver experiencia publica
                    </a>
                  </div>
                </article>
              ))}
            </div>
          </section>
        </section>

        <section className="marketing-panel">
          <div className="marketing-panel-heading">
            <p className="marketing-section-tag">CRM source of truth</p>
            <h2>Base atual aproveitada pela automacao</h2>
            <p>Os segmentos novos partem da sua base real de contatos existente, sem duplicar dominio.</p>
          </div>

          <div className="marketing-contact-grid">
            {contacts.map((contact) => (
              <article key={contact.id} className="marketing-contact-card">
                <strong>{contact.name}</strong>
                <p>{contact.email}</p>
                <span>{contact.company || "Sem empresa"} • {contact.status}</span>
                <small>{segments.filter((segment) => segment.contacts.some((item) => item.id === contact.id)).map((segment) => segment.name).join(", ") || "Sem segmento"}</small>
              </article>
            ))}
          </div>
        </section>
      </div>
    </main>
  );
}
