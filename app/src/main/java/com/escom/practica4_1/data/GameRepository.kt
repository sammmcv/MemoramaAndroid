package com.escom.practica4_1.data

import android.content.Context
import android.content.SharedPreferences

class GameRepository(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("memorama_prefs", Context.MODE_PRIVATE)
    
    // Guardar puntuación alta
    fun saveHighScore(difficulty: String, score: Int) {
        val currentHighScore = getHighScore(difficulty)
        if (score > currentHighScore) {
            sharedPreferences.edit().putInt("high_score_$difficulty", score).apply()
        }
    }
    
    // Obtener puntuación alta
    fun getHighScore(difficulty: String): Int {
        return sharedPreferences.getInt("high_score_$difficulty", 0)
    }
    
    // Guardar progreso actual
    fun saveGameProgress(difficulty: String, cards: List<com.escom.practica4_1.model.Card>, moves: Int, time: Long, gameMode: String = "classic") {
        val editor = sharedPreferences.edit()
        editor.putInt("current_moves", moves)
        editor.putLong("current_time", time)
        editor.putString("current_difficulty", difficulty)
        editor.putString("current_game_mode", gameMode)
        
        // Guardar estado de las cartas
        val cardsState = cards.joinToString(",") { 
            "${it.id},${it.pairId},${it.isFlipped},${it.isMatched}" 
        }
        editor.putString("cards_state", cardsState)
        editor.apply()
    }
    
    // Comprobar si hay un juego guardado
    fun hasGameInProgress(): Boolean {
        return sharedPreferences.contains("cards_state")
    }
    
    // Obtener modo de juego guardado
    fun getSavedGameMode(): String {
        return sharedPreferences.getString("current_game_mode", "classic") ?: "classic"
    }
    
    // Limpiar juego guardado
    fun clearGameProgress() {
        sharedPreferences.edit()
            .remove("current_moves")
            .remove("current_time")
            .remove("current_difficulty")
            .remove("cards_state")
            .remove("current_game_mode")
            .apply()
    }
}