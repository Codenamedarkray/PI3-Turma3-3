package br.edu.puccampinas.superid.functions

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Função que cria um usúario no Firebase Authentication,
 * e caso bem sucedido, salva as informações do usuário e dispositivo
 * no Firestore, no campo user
 */
fun createUser(context: Context, name: String, email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = Firebase.firestore
    val auth = Firebase.auth

    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    Log.i("IMEI", "$androidId")

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("AUTH", "Criação de conta bem-sucedida.")
                val userId = auth.currentUser?.uid

                if (userId != null) {
                    Log.i("AUTH", "DADOS SALVOS")

                    val user: HashMap<String, String> = hashMapOf(
                        "IMEI" to androidId,
                        "UID" to userId,
                        "NAME" to name
                    )

                    db.collection("users").document(userId).set(user)
                        .addOnCompleteListener {
                            Log.d("FIREBASE", "Sucesso ao salvar os dados")
                            sendVerificationEmail()
                            onSuccess() // Chama o callback de sucesso
                        }.addOnFailureListener { e ->
                            Log.e("FIREBASE", "Erro ao salvar dados", e)
                            onFailure(e)
                        }
                } else {
                    Log.e("AUTH", "ERRO AO RECUPERAR O UID")
                    onFailure(Exception("Erro ao recuperar o UID"))
                }
            } else {
                Log.e("AUTH", "OCORREU UM ERRO")
                onFailure(task.exception ?: Exception("Erro desconhecido"))
            }
        }.addOnFailureListener { e ->
            Log.e("AUTH", "ERRO AO SALVAR", e)
            onFailure(e)
        }
}

/**
 * Envia email de verificação via firebase Authentication para o novo usuário
 */
fun sendVerificationEmail() {
    val user = Firebase.auth.currentUser

    user?.sendEmailVerification()
        ?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("AUTH", "EMAIL ENVIADO COM SUCESSO")
            } else {
                Log.e("AUTH", "Erro ao enviar e-mail de validação: ${task.exception?.message}")
            }
        }
}

/**
 * Verifica se os campos preenchidos para o cadastro estão vazios
 */
fun areSignUpFieldsNull(name: String, email: String, password: String): Boolean {
    if (email == "" || password == "" || name == "") {
        Log.i("FIREBASE", "CAMPOS ESTÃO NULOS")
        return true
    }
    return false
}

/**
 * Chama as funções de validação dos campos do cadastro,
 * para descobrir se todos são validos
 */
fun validateSignUpFields(name: String, email: String, password: String): Boolean {
    if (areSignUpFieldsNull(name, email, password) || validationUtils.emailIsInvalid(email) || validationUtils.passwordIsInvalid(password)) {
        return false
    }
    return true
}

/**
 * Chama a validação dos campos e criação de usuário
 */
fun performSignUp(context: Context, name: String, email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

    if (!validateSignUpFields(name, email, password)) {
        onFailure(Exception("Campos inválidos"))
        return
    }

    createUser(context, name, email, password, onSuccess, onFailure)
}