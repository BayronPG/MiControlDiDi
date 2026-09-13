package com.jhon.micontroldidi.ui.tanqueo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R

/**
 * Formulario de tanqueo. Cada guardado mantiene sincronizado su gasto de
 * categoría Gasolina.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarTanqueoScreen(
    viewModel: TanqueoViewModel,
    tanqueoId: Long? = null,
    onGuardadoExitoso: () -> Unit,
    onCancelar: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(tanqueoId) {
        if (tanqueoId != null) {
            viewModel.cargarTanqueoParaEditar(tanqueoId)
        }
    }

    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            viewModel.limpiarEstadoTransitorio()
            onGuardadoExitoso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.editando) R.string.tanqueo_editar else R.string.tanqueo_nuevo
                        )
                    )
                },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("formulario_tanqueo")
        ) {
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.odometroText,
                onValueChange = { viewModel.actualizarOdometro(it) },
                label = { Text(stringResource(R.string.odometro_tanqueo)) },
                isError = state.errorOdometro != null,
                supportingText = state.errorOdometro?.let { error -> { Text(error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("campo_odometro_tanqueo")
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.litrosText,
                onValueChange = { viewModel.actualizarLitros(it) },
                label = { Text(stringResource(R.string.litros_tanqueo)) },
                isError = state.errorLitros != null,
                supportingText = state.errorLitros?.let { error -> { Text(error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("campo_litros_tanqueo")
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.importeText,
                onValueChange = { viewModel.actualizarImporte(it) },
                label = { Text(stringResource(R.string.importe_tanqueo)) },
                isError = state.errorImporte != null,
                supportingText = state.errorImporte?.let { error -> { Text(error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("campo_importe_tanqueo")
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.tipoCombustible,
                onValueChange = { viewModel.actualizarTipoCombustible(it) },
                label = { Text(stringResource(R.string.tipo_combustible_tanqueo)) },
                isError = state.errorTipoCombustible != null,
                supportingText = state.errorTipoCombustible?.let { error -> { Text(error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("campo_tipo_combustible_tanqueo")
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        if (state.esLleno) R.string.tanqueo_lleno else R.string.tanqueo_parcial
                    ),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.esLleno,
                    onCheckedChange = { viewModel.alternarLleno() },
                    modifier = Modifier.testTag("switch_tanqueo_lleno")
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.observacionText,
                onValueChange = { viewModel.actualizarObservacion(it) },
                label = { Text(stringResource(R.string.observacion_opcional)) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth().testTag("campo_observacion_tanqueo")
            )

            Spacer(Modifier.height(20.dp))

            if (state.errorGuardado != null) {
                Text(
                    text = state.errorGuardado!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("error_guardado_tanqueo")
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.guardar() },
                enabled = state.formularioValido,
                modifier = Modifier.fillMaxWidth().testTag("boton_guardar_tanqueo")
            ) {
                Text(stringResource(if (state.guardando) R.string.guardando else R.string.guardar))
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onCancelar,
                enabled = !state.guardando,
                modifier = Modifier.fillMaxWidth().testTag("boton_cancelar_tanqueo")
            ) {
                Text(stringResource(R.string.cancelar))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
