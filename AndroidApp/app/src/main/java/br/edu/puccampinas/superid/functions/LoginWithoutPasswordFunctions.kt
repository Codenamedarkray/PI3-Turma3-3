package br.edu.puccampinas.superid.functions

import android.util.Log
import androidx.compose.runtime.Composable
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
/*
*A funcao validQRcode le um qr code que sera verificado e dará uma resposta ao usuario
*A funcao confirmlogin faz com que o firebase autenticator confirme o acesso ao usuario tendo uma certa prevencao de erro
*/
fun validQRCode(
    loginToken: String,
    onSuccess: (DocumentSnapshot)-> Unit,
    onFailure: ()-> Unit
){
    val db = Firebase.firestore

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

fun confirmLogin(loginToken: String) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid ?: return

    val loginRef = db.collection("login").document(loginToken)

    loginRef.update(
        mapOf(
            "user" to uid,
            "loginTime" to Timestamp.now()
        )
    ).addOnSuccessListener {
        Log.d("Auth", "Login confirmado com sucesso!")
    }.addOnFailureListener {
        Log.e("Auth", "Erro ao confirmar login", it)
    }
}
