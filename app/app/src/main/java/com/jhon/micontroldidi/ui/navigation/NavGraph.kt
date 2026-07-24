package com.jhon.micontroldidi.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.repository.CategoriaGastoRepository
import com.jhon.micontroldidi.data.repository.GastoRepository
import com.jhon.micontroldidi.data.repository.ViajeRepository
import com.jhon.micontroldidi.ui.gasto.GastoViewModel
import com.jhon.micontroldidi.ui.gasto.ListaGastosScreen
import com.jhon.micontroldidi.ui.gasto.RegistrarGastoScreen
import com.jhon.micontroldidi.ui.viaje.ListaViajesScreen
import com.jhon.micontroldidi.ui.viaje.RegistrarViajeScreen
import com.jhon.micontroldidi.ui.viaje.ViajeViewModel

object Rutas {
    const val LISTA_VIAJES = "lista_viajes"
    const val REGISTRAR_VIAJE = "registrar_viaje"
    const val LISTA_GASTOS = "lista_gastos"
    const val REGISTRAR_GASTO = "registrar_gasto"
}

private val rutasConBarraInferior = setOf(Rutas.LISTA_VIAJES, Rutas.LISTA_GASTOS)

data class BottomNavItem(
    val ruta: String,
    val labelRes: Int,
    val icon: ImageVector,
    val testTag: String
)

private val bottomNavItems = listOf(
    BottomNavItem(
        ruta = Rutas.LISTA_VIAJES,
        labelRes = R.string.viajes,
        icon = Icons.AutoMirrored.Filled.List,
        testTag = "navegacion_viajes"
    ),
    BottomNavItem(
        ruta = Rutas.LISTA_GASTOS,
        labelRes = R.string.gastos,
        icon = Icons.AutoMirrored.Filled.List,
        testTag = "navegacion_gastos"
    )
)

@Composable
private fun NavHostController.mostrarBarraInferior(): Boolean {
    val rutaActual = currentBackStackEntryAsState().value?.destination?.route
    return rutaActual in rutasConBarraInferior
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viajeRepository: ViajeRepository,
    gastoRepository: GastoRepository,
    categoriaGastoRepository: CategoriaGastoRepository
) {
    val viajeViewModel: ViajeViewModel = viewModel(
        factory = ViajeViewModel.Factory(viajeRepository)
    )
    val gastoViewModel: GastoViewModel = viewModel(
        factory = GastoViewModel.Factory(gastoRepository, categoriaGastoRepository)
    )

    val mostrarBarra = navController.mostrarBarraInferior()

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val destinoActual = navBackStackEntry?.destination

                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = destinoActual?.hierarchy?.any { it.route == item.ruta } == true,
                            onClick = {
                                if (navController.currentDestination?.route != item.ruta) {
                                    navController.navigate(item.ruta) {
                                        popUpTo(Rutas.LISTA_VIAJES) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.labelRes)) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.LISTA_VIAJES,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Rutas.LISTA_VIAJES) {
                ListaViajesScreen(
                    viewModel = viajeViewModel,
                    onNavegarARegistrar = {
                        navController.navigate(Rutas.REGISTRAR_VIAJE)
                    }
                )
            }

            composable(Rutas.REGISTRAR_VIAJE) {
                RegistrarViajeScreen(
                    viewModel = viajeViewModel,
                    onGuardadoExitoso = {
                        navController.popBackStack()
                    },
                    onCancelar = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Rutas.LISTA_GASTOS) {
                ListaGastosScreen(
                    viewModel = gastoViewModel,
                    onNavegarARegistrar = {
                        navController.navigate(Rutas.REGISTRAR_GASTO)
                    }
                )
            }

            composable(Rutas.REGISTRAR_GASTO) {
                RegistrarGastoScreen(
                    viewModel = gastoViewModel,
                    onGuardadoExitoso = {
                        navController.popBackStack()
                    },
                    onCancelar = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
