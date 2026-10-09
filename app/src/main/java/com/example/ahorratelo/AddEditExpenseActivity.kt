package com.example.ahorratelo

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.ahorratelo.databinding.ActivityAddEditExpenseBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.annotation.SuppressLint

class AddEditExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditExpenseBinding
    private lateinit var expenseViewModel: ExpenseViewModel

    // Cliente de localización, lo usamos para rellenar latitud y longitud al añadir un gasto.
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Si currentExpense es null, estamos añadiendo un nuevo gasto.
    // Si tiene valor, estamos editando un gasto existente.
    private var currentExpense: Expense? = null

    /*
        Aquí guardamos la ruta de la imagen del ticket.
        Aunque el campo se llame ticketImageUri en Expense, ahora guardamos
        una ruta interna de la app, para poder volver a mostrar la imagen sin fallos.
    */
    private var selectedTicketImageUri: String? = null

    private val typeOptions = arrayOf("Físico", "Virtual")

    private val categoryOptions = arrayOf(
        "Restaurante",
        "Supermercado",
        "Transporte",
        "Ocio",
        "Servicio",
        "Otros"
    )


    //Aqui lazo , para seleccionar una imagen y utilizamos PickVisualMedia.
    private val pickTicketImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            /*
            Cuando el usuario elige una imagen, la copiamos al almacenamiento interno de la app,
            asi no dependemos de permisos temporales.
            La funcion copytciket la tenemos abajo, para ponerlo en el alamacenamiento interno
            */
            val savedPath = copyTicketImageToInternalStorage(uri)
            if (savedPath != null) {
                selectedTicketImageUri = savedPath

               showTicketImage(savedPath)
            } else {
                Toast.makeText(
                    this,
                    "No se pudo guardar la imagen del ticket",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /*
        Petición de permisos de ubicación
        Si el usuario acepta ubicación fina o aproximada, rellenamos latitud y longitud.
    */
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        if (fineLocationGranted || coarseLocationGranted) {
            //Definimos abajo su funcionalidad
            getLastLocationAndFillFields()
        } else {
            Toast.makeText(
                this,
                "Permiso de ubicación rechazado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_edit_expense)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(applicationContext)
        )[ExpenseViewModel::class.java]

        setupSpinners()
        /*
        Aqui hacemos mirar en el intent , si viene un expenseid , si no viene le ponemos un -1
        para luego entrar en en add o en edit
         */
        val expenseId = intent.getIntExtra("expenseId", -1)
         //aqui lo ponemos asi , por que de primeras viene el modo añadir , no editar
        if (expenseId != -1) {
            prepareEditMode(expenseId)
        } else {
            fillCurrentLocation()
        }

        binding.selectTicketButton.setOnClickListener {
            openTicketPicker()
        }

        binding.saveExpenseButton.setOnClickListener {
            saveExpense()
        }

        binding.deleteExpenseButton.setOnClickListener {
            deleteExpense()
        }
    }

    private fun setupSpinners() {
        //Este adapter le dice al spinner que opciones mostrar y como mostrarlas
        val typeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            typeOptions
        )

        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.typeSpinner.adapter = typeAdapter

        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categoryOptions
        )

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = categoryAdapter
    }

    private fun openTicketPicker() {
        //Llamamaos a la funcion de arriba diciendolo qeu solo queremos seleccionar imgs.
        pickTicketImage.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    /*
        Copia la imagen seleccionada a una carpeta interna de la aplicación.
        De esta forma, aunque la imagen venga de la galería, luego podemos volver
        a mostrarla desde nuestra propia app sin depender de permisos externos.
    */
    private fun copyTicketImageToInternalStorage(uri: Uri): String? {
        return try {
            //Aqui creamos una referencia  a una carpeta de nuestra app
            val ticketsDir = File(filesDir, "tickets")
            //Si la carpeta no existe , se crea
            if (!ticketsDir.exists()) {
                ticketsDir.mkdirs()
            }
            //Creamos un archivo nuevo dentro de tickets que se llamara ticket_12412412 (ej)
            val ticketFile = File(ticketsDir, "ticket_${System.currentTimeMillis()}.jpg")
            //Abre la imagen seleccionada para poder leerla
            val inputStream = contentResolver.openInputStream(uri)
            //Aqui preparamos nuestro archivo donde vamos a copiar la imagen
            val outputStream = FileOutputStream(ticketFile)

            //Aqui copiamos la imagen
            if (inputStream != null) {
                inputStream.copyTo(outputStream)
            }

            inputStream?.close()
            outputStream.close()

            //Aqui devolvemos l aruta del archivo en el almacenamiento interno
            ticketFile.absolutePath
        } catch (e: Exception) {
            /*
           Si algo falla al copiar la imagen, devolvemos null.
           Luego, en la función que llama a esta, ya mostramos un Toast avisando
           de que no se pudo guardar la imagen del ticket.
       */
            null
        }
    }

    //Si tenemos permisos rellenamos , pero si no , los pedimos.
    private fun fillCurrentLocation() {
        if (hasLocationPermission()) {
            getLastLocationAndFillFields()
        } else {
            //Esta la utilizamos arriba para los permisos
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    //Comprobamos si tenemos permisos
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    //Se pone para porque comprobamos permisos en otra funcion y si no nos avisa de que no lo hacemos
    @SuppressLint("MissingPermission")
    private fun getLastLocationAndFillFields() {
        if (hasLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    binding.latitudeEditText.setText(location.latitude.toString())
                    binding.longitudeEditText.setText(location.longitude.toString())
                }
            }
        }
    }

    private fun prepareEditMode(expenseId: Int) {
        binding.formTitleText.text = "Editar gasto"
        binding.toolbar.title = "Editar gasto"
        binding.saveExpenseButton.text = "Guardar cambios"
        binding.deleteExpenseButton.visibility = View.VISIBLE
        //Utilizamos la funcion getbyid del viewmodel
        expenseViewModel.getById(expenseId).observe(this) { expense ->
            if (expense != null) {
                currentExpense = expense

                binding.nameEditText.setText(expense.name)
                binding.amountEditText.setText(expense.amount.toString())
                binding.commentEditText.setText(expense.comment)

                setSpinnerSelection(binding.typeSpinner, expense.type)
                setSpinnerSelection(binding.categorySpinner, expense.category)

                if (expense.latitude != null) {
                    binding.latitudeEditText.setText(expense.latitude.toString())
                }

                if (expense.longitude != null) {
                    binding.longitudeEditText.setText(expense.longitude.toString())
                }

                /*
                    Si el gasto ya tenía ticket, mostramos la imagen.
                    Puede ser una ruta interna nueva o una URI antigua.
                */
                if (expense.ticketImageUri != null) {
                    selectedTicketImageUri = expense.ticketImageUri
                    showTicketImage(expense.ticketImageUri)
                } else {
                    selectedTicketImageUri = null
                    binding.ticketStatusText.text = "Ticket no seleccionado"
                    binding.ticketImageView.visibility = View.GONE
                }
            }
        }
    }

    /*
        Muestra el ticket guardado.
        Si es una ruta interna, se abre como archivo.
        Si es una URI antigua, intenta abrirla como Uri.
    */
    private fun showTicketImage(ticketValue: String) {
        try {
            binding.ticketStatusText.text = "Ticket seleccionado"
            binding.ticketImageView.visibility = View.VISIBLE

            if (ticketValue.startsWith("content://")) {
                //Aqui convertimos el texto a una URI que viene de galeria
                binding.ticketImageView.setImageURI(Uri.parse(ticketValue))
            } else {
                /*
                Si no sale lo de arriba suponemos que es una ruta interna , entonces con File
                lo convertimos a archivo , y con Uri. lo convertimos a uri para mostarlo.
                 */
                binding.ticketImageView.setImageURI(Uri.fromFile(File(ticketValue)))
            }
        } catch (e: Exception) {
            binding.ticketStatusText.text = "Ticket seleccionado, pero no se pudo mostrar"
            binding.ticketImageView.visibility = View.GONE
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        //Utilizamos el spinner que habiamos guardado antes
        val adapter = spinner.adapter
        //Recorremos las opciones del spinner y ponmeos la qeu hemos elegido
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                spinner.setSelection(i)
                return
            }
        }
    }

    private fun saveExpense() {
        val name = binding.nameEditText.text.toString()
        val amountText = binding.amountEditText.text.toString()

        val type = binding.typeSpinner.selectedItem.toString()
        val category = binding.categorySpinner.selectedItem.toString()

        val comment = binding.commentEditText.text.toString()
        val latitudeText = binding.latitudeEditText.text.toString()
        val longitudeText = binding.longitudeEditText.text.toString()

        //Comprobamos los campos minimos
        if (name.isEmpty() || amountText.isEmpty()) {
            Toast.makeText(this, "Rellena los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        //Comprobamos que el importe sea valido.
        if (amount == null) {
            Toast.makeText(this, "El importe no es válido", Toast.LENGTH_SHORT).show()
            return
        }

        val latitude = latitudeText.toDoubleOrNull()
        val longitude = longitudeText.toDoubleOrNull()
        //Si el gasto es Fisico , no dejamos guaradar el gasto sin una ubicacion
        if (type == "Físico" && (latitude == null || longitude == null)) {
            Toast.makeText(
                this,
                "Para un gasto físico indica latitud y longitud",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        //Aqui los nombre tiene que ser iguales a los Expense
        val expense = Expense(
            //Si estoy editando cogeremos un id por qeu current expense valdra algo, pero si no sera null
            expenseId = currentExpense?.expenseId,
            name = name,
            amount = amount,
            category = category,
            type = type,
            comment = comment,
            //Si tenemos una fecha usamos esa  si no tenemos(es null), cogemos una nueva.
            date = currentExpense?.date ?: getCurrentDate(),
            latitude = latitude,
            longitude = longitude,
            ticketImageUri = selectedTicketImageUri
        )

        if (currentExpense == null) {
            expenseViewModel.insertExpense(expense)
            Toast.makeText(this, "Gasto guardado", Toast.LENGTH_SHORT).show()
        } else {
            expenseViewModel.updateExpense(expense)
            Toast.makeText(this, "Gasto actualizado", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun deleteExpense() {
        if (currentExpense != null) {
            expenseViewModel.deleteExpense(currentExpense!!)
            Toast.makeText(this, "Gasto borrado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }
}