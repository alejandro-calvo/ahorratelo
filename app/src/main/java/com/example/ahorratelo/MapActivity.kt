package com.example.ahorratelo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.ahorratelo.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding

    private lateinit var expenseViewModel: ExpenseViewModel

    // Cliente de localización. Lo usamos para obtener la última ubicación conocida del dispositivo.
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Guardamos el objeto GoogleMap cuando el MapView haya cargado.
    private lateinit var googleMap: GoogleMap

    // Lista de gastos físicos que tienen coordenadas.
    private var physicalExpenses: List<Expense> = emptyList()

    /*
        Controla si ya hemos centrado la camara en la ubicacion del usuario.
        Asi evitamos que los marcadores muevan la cámara después.
    */
    private var movedToUserLocation = false

    /*
        Peticion de permisos de ubicacion .
        El permiso se pide realmente cuando llamamos a locationPermissionRequest.launch(...).
    */
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val fineLocationGranted = permissions.getOrDefault(
            Manifest.permission.ACCESS_FINE_LOCATION,
            false
        )

        val coarseLocationGranted = permissions.getOrDefault(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            false
        )

        if (fineLocationGranted || coarseLocationGranted) {
            enableUserLocation()
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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Avis al mapview de que la activity se esta creando
        binding.mapView.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(applicationContext)
        )[ExpenseViewModel::class.java]

        /*
            Observamos los gastos fisicos guardados.
            Cuando cambian, actualizamos la lista y pintamos los marcadores si el mapa ya esta cargado.
        */
        expenseViewModel.physicalExpenses.observe(this) { expenses ->
            physicalExpenses = expenses
            //Comprobamos s iel mapa ya esta preparado
            if (::googleMap.isInitialized) {
                //Si lo está pintamos los marcadores.
                paintExpenseMarkers()
            }
        }

        binding.mapView.getMapAsync { map ->
            googleMap = map

            /*
                Cámara inicial por defecto.
                Si hay permisos y ubicación disponible, luego se moverá a la ubicación real.
            */
            val madrid = LatLng(40.4168, -3.7038)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 10f))

            enableUserLocation()
            //Lo ponemos tambien aqui , por si llegan los gastos antes qeu el mapa .
            paintExpenseMarkers()
        }
    }

    /*
        Activa la capa de ubicación del mapa.
        Esta capa es la que muestra el punto azul típico de Google Maps.
        Si no tenemos permisos, los pedimos usando locationPermissionRequest.launch(...).
    */
    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        if (hasLocationPermission()) {
            googleMap.isMyLocationEnabled = true
            moveCameraToUserLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /*
        Comprueba si tenemos permiso de ubicación fina o aproximada.
        Con que tengamos uno de los dos, nos sirve para mostrar la ubicación del usuario.
    */
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /*
        Mueve la cámara a la última ubicación conocida del usuario.
        No es ubicación en tiempo real: solo se consulta al abrir el mapa.
    */
    @SuppressLint("MissingPermission")
    private fun moveCameraToUserLocation() {
        if (hasLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userPosition = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 15f))
                    movedToUserLocation = true
                }
            }
        }
    }

    /*
        Limpia el mapa y añade un marcador por cada gasto fisico con latitud y longitud.
        El color del marcador cambia según la categoría del gasto.
    */
    private fun paintExpenseMarkers() {
        googleMap.clear()

        var firstPosition: LatLng? = null

        for (expense in physicalExpenses) {
            if (expense.latitude != null && expense.longitude != null) {
                val position = LatLng(expense.latitude, expense.longitude)

                if (firstPosition == null) {
                    firstPosition = position
                }
                googleMap.addMarker(
                    MarkerOptions()
                        .title(expense.name)
                        .snippet("${expense.category} - ${expense.amount} €")
                        .position(position)
                        .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(expense.category)))
                )
            }
        }

        /*
            Si no hemos podido mover la cámara a la ubicación del usuario,
            la movemos al primer gasto físico que exista.
        */
        if (!movedToUserLocation && firstPosition != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, 14f))
        }
    }


    //Devuelve el color del marcador según la categoría.
    private fun getMarkerColor(category: String): Float {
        return when (category) {
            "Restaurante" -> BitmapDescriptorFactory.HUE_RED
            "Supermercado" -> BitmapDescriptorFactory.HUE_GREEN
            "Transporte" -> BitmapDescriptorFactory.HUE_BLUE
            "Ocio" -> BitmapDescriptorFactory.HUE_VIOLET
            "Servicio" -> BitmapDescriptorFactory.HUE_ORANGE
            //El else se refiere a "otros"
            else -> BitmapDescriptorFactory.HUE_ROSE
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }
}