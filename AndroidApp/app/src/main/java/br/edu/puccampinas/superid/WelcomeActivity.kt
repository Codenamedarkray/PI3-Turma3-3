package br.edu.puccampinas.superid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.getString
import br.edu.puccampinas.superid.ui.theme.SuperIDTheme

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WelcomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier=modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = getString(context, R.string.app_name),
        )
        Button(onClick = {
            val intent = Intent(context, SignUpActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(
                text = "Faça seu cadastro"
            )
        }
        Button(onClick = {
            val intent = Intent(context, SignInActivity::class.java)
            context.startActivity(intent)
        }) {
            Text(
                text = "Entrar"
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SuperIDTheme {
        WelcomeScreen()
    }
}