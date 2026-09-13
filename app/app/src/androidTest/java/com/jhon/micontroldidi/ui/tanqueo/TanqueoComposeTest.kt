package com.jhon.micontroldidi.ui.tanqueo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.jhon.micontroldidi.data.local.dao.CategoriaGastoDao
import com.jhon.micontroldidi.data.local.dao.PerfilTrabajoDao
import com.jhon.micontroldidi.data.local.dao.TanqueoDao
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
import com.jhon.micontroldidi.data.local.entity.TanqueoEntity
import com.jhon.micontroldidi.data.repository.PerfilTrabajoRepository
import com.jhon.micontroldidi.data.repository.TanqueoRepository
import com.jhon.micontroldidi.util.ResourceProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas Compose del listado y del formulario de tanqueos.
 * El contenido se valida por etiquetas de prueba, sin depender de los textos.
 */
class TanqueoComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val tanqueos = MutableStateFlow<List<TanqueoEntity>>(emptyList())
    private var errorCarga = false

    private val resourceProvider = object : ResourceProvider {
        override fun getString(resId: Int): String = ""
    }

    private val tanqueoDaoFalso = object : TanqueoDao() {
        override suspend fun insertarGasto(gasto: GastoEntity): Long = 1L
        override suspend fun actualizarGasto(
            id: Long,
            fechaHora: Long,
            categoriaId: Long,
            valor: Long,
            descripcion: String
        ): Int = 1

        override suspend fun eliminarGasto(id: Long): Int = 1
        override suspend fun insertarTanqueo(tanqueo: TanqueoEntity): Long = 1L
        override suspend fun actualizarTanqueo(tanqueo: TanqueoEntity): Int = 1
        override suspend fun eliminarTanqueo(id: Long): Int = 1

        override fun observarTodos(): Flow<List<TanqueoEntity>> =
            if (errorCarga) flow { throw RuntimeException("Error BD") } else tanqueos

        override fun observarUltimo(): Flow<TanqueoEntity?> = flowOf(null)
        override suspend fun obtenerPorId(id: Long): TanqueoEntity? = null
    }

    private val categoriaDaoFalso = object : CategoriaGastoDao {
        override suspend fun insertar(categoria: CategoriaGastoEntity): Long = 1L
        override suspend fun insertarLista(categorias: List<CategoriaGastoEntity>): List<Long> =
            emptyList()

        override fun obtenerActivas(): Flow<List<CategoriaGastoEntity>> = flowOf(emptyList())
        override suspend fun obtenerPorId(id: Long): CategoriaGastoEntity? = null
        override suspend fun existePorNombre(nombre: String): Boolean = true
        override suspend fun obtenerIdGasolina(): Long? = 1L
    }

    private val perfilDaoFalso = object : PerfilTrabajoDao {
        override fun observar(id: Int): Flow<PerfilTrabajoEntity?> = flowOf(PerfilTrabajoEntity())
        override suspend fun obtener(id: Int): PerfilTrabajoEntity? = null
        override suspend fun guardar(perfilNuevo: PerfilTrabajoEntity) = Unit
    }

    private lateinit var viewModel: TanqueoViewModel

    @Before
    fun setUp() {
        tanqueos.value = emptyList()
        errorCarga = false
        viewModel = TanqueoViewModel(
            TanqueoRepository(tanqueoDaoFalso, categoriaDaoFalso),
            PerfilTrabajoRepository(perfilDaoFalso),
            resourceProvider
        )
    }

    private fun montarLista() {
        composeTestRule.setContent {
            ListaTanqueosScreen(
                viewModel = viewModel,
                onNavegarARegistrar = {},
                onNavegarAEditar = {}
            )
        }
    }

    @Test
    fun listaVacia_muestraEstadoVacioYBotonRegistrar() {
        montarLista()

        composeTestRule.onNodeWithTag("estado_vacio_tanqueos").assertIsDisplayed()
        composeTestRule.onNodeWithTag("boton_registrar_tanqueo").assertIsDisplayed()
    }

    @Test
    fun listaConTanqueo_muestraElDetalle() {
        tanqueos.value = listOf(
            TanqueoEntity(
                id = 3L,
                fechaHora = 5000L,
                odometroMetros = 10_045_000L,
                litrosMililitros = 4000L,
                importePagado = 60000L,
                esLleno = true,
                tipoCombustible = "Extra",
                gastoId = 42L
            )
        )

        montarLista()

        composeTestRule.onNodeWithTag("lista_tanqueos").assertIsDisplayed()
        composeTestRule.onNodeWithTag("detalle_tanqueo_3").assertIsDisplayed()
        composeTestRule.onNodeWithTag("boton_editar_tanqueo_3").assertIsDisplayed()
        composeTestRule.onNodeWithTag("boton_eliminar_tanqueo_3").assertIsDisplayed()
    }

    @Test
    fun errorDeCarga_muestraEstadoDeError() {
        errorCarga = true

        montarLista()

        composeTestRule.onNodeWithTag("estado_error_tanqueos").assertIsDisplayed()
    }

    @Test
    fun formulario_muestraLosCamposYElBotonDeshabilitado() {
        composeTestRule.setContent {
            RegistrarTanqueoScreen(
                viewModel = viewModel,
                onGuardadoExitoso = {},
                onCancelar = {}
            )
        }

        composeTestRule.onNodeWithTag("campo_odometro_tanqueo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("campo_litros_tanqueo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("campo_importe_tanqueo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("campo_tipo_combustible_tanqueo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("switch_tanqueo_lleno").assertIsDisplayed()
        composeTestRule.onNodeWithTag("boton_guardar_tanqueo").assertIsNotEnabled()
    }
}
