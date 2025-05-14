package br.edu.puccampinas.superid.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import androidx.navigation.compose.currentBackStackEntryAsState
import br.edu.puccampinas.superid.BottomNavigationBar
import br.edu.puccampinas.superid.R
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.shape.RoundedCornerShape


@Composable
fun PasswordScreen(innerPadding: PaddingValues) {
    val context = LocalContext.current
    val uid = Firebase.auth.currentUser?.uid ?: return

    var verifiedEmail by remember { mutableStateOf(true) }
    val message = "Seu e-mail ainda não foi verificado. Verifique-o para habilitar recuperação de senha futuramente."

    var categories by remember { mutableStateOf<List<DocumentSnapshot>>(emptyList()) }
    var passwordsMap by remember {
        mutableStateOf<Map<String, List<Pair<String, Map<String, Any?>>>>>(emptyMap())
    }
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var isCategoryNameValid by remember { mutableStateOf(false) }

    var showCreatePasswordDialog by remember { mutableStateOf(false) }
    var newPasswordTitle by remember { mutableStateOf("") }
    var newPasswordEmail by remember { mutableStateOf("") }
    var newPasswordPassword by remember { mutableStateOf("") }
    var newPasswordDescription by remember { mutableStateOf("") }
    var selectedCategoryForPassword by remember { mutableStateOf<String?>(null) }

    var viewPasswordDialog by remember { mutableStateOf(false) }
    var selectedPlatformName by remember { mutableStateOf("") }
    var selectedPlatformData by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }
    var selectedCategoryId by remember { mutableStateOf("") }

    var isCategoryEditMode by remember { mutableStateOf(false) }

    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    checkUserEmailVerification(
        onResult = { isVerified ->
            if (!isVerified) verifiedEmail = false
        },
        onFailure = { }
    )

    fun clearNewPasswordFields() {
        newPasswordTitle = ""
        newPasswordEmail = ""
        newPasswordPassword = ""
        newPasswordDescription = ""
        selectedCategoryForPassword = null
    }

    LaunchedEffect(uid) {
        fetchPasswordData(
            uid = uid,
            onCategoriesFetched = { categories = it },
            onPasswordsFetched = { passwordsMap = it },
            onExpandedMapUpdated = { expanded ->
                expanded.forEach { (key, value) -> expandedMap.putIfAbsent(key, value) }
            }
        )
    }

    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .padding(innerPadding)
            .background(Color.Black)
            .padding(16.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!verifiedEmail) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFF3B3B), shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = message,
                        color = Color.White,
                        fontFamily = montserrat,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        item {
            /**Text(
                "SuperID",
                fontFamily = montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.White
            )*/
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { isCategoryEditMode = !isCategoryEditMode },
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Editar Categorias", fontFamily = montserrat, color = Color.White)
                }
                OutlinedButton(
                    onClick = { showCreateCategoryDialog = true },
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Nova Categoria", fontFamily = montserrat, color = Color.White)
                }
            }
        }

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
                        onFailure = { }
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
            onNameChange = {
                newCategoryName = it
                isCategoryNameValid = categories.none { it.id.equals(newCategoryName, ignoreCase = true) }
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
                    onFailure = { }
                )
            }
        )
    }

    if (showCreatePasswordDialog) {
        NewPasswordDialog(
            categoryName = selectedCategoryForPassword,
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
                        onFailure = { }
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
                        Toast.makeText(context, "Erro ao salvar alterações", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(context, "Erro ao deletar senha", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithLogout(onLogout: () -> Unit) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    TopAppBar(
        title = {
            Text(
                text = "SuperID",
                fontFamily = montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        },
        actions = {
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color.White
                )
            }
        },
        colors = topAppBarColors(
            containerColor = Color(0xFF0D1117),
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
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
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(
                    "Nova Categoria",
                    fontFamily = montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = onNameChange,
                        label = { Text("Nome da categoria", fontFamily = montserrat) },
                        singleLine = true,
                        isError = !isCategoryNameValid && newCategoryName.isNotBlank(),
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF007BFF),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF007BFF),
                            cursorColor = Color.White
                        )
                    )
                    if (!isCategoryNameValid && newCategoryName.isNotBlank()) {
                        Text(
                            "Nome inválido ou já existente",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontFamily = montserrat
                        )
                    }
                }
            },
            containerColor = Color(0xFF0D1117),
            confirmButton = {
                Button(
                    onClick = onSave,
                    enabled = isCategoryNameValid && newCategoryName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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
                        Text("Salvar", color = Color.White, fontFamily = montserrat)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancelar", fontFamily = montserrat, color = Color(0xFF9CA3AF))
                }
            }
        )
    }
}



@Composable
fun CategoryCard(
    categoryId: String,
    categoryName: String,
    isExpanded: Boolean,
    passwords: List<Pair<String, Map<String, Any?>>>?,
    isEditMode: Boolean,
    deletable: Boolean,
    onExpandToggle: () -> Unit,
    onPasswordClick: (platformName: String, platformData: Map<String, Any?>) -> Unit,
    onDeleteCategory: (categoryId: String) -> Unit,
    onAddPasswordClick: () -> Unit
) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showCannotDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = montserrat
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isEditMode) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    if (isEditMode && deletable) {
                        IconButton(onClick = {
                            if (passwords.isNullOrEmpty()) {
                                showDeleteConfirmation = true
                            } else {
                                showCannotDeleteDialog = true
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir",
                                tint = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Divider(color = Color(0xFF1F2937), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                    TextButton(
                        onClick = onAddPasswordClick,
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = Color(0xFF00BCD4))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Adicionar senha", color = Color(0xFF00BCD4), fontFamily = montserrat)
                    }

                    if (passwords.isNullOrEmpty()) {
                        Text(
                            text = "Nenhuma senha cadastrada.",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF),
                            fontFamily = montserrat
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            passwords.forEach { (platformName, platformData) ->
                                OutlinedButton(
                                    onClick = { onPasswordClick(platformName, platformData) },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, Color.Gray),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text(
                                        text = platformName,
                                        fontFamily = montserrat,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
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
            containerColor = Color(0xFF0D1117),
            title = {
                Text(
                    "Confirmar Exclusão",
                    fontFamily = montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Deseja mesmo excluir a categoria '$categoryName'? Esta ação não pode ser desfeita.",
                    fontFamily = montserrat,
                    fontSize = 14.sp,
                    color = Color(0xFFD1D5DB)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteCategory(categoryId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Confirmar", fontFamily = montserrat, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar", fontFamily = montserrat, color = Color(0xFF9CA3AF))
                }
            }
        )
    }

    if (showCannotDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showCannotDeleteDialog = false },
            containerColor = Color(0xFF0D1117),
            title = {
                Text(
                    "Não é possível excluir",
                    fontFamily = montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "A categoria '$categoryName' possui senhas cadastradas. Exclua todas as senhas antes de removê-la.",
                    fontFamily = montserrat,
                    fontSize = 14.sp,
                    color = Color(0xFFD1D5DB)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showCannotDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
                ) {
                    Text("Entendi", fontFamily = montserrat, color = Color.White)
                }
            }
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
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f)
    ) {
        AlertDialog(
            onDismissRequest = {
                visible = false
                onDismiss()
            },
            title = {
                Text(
                    text = "Nova Senha",
                    fontFamily = montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF007BFF),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF007BFF),
                    cursorColor = Color.White
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Categoria",
                            fontFamily = montserrat,
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = Color.Gray,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = categoryName ?: "",
                                fontFamily = montserrat,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = { Text("Título da Plataforma*", fontFamily = montserrat) },
                        singleLine = true,
                        isError = title == "deletable",
                        textStyle = TextStyle(color = Color.White),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email/Login (Opcional)", fontFamily = montserrat) },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Senha*", fontFamily = montserrat) },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Descrição (Opcional)", fontFamily = montserrat) },
                        textStyle = TextStyle(color = Color.White),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = Color(0xFF0D1117),
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Button(
                        onClick = {
                            visible = false
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancelar", fontFamily = montserrat, color = Color.White)
                    }

                    Button(
                        onClick = onSave,
                        enabled = !title.isBlank() && categoryName != null && !password.isBlank() && title != "deletable",
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF007BFF), Color(0xFF00BCD4))
                                    ),
                                    shape = MaterialTheme.shapes.medium
                                )
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Salvar", color = Color.White, fontFamily = montserrat)
                        }
                    }
                }
            }
        )
    }
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
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    val newAcessToken = generateRandomBase64Token()

    var isEditing by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf(initialData["email"] as? String ?: "") }
    var password by remember { mutableStateOf(initialData["password"] as? String ?: "") }
    var description by remember { mutableStateOf(initialData["description"] as? String ?: "") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF007BFF),
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = Color(0xFF007BFF),
        cursorColor = Color.White
    )

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Detalhes da Senha",
                fontFamily = montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = platformName,
                    onValueChange = {},
                    label = { Text("Plataforma", fontFamily = montserrat) },
                    singleLine = true,
                    enabled = false,
                    textStyle = TextStyle(color = Color.White),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { if (isEditing) email = it },
                    label = { Text("Email/Login", fontFamily = montserrat) },
                    singleLine = true,
                    enabled = true,
                    textStyle = TextStyle(color = Color.White),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { if (isEditing) password = it },
                    label = { Text("Senha", fontFamily = montserrat) },
                    isError = password.isBlank(),
                    enabled = true,
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (isEditing) description = it },
                    label = { Text("Descrição", fontFamily = montserrat) },
                    enabled = true,
                    textStyle = TextStyle(color = Color.White),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        containerColor = Color(0xFF0D1117),
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Button(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Deletar", fontFamily = montserrat, color = Color.White)
                }
                Button(
                    onClick = {
                        if (isEditing) {
                            onSave(
                                mapOf(
                                    "email" to email,
                                    "password" to password,
                                    "description" to description,
                                    "accessToken" to newAcessToken
                                )
                            )
                        }
                        isEditing = !isEditing
                    },
                    enabled = !isEditing || password.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF007BFF), Color(0xFF00BCD4))
                                ),
                                shape = MaterialTheme.shapes.medium
                            )
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isEditing) "Salvar" else "Editar",
                            fontFamily = montserrat,
                            color = Color.White
                        )
                    }
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text("Fechar", fontFamily = montserrat)
                }
            }
        }
    )

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    "Confirmar Exclusão",
                    fontFamily = montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    "Tem certeza que deseja excluir esta senha?",
                    fontFamily = montserrat,
                    color = Color(0xFF9CA3AF),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            containerColor = Color(0xFF0D1117),
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Button(
                        onClick = {
                            showDeleteConfirmation = false
                            onDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Confirmar", fontFamily = montserrat, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirmation = false },
                        border = BorderStroke(1.dp, Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancelar", fontFamily = montserrat, color = Color.White)
                    }
                }
            },
            dismissButton = {}
        )
    }
}
