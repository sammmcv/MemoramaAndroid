package com.escom.practica4_1.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class GameState(
    val difficulty: String,
    val totalPairs: Int
) {
    var cards: List<Card> by mutableStateOf(emptyList())
    var matchedPairs: Int by mutableStateOf(0)
    var moves: Int by mutableStateOf(0)
    var timeElapsed: Long by mutableStateOf(0L)
    var gameMode: String by mutableStateOf("classic")
    var score: Int by mutableStateOf(0)
    var isGameOver: Boolean by mutableStateOf(false)
    var timeLimit: Long by mutableStateOf(30000L) // 30 segundos por defecto
    var isTimeUp: Boolean by mutableStateOf(false)
    
    // Método para calcular la puntuación
    private fun calculateScore(): Int {
        return if (gameMode == "timeAttack") {
            // Time attack scoring
            val pairsBonus = matchedPairs * 100
            val timeBonus = if (timeElapsed < timeLimit) ((timeLimit - timeElapsed) / 100).toInt() else 0
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
    
    fun updateScore() {
        score = calculateScore()
    }
}