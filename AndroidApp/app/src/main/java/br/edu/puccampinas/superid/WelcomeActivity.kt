package br.edu.puccampinas.superid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.edu.puccampinas.superid.ui.theme.SuperIDTheme
import com.google.api.Context

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyApp(Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun MyApp(modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController) }
        composable("terms") { TermsScreen(navController) }
    }
}

@Composable
fun WelcomeScreen(navController: NavController) {
    val appName: String = getString(LocalContext.current, R.string.app_name)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Bem-vindo ao $appName", fontSize = 24.sp)
        Text("Explicação do app a ser Estruturada.")

        Button(
            onClick = { navController.navigate("terms") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Próximo")
        }
    }
}

@Composable
fun TermsScreen(navController: NavController) {
    var accepted: Boolean by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = {
                navController.navigate("welcome")
            }
        ) {
            Text("←")
        }
        Text("Termos de Uso")
        Text("Termos de uso a serem definidos")

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = accepted, onCheckedChange = { accepted = it })
            Spacer(modifier = Modifier.width(8.dp))
            Text("Li e aceito os termos de uso")
        }

        Button(
            onClick = {
                val intent = Intent(context, SignInActivity::class.java)
                context.startActivity(intent)
            },
            enabled = accepted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SuperIDTheme {
        MyApp(modifier = Modifier)
    }
}