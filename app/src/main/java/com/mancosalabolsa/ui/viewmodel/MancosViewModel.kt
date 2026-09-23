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
import kotlin.math.abs

class MancosViewModel(private val repository: MancosRepository) : ViewModel() {

    // Lista de todos los jugadores
    val allJugadores: StateFlow<List<Jugador>> = repository.allJugadores.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    // Lista de jugadores activos para el sorteo
    val jugadoresActivos: StateFlow<List<Jugador>> = repository.jugadoresActivos.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    // Lista de exclusiones
    val allExclusiones: StateFlow<List<Exclusion>> = repository.allExclusiones.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    // Historial
    val historial: StateFlow<List<com.mancosalabolsa.data.entity.Historial>> = repository.historial.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    // ID de jugadores seleccionados para el sorteo
    private val _selectedPlayerIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedPlayerIds = _selectedPlayerIds.asStateFlow()

    // Lista de jugadores ordenados (seleccionados arriba, alfabético en ambos grupos)
    val jugadoresOrdenados: StateFlow<List<Jugador>> = combine(
        jugadoresActivos,
        _selectedPlayerIds
    ) { activos, seleccionados ->
        val (conPresencia, sinPresencia) = activos.partition { seleccionados.contains(it.id) }
        conPresencia.sortedBy { it.nombre } + sinPresencia.sortedBy { it.nombre }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Resultado del sorteo
    private val _resultadoSorteo = MutableStateFlow<String?>(null)
    val resultadoSorteo = _resultadoSorteo.asStateFlow()

    // Error del sorteo
    private val _errorSorteo = MutableStateFlow<String?>(null)
    val errorSorteo = _errorSorteo.asStateFlow()

    // Flag de inconsistencia de posiciones
    private val _inconsistenciaPosiciones = MutableStateFlow<Boolean>(false)
    val inconsistenciaPosiciones = _inconsistenciaPosiciones.asStateFlow()

    // Estado de "Sorteando" (para animación)
    private val _sorteando = MutableStateFlow(false)
    val sorteando = _sorteando.asStateFlow()

    // Filtros dinámicos de sorteo
    private val _respetarExclusiones = MutableStateFlow(true)
    val respetarExclusiones = _respetarExclusiones.asStateFlow()

    private val _nivelarPosiciones = MutableStateFlow(true)
    val nivelarPosiciones = _nivelarPosiciones.asStateFlow()

    private val _nivelarCategorias = MutableStateFlow(true)
    val nivelarCategorias = _nivelarCategorias.asStateFlow()

    fun toggleRespetarExclusiones(value: Boolean) { _respetarExclusiones.value = value }
    fun toggleNivelarPosiciones(value: Boolean) { _nivelarPosiciones.value = value }
    fun toggleNivelarCategorias(value: Boolean) { _nivelarCategorias.value = value }

    fun togglePlayerSelection(playerId: Int) {
        val current = _selectedPlayerIds.value
        if (current.contains(playerId)) {
            _selectedPlayerIds.value = current - playerId
        } else {
            _selectedPlayerIds.value = current + playerId
        }
    }

    fun realizarSorteo() {
        viewModelScope.launch {
            val presentes = jugadoresActivos.value.filter { 
                _selectedPlayerIds.value.contains(it.id)
            }
            val exclusiones = allExclusiones.value

            if (presentes.size < 2) {
                _errorSorteo.value = "Selecciona al menos 2 jugadores"
                return@launch
            }

            _sorteando.value = true
            kotlinx.coroutines.delay(2000) // 2 segundos de animación
            _sorteando.value = false

            // Filtros dinámicos elegidos por el usuario
            val configRespetarExcl = _respetarExclusiones.value
            val configNivelarPos = _nivelarPosiciones.value
            val configNivelarCat = _nivelarCategorias.value

            // Intento 1: Obedece al 100% las preferencias elegidas por el usuario
            var resultado = intentarSorteo(
                jugadores = presentes, 
                exclusiones = exclusiones, 
                aplicarExclusiones = configRespetarExcl,
                forzarPosiciones = configNivelarPos, 
                filtrarCategoria = configNivelarCat
            )
            
            if (resultado != null) {
                _resultadoSorteo.value = resultado
                _inconsistenciaPosiciones.value = false
                _errorSorteo.value = null
                saveResult(resultado)
            } else {
                // Si el intento ideal con las preferencias del usuario falla por rigidez matemática,
                // empezamos a relajar las condiciones secundarias automáticamente para no trabar el sorteo,
                // excepto aquellas que el usuario apagó explícitamente (si ya estaban apagadas no tiene sentido).
                
                // Intento 2: Relajar categorías (siempre que el usuario las haya querido nivelar originalmente)
                if (configNivelarCat) {
                    resultado = intentarSorteo(
                        jugadores = presentes, 
                        exclusiones = exclusiones, 
                        aplicarExclusiones = configRespetarExcl,
                        forzarPosiciones = configNivelarPos, 
                        filtrarCategoria = false
                    )
                }
                
                if (resultado != null) {
                    _resultadoSorteo.value = resultado
                    _inconsistenciaPosiciones.value = false
                    _errorSorteo.value = null
                    saveResult(resultado)
                } else {
                    // Intento 3: Permitir forzar posiciones cruzadas (D-D, R-R) si la matemática sigue trabada
                    resultado = intentarSorteo(
                        jugadores = presentes, 
                        exclusiones = exclusiones, 
                        aplicarExclusiones = configRespetarExcl,
                        forzarPosiciones = false, 
                        filtrarCategoria = false
                    )
                    if (resultado != null) {
                        _resultadoSorteo.value = resultado
                        _inconsistenciaPosiciones.value = configNivelarPos // Solo marca aviso si el usuario quería nivelarlas
                        _errorSorteo.value = null
                        saveResult(resultado)
                    } else {
                        _errorSorteo.value = "No se encontró una combinación válida (revisa las exclusiones)"
                        _resultadoSorteo.value = null
                    }
                }
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
        aplicarExclusiones: Boolean,
        forzarPosiciones: Boolean,
        filtrarCategoria: Boolean
    ): String? {
        val totalIntentos = 2000
        val candidatosMejores = mutableListOf<String>()
        var menorDesvioTotal = Int.MAX_VALUE
        
        repeat(totalIntentos) {
            val shuffled = jugadores.shuffled()
            var esValido = true
            val listaParejas = mutableListOf<List<Jugador>>()
            var desvioSorteoActual = 0
            
            var i = 0
            while (i < shuffled.size) {
                if (i + 1 < shuffled.size) {
                    val p1 = shuffled[i]
                    val p2 = shuffled[i + 1]
                    
                    // Regla de Exclusiones (Condicionada dinámicamente)
                    if (aplicarExclusiones && estanExcluidos(p1, p2, exclusiones)) {
                        esValido = false
                        break
                    }

                    // Regla de Posiciones (Condicionada dinámicamente)
                    if (forzarPosiciones) {
                        if ((p1.posicion == "D" && p2.posicion == "D") || 
                            (p1.posicion == "R" && p2.posicion == "R")) {
                            esValido = false
                            break
                        }
                    }

                    // Regla de Categorías (Condicionada dinámicamente)
                    val sumaCat = p1.categoria + p2.categoria
                    if (filtrarCategoria) {
                        if (sumaCat < 14 || sumaCat > 16) {
                            esValido = false
                            break
                        }
                    }
                    
                    // Si nivelar categorías está activado, calculamos desvío hacia 15.
                    // Si está desactivado por el usuario, el desvío es siempre 0 (puro azar).
                    desvioSorteoActual += if (filtrarCategoria || _nivelarCategorias.value) {
                        abs(sumaCat - 15)
                    } else {
                        0
                    }

                    listaParejas.add(listOf(p1, p2))
                    i += 2
                } else {
                    listaParejas.add(listOf(shuffled[i]))
                    i += 1
                }
            }

            if (esValido) {
                if (desvioSorteoActual < menorDesvioTotal) {
                    // Encontramos un desvío estrictamente mejor: limpiamos los anteriores
                    menorDesvioTotal = desvioSorteoActual
                    candidatosMejores.clear()
                    candidatosMejores.add(formatResultado(listaParejas))
                } else if (desvioSorteoActual == menorDesvioTotal) {
                    // Encontramos otra combinación con la misma calidad de desvío: la sumamos como candidata
                    candidatosMejores.add(formatResultado(listaParejas))
                }
            }
        }
        // Retornamos uno al azar de entre todos los mejores candidatos encontrados
        return candidatosMejores.randomOrNull()
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
                "${index + 1}. ${p[0].nombre} (${p[0].posicion}) [C${p[0].categoria}] - (Suelto/a)"
            } else {
                val p1 = p[0]
                val p2 = p[1]
                "${index + 1}. ${p1.nombre} (${p1.posicion}) [C${p1.categoria}] - ${p2.nombre} (${p2.posicion}) [C${p2.categoria}]"
            }
        }.joinToString("\n")
    }

    fun limpiarResultado() {
        _resultadoSorteo.value = null
        _errorSorteo.value = null
        _inconsistenciaPosiciones.value = false
    }

    // Métodos para gestión de jugadores
    fun addJugador(nombre: String, posicion: String = "X", categoria: Int = 7) {
        viewModelScope.launch {
            repository.insertJugador(Jugador(nombre = nombre, posicion = posicion, categoria = categoria))
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
