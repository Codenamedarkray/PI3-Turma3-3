/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import {onRequest} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";

import * as crypto from "crypto";
import * as QRCode from "qrcode";
import { initializeApp } from "firebase-admin/app";
import { getFirestore, Timestamp } from "firebase-admin/firestore";

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// export const helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

initializeApp();
const db = getFirestore();

/**
 * Firebase Function performAuth
 * Um site parceiro chama essa função com sua apiKey e url
 * Recebe um QR Code base64 com um token gerado para login
 */
export const performAuth = onRequest(async (req, res) => {
  const { apiKey, url } = req.body;

  if (!apiKey || !url) {
    res.status(400).send("Missing apiKey or url");
    return;
  }

  try {
    const partnersRef = db.collection("partners");
    const snapshot = await partnersRef
      .where("url", "==", url)
      .where("apiKey", "==", apiKey)
      .get();

    if (snapshot.empty) {
      logger.warn("Parceiro não autorizado:", { apiKey, url });
      res.status(403).send("Unauthorized partner");
      return;
    }

    const loginToken = generateRandomBase64(256);
    const createdAt = Timestamp.now();

    await db.collection("login").doc(loginToken).set({
      apiKey,
      loginToken,
      createdAt,
      attempts: 0,
    });

    const qrCodeBase64 = await generateQRCodeBase64(loginToken);

    res.status(200).send({ qrBase64: qrCodeBase64, loginToken: loginToken });
  } catch (error) {
    logger.error("Erro em performAuth", error);
    res.status(500).send("Internal server error");
  }
});

/**
 * Firebase Function getLoginStatus
 * O site parceiro consulta se o QR code foi usado por algum usuário (verifica o status do loginToken)
 */
export const getLoginStatus = onRequest(async (req, res) => {
  const { loginToken } = req.body;

  if (!loginToken) {
    res.status(400).send("Missing loginToken");
    return;
  }

  try {
    const loginDocRef = db.collection("login").doc(loginToken);
    const loginSnap = await loginDocRef.get();

    if (!loginSnap.exists) {
      res.status(404).send("Token not found");
      return;
    }

    const loginData = loginSnap.data();
    const now = Timestamp.now();
    const created = loginData?.createdAt as Timestamp;
    const diff = now.seconds - created.seconds;

    if (diff > 60 || (loginData?.attempts ?? 0) >= 3) {
      await loginDocRef.delete();
      res.status(410).send({ status: "expired" });
      return;
    }

    // Incrementar tentativas
    await loginDocRef.update({
      attempts: (loginData?.attempts ?? 0) + 1,
    });

    if (loginData?.user) {
      res.status(200).send({ status: "success", uid: loginData.user });
    } else {
      res.status(202).send({ status: "pending" });
    }
  } catch (error) {
    logger.error("Erro em getLoginStatus", error);
    res.status(500).send("Internal server error");
  }
});

/**
 * Gera uma string base64 aleatória com o tamanho desejado
 */
function generateRandomBase64(length: number): string {
  return crypto.randomBytes(length).toString("base64url").slice(0, length);
}

async function generateQRCodeBase64(text: string): Promise<string> {
  return await QRCode.toDataURL(text);
}