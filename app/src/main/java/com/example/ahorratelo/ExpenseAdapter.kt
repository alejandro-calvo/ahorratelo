package com.example.ahorratelo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

   // Interfaz para comunicar el click de un gasto desde el Adapter hacia la Activity.

interface ExpenseRecyclerViewEvent {
    fun onItemClick(position: Int)
}

/*
    Se utiliza un Adapter porque es la clase encargada de crear las filas del RecyclerView
    y rellenarlas con los datos de la lista de gastos.
*/
class ExpenseAdapter(
    private val data: MutableList<Expense>,
    private val listener: ExpenseRecyclerViewEvent? = null
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    /*
        En una fase anterior el ViewHolder estaba en un archivo separado.

        Aquí lo usamos como inner class dentro del Adapter porque necesitamos
        capturar el click de cada fila del RecyclerView. De esta forma, el ViewHolder
        puede implementar View.OnClickListener y avisar al listener que recibe el Adapter.

        Esta estructura sigue el ejemplo de los apuntes de RecyclerView con eventos.
    */
    inner class ExpenseViewHolder(val row: View) :
        RecyclerView.ViewHolder(row),
        View.OnClickListener {

        //Todas aqui van al xml item_expense
        val expenseNameText = row.findViewById<android.widget.TextView>(R.id.expenseNameText)
        val expenseAmountText = row.findViewById<android.widget.TextView>(R.id.expenseAmountText)
        val expenseCategoryText = row.findViewById<android.widget.TextView>(R.id.expenseCategoryText)
        val expenseTypeText = row.findViewById<android.widget.TextView>(R.id.expenseTypeText)
        val expenseDateText = row.findViewById<android.widget.TextView>(R.id.expenseDateText)
        val expenseTicketText = row.findViewById<android.widget.TextView>(R.id.expenseTicketText)

        //Aqui es donde detectamos el click en una fila del RV
        init {
            row.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition

            if (position != RecyclerView.NO_POSITION) {
                listener?.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)

        return ExpenseViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = data[position]

        holder.expenseNameText.text = expense.name
        holder.expenseAmountText.text = "${expense.amount} €"
        holder.expenseCategoryText.text = expense.category
        holder.expenseTypeText.text = expense.type
        holder.expenseDateText.text = expense.date

        /*
            Si el gasto tiene una URI guardada, significa que tiene foto de ticket.
            No mostramos la imagen en la lista para mantenerla sencilla, solo indicamos si existe.
        */
        if (expense.ticketImageUri != null) {
            holder.expenseTicketText.text = "Ticket: Sí"
        } else {
            holder.expenseTicketText.text = "Ticket: No"
        }
    }

    //Devuelve los elementos que tiene la lista.
    override fun getItemCount(): Int {
        return data.size
    }

    //Actualizamos el adapter cuado llegan nuevos datos del VM
    fun updateData(newData: List<Expense>) {
        data.clear()
        data.addAll(newData)
        //Avisa al RV de que los datos han cambiado
        notifyDataSetChanged()
    }

    //Devuelve el gasto que este en esa posicion
    fun getExpenseAt(position: Int): Expense {
        return data[position]
    }
}