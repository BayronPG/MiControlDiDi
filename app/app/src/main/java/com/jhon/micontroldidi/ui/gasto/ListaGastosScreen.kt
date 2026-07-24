package com.jhon.micontroldidi.ui.gasto

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.jhon.micontroldidi.data.local.entity.GastoConCategoria
import com.jhon.micontroldidi.util.CurrencyFormatter
import com.jhon.micontroldidi.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
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
            text = { Text(stringResource(R.string.eliminacion_no_reversible)) },
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
        if (state.cargando) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.cargando), style = MaterialTheme.typography.bodyLarge)
            }
        } else if (state.gastos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).testTag("estado_vacio_gastos"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.sin_gastos),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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

@Composable
private fun GastoCard(
    gasto: GastoConCategoria,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("item_gasto_${gasto.id}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = gasto.nombreCategoria,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = CurrencyFormatter.format(gasto.valor),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = DateFormatter.format(gasto.fechaHora),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (gasto.descripcion.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = gasto.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(
                    onClick = onEditar,
                    modifier = Modifier
                        .testTag("boton_editar_gasto_${gasto.id}")
                        .testTag("boton_editar_gasto")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.editar),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier
                        .testTag("boton_eliminar_gasto_${gasto.id}")
                        .testTag("boton_eliminar_gasto")
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
