package com.example.ahorratelo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    /*
        En esta Activity usamos findViewById porque el layout principal tiene
        DrawerLayout e incluye content_main.xml.

        Esta forma también aparece en los apuntes para inicializar la AppBar.
    */
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar

    private lateinit var budgetText: TextView
    private lateinit var budgetEditText: EditText
    private lateinit var saveBudgetButton: Button

    private lateinit var totalText: TextView
    private lateinit var remainingText: TextView
    private lateinit var statusMessageText: TextView

    private lateinit var addExpenseFab: FloatingActionButton

    private lateinit var expenseViewModel: ExpenseViewModel

    /*
        SharedPreferences guarda datos sencillos de la app.
        Lo usamos para guardar el presupuesto mensual porque es un único valor simple.
    */
    private lateinit var preferences: SharedPreferences

    /*
        Presupuesto mensual actual.
        Empieza en 500.0 si el usuario todavía no ha guardado otro.
    */
    private var monthlyBudget = 500.0

    /*
        Guardamos la última lista de gastos recibida para poder recalcular
        cuando el usuario cambie el presupuesto.
    */
    private var currentExpenses: List<Expense> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Con esto hacemos que se conecte con activity_main
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        toolbar = findViewById(R.id.toolbar)

        budgetText = findViewById(R.id.budgetText)
        budgetEditText = findViewById(R.id.budgetEditText)
        saveBudgetButton = findViewById(R.id.saveBudgetButton)

        totalText = findViewById(R.id.totalText)
        remainingText = findViewById(R.id.remainingText)
        statusMessageText = findViewById(R.id.statusMessageText)

        addExpenseFab = findViewById(R.id.addExpenseFab)

        /*
            La barra creada en mi layout será la barra superior de la app
            Activamos el boton de la izquierda de la barra
        */
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*
            Cargamos el presupuesto guardado.
            Si no hay ninguno, usamos 500.0 como valor por defecto.
        */
        preferences = getSharedPreferences("ahorratelo_preferences", MODE_PRIVATE)
        //se utiliza get float por que no hay metodo getDouble , pero luego,
        // se pasa a double por qeu nuestra variable monthlyBudget es double
        monthlyBudget = preferences.getFloat("monthlyBudget", 500.0f).toDouble()


        //Creamos el ViewModel usando la Factory porque necesita Context.
        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(applicationContext)
        )[ExpenseViewModel::class.java]

        /*
            Observamos la lista de gastos para actualizar el resumen principal
            y cuando cambia es cuando se ejecuta el bloque
            y allExpenses es la lista de todos los datos que nos la pasa el viewModel.
        */
        expenseViewModel.allExpenses.observe(this) { expenses ->
            currentExpenses = expenses
            updateMainSummary(expenses)
        }


        //Guarda el presupuesto escrito por el usuario.

        saveBudgetButton.setOnClickListener {
            saveBudget()
        }

        /*
            FAB de acción rápida.
            Abre directamente la pantalla para añadir un gasto.
        */
        addExpenseFab.setOnClickListener {
            val intent = Intent(this, AddEditExpenseActivity::class.java)
            startActivity(intent)
        }


        // Configuramos las opciones del menú lateral.

        navView.setNavigationItemSelectedListener {
            drawerLayout.close()
            // it representa la opcino que se ha pulsado, itemId es el id de esa opcion
            when (it.itemId) {
                R.id.nav_expenses -> {
                    val intent = Intent(this, ExpenseListActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_summary -> {
                    val intent = Intent(this, SummaryActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_map -> {
                    val intent = Intent(this, MapActivity::class.java)
                    startActivity(intent)
                }
            }
            //Devuelvo true por que la opcion ya la hemos hecho correctamente.
            true
        }
    }


    //Guarda el presupuesto mensual en SharedPreferences.
    private fun saveBudget() {
        val budgetTextValue = budgetEditText.text.toString()

        if (budgetTextValue.isEmpty()) {
            Toast.makeText(this, "Introduce un presupuesto", Toast.LENGTH_SHORT).show()
            return
        }
        // con to doubleornull  conseguimos que si nos introducen hola , devuelva null
        val newBudget = budgetTextValue.toDoubleOrNull()

        if (newBudget == null) {
            Toast.makeText(this, "Presupuesto no válido", Toast.LENGTH_SHORT).show()
            return
        }

        monthlyBudget = newBudget
        //Lo pongo asi por que shared preference no puede guardar doubles.
        preferences.edit().putFloat("monthlyBudget", monthlyBudget.toFloat()).apply()
        //Borra lo que el user habia puetso en el campo
        budgetEditText.text.clear()

        Toast.makeText(this, "Presupuesto guardado", Toast.LENGTH_SHORT).show()


        // Recalculamos la pantalla con el nuevo presupuesto.
        updateMainSummary(currentExpenses)
    }

    //Infla el menu de la AppBar , es decir mete menu_toolBAR ahí.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    //Controlamos las acciones de la AppBar.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // cuando se pulse el boton de menu , que se abra la barra lateral
            android.R.id.home -> drawerLayout.open()
            //cuando se pulse el boton ayuda , que saque ese toast
            R.id.action_info -> Toast.makeText(this,"ahorratelo - App de control de gastos",Toast.LENGTH_SHORT).show()

            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    //Gestiona las valores de presupuesto , gastos sumados...
    private fun updateMainSummary(expenses: List<Expense>) {
        var total = 0.0

        for (expense in expenses) {
            total += expense.amount
        }

        val remaining = monthlyBudget - total

        budgetText.text = "${monthlyBudget} €"
        totalText.text = "Gastado este mes: ${total} €"
        remainingText.text = "Dinero restante: ${remaining} €"

        if (expenses.isEmpty()) {
            statusMessageText.text = "Todavía no hay gastos registrados."
        } else if (remaining < 0) {
            statusMessageText.text = "Has superado tu presupuesto mensual."
        } else {
            statusMessageText.text = "Vas bien este mes."
        }
    }
}