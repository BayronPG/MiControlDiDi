package com.jhon.micontroldidi.ui.gasto

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
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
    gastoId: Long?,
    onGuardadoExitoso: () -> Unit,
    onCancelar: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(gastoId) {
        if (gastoId != null && gastoId > 0) {
            viewModel.cargarGastoParaEditar(gastoId)
        }
    }

    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            viewModel.limpiarEstadoTransitorio()
            onGuardadoExitoso()
        }
    }

    val enCarga = state.modoFormulario == ModoFormulario.CARGANDO_EDICION
    val enError = state.modoFormulario == ModoFormulario.ERROR_EDICION
    val esEdicion = state.modoFormulario == ModoFormulario.EDICION

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (enError) ""
                        else if (esEdicion) stringResource(R.string.editar_gasto)
                        else stringResource(R.string.registrar_gasto)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        when {
            enCarga -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            enError -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(48.dp))
                    Text(
                        text = state.errorEdicion ?: "Gasto no encontrado",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = onCancelar,
                        modifier = Modifier.testTag("boton_cancelar_gasto")
                    ) {
                        Text(stringResource(R.string.cancelar))
                    }
                }
            }
            else -> {
                Column(Modifier.fillMaxSize().padding(innerPadding)) {
                    FormularioGasto(viewModel, state, onCancelar)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioGasto(
    viewModel: GastoViewModel,
    state: GastoUiState,
    onCancelar: () -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = if (state.editando && state.fechaHoraOriginal > 0)
                "Registrado: ${DateFormatter.format(state.fechaHoraOriginal)}"
            else DateFormatter.format(System.currentTimeMillis()),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

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

        OutlinedTextField(
            value = state.descripcionText,
            onValueChange = { viewModel.actualizarDescripcion(it) },
            label = { Text(stringResource(R.string.descripcion_opcional)) },
            minLines = 2,
            maxLines = 4,
            singleLine = false,
            modifier = Modifier.fillMaxWidth().testTag("campo_descripcion_gasto")
        )

        if (state.errorGuardado != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.errorGuardado!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))

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
