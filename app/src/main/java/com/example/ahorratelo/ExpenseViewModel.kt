package com.example.ahorratelo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
    Se utiliza un ViewModel porque queremos separar los datos y la lógica
    de la interfaz de usuario.

    La Activity no accederá directamente a Room. Pedirá los datos al ViewModel,
    y el ViewModel se apoyará en el Repository.
*/
class ExpenseViewModel(context: Context) : ViewModel() {

    //El repositorio es la capa que comunica el ViewModel con el dao y con room.
    private val repository: ExpenseRepository

    //La Activity observara este live data que se usara para listas y resumenes
    val allExpenses: LiveData<List<Expense>>

    //Lista publica observable con los gastos fisicos y la usaremos para el mapa
    val physicalExpenses: LiveData<List<Expense>>
    //se ejecuta automaticamente cuando se crea el VM
    init {
        /*
            Obtenemos el DAO desde la base de datos.
            Usamos applicationContext para no guardar una referencia directa
            a una Activity.
        */
        val expenseDao = AppDatabase.getInstance(context.applicationContext).expenseDao()

        //Creamos el repositorio usando el DAO. Porque el repository necesita DAO para llamar al room
        repository = ExpenseRepository(expenseDao)


        //Asociamos los live data del VM con los del Repository, es decir cogemos los datos del repositorycon el DAO
        allExpenses = repository.allExpenses
        physicalExpenses = repository.physicalExpenses
    }


    //Recive el id de un expense, y expense concreto de la base de datos
    fun getById(id: Int): LiveData<Expense> {
        return repository.getById(id)
    }


    //Insertar un gasto se hace dentro de viewModelScope. Usamos IO porque es para los datos
    fun insertExpense(expense: Expense) {
        //Lanzamos una tarea para que se ejecute en el IO(input/output), que es segundo plano.
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(expense)
        }
    }

    //Actualizar también se hace en segundo plano
    fun updateExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(expense)
        }
    }

    //Borrar en segundo plano
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(expense)
        }
    }
}