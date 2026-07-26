package com.jhon.micontroldidi.ui.meta

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarMetaScreen(
    viewModel: MetaViewModel,
    onNavegarAtras: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.meta_titulo)) },
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
        ) {
            if (state.cargando) {
                Text(
                    text = stringResource(R.string.cargando),
                    style = MaterialTheme.typography.bodyLarge
                )
                return@Column
            }

            // Meta activa existente
            val meta = state.metaActiva
            if (meta != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_meta_activa"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.meta_actual),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = CurrencyFormatter.format(meta.valorObjetivo),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = if (meta.tipoPeriodo == "DIA")
                                stringResource(R.string.meta_diaria)
                            else
                                stringResource(R.string.meta_mensual),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Selector de periodo
            Text(
                text = stringResource(R.string.meta_periodo),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.testTag("selector_periodo_meta")) {
                FilterChip(
                    selected = state.tipoPeriodo == "DIA",
                    onClick = { viewModel.seleccionarPeriodo("DIA") },
                    label = { Text(stringResource(R.string.meta_diaria)) },
                    modifier = Modifier.testTag("chip_meta_dia")
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = state.tipoPeriodo == "MES",
                    onClick = { viewModel.seleccionarPeriodo("MES") },
                    label = { Text(stringResource(R.string.meta_mensual)) },
                    modifier = Modifier.testTag("chip_meta_mes")
                )
            }
            if (state.errorPeriodo != null) {
                Text(
                    text = state.errorPeriodo!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("error_periodo_meta")
                )
            }

            Spacer(Modifier.height(16.dp))

            // Campo de valor objetivo
            OutlinedTextField(
                value = state.valorText,
                onValueChange = { viewModel.actualizarValor(it) },
                label = { Text(stringResource(R.string.meta_valor_objetivo)) },
                isError = state.errorValor != null,
                supportingText = state.errorValor?.let { error ->
                    { Text(text = error, modifier = Modifier.testTag("error_valor_meta")) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campo_valor_meta"),
                enabled = !state.guardando
            )

            Spacer(Modifier.height(24.dp))

            // Mensaje de error
            if (state.mensajeError != null) {
                Text(
                    text = state.mensajeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("error_guardado_meta")
                )
                Spacer(Modifier.height(8.dp))
            }

            // Botones
            Button(
                onClick = { viewModel.guardarMeta() },
                enabled = state.formularioValido && !state.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("boton_guardar_meta")
            ) {
                Text(
                    if (state.guardando) stringResource(R.string.guardando)
                    else stringResource(R.string.guardar)
                )
            }

            if (state.metaActiva != null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.eliminarMeta() },
                    enabled = !state.guardando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("boton_eliminar_meta")
                ) {
                    Text(
                        stringResource(R.string.eliminar_meta),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onNavegarAtras,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("boton_volver_meta")
            ) {
                Text(stringResource(R.string.volver))
            }
        }
    }
}
