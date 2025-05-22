package br.edu.puccampinas.superid.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import br.edu.puccampinas.superid.screens.WelcomeFlow
// Data class

data class PageInfo(
    val title: String,
    val description: String,
    val imageRes: Int
)

@Composable
fun WelcomeFlow(onFinish: () -> Unit) {
    val context = LocalContext.current
    var showTermsScreen by rememberSaveable { mutableStateOf(false) }

    if (!showTermsScreen) {
        WelcomeCarouselScreen(onFinishWelcome = {
            showTermsScreen = true
        })
    } else {
        TermsScreen(onAccepted = {
            context.getSharedPreferences("superid_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("has_seen_welcome", true)
                .apply()
            onFinish()
        })
    }
}


@Composable
fun WelcomeCarouselScreen(onFinishWelcome: () -> Unit) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    val appName = LocalContext.current.getString(R.string.app_name)

    val pages = listOf(
        PageInfo("Bem-vindo ao $appName", "Seu cofre digital de senhas com segurança e praticidade.", R.drawable.ic_shield_lock),
        PageInfo("Armazene Suas Senhas", "Organize todas as suas senhas em categorias personalizadas.", R.drawable.ic_file_earmark_lock),
        PageInfo("Login por QR Code", "Faça login em sites usando QR Code, de forma rápida e segura.", R.drawable.ic_qr_code),
        PageInfo("Segurança Avançada", "Criptografia forte protege suas senhas a todo momento.", R.drawable.ic_lock_fill),
        PageInfo("Comece Agora", "Simplifique sua vida com o SuperID. Vamos começar!", R.drawable.ic_rocket_takeoff_fill)
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
fun TermsScreen(onAccepted: () -> Unit) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    var accepted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Termos de Uso",
                    fontSize = 28.sp,
                    fontFamily = montserrat,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Termos de Uso do SuperID\n" +
                            "Última atualização: [20/05/2025]\n" +
                            "\n" +
                            "Bem-vindo ao SuperID! Ao utilizar nosso aplicativo, você concorda com os seguintes Termos de Uso. Leia atentamente antes de usar o aplicativo.\n" +
                            "\n" +
                            "1. Aceitação dos Termos\n" +
                            "Ao acessar e utilizar o SuperID, você concorda em cumprir estes Termos de Uso e todas as leis e regulamentos aplicáveis. Caso não concorde com estes termos, por favor, não utilize o aplicativo.\n" +
                            "\n" +
                            "2. Descrição do Aplicativo\n" +
                            "O SuperID é um aplicativo de segurança digital que permite ao usuário:\n" +
                            "\n" +
                            "Armazenar com segurança informações como e-mails e senhas;\n" +
                            "\n" +
                            "Utilizar a câmera do dispositivo para leitura de QR Codes;\n" +
                            "\n" +
                            "Interagir com serviços em nuvem por meio do Firebase, incluindo autenticação e armazenamento de dados.\n" +
                            "\n" +
                            "3. Coleta e Uso de Dados\n" +
                            "Ao utilizar o SuperID, coletamos e armazenamos dados pessoais fornecidos por você, tais como:\n" +
                            "\n" +
                            "Endereço de e-mail;\n" +
                            "\n" +
                            "Senhas (armazenadas de forma segura e criptografada);\n" +
                            "\n" +
                            "Dados de uso e interação com o aplicativo;\n" +
                            "\n" +
                            "Imagens capturadas pela câmera, exclusivamente para fins de leitura de QR Codes.\n" +
                            "\n" +
                            "Esses dados são armazenados com segurança utilizando os serviços do Firebase (Google LLC).\n" +
                            "\n" +
                            "4. Permissões Necessárias\n" +
                            "Para o pleno funcionamento do SuperID, solicitamos as seguintes permissões:\n" +
                            "\n" +
                            "Acesso à câmera: necessário para escanear QR Codes;\n" +
                            "\n" +
                            "Acesso à internet: necessário para sincronização com o Firebase.\n" +
                            "\n" +
                            "O uso dessas permissões é restrito às funcionalidades essenciais do aplicativo e não é feito uso indevido ou contínuo sem necessidade.\n" +
                            "\n" +
                            "5. Responsabilidades do Usuário\n" +
                            "Você é o único responsável por manter a confidencialidade de suas credenciais de acesso e pelas informações que decide armazenar no aplicativo. Recomendamos o uso de senhas fortes e únicas.\n" +
                            "\n" +
                            "6. Segurança e Criptografia\n" +
                            "O SuperID utiliza criptografia para armazenar dados sensíveis como senhas, garantindo que terceiros não autorizados não possam acessá-los, mesmo em caso de violação do banco de dados.\n" +
                            "\n" +
                            "7. Limitação de Responsabilidade\n" +
                            "Embora tomemos medidas de segurança avançadas, o SuperID não se responsabiliza por perdas causadas por:\n" +
                            "\n" +
                            "Uso indevido do aplicativo;\n" +
                            "\n" +
                            "Vazamento de dados por negligência do usuário;\n" +
                            "\n" +
                            "Interrupções ou falhas nos serviços do Firebase ou na conexão com a internet.\n" +
                            "\n" +
                            "8. Modificações nos Termos\n" +
                            "Reservamo-nos o direito de atualizar estes Termos de Uso a qualquer momento. Notificaremos os usuários em caso de alterações significativas. O uso contínuo do aplicativo após tais mudanças constitui aceitação dos novos termos.\n",
                    fontSize = 16.sp,
                    fontFamily = montserrat,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Checkbox(
                        checked = accepted,
                        onCheckedChange = { accepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00BCD4))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Li e aceito os termos de uso",
                        fontFamily = montserrat,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onAccepted,
                    enabled = accepted,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    if (accepted) listOf(Color(0xFF007BFF), Color(0xFF00BCD4))
                                    else listOf(Color.Gray, Color.DarkGray)
                                ),
                                shape = RoundedCornerShape(50)
                            )
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Continuar",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = montserrat
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
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
                        if (enabled) listOf(Color(0xFF007BFF), Color(0xFF00BCD4))
                        else listOf(Color.Gray, Color.DarkGray)
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
