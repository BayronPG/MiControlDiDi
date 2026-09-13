package com.jhon.micontroldidi.ui.jornada

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.domain.NivelCombustible
import com.jhon.micontroldidi.domain.PuntoRevision
import com.jhon.micontroldidi.util.DateFormatter

/**
 * Registro de jornada: datos del inicio del día y revisión previa de la moto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarJornadaScreen(
    viewModel: JornadaViewModel,
    onNavegarAtras: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.jornada_titulo)) },
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
                .testTag("jornada_contenido")
        ) {
            if (state.cargando) {
                Text(stringResource(R.string.cargando), style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            Text(
                text = stringResource(R.string.jornada_descripcion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            state.jornadaDeHoy?.let { jornada ->
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("jornada_hoy_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = stringResource(
                            R.string.jornada_hoy_registrada,
                            DateFormatter.formatHora(jornada.fechaHoraInicio)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // --- Inicio de jornada ---
            EncabezadoSeccion(stringResource(R.string.jornada_seccion_inicio))
            CampoTexto(
                state, CampoJornada.KILOMETRAJE_INICIAL,
                R.string.jornada_kilometraje_inicial, viewModel
            )

            Text(
                text = stringResource(R.string.jornada_nivel_combustible),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            FilaCombustible(state, viewModel)

            CampoTexto(state, CampoJornada.PRECIO_GALON, R.string.jornada_precio_galon, viewModel)
            CampoTexto(state, CampoJornada.ZONA_INICIAL, R.string.jornada_zona_inicial, viewModel)
            CampoTexto(state, CampoJornada.PLATAFORMA, R.string.jornada_plataforma, viewModel)
            CampoTexto(state, CampoJornada.META_BRUTA, R.string.jornada_meta_bruta, viewModel)
            CampoTexto(state, CampoJornada.NIVEL_ENERGIA, R.string.jornada_energia, viewModel)
            CampoTexto(state, CampoJornada.CLIMA, R.string.jornada_clima, viewModel)
            CampoTexto(state, CampoJornada.OBSERVACIONES, R.string.jornada_observaciones, viewModel)

            // --- Revisión previa ---
            EncabezadoSeccion(stringResource(R.string.jornada_seccion_revision))
            Text(
                text = stringResource(R.string.jornada_revision_ayuda),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PuntoRevision.entries.forEach { punto ->
                FilaRevision(punto, state, viewModel)
            }

            val criticos = state.puntosCriticosEnMalEstado
            if (criticos.isNotEmpty()) {
                val context = LocalContext.current
                val nombresCriticos = criticos.joinToString(", ") {
                    context.getString(etiquetaPunto(it))
                }
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alerta_critica_jornada"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.jornada_alerta_critica_titulo),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = stringResource(
                                R.string.jornada_alerta_critica,
                                nombresCriticos
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.mensajeError != null) {
                Text(
                    text = state.mensajeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("error_guardar_jornada")
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.guardadoExitoso) {
                Text(
                    text = stringResource(R.string.jornada_guardada),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("mensaje_jornada_guardada")
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.guardar() },
                enabled = state.formularioValido,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("boton_guardar_jornada")
            ) {
                Text(
                    if (state.guardando) stringResource(R.string.guardando)
                    else stringResource(R.string.jornada_confirmar_guardar)
                )
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onNavegarAtras,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("boton_volver_jornada")
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
private fun FilaCombustible(
    state: JornadaUiState,
    viewModel: JornadaViewModel
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        NivelCombustible.entries.forEach { nivel ->
            FilterChip(
                selected = state.nivelCombustible == nivel,
                onClick = { viewModel.seleccionarCombustible(nivel) },
                label = { Text(stringResource(etiquetaCombustible(nivel))) },
                enabled = !state.guardando,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("chip_combustible_${nivel.name.lowercase()}")
            )
        }
    }
}

@Composable
private fun FilaRevision(
    punto: PuntoRevision,
    state: JornadaUiState,
    viewModel: JornadaViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .testTag("revision_${punto.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(etiquetaPunto(punto)),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (punto.critico) {
            Text(
                text = "*",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Switch(
            checked = state.estaEnBuenEstado(punto),
            onCheckedChange = { viewModel.alternarRevision(punto) },
            enabled = !state.guardando,
            modifier = Modifier.testTag("switch_revision_${punto.name.lowercase()}")
        )
    }
}

@Composable
private fun CampoTexto(
    state: JornadaUiState,
    campo: CampoJornada,
    etiquetaRes: Int,
    viewModel: JornadaViewModel
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
            keyboardType = if (campo.tipo == TipoCampoJornada.ENTERO) KeyboardType.Number
            else KeyboardType.Text
        ),
        singleLine = true,
        enabled = !state.guardando,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(etiquetaTag(campo))
    )
}

private fun etiquetaTag(campo: CampoJornada): String = "campo_jornada_${campo.name.lowercase()}"

private fun etiquetaPunto(punto: PuntoRevision): Int = when (punto) {
    PuntoRevision.LLANTAS -> R.string.jornada_punto_llantas
    PuntoRevision.FRENOS -> R.string.jornada_punto_frenos
    PuntoRevision.LUCES -> R.string.jornada_punto_luces
    PuntoRevision.DIRECCIONALES -> R.string.jornada_punto_direccionales
    PuntoRevision.CADENA -> R.string.jornada_punto_cadena
    PuntoRevision.ACEITE -> R.string.jornada_punto_aceite
    PuntoRevision.GASOLINA -> R.string.jornada_punto_gasolina
    PuntoRevision.SOPORTE_TELEFONO -> R.string.jornada_punto_soporte
    PuntoRevision.CARGA_TELEFONO -> R.string.jornada_punto_carga
    PuntoRevision.IMPERMEABLE -> R.string.jornada_punto_impermeable
    PuntoRevision.DOCUMENTOS -> R.string.jornada_punto_documentos
    PuntoRevision.AGUA -> R.string.jornada_punto_agua
}

private fun etiquetaCombustible(nivel: NivelCombustible): Int = when (nivel) {
    NivelCombustible.RESERVA -> R.string.jornada_combustible_reserva
    NivelCombustible.CUARTO -> R.string.jornada_combustible_cuarto
    NivelCombustible.MEDIO -> R.string.jornada_combustible_medio
    NivelCombustible.TRES_CUARTOS -> R.string.jornada_combustible_tres_cuartos
    NivelCombustible.LLENO -> R.string.jornada_combustible_lleno
}
