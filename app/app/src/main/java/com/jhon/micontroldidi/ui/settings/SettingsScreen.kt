package com.jhon.micontroldidi.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavegarAConfigurarMeta: () -> Unit = {},
    onNavegarAConfigurarPerfil: () -> Unit = {},
    onNavegarARegistrarJornada: () -> Unit = {},
    onNavegarATanqueos: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_titulo)) },
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
                .testTag("settings_contenido")
        ) {
            Text(
                text = stringResource(R.string.meta_titulo),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = onNavegarAConfigurarMeta,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("boton_ir_a_meta")
            ) {
                Text(stringResource(R.string.meta_configurar))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.perfil_titulo),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = stringResource(R.string.perfil_descripcion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = onNavegarAConfigurarPerfil,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("boton_ir_a_perfil")
            ) {
                Text(stringResource(R.string.perfil_configurar))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.jornada_titulo),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = stringResource(R.string.jornada_descripcion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = onNavegarARegistrarJornada,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("boton_ir_a_jornada")
            ) {
                Text(stringResource(R.string.jornada_confirmar_guardar))
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onNavegarATanqueos,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("boton_ir_a_tanqueos")
            ) {
                Text(stringResource(R.string.configurar_tanqueos))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_acerca),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_version, "1.0.0"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.settings_app_name, stringResource(R.string.app_name)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
