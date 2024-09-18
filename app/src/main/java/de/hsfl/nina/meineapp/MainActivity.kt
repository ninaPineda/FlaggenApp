package de.hsfl.nina.meineapp

import android.content.Intent
import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity() {
    val mainViewModel : MainViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startLocation()
            } else {
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        setContentView(R.layout.activity_main)
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Lade MapPointsList
        val jsonMapPointsList = sharedPreferences.getString("MapPointsList", null)
        val typeMapPointsList = object : TypeToken<MutableList<MapPoint>>() {}.type
        val mapPointsList = gson.fromJson<MutableList<MapPoint>>(jsonMapPointsList, typeMapPointsList)

        // Lade HighscoreList
        val jsonHighscoreList = sharedPreferences.getString("HighscoreList", null)
        val typeHighscoreList = object : TypeToken<MutableList<Highscore>>() {}.type
        val highscoreList = gson.fromJson<MutableList<Highscore>>(jsonHighscoreList, typeHighscoreList)

        // Füge die geladenen Listen dem ViewModel hinzu
        if(jsonMapPointsList != null && jsonHighscoreList != null){
            mainViewModel.setMapPointsList(mapPointsList ?: mutableListOf())
            mainViewModel.setHighscoreList(highscoreList ?: mutableListOf())
        }




        startLocation();
    }

    override fun onResume() {
        super.onResume()
        checkLocationSettings()
        startLocation()
    }

    override fun onStop() {
        super.onStop()
        stopLocation()
        val editor = sharedPreferences.edit()
        val json1 = gson.toJson(mainViewModel.getMapPointsList().value)
        val json2 = gson.toJson(mainViewModel.getHighscoreList().value)
        editor.putString("MapPointsList", json1)
        editor.putString("HighscoreList", json2)
        editor.apply()
    }


    private fun startLocation() {
        Log.d("Location", "Location updates started.")
        // Initialize FusedLocationProviderClient, LocationRequest, and LocationCallback
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0?.lastLocation?.let { location ->
                    mainViewModel.setLocation(location)
                }
            }
        }

        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            Log.d("Location", "Location updates started.")
        } else {
            requestLocationPermission()
        }
    }


    private fun stopLocation() {
        Log.d("Location", "Location updates stopped.")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(
                this,
                "Diese App funktioniert ohne deinen Standort nicht sorry :D.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkLocationSettings() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (locationEnabled) {
            Log.d("Location", "Location services are enabled.")
        } else {
            Toast.makeText(
                this,
                "Bitte schalte die Standortbestimmung ein!.",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }
}