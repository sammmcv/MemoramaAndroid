package com.escom.practica4_1.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.escom.practica4_1.R

@OptIn(ExperimentalMaterial3Api::class) // Add this annotation to acknowledge experimental API
@Composable
fun DifficultyScreen(
    onDifficultySelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.difficulty_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        // Add back icon here if needed
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.difficulty_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            Button(
                onClick = { onDifficultySelected("easy") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(text = stringResource(R.string.easy_difficulty))
            }
            
            Button(
                onClick = { onDifficultySelected("medium") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(text = stringResource(R.string.medium_difficulty))
            }
            
            Button(
                onClick = { onDifficultySelected("hard") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(text = stringResource(R.string.hard_difficulty))
            }
        }
    }
}