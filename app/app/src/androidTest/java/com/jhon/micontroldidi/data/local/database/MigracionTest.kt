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
import com.jhon.micontroldidi.data.local.entity.JornadaEntity
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
import com.jhon.micontroldidi.data.local.entity.TanqueoEntity
import com.jhon.micontroldidi.domain.NivelCombustible
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
            .addMigrations(
                MiControlDatabase.MIGRATION_1_2,
                MiControlDatabase.MIGRATION_2_3,
                MiControlDatabase.MIGRATION_3_4,
                MiControlDatabase.MIGRATION_4_5,
                MiControlDatabase.MIGRATION_5_6,
                MiControlDatabase.MIGRATION_6_7,
            MiControlDatabase.MIGRATION_7_8
            )
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

    @Test
    fun migracionDosATres_creaTablaMetas() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migracion_v2_v3_test.db"
        context.deleteDatabase(dbName)

        // --- 1. Crear base SQLite v2 con tablas de v1 + v2 ---
        val factory = FrameworkSQLiteOpenHelperFactory()
        val helper = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(2) {
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
                            """CREATE TABLE IF NOT EXISTS `categorias_gasto` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `nombre` TEXT COLLATE NOCASE NOT NULL,
                                `activa` INTEGER NOT NULL DEFAULT 1
                            )"""
                        )
                        db.execSQL(
                            "CREATE UNIQUE INDEX IF NOT EXISTS `index_categorias_gasto_nombre` " +
                            "ON `categorias_gasto` (`nombre`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `gastos` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `fechaHora` INTEGER NOT NULL,
                                `categoriaId` INTEGER NOT NULL,
                                `valor` INTEGER NOT NULL,
                                `descripcion` TEXT NOT NULL DEFAULT '',
                                FOREIGN KEY (`categoriaId`) REFERENCES `categorias_gasto`(`id`)
                                ON DELETE RESTRICT
                            )"""
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_gastos_categoriaId` " +
                            "ON `gastos` (`categoriaId`)"
                        )
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS room_master_table " +
                            "(id INTEGER PRIMARY KEY,identity_hash TEXT)"
                        )
                        db.execSQL(
                            "INSERT OR REPLACE INTO room_master_table " +
                            "(id,identity_hash) VALUES(42, 'dummy_v2')"
                        )
                    }
                    override fun onUpgrade(
                        db: SupportSQLiteDatabase, old: Int, new: Int
                    ) = Unit
                }).build()
        )
        val sqLiteDb = helper.writableDatabase

        // Insertar datos de prueba en v2
        sqLiteDb.execSQL(
            "INSERT INTO viajes (fechaHora, valor, propina, observacion) " +
            "VALUES (1000, 15000, 2000, 'Viaje pre-migración')"
        )
        sqLiteDb.execSQL(
            "INSERT INTO categorias_gasto (nombre, activa) VALUES ('Gasolina', 1)"
        )
        sqLiteDb.execSQL(
            "INSERT INTO gastos (fechaHora, categoriaId, valor, descripcion) " +
            "VALUES (2000, 1, 18000, 'Tanqueo')"
        )
        sqLiteDb.close()
        helper.close()

        // --- 2. Abrir con Room + MIGRATION_2_3 y MIGRATION_3_4 ---
        val database = Room.databaseBuilder(
            context, MiControlDatabase::class.java, dbName
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
            .build()

        runBlocking {
            // --- 3. Verificar datos v2 conservados ---
            val viajes = database.viajeDao().obtenerTodos().first()
            assertEquals(1, viajes.size)
            assertEquals(15000L, viajes[0].valor)

            val categorias = database.categoriaGastoDao().obtenerActivas().first()
            assertTrue(categorias.isNotEmpty())

            val gastos = database.gastoDao().obtenerTodos().first()
            assertEquals(1, gastos.size)
            assertEquals(18000L, gastos[0].valor)

            // --- 4. Verificar nueva tabla metas ---
            // Insertar meta después de migración
            database.metaDao().insertar(
                com.jhon.micontroldidi.data.local.entity.MetaEntity(
                    tipoPeriodo = "DIA",
                    valorObjetivo = 50000,
                    activa = true
                )
            )

            val metaActiva = database.metaDao().obtenerActiva().first()
            assertTrue("Debe haber una meta activa", metaActiva != null)
            assertEquals(50000L, metaActiva!!.valorObjetivo)
            assertEquals("DIA", metaActiva!!.tipoPeriodo)
            assertTrue(metaActiva!!.activa)
        }

        database.close()
    }

    @Test
    fun migracionTresACuatro_creaIndicesDeFechaYConservaDatos() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migracion_v3_v4_test.db"
        context.deleteDatabase(dbName)

        // --- 1. Crear base SQLite v3 con todas las tablas (viajes, categorias, gastos, metas) ---
        val factory = FrameworkSQLiteOpenHelperFactory()
        val helper = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(3) {
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
                            """CREATE TABLE IF NOT EXISTS `categorias_gasto` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `nombre` TEXT COLLATE NOCASE NOT NULL,
                                `activa` INTEGER NOT NULL DEFAULT 1
                            )"""
                        )
                        db.execSQL(
                            "CREATE UNIQUE INDEX IF NOT EXISTS `index_categorias_gasto_nombre` " +
                            "ON `categorias_gasto` (`nombre`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `gastos` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `fechaHora` INTEGER NOT NULL,
                                `categoriaId` INTEGER NOT NULL,
                                `valor` INTEGER NOT NULL,
                                `descripcion` TEXT NOT NULL DEFAULT '',
                                FOREIGN KEY (`categoriaId`) REFERENCES `categorias_gasto`(`id`)
                                ON DELETE RESTRICT
                            )"""
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_gastos_categoriaId` " +
                            "ON `gastos` (`categoriaId`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `metas` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `tipoPeriodo` TEXT NOT NULL,
                                `valorObjetivo` INTEGER NOT NULL,
                                `activa` INTEGER NOT NULL DEFAULT 1,
                                `createdAt` INTEGER NOT NULL
                            )"""
                        )
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS room_master_table " +
                            "(id INTEGER PRIMARY KEY,identity_hash TEXT)"
                        )
                        db.execSQL(
                            "INSERT OR REPLACE INTO room_master_table " +
                            "(id,identity_hash) VALUES(42, 'dummy_v3')"
                        )
                    }
                    override fun onUpgrade(
                        db: SupportSQLiteDatabase, old: Int, new: Int
                    ) = Unit
                }).build()
        )
        val sqLiteDb = helper.writableDatabase

        // Insertar datos de prueba en v3
        sqLiteDb.execSQL(
            "INSERT INTO viajes (fechaHora, valor, propina, observacion) " +
            "VALUES (1000, 15000, 2000, 'Viaje pre-migraci\u00f3n')"
        )
        sqLiteDb.execSQL(
            "INSERT INTO categorias_gasto (nombre, activa) VALUES ('Gasolina', 1)"
        )
        sqLiteDb.execSQL(
            "INSERT INTO gastos (fechaHora, categoriaId, valor, descripcion) " +
            "VALUES (2000, 1, 18000, 'Tanqueo')"
        )
        sqLiteDb.execSQL(
            "INSERT INTO metas (tipoPeriodo, valorObjetivo, activa, createdAt) " +
            "VALUES ('DIA', 50000, 1, 100)"
        )
        sqLiteDb.close()
        helper.close()

        // --- 2. Abrir con Room + MIGRATION_3_4 ---
        val database = Room.databaseBuilder(
            context, MiControlDatabase::class.java, dbName
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
            .build()

        runBlocking {
            // --- 3. Verificar datos conservados ---
            val viajes = database.viajeDao().obtenerTodos().first()
            assertEquals(1, viajes.size)
            assertEquals(15000L, viajes[0].valor)

            val gastos = database.gastoDao().obtenerTodos().first()
            assertEquals(1, gastos.size)
            assertEquals(18000L, gastos[0].valor)

            val metaActiva = database.metaDao().obtenerActiva().first()
            assertTrue("Debe conservarse la meta activa", metaActiva != null)
            assertEquals(50000L, metaActiva!!.valorObjetivo)

            // --- 4. Verificar que los índices de fechaHora existen ---
            val indices = mutableListOf<String>()
            database.openHelper.readableDatabase.query(
                "SELECT name FROM sqlite_master WHERE type = 'index' AND name LIKE 'index_%_fechaHora'"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    indices.add(cursor.getString(0))
                }
            }
            assertTrue("Debe existir índice de viajes por fecha",
                indices.contains("index_viajes_fechaHora"))
            assertTrue("Debe existir índice de gastos por fecha",
                indices.contains("index_gastos_fechaHora"))
        }

        database.close()
    }

    @Test
    fun migracionCuatroACinco_creaPerfilTrabajoYConservaDatos() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migracion_v4_v5_test.db"
        context.deleteDatabase(dbName)

        // --- 1. Crear base SQLite v4 con las cuatro tablas anteriores ---
        val factory = FrameworkSQLiteOpenHelperFactory()
        val helper = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(4) {
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
                            "CREATE INDEX IF NOT EXISTS `index_viajes_fechaHora` " +
                            "ON `viajes` (`fechaHora`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `categorias_gasto` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `nombre` TEXT COLLATE NOCASE NOT NULL,
                                `activa` INTEGER NOT NULL DEFAULT 1
                            )"""
                        )
                        db.execSQL(
                            "CREATE UNIQUE INDEX IF NOT EXISTS `index_categorias_gasto_nombre` " +
                            "ON `categorias_gasto` (`nombre`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `gastos` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `fechaHora` INTEGER NOT NULL,
                                `categoriaId` INTEGER NOT NULL,
                                `valor` INTEGER NOT NULL,
                                `descripcion` TEXT NOT NULL DEFAULT '',
                                FOREIGN KEY (`categoriaId`) REFERENCES `categorias_gasto`(`id`)
                                ON DELETE RESTRICT
                            )"""
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_gastos_categoriaId` " +
                            "ON `gastos` (`categoriaId`)"
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_gastos_fechaHora` " +
                            "ON `gastos` (`fechaHora`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `metas` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `tipoPeriodo` TEXT NOT NULL,
                                `valorObjetivo` INTEGER NOT NULL,
                                `activa` INTEGER NOT NULL DEFAULT 1,
                                `createdAt` INTEGER NOT NULL
                            )"""
                        )
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS room_master_table " +
                            "(id INTEGER PRIMARY KEY,identity_hash TEXT)"
                        )
                        db.execSQL(
                            "INSERT OR REPLACE INTO room_master_table " +
                            "(id,identity_hash) VALUES(42, 'dummy_v4')"
                        )
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase, old: Int, new: Int
                    ) = Unit
                }).build()
        )
        val sqLiteDb = helper.writableDatabase

        // Datos de prueba en v4
        sqLiteDb.execSQL(
            "INSERT INTO viajes (fechaHora, valor, propina, observacion) " +
            "VALUES (1000, 15000, 2000, 'Viaje pre-migraci\u00f3n')"
        )
        sqLiteDb.execSQL(
            "INSERT INTO categorias_gasto (nombre, activa) VALUES ('Gasolina', 1)"
        )
        sqLiteDb.execSQL(
            "INSERT INTO gastos (fechaHora, categoriaId, valor, descripcion) " +
            "VALUES (2000, 1, 18000, 'Tanqueo')"
        )
        sqLiteDb.execSQL(
            "INSERT INTO metas (tipoPeriodo, valorObjetivo, activa, createdAt) " +
            "VALUES ('DIA', 50000, 1, 100)"
        )
        sqLiteDb.close()
        helper.close()

        // --- 2. Abrir con Room + MIGRATION_4_5 ---
        val database = Room.databaseBuilder(
            context, MiControlDatabase::class.java, dbName
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
            .build()

        runBlocking {
            // --- 3. Datos anteriores conservados ---
            val viajes = database.viajeDao().obtenerTodos().first()
            assertEquals(1, viajes.size)
            assertEquals(15000L, viajes[0].valor)

            val gastos = database.gastoDao().obtenerTodos().first()
            assertEquals(1, gastos.size)
            assertEquals(18000L, gastos[0].valor)

            val metaActiva = database.metaDao().obtenerActiva().first()
            assertTrue("Debe conservarse la meta activa", metaActiva != null)
            assertEquals(50000L, metaActiva!!.valorObjetivo)

            // --- 4. El perfil de trabajo queda sembrado ---
            val perfil = database.perfilTrabajoDao().obtener(PerfilTrabajoEntity.ID_UNICO)
            assertTrue("La migración debe sembrar el perfil de trabajo", perfil != null)
            assertEquals("inDrive", perfil!!.plataforma)
            assertEquals("TVS Raider 125 FI", perfil.vehiculo)
            assertEquals("Extra", perfil.tipoCombustible)
            assertEquals("1,2,3,4,5", perfil.diasLaborales)
            assertEquals(360, perfil.horaInicioMinutos)
            assertEquals(900, perfil.horaFinMinutos)
            assertEquals(25, perfil.maxPorcentajeKmVacios)

            // --- 5. El perfil se puede actualizar ---
            database.perfilTrabajoDao().guardar(perfil.copy(ciudad = "Bogotá"))
            val actualizado = database.perfilTrabajoDao().obtener(PerfilTrabajoEntity.ID_UNICO)
            assertEquals("Bogotá", actualizado!!.ciudad)
        }

        database.close()
    }

    @Test
    fun migracionCincoASeis_creaTablaJornadasYConservaDatos() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migracion_v5_v6_test.db"
        context.deleteDatabase(dbName)

        // --- 1. Crear base SQLite v5 con las cinco tablas anteriores ---
        val factory = FrameworkSQLiteOpenHelperFactory()
        val helper = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(5) {
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
                            "CREATE INDEX IF NOT EXISTS `index_viajes_fechaHora` " +
                            "ON `viajes` (`fechaHora`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `categorias_gasto` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `nombre` TEXT COLLATE NOCASE NOT NULL,
                                `activa` INTEGER NOT NULL DEFAULT 1
                            )"""
                        )
                        db.execSQL(
                            "CREATE UNIQUE INDEX IF NOT EXISTS `index_categorias_gasto_nombre` " +
                            "ON `categorias_gasto` (`nombre`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `gastos` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `fechaHora` INTEGER NOT NULL,
                                `categoriaId` INTEGER NOT NULL,
                                `valor` INTEGER NOT NULL,
                                `descripcion` TEXT NOT NULL DEFAULT '',
                                FOREIGN KEY (`categoriaId`) REFERENCES `categorias_gasto`(`id`)
                                ON DELETE RESTRICT
                            )"""
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_gastos_categoriaId` " +
                            "ON `gastos` (`categoriaId`)"
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_gastos_fechaHora` " +
                            "ON `gastos` (`fechaHora`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `metas` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `tipoPeriodo` TEXT NOT NULL,
                                `valorObjetivo` INTEGER NOT NULL,
                                `activa` INTEGER NOT NULL DEFAULT 1,
                                `createdAt` INTEGER NOT NULL
                            )"""
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `perfil_trabajo` (
                                `id` INTEGER NOT NULL,
                                `plataforma` TEXT NOT NULL,
                                `plataformasDisponibles` TEXT NOT NULL,
                                `vehiculo` TEXT NOT NULL,
                                `tipoCombustible` TEXT NOT NULL,
                                `ciudad` TEXT NOT NULL,
                                `diasLaborales` TEXT NOT NULL,
                                `horaInicioMinutos` INTEGER NOT NULL,
                                `horaFinMinutos` INTEGER NOT NULL,
                                `maxPorcentajeKmVacios` INTEGER NOT NULL,
                                `costoAceite` INTEGER NOT NULL,
                                `intervaloAceiteKm` INTEGER NOT NULL,
                                `costoLlantas` INTEGER NOT NULL,
                                `intervaloLlantasKm` INTEGER NOT NULL,
                                `costoFrenos` INTEGER NOT NULL,
                                `intervaloFrenosKm` INTEGER NOT NULL,
                                `costoKitArrastre` INTEGER NOT NULL,
                                `intervaloKitArrastreKm` INTEGER NOT NULL,
                                `costoMantenimiento` INTEGER NOT NULL,
                                `intervaloMantenimientoKm` INTEGER NOT NULL,
                                `costoDepreciacion` INTEGER NOT NULL,
                                `intervaloDepreciacionKm` INTEGER NOT NULL,
                                `actualizadoEn` INTEGER NOT NULL,
                                PRIMARY KEY(`id`)
                            )"""
                        )
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS room_master_table " +
                            "(id INTEGER PRIMARY KEY,identity_hash TEXT)"
                        )
                        db.execSQL(
                            "INSERT OR REPLACE INTO room_master_table " +
                            "(id,identity_hash) VALUES(42, 'dummy_v5')"
                        )
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase, old: Int, new: Int
                    ) = Unit
                }).build()
        )
        val sqLiteDb = helper.writableDatabase

        // Datos de prueba en v5
        sqLiteDb.execSQL(
            "INSERT INTO viajes (fechaHora, valor, propina, observacion) " +
            "VALUES (1000, 15000, 2000, 'Viaje pre-migración v6')"
        )
        sqLiteDb.execSQL(
            "INSERT INTO categorias_gasto (nombre, activa) VALUES ('Gasolina', 1)"
        )
        sqLiteDb.execSQL(
            "INSERT INTO gastos (fechaHora, categoriaId, valor, descripcion) " +
            "VALUES (2000, 1, 18000, 'Tanqueo')"
        )
        sqLiteDb.execSQL(
            "INSERT INTO metas (tipoPeriodo, valorObjetivo, activa, createdAt) " +
            "VALUES ('DIA', 50000, 1, 100)"
        )
        sqLiteDb.execSQL(
            "INSERT INTO perfil_trabajo (id, plataforma, plataformasDisponibles, vehiculo, " +
            "tipoCombustible, ciudad, diasLaborales, horaInicioMinutos, horaFinMinutos, " +
            "maxPorcentajeKmVacios, costoAceite, intervaloAceiteKm, costoLlantas, " +
            "intervaloLlantasKm, costoFrenos, intervaloFrenosKm, costoKitArrastre, " +
            "intervaloKitArrastreKm, costoMantenimiento, intervaloMantenimientoKm, " +
            "costoDepreciacion, intervaloDepreciacionKm, actualizadoEn) " +
            "VALUES (1, 'inDrive', 'inDrive, DiDi', 'TVS Raider 125 FI', 'Extra', 'Medellín', " +
            "'1,2,3,4,5', 360, 900, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)"
        )
        sqLiteDb.close()
        helper.close()

        // --- 2. Abrir con Room + MIGRATION_5_6 ---
        val database = Room.databaseBuilder(
            context, MiControlDatabase::class.java, dbName
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
            .build()

        runBlocking {
            // --- 3. Los datos anteriores se conservan ---
            val viajes = database.viajeDao().obtenerTodos().first()
            assertEquals(1, viajes.size)
            assertEquals(15000L, viajes[0].valor)

            val gastos = database.gastoDao().obtenerTodos().first()
            assertEquals(1, gastos.size)
            assertEquals(18000L, gastos[0].valor)

            val metaActiva = database.metaDao().obtenerActiva().first()
            assertTrue("Debe conservarse la meta activa", metaActiva != null)

            val perfil = database.perfilTrabajoDao().obtener(PerfilTrabajoEntity.ID_UNICO)
            assertTrue("Debe conservarse el perfil", perfil != null)
            assertEquals("inDrive", perfil!!.plataforma)

            // --- 4. La tabla jornadas existe y es usable ---
            val idJornada = database.jornadaDao().insertar(
                JornadaEntity(
                    fechaHoraInicio = 3000L,
                    kilometrajeInicialMetros = 12345000L,
                    precioGalonExtra = 17000L,
                    zonaInicial = "Belén",
                    plataforma = "inDrive",
                    metaBrutaDia = 120000L,
                    nivelEnergia = 8
                )
            )
            assertTrue("La jornada debe insertarse", idJornada > 0)

            val jornada = database.jornadaDao().observarUltima().first()
            assertTrue("Debe leerse la jornada recién creada", jornada != null)
            assertEquals(12345000L, jornada!!.kilometrajeInicialMetros)
            assertEquals(120000L, jornada.metaBrutaDia)
            assertEquals(NivelCombustible.MEDIO, jornada.nivel)
            assertTrue("La revisión debe empezar en buen estado", jornada.puntosEnMalEstado.isEmpty())
        }

        database.close()
    }

    @Test
    fun migracionSeisASiete_agregaDatosDePlataformaYConservaDatos() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migracion_v6_v7_test.db"
        context.deleteDatabase(dbName)

        // --- 1. Crear base SQLite v6 con las seis tablas anteriores ---
        val factory = FrameworkSQLiteOpenHelperFactory()
        val helper = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(6) {
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
                            "CREATE INDEX IF NOT EXISTS `index_viajes_fechaHora` " +
                            "ON `viajes` (`fechaHora`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `categorias_gasto` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `nombre` TEXT COLLATE NOCASE NOT NULL,
                                `activa` INTEGER NOT NULL DEFAULT 1
                            )"""
                        )
                        db.execSQL(
                            "CREATE UNIQUE INDEX IF NOT EXISTS `index_categorias_gasto_nombre` " +
                            "ON `categorias_gasto` (`nombre`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `gastos` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `fechaHora` INTEGER NOT NULL,
                                `categoriaId` INTEGER NOT NULL,
                                `valor` INTEGER NOT NULL,
                                `descripcion` TEXT NOT NULL DEFAULT '',
                                FOREIGN KEY (`categoriaId`) REFERENCES `categorias_gasto`(`id`)
                                ON DELETE RESTRICT
                            )"""
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_gastos_categoriaId` " +
                            "ON `gastos` (`categoriaId`)"
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_gastos_fechaHora` " +
                            "ON `gastos` (`fechaHora`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `metas` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `tipoPeriodo` TEXT NOT NULL,
                                `valorObjetivo` INTEGER NOT NULL,
                                `activa` INTEGER NOT NULL DEFAULT 1,
                                `createdAt` INTEGER NOT NULL
                            )"""
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `perfil_trabajo` (
                                `id` INTEGER NOT NULL,
                                `plataforma` TEXT NOT NULL,
                                `plataformasDisponibles` TEXT NOT NULL,
                                `vehiculo` TEXT NOT NULL,
                                `tipoCombustible` TEXT NOT NULL,
                                `ciudad` TEXT NOT NULL,
                                `diasLaborales` TEXT NOT NULL,
                                `horaInicioMinutos` INTEGER NOT NULL,
                                `horaFinMinutos` INTEGER NOT NULL,
                                `maxPorcentajeKmVacios` INTEGER NOT NULL,
                                `costoAceite` INTEGER NOT NULL,
                                `intervaloAceiteKm` INTEGER NOT NULL,
                                `costoLlantas` INTEGER NOT NULL,
                                `intervaloLlantasKm` INTEGER NOT NULL,
                                `costoFrenos` INTEGER NOT NULL,
                                `intervaloFrenosKm` INTEGER NOT NULL,
                                `costoKitArrastre` INTEGER NOT NULL,
                                `intervaloKitArrastreKm` INTEGER NOT NULL,
                                `costoMantenimiento` INTEGER NOT NULL,
                                `intervaloMantenimientoKm` INTEGER NOT NULL,
                                `costoDepreciacion` INTEGER NOT NULL,
                                `intervaloDepreciacionKm` INTEGER NOT NULL,
                                `actualizadoEn` INTEGER NOT NULL,
                                PRIMARY KEY(`id`)
                            )"""
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `jornadas` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `fechaHoraInicio` INTEGER NOT NULL,
                                `kilometrajeInicialMetros` INTEGER NOT NULL,
                                `nivelCombustible` TEXT NOT NULL,
                                `precioGalonExtra` INTEGER NOT NULL,
                                `zonaInicial` TEXT NOT NULL,
                                `plataforma` TEXT NOT NULL,
                                `metaBrutaDia` INTEGER NOT NULL,
                                `nivelEnergia` INTEGER NOT NULL,
                                `clima` TEXT NOT NULL,
                                `observaciones` TEXT NOT NULL,
                                `llantasOk` INTEGER NOT NULL,
                                `frenosOk` INTEGER NOT NULL,
                                `lucesOk` INTEGER NOT NULL,
                                `direccionalesOk` INTEGER NOT NULL,
                                `cadenaOk` INTEGER NOT NULL,
                                `aceiteOk` INTEGER NOT NULL,
                                `gasolinaOk` INTEGER NOT NULL,
                                `soporteTelefonoOk` INTEGER NOT NULL,
                                `cargaTelefonoOk` INTEGER NOT NULL,
                                `impermeableOk` INTEGER NOT NULL,
                                `documentosOk` INTEGER NOT NULL,
                                `aguaOk` INTEGER NOT NULL
                            )"""
                        )
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS room_master_table " +
                            "(id INTEGER PRIMARY KEY,identity_hash TEXT)"
                        )
                        db.execSQL(
                            "INSERT OR REPLACE INTO room_master_table " +
                            "(id,identity_hash) VALUES(42, 'dummy_v6')"
                        )
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase, old: Int, new: Int
                    ) = Unit
                }).build()
        )
        val sqLiteDb = helper.writableDatabase

        // Datos de prueba en v6 (viaje sin datos de plataforma)
        sqLiteDb.execSQL(
            "INSERT INTO viajes (fechaHora, valor, propina, observacion) " +
            "VALUES (1000, 15000, 2000, 'Viaje pre-migraci\u00f3n v7')"
        )
        sqLiteDb.execSQL(
            "INSERT INTO categorias_gasto (nombre, activa) VALUES ('Gasolina', 1)"
        )
        sqLiteDb.execSQL(
            "INSERT INTO gastos (fechaHora, categoriaId, valor, descripcion) " +
            "VALUES (2000, 1, 18000, 'Tanqueo')"
        )
        sqLiteDb.execSQL(
            "INSERT INTO metas (tipoPeriodo, valorObjetivo, activa, createdAt) " +
            "VALUES ('DIA', 50000, 1, 100)"
        )
        sqLiteDb.execSQL(
            "INSERT INTO perfil_trabajo (id, plataforma, plataformasDisponibles, vehiculo, " +
            "tipoCombustible, ciudad, diasLaborales, horaInicioMinutos, horaFinMinutos, " +
            "maxPorcentajeKmVacios, costoAceite, intervaloAceiteKm, costoLlantas, " +
            "intervaloLlantasKm, costoFrenos, intervaloFrenosKm, costoKitArrastre, " +
            "intervaloKitArrastreKm, costoMantenimiento, intervaloMantenimientoKm, " +
            "costoDepreciacion, intervaloDepreciacionKm, actualizadoEn) " +
            "VALUES (1, 'inDrive', 'inDrive, DiDi', 'TVS Raider 125 FI', 'Extra', 'Medell\u00edn', " +
            "'1,2,3,4,5', 360, 900, 25, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)"
        )
        sqLiteDb.close()
        helper.close()

        // --- 2. Abrir con Room + MiControlDatabase.MIGRATION_6_7 ---
        val database = Room.databaseBuilder(
            context, MiControlDatabase::class.java, dbName
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
            .build()

        runBlocking {
            // --- 3. El viaje anterior se conserva y queda con valores por defecto ---
            val viajes = database.viajeDao().obtenerTodos().first()
            assertEquals(1, viajes.size)
            assertEquals(15000L, viajes[0].valor)
            assertEquals(2000L, viajes[0].propina)
            assertEquals("Viaje pre-migración v7", viajes[0].observacion)
            assertEquals(17000L, viajes[0].ingresoTotal)

            assertEquals("", viajes[0].plataforma)
            assertEquals("", viajes[0].zona)
            assertEquals(0L, viajes[0].distanciaMetros)
            assertEquals("", viajes[0].formaPago)
            assertEquals(0L, viajes[0].peaje)
            assertEquals(0L, viajes[0].distanciaKm)
            assertTrue("El viaje migrado no tiene forma de pago", viajes[0].formaPagoTipo == null)

            // --- 4. Los agregados monetarios no cambian ---
            val ingresos = database.viajeDao().obtenerIngresosPorRango(0L, 9999L).first()
            assertEquals("El ingreso agregado no debe incluir el peaje", 17000L, ingresos)

            // --- 5. Se conservan las seis tablas ---
            val tablas = mutableListOf<String>()
            database.openHelper.readableDatabase.query(
                "SELECT name FROM sqlite_master WHERE type = 'table'"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    tablas.add(cursor.getString(0))
                }
            }
            assertTrue(tablas.containsAll(listOf(
                "viajes", "categorias_gasto", "gastos", "metas", "perfil_trabajo", "jornadas"
            )))

            // --- 6. El índice de fechaHora se conserva ---
            val indices = mutableListOf<String>()
            database.openHelper.readableDatabase.query(
                "SELECT name FROM sqlite_master WHERE type = 'index' AND name LIKE 'index_%_fechaHora'"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    indices.add(cursor.getString(0))
                }
            }
            assertTrue(
                "Debe conservarse el índice de viajes por fecha",
                indices.contains("index_viajes_fechaHora")
            )

            // --- 7. Un viaje nuevo usa los cinco campos ---
            val nuevoId = database.viajeDao().insertar(
                ViajeEntity(
                    fechaHora = 3000L,
                    valor = 20000L,
                    propina = 1000L,
                    observacion = "Viaje con plataforma",
                    plataforma = "inDrive",
                    zona = "Belén",
                    distanciaMetros = 8500L,
                    formaPago = "EFECTIVO",
                    peaje = 12000L
                )
            )
            assertTrue("El viaje nuevo debe insertarse", nuevoId > 0)

            val todos = database.viajeDao().obtenerTodos().first()
            val nuevo = todos.first { it.id == nuevoId }
            assertEquals("inDrive", nuevo.plataforma)
            assertEquals("Belén", nuevo.zona)
            assertEquals(8500L, nuevo.distanciaMetros)
            assertEquals(8L, nuevo.distanciaKm)
            assertEquals("EFECTIVO", nuevo.formaPago)
            assertEquals(12000L, nuevo.peaje)
            assertEquals("El peaje no altera el ingreso total", 21000L, nuevo.ingresoTotal)

            // --- 8. Un viaje migrado se puede editar sin errores ---
            val migrado = todos.first { it.id == viajes[0].id }
            val filas = database.viajeDao().actualizar(
                migrado.id,
                migrado.fechaHora,
                migrado.valor,
                migrado.propina,
                migrado.observacion,
                migrado.plataforma,
                migrado.zona,
                migrado.distanciaMetros,
                migrado.formaPago,
                migrado.peaje
            )
            assertEquals("El viaje migrado debe poder editarse", 1, filas)
        }

        database.close()
    }

    @Test
    fun migracionSieteAOcho_creaTanqueosYAnadeCierreDeJornada() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migracion_v7_v8_test.db"
        context.deleteDatabase(dbName)

        // --- 1. Base SQLite v7: seis tablas, viajes con datos de plataforma
        //        y jornadas todavía sin columnas de cierre ---
        val factory = FrameworkSQLiteOpenHelperFactory()
        val helper = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(7) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `viajes` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `fechaHora` INTEGER NOT NULL,
                                `valor` INTEGER NOT NULL,
                                `propina` INTEGER NOT NULL,
                                `observacion` TEXT NOT NULL,
                                `plataforma` TEXT NOT NULL DEFAULT '',
                                `zona` TEXT NOT NULL DEFAULT '',
                                `distanciaMetros` INTEGER NOT NULL DEFAULT 0,
                                `formaPago` TEXT NOT NULL DEFAULT '',
                                `peaje` INTEGER NOT NULL DEFAULT 0
                            )"""
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_viajes_fechaHora` " +
                            "ON `viajes` (`fechaHora`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `categorias_gasto` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `nombre` TEXT COLLATE NOCASE NOT NULL,
                                `activa` INTEGER NOT NULL DEFAULT 1
                            )"""
                        )
                        db.execSQL(
                            "CREATE UNIQUE INDEX IF NOT EXISTS `index_categorias_gasto_nombre` " +
                            "ON `categorias_gasto` (`nombre`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `gastos` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `fechaHora` INTEGER NOT NULL,
                                `categoriaId` INTEGER NOT NULL,
                                `valor` INTEGER NOT NULL,
                                `descripcion` TEXT NOT NULL DEFAULT '',
                                FOREIGN KEY (`categoriaId`) REFERENCES `categorias_gasto`(`id`)
                                ON DELETE RESTRICT
                            )"""
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_gastos_categoriaId` " +
                            "ON `gastos` (`categoriaId`)"
                        )
                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_gastos_fechaHora` " +
                            "ON `gastos` (`fechaHora`)"
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `metas` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `tipoPeriodo` TEXT NOT NULL,
                                `valorObjetivo` INTEGER NOT NULL,
                                `activa` INTEGER NOT NULL DEFAULT 1,
                                `createdAt` INTEGER NOT NULL
                            )"""
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `perfil_trabajo` (
                                `id` INTEGER NOT NULL,
                                `plataforma` TEXT NOT NULL,
                                `plataformasDisponibles` TEXT NOT NULL,
                                `vehiculo` TEXT NOT NULL,
                                `tipoCombustible` TEXT NOT NULL,
                                `ciudad` TEXT NOT NULL,
                                `diasLaborales` TEXT NOT NULL,
                                `horaInicioMinutos` INTEGER NOT NULL,
                                `horaFinMinutos` INTEGER NOT NULL,
                                `maxPorcentajeKmVacios` INTEGER NOT NULL,
                                `costoAceite` INTEGER NOT NULL,
                                `intervaloAceiteKm` INTEGER NOT NULL,
                                `costoLlantas` INTEGER NOT NULL,
                                `intervaloLlantasKm` INTEGER NOT NULL,
                                `costoFrenos` INTEGER NOT NULL,
                                `intervaloFrenosKm` INTEGER NOT NULL,
                                `costoKitArrastre` INTEGER NOT NULL,
                                `intervaloKitArrastreKm` INTEGER NOT NULL,
                                `costoMantenimiento` INTEGER NOT NULL,
                                `intervaloMantenimientoKm` INTEGER NOT NULL,
                                `costoDepreciacion` INTEGER NOT NULL,
                                `intervaloDepreciacionKm` INTEGER NOT NULL,
                                `actualizadoEn` INTEGER NOT NULL,
                                PRIMARY KEY(`id`)
                            )"""
                        )
                        db.execSQL(
                            """CREATE TABLE IF NOT EXISTS `jornadas` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `fechaHoraInicio` INTEGER NOT NULL,
                                `kilometrajeInicialMetros` INTEGER NOT NULL,
                                `nivelCombustible` TEXT NOT NULL,
                                `precioGalonExtra` INTEGER NOT NULL,
                                `zonaInicial` TEXT NOT NULL,
                                `plataforma` TEXT NOT NULL,
                                `metaBrutaDia` INTEGER NOT NULL,
                                `nivelEnergia` INTEGER NOT NULL,
                                `clima` TEXT NOT NULL,
                                `observaciones` TEXT NOT NULL,
                                `llantasOk` INTEGER NOT NULL,
                                `frenosOk` INTEGER NOT NULL,
                                `lucesOk` INTEGER NOT NULL,
                                `direccionalesOk` INTEGER NOT NULL,
                                `cadenaOk` INTEGER NOT NULL,
                                `aceiteOk` INTEGER NOT NULL,
                                `gasolinaOk` INTEGER NOT NULL,
                                `soporteTelefonoOk` INTEGER NOT NULL,
                                `cargaTelefonoOk` INTEGER NOT NULL,
                                `impermeableOk` INTEGER NOT NULL,
                                `documentosOk` INTEGER NOT NULL,
                                `aguaOk` INTEGER NOT NULL
                            )"""
                        )
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS room_master_table " +
                            "(id INTEGER PRIMARY KEY,identity_hash TEXT)"
                        )
                        db.execSQL(
                            "INSERT OR REPLACE INTO room_master_table " +
                            "(id,identity_hash) VALUES(42, 'dummy_v7')"
                        )
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase, old: Int, new: Int
                    ) = Unit
                }).build()
        )
        val sqLiteDb = helper.writableDatabase

        // Datos de prueba en v7
        sqLiteDb.execSQL(
            "INSERT INTO viajes (fechaHora, valor, propina, observacion, plataforma, zona, " +
            "distanciaMetros, formaPago, peaje) " +
            "VALUES (1000, 15000, 2000, 'Viaje v7', 'inDrive', 'Belén', 8500, 'EFECTIVO', 12000)"
        )
        sqLiteDb.execSQL(
            "INSERT INTO categorias_gasto (nombre, activa) VALUES ('Gasolina', 1)"
        )
        sqLiteDb.execSQL(
            "INSERT INTO gastos (fechaHora, categoriaId, valor, descripcion) " +
            "VALUES (2000, 1, 18000, 'Gasto previo')"
        )
        sqLiteDb.execSQL(
            "INSERT INTO metas (tipoPeriodo, valorObjetivo, activa, createdAt) " +
            "VALUES ('DIA', 50000, 1, 100)"
        )
        sqLiteDb.execSQL(
            "INSERT INTO jornadas (fechaHoraInicio, kilometrajeInicialMetros, nivelCombustible, " +
            "precioGalonExtra, zonaInicial, plataforma, metaBrutaDia, nivelEnergia, clima, " +
            "observaciones, llantasOk, frenosOk, lucesOk, direccionalesOk, cadenaOk, aceiteOk, " +
            "gasolinaOk, soporteTelefonoOk, cargaTelefonoOk, impermeableOk, documentosOk, aguaOk) " +
            "VALUES (3000, 10000000, 'MEDIO', 17000, 'Belén', 'inDrive', 120000, 8, 'Soleado', '', " +
            "1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)"
        )
        sqLiteDb.close()
        helper.close()

        // --- 2. Abrir con Room + MIGRATION_7_8 ---
        val database = Room.databaseBuilder(
            context, MiControlDatabase::class.java, dbName
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
            .build()

        runBlocking {
            // --- 3. Los datos anteriores se conservan ---
            val viaje = database.viajeDao().obtenerTodos().first().single()
            assertEquals(15000L, viaje.valor)
            assertEquals("inDrive", viaje.plataforma)
            assertEquals(8500L, viaje.distanciaMetros)

            val gastosPrevios = database.gastoDao().obtenerTodos().first()
            assertEquals(1, gastosPrevios.size)
            assertEquals(18000L, gastosPrevios[0].valor)
            assertTrue("El gasto previo no viene de un tanqueo", !gastosPrevios[0].esTanqueo)

            // --- 4. La jornada anterior queda abierta ---
            val jornada = database.jornadaDao().observarUltima().first()!!
            assertEquals(0L, jornada.fechaHoraFin)
            assertEquals(0L, jornada.kilometrajeFinalMetros)
            assertTrue("Una jornada migrada queda abierta", !jornada.cerrada)
            assertEquals(0L, jornada.kilometrosTotales)

            // --- 5. El cierre funciona y no se puede repetir ---
            assertEquals(1, database.jornadaDao().cerrar(jornada.id, 4000L, 10_045_000L))
            assertEquals(
                "Un segundo cierre debe rechazarse",
                0,
                database.jornadaDao().cerrar(jornada.id, 5000L, 10_090_000L)
            )
            val cerrada = database.jornadaDao().obtenerPorId(jornada.id)!!
            assertTrue(cerrada.cerrada)
            assertEquals(45L, cerrada.kilometrosTotales)

            // --- 6. La tabla de tanqueos existe y el DAO coordinador funciona ---
            val categoriaGasolina = database.categoriaGastoDao().obtenerIdGasolina()
            assertTrue("La categoría Gasolina debe existir", categoriaGasolina != null)

            val tanqueoId = database.tanqueoDao().crear(
                TanqueoEntity(
                    fechaHora = 5000L,
                    odometroMetros = 10_045_000L,
                    litrosMililitros = 4000L,
                    importePagado = 60000L,
                    esLleno = true,
                    tipoCombustible = "Extra",
                    observacion = "Tanqueo lleno"
                ),
                GastoEntity(
                    fechaHora = 5000L,
                    categoriaId = categoriaGasolina!!,
                    valor = 60000L,
                    descripcion = "Tanqueo lleno"
                )
            )
            assertTrue("El tanqueo debe crearse con su gasto", tanqueoId > 0)

            val tanqueo = database.tanqueoDao().obtenerPorId(tanqueoId)!!
            assertTrue("El tanqueo queda enlazado a su gasto", tanqueo.gastoId > 0)
            assertEquals(15000L, tanqueo.precioLitro)

            // --- 7. El gasto del tanqueo cuenta una sola vez ---
            val gastos = database.gastoDao().obtenerTodos().first()
            val delTanqueo = gastos.filter { it.id == tanqueo.gastoId }
            assertEquals("El gasto del tanqueo existe una sola vez", 1, delTanqueo.size)
            assertTrue("El gasto debe marcarse como procedente de un tanqueo", delTanqueo[0].esTanqueo)
            assertEquals(78000L, database.gastoDao().obtenerTotalGastosPorRango(0L, 9999L).first())

            // --- 8. La FK impide borrar el gasto mientras exista el tanqueo ---
            var bloqueado = false
            try {
                database.gastoDao().eliminar(tanqueo.gastoId)
            } catch (e: Exception) {
                bloqueado = true
            }
            assertTrue("La FK RESTRICT debe bloquear el borrado directo", bloqueado)

            // --- 9. El DAO coordinador elimina tanqueo y gasto juntos ---
            assertEquals(1, database.tanqueoDao().eliminar(tanqueoId))
            assertEquals(1, database.gastoDao().obtenerTodos().first().size)
            assertEquals(18000L, database.gastoDao().obtenerTotalGastosPorRango(0L, 9999L).first())

            // --- 10. Siete tablas ---
            val tablas = mutableListOf<String>()
            database.openHelper.readableDatabase.query(
                "SELECT name FROM sqlite_master WHERE type = 'table'"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    tablas.add(cursor.getString(0))
                }
            }
            assertTrue(tablas.containsAll(listOf(
                "viajes", "categorias_gasto", "gastos", "metas", "perfil_trabajo",
                "jornadas", "tanqueos"
            )))
        }

        database.close()
    }
}
