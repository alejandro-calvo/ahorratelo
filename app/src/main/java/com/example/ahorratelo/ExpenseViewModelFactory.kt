package com.example.ahorratelo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/*
    Se utiliza una Factory porque ExpenseViewModel recibe un parámetro en su constructor.
    En este caso recibe un Context para poder acceder a la base de datos Room.
*/
class ExpenseViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    //Este metodo crea el ViewModel y le pasa el Context necesario.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpenseViewModel(context) as T
    }
}