package br.edu.puccampinas.superid.functions
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

val key = "12345678910"

fun encrypt(text: String): String? {
    val fixedKey = fixKey(key)
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    val secretKey = SecretKeySpec(fixedKey.toByteArray(Charsets.UTF_8), "AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)

    val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
}

fun decrypt(encryptedBase64: String): String? {
    val fixedKey = fixKey(key)
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    val secretKey = SecretKeySpec(fixedKey.toByteArray(Charsets.UTF_8), "AES")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)

    val decodedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
    val decryptedBytes = cipher.doFinal(decodedBytes)
    return String(decryptedBytes, Charsets.UTF_8)
}

fun fixKey(key: String): String {
    return key.padEnd(16, '0').take(16)
}