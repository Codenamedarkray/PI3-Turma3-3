package br.edu.puccampinas.superid.permissions

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.puccampinas.superid.R

@Composable
fun WithPermission(
    modifier: Modifier = Modifier,
    permission: String,
    buttonLabelPermission: String,
    content: @Composable () -> Unit
){
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
    }

    //Se a permissão não foi concedida, carregamos outra função composable
    // que é o RequestPermissionScreen
    if(!permissionGranted){
        //solicitar permissão
        RequestPermissionScreen(
            modifier = modifier,
            permission = permission,
            buttonLabelPermission = buttonLabelPermission
        ) {
            permissionGranted = true
        }
    }else{
        Surface(modifier = modifier){
            content()
        }
    }


}

@Composable
fun RequestPermissionScreen(
    modifier: Modifier = Modifier,
    permission: String,
    buttonLabelPermission: String,
    onPermissionGranted: () -> Unit
) {
    val montserrat = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onPermissionGranted()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117)), // caso deseje fundo escuro
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { launcher.launch(permission) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(56.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF007BFF), Color(0xFF00BCD4))
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonLabelPermission,
                    fontFamily = montserrat,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}
