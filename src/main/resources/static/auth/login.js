(async function () {
    const params = new URLSearchParams(window.location.search);
    const form = document.querySelector(".login-form");
    const submitButton = document.getElementById("loginButton");
    const errorMessage = document.getElementById("errorMessage");
    const lockedMessage = document.getElementById("lockedMessage");
    const expiredMessage = document.getElementById("expiredMessage");
    const logoutMessage = document.getElementById("logoutMessage");
    const readyMessage = document.getElementById("readyMessage");

    if (params.has("error")) {
        errorMessage.hidden = false;
    }

    if (params.has("locked")) {
        lockedMessage.hidden = false;
    }

    if (params.has("expired")) {
        expiredMessage.hidden = false;
    }

    if (params.has("logout")) {
        logoutMessage.hidden = false;
    }

    try {
        const response = await fetch("/auth/csrf", {
            credentials: "same-origin",
            headers: {
                "Accept": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error("Falha ao inicializar o token de segurança.");
        }

        const data = await response.json();
        const hiddenInput = document.createElement("input");
        hiddenInput.type = "hidden";
        hiddenInput.name = data.parameterName;
        hiddenInput.value = data.token;
        form.appendChild(hiddenInput);

        submitButton.disabled = false;
        submitButton.textContent = "Entrar";
        readyMessage.hidden = false;
    } catch (error) {
        errorMessage.textContent = "Nao foi possivel preparar o login com seguranca. Recarregue a pagina e tente novamente.";
        errorMessage.hidden = false;
        submitButton.textContent = "Falha na inicializacao";
    }
}());
