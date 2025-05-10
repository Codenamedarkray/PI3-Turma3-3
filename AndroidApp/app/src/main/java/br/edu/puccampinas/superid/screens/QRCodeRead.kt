package br.edu.puccampinas.superid.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.edu.puccampinas.superid.BottomNavigationBar
import br.edu.puccampinas.superid.functions.validationUtils.performLogout

@Composable
fun ReadQRCodeScreen(innerPadding: PaddingValues){
    Text("Qr", modifier = Modifier.padding(innerPadding))
}