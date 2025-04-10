package br.edu.puccampinas.superid.functions

import android.content.Context
import br.edu.puccampinas.superid.functions.validationUtils.checkUserEmailVerification
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

fun recoverPassword(
    email: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    checkUserEmailVerification(
        onResult = { isVerified ->
            if (!isVerified) {
                onFailure(Exception("Email não foi verificado"))
                return@checkUserEmailVerification
            }

            // agora sim, enviar e-mail de redefinição
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }

        },
        onFailure = { e ->
            onFailure(e)
        }
    )
}