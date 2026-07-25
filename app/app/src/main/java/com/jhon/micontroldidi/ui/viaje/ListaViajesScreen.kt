package com.jhon.micontroldidi.ui.viaje

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.util.CurrencyFormatter
import com.jhon.micontroldidi.util.DateFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaViajesScreen(
    viewModel: ViajeViewModel,
    onNavegarARegistrar: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.viajes_titulo))
                        if (state.filtroActivo) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.viajes_filtro_activo),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleSelectorFecha() },
                        modifier = Modifier.testTag("boton_abrir_filtro")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.viajes_filtrar)
                        )
                    }
                    if (state.filtroActivo) {
                        IconButton(
                            onClick = { viewModel.limpiarFiltro() },
                            modifier = Modifier.testTag("boton_limpiar_filtro")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.viajes_limpiar_filtro)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (state.filtroActivo)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = if (state.filtroActivo)
                        MaterialTheme.colorScheme.onTertiaryContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavegarARegistrar,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("boton_registrar_viaje")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.registrar_viaje)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Selector de fechas
            if (state.mostrarSelectorFecha) {
                SelectorFechaContent(
                    filtroInicio = state.filtroInicio,
                    filtroFin = state.filtroFin,
                    onAplicar = { inicio, fin ->
                        viewModel.aplicarFiltroFecha(inicio, fin)
                    },
                    onCancelar = { viewModel.toggleSelectorFecha() }
                )
            }

            // Contenido principal
            when {
                state.cargando -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.cargando),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                state.mensajeFiltroVacio != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("estado_vacio_filtro"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.mensajeFiltroVacio!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                state.viajes.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("estado_vacio_viajes"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.sin_viajes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("lista_viajes"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.viajes,
                            key = { it.id }
                        ) { viaje ->
                            ViajeCard(viaje = viaje)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFechaContent(
    filtroInicio: Long?,
    filtroFin: Long?,
    onAplicar: (inicio: Long, fin: Long) -> Unit,
    onCancelar: () -> Unit
) {
    val zona = ZoneId.systemDefault()

    val inicioPickerState = rememberDatePickerState(
        initialSelectedDateMillis = filtroInicio ?: inicioDelDia(zona)
    )
    val finPickerState = rememberDatePickerState(
        initialSelectedDateMillis = filtroFin ?: finDelDia(zona)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("selector_fecha_container"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.viajes_fecha_inicio),
            style = MaterialTheme.typography.labelMedium
        )
        DatePicker(
            state = inicioPickerState,
            modifier = Modifier.testTag("date_picker_inicio"),
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Text(
            text = stringResource(R.string.viajes_fecha_fin),
            style = MaterialTheme.typography.labelMedium
        )
        DatePicker(
            state = finPickerState,
            modifier = Modifier.testTag("date_picker_fin"),
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancelar) {
                Text(stringResource(R.string.cancelar))
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = {
                    val inicioMs = inicioPickerState.selectedDateMillis
                    val finMs = finPickerState.selectedDateMillis
                    if (inicioMs != null && finMs != null) {
                        val inicio = inicioDelDia(zona, inicioMs)
                        val fin = inicioDelDia(zona, finMs) + 86_400_000L // +1 día
                        onAplicar(inicio, fin)
                    }
                },
                modifier = Modifier.testTag("boton_aplicar_filtro")
            ) {
                Text(stringResource(R.string.viajes_aplicar_filtro))
            }
        }
    }
}

/** Devuelve el timestamp de las 00:00:00.000 del día que contiene [epochMs]. */
private fun inicioDelDia(zona: ZoneId, epochMs: Long = System.currentTimeMillis()): Long {
    val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMs), zona)
    return zdt.toLocalDate().atStartOfDay(zona).toInstant().toEpochMilli()
}

/** Devuelve el timestamp de las 23:59:59.999 del día que contiene [epochMs]. */
@Suppress("UNUSED_PRIVATE_PARAMETER")
private fun finDelDia(zona: ZoneId, epochMs: Long = System.currentTimeMillis()): Long {
    val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMs), zona)
    return zdt.toLocalDate().plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli() - 1
}

@Composable
private fun ViajeCard(viaje: ViajeEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateFormatter.format(viaje.fechaHora),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(viaje.ingresoTotal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.valor_viaje)}: ${CurrencyFormatter.format(viaje.valor)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (viaje.propina > 0) {
                    Text(
                        text = "${stringResource(R.string.propina)}: ${CurrencyFormatter.format(viaje.propina)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (viaje.observacion.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = viaje.observacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
