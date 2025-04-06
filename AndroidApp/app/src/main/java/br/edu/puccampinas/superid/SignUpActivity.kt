package br.edu.puccampinas.superid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.edu.puccampinas.superid.ui.theme.SuperIDTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.String
import kotlin.collections.HashMap
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o Firebase
        FirebaseApp.initializeApp(this)

        // Teste para ver se o Firebase foi inicializado
        if (FirebaseApp.getApps(this).isNotEmpty()) {
            Log.d("FirebaseCheck", "Firebase foi inicializado com sucesso!")
        } else {
            Log.e("FirebaseCheck", "Firebase NÃO foi inicializado!")
        }

        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                var isFirebaseReady by remember { mutableStateOf(false) }

                // Checa se o Firebase está pronto
                LaunchedEffect(Unit) {
                    if (FirebaseApp.getApps(this@SignUpActivity).isNotEmpty()) {
                        isFirebaseReady = true
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (isFirebaseReady) {
                        SignUpForm(Modifier.padding(innerPadding))
                    } else {
                        Text("Inicializando...")
                    }
                }
            }
        }
    }
}



fun createUser(name: String, email: String, password: String, androidId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = Firebase.firestore
    val auth = Firebase.auth

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

fun sendVerificationEmail() {
    val auth = Firebase.auth
    val user: FirebaseUser? = auth.currentUser

    user?.sendEmailVerification()
        ?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("AUTH", "EMAIL ENVIADO COM SUCESSO")
            } else {
                Log.e("AUTH", "Erro ao enviar e-mail de validação: ${task.exception?.message}")
            }
        }
}

fun fieldsAreNull(name: String, email: String, password: String): Boolean {
    if (email == "" || password == "" || name == "") {
        Log.i("FIREBASE", "CAMPOS ESTÃO NULOS")
        return true
    }
    return false
}

fun emailIsInvalid(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    if (!emailRegex.matches(email)) {
        Log.i("FIREBASE", "EMAIL INVALIDO")
        return true
    }
    return false
}

fun passwordIsInvalid(password: String): Boolean {
    if (password.length < 6) {
        Log.i("FIREBASE", "SENHA INVALIDA")
        return true
    }
    return false
}

fun validateSignUpFields(name: String, email: String, password: String): Boolean {
    if (fieldsAreNull(name, email, password) || emailIsInvalid(email) || passwordIsInvalid(password)) {
        return false
    }
    return true
}

fun performSignUp(context: Context, name: String, email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    Log.i("IMEI", "$androidId")

    if (!validateSignUpFields(name, email, password)) {
        onFailure(Exception("Campos inválidos"))
        return
    }

    createUser(name, email, password, androidId, onSuccess, onFailure)
}

@Composable
fun SignUpForm(modifier: Modifier = Modifier) {
    var name: String by remember { mutableStateOf("") }
    var email: String by remember { mutableStateOf("") }
    var password: String by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = {
                val intent = Intent(context, WelcomeActivity::class.java)
                context.startActivity(intent)
            }
        ) {
            Text("←")
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                performSignUp(
                    context,
                    name,
                    email,
                    password,
                    onSuccess = {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                    },
                    onFailure = { exception ->
                        Log.e("SIGNUP", "ERRO AO CRIAR CONTA: ${exception.message}")
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar Minha Conta")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpFormPreview() {
    SuperIDTheme {
        SignUpForm()
    }
}