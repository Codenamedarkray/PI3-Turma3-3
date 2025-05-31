package br.edu.puccampinas.superid.functions

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import br.edu.puccampinas.superid.WelcomeActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object validationUtils {
    /**
     * Verifica por meio de regex se o formato do email inserido é valido
     */
    fun emailIsInvalid(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        if (!emailRegex.matches(email)) {
            Log.i("FIREBASE", "EMAIL INVALIDO")
            return true
        }
        return false
    }

    /**
     * Verifica se a senha de acesso corresponde as regras pre-definidas
     */
    fun passwordIsInvalid(password: String): Boolean {
        if (password.length < 6) {
            Log.i("FIREBASE", "SENHA INVALIDA")
            return true
        }
        return false
    }

    /**
     * Salva o email do usuário para ele se reautenticar ao entrar novamente no
     * app
     */
    fun saveEmailLocally(context: Context, email: String) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit() { putString("user_email", email) }
    }

    /**
     * Retorna o email salvo para fazer a reautenticação
     */
    fun getSavedEmail(context: Context): String? {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_email", null)
    }

    /**
     * reautentica o usuário com o email salvo localmente e
     * a senha fornecida
     */
    fun reauthenticateUser(
        context: Context,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = Firebase.auth.currentUser
        val email = getSavedEmail(context)

        if (user != null && email != null) {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    Log.i("LOGIN", "Reautenticado com sucesso")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("LOGIN", "Erro ao reautenticar", e)
                    onFailure(e)
                }
        } else {
            onFailure(Exception("Usuário não autenticado ou e-mail não salvo"))
        }
    }

    /**
     * Faz o logout da autenticação do firebase e
     * retira o email salvo localmente
     */
    fun performLogout(context: Context) {
        // Desloga do Firebase
        Firebase.auth.signOut()

        // Limpa e-mail salvo localmente
        val email = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        email.edit() { remove("user_email") }

        //limpar que viu os termos de serviço
        context.getSharedPreferences("superid_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("has_seen_welcome", false)
            .apply()

        val intent = Intent(context, WelcomeActivity::class.java)
        context.startActivity(intent)
    }

    /**
     * Checa se o usuário está autenticado
     */
    fun checkUserAuthentication(context: Context): Boolean {
        val user = Firebase.auth.currentUser
        val email = getSavedEmail(context)

        return user != null && email != null

    }

    /**
     * Checa se o email do usuário foi validado via
     * email de validação
     */
    fun checkUserEmailVerification(
        onResult: (Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    )
    {
        val user = Firebase.auth.currentUser
        var userVerified = false

        if (user == null) {
            onResult(false)
            return
        }

        user.reload()
            .addOnSuccessListener {
                userVerified = user.isEmailVerified
                Log.i("VERIFICATION", "Dados atualizados, verificação $userVerified")
                onResult(userVerified)
            }
            .addOnFailureListener {
                Log.e("VERIFICATION" , "Falha ao atualizar as informações")
                onFailure(it)
            }

    }

    /**
     * Função que pega o nome de usuário do firebase e o devolve
     */
    fun getUsername(
        onSuccess: (String?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = Firebase.firestore
        val uid = Firebase.auth.currentUser?.uid ?: return onFailure(Exception("Usuário não autenticado"))

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                val username = document.getString("NAME")
                onSuccess(username)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

}