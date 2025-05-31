# Projeto Super-ID - Grupo 3 - Turma 3

## 🛡️ Sobre o Projeto

**Super-ID** é um aplicativo de gerenciamento seguro de senhas que permite armazenar credenciais de forma criptografada, organizar por categorias e realizar login em sites parceiros através de **QR Code** sem a necessidade de digitar senhas. O projeto foi desenvolvido como parte da disciplina de Desenvolvimento Mobile.

---

## 📦 Instalação

O aplicativo está disponível para download diretamente na seção **[Releases](https://github.com/Codenamedarkray/PI3-Turma3-3/releases)** do GitHub.

1. Acesse a aba **Releases** deste repositório.
2. Baixe o arquivo **APK** mais recente.
3. Instale o APK em seu dispositivo Android.
   - ⚠️ É necessário habilitar a instalação de apps de fontes desconhecidas.

---

## 🚀 Como Usar

1. Ao abrir o aplicativo pela primeira vez, **concorde com os termos e condições**.
2. **Crie sua conta** preenchendo os campos solicitados.
3. Após a criação da conta, **faça login** com os dados informados.
4. _(Opcional, mas altamente recomendado)_ **Verifique seu e-mail** na tela de usuário para ativar a recuperação de senha e o login sem senha.
5. Na **tela principal**, crie uma nova **categoria**.
6. **Adicione uma senha** à categoria, preenchendo os campos necessários com os dados de login do site desejado.
7. Para testar a funcionalidade de **leitura de QR Code**:
   - Toque na imagem do QR Code no app.
   - Aponte a câmera para o QR Code gerado pelo site de teste (veja abaixo).
8. Para encerrar a sessão, **faça logout** pela tela do usuário.


---

## 🧪 Como Testar o Login Sem Senha

Você pode testar o recurso de **Login sem Senha** utilizando o site de testes oficial:

➡️ Acesse: **[https://site-test-superid.netlify.app/](https://site-test-superid.netlify.app/)**

1. Clique na opção **"Login com Super ID"**.
![Tela principal do Site](./Documentation/preview%20images/Site-Test-MainScreen.png)
2. O site irá gerar um **QR Code**.
3. Abra o aplicativo e escaneie o QR Code usando a funcionalidade de leitura do Super-ID.
4. Após escanear, o login será efetuado automaticamente no site.

---

## 🛠️ Tecnologias Usadas

- **Kotlin** — Linguagem principal para o desenvolvimento Android.
- **Jetpack Compose** — Para construção de interfaces reativas.
- **Firebase**:
  - **Authentication** — Autenticação de usuários.
  - **Firestore** — Armazenamento em nuvem.
  - **Cloud Functions** — Backend serverless.
- **ZXing Scanner** — Para leitura de QR Codes.
- **HTML, CSS, JavaScript** — Para o desenvolvimento do site de testes.

---

## 📌 Observações

- O recurso de **verificação de e-mail** é necessário para ativar a recuperação de senha e login sem senha.
- Certifique-se de estar conectado à internet durante o uso do aplicativo.
- O **login sem senha** só funciona em sites parceiros autorizados.

---

## 🔗 Links Importantes
- [Descritivo do projeto](./Documentation/PI3-SuperID.pdf)
- [Protótipos](https://www.figma.com/design/Lu0ipAQPPJfgUJabIu9eY8/SuperID?node-id=0-1&t=0tOSP8JzGM6jgDOX-1)

---

> Desenvolvido por **Grupo 3 — Turma 3**.
