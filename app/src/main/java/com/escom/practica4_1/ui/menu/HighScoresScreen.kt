package com.escom.practica4_1.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.escom.practica4_1.R
import com.escom.practica4_1.data.GameRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighScoresScreen(navController: NavController) {
    val context = LocalContext.current
    val gameRepository = remember { GameRepository(context) }
    
    val easyHighScore = gameRepository.getHighScore("easy")
    val mediumHighScore = gameRepository.getHighScore("medium")
    val hardHighScore = gameRepository.getHighScore("hard")
    
    val hasScores = easyHighScore > 0 || mediumHighScore > 0 || hardHighScore > 0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.high_scores_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (hasScores) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (easyHighScore > 0) {
                            Text(
                                text = stringResource(R.string.easy_high_score, easyHighScore),
                                fontSize = 20.sp
                            )
                        }
                        
                        if (mediumHighScore > 0) {
                            Text(
                                text = stringResource(R.string.medium_high_score, mediumHighScore),
                                fontSize = 20.sp
                            )
                        }
                        
                        if (hardHighScore > 0) {
                            Text(
                                text = stringResource(R.string.hard_high_score, hardHighScore),
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.no_scores_yet),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}