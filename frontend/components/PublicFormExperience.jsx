"use client";

import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "next/navigation";

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

  return response.json();
}

function fieldType(question) {
  if (question.questionType === "EMAIL") {
    return "email";
  }
  if (question.questionType === "PHONE") {
    return "tel";
  }
  return "text";
}

export default function PublicFormExperience() {
  const searchParams = useSearchParams();
  const slug = searchParams.get("slug") || "";
  const [form, setForm] = useState(null);
  const [answers, setAnswers] = useState({});
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [completed, setCompleted] = useState(false);

  useEffect(() => {
    if (!slug) {
      setLoading(false);
      return;
    }

    let cancelled = false;
    setLoading(true);
    setCompleted(false);
    setError("");
    setAnswers({});
    setStep(0);

    fetchJson(`/api/public/forms/${encodeURIComponent(slug)}`)
      .then((payload) => {
        if (!cancelled) {
          setForm(payload);
        }
      })
      .catch((fetchError) => {
        if (!cancelled) {
          setError(fetchError.message || "Nao foi possivel carregar o formulario.");
          setForm(null);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [slug]);

  const questions = useMemo(() => form?.questions || [], [form]);
  const currentQuestion = questions[step];
  const progress = questions.length ? ((step + 1) / questions.length) * 100 : 0;

  function currentValue() {
    return answers[currentQuestion?.id] || "";
  }

  function canAdvance() {
    if (!currentQuestion) {
      return false;
    }
    if (!currentQuestion.required) {
      return true;
    }
    const value = currentValue();
    return Array.isArray(value) ? value.length > 0 : String(value || "").trim().length > 0;
  }

  function setAnswer(questionId, value) {
    setAnswers((current) => ({
      ...current,
      [questionId]: value
    }));
  }

  async function submit() {
    if (!form) {
      return;
    }

    setSubmitting(true);
    setError("");

    try {
      await fetchJson(`/api/public/forms/${encodeURIComponent(form.slug)}/responses`, {
        method: "POST",
        body: JSON.stringify({
          answers: questions.map((question) => ({
            questionId: question.id,
            value: Array.isArray(answers[question.id]) ? answers[question.id].join(", ") : String(answers[question.id] || "")
          }))
        })
      });
      setCompleted(true);
    } catch (submitError) {
      setError(submitError.message || "Nao foi possivel enviar suas respostas.");
    } finally {
      setSubmitting(false);
    }
  }

  function handleNext() {
    if (!canAdvance()) {
      setError("Preencha a resposta para continuar.");
      return;
    }

    setError("");
    if (step === questions.length - 1) {
      submit();
      return;
    }
    setStep((current) => current + 1);
  }

  if (loading) {
    return <main className="public-form-shell"><div className="public-form-card">Carregando experiencia...</div></main>;
  }

  if (!slug) {
    return (
      <main className="public-form-shell">
        <div className="public-form-card">
          <span className="public-form-brand">AIOX Forms</span>
          <h1 className="public-form-title">Informe o slug do formulario</h1>
          <p className="public-form-description">Abra esta pagina com um endereco como <strong>/formulario?slug=natal-premium</strong>.</p>
        </div>
      </main>
    );
  }

  if (error && !form) {
    return <main className="public-form-shell"><div className="public-form-card">{error}</div></main>;
  }

  if (completed && form) {
    return (
      <main className="public-form-shell">
        <div className="public-form-stage">
          <div className="public-form-meta">
            <span className="public-form-brand">AIOX Forms</span>
          </div>
          <div className="public-form-card public-form-success">
            <span className="public-form-counter">100%</span>
            <h1 className="public-form-title">{form.successTitle}</h1>
            <p className="public-form-description">{form.successMessage}</p>
          </div>
        </div>
      </main>
    );
  }

  if (!form || !currentQuestion) {
    return <main className="public-form-shell"><div className="public-form-card">Formulario sem perguntas publicadas.</div></main>;
  }

  return (
    <main className="public-form-shell">
      <div className="public-form-stage">
        <div className="public-form-meta">
          <span className="public-form-brand">AIOX Forms</span>
          <span className="public-form-counter">{step + 1}/{questions.length}</span>
        </div>

        <div className="public-form-progress">
          <div className="public-form-progress-bar" style={{ width: `${progress}%` }} />
        </div>

        <div className="public-form-content">
          <p className="public-form-caption">{form.headline}</p>
          <h1 className="public-form-title">{currentQuestion.label}</h1>
          {currentQuestion.description ? <p className="public-form-description">{currentQuestion.description}</p> : null}

          {currentQuestion.questionType === "LONG_TEXT" ? (
            <textarea
              className="public-form-input public-form-textarea"
              placeholder={currentQuestion.placeholder || "Digite sua resposta"}
              value={currentValue()}
              onChange={(event) => setAnswer(currentQuestion.id, event.target.value)}
            />
          ) : null}

          {currentQuestion.questionType === "MULTIPLE_CHOICE" ? (
            <div className="public-form-options">
              {currentQuestion.options.map((option) => {
                const selected = currentValue() === option;
                return (
                  <button
                    key={option}
                    type="button"
                    className={`public-form-option ${selected ? "public-form-option-active" : ""}`}
                    onClick={() => setAnswer(currentQuestion.id, option)}
                  >
                    {option}
                  </button>
                );
              })}
            </div>
          ) : null}

          {currentQuestion.questionType !== "LONG_TEXT" && currentQuestion.questionType !== "MULTIPLE_CHOICE" ? (
            <input
              className="public-form-input"
              type={fieldType(currentQuestion)}
              placeholder={currentQuestion.placeholder || "Digite sua resposta"}
              value={currentValue()}
              onChange={(event) => setAnswer(currentQuestion.id, event.target.value)}
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  event.preventDefault();
                  handleNext();
                }
              }}
            />
          ) : null}

          {error ? <p className="public-form-error">{error}</p> : null}
        </div>

        <div className="public-form-actions">
          <button className="public-form-link" type="button" onClick={() => setStep((current) => Math.max(current - 1, 0))} disabled={step === 0 || submitting}>
            voltar
          </button>
          <button className="public-form-button" type="button" onClick={handleNext} disabled={submitting}>
            {submitting ? "Enviando..." : step === questions.length - 1 ? form.submitLabel : "OK"}
          </button>
        </div>
      </div>
    </main>
  );
}
