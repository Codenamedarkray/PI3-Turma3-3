package br.edu.puccampinas.superid.functions

import android.annotation.SuppressLint
import android.util.Base64
import androidx.compose.runtime.MutableState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom

object PasswordStorageUtils {
    @SuppressLint("StaticFieldLeak")
    val db = Firebase.firestore

    fun fetchPasswordData(
        uid: String,
        onCategoriesFetched: (List<DocumentSnapshot>) -> Unit,
        onPasswordsFetched: (Map<String, List<Pair<String, Map<String, Any?>>>>) -> Unit,
        onExpandedMapUpdated: (MutableMap<String, Boolean>) -> Unit
    ) {
        val tempExpandedMap = mutableMapOf<String, Boolean>()
        val tempPasswordsMap = mutableMapOf<String, List<Pair<String, Map<String, Any?>>>>()

        // Fetch nas categorias do usuário
        db.collection("users").document(uid).collection("category")
            .get()
            .addOnSuccessListener { snapshot ->
                // Obtém os documentos do fetch e salva na lista de categorias
                val cats = snapshot.documents

                // Para cada documento de categoria, faz o mapa para as senhas
                cats.forEach { category ->
                    // Coloca por padrão que a categoria está fechada na visualização
                    tempExpandedMap.putIfAbsent(category.id, false)

                    // Pega os dados da categoria ou coloca como vazio
                    val fields = category.data ?: emptyMap<String, Any>()

                    // Pega só os campos que não sejam "deletable"
                    val passwordEntries = fields
                        .filterKeys { it != "deletable" }
                        .map { (platformName, platformData) ->
                            platformName to (platformData as Map<String, Any?>)
                        }

                    // Atualiza o mapa de senhas temporário
                    tempPasswordsMap[category.id] = passwordEntries
                }

                onCategoriesFetched(cats) // Atualiza lista de categorias
                // Atualiza os estados principais depois de processar tudo
                onPasswordsFetched(tempPasswordsMap)
                onExpandedMapUpdated(tempExpandedMap)
            }
    }

    fun createNewCategory(
        uid: String,
        categoryName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val categoryData = mapOf(
            "deletable" to true
        )

        db.collection("users").document(uid)
            .collection("category")
            .document(categoryName)
            .set(categoryData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun createNewPassword(
        uid: String,
        categoryName: String,
        title: String,
        email: String,
        password: String,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val passwordData = mapOf(
            "email" to email,
            "password" to password,
            "description" to description,
            "accessToken" to generateRandomBase64Token()
        )

        db.collection("users").document(uid)
            .collection("category").document(categoryName)
            .update(mapOf(title to passwordData))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun generateRandomBase64Token(): String {
        val bytes = ByteArray(192)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun updatePassword(
        uid: String,
        category: String,
        title: String,
        updatedData: Map<String, Any?>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(uid)
            .collection("category").document(category)
            .update(mapOf(title to updatedData))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun deletePassword(
        uid: String,
        category: String,
        title: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(uid)
            .collection("category").document(category)
            .update(mapOf(title to FieldValue.delete()))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun deleteCategory(
        uid: String,
        categoryId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(uid)
            .collection("category")
            .document(categoryId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

}