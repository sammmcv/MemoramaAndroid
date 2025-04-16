package com.escom.practica4_1.model

data class Card(
    val id: Int,
    val pairId: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)