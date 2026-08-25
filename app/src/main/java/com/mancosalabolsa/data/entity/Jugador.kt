package com.mancosalabolsa.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jugadores")
data class Jugador(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val activo: Boolean = true
)
