package com.escom.practica4_1.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.escom.practica4_1.R

@Composable
fun MainMenuScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.main_menu_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = { navController.navigate("difficulty") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.play_button),
                fontSize = 18.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        // Botón para ver puntuaciones
        Button(
            onClick = { navController.navigate("high_scores") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.high_scores),
                fontSize = 18.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}