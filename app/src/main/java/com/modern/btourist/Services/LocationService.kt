package com.modern.btourist.Services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.modern.btourist.Database.FirestoreUtil


class LocationService : Service() {

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()

            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: called.")
        getLocation()
        return Service.START_NOT_STICKY
    }

    private fun getLocation() {

        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        val mLocationRequestHighAccuracy = LocationRequest()
        mLocationRequestHighAccuracy.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequestHighAccuracy.interval = UPDATE_INTERVAL
        mLocationRequestHighAccuracy.fastestInterval = FASTEST_INTERVAL


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "getLocation: stopping the location service.")
            stopForeground(true)
            stopSelf()
            return
        }
        Log.d(TAG, "getLocation: getting location information.")
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequestHighAccuracy, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {

                    Log.d(TAG, "onLocationResult: got location result.")

                    val location = locationResult!!.lastLocation

                    if (location != null) {

                        val latitude = location.latitude
                        val longitude = location.longitude
                        try{
                        FirestoreUtil.updateCurrentUser("","","",0L,"","","",0,"","","",null,latitude,longitude)
                        }catch (e: NullPointerException) {
                            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.")
                            Log.e(TAG, "saveUserLocation: NullPointerException: " + e.message)
                            stopForeground(true)
                            stopSelf()
                        }

                    }
                }
            },
            Looper.myLooper()
        ) // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

    /*private fun saveUserLocation(userLocation: User) {

        try {
            val locationRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().uid!!)

            FirestoreUtil.updateCurrentUser(userLocation)
            locationRef.set(userLocation).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(
                        TAG, "onComplete: \ninserted user location into database." +
                                "\n latitude: " + userLocation.latitude +
                                "\n longitude: " + userLocation.longitude
                    )
                }
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.")
            Log.e(TAG, "saveUserLocation: NullPointerException: " + e.message)
            stopSelf()
        }

    }*/

    companion object {

        private val TAG = "LocationService"
        private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 4 secs */
        private val FASTEST_INTERVAL: Long = 1000 /* 2 sec */
    }


}