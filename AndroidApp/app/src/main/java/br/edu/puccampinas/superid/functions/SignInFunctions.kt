package br.edu.puccampinas.superid.functions

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


fun areSignInFieldsNull(email: String, password: String): Boolean {
    if (email.isEmpty() || password.isEmpty()) {
        Log.i("FIREBASE", "CAMPOS ESTÃO NULOS")
        return true
    }
    return false
}

fun validateSignInFields(email: String, password: String): Boolean {
    if (areSignInFieldsNull(email, password) || validationUtils.emailIsInvalid(email) || validationUtils.passwordIsInvalid(password)) {
        return false
    }
    return true
}

fun performSignIn(
    context: Context,
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {

    if (!validateSignInFields(email, password)) {
        onFailure(Exception("Campos inválidos"))
        return
    }

    val auth = Firebase.auth

    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    Log.i("IMEI", androidId)

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("LOGIN", "Login realizado com sucesso.")
                onSuccess()
            } else {
                Log.e("LOGIN", "Erro ao fazer login", task.exception)
                onFailure(task.exception ?: Exception("Erro desconhecido no login"))
            }
        }
        .addOnFailureListener { e ->
            Log.e("LOGIN", "Falha na autenticação", e)
            onFailure(e)
        }
}

