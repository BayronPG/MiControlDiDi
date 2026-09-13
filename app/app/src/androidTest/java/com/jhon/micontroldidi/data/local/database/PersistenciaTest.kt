package com.jhon.micontroldidi.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.local.entity.MetaEntity
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import com.jhon.micontroldidi.domain.FormaPago
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de persistencia real: BD Room temporal en archivo.
 *
 * A diferencia de los tests DAO (que usan `inMemoryDatabaseBuilder`),
 * aquí se crea una base en un archivo temporal, se insertan datos,
 * se cierra la instancia y se abre una NUEVA instancia sobre el mismo
 * archivo para verificar que los datos sobreviven al cierre/reapertura.
 *
 * Esto valida directamente el criterio de aceptación de Fase 8:
 * "Los datos persisten después de cerrar y reabrir la app".
 */
@RunWith(AndroidJUnit4::class)
class PersistenciaTest {

    private val dbName = "persistencia_test.db"

    @After
    fun tearDown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)
    }

    private fun crearBaseDatos(): MiControlDatabase =
        Room.databaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            MiControlDatabase::class.java,
            dbName
        )
            .addMigrations(
                MiControlDatabase.MIGRATION_1_2,
                MiControlDatabase.MIGRATION_2_3,
                MiControlDatabase.MIGRATION_3_4,
                MiControlDatabase.MIGRATION_4_5,
                MiControlDatabase.MIGRATION_5_6,
                MiControlDatabase.MIGRATION_6_7,
            MiControlDatabase.MIGRATION_7_8
            )
            .addCallback(MiControlDatabase.obtenerCallbackPrepoblar())
            .build()

    @Test
    fun cerrarYReabrir_conservaViaje() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)

        // --- 1. Primera instancia: insertar un viaje y cerrar ---
        var database = crearBaseDatos()
        val viajeId = database.viajeDao().insertar(
            ViajeEntity(
                fechaHora = 1000L,
                valor = 15000,
                propina = 3000,
                observacion = "Viaje persistente",
                plataforma = "inDrive",
                zona = "Belén",
                distanciaMetros = 8500L,
                formaPago = "EFECTIVO",
                peaje = 12000L
            )
        )
        assertTrue(viajeId > 0)
        database.close()

        // --- 2. Nueva instancia sobre el mismo archivo ---
        database = crearBaseDatos()

        val viajes = database.viajeDao().obtenerTodos().first()
        assertEquals("Debe conservarse el viaje tras reabrir", 1, viajes.size)
        assertEquals(viajeId, viajes[0].id)
        assertEquals(15000L, viajes[0].valor)
        assertEquals(3000L, viajes[0].propina)
        assertEquals("Viaje persistente", viajes[0].observacion)
        assertEquals(18000L, viajes[0].ingresoTotal)

        // Datos de plataforma del incremento E
        assertEquals("inDrive", viajes[0].plataforma)
        assertEquals("Belén", viajes[0].zona)
        assertEquals(8500L, viajes[0].distanciaMetros)
        assertEquals("EFECTIVO", viajes[0].formaPago)
        assertEquals(FormaPago.EFECTIVO, viajes[0].formaPagoTipo)
        assertEquals(12000L, viajes[0].peaje)
        assertEquals("El peaje no forma parte del ingreso total", 18000L, viajes[0].ingresoTotal)

        database.close()
    }

    @Test
    fun cerrarYReabrir_conservaGastoYCategorias() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)

        // --- 1. Primera instancia: insertar gasto y cerrar ---
        var database = crearBaseDatos()
        val categorias = database.categoriaGastoDao().obtenerActivas().first()
        assertTrue("Deben existir categorías prepobladas", categorias.isNotEmpty())

        // El orden de las categorías prepobladas no está garantizado;
        // se usa la primera activa y se recuerda su nombre para verificar.
        val categoriaUsada = categorias.first()
        val gastoId = database.gastoDao().insertar(
            GastoEntity(
                fechaHora = 2000L,
                categoriaId = categoriaUsada.id,
                valor = 18000,
                descripcion = "Tanqueo persistente"
            )
        )
        assertTrue(gastoId > 0)
        database.close()

        // --- 2. Nueva instancia sobre el mismo archivo ---
        database = crearBaseDatos()

        val gastos = database.gastoDao().obtenerTodos().first()
        assertEquals("Debe conservarse el gasto tras reabrir", 1, gastos.size)
        assertEquals(gastoId, gastos[0].id)
        assertEquals(18000L, gastos[0].valor)
        assertEquals("Tanqueo persistente", gastos[0].descripcion)
        assertEquals("El nombre de categoría debe conservarse", categoriaUsada.nombre, gastos[0].nombreCategoria)

        database.close()
    }

    @Test
    fun cerrarYReabrir_conservaMetaActiva() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)

        // --- 1. Primera instancia: guardar meta y cerrar ---
        var database = crearBaseDatos()
        database.metaDao().insertar(
            MetaEntity(
                tipoPeriodo = "DIA",
                valorObjetivo = 50000,
                activa = true
            )
        )
        database.close()

        // --- 2. Nueva instancia sobre el mismo archivo ---
        database = crearBaseDatos()

        val metaActiva = database.metaDao().obtenerActiva().first()
        assertTrue("Debe conservarse la meta activa tras reabrir", metaActiva != null)
        assertEquals(50000L, metaActiva!!.valorObjetivo)
        assertEquals("DIA", metaActiva.tipoPeriodo)
        assertTrue(metaActiva.activa)

        database.close()
    }

    @Test
    fun cerrarYReabrir_conservaTodoElConjuntoDeDatos() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)

        // --- 1. Primera instancia: viaje + gasto + meta, y cerrar ---
        var database = crearBaseDatos()

        database.viajeDao().insertar(
            ViajeEntity(fechaHora = 1000L, valor = 20000, propina = 5000)
        )
        val categorias = database.categoriaGastoDao().obtenerActivas().first()
        database.gastoDao().insertar(
            GastoEntity(
                fechaHora = 2000L,
                categoriaId = categorias.first().id,
                valor = 8000
            )
        )
        database.metaDao().insertar(
            MetaEntity(tipoPeriodo = "MES", valorObjetivo = 1000000, activa = true)
        )
        database.close()

        // --- 2. Nueva instancia: verificar todo el conjunto ---
        database = crearBaseDatos()

        val viajes = database.viajeDao().obtenerTodos().first()
        assertEquals(1, viajes.size)
        assertEquals(25000L, viajes[0].ingresoTotal)

        val gastos = database.gastoDao().obtenerTodos().first()
        assertEquals(1, gastos.size)
        assertEquals(8000L, gastos[0].valor)

        val metaActiva = database.metaDao().obtenerActiva().first()
        assertTrue("Meta activa debe conservarse", metaActiva != null)
        assertEquals(1000000L, metaActiva!!.valorObjetivo)

        val totalIngresos = database.viajeDao().obtenerIngresosPorRango(0L, 9999L).first()
        assertEquals("Los ingresos por rango deben reflejar los datos persistidos", 25000L, totalIngresos)

        database.close()
    }
}
