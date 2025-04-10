package br.edu.puccampinas.superid.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.edu.puccampinas.superid.MainActivity
import br.edu.puccampinas.superid.WelcomeActivity
import br.edu.puccampinas.superid.functions.performSignIn
import br.edu.puccampinas.superid.functions.recoverPassword
import br.edu.puccampinas.superid.functions.validationUtils.getSavedEmail
import br.edu.puccampinas.superid.functions.validationUtils.passwordIsInvalid
import br.edu.puccampinas.superid.functions.validationUtils.reauthenticateUser
import br.edu.puccampinas.superid.ui.theme.SuperIDTheme

@Composable
fun ReAuthenticationForm(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var messageColor by remember { mutableStateOf(Color.Unspecified) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("INSIRA A SENHA MESTRE", fontSize = 20.sp)

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
                if (!passwordIsInvalid(password)) {
                    reauthenticateUser(
                        context,
                        password,
                        onSuccess = {
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        },
                        onFailure = { exception ->
                            Log.e("LOGIN", "ERRO AO ACESSAR A CONTA: ${exception.message}")
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ){
            Text(
                "Esqueceu a senha?",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                "Clique Aqui!",
                color = Color.Blue,
                fontSize = 16.sp,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier.clickable {
                    recoverPassword(
                        email = getSavedEmail(context)!!,
                        onSuccess = {
                            message = "Link de recuperação enviado para o e-mail"
                            messageColor = Color.Blue
                        },
                        onFailure = {
                            message = it.message
                            messageColor = Color.Red
                        }
                    )
                }
            )
        }

        message?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = messageColor)
        }

    }
}

@Preview(showBackground = true)
@Composable
fun ReAuthenticationFormPreview() {
    SuperIDTheme {
        ReAuthenticationForm(
            modifier = Modifier
        )
    }
}