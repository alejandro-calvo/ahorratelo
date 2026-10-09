package com.example.ahorratelo

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
    Se ha utilizado una data class porque Expense representa una entidad de datos.
    Cada objeto Expense será un gasto guardado por el usuario.

    Se usa @Entity porque esta clase se corresponde con una tabla de Room.
*/
@Entity(tableName = "expense_table")
data class Expense(


    //autoGenerate = true permite que Room genere el id automáticamente.

    @PrimaryKey(autoGenerate = true)
    val expenseId: Int? = null,

    // Nombre del gasto o del sitio, por ejemplo "Mercadona" o "Netflix".
    val name: String,

    // Importe del gasto.
    val amount: Double,

    // Categoría del gasto, por ejemplo "Supermercado", "Restaurante" o "Suscripción".
    val category: String,

    // Tipo de gasto: "Físico" o "Virtual".
    val type: String,

    // Comentario opcional escrito por el usuario.
    val comment: String,

    // Fecha guardada como texto para hacerlo simple con Room.
    val date: String,

    // Latitud solo se usará si el gasto es físico.
    val latitude: Double?,

    // Longitud solo se usará si el gasto es físico.
    val longitude: Double?,

    // Ruta o URI de la imagen del ticket. Será opcional.
    val ticketImageUri: String?
)