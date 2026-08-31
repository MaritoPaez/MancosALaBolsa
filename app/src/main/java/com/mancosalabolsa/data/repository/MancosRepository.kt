package com.mancosalabolsa.data.repository

import com.mancosalabolsa.data.dao.MancosDao
import com.mancosalabolsa.data.entity.Exclusion
import com.mancosalabolsa.data.entity.Historial
import com.mancosalabolsa.data.entity.Jugador
import com.mancosalabolsa.data.model.BackupData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MancosRepository(private val mancosDao: MancosDao) {

    val jugadoresActivos: Flow<List<Jugador>> = mancosDao.getJugadoresActivos()
    val allJugadores: Flow<List<Jugador>> = mancosDao.getAllJugadores()
    val allExclusiones: Flow<List<Exclusion>> = mancosDao.getAllExclusiones()
    val historial: Flow<List<Historial>> = mancosDao.getHistorial()

    suspend fun insertJugador(jugador: Jugador) {
        mancosDao.insertJugador(jugador)
    }

    suspend fun updateJugador(jugador: Jugador) {
        mancosDao.updateJugador(jugador)
    }

    suspend fun deleteJugador(jugador: Jugador) {
        mancosDao.deleteJugador(jugador)
    }

    suspend fun addExclusion(exclusion: Exclusion) {
        mancosDao.insertExclusion(exclusion)
    }

    suspend fun removeExclusion(idA: Int, idB: Int) {
        mancosDao.deleteExclusion(idA, idB)
    }

    suspend fun saveSorteo(resultado: String) {
        val entry = Historial(
            fecha = System.currentTimeMillis(),
            resultado = resultado
        )
        mancosDao.insertHistorial(entry)
    }

    suspend fun clearHistorial() {
        mancosDao.clearHistorial()
    }

    suspend fun clearAllData() {
        mancosDao.clearAllExclusiones()
        mancosDao.clearAllJugadores()
        mancosDao.clearHistorial()
    }

    suspend fun getBackupData(): BackupData {
        return BackupData(
            jugadores = mancosDao.getAllJugadores().first(),
            exclusiones = mancosDao.getAllExclusiones().first(),
            historial = mancosDao.getHistorial().first()
        )
    }

    suspend fun restoreBackup(data: BackupData) {
        mancosDao.clearAllExclusiones()
        mancosDao.clearAllJugadores()
        mancosDao.clearHistorial()
        
        mancosDao.insertJugadores(data.jugadores)
        mancosDao.insertExclusiones(data.exclusiones)
        mancosDao.insertHistoriales(data.historial)
    }
}
