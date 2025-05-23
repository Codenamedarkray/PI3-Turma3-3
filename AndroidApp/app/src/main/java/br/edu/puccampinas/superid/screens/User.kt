package br.edu.puccampinas.superid.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.edu.puccampinas.superid.BottomNavigationBar
import br.edu.puccampinas.superid.functions.validationUtils.performLogout
import com.google.firebase.auth.FirebaseAuth

@Composable
fun UserProfileScreen(innerPadding: PaddingValues){
        Column(
                modifier = Modifier
                        .padding(innerPadding)
                        .background(Color.Black)
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                Text(
                        text = "Olá, usuário",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                                .padding(bottom = 24.dp)
                )

                val emailVerified = false
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
                                                onClick = { /* reenviar email */ },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))) {
                                                Text("Clique aqui para re-enviar email de verificação")
                                        }
                                }
                        }
                }

                Text(
                        text = "email@email.com",
                        color = Color.White,
                        modifier = Modifier
                                .padding(bottom = 24.dp)
                                .align(Alignment.CenterHorizontally)
                )

                val context = LocalContext.current
                val email = remember { mutableStateOf("") } // ou pegue de um TextField

                Button(
                        onClick = {
                                if (email.value.isNotBlank()) {
                                        FirebaseAuth.getInstance().sendPasswordResetEmail(email.value)
                                                .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                                Toast.makeText(context, "Email de redefinição enviado!", Toast.LENGTH_LONG).show()
                                                        } else {
                                                                Toast.makeText(context, "Erro ao enviar email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                }
                                } else {
                                        Toast.makeText(context, "Informe o email!", Toast.LENGTH_SHORT).show()
                                }
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


                Spacer(modifier = Modifier.weight(1f))

                Button(
                        onClick = { /* sair */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                ) {
                        Text("Sair", color = Color.White)
                }
        }
}