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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.domain.FormaPago
import com.jhon.micontroldidi.util.CalculadorFechaFormularioViaje
import com.jhon.micontroldidi.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarViajeScreen(
    viewModel: ViajeViewModel,
    viajeId: Long? = null,
    onGuardadoExitoso: () -> Unit,
    onCancelar: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var menuFormaPagoAbierto by remember { mutableStateOf(false) }

    // Cargar datos para edición si se proporciona un ID
    LaunchedEffect(viajeId) {
        if (viajeId != null) {
            viewModel.cargarViajeParaEditar(viajeId)
        }
    }

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

            // Fecha y hora (solo informativa):
            // en edición se muestra la del viaje y no la actual.
            val fechaInformativa = CalculadorFechaFormularioViaje.fechaVisible(
                esEdicion = viajeId != null,
                editando = state.editando,
                fechaHoraOriginal = state.fechaHoraOriginal,
                ahora = System.currentTimeMillis()
            )
            if (fechaInformativa != null) {
                Text(
                    text = DateFormatter.format(fechaInformativa),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("fecha_viaje")
                )
            }

            Spacer(Modifier.height(20.dp))

            // Valor del viaje
            OutlinedTextField(
                value = state.valorText,
                onValueChange = { viewModel.actualizarValor(it) },
                label = { Text(stringResource(R.string.valor_viaje)) },
                isError = state.errorValor != null,
                supportingText = state.errorValor?.let { error ->
                    { Text(text = error) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campo_valor_viaje")
            )

            Spacer(Modifier.height(16.dp))

            // Propina
            OutlinedTextField(
                value = state.propinaText,
                onValueChange = { viewModel.actualizarPropina(it) },
                label = { Text(stringResource(R.string.propina_opcional)) },
                isError = state.errorPropina != null,
                supportingText = state.errorPropina?.let { error ->
                    { Text(text = error) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campo_propina_viaje")
            )

            Spacer(Modifier.height(16.dp))

            // Observación
            OutlinedTextField(
                value = state.observacionText,
                onValueChange = { viewModel.actualizarObservacion(it) },
                label = { Text(stringResource(R.string.observacion_opcional)) },
                minLines = 3,
                maxLines = 5,
                singleLine = false,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campo_observacion_viaje")
            )

            Spacer(Modifier.height(16.dp))

            // Plataforma (prellenada desde el perfil de trabajo)
            OutlinedTextField(
                value = state.plataformaText,
                onValueChange = { viewModel.actualizarPlataforma(it) },
                label = { Text(stringResource(R.string.plataforma_viaje)) },
                isError = state.errorPlataforma != null,
                supportingText = state.errorPlataforma?.let { error ->
                    { Text(text = error) }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campo_plataforma_viaje")
            )

            Spacer(Modifier.height(16.dp))

            // Zona
            OutlinedTextField(
                value = state.zonaText,
                onValueChange = { viewModel.actualizarZona(it) },
                label = { Text(stringResource(R.string.zona_viaje)) },
                isError = state.errorZona != null,
                supportingText = state.errorZona?.let { error ->
                    { Text(text = error) }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campo_zona_viaje")
            )

            Spacer(Modifier.height(16.dp))

            // Distancia del viaje en kilómetros
            OutlinedTextField(
                value = state.distanciaText,
                onValueChange = { viewModel.actualizarDistancia(it) },
                label = { Text(stringResource(R.string.distancia_viaje)) },
                isError = state.errorDistancia != null,
                supportingText = state.errorDistancia?.let { error ->
                    { Text(text = error) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campo_distancia_viaje")
            )

            Spacer(Modifier.height(16.dp))

            // Forma de pago (catálogo cerrado)
            ExposedDropdownMenuBox(
                expanded = menuFormaPagoAbierto,
                onExpandedChange = { menuFormaPagoAbierto = !menuFormaPagoAbierto },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selector_forma_pago_viaje")
            ) {
                OutlinedTextField(
                    value = state.formaPago?.nombre.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.forma_pago_viaje)) },
                    isError = state.errorFormaPago != null,
                    supportingText = state.errorFormaPago?.let { error ->
                        { Text(text = error) }
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuFormaPagoAbierto)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = menuFormaPagoAbierto,
                    onDismissRequest = { menuFormaPagoAbierto = false }
                ) {
                    FormaPago.entries.forEach { forma ->
                        DropdownMenuItem(
                            text = { Text(forma.nombre) },
                            onClick = {
                                viewModel.seleccionarFormaPago(forma)
                                menuFormaPagoAbierto = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Peaje (dato informativo del viaje)
            OutlinedTextField(
                value = state.peajeText,
                onValueChange = { viewModel.actualizarPeaje(it) },
                label = { Text(stringResource(R.string.peaje_viaje)) },
                isError = state.errorPeaje != null,
                supportingText = state.errorPeaje?.let { error ->
                    { Text(text = error) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campo_peaje_viaje")
            )

            Spacer(Modifier.height(28.dp))

            // Error de guardado (no es validación de campo)
            if (state.errorGuardado != null) {
                Text(
                    text = state.errorGuardado!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("error_guardado_viaje")
                )
                Spacer(Modifier.height(8.dp))
            }

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
