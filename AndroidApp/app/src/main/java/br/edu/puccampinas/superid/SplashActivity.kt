package br.edu.puccampinas.superid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.puccampinas.superid.screens.WelcomeFlow
import br.edu.puccampinas.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SuperIDTheme {
                val context = this@SplashActivity
                var showSplash by remember { mutableStateOf(true) }

                val sharedPreferences = context.getSharedPreferences("superid_prefs", Context.MODE_PRIVATE)
                val hasSeenWelcome = sharedPreferences.getBoolean("has_seen_welcome", false)
                val user = FirebaseAuth.getInstance().currentUser

                when {
                    showSplash -> {
                        SplashScreen(onFinish = {
                            showSplash = false
                        })
                    }

                    user != null -> {
                        LaunchedEffect(Unit) {
                            context.startActivity(Intent(context, ReAuthenticationActivity::class.java))
                            finish()
                        }
                    }

                    else -> {
                        WelcomeFlow(onFinish = {
                            sharedPreferences.edit().putBoolean("has_seen_welcome", true).apply()
                            context.startActivity(Intent(context, AuthenticationActivity::class.java))
                            finish()
                        })
                    }

                }
            }
        }
    }
}




@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
        delay(1000L)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_shield_lock),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value
                    )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SuperID",
                fontFamily = montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.White
            )
        }
    }
}
