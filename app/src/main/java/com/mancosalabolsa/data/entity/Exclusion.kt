package com.mancosalabolsa.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exclusiones",
    foreignKeys = [
        ForeignKey(
            entity = Jugador::class,
            parentColumns = ["id"],
            childColumns = ["jugadorAId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Jugador::class,
            parentColumns = ["id"],
            childColumns = ["jugadorBId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("jugadorAId"), Index("jugadorBId")]
)
data class Exclusion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val jugadorAId: Int,
    val jugadorBId: Int
)
