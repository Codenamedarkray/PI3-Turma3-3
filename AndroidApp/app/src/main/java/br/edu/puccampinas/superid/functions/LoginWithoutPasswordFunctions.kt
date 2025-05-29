package br.edu.puccampinas.superid.functions

import android.util.Log
import androidx.compose.runtime.Composable
import br.edu.puccampinas.superid.functions.PasswordStorageUtils.generateRandomBase64Token
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/*
*A funcao validQRcode le um qr code que sera verificado e dará uma resposta ao usuario
*A funcao confirmlogin faz com que o firebase autenticator confirme o acesso ao usuario tendo uma certa prevencao de erro
*/


val db = Firebase.firestore
/**
 * Verifica se o qrcode escaneado ainda é valido
 * para fazer o login sem senha
 */

fun validQRCode(
    loginToken: String,
    onSuccess: (DocumentSnapshot)-> Unit,
    onFailure: ()-> Unit
){

    if (loginToken.length != 256) {
        onFailure()
        return
    }

    db.collection("login").document(loginToken).get()
        .addOnSuccessListener { doc ->
            if (doc.exists()) {
                if(doc.getString("user") == null){
                    onSuccess(doc)
                }else{
                    onFailure()
                }

            } else {
                onFailure()
            }
        }
        .addOnFailureListener {
            onFailure()
        }

}

/**
 * Confirma o login colocando o UID do usuário
 * dentro do arquivo de login referente
 */
fun confirmLogin(loginToken: String, onComplete: () -> Unit) {
    val uid = Firebase.auth.currentUser?.uid ?: return

    val loginRef = db.collection("login").document(loginToken)

    loginRef.update(
        mapOf(
            "user" to uid,
            "loginTime" to Timestamp.now()
        )
    ).addOnSuccessListener {
        Log.d("Auth", "Login confirmado com sucesso!")
        loginRef.get()
            .addOnSuccessListener { doc ->
                addPasswordlessLogin(
                    doc.getString("apiKey").toString(),
                    onSuccess = {
                        onComplete()
                    },
                    onFailure = {
                        onComplete()
                    })

            }

    }.addOnFailureListener {
        Log.e("Auth", "Erro ao confirmar login", it)
        onComplete()
    }
}

/**
 * Tira o www. e .com da url para se tornar segura
 * de salvar no firebase
 */
fun cleanUrl(url: String): String {
    val noPrefix = url.removePrefix("www.")
    return noPrefix.replace(Regex("\\.(com|net|org)$"), "")
}

/**
 * Pega a url do web site
 */
fun getWebSiteDomain(
    apiKey: String,
    onSuccess: (String) -> Unit,
    onFailure: () -> Unit
){
    db.collection("partners").whereEqualTo("apiKey", apiKey).get()
        .addOnSuccessListener { docs ->
            for(doc in docs){
                val cleanedURL = cleanUrl(doc.getString("url").toString())
                onSuccess(cleanedURL)
            }
    }.addOnFailureListener {
        onFailure()
    }

}

/**
 * Caso o user já tenha usado o site antes, atualiza o access token e
 * caso não tenha, salva na categoria Sites Web
 */
fun addPasswordlessLogin(apiKey: String, onSuccess: () -> Unit, onFailure: () -> Unit){
    val uid = Firebase.auth.currentUser?.uid ?: return

    Log.i("PASS", apiKey)

    getWebSiteDomain(
        apiKey = apiKey,
        onSuccess = { url ->
            Log.i("PASS", "$apiKey 2")
            val website = url.toString()
            val newWebLogins = mapOf(
                website to mapOf(
                    "email" to "",
                    "password" to "",
                    "description" to "Login sem senha utilizado no site $url",
                    "accessToken" to generateRandomBase64Token()
                )
            )

            db.collection("users").document(uid).collection("category").document("Sites Web")
                .set(newWebLogins, SetOptions.merge())
                .addOnSuccessListener {
                    Log.i("PASS", "Login salvo com sucesso.")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("PASS", "Erro ao salvar login", e)
                    onFailure()
                }

        },
        onFailure = {

        }
    )

}

