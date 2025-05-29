package br.edu.puccampinas.superid.screens

import android.Manifest
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.edu.puccampinas.superid.BottomNavigationBar
import br.edu.puccampinas.superid.R
import br.edu.puccampinas.superid.functions.confirmLogin
import br.edu.puccampinas.superid.functions.validQRCode
import br.edu.puccampinas.superid.functions.validationUtils.checkUserEmailVerification
import br.edu.puccampinas.superid.functions.validationUtils.performLogout
import br.edu.puccampinas.superid.permissions.WithPermission
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay

@Composable
fun EmailNotValidatedDialog(onDismiss: () -> Unit) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1117),
        title = {
            Text(
                "Email não verificado!",
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
                "Para utilizar a funcionalidade login sem senha, por favor verifique seu email.",
                fontFamily = montserrat,
                fontSize = 14.sp,
                color = Color(0xFFD1D5DB),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
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
                    Text("Ok", color = Color.White, fontFamily = montserrat)
                }
            }
        }
    )
}


@Composable
fun ReadQRCodeScreen(
    innerPadding: PaddingValues,
    navController: NavController
) {
    var verifiedEmail by remember { mutableStateOf(true) }

    checkUserEmailVerification(
        onResult = { isVerified ->
            if (!isVerified) verifiedEmail = false
        },
        onFailure = { }
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D1117))) {
        if (!verifiedEmail) {
            EmailNotValidatedDialog {
                navController.popBackStack()
            }
        }

        WithPermission(
            modifier = Modifier.padding(innerPadding),
            permission = Manifest.permission.CAMERA,
            buttonLabelPermission = "Conceder permissão de câmera"
        ) {
            QRScannerScreen(navController = navController)
        }
    }
}

@Composable
fun ConfirmLoginWithoutPasswordDialog(
    document: DocumentSnapshot?,
    onDismiss: () -> Unit,
    onConfirm: (DocumentSnapshot?) -> Unit
) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1117),
        title = {
            Text(
                "Confirmação de Login",
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
                "Deseja se autenticar no site?",
                fontFamily = montserrat,
                fontSize = 14.sp,
                color = Color(0xFFD1D5DB),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(document)
                    onDismiss()
                },
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
                    Text("Sim", color = Color.White, fontFamily = montserrat)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontFamily = montserrat, color = Color(0xFF9CA3AF))
            }
        }
    )
}

@Composable
fun InvalidQRCodeDialog(onDismiss: () -> Unit) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1117),
        title = {
            Text(
                "QR Code Inválido",
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
                "Este QR Code é inválido ou expirou.",
                fontFamily = montserrat,
                fontSize = 14.sp,
                color = Color(0xFFD1D5DB),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
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
                    Text("OK", color = Color.White, fontFamily = montserrat)
                }
            }
        }
    )
}


@Composable
fun QRScannerScreen(navController: NavController) {
    val lifecycleOwner = LocalLifecycleOwner.current

    var scannedText by remember { mutableStateOf<String?>(null) }

    var invalidDialog by remember {mutableStateOf(false)}
    var loginDialog by remember {mutableStateOf(false)}

    var document by remember {mutableStateOf<DocumentSnapshot?>(null)}

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val barcodeView = DecoratedBarcodeView(ctx).apply {
                    barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
                    initializeFromIntent(Intent())
                    resume()
                    statusView.visibility = View.GONE
                }

                barcodeView.decodeContinuous(object : BarcodeCallback {
                    override fun barcodeResult(result: BarcodeResult?) {
                        val code = result?.text ?: return

                        if (scannedText != code) {
                            scannedText = code
                            Log.d("QRScanner", "QR Lido: $code")

                            validQRCode(
                                loginToken = code,
                                onSuccess = { doc->
                                    document = doc
                                    loginDialog = true
                                },
                                onFailure = {
                                    invalidDialog = true
                                }
                            )

                            scannedText = ""

                        }
                    }

                    override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
                })

                lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onPause(owner: LifecycleOwner) {
                        barcodeView.pause()
                    }

                    override fun onResume(owner: LifecycleOwner) {
                        barcodeView.resume()
                    }
                })

                barcodeView
            }
        )

    }

    if(invalidDialog){
        InvalidQRCodeDialog(
            onDismiss = { invalidDialog = !invalidDialog }
        )

    }
    if(loginDialog){
        ConfirmLoginWithoutPasswordDialog(
            document = document,
            onDismiss = { loginDialog = !loginDialog},
            onConfirm = { doc ->
                confirmLogin(
                    loginToken = doc?.getString("loginToken").toString(),
                    onComplete = {
                        navController.navigate("main")
                    }
                )

            }
        )
    }
}