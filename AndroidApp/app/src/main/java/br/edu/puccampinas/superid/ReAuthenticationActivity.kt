package br.edu.puccampinas.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.edu.puccampinas.superid.screens.ReAuthenticationForm
import br.edu.puccampinas.superid.screens.RecoverPassword
import br.edu.puccampinas.superid.screens.RecoverPasswordForm
import br.edu.puccampinas.superid.screens.SignInForm
import br.edu.puccampinas.superid.screens.SignUpForm
import br.edu.puccampinas.superid.ui.theme.SuperIDTheme

class ReAuthenticationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ReAuthenticationNav(modifier = Modifier)
                }
            }
        }
    }
}

@Composable
fun ReAuthenticationNav(modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "reauth") {
        composable("reauth") { ReAuthenticationForm(navController = navController) }
        composable("recover") { RecoverPassword(navController = navController) }
    }
}
