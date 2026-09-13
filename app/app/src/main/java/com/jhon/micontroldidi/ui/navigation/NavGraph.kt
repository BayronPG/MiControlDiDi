package com.jhon.micontroldidi.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.jhon.micontroldidi.R
import com.jhon.micontroldidi.data.repository.CategoriaGastoRepository
import com.jhon.micontroldidi.data.repository.GastoRepository
import com.jhon.micontroldidi.data.repository.JornadaRepository
import com.jhon.micontroldidi.data.repository.MetaRepository
import com.jhon.micontroldidi.data.repository.PerfilTrabajoRepository
import com.jhon.micontroldidi.data.repository.TanqueoRepository
import com.jhon.micontroldidi.data.repository.ViajeRepository
import com.jhon.micontroldidi.ui.dashboard.DashboardScreen
import com.jhon.micontroldidi.ui.dashboard.DashboardViewModel
import com.jhon.micontroldidi.ui.gasto.GastoViewModel
import com.jhon.micontroldidi.ui.gasto.ListaGastosScreen
import com.jhon.micontroldidi.ui.gasto.RegistrarGastoScreen
import com.jhon.micontroldidi.ui.jornada.JornadaViewModel
import com.jhon.micontroldidi.ui.jornada.RegistrarJornadaScreen
import com.jhon.micontroldidi.ui.meta.ConfigurarMetaScreen
import com.jhon.micontroldidi.ui.meta.MetaViewModel
import com.jhon.micontroldidi.ui.settings.ConfigurarPerfilScreen
import com.jhon.micontroldidi.ui.settings.PerfilTrabajoViewModel
import com.jhon.micontroldidi.ui.settings.SettingsScreen
import com.jhon.micontroldidi.ui.stats.StatsScreen
import com.jhon.micontroldidi.ui.stats.StatsViewModel
import com.jhon.micontroldidi.ui.tanqueo.ListaTanqueosScreen
import com.jhon.micontroldidi.ui.tanqueo.RegistrarTanqueoScreen
import com.jhon.micontroldidi.ui.tanqueo.TanqueoViewModel
import com.jhon.micontroldidi.ui.viaje.ListaViajesScreen
import com.jhon.micontroldidi.ui.viaje.RegistrarViajeScreen
import com.jhon.micontroldidi.ui.viaje.ViajeViewModel
import com.jhon.micontroldidi.util.ResourceProvider

object Rutas {
    const val DASHBOARD = "dashboard"
    const val LISTA_VIAJES = "lista_viajes"
    const val REGISTRAR_VIAJE = "registrar_viaje"
    const val REGISTRAR_VIAJE_CON_ID = "registrar_viaje/{viajeId}"
    const val LISTA_GASTOS = "lista_gastos"
    const val REGISTRAR_GASTO = "registrar_gasto"
    const val REGISTRAR_GASTO_CON_ID = "registrar_gasto/{gastoId}"
    const val CONFIGURAR_META = "configurar_meta"
    const val CONFIGURAR_PERFIL = "configurar_perfil"
    const val REGISTRAR_JORNADA = "registrar_jornada"
    const val LISTA_TANQUEOS = "lista_tanqueos"
    const val REGISTRAR_TANQUEO = "registrar_tanqueo"
    const val REGISTRAR_TANQUEO_CON_ID = "registrar_tanqueo/{tanqueoId}"
    const val ESTADISTICAS = "estadisticas"
    const val CONFIGURACION = "configuracion"
}

private val rutasConBarraInferior = setOf(
    Rutas.DASHBOARD,
    Rutas.LISTA_VIAJES,
    Rutas.LISTA_GASTOS,
    Rutas.ESTADISTICAS,
    Rutas.CONFIGURACION
)

data class BottomNavItem(
    val ruta: String,
    val labelRes: Int,
    val labelCortoRes: Int,
    val icon: ImageVector,
    val testTag: String
)

private val bottomNavItems = listOf(
    BottomNavItem(
        ruta = Rutas.DASHBOARD,
        labelRes = R.string.dashboard_titulo,
        labelCortoRes = R.string.nav_inicio,
        icon = Icons.Filled.Home,
        testTag = "navegacion_dashboard"
    ),
    BottomNavItem(
        ruta = Rutas.LISTA_VIAJES,
        labelRes = R.string.viajes,
        labelCortoRes = R.string.nav_viajes,
        icon = Icons.AutoMirrored.Filled.List,
        testTag = "navegacion_viajes"
    ),
    BottomNavItem(
        ruta = Rutas.LISTA_GASTOS,
        labelRes = R.string.gastos,
        labelCortoRes = R.string.nav_gastos,
        icon = Icons.Filled.ShoppingCart,
        testTag = "navegacion_gastos"
    ),
    BottomNavItem(
        ruta = Rutas.ESTADISTICAS,
        labelRes = R.string.stats_titulo,
        labelCortoRes = R.string.nav_datos,
        icon = Icons.Default.DateRange,
        testTag = "navegacion_estadisticas"
    ),
    BottomNavItem(
        ruta = Rutas.CONFIGURACION,
        labelRes = R.string.settings_titulo,
        labelCortoRes = R.string.nav_ajustes,
        icon = Icons.Default.Settings,
        testTag = "navegacion_configuracion"
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
    categoriaGastoRepository: CategoriaGastoRepository,
    metaRepository: MetaRepository,
    perfilTrabajoRepository: PerfilTrabajoRepository,
    jornadaRepository: JornadaRepository,
    tanqueoRepository: TanqueoRepository,
    resourceProvider: ResourceProvider
) {
    val viajeViewModel: ViajeViewModel = viewModel(
        factory = ViajeViewModel.Factory(
            viajeRepository,
            perfilTrabajoRepository,
            resourceProvider
        )
    )
    val gastoViewModel: GastoViewModel = viewModel(
        factory = GastoViewModel.Factory(gastoRepository, categoriaGastoRepository, resourceProvider)
    )
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(viajeRepository, gastoRepository, metaRepository, resourceProvider)
    )
    val metaViewModel: MetaViewModel = viewModel(
        factory = MetaViewModel.Factory(metaRepository, resourceProvider)
    )
    val statsViewModel: StatsViewModel = viewModel(
        factory = StatsViewModel.Factory(viajeRepository, gastoRepository, resourceProvider)
    )
    val perfilTrabajoViewModel: PerfilTrabajoViewModel = viewModel(
        factory = PerfilTrabajoViewModel.Factory(perfilTrabajoRepository, resourceProvider)
    )
    val jornadaViewModel: JornadaViewModel = viewModel(
        factory = JornadaViewModel.Factory(
            jornadaRepository,
            perfilTrabajoRepository,
            resourceProvider
        )
    )
    val tanqueoViewModel: TanqueoViewModel = viewModel(
        factory = TanqueoViewModel.Factory(
            tanqueoRepository,
            perfilTrabajoRepository,
            resourceProvider
        )
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
                                        popUpTo(Rutas.DASHBOARD) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = {
                                Text(
                                    text = stringResource(item.labelCortoRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Rutas.DASHBOARD) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavegarARegistrarViaje = {
                        navController.navigate(Rutas.REGISTRAR_VIAJE)
                    },
                    onNavegarARegistrarGasto = {
                        navController.navigate(Rutas.REGISTRAR_GASTO)
                    },
                    onNavegarAConfigurarMeta = {
                        navController.navigate(Rutas.CONFIGURAR_META)
                    }
                )
            }

            composable(Rutas.LISTA_VIAJES) {
                ListaViajesScreen(
                    viewModel = viajeViewModel,
                    onNavegarARegistrar = {
                        navController.navigate(Rutas.REGISTRAR_VIAJE)
                    },
                    onNavegarAEditar = { viajeId ->
                        navController.navigate("registrar_viaje/$viajeId")
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

            composable(
                route = Rutas.REGISTRAR_VIAJE_CON_ID,
                arguments = listOf(
                    navArgument("viajeId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStackEntry ->
                val viajeId = backStackEntry.arguments?.getLong("viajeId") ?: -1L
                RegistrarViajeScreen(
                    viewModel = viajeViewModel,
                    viajeId = if (viajeId > 0) viajeId else null,
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
                    },
                    onNavegarAEditar = { gastoId ->
                        navController.navigate("registrar_gasto/$gastoId")
                    }
                )
            }

            composable(
                route = Rutas.REGISTRAR_GASTO_CON_ID,
                arguments = listOf(
                    navArgument("gastoId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStackEntry ->
                val gastoId = backStackEntry.arguments?.getLong("gastoId") ?: -1L
                RegistrarGastoScreen(
                    viewModel = gastoViewModel,
                    gastoId = if (gastoId > 0) gastoId else null,
                    onGuardadoExitoso = {
                        navController.popBackStack()
                    },
                    onCancelar = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Rutas.REGISTRAR_GASTO) {
                RegistrarGastoScreen(
                    viewModel = gastoViewModel,
                    gastoId = null,
                    onGuardadoExitoso = {
                        navController.popBackStack()
                    },
                    onCancelar = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Rutas.CONFIGURAR_META) {
                ConfigurarMetaScreen(
                    viewModel = metaViewModel,
                    onNavegarAtras = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Rutas.CONFIGURACION) {
                SettingsScreen(
                    onNavegarAConfigurarMeta = {
                        navController.navigate(Rutas.CONFIGURAR_META)
                    },
                    onNavegarAConfigurarPerfil = {
                        navController.navigate(Rutas.CONFIGURAR_PERFIL)
                    },
                    onNavegarARegistrarJornada = {
                        navController.navigate(Rutas.REGISTRAR_JORNADA)
                    },
                    onNavegarATanqueos = {
                        navController.navigate(Rutas.LISTA_TANQUEOS)
                    }
                )
            }

            composable(Rutas.REGISTRAR_JORNADA) {
                RegistrarJornadaScreen(
                    viewModel = jornadaViewModel,
                    onNavegarAtras = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Rutas.CONFIGURAR_PERFIL) {
                ConfigurarPerfilScreen(
                    viewModel = perfilTrabajoViewModel,
                    onNavegarAtras = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Rutas.LISTA_TANQUEOS) {
                ListaTanqueosScreen(
                    viewModel = tanqueoViewModel,
                    onNavegarARegistrar = {
                        navController.navigate(Rutas.REGISTRAR_TANQUEO)
                    },
                    onNavegarAEditar = { tanqueoId ->
                        navController.navigate("registrar_tanqueo/$tanqueoId")
                    }
                )
            }

            composable(Rutas.REGISTRAR_TANQUEO) {
                RegistrarTanqueoScreen(
                    viewModel = tanqueoViewModel,
                    onGuardadoExitoso = { navController.popBackStack() },
                    onCancelar = { navController.popBackStack() }
                )
            }

            composable(
                route = Rutas.REGISTRAR_TANQUEO_CON_ID,
                arguments = listOf(
                    navArgument("tanqueoId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStackEntry ->
                val tanqueoId = backStackEntry.arguments?.getLong("tanqueoId") ?: -1L
                RegistrarTanqueoScreen(
                    viewModel = tanqueoViewModel,
                    tanqueoId = if (tanqueoId > 0) tanqueoId else null,
                    onGuardadoExitoso = { navController.popBackStack() },
                    onCancelar = { navController.popBackStack() }
                )
            }

            composable(Rutas.ESTADISTICAS) {
                StatsScreen(
                    viewModel = statsViewModel
                )
            }
        }
    }
}
