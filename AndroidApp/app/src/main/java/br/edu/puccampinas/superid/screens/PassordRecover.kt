package br.edu.puccampinas.superid.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.edu.puccampinas.superid.R
import br.edu.puccampinas.superid.functions.forceRecoverPassword
import br.edu.puccampinas.superid.functions.recoverPassword
import br.edu.puccampinas.superid.functions.validationUtils.emailIsInvalid
import kotlinx.coroutines.launch

@Composable
fun RecoverPasswordForm(navController: NavController) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarColor by remember { mutableStateOf(Color(0xFFDC2626)) }

    val isEmailValid = remember(email) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            kotlinx.coroutines.delay(3000L)
            showSnackbar = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(horizontal = 24.dp)
    ) {
        /** SETA DE VOLTAR NO TOPO **/
        IconButton(
            onClick = { navController.navigate("signin") },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        /** CONTEÚdo AGRUPADO E CENTRALIZADO **/
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RECUPERAR SENHA",
                fontSize = 28.sp,
                fontFamily = montserrat,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.replace(" ", "") },
                label = { Text("E-mail", fontFamily = montserrat) },
                textStyle = TextStyle(color = Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                trailingIcon = {
                    if (email.isNotBlank()) {
                        Icon(
                            imageVector = if (isEmailValid) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isEmailValid) Color(0xFF00FF00) else Color.Red
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF007BFF),
                    unfocusedBorderColor = Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Button(
                onClick = {
                    if (!isLoading) {
                        coroutineScope.launch {
                            isLoading = true
                            if (!emailIsInvalid(email)) {
                                forceRecoverPassword(
                                    email = email,
                                    onSuccess = {
                                        snackbarMessage = "Link enviado para o e-mail."
                                        showSnackbar = true
                                        isLoading = false
                                        snackbarColor = Color(0xFF007BFF)
                                    },
                                    onFailure = {
                                        snackbarMessage = "Erro: ${it.message ?: "Erro desconhecido"}"
                                        showSnackbar = true
                                        isLoading = false
                                        snackbarColor = Color(0xFFDC2626)
                                    }
                                )
                            } else {
                                snackbarMessage = "E-mail inválido"
                                showSnackbar = true
                                isLoading = false
                                snackbarColor = Color(0xFFDC2626)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF007BFF), Color(0xFF00BCD4))
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar Link", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }

        /** SNACKBAR BONITO **/
        AnimatedVisibility(
            visible = showSnackbar,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
        ) {
            Snackbar(
                containerColor = snackbarColor,
                contentColor = Color.White,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = snackbarMessage,
                    fontFamily = montserrat,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
