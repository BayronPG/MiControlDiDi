package com.jhon.micontroldidi.ui.viaje

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarViajeScreen(
    viewModel: ViajeViewModel,
    onGuardadoExitoso: () -> Unit,
    onCancelar: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Navegar atrás cuando el guardado sea exitoso
    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            viewModel.limpiarEstadoTransitorio()
            onGuardadoExitoso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.registrar_viaje)) },
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
        ) {
            Spacer(Modifier.height(16.dp))

            // Fecha y hora (solo informativa)
            Text(
                text = DateFormatter.format(System.currentTimeMillis()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            // Valor del viaje
            OutlinedTextField(
                value = state.valorText,
                onValueChange = { viewModel.actualizarValor(it) },
                label = { Text(stringResource(R.string.valor_viaje)) },
                isError = state.errorValor != null,
                supportingText = state.errorValor?.let { error ->
                    { Text(text = error, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("campo_valor_viaje")
            )

            Spacer(Modifier.height(12.dp))

            // Propina
            OutlinedTextField(
                value = state.propinaText,
                onValueChange = { viewModel.actualizarPropina(it) },
                label = { Text(stringResource(R.string.propina_opcional)) },
                isError = state.errorPropina != null,
                supportingText = state.errorPropina?.let { error ->
                    { Text(text = error, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("campo_propina_viaje")
            )

            Spacer(Modifier.height(12.dp))

            // Observación
            OutlinedTextField(
                value = state.observacionText,
                onValueChange = { viewModel.actualizarObservacion(it) },
                label = { Text(stringResource(R.string.observacion_opcional)) },
                minLines = 2,
                maxLines = 4,
                singleLine = false,
                modifier = Modifier.fillMaxWidth().testTag("campo_observacion_viaje")
            )

            Spacer(Modifier.height(24.dp))

            // Botón Guardar
            Button(
                onClick = { viewModel.guardarViaje() },
                enabled = state.formularioValido,
                modifier = Modifier.fillMaxWidth().testTag("boton_guardar_viaje")
            ) {
                if (state.guardando) {
                    Text(stringResource(R.string.guardando))
                } else {
                    Text(stringResource(R.string.guardar))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Botón Cancelar
            OutlinedButton(
                onClick = onCancelar,
                enabled = !state.guardando,
                modifier = Modifier.fillMaxWidth().testTag("boton_cancelar_viaje")
            ) {
                Text(stringResource(R.string.cancelar))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
