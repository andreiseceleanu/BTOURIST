package com.modern.btourist.Map


import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.modern.btourist.databinding.FragmentMapBinding
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.nsd.NsdManager
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.google.common.base.Predicates.equalTo
import com.google.common.reflect.Reflection.getPackageName
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.util.Listener
import com.google.gson.Gson
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.modern.btourist.Database.*
import com.modern.btourist.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import org.w3c.dom.Text
import java.io.*
import java.lang.Exception
import java.net.URL
import java.net.URLConnection


class MapFragment : Fragment(),OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnPolylineClickListener {



    lateinit var binding: FragmentMapBinding
    private lateinit var mMapView: MapView
    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

    private var firebaseFirestore = FirebaseFirestore.getInstance()
    private var docRef = firebaseFirestore.document("users/${FirebaseAuth.getInstance().uid
    ?: throw NullPointerException("UID is null.")} ")
    private var colRef = firebaseFirestore.collection("users")
    private var attractionColRef = firebaseFirestore.collection("attractions")
    private var markerMap: HashMap<String,Marker> = HashMap()
    private lateinit var registration: ListenerRegistration
     var markerPlaces: ArrayList<String> = ArrayList()
    var markerEndLocation: ArrayList<String> = ArrayList()
    private val attractionTripMarkerList: ArrayList<Marker> = ArrayList()
    private var mGeoApiContext: GeoApiContext? = null
    private var googleMap: GoogleMap? = null
    var polylines: ArrayList<PolylineData> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

            // Inflate the layout for this fragment
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map,container,false)

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMapView = binding.mapView
        mMapView.onCreate(mapViewBundle)

        mMapView.getMapAsync(this)

        if(mGeoApiContext == null){
            mGeoApiContext = GeoApiContext.Builder()
                .apiKey(getString(R.string.maps_api_key))
                .build()
        }

            return binding.root
    }


    private fun addPolylinesToMap(result:DirectionsResult) {
        var duration = 999999999L
        Handler(Looper.getMainLooper()).post {
            if(polylines.size > 0){
                for(data in polylines){
                    data.polyline.remove()
                }
                polylines.clear()
                polylines = ArrayList()

                for( m in attractionTripMarkerList){
                    m.remove()
                }
            }
            for (route in result.routes) {
                Log.d("Polylines", "run: leg: " + route.legs[0].toString())
                val decodedPath = PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                val newDecodedPath = ArrayList<LatLng>()
                // This loops through all the LatLng coordinates of ONE polyline.
                for (latLng in decodedPath) {
                    // Log.d(TAG, "run: latlng: " + latLng.toString());
                    newDecodedPath.add(LatLng(
                        latLng.lat,
                        latLng.lng
                    ))
                }

                val polyline = googleMap!!.addPolyline(PolylineOptions().addAll(newDecodedPath))
                polyline.color = ContextCompat.getColor(context!!, R.color.lightGreyAccent)
                polyline.isClickable = true
                polylines.add(PolylineData(polyline,route.legs[0]))

                var tempDuration  = route.legs[0].duration.inSeconds
                if(tempDuration < duration){
                    duration = tempDuration
                    onPolylineClick(polyline)
                    zoomRoute(polyline.points)
                }

            }
        }
    }

    private fun calculateDirections(marker:Marker) {
        docRef.get().addOnSuccessListener {

            val user: User? = it.toObject(User::class.java)

            Log.d("DirectionsApi", "calculateDirections: calculating directions.")
            val destination = com.google.maps.model.LatLng(
                marker.position.latitude,
                marker.position.longitude
            )
            val directions = DirectionsApiRequest(mGeoApiContext)
            directions.alternatives(true)
            directions.origin(
                com.google.maps.model.LatLng(user!!.latitude,user.longitude)
            )
            Log.d("DirectionsApi", "calculateDirections: destination: " + destination.toString())
            directions.destination(destination).setCallback(object : PendingResult.Callback<DirectionsResult> {
                override fun onResult(result: DirectionsResult) {
                    addPolylinesToMap(result)
                }

                override fun onFailure(e: Throwable) {
                    Log.e("DirectionsApi", "calculateDirections: Failed to get directions: " + e.message)
                }
            })
        }
    }

    override fun onMapReady(map: GoogleMap) {

        googleMap = map
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, com.modern.btourist.R.raw.mapstyle_night))
        map.setOnInfoWindowClickListener(this)
        map.setOnPolylineClickListener(this)

        attractionColRef.addSnapshotListener(activity as Activity) { querySnapshot, firebaseFirestoreException ->

            if( firebaseFirestoreException!=null){
                Log.e("MapFragment","Attraction snapshot listen failed",firebaseFirestoreException)
            }
            if(querySnapshot!=null) {
                for (document in querySnapshot) {
                    var attractionList = ArrayList<Attraction>()
                    var attraction: Attraction = document.toObject(Attraction::class.java)
                    var gson: Gson = Gson()
                    var attractionInfoString: String = gson.toJson(attraction)

                    fun getIcon(category: String): Int{
                        when(category){
                            "Culture, Architecture and History" -> return R.drawable.museum_gradient
                            "Nature" -> return R.drawable.park_gradient
                            "Nightlife" -> return R.drawable.bar_gradient
                            "Food" -> return R.drawable.restaurant_gradient
                            "Shopping" -> return R.drawable.shop_gradient
                        }
                        return 0
                    }

                    var icon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(getIcon(attraction.category))
                    var markerOptionsAttraction: MarkerOptions =
                        MarkerOptions().position(LatLng(attraction.latitude, attraction.longitude))
                            .title(attraction.name)
                            .snippet(attractionInfoString)
                            .icon(icon)

                    var attractionMarker = map.addMarker(markerOptionsAttraction)
                    markerPlaces.add(attractionMarker.id)
                    attractionList.add(attraction)
                }
            }
        }


        docRef.get().addOnSuccessListener(activity as Activity) {
            try {
                var bundle = MapFragmentArgs.fromBundle(arguments!!)
                if(arguments!=null){
                    var attrDoc = attractionColRef.whereEqualTo("name",bundle.name)
                    attrDoc.get().addOnSuccessListener {snapshot: QuerySnapshot ->
                        var attr = snapshot.toObjects(Attraction::class.java)
                        var attraction = attr.get(0)
                        var loc: LatLng = LatLng(attraction.latitude,attraction.longitude)

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,17F))

                    }

                }else{

                }
            }catch (e: Exception){
                e.printStackTrace()
            }

            val user: User? = it.toObject(User::class.java)
            var latLan: LatLng = LatLng(user!!.latitude, user.longitude)

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLan, 15F))

        }

        registration = colRef.addSnapshotListener(activity as Activity) { querySnapshot, firebaseFirestoreException ->

            if( firebaseFirestoreException!=null){
                Log.e("MapFragment","Snapshot listen failed",firebaseFirestoreException)
            }
            if(querySnapshot!=null){


                for(document in querySnapshot) {
                    var user: User = document.toObject(User::class.java)

                    var ref: StorageReference = StorageUtil.pathToReference(user.profilePicturePath!!)

                    var myImage: Bitmap
                    var localFile: File = File.createTempFile("Images", "jpeg")
                    ref.getFile(localFile).addOnSuccessListener(activity as Activity) {
                        myImage = BitmapFactory.decodeFile(localFile.absolutePath)

                        //resize marker icon
                        var resized: Bitmap = Bitmap.createScaledBitmap(myImage, 128, 128, true)

                        // Create a rounded corners bitmap
                        myImage = getRoundedBitmap(resized, 150F)


                        var previousMarker: Marker? = markerMap[user.email]
                        try{var gson: Gson = Gson()
                            var userInfoString: String = gson.toJson(user)
                        if (previousMarker != null) {
                            var latLng = LatLng(user.latitude, user.longitude)

                            if (FirebaseAuth.getInstance().currentUser!!.email == user.email) {
                                // Add a border around rounded corners bitmap
                                myImage = addBorderToRoundedBitmap(myImage, 150F, 10F, Color.rgb(216, 120, 45))

                                // Add a border around rounded corners bitmap as shadow
                                myImage = addBorderToRoundedBitmap(myImage, 150F, 3F, Color.LTGRAY)

                                previousMarker.setIcon(BitmapDescriptorFactory.fromBitmap(myImage))
                                previousMarker.position = latLng

                            } else {

                                myImage = addBorderToRoundedBitmap(myImage, 150F, 10F, Color.WHITE)

                                // Add a border around rounded corners bitmap as shadow
                                myImage = addBorderToRoundedBitmap(myImage, 150F, 3F, Color.LTGRAY)
                                previousMarker.setIcon(BitmapDescriptorFactory.fromBitmap(myImage))
                                previousMarker.position = latLng
                            }
                        } else {

                            if (FirebaseAuth.getInstance().currentUser!!.email == user.email) {


                                myImage = addBorderToRoundedBitmap(myImage, 150F, 10F, Color.rgb(216, 120, 45))

                                // Add a border around rounded corners bitmap as shadow
                                myImage = addBorderToRoundedBitmap(myImage, 150F, 3F, Color.LTGRAY)
                                var markerOptionsMyUser: MarkerOptions =
                                    MarkerOptions().position(LatLng(user!!.latitude, user.longitude))
                                        .title(user.firstName)
                                        .snippet(userInfoString)
                                        .icon(BitmapDescriptorFactory.fromBitmap(myImage))
                                        .zIndex(1F)

                                var marker = map.addMarker(markerOptionsMyUser)
                                markerMap[user.email] = marker
                            } else {

                                // Add a border around rounded corners bitmap
                                myImage = addBorderToRoundedBitmap(myImage, 150F, 10F, Color.WHITE)

                                // Add a border around rounded corners bitmap as shadow
                                myImage = addBorderToRoundedBitmap(myImage, 150F, 3F, Color.LTGRAY)
                                var markerOptionsUsers: MarkerOptions =
                                    MarkerOptions().position(LatLng(user!!.latitude, user.longitude))
                                        .title(user.firstName)
                                        .snippet(userInfoString)
                                        .icon(BitmapDescriptorFactory.fromBitmap(myImage))

                                var marker = map.addMarker(markerOptionsUsers)
                                markerMap[user.email] = marker
                            }
                        }
                    }catch (e : KotlinNullPointerException){
                            Log.d("MapFragment","KotlinNullPointerException catched")
                            e.printStackTrace()
                        }
                        }


                    map.setInfoWindowAdapter(object: GoogleMap.InfoWindowAdapter {

                        override fun getInfoWindow(marker: Marker): View? {

                            return null
                        }

                        override fun getInfoContents(marker: Marker): View {

                            if(markerEndLocation.contains(marker.id)){
                                val view = (context as Activity).layoutInflater
                                    .inflate(R.layout.custom_end_location_info_window, null)

                                val titleText = view.findViewById<TextView>(R.id.titleText)
                                val snippetText = view.findViewById<TextView>(R.id.snippetText)

                                titleText.text = marker.title
                                snippetText.text = marker.snippet

                                return view
                            }else if (markerPlaces.contains(marker.id)) {

                                fun getImage(imageName: String): Int {

                                    var app = Btourist.instance.applicationContext

                                    var drawableResourceId = app.resources.getIdentifier(imageName, "drawable", app.packageName)

                                    return drawableResourceId
                                }


                                val view = (context as Activity).layoutInflater
                                    .inflate(R.layout.custom_info_window_attraction, null)

                                val nameText = view.findViewById<TextView>(R.id.attractionNameText)
                                val categoryText = view.findViewById<TextView>(R.id.attractionCategoryText)
                                val descriptorText = view.findViewById<TextView>(R.id.attractionDescriptionText)
                                val phoneText = view.findViewById<TextView>(R.id.attractionPhoneText)
                                val websiteText = view.findViewById<TextView>(R.id.attractionWebsiteText)
                                val attrImage = view.findViewById<ImageView>(R.id.attractionImage)

                                var gson = Gson()
                                var attraction: Attraction = gson.fromJson(marker.snippet, Attraction::class.java)

                                var resource = getImage(attraction.image)
                                nameText.text = attraction.name
                                categoryText.text = attraction.category+"\n"+"\n"+"Touch to Navigate to Location"
                                var description = attraction.description
                                descriptorText.text = description
                                phoneText.text ="Phone: "+ attraction.phone.toString()
                                websiteText.text = "Website: "+ attraction.website

                                attrImage.setImageResource(resource)

                                return view
                            }else {
                                val view = (context as Activity).layoutInflater
                                    .inflate(R.layout.custom_info_window, null)

                                val name_tv = view.findViewById<TextView>(R.id.nameText)
                                val age_tv = view.findViewById<TextView>(R.id.ageTextView)
                                val phone_tv = view.findViewById<TextView>(R.id.phoneText)

                                val interest1_tv = view.findViewById<TextView>(R.id.interestText1)
                                val interest2_tv = view.findViewById<TextView>(R.id.interestText2)
                                val interest3_tv = view.findViewById<TextView>(R.id.interestText3)

                                val language1_tv = view.findViewById<TextView>(R.id.languageText1)
                                val language2_tv = view.findViewById<TextView>(R.id.languageText2)

                                val userText = view.findViewById<TextView>(R.id.fullNameText)

                                var gson: Gson = Gson()
                                var user: User = gson.fromJson(marker.snippet, User::class.java)
                                var fullName: String = user.firstName + " " + user.lastName
                                userText.text = fullName
                                name_tv.text = user.sex
                                var ageString: String = user.age.toString()
                                age_tv.text = ageString
                                phone_tv.text = user.phone.toString()
                                interest1_tv.text = user.interest1
                                interest2_tv.text = user.interest2
                                interest3_tv.text = user.interest3
                                language1_tv.text = user.language1
                                language2_tv.text = user.language2

                                return view
                            }
                            }

                    })

                    }

                }


        }
        val button = binding.endTripButton
        button.setOnClickListener {
            for(data in polylines){
                data.polyline.remove()
            }
            polylines.clear()
            polylines = ArrayList()

            for( m in attractionTripMarkerList){
                m.remove()
            }
            button.visibility = View.GONE
        }
    }

    fun zoomRoute(lstLatLngRoute:List<LatLng>) {
        if (googleMap ==
            null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return
        val boundsBuilder = LatLngBounds.Builder()
        for (latLngPoint in lstLatLngRoute)
            boundsBuilder.include(latLngPoint)
        val routePadding = 120
        val latLngBounds = boundsBuilder.build()
        googleMap!!.animateCamera(
            CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
            600, null
        )
    }

    override fun onInfoWindowClick(p0: Marker?) {
        Log.d("InfoClick", "Infow window clicked")
        if (markerPlaces.contains(p0!!.id)) {
            var builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setMessage("Set route to Attraction?")
                .setCancelable(true)
                .setPositiveButton("Yes"){dialog, which ->
                    calculateDirections(p0!!)
                    Snackbar.make(view!!,"Select Preferred Route by clicking on it", Snackbar.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){dialog, which ->
                    dialog.cancel()
                }
            var alert: AlertDialog = builder.create()
            alert.show()
        }
    }

    override fun onPolylineClick(polyline: Polyline?) {
        var index = 0
        for (polylineData in polylines)
        { index++
            if (polyline!!.id == polylineData.polyline.id)
            { polylineData.polyline.color = ContextCompat.getColor(activity as Activity, R.color.light_blue)
                polylineData.polyline.zIndex = 1F
                val endLocation = LatLng(
                    polylineData.leg.endLocation.lat,
                    polylineData.leg.endLocation.lng
                )

                val marker: Marker = googleMap!!.addMarker(MarkerOptions()
                    .position(endLocation)
                    .title("Trip: NR"+index)
                    .snippet("Duration: "+polylineData.leg.duration)
                )
                markerEndLocation.add(marker.id)
                attractionTripMarkerList.add(marker)
                marker.showInfoWindow()
                binding.endTripButton.visibility = View.VISIBLE

            }
            else
            { polylineData.polyline.color = ContextCompat.getColor(activity as Activity, R.color.lightGreyAccent)
                polylineData.polyline.zIndex = 0F
            }
        }

    }

    fun getRoundedBitmap(srcBitmap: Bitmap, cornerRadius: Float): Bitmap {
        // Initialize a new instance of Bitmap
        var dstBitmap: Bitmap = Bitmap.createBitmap(
            srcBitmap.width, // Width
            srcBitmap.height, // Height

            Bitmap.Config.ARGB_8888 // Config
        )

        var canvas: Canvas = Canvas(dstBitmap)

        // Initialize a new Paint instance
        var paint = Paint()
        paint.isAntiAlias = true

        // Initialize a new Rect instance
        var rect = Rect(0, 0, srcBitmap.width, srcBitmap.height)

        // Initialize a new RectF instance
        var rectF = RectF(rect)

        // Draw a rounded rectangle object on canvas
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Make a rounded image by copying at the exact center position of source image
        canvas.drawBitmap(srcBitmap, 0F, 0F, paint)

        // Free the native object associated with this bitmap.
        srcBitmap.recycle()

        // Return the circular bitmap
        return dstBitmap
    }

   fun addBorderToRoundedBitmap(srcBitmap: Bitmap, cornerRadius: Float,borderWidth: Float,borderColor: Int) : Bitmap{
        // We will hide half border by bitmap
        var borderWidth = borderWidth*2

        // Initialize a new Bitmap to make it bordered rounded bitmap
        var dstBitmap: Bitmap = Bitmap.createBitmap(
                srcBitmap.width + borderWidth.toInt(), // Width
                srcBitmap.height + borderWidth.toInt(), // Height
                Bitmap.Config.ARGB_8888 // Config
        );

        // Initialize a new Canvas instance
        var canvas = Canvas(dstBitmap)

        // Initialize a new Paint instance to draw border
        var paint = Paint()
       paint.color = borderColor
       paint.style = Paint.Style.STROKE
       paint.strokeWidth = borderWidth
       paint.isAntiAlias = true

        // Initialize a new Rect instance
         var rect = Rect(
                borderWidth.toInt()/2,
                borderWidth.toInt()/2,
                dstBitmap.getWidth() - borderWidth.toInt()/2,
                dstBitmap.getHeight() - borderWidth.toInt()/2
        )

        // Initialize a new instance of RectF
        var rectF = RectF(rect)

        // Draw rounded rectangle as a border/shadow on canvas
        canvas.drawRoundRect(rectF,cornerRadius,cornerRadius,paint)

        // Draw source bitmap to canvas
        canvas.drawBitmap(srcBitmap, borderWidth / 2, borderWidth / 2, null)

        srcBitmap.recycle()

        // Return the bordered circular bitmap
        return dstBitmap
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }

        mMapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mMapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        //markerMap.clear()
        mMapView.onStop()
    }


    override fun onPause() {
        mMapView.onPause()
        super.onPause()
        //markerMap.clear()
    }

    override fun onDestroy() {
        mMapView.onDestroy()
        registration.remove()
        markerMap.clear()
        super.onDestroy()

    }

    override fun onLowMemory() {
        super.onLowMemory()

        mMapView.onLowMemory()
    }

}
