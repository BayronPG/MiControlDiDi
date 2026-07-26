package com.jhon.micontroldidi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.jhon.micontroldidi.data.local.database.MiControlDatabase
import com.jhon.micontroldidi.data.repository.CategoriaGastoRepository
import com.jhon.micontroldidi.data.repository.GastoRepository
import com.jhon.micontroldidi.data.repository.MetaRepository
import com.jhon.micontroldidi.data.repository.ViajeRepository
import com.jhon.micontroldidi.ui.navigation.AppNavGraph
import com.jhon.micontroldidi.ui.settings.ThemePreferenceManager
import com.jhon.micontroldidi.ui.theme.MiControlDiDiTheme
import com.jhon.micontroldidi.util.ResourceProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = MiControlDatabase.obtenerInstancia(applicationContext)
        val viajeRepository = ViajeRepository(database.viajeDao())
        val categoriaGastoRepository = CategoriaGastoRepository(database.categoriaGastoDao())
        val gastoRepository = GastoRepository(database.gastoDao())
        val metaRepository = MetaRepository(database.metaDao())

        val themeManager = ThemePreferenceManager(applicationContext)
        val resourceProvider = ResourceProvider { resId -> resources.getString(resId) }

        setContent {
            MiControlDiDiTheme(
                themeManager = themeManager
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        viajeRepository = viajeRepository,
                        gastoRepository = gastoRepository,
                        categoriaGastoRepository = categoriaGastoRepository,
                        metaRepository = metaRepository,
                        themeManager = themeManager,
                        resourceProvider = resourceProvider
                    )
                }
            }
        }
    }
}
