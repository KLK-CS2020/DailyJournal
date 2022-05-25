package com.klk.dailyjournal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.klk.dailyjournal.databinding.ActivityMapsBinding
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var latLngToSave: LatLng? = null
    var addressToSave: String? = null

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.setOnMapClickListener {
            mMap.clear()
            placeMarkerOnMap(it)
            setAddress(it)
        }
        setUpMap()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if(location!=null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.altitude)
                placeMarkerOnMap(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
         }
    }

    private fun placeMarkerOnMap(currentLatLng: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLng)
        markerOptions.title("$currentLatLng")
        mMap.addMarker(markerOptions)
        latLngToSave = currentLatLng
    }

    private fun setAddress(currentLatLng: LatLng) {
        var addresses: List<Address>? = null
        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            addresses = geocoder.getFromLocation(currentLatLng.latitude,
                currentLatLng.longitude, 1)
        } catch (e:IOException) {
            e.printStackTrace()
        }

        val address = addresses?.get(0)

        if(address!=null) {
            if(address.getAddressLine(0) != null) {
                addressTv.text = address.getAddressLine(0)
            }
            if(address.getAddressLine(1) != null) {
                addressTv.text = addressTv.text.toString() + address.getAddressLine(1)
            }

            addressToSave = addressTv.text.toString()
        }
    }

    fun saveLocation(view: View) {
        val intent = Intent()

        if(addressToSave!=null)
            intent.putExtra("address", addressToSave)

        if(latLngToSave!=null)
            intent.putExtra("latlng", latLngToSave.toString())

        setResult(RESULT_OK, intent)
        finish()
    }

    fun cancel(view: View) {
        finish()
    }

}