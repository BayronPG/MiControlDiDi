package com.jhon.micontroldidi.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.jhon.micontroldidi.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeManager: ThemePreferenceManager,
    onNavegarAConfigurarMeta: () -> Unit = {}
) {
    val themeMode by themeManager.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)

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
                text = stringResource(R.string.settings_tema),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    TemaOption(
                        label = stringResource(R.string.settings_tema_sistema),
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { themeManager.setThemeMode(ThemeMode.SYSTEM) },
                        testTag = "tema_sistema"
                    )
                    TemaOption(
                        label = stringResource(R.string.settings_tema_claro),
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { themeManager.setThemeMode(ThemeMode.LIGHT) },
                        testTag = "tema_claro"
                    )
                    TemaOption(
                        label = stringResource(R.string.settings_tema_oscuro),
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { themeManager.setThemeMode(ThemeMode.DARK) },
                        testTag = "tema_oscuro"
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

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

@Composable
private fun TemaOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = Modifier.testTag("${testTag}_radio")
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
