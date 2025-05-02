package br.edu.puccampinas.superid.functions

import android.content.Context
import android.provider.Settings
import android.util.Log
import br.edu.puccampinas.superid.functions.validationUtils.saveEmailLocally
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Função que cria as categorias padrão no documento
 * do usuário no firebase
 */
fun addDefaultCategories(userId: String, onComplete: () -> Unit, onError: (Exception) -> Unit) {
    val db = Firebase.firestore
    val userDocRef = db.collection("users").document(userId) //pega o documento de usuário criado

    val categories = listOf(
        "Sites Web" to false,
        "Aplicativos" to true,
        "Teclados Físicos" to true
    )

    val batch = db.batch()  //executa várias instruções do firestore de uma vez

    categories.forEach { (name, deletable) ->
        val catRef = userDocRef.collection("category").document(name) //cria a referencia para cada documento
        val catData = mapOf("deletable" to deletable)  //cria os dados para cada documento
        batch.set(catRef, catData)
    }

    batch.commit() //executa todas as operações registradas no foreach
        .addOnSuccessListener { onComplete() }
        .addOnFailureListener { e -> onError(e) }
}

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

                            addDefaultCategories(
                                userId = userId,
                                onComplete = {
                                    saveEmailLocally(context, email)
                                    sendVerificationEmail()
                                    Log.d("FIREBASE", "Usuário e categorias criados com sucesso")
                                    onSuccess()
                                },
                                onError = { categoryError ->
                                    Log.e("FIREBASE", "Erro ao criar categorias", categoryError)
                                    onFailure(categoryError)
                                }
                            )

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