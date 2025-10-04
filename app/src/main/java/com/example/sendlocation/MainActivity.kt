package com.example.sendlocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var tvCoords: TextView
    private lateinit var tvStatus: TextView
    private val PHONE_NUMBER = "+923001234567"

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            val sms = perms[Manifest.permission.SEND_SMS] ?: false
            if (fine || coarse) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCoords = findViewById(R.id.tvCoords)
        tvStatus = findViewById(R.id.tvStatus)
        val btn = findViewById<Button>(R.id.btnSendLocation)

        btn.setOnClickListener {
            checkAndRequestPermissions()
        }
    }

    private fun checkAndRequestPermissions() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val sms = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED

        val needed = mutableListOf<String>()
        if (!fine) needed.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!coarse) needed.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (!sms) needed.add(Manifest.permission.SEND_SMS)

        if (needed.isEmpty()) {
            getCurrentLocation()
        } else {
            requestPermissions.launch(needed.toTypedArray())
        }
    }

    private fun getCurrentLocation() {
        tvStatus.text = "Status: Fetching location..."
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val mapLink = "https://maps.google.com/?q=$lat,$lon"
                    tvCoords.text = "Coordinates: $lat, $lon"
                    sendSMS(mapLink)
                } else {
                    tvStatus.text = "Status: Location unavailable"
                }
            }.addOnFailureListener {
                tvStatus.text = "Status: Failed to get location"
            }
        } catch (e: SecurityException) {
            tvStatus.text = "Status: Permission denied"
        }
    }

    private fun sendSMS(message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(PHONE_NUMBER, null, message, null, null)
            tvStatus.text = "Status: Location sent successfully!"
            Toast.makeText(this, "SMS sent!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            tvStatus.text = "Status: Failed to send SMS"
        }
    }
}
