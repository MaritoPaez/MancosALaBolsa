package com.mancosalabolsa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancosalabolsa.data.entity.Exclusion
import com.mancosalabolsa.data.entity.Jugador
import com.mancosalabolsa.data.model.BackupData
import com.mancosalabolsa.data.repository.MancosRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MancosViewModel(private val repository: MancosRepository) : ViewModel() {

    // Lista de todos los jugadores
    val allJugadores: StateFlow<List<Jugador>> = repository.allJugadores.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Lista de jugadores activos para el sorteo
    val jugadoresActivos: StateFlow<List<Jugador>> = repository.jugadoresActivos.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Lista de exclusiones
    val allExclusiones: StateFlow<List<Exclusion>> = repository.allExclusiones.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Historial
    val historial: StateFlow<List<com.mancosalabolsa.data.entity.Historial>> = repository.historial.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // ID de jugadores seleccionados para el sorteo
    private val _selectedPlayerIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedPlayerIds = _selectedPlayerIds.asStateFlow()

    // Resultado del sorteo
    private val _resultadoSorteo = MutableStateFlow<String?>(null)
    val resultadoSorteo = _resultadoSorteo.asStateFlow()

    // Error del sorteo
    private val _errorSorteo = MutableStateFlow<String?>(null)
    val errorSorteo = _errorSorteo.asStateFlow()

    // Flag de inconsistencia de posiciones
    private val _inconsistenciaPosiciones = MutableStateFlow<Boolean>(false)
    val inconsistenciaPosiciones = _inconsistenciaPosiciones.asStateFlow()

    fun togglePlayerSelection(playerId: Int) {
        val current = _selectedPlayerIds.value
        if (current.contains(playerId)) {
            _selectedPlayerIds.value = current - playerId
        } else {
            _selectedPlayerIds.value = current + playerId
        }
    }

    fun realizarSorteo() {
        val presentes = allJugadores.value.filter { 
            _selectedPlayerIds.value.contains(it.id) && it.activo 
        }
        val exclusiones = allExclusiones.value

        if (presentes.size < 2) {
            _errorSorteo.value = "Selecciona al menos 2 jugadores"
            return
        }

        // Intento 1: Sorteo ideal (Sin D-D ni R-R)
        var resultado = intentarSorteo(presentes, exclusiones, permitirForzados = false)
        
        if (resultado != null) {
            _resultadoSorteo.value = resultado
            _inconsistenciaPosiciones.value = false
            _errorSorteo.value = null
            saveResult(resultado)
        } else {
            // Intento 2: Sorteo con inconsistencias (Permitiendo D-D o R-R)
            resultado = intentarSorteo(presentes, exclusiones, permitirForzados = true)
            if (resultado != null) {
                _resultadoSorteo.value = resultado
                _inconsistenciaPosiciones.value = true
                _errorSorteo.value = null
                saveResult(resultado)
            } else {
                _errorSorteo.value = "No se encontró una combinación válida (revisa las exclusiones)"
                _resultadoSorteo.value = null
            }
        }
    }

    private fun saveResult(resultado: String) {
        viewModelScope.launch {
            repository.saveSorteo(resultado)
        }
    }

    private fun intentarSorteo(
        jugadores: List<Jugador>, 
        exclusiones: List<Exclusion>, 
        permitirForzados: Boolean
    ): String? {
        val totalIntentos = 1000
        repeat(totalIntentos) {
            val shuffled = jugadores.shuffled()
            var esValido = true
            val listaParejas = mutableListOf<List<Jugador>>()
            
            var i = 0
            while (i < shuffled.size) {
                if (i + 1 < shuffled.size) {
                    val p1 = shuffled[i]
                    val p2 = shuffled[i + 1]
                    
                    // Regla de Exclusiones (Estricta)
                    if (estanExcluidos(p1, p2, exclusiones)) {
                        esValido = false
                        break
                    }

                    // Regla de Posiciones (Opcional según permitirForzados)
                    if (!permitirForzados) {
                        if ((p1.posicion == "D" && p2.posicion == "D") || 
                            (p1.posicion == "R" && p2.posicion == "R")) {
                            esValido = false
                            break
                        }
                    }

                    listaParejas.add(listOf(p1, p2))
                    i += 2
                } else {
                    listaParejas.add(listOf(shuffled[i]))
                    i += 1
                }
            }

            if (esValido) {
                return formatResultado(listaParejas)
            }
        }
        return null
    }

    private fun estanExcluidos(j1: Jugador, j2: Jugador, exclusiones: List<Exclusion>): Boolean {
        return exclusiones.any { 
            (it.jugadorAId == j1.id && it.jugadorBId == j2.id) || 
            (it.jugadorAId == j2.id && it.jugadorBId == j1.id) 
        }
    }

    private fun formatResultado(parejas: List<List<Jugador>>): String {
        return parejas.mapIndexed { index, p ->
            if (p.size == 1) {
                "${index + 1}. ${p[0].nombre} (${p[0].posicion}) - (Suelto/a)"
            } else {
                val p1 = p[0]
                val p2 = p[1]
                "${index + 1}. ${p1.nombre} (${p1.posicion}) - ${p2.nombre} (${p2.posicion})"
            }
        }.joinToString("\n")
    }

    fun limpiarResultado() {
        _resultadoSorteo.value = null
        _errorSorteo.value = null
        _inconsistenciaPosiciones.value = false
    }

    // Métodos para gestión de jugadores
    fun addJugador(nombre: String, posicion: String = "X") {
        viewModelScope.launch {
            repository.insertJugador(Jugador(nombre = nombre, posicion = posicion))
        }
    }

    fun updateJugador(jugador: Jugador) {
        viewModelScope.launch {
            repository.updateJugador(jugador)
        }
    }

    fun deleteJugador(jugador: Jugador) {
        viewModelScope.launch {
            repository.deleteJugador(jugador)
        }
    }

    // Métodos para exclusiones
    fun addExclusion(idA: Int, idB: Int) {
        viewModelScope.launch {
            repository.addExclusion(Exclusion(jugadorAId = idA, jugadorBId = idB))
        }
    }

    fun removeExclusion(idA: Int, idB: Int) {
        viewModelScope.launch {
            repository.removeExclusion(idA, idB)
        }
    }

    fun borrarHistorial() {
        viewModelScope.launch {
            repository.clearHistorial()
        }
    }

    fun reiniciarApp() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun exportarDatos(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val data = repository.getBackupData()
            val json = Gson().toJson(data)
            onResult(json)
        }
    }

    fun importarDatos(json: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val data = Gson().fromJson(json, BackupData::class.java)
                if (data != null && data.jugadores.isNotEmpty()) {
                    repository.restoreBackup(data)
                    onSuccess()
                } else {
                    onError("El archivo no tiene el formato correcto o está vacío")
                }
            } catch (e: Exception) {
                onError("Error al procesar el archivo: ${e.message}")
            }
        }
    }
}

class MancosViewModelFactory(private val repository: MancosRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MancosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MancosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
