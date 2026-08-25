package com.mancosalabolsa.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial")
data class Historial(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fecha: Long,
    val resultado: String
)
