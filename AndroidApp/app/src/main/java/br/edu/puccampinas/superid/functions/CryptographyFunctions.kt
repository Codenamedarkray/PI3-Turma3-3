package br.edu.puccampinas.superid.functions
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

//chave utilizada na criptografia
val key = "12345678910"

/**
 * Função que criptografa as senhas do usuário usando AES.
 * AES usa criptografia simétrica (mesma chave para encriptar e descriptografar)
 */
fun encrypt(text: String): String? {
    val fixedKey = fixKey(key)
    // Cria o objeto de cifra usando AES com modo ECB e padding PKCS5
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    // Cria a chave secreta baseada na chave corrigida
    val secretKey = SecretKeySpec(fixedKey.toByteArray(Charsets.UTF_8), "AES")
    // Inicializa a cifra em modo ENCRYPTION (encriptação)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)

    // Aplica a cifra ao texto e converte o resultado para Base64 (texto legível)
    val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
}

fun decrypt(encryptedBase64: String): String? {
    val fixedKey = fixKey(key)
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    val secretKey = SecretKeySpec(fixedKey.toByteArray(Charsets.UTF_8), "AES")
    // Inicializa a cifra em modo DECRYPTION (descriptografar)
    cipher.init(Cipher.DECRYPT_MODE, secretKey)

    // Decodifica a string Base64 para bytes e aplica a descriptografia
    val decodedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
    val decryptedBytes = cipher.doFinal(decodedBytes)
    // Retorna o texto original descriptografado
    return String(decryptedBytes, Charsets.UTF_8)
}

/**
 * Ajusta a chave para ter exatamente 16 caracteres (128 bits)
 * AES exige chave de 16 (AES-128), 24 (AES-192) ou 32 (AES-256) bytes.
 *
 */
fun fixKey(key: String): String {
    return key.padEnd(16, '0').take(16)
}