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
import br.edu.puccampinas.superid.functions.validationUtils
import br.edu.puccampinas.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.String

class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SignInForm(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

fun areLoginFieldsNull(email: String, password: String): Boolean {
    if (email.isEmpty() || password.isEmpty()) {
        Log.i("FIREBASE", "CAMPOS ESTÃO NULOS")
        return true
    }
    return false
}

fun validateLoginFields(email: String, password: String): Boolean {
    if (areLoginFieldsNull(email, password) || validationUtils.emailIsInvalid(email) || validationUtils.passwordIsInvalid(password)) {
        return false
    }
    return true
}

fun performLogin(
    context: Context,
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {

    if (!validateLoginFields(email, password)) {
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

@Composable
fun SignInForm(modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                performLogin(
                    context,
                    email,
                    password,
                    onSuccess = {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                    },
                    onFailure = { exception ->
                        Log.e("LOGIN", "ERRO AO ACESSAR A CONTA: ${exception.message}")
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignIpFormPreview() {
    SuperIDTheme {
        SignInForm()
    }
}