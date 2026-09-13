package com.jhon.micontroldidi.ui.tanqueo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.local.entity.TanqueoEntity
import com.jhon.micontroldidi.util.CurrencyFormatter
import com.jhon.micontroldidi.util.DateFormatter

/**
 * Lista de tanqueos con acceso a registrar, editar y eliminar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaTanqueosScreen(
    viewModel: TanqueoViewModel,
    onNavegarARegistrar: () -> Unit,
    onNavegarAEditar: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tanqueos_titulo)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavegarARegistrar,
                modifier = Modifier.testTag("boton_registrar_tanqueo")
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.tanqueo_nuevo))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.cargando -> Text(
                    text = stringResource(R.string.cargando),
                    modifier = Modifier.padding(16.dp).testTag("cargando_tanqueos")
                )

                state.mensajeError != null -> Text(
                    text = state.mensajeError!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp).testTag("estado_error_tanqueos")
                )

                state.tanqueos.isEmpty() -> Text(
                    text = stringResource(R.string.tanqueos_sin_resultados),
                    modifier = Modifier.padding(16.dp).testTag("estado_vacio_tanqueos")
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("lista_tanqueos"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.tanqueos, key = { it.id }) { tanqueo ->
                        TanqueoCard(
                            tanqueo = tanqueo,
                            onEditar = { onNavegarAEditar(tanqueo.id) },
                            onEliminar = { viewModel.mostrarDialogoEliminar(tanqueo.id) }
                        )
                    }
                }
            }
        }
    }

    if (state.tanqueoIdAEliminar != null) {
        AlertDialog(
            onDismissRequest = { viewModel.ocultarDialogoEliminar() },
            title = { Text(stringResource(R.string.eliminar)) },
            text = { Text(stringResource(R.string.confirmar_eliminacion)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmarEliminacion() },
                    modifier = Modifier.testTag("boton_confirmar_eliminar_tanqueo")
                ) {
                    Text(stringResource(R.string.eliminar))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.ocultarDialogoEliminar() }) {
                    Text(stringResource(R.string.cancelar))
                }
            },
            modifier = Modifier.testTag("dialogo_confirmar_eliminar_tanqueo")
        )
    }
}

@Composable
private fun TanqueoCard(
    tanqueo: TanqueoEntity,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = DateFormatter.format(tanqueo.fechaHora),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(tanqueo.importePagado),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "${tanqueo.litros} L · " +
                    "${CurrencyFormatter.format(tanqueo.precioLitro)} / L · " +
                    stringResource(
                        if (tanqueo.esLleno) R.string.tanqueo_lleno else R.string.tanqueo_parcial
                    ),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("detalle_tanqueo_${tanqueo.id}")
            )

            Text(
                text = "${stringResource(R.string.odometro_tanqueo)} ${tanqueo.odometroMetros / 1000}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEditar,
                    modifier = Modifier.testTag("boton_editar_tanqueo_${tanqueo.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.editar),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.testTag("boton_eliminar_tanqueo_${tanqueo.id}")
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
