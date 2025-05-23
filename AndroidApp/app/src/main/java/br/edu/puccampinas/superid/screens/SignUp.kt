package br.edu.puccampinas.superid.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.edu.puccampinas.superid.MainActivity
import br.edu.puccampinas.superid.R
import br.edu.puccampinas.superid.functions.performSignUp
import kotlinx.coroutines.launch

@Composable
fun SignUpForm(modifier: Modifier = Modifier, navController: NavController) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isEmailValid = remember(email) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val isPasswordValid = password.length >= 6
    val isNameValid = name.trim().length >= 1

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 100.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CRIAR CONTA",
                fontSize = 28.sp,
                fontFamily = montserrat,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome", fontFamily = montserrat) },
                textStyle = TextStyle(color = Color.White),
                trailingIcon = {
                    if (name.isNotBlank()) {
                        Icon(
                            imageVector = if (isNameValid) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isNameValid) Color(0xFF00FF00) else Color.Red
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF007BFF),
                    unfocusedBorderColor = Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
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
                    .padding(bottom = 16.dp)
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
                    .padding(bottom = 16.dp)
            )



            AnimatedVisibility(visible = password.isNotBlank()) {
                Text(
                    text = if (isPasswordValid) "Senha válida" else "A senha deve ter no mínimo 6 caracteres",
                    color = if (isPasswordValid) Color(0xFF00FF00) else Color(0xFFFF5555),
                    fontSize = 12.sp,
                    fontFamily = montserrat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    if (isPasswordValid && !isLoading) {
                        coroutineScope.launch {
                            isLoading = true
                            performSignUp(
                                context,
                                name,
                                email.replace(" ", ""),
                                password,
                                onSuccess = {
                                    val intent = Intent(context, MainActivity::class.java)
                                    context.startActivity(intent)

                                    isLoading = false
                                },
                                onFailure = {
                                    snackbarMessage = "Erro ao criar conta. Tente novamente."
                                    showSnackbar = true
                                    isLoading = false
                                }
                            )
                        }
                    } else if (!isPasswordValid) {
                        snackbarMessage = "Erro ao criar conta. Confira todos os campos e tente novamente."
                        showSnackbar = true
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
                        Text(
                            text = "Criar Minha Conta",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text(
                    text = "Já possui conta?",
                    fontSize = 16.sp,
                    fontFamily = montserrat,
                    color = Color(0xFF9CA3AF)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Entrar",
                    fontSize = 16.sp,
                    fontFamily = montserrat,
                    color = Color(0xFF00BCD4),
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    modifier = Modifier.clickable { navController.navigate("signin") }
                )
            }
        }

        AnimatedVisibility(
            visible = showSnackbar,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
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
                    text = snackbarMessage,
                    fontFamily = montserrat,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
