package com.example.ahorratelo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase



//Se utiliza una clase abstract , por que esat sera mi base de datos , pero room se encargara de crearla de verdad
@Database(entities = [Expense::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    /*
        Metodo abstracto que devuelve el DAO.
        Desde el DAO se haran las operaciones sobre la tabla expense_table.
    */
    abstract fun expenseDao(): ExpenseDao

    companion object {

        /*
            INSTANCE guarda una única instancia de la base de datos.
            @Volatile ayuda a que el valor sea visible correctamente entre hilos.
        */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /*
            getInstance devuelve la base de datos.
            Si todavía no existe, la crea con Room.databaseBuilder.
        */
        fun getInstance(context: Context): AppDatabase {

            val tempInstance = INSTANCE

            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ahorratelo_database"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}