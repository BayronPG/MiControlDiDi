package com.jhon.micontroldidi.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.jhon.micontroldidi.domain.PeriodoDashboard
import com.jhon.micontroldidi.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavegarARegistrarViaje: () -> Unit = {},
    onNavegarARegistrarGasto: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    DashboardScreen(
        uiState = state,
        onPeriodoSeleccionado = viewModel::seleccionarPeriodo,
        onNavegarARegistrarViaje = onNavegarARegistrarViaje,
        onNavegarARegistrarGasto = onNavegarARegistrarGasto
    )
}

/**
 * Overload que recibe el estado directamente, útil para pruebas Compose
 * sin dependencia del ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onPeriodoSeleccionado: (PeriodoDashboard) -> Unit,
    onNavegarARegistrarViaje: () -> Unit = {},
    onNavegarARegistrarGasto: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_titulo)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag("dashboard_fab_container")
            ) {
                FloatingActionButton(
                    onClick = onNavegarARegistrarViaje,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("fab_registrar_viaje_dashboard")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = stringResource(R.string.registrar_viaje)
                    )
                }
                FloatingActionButton(
                    onClick = onNavegarARegistrarGasto,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.testTag("fab_registrar_gasto_dashboard")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.registrar_gasto)
                    )
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.cargando),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            uiState.mensajeError != null -> {
                ErrorContent(
                    mensaje = uiState.mensajeError!!,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            else -> {
                DashboardContent(
                    state = uiState,
                    onPeriodoSeleccionado = onPeriodoSeleccionado,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    mensaje: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("dashboard_error"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onPeriodoSeleccionado: (PeriodoDashboard) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("dashboard_contenido"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PeriodoSelector(
            periodoActual = state.periodoSeleccionado,
            onPeriodoSeleccionado = onPeriodoSeleccionado
        )

        ResumenCard(
            titulo = stringResource(R.string.dashboard_ingresos),
            valor = state.ingresos,
            color = MaterialTheme.colorScheme.primary,
            testTag = "dashboard_tarjeta_ingresos"
        )

        ResumenCard(
            titulo = stringResource(R.string.dashboard_gastos),
            valor = state.gastos,
            color = MaterialTheme.colorScheme.secondary,
            testTag = "dashboard_tarjeta_gastos"
        )

        ResumenCard(
            titulo = stringResource(R.string.dashboard_ganancia_neta),
            valor = state.gananciaNeta,
            color = when {
                state.gananciaNeta > 0 -> MaterialTheme.colorScheme.primary
                state.gananciaNeta < 0 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            testTag = "dashboard_tarjeta_ganancia"
        )

        if (state.ingresos == 0L && state.gastos == 0L) {
            Text(
                text = stringResource(R.string.dashboard_sin_datos),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("dashboard_sin_datos")
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PeriodoSelector(
    periodoActual: PeriodoDashboard,
    onPeriodoSeleccionado: (PeriodoDashboard) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_selector_periodo"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PeriodoDashboard.entries.forEach { periodo ->
            val seleccionado = periodo == periodoActual
            val label = when (periodo) {
                PeriodoDashboard.DIA -> stringResource(R.string.dashboard_periodo_dia)
                PeriodoDashboard.SEMANA -> stringResource(R.string.dashboard_periodo_semana)
                PeriodoDashboard.MES -> stringResource(R.string.dashboard_periodo_mes)
            }

            FilterChip(
                selected = seleccionado,
                onClick = { onPeriodoSeleccionado(periodo) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("chip_periodo_${periodo.name}")
            )
        }
    }
}

@Composable
private fun ResumenCard(
    titulo: String,
    valor: Long,
    color: Color,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = CurrencyFormatter.format(valor),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
