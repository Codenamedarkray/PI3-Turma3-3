package br.edu.puccampinas.superid.screens

import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.edu.puccampinas.superid.MainActivity
import br.edu.puccampinas.superid.R
import br.edu.puccampinas.superid.ReAuthenticationActivity
import br.edu.puccampinas.superid.functions.recoverPassword
import br.edu.puccampinas.superid.functions.sendVerificationEmail
import br.edu.puccampinas.superid.functions.validationUtils.checkUserEmailVerification
import br.edu.puccampinas.superid.functions.validationUtils.getSavedEmail
import br.edu.puccampinas.superid.functions.validationUtils.passwordIsInvalid
import br.edu.puccampinas.superid.functions.validationUtils.performLogout
import br.edu.puccampinas.superid.functions.validationUtils.reauthenticateUser

@Composable
fun ReAuthenticationForm(modifier: Modifier = Modifier, navController: NavController) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var isButtonPressed by remember { mutableStateOf(false) }

    val buttonScale by animateFloatAsState(
        targetValue = if (isButtonPressed) 0.95f else 1f,
        label = "ButtonScale"
    )

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            kotlinx.coroutines.delay(3000L)
            showSnackbar = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(horizontal = 24.dp)
    ) {
        IconButton(onClick = { performLogout(context) } ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Logout",
                tint = Color.White,
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "INSIRA A SENHA MESTRA",
                fontFamily = montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha", fontFamily = montserrat) },
                textStyle = TextStyle(color = Color.White),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = "Mostrar senha", tint = Color.White)
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Esqueceu a senha?",
                    color = Color.White,
                    fontFamily = montserrat,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Clique aqui",
                    color = Color(0xFF007BFF),
                    fontFamily = montserrat,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { navController.navigate("recover") }
                )
            }

            Button(
                onClick = {
                    if (!passwordIsInvalid(password) && !isLoading) {
                        isButtonPressed = true
                        isLoading = true
                        reauthenticateUser(
                            context,
                            password,
                            onSuccess = {
                                isButtonPressed = false
                                isLoading = false
                                val intent = Intent(context, MainActivity::class.java)
                                context.startActivity(intent)
                            },
                            onFailure = { exception ->
                                Log.e("LOGIN", "ERRO AO AUTENTICAR: ${exception.message}")
                                isButtonPressed = false
                                isLoading = false
                                showSnackbar = true
                            }
                        )
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(buttonScale)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
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
                        Text(
                            text = "Entrar",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = montserrat
                        )
                    }
                }
            }
        }

        // Snackbar agora aparece embaixo fixo
        AnimatedVisibility(
            visible = showSnackbar,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Snackbar(
                containerColor = Color(0xFFDC2626),
                contentColor = Color.White,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "SENHA INCORRETA",
                    fontFamily = montserrat,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun RecoverPassword(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    var isLoading by remember { mutableStateOf(true) }
    var resultMessage by remember { mutableStateOf<String?>("Erro ao enviar email de recuperação. Tente novamente mais tarde.") }
    var isVerified by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val email = getSavedEmail(context)
        checkUserEmailVerification(
            onResult = { userVerified ->
                isVerified = userVerified
                if (!isVerified) {
                    sendVerificationEmail()
                    resultMessage = "Seu email ainda não foi verificado.\nUm novo email de verificação foi enviado para $email."
                } else {
                    recoverPassword(
                        email = email.toString(),
                        onSuccess = {
                            resultMessage = "Email de recuperação de senha enviado para $email."
                        },
                        onFailure = {
                            resultMessage = "Erro ao enviar email de recuperação. Tente novamente mais tarde."
                        }
                    )
                }
                isLoading = false
            },
            onFailure = {
                resultMessage = "Erro ao verificar email. Tente novamente."
                isLoading = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = resultMessage ?: "Erro desconhecido.",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = montserrat
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
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
                        Text(
                            text = "Voltar",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = montserrat
                        )
                    }
                }
            }
        }
    }
}


