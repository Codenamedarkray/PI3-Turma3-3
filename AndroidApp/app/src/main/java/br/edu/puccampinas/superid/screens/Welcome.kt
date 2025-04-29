package br.edu.puccampinas.superid.screens

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.puccampinas.superid.AuthenticationActivity
import br.edu.puccampinas.superid.R
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

data class PageInfo(
    val title: String,
    val description: String,
    val imageRes: Int
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun WelcomeCarouselScreen(onFinishWelcome: () -> Unit) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    val appName = LocalContext.current.getString(R.string.app_name)

    val pages = listOf(
        PageInfo(
            title = "Bem-vindo ao $appName",
            description = "Seu cofre digital de senhas com segurança e praticidade.",
            imageRes = R.drawable.ic_shield_lock
        ),
        PageInfo(
            title = "Armazene Suas Senhas",
            description = "Organize todas as suas senhas em categorias personalizadas.",
            imageRes = R.drawable.ic_shield_lock
        ),
        PageInfo(
            title = "Login por QR Code",
            description = "Faça login em sites usando QR Code, de forma rápida e segura.",
            imageRes = R.drawable.ic_shield_lock
        ),
        PageInfo(
            title = "Segurança Avançada",
            description = "Criptografia forte protege suas senhas a todo momento.",
            imageRes = R.drawable.ic_shield_lock
        ),
        PageInfo(
            title = "Comece Agora",
            description = "Simplifique sua vida com o SuperID. Vamos começar!",
            imageRes = R.drawable.ic_shield_lock
        )
    )

    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(horizontal = 24.dp)
    ) {
        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageInfo = pages[page]

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = pageInfo.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .padding(bottom = 24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )

                Text(
                    text = pageInfo.title,
                    fontSize = 28.sp,
                    fontFamily = montserrat,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = pageInfo.description,
                    fontSize = 16.sp,
                    fontFamily = montserrat,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageIndicator(currentPage = pagerState.currentPage, pageCount = pages.size)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (pagerState.currentPage > 0) {
                    GradientButton(
                        text = "Voltar",
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .padding(end = 8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                GradientButton(
                    text = if (pagerState.currentPage == pages.size - 1) "Começar" else "Próximo",
                    onClick = {
                        coroutineScope.launch {
                            if (pagerState.currentPage < pages.size - 1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                onFinishWelcome()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PageIndicator(currentPage: Int, pageCount: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(pageCount) { index ->
            val animatedWidth by animateDpAsState(
                targetValue = if (index == currentPage) 16.dp else 8.dp,
                label = "IndicatorWidth"
            )

            val animatedColor by animateColorAsState(
                targetValue = if (index == currentPage) Color(0xFF00BCD4) else Color(0xFF9CA3AF),
                label = "IndicatorColor"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .width(animatedWidth)
                    .height(8.dp)
                    .background(
                        color = animatedColor,
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        if (enabled)
                            listOf(Color(0xFF007BFF), Color(0xFF00BCD4))
                        else
                            listOf(Color.Gray, Color.DarkGray)
                    ),
                    shape = RoundedCornerShape(50)
                )
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
}
