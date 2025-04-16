package com.escom.practica4_1.data

import com.escom.practica4_1.model.GameState
/**
 * Singleton para gestionar el estado del juego entre pantallas
 */
object GameStateManager {
    var currentGameState: GameState? = null
}