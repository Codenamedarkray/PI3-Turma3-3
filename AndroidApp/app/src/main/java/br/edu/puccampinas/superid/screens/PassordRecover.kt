package br.edu.puccampinas.superid.screens

import android.R
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.edu.puccampinas.superid.MainScreen
import br.edu.puccampinas.superid.functions.recoverPassword
import br.edu.puccampinas.superid.ui.theme.SuperIDTheme

@Composable
fun RecoverPasswordForm(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var messageColor by remember { mutableStateOf(Color.Unspecified) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                navController.navigate("signin")
            }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier.size(32.dp) //
            )
        }

        Text("Recuperar Senha", fontSize = 24.sp)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.replace(" ", "") },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            recoverPassword(
                email = email,
                onSuccess = {
                    message = "Link de recuperação enviado para o e-mail"
                    messageColor = Color.Blue
                },
                onFailure = {
                    message = it.message
                    messageColor = Color.Red
                }
            )
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Enviar link")
        }

        message?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = messageColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRecoverPasswordForm() {
    SuperIDTheme {
        RecoverPasswordForm(
            navController = TODO()
        )
    }
}