const PROJECT_ID = "super-id-f18ec";
const API_KEY = "f6UIj/l23X+M4xW7yZ9aQ0bVpEs6iYdZgB8nJt2HuKl9rSwzXcAe5oPq1I7b8UjYn2OmLwzXcAe5oPq1I7b8e5oPq1I7b8UjYn2OmLwzXcAe5oPq1I7b8UjYn2OmLw==";
const SITE_URL = "www.example.com";

let loginToken = null;
let pollInterval = null;
let pollCount = 0;
const MAX_POLLS = 4;
const POLL_INTERVAL_MS = 15000;

function openModal() {
  document.getElementById("modal").style.display = "flex";
  generateQRCode();
}

function closeModal() {
  document.getElementById("modal").style.display = "none";
  clearInterval(pollInterval);
  document.getElementById("status").innerText = "Aguardando escaneamento...";
  document.getElementById("qrcode").src = "";
}

async function generateQRCode() {
  try {
    const res = await fetch(`http://localhost:5001/${PROJECT_ID}/us-central1/performAuth`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        apiKey: API_KEY,
        url: SITE_URL
      })
    });

    const data = await res.json();
    loginToken = data.loginToken;

    document.getElementById("qrcode").src = data.qrBase64;
    document.getElementById("status").innerText = "Escaneie com o app SuperID";

    pollCount = 0;
    if (pollInterval) clearInterval(pollInterval);
    pollInterval = setInterval(checkLoginStatus, POLL_INTERVAL_MS);

  } catch (err) {
    document.getElementById("status").innerText = "Erro ao gerar QR Code";
    console.error(err);
  }
}

async function checkLoginStatus() {
  try {
    const res = await fetch(`http://localhost:5001/${PROJECT_ID}/us-central1/getLoginStatus`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ loginToken })
    });

    const data = await res.json();

    if (data.status === "success") {
      clearInterval(pollInterval);
      document.getElementById("status").innerText = "Login efetuado com sucesso!";
    } else if (data.status === "expired") {
      clearInterval(pollInterval);
      document.getElementById("status").innerText = "Token expirado. Gerando novo QR Code...";
      setTimeout(generateQRCode, 1000);
    } else {
      pollCount++;
      if (pollCount >= MAX_POLLS) {
        clearInterval(pollInterval);
        document.getElementById("status").innerText = "Tempo esgotado. Gerando novo QR Code...";
        setTimeout(generateQRCode, 1000);
      }
    }

  } catch (err) {
    clearInterval(pollInterval);
    document.getElementById("status").innerText = "Erro ao verificar status";
    console.error(err);
  }
}


