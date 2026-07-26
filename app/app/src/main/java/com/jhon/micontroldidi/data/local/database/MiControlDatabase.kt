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
import com.jhon.micontroldidi.data.local.entity.CategoriaGastoEntity
import com.jhon.micontroldidi.data.local.entity.GastoEntity
import com.jhon.micontroldidi.data.local.entity.MetaEntity
import com.jhon.micontroldidi.data.local.entity.ViajeEntity

@Database(
    entities = [
        ViajeEntity::class,
        CategoriaGastoEntity::class,
        GastoEntity::class,
        MetaEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MiControlDatabase : RoomDatabase() {

    abstract fun viajeDao(): ViajeDao
    abstract fun categoriaGastoDao(): CategoriaGastoDao
    abstract fun gastoDao(): GastoDao
    abstract fun metaDao(): MetaDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(PREPOBLAR_CATEGORIAS)
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
