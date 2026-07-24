package com.jhon.micontroldidi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jhon.micontroldidi.data.repository.ViajeRepository
import com.jhon.micontroldidi.ui.viaje.ListaViajesScreen
import com.jhon.micontroldidi.ui.viaje.RegistrarViajeScreen
import com.jhon.micontroldidi.ui.viaje.ViajeViewModel

object Rutas {
    const val LISTA_VIAJES = "lista_viajes"
    const val REGISTRAR_VIAJE = "registrar_viaje"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viajeRepository: ViajeRepository,
    viajeViewModel: ViajeViewModel = viewModel(
        factory = ViajeViewModel.Factory(viajeRepository)
    )
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.LISTA_VIAJES
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
    }
}
