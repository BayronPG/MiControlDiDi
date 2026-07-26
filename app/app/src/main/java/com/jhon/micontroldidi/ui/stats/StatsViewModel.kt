package com.jhon.micontroldidi.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.repository.GastoRepository
import com.jhon.micontroldidi.data.repository.ViajeRepository
import com.jhon.micontroldidi.domain.CalculadorRangoPeriodo
import com.jhon.micontroldidi.domain.RangoPeriodo
import com.jhon.micontroldidi.util.ResourceProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val viajeRepository: ViajeRepository,
    private val gastoRepository: GastoRepository,
    private val clock: Clock,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _periodo = MutableStateFlow(StatsPeriodo.HOY)
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _periodo
                .flatMapLatest { periodo ->
                    val periodoDashboard = StatsUiState.statsPeriodoToDashboard(periodo)
                    val ahora = clock.millis()
                    val zoneId = clock.zone
                    val rangoActual = CalculadorRangoPeriodo.calcular(periodoDashboard, ahora, zoneId)
                    val rangoAnterior = calcularRangoAnterior(periodo, rangoActual, zoneId)

                    kotlinx.coroutines.flow.combine(
                        viajeRepository.obtenerIngresosPorRango(rangoActual.inicioInclusivo, rangoActual.finExclusivo),
                        gastoRepository.obtenerTotalGastosPorRango(rangoActual.inicioInclusivo, rangoActual.finExclusivo),
                        viajeRepository.obtenerPorRango(rangoActual.inicioInclusivo, rangoActual.finExclusivo).map { it.size },
                        viajeRepository.obtenerIngresosPorRango(rangoAnterior.inicioInclusivo, rangoAnterior.finExclusivo),
                        gastoRepository.obtenerTotalGastosPorRango(rangoAnterior.inicioInclusivo, rangoAnterior.finExclusivo)
                    ) { ingAct, gasAct, cantViajes, ingAnt, gasAnt ->
                        val ganAct = ingAct - gasAct
                        val ganAnt = ingAnt - gasAnt
                        StatsUiState(
                            periodoSeleccionado = periodo,
                            cargando = false,
                            ingresosActual = ingAct,
                            gastosActual = gasAct,
                            gananciaNetaActual = ganAct,
                            cantidadViajesActual = cantViajes,
                            cantidadGastosActual = 0,
                            ingresosAnterior = ingAnt,
                            gastosAnterior = gasAnt,
                            gananciaNetaAnterior = ganAnt
                        )
                    }
                    .catch { e ->
                        if (e is CancellationException) throw e
                        emit(
                            StatsUiState(
                                periodoSeleccionado = periodo,
                                cargando = false,
                                mensajeError = resourceProvider.getString(R.string.stats_error)
                            )
                        )
                    }
                }
                .collect { nuevoEstado ->
                    _uiState.value = nuevoEstado
                }
        }
    }

    fun seleccionarPeriodo(periodo: StatsPeriodo) {
        _periodo.value = periodo
    }

    private fun calcularRangoAnterior(
        periodo: StatsPeriodo,
        rangoActual: RangoPeriodo,
        zoneId: ZoneId
    ): RangoPeriodo {
        val duracion = rangoActual.finExclusivo - rangoActual.inicioInclusivo
        return RangoPeriodo(
            inicioInclusivo = rangoActual.inicioInclusivo - duracion,
            finExclusivo = rangoActual.inicioInclusivo
        )
    }

    class Factory(
        private val viajeRepository: ViajeRepository,
        private val gastoRepository: GastoRepository,
        private val resourceProvider: ResourceProvider,
        private val clock: Clock = Clock.systemDefaultZone()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatsViewModel(viajeRepository, gastoRepository, clock, resourceProvider) as T
        }
    }
}
