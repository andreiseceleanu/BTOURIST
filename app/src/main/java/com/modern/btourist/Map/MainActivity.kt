@file:Suppress("DEPRECATION")

package com.modern.btourist.Map

import android.Manifest
import android.app.ActivityManager
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.modern.btourist.Database.FirestoreUtil
import com.modern.btourist.LoginRegister.LoginActivity
import com.modern.btourist.R
import com.modern.btourist.Services.LocationService
import com.modern.btourist.databinding.ActivityMainBinding
import java.text.DateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    val ERROR_DIALOG_REQUEST: Int = 1001
    val PERMISSIONS_REQUEST_ENABLE_GPS: Int = 1002
    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: Int = 1003
    private lateinit var binding: ActivityMainBinding
    private var mLocationPermissionGranted: Boolean = false
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        FirestoreUtil.updateCurrentUser("","","",0L,"","","",0,"",
            "","",null,0.0,0.0)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this )

        var bottomNav: BottomNavigationView = findViewById(R.id.bottomNav)
        var host: NavHostFragment? = supportFragmentManager.findFragmentById(R.id.navHostMain) as NavHostFragment?
        navController = host!!.navController

        bottomNav.setOnNavigationItemSelectedListener(
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.bottomMapButton -> {
                        navController.navigate(R.id.mapFragment, null)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.bottonGuidesButton -> {
                        var bundle: Bundle = Bundle()
                        bundle.putString("userFullName","")
                        navController.navigate(R.id.guidesFragment,bundle)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.bottomAttractionsButton -> {
                        navController.navigate(R.id.categoriesFragment, null)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.bottomInfoButton -> {
                        navController.navigate(R.id.infoListFragment, null)
                        return@OnNavigationItemSelectedListener true
                    }

                }
                false
            })

    }



    private fun getLastKnownLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            return
        }

        startLocationService()
    }

    private fun checkMapServices(): Boolean {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true
            }
        }
        return false
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                val enableGpsIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
            }
        val alert = builder.create()
        alert.show()
    }

    fun isMapsEnabled(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            return false
        }
        return true
    }

    private fun getLocationPermission() {

        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
            getLastKnownLocation()

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )

        }
    }

    fun isServicesOK(): Boolean {
        d("MainActivity", "isServicesOK: checking google services version")

        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MainActivity)

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            d("MainActivity", "isServicesOK: Google Services is working")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            d("MainActivity", "isServicesOK: an error occured but we can fix it")
            val dialog =
                GoogleApiAvailability.getInstance().getErrorDialog(this@MainActivity, available, ERROR_DIALOG_REQUEST)
            dialog.show()
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(checkMapServices()){
            if(mLocationPermissionGranted)
                getLastKnownLocation()

            else {getLocationPermission()

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        d("MainActivity", "onActivityResult: called.")
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                if (mLocationPermissionGranted) {

                    getLastKnownLocation()

                } else {
                    getLocationPermission()

                }
            }
        }

    }

    //Create overflow menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    //Log out selected -> Set AUTHENTIFICATED=false in Shared Preferences
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item!!.itemId== R.id.logOutItem){
            val PREFS_FILENAME = "com.modern.btourist.prefs"
            val prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
            val editor = prefs.edit()
            editor.putBoolean("AUTHENTIFICATED",false)
            editor.putBoolean("joined",false)
            editor.apply()

            //Start login activity, Close main activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
            FirebaseAuth.getInstance().signOut()

        }
        if(item!!.itemId==R.id.historyItem){
            navController.navigate(R.id.visitedFragment)
        }
        return super.onOptionsItemSelected(item)
    }
    private fun startLocationService(){
        if(!isLocationServiceRunning()){
            var serviceIntent = Intent(this,LocationService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                this.startForegroundService(serviceIntent)
            }else{
                startService(serviceIntent)
            }
        }
    }

    private fun isLocationServiceRunning():Boolean {
        var manager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.modern.btourist.Services.LocationService" == service.service.className) {
                Log.d("MainActivity", "isLocationServiceRunning: location service is already running.")
                return true
            }
        }
        Log.d("MainActivity", "isLocationServiceRunning: location service is not running.")
        return false
    }


}
