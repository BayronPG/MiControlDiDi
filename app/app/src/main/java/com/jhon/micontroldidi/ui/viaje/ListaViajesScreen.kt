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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.util.CalculadorRangoFiltro
import com.jhon.micontroldidi.util.CurrencyFormatter
import com.jhon.micontroldidi.util.DateFormatter
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaViajesScreen(
    state: ViajeUiState,
    onNavegarARegistrar: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.viajes_titulo)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
        ListaViajesContent(
            state = state,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun ListaViajesContent(
    state: ViajeUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        when {
            state.mensajeError != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .testTag("estado_error_viajes"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.mensajeError!!,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            state.cargando -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                    items(items = state.viajes, key = { it.id }) { viaje ->
                        ViajeCard(
                            viaje = viaje,
                            onEditar = {},
                            onEliminar = {}
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaViajesScreen(
    viewModel: ViajeViewModel,
    onNavegarARegistrar: () -> Unit,
    onNavegarAEditar: (Long) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    if (state.viajeIdAEliminar != null) {
        AlertDialog(
            onDismissRequest = {
                if (!state.eliminando) viewModel.ocultarDialogoEliminar()
            },
            title = { Text(stringResource(R.string.confirmar_eliminacion_viaje)) },
            text = {
                Column {
                    Text(stringResource(R.string.eliminacion_no_reversible))
                    if (state.errorEliminacion != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = state.errorEliminacion!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmarEliminacion() },
                    enabled = !state.eliminando,
                    modifier = Modifier.testTag("boton_confirmar_eliminar_viaje")
                ) {
                    Text(
                        if (state.eliminando) stringResource(R.string.guardando)
                        else stringResource(R.string.si_eliminar),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.ocultarDialogoEliminar() },
                    enabled = !state.eliminando,
                    modifier = Modifier.testTag("boton_cancelar_eliminar_viaje")
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            },
            modifier = Modifier.testTag("dialogo_confirmar_eliminar_viaje")
        )
    }

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
                state.mensajeError != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .testTag("estado_error_viajes"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.mensajeError!!,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                state.cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                            ViajeCard(
                                viaje = viaje,
                                onEditar = { onNavegarAEditar(viaje.id) },
                                onEliminar = { viewModel.mostrarDialogoEliminar(viaje.id) }
                            )
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
    val errorRangoInvalido = stringResource(R.string.error_filtro_rango_invalido)

    // El DatePicker de Material 3 expone la fecha seleccionada como medianoche UTC.
    val inicioPickerState = rememberDatePickerState(
        initialSelectedDateMillis = filtroInicio?.let { CalculadorRangoFiltro.aUtcMedianoche(it, zona) }
            ?: CalculadorRangoFiltro.aUtcMedianoche(System.currentTimeMillis(), zona)
    )
    val finPickerState = rememberDatePickerState(
        initialSelectedDateMillis = filtroFin?.let { CalculadorRangoFiltro.aUtcMedianoche(it, zona) }
            ?: CalculadorRangoFiltro.aUtcMedianoche(System.currentTimeMillis(), zona)
    )

    var errorRango by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
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

        errorRango?.let { mensajeRangoError ->
            Text(
                text = mensajeRangoError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

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
                        val rango = CalculadorRangoFiltro.rangoFiltro(inicioMs, finMs, zona)
                        if (rango != null) {
                            errorRango = null
                            onAplicar(rango.inicioInclusivo, rango.finExclusivo)
                        } else {
                            errorRango = errorRangoInvalido
                        }
                    }
                },
                modifier = Modifier.testTag("boton_aplicar_filtro")
            ) {
                Text(stringResource(R.string.viajes_aplicar_filtro))
            }
        }
    }
}

@Composable
private fun detallesDePlataforma(viaje: ViajeEntity): String {
    val partes = mutableListOf<String>()
    if (viaje.plataforma.isNotBlank()) partes.add(viaje.plataforma.trim())
    if (viaje.zona.isNotBlank()) partes.add(viaje.zona.trim())
    if (viaje.distanciaMetros > 0) {
        partes.add(stringResource(R.string.distancia_km_viaje, viaje.distanciaKm))
    }
    return partes.joinToString(" · ")
}

@Composable
private fun ViajeCard(
    viaje: ViajeEntity,
    onEditar: () -> Unit = {},
    onEliminar: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = DateFormatter.format(viaje.fechaHora),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (viaje.observacion.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = viaje.observacion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    val detalles = detallesDePlataforma(viaje)
                    if (detalles.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = detalles,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("detalles_plataforma_viaje")
                        )
                    }
                }
                Text(
                    text = CurrencyFormatter.format(viaje.ingresoTotal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.valor_viaje)} ${CurrencyFormatter.format(viaje.valor)}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (viaje.propina > 0) {
                    Text(
                        text = "${stringResource(R.string.propina)} ${CurrencyFormatter.format(viaje.propina)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEditar,
                    modifier = Modifier.testTag("boton_editar_viaje_${viaje.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.editar),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.testTag("boton_eliminar_viaje_${viaje.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.eliminar),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
