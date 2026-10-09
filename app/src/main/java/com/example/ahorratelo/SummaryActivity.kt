package com.example.ahorratelo

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.ahorratelo.databinding.ActivitySummaryBinding

class SummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySummaryBinding

    private lateinit var expenseViewModel: ExpenseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_summary)

        //Configuramos appbar , para volver atrás.
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(applicationContext)
        )[ExpenseViewModel::class.java]

        expenseViewModel.allExpenses.observe(this) { expenses ->
            updateSummary(expenses)
        }
    }

    private fun updateSummary(expenses: List<Expense>) {
        var total = 0.0
        var physicalTotal = 0.0
        var virtualTotal = 0.0

        //Tabla de acumulados por categoria, string(nombre categoria),double(dinero acumulado categoria)
        val categoryTotals = mutableMapOf<String, Double>()

        for (expense in expenses) {
            total += expense.amount

            if (expense.type == "Físico") {
                physicalTotal += expense.amount
            } else if (expense.type == "Virtual") {
                virtualTotal += expense.amount
            }
            //Miro caunto dinero lleva acumulado esa categoria , si no pongo 0.0
            val previousCategoryTotal = categoryTotals[expense.category] ?: 0.0
            categoryTotals[expense.category] = previousCategoryTotal + expense.amount
        }

        binding.summaryTotalText.text = "Total gastado: ${total} €"
        binding.summaryPhysicalText.text = "Gastos físicos: ${physicalTotal} €"
        binding.summaryVirtualText.text = "Gastos virtuales: ${virtualTotal} €"

        if (categoryTotals.isEmpty()) {
            binding.categorySummaryText.text = "Todavía no hay gastos."
        } else {
            var categoryText = ""

            for (entry in categoryTotals) {
                categoryText += "${entry.key}: ${entry.value} €\n"
            }

            binding.categorySummaryText.text = categoryText
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }
}