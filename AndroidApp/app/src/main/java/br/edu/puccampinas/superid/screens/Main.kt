package br.edu.puccampinas.superid.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.edu.puccampinas.superid.BottomNavigationBar
import br.edu.puccampinas.superid.functions.PasswordStorageUtils.createNewCategory
import br.edu.puccampinas.superid.functions.PasswordStorageUtils.createNewPassword
import br.edu.puccampinas.superid.functions.PasswordStorageUtils.deleteCategory
import br.edu.puccampinas.superid.functions.PasswordStorageUtils.deletePassword
import br.edu.puccampinas.superid.functions.PasswordStorageUtils.fetchPasswordData
import br.edu.puccampinas.superid.functions.PasswordStorageUtils.generateRandomBase64Token
import br.edu.puccampinas.superid.functions.PasswordStorageUtils.updatePassword
import br.edu.puccampinas.superid.functions.validationUtils.checkUserEmailVerification
import br.edu.puccampinas.superid.functions.validationUtils.performLogout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun PasswordScreen(innerPadding: PaddingValues){
    val context = LocalContext.current
    val uid = Firebase.auth.currentUser?.uid ?: return

    var verifiedEmail by remember { mutableStateOf(true) }
    val message = "Email não verificado, não será capaz de recuperar senha"
    val messageColor = Color.Red

    //guarda lista com nomes das categorias
    var categories by remember { mutableStateOf<List<DocumentSnapshot>>(emptyList()) }

    //guarda um Mapa ligando o nome da categoria com um par de valores(nome da plataforma, Mapa
    // ligando atributo da plataforma com valor do atributo)
    var passwordsMap by remember {
        mutableStateOf<Map<String, List<Pair<String, Map<String, Any?>>>>>(
            emptyMap()
        )
    }

    // Guada o nome das categorias ligadas a se elas estão expandidas mostrando as senhas
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    //variáveis para ver o menu de adição de categoria
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var isCategoryNameValid by remember { mutableStateOf(false) }

    //variaveis para menu de criação de senha
    var showCreatePasswordDialog by remember { mutableStateOf(false) }
    var newPasswordTitle by remember { mutableStateOf("") }
    var newPasswordEmail by remember { mutableStateOf("") }
    var newPasswordPassword by remember { mutableStateOf("") }
    var newPasswordDescription by remember { mutableStateOf("") }
    var selectedCategoryForPassword by remember { mutableStateOf<String?>(null) }

    //dados de visualização da senha
    var viewPasswordDialog by remember { mutableStateOf(false) }
    var selectedPlatformName by remember { mutableStateOf("") }
    var selectedPlatformData by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }
    var selectedCategoryId by remember { mutableStateOf<String>("") }

    //modo edição de categorias
    var isCategoryEditMode by remember { mutableStateOf(false) }

    checkUserEmailVerification(
        onResult = { isVerified ->
            if (!isVerified) {
                verifiedEmail = false
            }
        },
        onFailure = { e ->
            //nada a fazer.
        }
    )

    fun clearNewPasswordFields() {
        newPasswordTitle = ""
        newPasswordEmail = ""
        newPasswordPassword = ""
        newPasswordDescription = ""
        selectedCategoryForPassword = null
    }

    /**
     * rotina que pega as informações para atualizar a composição da tela
     * adiciona as categorias a lista de categorias e as senhas ao mapa de senhas
     */
    LaunchedEffect(uid) {
        if (uid != null) {
            fetchPasswordData(
                uid = uid,
                onCategoriesFetched = { fetchedCategories ->
                    categories = fetchedCategories
                },
                onPasswordsFetched = { fetchedPasswords ->
                    passwordsMap = fetchedPasswords
                },
                onExpandedMapUpdated = { expanded ->
                    expanded.forEach { (key, value) ->
                        expandedMap.putIfAbsent(key, value)
                    }
                }
            )
        } else {
            Toast.makeText(
                context,
                "Erro ao carregar informações, reinicie o App",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Se email não verificado, exibe aviso
        if (!verifiedEmail) {
            item {
                Text(
                    text = message,
                    color = messageColor
                )
            }
        }

        // Título
        item {
            Text("Minhas Senhas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // Botão de edição de categorias
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {isCategoryEditMode = !isCategoryEditMode}) {
                    Icon(
                        imageVector = if (isCategoryEditMode) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isCategoryEditMode) "Finalizar Edição" else "Editar Categorias"
                    )
                    val text =
                        if (isCategoryEditMode) "Finalizar Edição" else "Editar Categorias"
                    Text(text)
                }

                Button(onClick = { showCreateCategoryDialog = true }){
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar Categoria"
                    )
                    Text("Adicionar categoria")
                }



            }
        }

        // Lista de categorias
        items(categories.size) { index ->
            val category = categories[index]
            val categoryId = category.id
            val categoryName = categoryId
            val isExpanded = expandedMap[categoryId] ?: false
            val deletable = category.getBoolean("deletable") ?: true
            val passwords = passwordsMap[categoryId] ?: emptyList()

            CategoryCard(
                categoryId = categoryId,
                categoryName = categoryName,
                isExpanded = isExpanded,
                passwords = passwords,
                isEditMode = isCategoryEditMode,
                deletable = deletable,
                onExpandToggle = { expandedMap[categoryId] = !isExpanded },
                onPasswordClick = { platformName, platformData ->
                    selectedPlatformName = platformName
                    selectedPlatformData = platformData
                    selectedCategoryId = categoryId
                    viewPasswordDialog = true
                },
                onDeleteCategory = { categoryIdToDelete ->
                    deleteCategory(
                        uid = uid,
                        categoryId = categoryIdToDelete,
                        onSuccess = {
                            fetchPasswordData(
                                uid = uid,
                                onCategoriesFetched = { categories = it },
                                onPasswordsFetched = { passwordsMap = it },
                                onExpandedMapUpdated = { expanded ->
                                    expanded.forEach { (key, value) ->
                                        expandedMap.putIfAbsent(key, value)
                                    }
                                }
                            )
                        },
                        onFailure = { /* Tratar erro aqui se necessário */ }
                    )
                },
                onAddPasswordClick = {
                    selectedCategoryForPassword = categoryId
                    showCreatePasswordDialog = true
                }
            )
        }

    }

    if (showCreateCategoryDialog) {
        NewCategoryDialog(
            newCategoryName = newCategoryName,
            isCategoryNameValid = isCategoryNameValid,
            onNameChange = { text ->
                newCategoryName = text
                isCategoryNameValid =
                    categories.none { it.id.equals(newCategoryName, ignoreCase = true) }
            },
            onDismiss = {
                showCreateCategoryDialog = false
                newCategoryName = ""
            },
            onSave = {
                createNewCategory(
                    uid = uid,
                    categoryName = newCategoryName.trim(),
                    onSuccess = {
                        showCreateCategoryDialog = false
                        newCategoryName = ""
                        fetchPasswordData(
                            uid = uid,
                            onCategoriesFetched = { categories = it },
                            onPasswordsFetched = { passwordsMap = it },
                            onExpandedMapUpdated = { expanded ->
                                expanded.forEach { (key, value) ->
                                    expandedMap.putIfAbsent(key, value)
                                }
                            }
                        )
                    },
                    onFailure = {
                        // TODO: Tratar erro
                    }
                )
            }
        )
    }
    if (showCreatePasswordDialog) {
        NewPasswordDialog(
            categoryName = selectedCategoryForPassword, // Passa o nome da categoria
            title = newPasswordTitle,
            email = newPasswordEmail,
            password = newPasswordPassword,
            description = newPasswordDescription,
            onTitleChange = { newPasswordTitle = it },
            onEmailChange = { newPasswordEmail = it },
            onPasswordChange = { newPasswordPassword = it },
            onDescriptionChange = { newPasswordDescription = it },
            onDismiss = {
                showCreatePasswordDialog = false
                clearNewPasswordFields()
            },
            onSave = {
                if (selectedCategoryForPassword != null) {
                    createNewPassword(
                        uid = uid,
                        categoryName = selectedCategoryForPassword!!,
                        title = newPasswordTitle,
                        email = newPasswordEmail,
                        password = newPasswordPassword,
                        description = newPasswordDescription,
                        onSuccess = {
                            showCreatePasswordDialog = false
                            clearNewPasswordFields()
                            fetchPasswordData(
                                uid = uid,
                                onCategoriesFetched = { categories = it },
                                onPasswordsFetched = { passwordsMap = it },
                                onExpandedMapUpdated = { expanded ->
                                    expanded.forEach { (key, value) ->
                                        expandedMap.putIfAbsent(key, value)
                                    }
                                }
                            )
                        },
                        onFailure = {
                            // TODO: Tratar erro
                        }
                    )
                }
            }
        )
    }

    if (viewPasswordDialog) {
        ViewPasswordDialog(
            categoryId = selectedCategoryId,
            platformName = selectedPlatformName,
            initialData = selectedPlatformData,
            onDismiss = {
                viewPasswordDialog = false
                selectedPlatformName = ""
                selectedPlatformData = emptyMap()
                selectedCategoryId = ""
            },
            onSave = { updatedData ->
                updatePassword(
                    uid = uid,
                    category = selectedCategoryId,
                    title = selectedPlatformName,
                    updatedData = updatedData,
                    onSuccess = {
                        fetchPasswordData(
                            uid = uid,
                            onCategoriesFetched = { categories = it },
                            onPasswordsFetched = { passwordsMap = it },
                            onExpandedMapUpdated = { expanded ->
                                expanded.forEach { (key, value) ->
                                    expandedMap.putIfAbsent(key, value)
                                }
                            }
                        )
                    },
                    onFailure = {
                        Toast.makeText(context, "Erro ao salvar alterações", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            },
            onDelete = {
                deletePassword(
                    uid = uid,
                    category = selectedCategoryId,
                    title = selectedPlatformName,
                    onSuccess = {
                        fetchPasswordData(
                            uid = uid,
                            onCategoriesFetched = { categories = it },
                            onPasswordsFetched = { passwordsMap = it },
                            onExpandedMapUpdated = { expanded ->
                                expanded.forEach { (key, value) ->
                                    expandedMap.putIfAbsent(key, value)
                                }
                            }
                        )
                    },
                    onFailure = {
                        Toast.makeText(context, "Erro ao deletar senha", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithLogout(onLogout: () -> Unit) {
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        actions = {
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
            }
        },
        title = { Text("SuperID") }
    )
}

@Composable
fun NewCategoryDialog(
    newCategoryName: String,
    isCategoryNameValid: Boolean,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Nova Categoria") },
        text = {
            Column {
                TextField(
                    value = newCategoryName,
                    onValueChange = onNameChange,
                    label = { Text("Nome da categoria") },
                    singleLine = true,
                    isError = !isCategoryNameValid && newCategoryName.isNotBlank()
                )
                if (!isCategoryNameValid && newCategoryName.isNotBlank()) {
                    Text(
                        text = "Nome inválido ou já existente",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = isCategoryNameValid
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun CategoryCard(
    categoryId: String,
    categoryName: String,
    isExpanded: Boolean,
    passwords: List<Pair<String, Map<String, Any?>>>,
    isEditMode: Boolean,
    deletable: Boolean,
    onExpandToggle: () -> Unit,
    onPasswordClick: (platformName: String, platformData: Map<String, Any?>) -> Unit,
    onDeleteCategory: (categoryId: String) -> Unit,
    onAddPasswordClick: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showCannotDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onExpandToggle() }
                )

                Row {
                    if(!isEditMode) {
                        Icon(if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                            contentDescription = if (isExpanded)  "Expandido" else "Retraido")
                    }
                    // Só exibe botão de deletar se estiver no modo edição E for deletável
                    if (isEditMode && deletable) {
                        IconButton(
                            onClick = {
                                if (passwords.isEmpty()) {
                                    showDeleteConfirmation = true
                                } else {
                                    showCannotDeleteDialog = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir Categoria"
                            )
                        }
                    }
                }
            }

            if (isExpanded) {
                Divider()
                Column(Modifier.padding(12.dp)) {
                    TextButton(onClick = onAddPasswordClick) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Senha")
                        Text("Adicionar senha")
                    }
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
                                        onPasswordClick(platformName, platformData)
                                    }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogo de confirmação para deletar categoria vazia
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Deseja mesmo excluir a categoria '$categoryName'? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    onDeleteCategory(categoryId)
                }) {
                    Text("Confirmar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialogo informando que não pode deletar categoria com senhas
    if (showCannotDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showCannotDeleteDialog = false },
            title = { Text("Não é possível excluir") },
            text = { Text("A categoria '$categoryName' possui senhas cadastradas. Exclua todas as senhas antes de removê-la.") },
            confirmButton = {
                TextButton(onClick = {
                    showCannotDeleteDialog = false
                }) {
                    Text("Entendi")
                }
            },
            dismissButton = {}
        )
    }
}

@Composable
fun NewPasswordDialog(
    categoryName: String?,
    title: String,
    email: String,
    password: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Nova Senha") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = categoryName ?: "", // Exibe o nome da categoria
                    onValueChange = {}, // Impede a edição
                    label = { Text("Categoria") },
                    singleLine = true,
                    enabled = false // Desabilita o campo
                )
                TextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Título da Plataforma*") },
                    singleLine = true,
                    isError = title == "deletable"
                )
                TextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email/Login (Opcional)") },
                    singleLine = true
                )
                TextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Senha*") },
                    singleLine = true
                )
                TextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Descrição (Opcional)") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = !title.isBlank() && categoryName != null && !password.isBlank() && title != "deletable"
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ViewPasswordDialog(
    categoryId: String,
    platformName: String,
    initialData: Map<String, Any?>,
    onDismiss: () -> Unit,
    onSave: (updatedData: Map<String, Any?>) -> Unit,
    onDelete: () -> Unit
) {
    val newAcessToken = generateRandomBase64Token()


    var isEditing by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf(initialData["email"] as? String ?: "") }
    var password by remember { mutableStateOf(initialData["password"] as? String ?: "") }
    var description by remember { mutableStateOf(initialData["description"] as? String ?: "") }

    onSave(
        mapOf(
            "email" to email,
            "password" to password,
            "description" to description,
            "accessToken" to newAcessToken
        )
    )

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Detalhes da Senha") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = platformName,
                    onValueChange = {},
                    label = { Text("Plataforma") },
                    enabled = false
                )
                TextField(
                    value = email,
                    onValueChange = { if (isEditing) email = it },
                    label = { Text("Email/Login") },
                    enabled = isEditing
                )
                TextField(
                    value = password,
                    onValueChange = { if (isEditing) password = it },
                    label = { Text("Senha") },
                    isError = password.isBlank(),
                    enabled = isEditing
                )
                TextField(
                    value = description,
                    onValueChange = { if (isEditing) description = it },
                    label = { Text("Descrição") },
                    enabled = isEditing
                )
            }
        },
        confirmButton = {
            if (isEditing) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = {
                        // Cancelar edição
                        email = initialData["email"] as? String ?: ""
                        password = initialData["password"] as? String ?: ""
                        description = initialData["description"] as? String ?: ""
                        isEditing = false
                    }) {
                        Text("Cancelar")
                    }
                    TextButton(onClick = {
                        onSave(
                            mapOf(
                                "email" to email,
                                "password" to password,
                                "description" to description,
                                "accessToken" to newAcessToken
                            )
                        )
                        isEditing = false
                    },
                        enabled = password.isNotBlank()
                    ) {
                        Text("Salvar")
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = { showDeleteConfirmation = true }) {
                        Text("Deletar", color = Color.Red)
                    }
                    TextButton(onClick = { isEditing = true }) {
                        Text("Editar")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Fechar")
            }
        }
    )

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza que deseja excluir esta senha?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    onDelete()
                    onDismiss()
                }) {
                    Text("Confirmar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}