package com.example.ahorratelo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/*
    Se utiliza una interfaz DAO porque Room genera automaticamente su implementacion.
    Aqui definimos las operaciones que se pueden hacer sobre la tabla de expenses.
*/
@Dao
interface ExpenseDao {

    /*
        Devuelve todos los gastos guardados.
        Como devuelve LiveData, la lista podrá observarse desde la Activity o el ViewModel.
    */
    @Query("SELECT * FROM expense_table")
    fun getAll(): LiveData<List<Expense>>

    //Devuelve un gasto concreto por su id, lo usamos para editar un gasto existente.
    @Query("SELECT * FROM expense_table WHERE expenseId = :id")
    fun getById(id: Int): LiveData<Expense>

    /*
        Devuelve solo los gastos físicos que tienen coordenadas.
        En el mapa saldran los gastos fisicos como marcadores , pero no apareceran , gastos virtuales ,
        ni fisicos sin ubicaion
    */
    @Query("SELECT * FROM expense_table WHERE type = 'Físico' AND latitude IS NOT NULL AND longitude IS NOT NULL")
    fun getPhysicalExpenses(): LiveData<List<Expense>>

    /*
        Insertar, actualizar y borrar se marcan como suspend.
        Asi podran ejecutarse desde corrutinas y no pillar la interfaz
    */
    @Insert
    suspend fun insert(expense: Expense)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}