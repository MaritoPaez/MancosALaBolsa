package com.mancosalabolsa.data.dao

import androidx.room.*
import com.mancosalabolsa.data.entity.Exclusion
import com.mancosalabolsa.data.entity.Historial
import com.mancosalabolsa.data.entity.Jugador
import kotlinx.coroutines.flow.Flow

@Dao
interface MancosDao {
    // Jugadores
    @Query("SELECT * FROM jugadores WHERE activo = 1 ORDER BY nombre ASC")
    fun getJugadoresActivos(): Flow<List<Jugador>>

    @Query("SELECT * FROM jugadores ORDER BY nombre ASC")
    fun getAllJugadores(): Flow<List<Jugador>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJugador(jugador: Jugador)

    @Update
    suspend fun updateJugador(jugador: Jugador)

    // Exclusiones
    @Query("SELECT * FROM exclusiones")
    fun getAllExclusiones(): Flow<List<Exclusion>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExclusion(exclusion: Exclusion)

    @Query("DELETE FROM exclusiones WHERE (jugadorAId = :idA AND jugadorBId = :idB) OR (jugadorAId = :idB AND jugadorBId = :idA)")
    suspend fun deleteExclusion(idA: Int, idB: Int)

    // Historial
    @Query("SELECT * FROM historial ORDER BY fecha DESC")
    fun getHistorial(): Flow<List<Historial>>

    @Insert
    suspend fun insertHistorial(historial: Historial)

    @Query("DELETE FROM historial")
    suspend fun clearHistorial()

    @Query("DELETE FROM jugadores")
    suspend fun clearAllJugadores()

    @Query("DELETE FROM exclusiones")
    suspend fun clearAllExclusiones()
}
