package br.edu.puccampinas.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.edu.puccampinas.superid.functions.validationUtils.performLogout
import br.edu.puccampinas.superid.screens.PasswordScreen
import br.edu.puccampinas.superid.screens.ReadQRCodeScreen
import br.edu.puccampinas.superid.screens.UserProfileScreen
import br.edu.puccampinas.superid.ui.theme.SuperIDTheme
import androidx.compose.ui.Alignment


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                val navController = rememberNavController()
                ScaffoldLayout(navController)
            }
        }
    }
}

@Composable
fun ScaffoldLayout(navController: NavHostController){
    val context = LocalContext.current
    BackHandler(enabled = true){

    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    Scaffold(
        topBar = { TopAppBarWithLogout { performLogout(context) } },
        bottomBar = { BottomNavigationBar(navController, currentRoute = currentRoute) },
    ) { innerPadding ->
        MainActivityNav(innerPadding = innerPadding, navController = navController)
    }
}

@Composable
fun MainActivityNav(innerPadding: PaddingValues, navController: NavHostController) {
    NavHost(navController = navController, startDestination = "main", modifier = Modifier.fillMaxSize()) {
        composable("main") { PasswordScreen(innerPadding = innerPadding) }
        composable("qr") { ReadQRCodeScreen(innerPadding = innerPadding, navController = navController) }
        composable("profile") { UserProfileScreen(innerPadding = innerPadding) }
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
        colors = topAppBarColors(
            containerColor = Color(0xFF0D1117),
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    NavigationBar(
        containerColor = Color.Black,
        contentColor = Color.Black
    ) {
        listOf(
            Triple("main", Icons.Default.Lock, "Senhas"),
            Triple("qr", Icons.Default.QrCodeScanner, "QR Code"),
            Triple("profile", Icons.Default.Person, "Perfil")
        ).forEach { (route, icon, label) ->
            val isSelected = currentRoute == route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(route)
                    }
                },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .height(3.dp)
                                    .width(24.dp)
                                    .background(Color(0xFF007BFF), shape = RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        } else {
                            Spacer(modifier = Modifier.height(7.dp))
                        }

                        Icon(imageVector = icon, contentDescription = label, tint = Color.White)
                    }
                },
                label = {
                    Text(label, fontFamily = montserrat, color = Color.White)
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent, // <-- remove o fundo branco
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.White
                )

            )
        }
    }
}

