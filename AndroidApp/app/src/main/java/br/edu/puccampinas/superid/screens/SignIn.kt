package br.edu.puccampinas.superid.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.navigation.compose.NavHost
import br.edu.puccampinas.superid.MainActivity
import br.edu.puccampinas.superid.WelcomeActivity
import br.edu.puccampinas.superid.functions.performSignIn
import br.edu.puccampinas.superid.ui.theme.SuperIDTheme

@Composable
fun SignInForm(modifier: Modifier = Modifier, navController: NavController) {
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

        Text("LOGIN", fontSize = 20.sp)

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
                    navController.navigate("recover")
                }
            )
        }

        Button(
            onClick = {
                performSignIn(
                    context,
                    email.replace(" ", ""),
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

        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ){
            Text(
                "Ainda não possui conta?",
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                "Cadastre-se",
                color = Color.Blue,
                fontSize = 16.sp,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier.clickable {
                    navController.navigate("signup")
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignIpFormPreview() {
    SuperIDTheme {
        SignInForm(
            modifier = Modifier,
            navController = TODO()
        )
    }
}