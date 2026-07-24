package com.jhon.micontroldidi.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jhon.micontroldidi.data.local.dao.ViajeDao
import com.jhon.micontroldidi.data.local.entity.ViajeEntity

@Database(
    entities = [ViajeEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MiControlDatabase : RoomDatabase() {

    abstract fun viajeDao(): ViajeDao

    companion object {
        @Volatile
        private var INSTANCIA: MiControlDatabase? = null

        fun obtenerInstancia(context: Context): MiControlDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    MiControlDatabase::class.java,
                    "micontrol_didi.db"
                ).build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
