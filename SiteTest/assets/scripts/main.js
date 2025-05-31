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

const API_KEY = "f6UIj/l23X+M4xW7yZ9aQ0bVpEs6iYdZgB8nJt2HuKl9rSwzXcAe5oPq1I7b8UjYn2OmLwzXcAe5oPq1I7b8e5oPq1I7b8UjYn2OmLwzXcAe5oPq1I7b8UjYn2OmLw==";
const SITE_URL = "www.example.com";

let loginToken = null;
let pollInterval = null;
const POLL_INTERVAL_MS = 20000; // 20 segundos

async function generateQRCode() {
    try {
    const res = await fetch(`https://southamerica-east1-super-id-f18ec.cloudfunctions.net/api/performAuth`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
        apiKey: API_KEY,
        url: SITE_URL
        })
    });

    const data = await res.json();
    loginToken = await data.loginToken;
    console.log("Login Token:", loginToken);

    document.getElementById("qrcode").src = data.qrBase64;
    document.getElementById("status").innerText = "Escaneie com o app SuperID";

    if (pollInterval) clearInterval(pollInterval);
    pollInterval = setInterval(checkLoginStatus, POLL_INTERVAL_MS);

    } catch (err) {
    document.getElementById("status").innerText = "Erro ao gerar QR Code, recarregue a página.";
    console.error(err);
    }
}

async function checkLoginStatus() {

try {
    const res = await fetch(`https://southamerica-east1-super-id-f18ec.cloudfunctions.net/api/getLoginStatus`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ loginToken })
    });

    const data = await res.json();

    if (data.status === "success") {
    clearInterval(pollInterval);
    document.getElementById("status").innerText = "Login efetuado com sucesso!";
    // Redireciona após 1 segundo
    setTimeout(() => {
    window.location.href = "logged.html"; 
    }, 1000);

    } else if (data.status === "expired") {
    clearInterval(pollInterval);
    document.getElementById("status").innerText = "Token expirado. Gerando novo QR Code...";
    setTimeout(generateQRCode, 1000);
    } else {
    

    }

} catch (err) {
    clearInterval(pollInterval);
    document.getElementById("status").innerText = "Erro ao verificar status";
    console.error(err);
}
}


