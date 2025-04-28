package br.edu.puccampinas.superid.functions

import android.content.Context
import android.provider.Settings
import android.util.Log
import br.edu.puccampinas.superid.functions.validationUtils.saveEmailLocally
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Verifica se os campos digitados durante o signIn são nulos
 */
fun areSignInFieldsNull(email: String, password: String): Boolean {
    return email.isEmpty() || password.isEmpty()
}

/**
 * Valida se os campos de SignIn foram preenchidos corretamente
 */
fun validateSignInFields(email: String, password: String): Boolean {
    return !(areSignInFieldsNull(email, password)
            || validationUtils.emailIsInvalid(email)
            || validationUtils.passwordIsInvalid(password))
}

/**
 * Realiza o SignIn no Firebase Auth
 */
fun performSignIn(
    context: Context,
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    if (!validateSignInFields(email, password)) {
        onFailure(Exception("Preencha todos os campos corretamente."))
        return
    }

    val auth = Firebase.auth

    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    Log.i("IMEI", "ANDROID ID: $androidId")

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveEmailLocally(context, email)
                Log.i("LOGIN", "Login realizado com sucesso.")
                onSuccess()
            } else {
                Log.e("LOGIN", "Erro ao fazer login", task.exception)
                onFailure(task.exception ?: Exception("Erro desconhecido ao fazer login."))
            }
        }
        .addOnFailureListener { e ->
            Log.e("LOGIN", "Falha na autenticação", e)
            onFailure(e)
        }
}
