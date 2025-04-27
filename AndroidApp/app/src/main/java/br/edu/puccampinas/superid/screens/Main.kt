package br.edu.puccampinas.superid.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.puccampinas.superid.functions.validationUtils.checkUserEmailVerification
import br.edu.puccampinas.superid.functions.validationUtils.performLogout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var verifiedEmail by remember { mutableStateOf(false) }
    val uid = Firebase.auth.currentUser?.uid
    val db = Firebase.firestore

    //guarda lista com nomes das categorias
    var categories by remember { mutableStateOf<List<DocumentSnapshot>>(emptyList()) }

    //guarda um Mapa ligando o nome da categoria com um par de valores(nome da plataforma, Mapa
    // ligando atributo da plataforma com valor do atributo)

    var passwordsMap by remember { mutableStateOf<Map<String, List<Pair<String, Map<String, Any?>>>>>(emptyMap()) }
    // Guada o nome das categorias ligadas a se elas estão expandidas mostrando as senhas
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    val message = "Por favor, verifique seu email para poder recuperar senha"
    val messageColor = Color.Red

    checkUserEmailVerification(
        onResult = { isVerified ->
            if (isVerified) {
                verifiedEmail = true
            }
        },
        onFailure = { e ->
            //nada a fazer.
        }
    )

    /**
     * rotina que pega as informações para atualizar a composição da tela
     * adiciona as categorias a lista de categorias e as senhas ao mapa de senhas
     */
    LaunchedEffect(uid) {
        if (uid != null) {
            //fetch nas categorias do usuário
            db.collection("users").document(uid).collection("category")
                .get()
                .addOnSuccessListener { snapshot ->
                    //obtém os documentos do fetch e salva na lista de categorias
                    val cats = snapshot.documents
                    categories = cats

                    //Para cada documento de categoria, faz o mapa para as senhas
                    cats.forEach { category ->
                        //coloca por padrão que a categoria está fechada na visualização
                        expandedMap.putIfAbsent(category.id, false)

                        //Pega os dados da categoria ou coloca como vazia a lista
                        val fields = category.data ?: emptyMap<String, Any>()

                        // Pega só os campos que não sejam "deletable"
                        val passwordEntries = fields
                            .filterKeys { it != "deletable" }
                            .map { (platformName, platformData) ->
                                platformName to (platformData as Map<String, Any?>)
                            }

                        // Atualiza o mapa de senhas
                        passwordsMap = passwordsMap + (category.id to passwordEntries)
                    }
                }
        }else{
            Toast.makeText(
                context,
                "Erro ao carregar informações, reinicie o App",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = {
                        performLogout(context)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                },
                title = {
                    Text("SuperID")
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Ícones a serem colocados",
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!verifiedEmail) {
                Text(message, color = messageColor)
            }
            Text("Minhas Senhas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            categories.forEach { category ->
                val categoryId = category.id
                val categoryName = categoryId
                val isExpanded = expandedMap[categoryId] ?: false
                val passwords = passwordsMap[categoryId] ?: emptyList()

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedMap[categoryId] = !isExpanded }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(categoryName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }

                        if (isExpanded) {
                            Divider()
                            Column(Modifier.padding(12.dp)) {
                                if (passwords.isEmpty()) {
                                    Text("Nenhuma senha cadastrada.", fontSize = 14.sp, color = Color.Gray)
                                } else {
                                    passwords.forEach { (platformName, platformData) ->
                                        Text(
                                            text = "• $platformName",
                                            fontSize = 14.sp,
                                            modifier = Modifier
                                                .padding(vertical = 2.dp)
                                                .clickable {
                                                    val email = platformData["email"] as? String ?: ""
                                                    val password = platformData["password"] as? String ?: ""
                                                    val description = platformData["description"] as? String ?: ""
                                                    val accessToken = platformData["accessToken"] as? String ?: ""

                                                    // Ainda falta definir como exibir a senha
                                                    Toast.makeText(
                                                        context,
                                                        "Usuário: $email\nSenha: $password\nDescrição: $description",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview(){
    MainScreen()
}