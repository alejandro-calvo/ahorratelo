package com.example.ahorratelo

import androidx.lifecycle.LiveData

/*
    Se utiliza una clase Repository para separar el acceso a datos del ViewModel.
    Asi el ViewModel no trabaja directamente con Room, sino con esta clase intermedia.
*/
class ExpenseRepository(private val expenseDao: ExpenseDao) {

    /*
        Lista observable con todos los gastos.
        La Activity podra recibir cambios a través del ViewModel.
    */
    val allExpenses: LiveData<List<Expense>> = expenseDao.getAll()

    /*
        Lista observable solo con los gastos fisicos.
        Luego lo usaremos para pintar marcadores en el mapa.
    */
    val physicalExpenses: LiveData<List<Expense>> = expenseDao.getPhysicalExpenses()

    /*
        Devuelve un gasto por su id.
        Lo usaremos cuando queramos editar un gasto existente.
    */
    fun getById(id: Int): LiveData<Expense> {
        return expenseDao.getById(id)
    }

    /*
        Las funciones insert, update y delete son suspend porque el DAO tambien
        las tiene marcadas como suspend. Se ejecutaran desde corrutinas.
    */
    suspend fun insert(expense: Expense) {
        expenseDao.insert(expense)
    }

    suspend fun update(expense: Expense) {
        expenseDao.update(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
    }
}