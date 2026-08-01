package com.jhon.micontroldidi.ui.gasto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria
import com.jhon.micontroldidi.util.CurrencyFormatter
import com.jhon.micontroldidi.util.DateFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListaGastosScreen(
    viewModel: GastoViewModel,
    onNavegarARegistrar: () -> Unit,
    onNavegarAEditar: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.gastoIdAEliminar != null) {
        AlertDialog(
            onDismissRequest = {
                if (!state.eliminando) viewModel.ocultarDialogoEliminar()
            },
            title = { Text(stringResource(R.string.confirmar_eliminacion)) },
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
                    modifier = Modifier.testTag("boton_confirmar_eliminar")
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
                    modifier = Modifier.testTag("boton_cancelar_eliminar")
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            },
            modifier = Modifier.testTag("dialogo_confirmar_eliminar")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.gastos_titulo))
                        if (state.filtroActivo) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.gastos_filtro_activo),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleSelectorFecha() },
                        modifier = Modifier.testTag("boton_abrir_filtro_gastos")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.gastos_filtrar)
                        )
                    }
                    if (state.filtroActivo) {
                        IconButton(
                            onClick = { viewModel.limpiarFiltro() },
                            modifier = Modifier.testTag("boton_limpiar_filtro_gastos")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.gastos_limpiar_filtro)
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
                modifier = Modifier.testTag("boton_registrar_gasto")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.registrar_gasto)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Selector de fechas y categoría
            if (state.mostrarSelectorFecha) {
                FiltroGastosContent(
                    categorias = state.categorias,
                    filtroInicio = state.filtroInicio,
                    filtroFin = state.filtroFin,
                    filtroCategoriaId = state.filtroCategoriaId,
                    onAplicar = { inicio, fin, catId ->
                        viewModel.aplicarFiltro(inicio, fin, catId)
                    },
                    onCancelar = { viewModel.toggleSelectorFecha() }
                )
            }

            // Contenido principal
            when {
                state.mensajeErrorCarga != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .testTag("estado_error_gastos"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.mensajeErrorCarga!!,
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
                            .testTag("estado_vacio_filtro_gastos"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.mensajeFiltroVacio!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                state.gastos.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("estado_vacio_gastos"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.sin_gastos),
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
                            .testTag("lista_gastos"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = state.gastos, key = { it.id }) { gasto ->
                            GastoCard(
                                gasto = gasto,
                                onEditar = { onNavegarAEditar(gasto.id) },
                                onEliminar = { viewModel.mostrarDialogoEliminar(gasto.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FiltroGastosContent(
    categorias: List<com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity>,
    filtroInicio: Long?,
    filtroFin: Long?,
    filtroCategoriaId: Long?,
    onAplicar: (inicio: Long, fin: Long, categoriaId: Long?) -> Unit,
    onCancelar: () -> Unit
) {
    val zona = ZoneId.systemDefault()

    val inicioPickerState = rememberDatePickerState(
        initialSelectedDateMillis = filtroInicio ?: inicioDelDia(zona)
    )
    val finPickerState = rememberDatePickerState(
        initialSelectedDateMillis = filtroFin ?: finDelDia(zona)
    )

    var categoriaSeleccionadaId by remember { mutableStateOf(filtroCategoriaId) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("selector_filtro_gastos_container"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.gastos_fecha_inicio),
            style = MaterialTheme.typography.labelMedium
        )
        DatePicker(
            state = inicioPickerState,
            modifier = Modifier.testTag("date_picker_inicio_gastos"),
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Text(
            text = stringResource(R.string.gastos_fecha_fin),
            style = MaterialTheme.typography.labelMedium
        )
        DatePicker(
            state = finPickerState,
            modifier = Modifier.testTag("date_picker_fin_gastos"),
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Text(
            text = stringResource(R.string.categoria),
            style = MaterialTheme.typography.labelMedium
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth().testTag("chips_categoria_filtro"),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = categoriaSeleccionadaId == null,
                onClick = { categoriaSeleccionadaId = null },
                label = { Text(stringResource(R.string.gastos_todas_categorias)) },
                modifier = Modifier.testTag("chip_todas_categorias")
            )
            categorias.forEach { cat ->
                FilterChip(
                    selected = categoriaSeleccionadaId == cat.id,
                    onClick = { categoriaSeleccionadaId = cat.id },
                    label = { Text(cat.nombre) },
                    modifier = Modifier.testTag("chip_categoria_${cat.id}")
                )
            }
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
                        val inicio = inicioDelDia(zona, inicioMs)
                        val fin = inicioDelDia(zona, finMs) + 86_400_000L
                        onAplicar(inicio, fin, categoriaSeleccionadaId)
                    }
                },
                modifier = Modifier.testTag("boton_aplicar_filtro_gastos")
            ) {
                Text(stringResource(R.string.gastos_aplicar_filtro))
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
@Suppress("unused")
private fun finDelDia(zona: ZoneId, epochMs: Long = System.currentTimeMillis()): Long {
    val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMs), zona)
    return zdt.toLocalDate().plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli() - 1
}

@Composable
private fun GastoCard(
    gasto: GastoConCategoria,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("item_gasto_${gasto.id}"),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = gasto.nombreCategoria,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = DateFormatter.format(gasto.fechaHora),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = CurrencyFormatter.format(gasto.valor),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (gasto.descripcion.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = gasto.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEditar,
                    modifier = Modifier.testTag("boton_editar_gasto_${gasto.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.editar),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.testTag("boton_eliminar_gasto_${gasto.id}")
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

/**
 * Overload de ListaGastosScreen que recibe el estado directamente,
 * �til para pruebas Compose sin dependencia del ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListaGastosScreen(
    state: GastoUiState,
    onNavegarARegistrar: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.gastos_titulo)) },
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
                modifier = Modifier.testTag("boton_registrar_gasto")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.registrar_gasto)
                )
            }
        }
    ) { innerPadding ->
        ListaGastosContent(
            state = state,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun ListaGastosContent(
    state: GastoUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        when {
            state.mensajeErrorCarga != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .testTag("estado_error_gastos"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.mensajeErrorCarga!!,
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
                        .testTag("estado_vacio_filtro_gastos"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.mensajeFiltroVacio!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            state.gastos.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("estado_vacio_gastos"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.sin_gastos),
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
                        .testTag("lista_gastos"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = state.gastos, key = { it.id }) { gasto ->
                        GastoCard(
                            gasto = gasto,
                            onEditar = {},
                            onEliminar = {}
                        )
                    }
                }
            }
        }
    }
}

