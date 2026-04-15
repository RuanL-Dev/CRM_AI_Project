const api = {
    contacts: "/api/contacts",
    deals: "/api/deals",
    activities: "/api/activities",
    metrics: "/api/dashboard/metrics"
};

const asJson = (form) => Object.fromEntries(new FormData(form).entries());

async function request(url, options = {}) {
    const response = await fetch(url, {
        headers: { "Content-Type": "application/json" },
        ...options
    });

    if (!response.ok) {
        const contentType = response.headers.get("Content-Type") || "";
        if (contentType.includes("application/json")) {
            const body = await response.json();
            throw new Error(body.error || "Erro inesperado");
        }
        const text = await response.text();
        throw new Error(text || "Erro inesperado");
    }
    return response.status === 204 ? null : response.json();
}

function toast(message, error = false) {
    const box = document.createElement("div");
    box.className = `toast${error ? " error" : ""}`;
    box.textContent = message;
    document.body.appendChild(box);
    setTimeout(() => box.remove(), 3000);
}

function clearElement(element) {
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
}

function cell(value) {
    const td = document.createElement("td");
    td.textContent = value ?? "-";
    return td;
}

function metricCard(label, value) {
    const article = document.createElement("article");
    article.className = "metric-card";

    const p = document.createElement("p");
    p.textContent = label;

    const h3 = document.createElement("h3");
    h3.textContent = value;

    article.append(p, h3);
    return article;
}

async function refreshMetrics() {
    const data = await request(api.metrics);
    const pipeline = data.pipelineByStage || {};
    const metrics = document.getElementById("metrics");
    clearElement(metrics);

    metrics.append(
        metricCard("Contatos", String(data.contacts)),
        metricCard("Deals", String(data.deals)),
        metricCard("Atividades", String(data.activities)),
        metricCard("Pipeline WON", `R$ ${Number(pipeline.WON || 0).toFixed(2)}`)
    );
}

async function refreshContacts() {
    const contacts = await request(api.contacts);
    const table = document.getElementById("contactsTable");
    clearElement(table);

    contacts.forEach((contact) => {
        const tr = document.createElement("tr");
        tr.append(
            cell(contact.id),
            cell(contact.name),
            cell(contact.email),
            cell(contact.status),
            cell(contact.company || "-")
        );
        table.appendChild(tr);
    });
}

async function refreshDeals() {
    const deals = await request(api.deals);
    const table = document.getElementById("dealsTable");
    clearElement(table);

    deals.forEach((deal) => {
        const tr = document.createElement("tr");
        tr.append(
            cell(deal.id),
            cell(deal.title),
            cell(`R$ ${Number(deal.value).toFixed(2)}`),
            cell(deal.stage),
            cell(deal.contact?.name || deal.contact?.id || "-")
        );
        table.appendChild(tr);
    });
}

async function refreshActivities() {
    const activities = await request(api.activities);
    const table = document.getElementById("activitiesTable");
    clearElement(table);

    activities.forEach((activity) => {
        const tr = document.createElement("tr");
        tr.append(
            cell(activity.id),
            cell(activity.type),
            cell(activity.notes),
            cell(activity.dueAt ? new Date(activity.dueAt).toLocaleString() : "-"),
            cell(activity.contact?.name || activity.contact?.id || "-")
        );
        table.appendChild(tr);
    });
}

async function reloadAll() {
    await Promise.all([refreshMetrics(), refreshContacts(), refreshDeals(), refreshActivities()]);
}

function wireForms() {
    const contactForm = document.getElementById("contactForm");
    const dealForm = document.getElementById("dealForm");
    const activityForm = document.getElementById("activityForm");

    contactForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            await request(api.contacts, { method: "POST", body: JSON.stringify(asJson(contactForm)) });
            contactForm.reset();
            toast("Contato criado com sucesso.");
            await reloadAll();
        } catch (error) {
            toast(`Erro ao criar contato: ${error.message}`, true);
        }
    });

    dealForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            await request(api.deals, { method: "POST", body: JSON.stringify(asJson(dealForm)) });
            dealForm.reset();
            toast("Deal criado com sucesso.");
            await reloadAll();
        } catch (error) {
            toast(`Erro ao criar deal: ${error.message}`, true);
        }
    });

    activityForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = asJson(activityForm);
        if (payload.dueAt) {
            payload.dueAt = new Date(payload.dueAt).toISOString();
        }
        try {
            await request(api.activities, { method: "POST", body: JSON.stringify(payload) });
            activityForm.reset();
            toast("Atividade criada com sucesso.");
            await reloadAll();
        } catch (error) {
            toast(`Erro ao criar atividade: ${error.message}`, true);
        }
    });
}

document.addEventListener("DOMContentLoaded", async () => {
    wireForms();
    try {
        await reloadAll();
    } catch (error) {
        toast(`Falha ao carregar dashboard: ${error.message}`, true);
    }
});
