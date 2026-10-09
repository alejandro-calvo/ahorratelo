package com.example.ahorratelo

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ahorratelo.databinding.ActivityExpenseListBinding

class ExpenseListActivity : AppCompatActivity(), ExpenseRecyclerViewEvent {

    private lateinit var binding: ActivityExpenseListBinding

    private lateinit var expenseViewModel: ExpenseViewModel

    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_expense_list)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(applicationContext)
        )[ExpenseViewModel::class.java]

        expenseAdapter = ExpenseAdapter(mutableListOf(), this)

        binding.expenseRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.expenseRecyclerView.adapter = expenseAdapter

        //Aqui le pasamos los datos del room al adapter , para que luego los muestre
        expenseViewModel.allExpenses.observe(this) { expenses ->
            expenseAdapter.updateData(expenses)
        }

        binding.addExpenseFab.setOnClickListener {
            val intent = Intent(this, AddEditExpenseActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onItemClick(position: Int) {
        val expense = expenseAdapter.getExpenseAt(position)

        val intent = Intent(this, AddEditExpenseActivity::class.java)
        intent.putExtra("expenseId", expense.expenseId)
        startActivity(intent)
    }


    //Con el finish() hacemos que se cierra esta Activity y se vuelva a la anterior

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }
}