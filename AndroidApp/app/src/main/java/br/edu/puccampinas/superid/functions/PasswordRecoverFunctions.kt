package br.edu.puccampinas.superid.functions

import android.content.Context
import br.edu.puccampinas.superid.functions.validationUtils.checkUserEmailVerification
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Função de envio de email de recuperação de senha
 * Funciona apenas se o usuário estiver com o email validado
 */
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

            // envio do email de redefinição
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }

        },
        onFailure = { e ->
            onFailure(e)
        }
    )
}