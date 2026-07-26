package com.jhon.micontroldidi.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.repository.GastoRepository
import com.jhon.micontroldidi.data.repository.MetaRepository
import com.jhon.micontroldidi.data.repository.ViajeRepository
import com.jhon.micontroldidi.domain.CalculadorRangoPeriodo
import com.jhon.micontroldidi.domain.PeriodoDashboard
import com.jhon.micontroldidi.util.ResourceProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val viajeRepository: ViajeRepository,
    private val gastoRepository: GastoRepository,
    private val metaRepository: MetaRepository,
    private val clock: Clock,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _periodo = MutableStateFlow(PeriodoDashboard.DIA)
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _periodo
                .flatMapLatest { periodo ->
                    val rango = CalculadorRangoPeriodo.calcular(
                        periodo = periodo,
                        fechaReferencia = clock.millis(),
                        zoneId = clock.zone
                    )
                    combine(
                        viajeRepository.obtenerIngresosPorRango(
                            rango.inicioInclusivo, rango.finExclusivo
                        ),
                        gastoRepository.obtenerTotalGastosPorRango(
                            rango.inicioInclusivo, rango.finExclusivo
                        ),
                        metaRepository.obtenerActiva()
                    ) { ingresos, gastos, meta ->
                        val gananciaNeta = ingresos - gastos
                        val progreso = if (meta != null && meta.valorObjetivo > 0L) {
                            gananciaNeta.toFloat() / meta.valorObjetivo.toFloat()
                        } else null

                        DashboardUiState(
                            periodoSeleccionado = periodo,
                            ingresos = ingresos,
                            gastos = gastos,
                            gananciaNeta = gananciaNeta,
                            cargando = false,
                            mensajeError = null,
                            metaActiva = meta,
                            progresoMeta = progreso
                        )
                    }.catch { e ->
                        if (e is CancellationException) throw e
                        emit(
                            DashboardUiState(
                                periodoSeleccionado = periodo,
                                cargando = false,
                                mensajeError = resourceProvider.getString(R.string.dashboard_error)
                            )
                        )
                    }
                }
                .collect { nuevoEstado ->
                    _uiState.value = nuevoEstado
                }
        }
    }

    /**
     * Cambia el periodo activo del Dashboard.
     * Al cambiar el periodo, [flatMapLatest] cancela la observación anterior
     * y suscribe las consultas agregadas al nuevo rango calculado.
     * Si el error fue transitorio, el nuevo periodo obtendrá datos frescos.
     */
    fun seleccionarPeriodo(periodo: PeriodoDashboard) {
        _periodo.value = periodo
    }

    class Factory(
        private val viajeRepository: ViajeRepository,
        private val gastoRepository: GastoRepository,
        private val metaRepository: MetaRepository,
        private val resourceProvider: ResourceProvider,
        private val clock: Clock = Clock.systemDefaultZone()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(viajeRepository, gastoRepository, metaRepository, clock, resourceProvider) as T
        }
    }
}
