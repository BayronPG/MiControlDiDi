package com.jhon.micontroldidi.ui.gasto

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
import androidx.compose.material3.MenuAnchorType
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
import com.jhon.micontroldidi.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarGastoScreen(
    viewModel: GastoViewModel,
    onGuardadoExitoso: () -> Unit,
    onCancelar: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            viewModel.limpiarEstadoTransitorio()
            onGuardadoExitoso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.registrar_gasto)) },
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

            // Selector de categoría
            CategoriaSelector(
                categorias = state.categorias.map { it.nombre },
                categoriaSeleccionada = state.categorias.find { it.id == state.categoriaSeleccionadaId }?.nombre,
                error = state.errorCategoria,
                onCategoriaSeleccionada = { nombre ->
                    val cat = state.categorias.find { it.nombre == nombre }
                    viewModel.seleccionarCategoria(cat?.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selector_categoria_gasto")
            )

            Spacer(Modifier.height(12.dp))

            // Valor del gasto
            OutlinedTextField(
                value = state.valorText,
                onValueChange = { viewModel.actualizarValor(it) },
                label = { Text(stringResource(R.string.valor_gasto)) },
                isError = state.errorValor != null,
                supportingText = state.errorValor?.let { error ->
                    { Text(text = error, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("campo_valor_gasto")
            )

            Spacer(Modifier.height(12.dp))

            // Descripción (opcional)
            OutlinedTextField(
                value = state.descripcionText,
                onValueChange = { viewModel.actualizarDescripcion(it) },
                label = { Text(stringResource(R.string.descripcion_opcional)) },
                minLines = 2,
                maxLines = 4,
                singleLine = false,
                modifier = Modifier.fillMaxWidth().testTag("campo_descripcion_gasto")
            )

            // Error de guardado
            if (state.errorGuardado != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.errorGuardado!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(24.dp))

            // Botón Guardar
            Button(
                onClick = {
                    viewModel.limpiarErrorGuardado()
                    viewModel.guardarGasto()
                },
                enabled = state.formularioValido,
                modifier = Modifier.fillMaxWidth().testTag("boton_guardar_gasto")
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
                modifier = Modifier.fillMaxWidth().testTag("boton_cancelar_gasto")
            ) {
                Text(stringResource(R.string.cancelar))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriaSelector(
    categorias: List<String>,
    categoriaSeleccionada: String?,
    error: String?,
    onCategoriaSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = categoriaSeleccionada ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.categoria)) },
            isError = error != null,
            supportingText = error?.let { msg ->
                { Text(text = msg, color = MaterialTheme.colorScheme.error) }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categorias.forEach { nombre ->
                DropdownMenuItem(
                    text = { Text(nombre) },
                    onClick = {
                        onCategoriaSeleccionada(nombre)
                        expanded = false
                    }
                )
            }
        }
    }
}
