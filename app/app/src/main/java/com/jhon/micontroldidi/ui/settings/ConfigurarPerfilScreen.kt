package com.jhon.micontroldidi.ui.settings

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.util.CurrencyFormatter

private val DIAS_ISO = listOf(1, 2, 3, 4, 5, 6, 7)

/**
 * Pantalla del perfil de trabajo: plataforma, vehículo, horario, umbral de
 * kilómetros vacíos y reserva por kilómetro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarPerfilScreen(
    viewModel: PerfilTrabajoViewModel,
    onNavegarAtras: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.perfil_titulo)) },
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
                .testTag("perfil_contenido")
        ) {
            if (state.cargando) {
                Text(
                    text = stringResource(R.string.cargando),
                    style = MaterialTheme.typography.bodyLarge
                )
                return@Column
            }

            Text(
                text = stringResource(R.string.perfil_descripcion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // --- Datos del trabajo ---
            EncabezadoSeccion(stringResource(R.string.perfil_seccion_trabajo))
            CampoTexto(state, CampoPerfil.PLATAFORMA, R.string.perfil_plataforma, viewModel)
            CampoTexto(
                state, CampoPerfil.PLATAFORMAS_DISPONIBLES,
                R.string.perfil_plataformas_disponibles, viewModel
            )
            CampoTexto(state, CampoPerfil.VEHICULO, R.string.perfil_vehiculo, viewModel)
            CampoTexto(
                state, CampoPerfil.TIPO_COMBUSTIBLE,
                R.string.perfil_tipo_combustible, viewModel
            )
            CampoTexto(state, CampoPerfil.CIUDAD, R.string.perfil_ciudad, viewModel)

            // --- Horario ---
            EncabezadoSeccion(stringResource(R.string.perfil_seccion_horario))
            Text(
                text = stringResource(R.string.perfil_dias_laborales),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FilaDias(DIAS_ISO.take(4), state, viewModel)
            FilaDias(DIAS_ISO.drop(4), state, viewModel)
            if (state.errorDias != null) {
                Text(
                    text = state.errorDias!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("error_dias_perfil")
                )
            }
            Spacer(Modifier.height(8.dp))
            CampoTexto(state, CampoPerfil.HORA_INICIO, R.string.perfil_hora_inicio, viewModel)
            CampoTexto(state, CampoPerfil.HORA_FIN, R.string.perfil_hora_fin, viewModel)

            // --- Umbral ---
            Spacer(Modifier.height(8.dp))
            CampoTexto(
                state, CampoPerfil.MAX_KM_VACIOS,
                R.string.perfil_max_km_vacios, viewModel
            )

            // --- Reservas ---
            EncabezadoSeccion(stringResource(R.string.perfil_seccion_reservas))
            Text(
                text = stringResource(R.string.perfil_reservas_ayuda),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            RESERVAS.forEach { par ->
                FilaReserva(state, par, viewModel)
            }

            Text(
                text = stringResource(R.string.perfil_reserva_total),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = stringResource(
                    R.string.perfil_reserva_por_km,
                    CurrencyFormatter.format(state.reservaTotalPorKm)
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("perfil_reserva_total")
            )

            Spacer(Modifier.height(16.dp))

            if (state.mensajeError != null) {
                Text(
                    text = state.mensajeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("error_guardar_perfil")
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.guardadoExitoso) {
                Text(
                    text = stringResource(R.string.perfil_guardado),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("mensaje_perfil_guardado")
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.guardar() },
                enabled = state.formularioValido,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("boton_guardar_perfil")
            ) {
                Text(
                    if (state.guardando) stringResource(R.string.guardando)
                    else stringResource(R.string.guardar)
                )
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onNavegarAtras,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("boton_volver_perfil")
            ) {
                Text(stringResource(R.string.volver))
            }
        }
    }
}

@Composable
private fun EncabezadoSeccion(titulo: String) {
    Spacer(Modifier.height(20.dp))
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun FilaDias(
    dias: List<Int>,
    state: PerfilTrabajoUiState,
    viewModel: PerfilTrabajoViewModel
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        dias.forEach { dia ->
            FilterChip(
                selected = state.estaSeleccionado(dia),
                onClick = { viewModel.alternarDia(dia) },
                label = { Text(stringResource(etiquetaDia(dia))) },
                enabled = !state.guardando,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("chip_dia_$dia")
            )
        }
    }
}

@Composable
private fun CampoTexto(
    state: PerfilTrabajoUiState,
    campo: CampoPerfil,
    etiquetaRes: Int,
    viewModel: PerfilTrabajoViewModel
) {
    val error = state.error(campo)
    OutlinedTextField(
        value = state.valor(campo),
        onValueChange = { viewModel.actualizarCampo(campo, it) },
        label = { Text(stringResource(etiquetaRes)) },
        isError = error != null,
        supportingText = error?.let { mensaje ->
            { Text(text = mensaje, modifier = Modifier.testTag("error_${etiquetaTag(campo)}")) }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (campo.tipo == TipoCampo.ENTERO) KeyboardType.Number else KeyboardType.Text
        ),
        singleLine = true,
        enabled = !state.guardando,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(etiquetaTag(campo))
    )
}

@Composable
private fun FilaReserva(
    state: PerfilTrabajoUiState,
    par: ParReserva,
    viewModel: PerfilTrabajoViewModel
) {
    val errorCosto = state.error(par.costo)
    val errorIntervalo = state.error(par.intervalo)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .testTag("reserva_${par.costo.name.lowercase()}")
    ) {
        Text(
            text = stringResource(etiquetaReserva(par)),
            style = MaterialTheme.typography.labelLarge
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.valor(par.costo),
                onValueChange = { viewModel.actualizarCampo(par.costo, it) },
                label = { Text(stringResource(R.string.perfil_costo)) },
                isError = errorCosto != null,
                supportingText = errorCosto?.let { mensaje ->
                    { Text(text = mensaje, modifier = Modifier.testTag("error_${etiquetaTag(par.costo)}")) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !state.guardando,
                modifier = Modifier
                    .weight(1f)
                    .testTag(etiquetaTag(par.costo))
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = state.valor(par.intervalo),
                onValueChange = { viewModel.actualizarCampo(par.intervalo, it) },
                label = { Text(stringResource(R.string.perfil_intervalo_km)) },
                isError = errorIntervalo != null,
                supportingText = errorIntervalo?.let { mensaje ->
                    { Text(text = mensaje, modifier = Modifier.testTag("error_${etiquetaTag(par.intervalo)}")) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !state.guardando,
                modifier = Modifier
                    .weight(1f)
                    .testTag(etiquetaTag(par.intervalo))
            )
        }
        Text(
            text = stringResource(
                R.string.perfil_reserva_por_km,
                CurrencyFormatter.format(state.reservaPorKm(par))
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun etiquetaTag(campo: CampoPerfil): String = "campo_perfil_${campo.name.lowercase()}"

private fun etiquetaDia(dia: Int): Int = when (dia) {
    1 -> R.string.perfil_dia_lun
    2 -> R.string.perfil_dia_mar
    3 -> R.string.perfil_dia_mie
    4 -> R.string.perfil_dia_jue
    5 -> R.string.perfil_dia_vie
    6 -> R.string.perfil_dia_sab
    else -> R.string.perfil_dia_dom
}

private fun etiquetaReserva(par: ParReserva): Int = when (par.costo) {
    CampoPerfil.COSTO_ACEITE -> R.string.perfil_item_aceite
    CampoPerfil.COSTO_LLANTAS -> R.string.perfil_item_llantas
    CampoPerfil.COSTO_FRENOS -> R.string.perfil_item_frenos
    CampoPerfil.COSTO_KIT_ARRASTRE -> R.string.perfil_item_kit_arrastre
    CampoPerfil.COSTO_MANTENIMIENTO -> R.string.perfil_item_mantenimiento
    CampoPerfil.COSTO_DEPRECIACION -> R.string.perfil_item_depreciacion
    else -> R.string.perfil_seccion_reservas
}
