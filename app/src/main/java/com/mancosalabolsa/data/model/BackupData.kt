package com.mancosalabolsa.data.model

import com.mancosalabolsa.data.entity.Exclusion
import com.mancosalabolsa.data.entity.Historial
import com.mancosalabolsa.data.entity.Jugador

data class BackupData(
    val jugadores: List<Jugador>,
    val exclusiones: List<Exclusion>,
    val historial: List<Historial>,
    val version: Int = 2
)
