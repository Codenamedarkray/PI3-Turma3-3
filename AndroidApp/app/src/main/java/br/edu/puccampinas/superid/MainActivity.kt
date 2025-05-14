package br.edu.puccampinas.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.edu.puccampinas.superid.functions.validationUtils.performLogout
import br.edu.puccampinas.superid.screens.PasswordScreen
import br.edu.puccampinas.superid.screens.ReadQRCodeScreen
import br.edu.puccampinas.superid.screens.TopAppBarWithLogout
import br.edu.puccampinas.superid.screens.UserProfileScreen
import br.edu.puccampinas.superid.ui.theme.SuperIDTheme

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
        composable("qr") { ReadQRCodeScreen(innerPadding = innerPadding) }
        composable("profile") { UserProfileScreen(innerPadding = innerPadding) }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "main",
            onClick = {
                if (currentRoute != "main") {
                    navController.navigate("main")
                }
            },
            icon = {
                Icon(Icons.Default.Lock, contentDescription = "Senhas")
            },
            label = { Text("Senhas") }
        )
        NavigationBarItem(
            selected = currentRoute == "qr",
            onClick = {
                if (currentRoute != "qr") {
                    navController.navigate("qr")
                }
            },
            icon = {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "QR Code")
            },
            label = { Text("QR Code") }
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile")
                }
            },
            icon = {
                Icon(Icons.Default.Person, contentDescription = "Perfil")
            },
            label = { Text("Perfil") }
        )
    }
}
