package com.jhon.micontroldidi.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jhon.micontroldidi.data.local.dao.CategoriaGastoDao
import com.jhon.micontroldidi.data.local.dao.GastoDao
import com.jhon.micontroldidi.data.local.dao.ViajeDao
import com.jhon.micontroldidi.data.local.dao.JornadaDao
import com.jhon.micontroldidi.data.local.dao.MetaDao
import com.jhon.micontroldidi.data.local.dao.PerfilTrabajoDao
import com.jhon.micontroldidi.data.local.dao.TanqueoDao
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.local.entity.JornadaEntity
import com.jhon.micontroldidi.data.local.entity.MetaEntity
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
import com.jhon.micontroldidi.data.local.entity.TanqueoEntity
import com.jhon.micontroldidi.data.local.entity.ViajeEntity

@Database(
    entities = [
        ViajeEntity::class,
        CategoriaGastoEntity::class,
        GastoEntity::class,
        MetaEntity::class,
        PerfilTrabajoEntity::class,
        JornadaEntity::class,
        TanqueoEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class MiControlDatabase : RoomDatabase() {

    abstract fun viajeDao(): ViajeDao
    abstract fun categoriaGastoDao(): CategoriaGastoDao
    abstract fun gastoDao(): GastoDao
    abstract fun metaDao(): MetaDao
    abstract fun perfilTrabajoDao(): PerfilTrabajoDao
    abstract fun jornadaDao(): JornadaDao
    abstract fun tanqueoDao(): TanqueoDao

    companion object {
        @Volatile
        private var INSTANCIA: MiControlDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
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

                // Categorías iniciales (INSERT OR IGNORE por si ya existen)
                val categorias = listOf(
                    "Gasolina", "Mantenimiento", "Parqueadero",
                    "Lavado", "Cuota de la moto", "Otros"
                )
                categorias.forEach { nombre ->
                    db.execSQL(
                        "INSERT OR IGNORE INTO `categorias_gasto` (`nombre`, `activa`) " +
                        "VALUES (?, 1)",
                        arrayOf(nombre)
                    )
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `metas` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `tipoPeriodo` TEXT NOT NULL,
                    `valorObjetivo` INTEGER NOT NULL,
                    `activa` INTEGER NOT NULL DEFAULT 1,
                    `createdAt` INTEGER NOT NULL
                )"""
            )
        }
    }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Índices para acelerar las consultas por rango de fechas
                // (dashboard y filtros). Los nombres coinciden con los que
                // Room genera automáticamente desde @Index en las entidades.
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_viajes_fechaHora` " +
                    "ON `viajes` (`fechaHora`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_gastos_fechaHora` " +
                    "ON `gastos` (`fechaHora`)"
                )
            }
        }

        /**
         * Inserta el perfil de trabajo por defecto (fila única) partiendo de los
         * valores por defecto de [PerfilTrabajoEntity], de modo que la migración y
         * la entidad no puedan quedar desincronizadas.
         */
        private fun sembrarPerfilPorDefecto(db: SupportSQLiteDatabase) {
            val perfil = PerfilTrabajoEntity()
            db.execSQL(
                """INSERT OR REPLACE INTO `perfil_trabajo` (
                    `id`, `plataforma`, `plataformasDisponibles`, `vehiculo`, `tipoCombustible`,
                    `ciudad`, `diasLaborales`, `horaInicioMinutos`, `horaFinMinutos`,
                    `maxPorcentajeKmVacios`, `costoAceite`, `intervaloAceiteKm`, `costoLlantas`,
                    `intervaloLlantasKm`, `costoFrenos`, `intervaloFrenosKm`, `costoKitArrastre`,
                    `intervaloKitArrastreKm`, `costoMantenimiento`, `intervaloMantenimientoKm`,
                    `costoDepreciacion`, `intervaloDepreciacionKm`, `actualizadoEn`
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
                arrayOf(
                    perfil.id,
                    perfil.plataforma,
                    perfil.plataformasDisponibles,
                    perfil.vehiculo,
                    perfil.tipoCombustible,
                    perfil.ciudad,
                    perfil.diasLaborales,
                    perfil.horaInicioMinutos,
                    perfil.horaFinMinutos,
                    perfil.maxPorcentajeKmVacios,
                    perfil.costoAceite,
                    perfil.intervaloAceiteKm,
                    perfil.costoLlantas,
                    perfil.intervaloLlantasKm,
                    perfil.costoFrenos,
                    perfil.intervaloFrenosKm,
                    perfil.costoKitArrastre,
                    perfil.intervaloKitArrastreKm,
                    perfil.costoMantenimiento,
                    perfil.intervaloMantenimientoKm,
                    perfil.costoDepreciacion,
                    perfil.intervaloDepreciacionKm,
                    perfil.actualizadoEn
                )
            )
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
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
                sembrarPerfilPorDefecto(db)
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
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
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // El viaje se amplía con los datos de plataforma. Las columnas
                // son NOT NULL con valor por defecto para que los viajes ya
                // registrados queden con texto vacío y cero, sin perder datos.
                db.execSQL(
                    "ALTER TABLE `viajes` ADD COLUMN `plataforma` TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE `viajes` ADD COLUMN `zona` TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE `viajes` ADD COLUMN `distanciaMetros` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE `viajes` ADD COLUMN `formaPago` TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE `viajes` ADD COLUMN `peaje` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Tanqueos: cada fila mantiene sincronizado un gasto de gasolina.
                // El gasto es el padre, así que la FK vive en el tanqueo.
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `tanqueos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `fechaHora` INTEGER NOT NULL,
                        `odometroMetros` INTEGER NOT NULL,
                        `litrosMililitros` INTEGER NOT NULL,
                        `importePagado` INTEGER NOT NULL,
                        `esLleno` INTEGER NOT NULL,
                        `tipoCombustible` TEXT NOT NULL,
                        `observacion` TEXT NOT NULL,
                        `gastoId` INTEGER NOT NULL,
                        FOREIGN KEY(`gastoId`) REFERENCES `gastos`(`id`)
                        ON UPDATE NO ACTION ON DELETE RESTRICT
                    )"""
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_tanqueos_fechaHora` " +
                    "ON `tanqueos` (`fechaHora`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_tanqueos_gastoId` " +
                    "ON `tanqueos` (`gastoId`)"
                )
                // Cierre de jornada: 0 significa jornada abierta, de modo que
                // las jornadas ya registradas quedan abiertas sin perder datos.
                db.execSQL(
                    "ALTER TABLE `jornadas` ADD COLUMN `fechaHoraFin` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE `jornadas` ADD COLUMN `kilometrajeFinalMetros` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

    private val PREPOBLAR_CATEGORIAS = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // La entidad declara @ColumnInfo(collate = ColumnInfo.NOCASE)
                // y un índice único. Room genera columna + índice con
                // COLLATE NOCASE automáticamente para bases nuevas.
                val categorias = listOf(
                    "Gasolina", "Mantenimiento", "Parqueadero",
                    "Lavado", "Cuota de la moto", "Otros"
                )
                categorias.forEach { nombre ->
                    db.execSQL(
                        "INSERT OR IGNORE INTO `categorias_gasto` (`nombre`, `activa`) " +
                        "VALUES (?, 1)",
                        arrayOf(nombre)
                    )
                }
                sembrarPerfilPorDefecto(db)
            }
        }

        @JvmStatic
        fun obtenerCallbackPrepoblar(): Callback = PREPOBLAR_CATEGORIAS

        fun obtenerInstancia(context: Context): MiControlDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    MiControlDatabase::class.java,
                    "micontrol_didi.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8
                    )
                    .addCallback(PREPOBLAR_CATEGORIAS)
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
