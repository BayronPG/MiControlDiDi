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
import com.jhon.micontroldidi.data.local.dao.MetaDao
import com.jhon.micontroldidi.data.local.dao.PerfilTrabajoDao
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.local.entity.MetaEntity
import com.jhon.micontroldidi.data.local.entity.PerfilTrabajoEntity
import com.jhon.micontroldidi.data.local.entity.ViajeEntity

@Database(
    entities = [
        ViajeEntity::class,
        CategoriaGastoEntity::class,
        GastoEntity::class,
        MetaEntity::class,
        PerfilTrabajoEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MiControlDatabase : RoomDatabase() {

    abstract fun viajeDao(): ViajeDao
    abstract fun categoriaGastoDao(): CategoriaGastoDao
    abstract fun gastoDao(): GastoDao
    abstract fun metaDao(): MetaDao
    abstract fun perfilTrabajoDao(): PerfilTrabajoDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(PREPOBLAR_CATEGORIAS)
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
