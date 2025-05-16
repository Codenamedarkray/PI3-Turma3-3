package br.edu.puccampinas.superid.permissions

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

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

    //Se a permissão não foi concedida, carregamos outra função ocmposable
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
){
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if(granted){
            onPermissionGranted()
        }
    }

    Box(modifier = modifier.fillMaxSize()){
        Button(
            modifier = modifier.align(Alignment.Center),
            onClick = { launcher.launch(permission) }
        ){
            Text(buttonLabelPermission)
        }
    }
}