package com.jhon.micontroldidi.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.local.entity.ViajeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigracionTest {

    @After
    fun tearDown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("migracion_test.db")
    }

    @Test
    fun migracionUnoADos_conservaViajesYCreaTablas() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migracion_test.db"
        context.deleteDatabase(dbName)

        // --- 1. Crear base SQLite v1 con tabla viajes ---
        val factory = FrameworkSQLiteOpenHelperFactory()
        val helper = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `viajes` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `fechaHora` INTEGER NOT NULL,
                                `valor` INTEGER NOT NULL,
                                `propina` INTEGER NOT NULL,
                                `observacion` TEXT NOT NULL
                            )"""
                        )
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS room_master_table " +
                            "(id INTEGER PRIMARY KEY,identity_hash TEXT)"
                        )
                        db.execSQL(
                            "INSERT OR REPLACE INTO room_master_table " +
                            "(id,identity_hash) VALUES(42, 'dummy_v1')"
                        )
                    }
                    override fun onUpgrade(
                        db: SupportSQLiteDatabase, old: Int, new: Int
                    ) = Unit
                }).build()
        )
        val sqLiteDb = helper.writableDatabase
        sqLiteDb.execSQL(
            "INSERT INTO viajes (fechaHora, valor, propina, observacion) " +
            "VALUES (1000, 15000, 3000, 'Viaje pre-migración')"
        )
        sqLiteDb.close()
        helper.close()

        // --- 2. Abrir con Room + MIGRATION_1_2 ---
        val database = Room.databaseBuilder(
            context, MiControlDatabase::class.java, dbName
        )
            .addMigrations(MiControlDatabase.MIGRATION_1_2)
            .build()

        runBlocking {
            // --- 3. Verificar viaje conservado ---
            val viajes: List<ViajeEntity> = database.viajeDao().obtenerTodos().first()
            assertEquals(1, viajes.size)
            assertEquals(15000L, viajes[0].valor)
            assertEquals(3000L, viajes[0].propina)
            assertEquals("Viaje pre-migración", viajes[0].observacion)

            // --- 4. Categorías iniciales ---
            val categorias: List<CategoriaGastoEntity> =
                database.categoriaGastoDao().obtenerActivas().first()
            val nombres: List<String> = categorias.map { it.nombre }
            assertEquals(6, nombres.size)
            assertTrue(nombres.containsAll(listOf(
                "Gasolina", "Mantenimiento", "Parqueadero",
                "Lavado", "Cuota de la moto", "Otros"
            )))

            // --- 5. Insertar gasto funciona ---
            val catId: Long = categorias.first().id
            val gastoId: Long = database.gastoDao().insertar(
                GastoEntity(fechaHora = 2000L, categoriaId = catId, valor = 8000)
            )
            assertTrue(gastoId > 0)
        }

        database.close()
    }
}
