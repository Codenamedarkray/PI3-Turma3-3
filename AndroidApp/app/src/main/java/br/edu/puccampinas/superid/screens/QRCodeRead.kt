package br.edu.puccampinas.superid.screens

import android.Manifest
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.edu.puccampinas.superid.BottomNavigationBar
import br.edu.puccampinas.superid.functions.confirmLogin
import br.edu.puccampinas.superid.functions.validQRCode
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
fun ReadQRCodeScreen(innerPadding: androidx.compose.foundation.layout.PaddingValues) {
    WithPermission(
        modifier = Modifier.padding(innerPadding),
        permission = Manifest.permission.CAMERA,
        buttonLabelPermission = "Conceder permissão de câmera"
    ) {
        QRScannerScreen()
    }
}

@Composable
fun ConfirmLoginWithoutPasswordDialog(
    document: DocumentSnapshot?,
    onDismiss: () -> Unit,
    onConfirm: (DocumentSnapshot?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmação de Login") },
        text = { Text("Deseja se autenticar no site?") },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(document)
                onDismiss()
            }) {
                Text("Sim")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun InvalidQRCodeDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("QR Code Inválido") },
        text = { Text("Este QR Code é inválido ou expirou.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}


@Composable
fun QRScannerScreen() {
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

        // Se quiser mostrar algo com o resultado:
        scannedText?.let {
            Text(
                text = "Último código: $it",
                modifier = Modifier.padding(16.dp)
            )
        }
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
                    loginToken = doc?.getString("loginToken").toString()
                )
            }
        )
    }
}