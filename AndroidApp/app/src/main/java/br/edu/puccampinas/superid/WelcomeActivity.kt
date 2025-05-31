package br.edu.puccampinas.superid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.edu.puccampinas.superid.screens.WelcomeCarouselScreen
import br.edu.puccampinas.superid.ui.theme.SuperIDTheme

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperIDTheme {
                WelcomeCarouselScreen(
                    onFinishWelcome = {
                        // Salva que o Welcome foi visto
                        val sharedPreferences = getSharedPreferences("superid_prefs", MODE_PRIVATE)
                        sharedPreferences.edit()
                            .putBoolean("has_seen_welcome", true)
                            .apply()

                        // Agora vai para o AuthenticationActivity
                        val intent = Intent(this@WelcomeActivity, AuthenticationActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}
