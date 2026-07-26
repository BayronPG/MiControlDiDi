package com.jhon.micontroldidi.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.util.CurrencyFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_titulo)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("stats_contenido"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector de periodo
            PeriodoStatsSelector(
                periodoActual = state.periodoSeleccionado,
                onPeriodoSeleccionado = viewModel::seleccionarPeriodo
            )

            if (state.cargando) {
                Text(
                    text = stringResource(R.string.cargando),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag("stats_cargando")
                )
                return@Column
            }

            // Resumen periodo actual
            StatsResumenCard(
                titulo = stringResource(R.string.stats_periodo_actual),
                ingresos = state.ingresosActual,
                gastos = state.gastosActual,
                ganancia = state.gananciaNetaActual,
                viajes = state.cantidadViajesActual,
                testTag = "stats_card_actual"
            )

            // Resumen periodo anterior
            StatsResumenCard(
                titulo = stringResource(R.string.stats_periodo_anterior),
                ingresos = state.ingresosAnterior,
                gastos = state.gastosAnterior,
                ganancia = state.gananciaNetaAnterior,
                viajes = 0,
                testTag = "stats_card_anterior"
            )

            // Diferencia
            StatsDiferenciaCard(
                diferenciaIngresos = state.diferenciaIngresos,
                diferenciaGastos = state.diferenciaGastos,
                diferenciaGanancia = state.diferenciaGanancia,
                testTag = "stats_card_diferencia"
            )
        }
    }
}

@Composable
private fun PeriodoStatsSelector(
    periodoActual: StatsPeriodo,
    onPeriodoSeleccionado: (StatsPeriodo) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stats_selector_periodo"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatsPeriodo.entries.forEach { periodo ->
            val seleccionado = periodo == periodoActual
            val label = when (periodo) {
                StatsPeriodo.HOY -> stringResource(R.string.stats_hoy)
                StatsPeriodo.SEMANA -> stringResource(R.string.stats_semana)
                StatsPeriodo.MES -> stringResource(R.string.stats_mes)
            }

            FilterChip(
                selected = seleccionado,
                onClick = { onPeriodoSeleccionado(periodo) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("chip_stats_${periodo.name}")
            )
        }
    }
}

@Composable
private fun StatsResumenCard(
    titulo: String,
    ingresos: Long,
    gastos: Long,
    ganancia: Long,
    viajes: Int,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            StatsRow(stringResource(R.string.stats_ingresos), CurrencyFormatter.format(ingresos), MaterialTheme.colorScheme.primary)
            StatsRow(stringResource(R.string.stats_gastos), CurrencyFormatter.format(gastos), MaterialTheme.colorScheme.secondary)
            StatsRow(
                stringResource(R.string.stats_ganancia),
                CurrencyFormatter.format(ganancia),
                when {
                    ganancia > 0 -> MaterialTheme.colorScheme.primary
                    ganancia < 0 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (testTag == "stats_card_actual") {
                StatsRow(stringResource(R.string.stats_viajes), viajes.toString(), MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatsDiferenciaCard(
    diferenciaIngresos: Long,
    diferenciaGastos: Long,
    diferenciaGanancia: Long,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.stats_diferencia),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.height(8.dp))
            DiferenciaRow(stringResource(R.string.stats_ingresos), diferenciaIngresos)
            DiferenciaRow(stringResource(R.string.stats_gastos), diferenciaGastos)
            DiferenciaRow(stringResource(R.string.stats_ganancia), diferenciaGanancia)
        }
    }
}

@Composable
private fun StatsRow(label: String, valor: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(text = valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun DiferenciaRow(label: String, diferencia: Long) {
    val signo = if (diferencia >= 0) "+" else ""
    val color = when {
        diferencia > 0 -> MaterialTheme.colorScheme.primary
        diferencia < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
        Text(
            text = "$signo${CurrencyFormatter.format(diferencia)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
