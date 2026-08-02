package com.jhon.micontroldidi.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.domain.PeriodoDashboard
import com.jhon.micontroldidi.domain.PeriodoMeta
import com.jhon.micontroldidi.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavegarARegistrarViaje: () -> Unit = {},
    onNavegarARegistrarGasto: () -> Unit = {},
    onNavegarAConfigurarMeta: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    DashboardScreen(
        uiState = state,
        onPeriodoSeleccionado = viewModel::seleccionarPeriodo,
        onNavegarARegistrarViaje = onNavegarARegistrarViaje,
        onNavegarARegistrarGasto = onNavegarARegistrarGasto,
        onNavegarAConfigurarMeta = onNavegarAConfigurarMeta
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
    onNavegarARegistrarGasto: () -> Unit = {},
    onNavegarAConfigurarMeta: () -> Unit = {}
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
        floatingActionButton = { }
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
                    onNavegarARegistrarViaje = onNavegarARegistrarViaje,
                    onNavegarARegistrarGasto = onNavegarARegistrarGasto,
                    onNavegarAConfigurarMeta = onNavegarAConfigurarMeta,
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
    onNavegarARegistrarViaje: () -> Unit = {},
    onNavegarARegistrarGasto: () -> Unit = {},
    onNavegarAConfigurarMeta: () -> Unit = {},
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

        GananciaHeroCard(
            valor = state.gananciaNeta,
            testTag = "dashboard_tarjeta_ganancia"
        )

        ResumenCard(
            titulo = stringResource(R.string.dashboard_ingresos),
            valor = state.ingresos,
            colorContenido = MaterialTheme.colorScheme.tertiary,
            icono = Icons.Filled.KeyboardArrowUp,
            testTag = "dashboard_tarjeta_ingresos"
        )

        ResumenCard(
            titulo = stringResource(R.string.dashboard_gastos),
            valor = state.gastos,
            colorContenido = MaterialTheme.colorScheme.error,
            icono = Icons.Filled.KeyboardArrowDown,
            testTag = "dashboard_tarjeta_gastos"
        )

        // Progreso de meta activa
        if (state.metaActiva != null && state.progresoMeta != null) {
            MetaProgressCard(
                metaActiva = state.metaActiva!!,
                progreso = state.progresoMeta!!,
                onConfigurar = onNavegarAConfigurarMeta,
                modifier = Modifier.testTag("dashboard_tarjeta_meta")
            )
        }

        Button(
            onClick = onNavegarARegistrarViaje,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_dashboard_viaje")
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.registrar_viaje))
        }

        Button(
            onClick = onNavegarARegistrarGasto,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_dashboard_gasto")
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.registrar_gasto))
        }

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
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = seleccionado,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                    borderWidth = 1.dp
                ),
                modifier = Modifier.testTag("chip_periodo_${periodo.name}")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetaProgressCard(
    metaActiva: com.jhon.micontroldidi.data.local.entity.MetaEntity,
    progreso: Float,
    onConfigurar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val porcentaje = (progreso * 100).toInt().coerceAtLeast(0)
    val metaCumplida = progreso >= 1.0f

    val textoPeriodo = if (metaActiva.periodo == PeriodoMeta.DIA)
        stringResource(R.string.meta_diaria)
    else
        stringResource(R.string.meta_mensual)

    // Familia azul: tertiary queda reservado para ingresos.
    val containerColor = if (metaCumplida)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface
    val contentColor = if (metaCumplida)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (metaCumplida) null
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.meta_progreso_titulo),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                IconButton(
                    onClick = onConfigurar,
                    modifier = Modifier.testTag("boton_configurar_meta")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.meta_configurar),
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = CurrencyFormatter.format(metaActiva.valorObjetivo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )

            Text(
                text = textoPeriodo,
                style = MaterialTheme.typography.bodySmall,
                color = if (metaCumplida)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progreso.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .testTag("barra_progreso_meta"),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (metaCumplida)
                    stringResource(R.string.meta_cumplida)
                else
                    stringResource(R.string.meta_progreso_porcentaje, porcentaje),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (metaCumplida)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("texto_progreso_meta")
            )
        }
    }
}

/**
 * Tarjeta hero de ganancia neta con tres estados visuales:
 * positiva (azul), negativa (rojo) y cero (neutro).
 * No lleva icono: la jerarquía se apoya en contenedor, etiqueta y monto.
 */
@Composable
private fun GananciaHeroCard(
    valor: Long,
    testTag: String
) {
    val (containerColor, contentColor, border) = when {
        valor > 0 -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            null
        )
        valor < 0 -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            null
        )
        else -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurfaceVariant,
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = border
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.dashboard_ganancia_neta),
                style = MaterialTheme.typography.titleSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = CurrencyFormatter.format(valor),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ResumenCard(
    titulo: String,
    valor: Long,
    colorContenido: Color,
    icono: ImageVector?,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icono != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorContenido,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
            }
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = CurrencyFormatter.format(valor),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorContenido
                )
            }
        }
    }
}
