package br.edu.puccampinas.superid.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.edu.puccampinas.superid.BottomNavigationBar
import br.edu.puccampinas.superid.R
import br.edu.puccampinas.superid.functions.recoverPassword
import br.edu.puccampinas.superid.functions.sendVerificationEmail
import br.edu.puccampinas.superid.functions.validationUtils.checkUserEmailVerification
import br.edu.puccampinas.superid.functions.validationUtils.getSavedEmail
import br.edu.puccampinas.superid.functions.validationUtils.getUsername
import br.edu.puccampinas.superid.functions.validationUtils.performLogout
import com.google.firebase.auth.FirebaseAuth

@Composable
fun UserProfileScreen(innerPadding: PaddingValues){
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(getSavedEmail(context)) }
    var emailVerified by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarColor by remember { mutableStateOf(Color(0xFFDC2626)) }

    LaunchedEffect(isLoading) {
        checkUserEmailVerification(
            onResult= { isVerified ->
                emailVerified = isVerified

                getUsername(
                    onSuccess = { name ->
                        username = name.toString()
                        isLoading = false

                    },
                    onFailure = {

                    }
                )

            },
            onFailure = {

            }
        )
    }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            kotlinx.coroutines.delay(3000L)
            showSnackbar = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.Black)
        }
    }

     else {
        Box {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(Color.Black)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Olá, ${username}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                )

                if (!emailVerified) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color.White)
                            Text(
                                text = "Email não verificado.",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Button(
                                onClick = {
                                    sendVerificationEmail(
                                        onSuccess = {
                                            snackbarMessage = "Email enviado com sucesso"
                                            snackbarColor = Color(0xFF007BFF)
                                            showSnackbar = true
                                        },
                                        onFailure = {
                                            snackbarMessage = "Ocorreu um erro ao tentar enviar o email, tente novamente mais tarde"
                                            snackbarColor = Color(0xFFDC2626)
                                            showSnackbar = true
                                        }
                                    )

                                          },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF007BFF
                                    )
                                )
                            ) {
                                Text("Clique aqui para re-enviar email de verificação")
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }

                Text(
                    text = email.toString(),
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .align(Alignment.CenterHorizontally)
                )

                if (emailVerified) {
                    Button(
                        onClick = {
                            recoverPassword(
                                email = email.toString(),
                                onSuccess = {
                                    snackbarMessage = "Email enviado com sucesso"
                                    snackbarColor = Color(0xFF007BFF)
                                    showSnackbar = true
                                },
                                onFailure = {
                                    snackbarMessage = "Ocorreu um erro ao tentar enviar o email, tente novamente mais tarde"
                                    snackbarColor = Color(0xFFDC2626)
                                    showSnackbar = true
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Text(
                            text = "Redefinir senha",
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { performLogout(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sair", color = Color.White)
                }
            }
            AnimatedVisibility(
                visible = showSnackbar,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 120.dp)
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
}