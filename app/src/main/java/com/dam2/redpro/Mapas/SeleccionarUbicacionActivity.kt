package com.dam2.redpro.Mapas

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dam2.redpro.R
import com.dam2.redpro.databinding.ActivitySeleccionarUbicacionBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.Locale

/**
 * Selección de ubicación con Google Maps + Places
 * - Devuelve latitud/longitud/dirección al caller.
 */
class SeleccionarUbicacionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivitySeleccionarUbicacionBinding

    private companion object {
        private const val DEFAULT_ZOOM = 15f
    }

    private var mMap: GoogleMap? = null
    private var mPlaceClient: PlacesClient? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    private var mLastKnowLocation: Location? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var address: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarUbicacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Panel inferior oculto hasta que haya una selección
        binding.listoLl.visibility = View.GONE

        // Cargar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.MapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Inicializar Places/Location
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.mi_google_maps_api_key))
        }
        mPlaceClient = Places.createClient(this)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar Autocomplete
        val autoCompleteFrag = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment
        autoCompleteFrag.setPlaceFields(
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        )
        autoCompleteFrag.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latlng = place.latLng
                selectedLatitude = latlng?.latitude
                selectedLongitude = latlng?.longitude
                address = place.address.orEmpty()
                if (latlng != null) {
                    addMarker(latlng, place.name.orEmpty(), address)
                }
            }
            override fun onError(status: Status) {
                Toast.makeText(this@SeleccionarUbicacionActivity, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })

        // Botón GPS: centrar en ubicación actual
        binding.IbGps.setOnClickListener {
            if (isGpsActivated()) {
                solicitarPermisoLocalizacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                Toast.makeText(
                    this,
                    "La ubicación no está activada. Actívela para usar el GPS.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Confirmar selección
        binding.BtnListo.setOnClickListener {
            val lat = selectedLatitude
            val lng = selectedLongitude
            if (lat == null || lng == null || address.isBlank()) {
                Toast.makeText(this, "Seleccione un punto en el mapa o use el buscador", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent().apply {
                putExtra("latitud", lat)
                putExtra("longitud", lng)
                putExtra("direccion", address)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    /** Solicita permiso y, si se concede, muestra ubicación actual. */
    @SuppressLint("MissingPermission")
    private val solicitarPermisoLocalizacion: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { seConcede ->
            if (seConcede) {
                mMap?.isMyLocationEnabled = true
                elegirLugarActual()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }

    /** Si el mapa está listo, intenta detectar la ubicación actual y pintarla. */
    private fun elegirLugarActual() {
        if (mMap == null) return
        detectAndShowDeviceLocationMap()
    }

    /** Obtiene la última ubicación conocida y centra el mapa; resuelve dirección. */
    @SuppressLint("MissingPermission")
    private fun detectAndShowDeviceLocationMap() {
        try {
            mFusedLocationProviderClient?.lastLocation
                ?.addOnSuccessListener { location ->
                    if (location != null) {
                        mLastKnowLocation = location
                        selectedLatitude = location.latitude
                        selectedLongitude = location.longitude
                        val latlng = LatLng(location.latitude, location.longitude)
                        mMap?.apply {
                            moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM))
                            animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM))
                        }
                        direccionLatLng(latlng)
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
                    }
                }
                ?.addOnFailureListener { e ->
                    Toast.makeText(this, "Error de ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (_: Exception) { /* no-op */ }
    }

    /** Comprueba si el GPS/Network provider están activos. */
    private fun isGpsActivated(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnable = runCatching { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)
        val networkEnable = runCatching { lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)
        return gpsEnable || networkEnable
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Pedir permiso apenas el mapa esté listo (mejor UX).
        solicitarPermisoLocalizacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        // Tap en el mapa: seleccionar punto y obtener dirección
        mMap?.setOnMapClickListener { latlng ->
            selectedLatitude = latlng.latitude
            selectedLongitude = latlng.longitude
            direccionLatLng(latlng)
        }
    }

    /** Resuelve dirección por lat/lng; añade marcador y muestra panel Listo. */
    private fun direccionLatLng(latlng: LatLng) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addressList = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1)
            if (!addressList.isNullOrEmpty()) {
                val mAddress = addressList[0]
                val addressLine = mAddress.getAddressLine(0) ?: ""
                val subLocality = mAddress.subLocality ?: ""
                address = addressLine
                addMarker(latlng, subLocality, addressLine)
            } else {
                // Fallback si geocoder no retorna nada
                address = "${latlng.latitude}, ${latlng.longitude}"
                addMarker(latlng, "Ubicación seleccionada", address)
            }
        } catch (e: Exception) {
            // Fallback ante error de geocoder
            address = "${latlng.latitude}, ${latlng.longitude}"
            addMarker(latlng, "Ubicación seleccionada", address)
        }
    }

    /** Dibuja marcador, centra cámara y habilita panel de confirmación. */
    private fun addMarker(latlng: LatLng, titulo: String, direccion: String) {
        val map = mMap ?: return
        map.clear()
        val markerOptions = MarkerOptions()
            .position(latlng)
            .title(titulo)
            .snippet(direccion)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

        map.addMarker(markerOptions)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM))
        binding.listoLl.visibility = View.VISIBLE
        binding.lugarSelecTv.text = direccion
    }
}