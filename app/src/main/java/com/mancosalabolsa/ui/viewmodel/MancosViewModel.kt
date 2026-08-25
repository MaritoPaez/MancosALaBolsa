package com.mancosalabolsa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mancosalabolsa.data.entity.Exclusion
import com.mancosalabolsa.data.entity.Jugador
import com.mancosalabolsa.data.repository.MancosRepository
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

    fun togglePlayerSelection(playerId: Int) {
        val current = _selectedPlayerIds.value
        if (current.contains(playerId)) {
            _selectedPlayerIds.value = current - playerId
        } else {
            _selectedPlayerIds.value = current + playerId
        }
    }

    fun realizarSorteo() {
        val presentes = allJugadores.value.filter { _selectedPlayerIds.value.contains(it.id) }
        val exclusiones = allExclusiones.value

        if (presentes.size < 2) {
            _errorSorteo.value = "Selecciona al menos 2 jugadores"
            return
        }

        val resultado = generarParejas(presentes, exclusiones)
        if (resultado != null) {
            _resultadoSorteo.value = resultado
            _errorSorteo.value = null
            viewModelScope.launch {
                repository.saveSorteo(resultado)
            }
        } else {
            _errorSorteo.value = "No se encontró una combinación válida con estas exclusiones"
            _resultadoSorteo.value = null
        }
    }

    private fun generarParejas(jugadores: List<Jugador>, exclusiones: List<Exclusion>): String? {
        val totalIntentos = 500
        repeat(totalIntentos) {
            val shuffled = jugadores.shuffled()
            val parejas = mutableListOf<String>()
            var esValido = true
            val usados = mutableSetOf<Int>()

            val listaParejas = mutableListOf<List<Jugador>>()
            
            var i = 0
            while (i < shuffled.size) {
                if (i + 1 < shuffled.size) {
                    val p1 = shuffled[i]
                    val p2 = shuffled[i + 1]
                    
                    if (estanExcluidos(p1, p2, exclusiones)) {
                        esValido = false
                        break
                    }
                    listaParejas.add(listOf(p1, p2))
                    i += 2
                } else {
                    // Último jugador impar (Suelto)
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
        return parejas.joinToString("\n") { p ->
            if (p.size == 1) {
                "${p[0].nombre} (Suelto/a)"
            } else {
                p.joinToString(" - ") { it.nombre }
            }
        }
    }

    fun limpiarResultado() {
        _resultadoSorteo.value = null
        _errorSorteo.value = null
    }

    // Métodos para gestión de jugadores
    fun addJugador(nombre: String) {
        viewModelScope.launch {
            repository.insertJugador(Jugador(nombre = nombre))
        }
    }

    fun updateJugador(jugador: Jugador) {
        viewModelScope.launch {
            repository.updateJugador(jugador)
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
