package com.escom.practica4_1.ui.game

import android.util.Log  // Añadir esta importación
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.escom.practica4_1.R
import com.escom.practica4_1.data.GameRepository
import com.escom.practica4_1.model.Card
import com.escom.practica4_1.model.GameState 
import com.escom.practica4_1.data.GameStateManager
import kotlinx.coroutines.delay

// En GameScreen(), al inicio de la función
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    difficulty: String,
    onBackToMenuClick: () -> Unit,
    onSaveGameClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val gameRepository = remember { GameRepository(context) }
    
    // Verificar si hay un estado cargado en el GameStateManager
    val loadedGameState = remember { GameStateManager.currentGameState }
    
    // Si hay un estado cargado y coincide con la dificultad actual, usarlo
    val initialGameState = remember {
        if (loadedGameState != null && loadedGameState.difficulty == difficulty) {
            Log.d("GameScreen", "Usando estado cargado: ${loadedGameState.difficulty}, Pares: ${loadedGameState.matchedPairs}/${loadedGameState.totalPairs}")
            // Limpiar el estado cargado para futuras partidas
            val state = loadedGameState
            GameStateManager.currentGameState = null
            state
        } else {
            // Configurar un nuevo juego
            val (rows, columns) = when (difficulty) {
                "easy" -> Pair(3, 4)
                "medium" -> Pair(4, 4)
                "hard" -> Pair(4, 5)
                else -> Pair(3, 4)
            }
            
            val totalPairs = (rows * columns) / 2
            GameState(difficulty, totalPairs)
        }
    }
    
    // Usar initialGameState para configurar el estado del juego
    var gameState by remember { mutableStateOf(initialGameState) }
    
    val (rows, columns) = when (difficulty) {
        "easy" -> Pair(3, 4)
        "medium" -> Pair(4, 4)
        "hard" -> Pair(4, 5)
        else -> Pair(3, 4)
    }
    
    val totalPairs = (rows * columns) / 2
    
    // Usar el estado de las cartas del gameState si está cargado, o crear nuevas
    var cards by remember { 
        mutableStateOf(
            if (gameState.cards.isNotEmpty()) gameState.cards 
            else createCards(totalPairs)
        ) 
    }
    var flippedCards by remember { mutableStateOf<List<Card>>(emptyList()) }
    var matchedPairs by remember { mutableStateOf(gameState.matchedPairs) }
    var moves by remember { mutableStateOf(gameState.moves) }
    var showGameOverDialog by remember { mutableStateOf(gameState.isGameOver) }
    
    // Add time tracking variables
    var timeElapsed by remember { mutableStateOf(gameState.timeElapsed) }
    var isTimeAttackMode by remember { mutableStateOf(gameState.gameMode == "timeAttack") }
    
    // Calculate score based on difficulty, moves and matched pairs
    val score = remember(matchedPairs, moves, difficulty, timeElapsed, isTimeAttackMode) {
        if (isTimeAttackMode) {
            // Time attack scoring
            val pairsBonus = matchedPairs * 100
            val timeBonus = if (timeElapsed < 30000) ((30000 - timeElapsed) / 100).toInt() else 0
            pairsBonus + timeBonus
        } else {
            // Regular scoring
            val baseScore = matchedPairs * 100
            val movePenalty = moves * 5
            val difficultyMultiplier = when(difficulty) {
                "easy" -> 1.0
                "medium" -> 1.5
                "hard" -> 2.0
                else -> 1.0
            }
            ((baseScore - movePenalty) * difficultyMultiplier.toInt()).coerceAtLeast(0)
        }
    }
    
    // Actualizar el estado del juego cuando cambian los valores relevantes
    LaunchedEffect(cards, matchedPairs, moves, timeElapsed, isTimeAttackMode, score) {
        gameState.cards = cards
        gameState.matchedPairs = matchedPairs
        gameState.moves = moves
        gameState.timeElapsed = timeElapsed
        gameState.gameMode = if (isTimeAttackMode) "timeAttack" else "classic"
        gameState.score = score
    }
    
    // Start timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            if (!showGameOverDialog) {
                timeElapsed += 100
                
                // Check time limit for time attack mode
                if (isTimeAttackMode && timeElapsed >= 30000 && !showGameOverDialog) {
                    showGameOverDialog = true
                }
            }
        }
    }
    
    // Check for game completion
    if (matchedPairs == totalPairs && totalPairs > 0) {
        LaunchedEffect(matchedPairs) {
            showGameOverDialog = true
        }
    }
    
    // Handle card matching logic
    LaunchedEffect(flippedCards) {
        if (flippedCards.size == 2) {
            delay(1000) // Delay to show the cards before checking match
            
            val (first, second) = flippedCards
            if (first.pairId == second.pairId) {
                // Match found
                cards = cards.map {
                    if (it.id == first.id || it.id == second.id) {
                        it.copy(isMatched = true)
                    } else {
                        it
                    }
                }
                matchedPairs++
            }
            
            // Flip back unmatched cards
            cards = cards.map {
                if (!it.isMatched) {
                    it.copy(isFlipped = false)
                } else {
                    it
                }
            }
            
            flippedCards = emptyList()
            moves++
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memorama - ${difficulty.capitalize()}") },
                navigationIcon = {
                    IconButton(onClick = onBackToMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Volver al Menú"
                        )
                    }
                },
                actions = {
                    Text(
                        text = "Movimientos: $moves",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pares encontrados: $matchedPairs/$totalPairs",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Display time for time attack mode
            if (isTimeAttackMode) {
                val remainingTime = maxOf(30000 - timeElapsed, 0L)
                Text(
                    text = "Tiempo restante: ${remainingTime / 1000} segundos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (remainingTime < 10000) Color.Red else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cards) { card ->
                    MemoryCard(
                        card = card,
                        onCardClick = {
                            if (flippedCards.size < 2 && !card.isFlipped && !card.isMatched) {
                                // Flip the card
                                cards = cards.map {
                                    if (it.id == card.id) {
                                        it.copy(isFlipped = true)
                                    } else {
                                        it
                                    }
                                }
                                
                                // Add to flipped cards
                                flippedCards = flippedCards + cards.first { it.id == card.id }
                            }
                        }
                    )
                }
            }
            
            Button(
                onClick = {
                    // Reset game
                    cards = createCards(totalPairs)
                    flippedCards = emptyList()
                    matchedPairs = 0
                    moves = 0
                    timeElapsed = 0L
                },
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text("Reiniciar Juego")
            }
            
            // Botón para activar/desactivar el modo contrarreloj
            Button(
                onClick = { 
                    // Toggle time attack mode
                    isTimeAttackMode = !isTimeAttackMode
                    
                    // Reset game when changing mode
                    cards = createCards(totalPairs)
                    flippedCards = emptyList()
                    matchedPairs = 0
                    moves = 0
                    timeElapsed = 0L
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(if (isTimeAttackMode) "Modo Normal" else stringResource(R.string.mode_timed))
            }
            
            // Modificar el botón de guardar/cargar
            Button(
                onClick = {
                    // Actualizar el estado del juego antes de guardar
                    gameState.cards = cards
                    gameState.matchedPairs = matchedPairs
                    gameState.moves = moves
                    gameState.timeElapsed = timeElapsed
                    gameState.gameMode = if (isTimeAttackMode) "timeAttack" else "classic"
                    gameState.score = score
                    gameState.isGameOver = showGameOverDialog
                    
                    // Guardar en el singleton
                    GameStateManager.currentGameState = gameState
                    
                    // Llamar al callback de navegación
                    onSaveGameClick()
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(stringResource(R.string.save_load_game))
            }
        }
        
        if (showGameOverDialog) {
            // Game completed dialog
            AlertDialog(
                onDismissRequest = { /* Do nothing */ },
                title = { 
                    Text(text = if (isTimeAttackMode && timeElapsed >= 30000 && matchedPairs < totalPairs)
                        "¡Tiempo agotado!" 
                    else 
                        stringResource(R.string.game_completed_title)) 
                },
                text = { 
                    Column {
                        if (isTimeAttackMode && timeElapsed >= 30000 && matchedPairs < totalPairs) {
                            Text(text = "Encontraste $matchedPairs de $totalPairs pares")
                        } else {
                            Text(text = stringResource(R.string.game_completed_message, moves))
                        }
                        Text(text = stringResource(R.string.score, score))
                        Text(
                            text = stringResource(
                                R.string.high_score, 
                                gameRepository.getHighScore(difficulty)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = onBackToMenuClick) {
                        Text(text = stringResource(R.string.back_to_menu))
                    }
                },
                dismissButton = {
                    Button(onClick = { 
                        // Reset game
                        cards = createCards(totalPairs)
                        flippedCards = emptyList()
                        matchedPairs = 0
                        moves = 0
                        timeElapsed = 0L
                        showGameOverDialog = false
                        
                    }) {
                        Text(text = stringResource(R.string.play_again))
                    }
                }
            )
            
            // Save high score when game is completed
            LaunchedEffect(showGameOverDialog) {
                if (showGameOverDialog) {
                    gameRepository.saveHighScore(difficulty, score)
                }
            }
        }
    }
}

@Composable
fun MemoryCard(
    card: Card,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(0.7f)
            .padding(4.dp)
            .clickable(
                enabled = !card.isFlipped && !card.isMatched,
                onClick = onCardClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isFlipped || card.isMatched) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.secondary
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (card.isMatched) 
                MaterialTheme.colorScheme.primary 
            else 
                Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (card.isFlipped || card.isMatched) {
                Text(
                    text = "${card.pairId}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun createCards(totalPairs: Int): List<Card> {
    val pairValues = List(totalPairs) { it + 1 }
    val allCards = mutableListOf<Card>()
    
    // Create pairs of cards
    pairValues.forEachIndexed { index, pairId ->
        allCards.add(Card(id = index * 2, pairId = pairId))
        allCards.add(Card(id = index * 2 + 1, pairId = pairId))
    }
    
    // Shuffle the cards
    return allCards.shuffled()
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase() else it.toString() 
    }
}