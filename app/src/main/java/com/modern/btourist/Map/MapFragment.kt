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
import com.google.firebase.storage.StorageReference
import com.modern.btourist.databinding.FragmentMapBinding
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.net.nsd.NsdManager
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.webkit.WebStorage
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
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
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.*
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
import kotlinx.android.synthetic.main.fragment_map.*
import org.w3c.dom.Text
import java.io.*
import java.lang.ClassCastException
import java.lang.RuntimeException
import java.lang.reflect.Array
import java.net.URL
import java.net.URLConnection
import java.util.NoSuchElementException
import kotlin.Exception


class MapFragment : Fragment(),OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnPolylineClickListener {

    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

    private var firebaseFirestore = FirebaseFirestore.getInstance()
    private var colRef = firebaseFirestore.collection("users")
    private var attractionColRef = firebaseFirestore.collection("attractions")
    private var guideColRef = firebaseFirestore.collection("guides")
    private var docRef = firebaseFirestore.document("users/${FirebaseAuth.getInstance().uid
        ?: throw NullPointerException("UID is null.")} ")
    private lateinit var registration: ListenerRegistration

    lateinit var binding: FragmentMapBinding
    private lateinit var mMapView: MapView

    private var markerMap: HashMap<String,Marker> = HashMap()
    var markerPlaces: ArrayList<String> = ArrayList()
    var markerEndLocation: ArrayList<String> = ArrayList()
    private val attractionTripMarkerList: ArrayList<Marker> = ArrayList()
    private var mGeoApiContext: GeoApiContext? = null
    private lateinit var googleMap: GoogleMap
    var polylines: ArrayList<PolylineData> = ArrayList()
    var guidesPolylines: ArrayList<PolylineData> = ArrayList()


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

            var prefs: SharedPreferences = activity!!.getSharedPreferences("com.modern.btourist.prefs",0)
            var editor = prefs.edit()
            var joined = prefs.getBoolean("joined",false)
            var owner = prefs.getString("owner","")
            var guideId = prefs.getString("guideId","")

            var gson:Gson = Gson()
            var listType = object: TypeToken<MutableList<String>>() {}.type

        lateinit var attractionList:MutableList<String>
        try {
            if(prefs.getString("attractionSet", null)!=null){
                var attractionString: String = prefs.getString("attractionSet", null)
                attractionList = gson.fromJson(attractionString, listType)
                Log.d("attr",attractionList.toString())
            }
        }catch (ex: ClassCastException){
            editor.remove("attractionSet")
            editor.putBoolean("joined",false)
            editor.apply()
        }
        editor.putBoolean("joined",false)
        editor.apply()
            var exitGuideButton = binding.exitGuideButton
            var visitingText = binding.visitingTextView
            var nowText = binding.textView9
            var nextButton = binding.nextButton
            var endButton = binding.finishButton

            FirestoreUtil.getCurrentUser {
                    user ->
                var ownerGuideId = prefs.getString("ownerGuideId","")
                if(owner==user!!.fullName){
                    if(joined){
                    visitingText.visibility = View.VISIBLE
                    nowText.visibility = View.VISIBLE
                    nextButton.visibility = View.VISIBLE
                    endButton.visibility = View.VISIBLE


                            visitingText.text = attractionList.first().toString()

                            var attractionsObjectsList: ArrayList<Attraction> = ArrayList()
                            docRef.get().addOnSuccessListener {user->
                                attractionColRef.get().addOnSuccessListener { attractions ->
                                    val user: User? = user.toObject(User::class.java)

                                    for (attraction in attractions) {
                                        var attractionObject = attraction.toObject(Attraction::class.java)
                                        for (a in attractionList) {
                                            if (attractionObject.name == a) {
                                                attractionsObjectsList.add(attractionObject)
                                            }
                                        }
                                        for (i in attractionsObjectsList.indices) {
                                            if (i < attractionsObjectsList.size - 1) {
                                                var origin = com.google.maps.model.LatLng(
                                                    attractionsObjectsList[i].latitude,
                                                    attractionsObjectsList[i].longitude
                                                )

                                                var destination = com.google.maps.model.LatLng(
                                                    attractionsObjectsList[i + 1].latitude, attractionsObjectsList[i + 1].longitude
                                                )

                                                calculateDirectionsGuide(origin,destination,false,1)
                                                calculateDirectionsGuide(com.google.maps.model.LatLng(user!!.latitude,user.longitude),
                                                    com.google.maps.model.LatLng(attractionsObjectsList[0].latitude,attractionsObjectsList[0].longitude),true,0)
                                            }

                                        }



                                    }

                                }
                            }


                    finishButton.setOnClickListener{
                        visitingText.visibility = View.INVISIBLE
                        nowText.visibility = View.INVISIBLE
                        nextButton.visibility = View.INVISIBLE
                        endButton.visibility = View.INVISIBLE

                        editor.putBoolean("joined", false)
                        editor.remove("attractionSet")
                        Log.d("SetuMasii",prefs.getString("attractionSet","nothing"))
                        editor.remove("AttractionList")
                        editor.apply()
                        if(guidesPolylines.size > 0){
                            for(data in guidesPolylines){
                                data.polyline.remove()
                            }
                            guidesPolylines.clear()
                            guidesPolylines = ArrayList()

                        }

                        Log.d("guideId",ownerGuideId)
                        firebaseFirestore.collection("guides").document(ownerGuideId!!).delete().addOnSuccessListener {
                            Log.d("DeleteGuide", "DocumentSnapshot successfully deleted!")
                        }
                    }

                        nextButton.setOnClickListener {
                            try {  docRef.update("visitedList",FieldValue.arrayUnion(attractionList.first()))
                                firebaseFirestore.collection("guides").whereEqualTo("owner",owner).addSnapshotListener {it,exception->
                                    if(exception!=null){

                                    }else {
                                        if (it!=null&&it.size()!=0) {
                                            var guide = it!!.first().toObject(Guide::class.java)
                                            var userList = guide.usersArray!!.toMutableList()
                                            for (u in userList) {
                                                firebaseFirestore.collection("users").whereEqualTo("fullName", u)
                                                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                                        var document = querySnapshot!!.first()
                                                        var userId = document.id
                                                        if (attractionList.isNotEmpty()) {
                                                            firebaseFirestore.collection("users").document(userId)
                                                                .update(
                                                                    "visitedList",
                                                                    FieldValue.arrayUnion(attractionList.first())
                                                                )
                                                        }
                                                    }
                                            }
                                        }
                                    }
                                }
                                if(attractionList.size!=1&&attractionList.size!=2&&attractionList.isNotEmpty()) {

                                    firebaseFirestore.collection("guides").document(ownerGuideId!!).update(
                                        "attractionsArray",
                                        FieldValue.arrayRemove(attractionList.first().toString())
                                    )
                                    attractionList.remove(attractionList.first().toString())
                                    editor.remove("attractionSet")
                                    editor.putString("attractionSet",gson.toJson(attractionList))
                                    editor.apply()

                                    if(guidesPolylines.size > 0){
                                        for(data in guidesPolylines){
                                            data.polyline.remove()
                                        }
                                        guidesPolylines.clear()
                                        guidesPolylines = ArrayList()

                                    }
                                    visitingText.text = attractionList.first().toString()
                                    var attractionsObjectsList: ArrayList<Attraction> = ArrayList()
                                    docRef.get().addOnSuccessListener {user->
                                        attractionColRef.get().addOnSuccessListener { attractions ->
                                            val user: User? = user.toObject(User::class.java)

                                            for (attraction in attractions) {
                                                var attractionObject = attraction.toObject(Attraction::class.java)
                                                for (a in attractionList) {
                                                    if (attractionObject.name == a) {
                                                        attractionsObjectsList.add(attractionObject)
                                                    }
                                                }
                                                for (i in attractionsObjectsList.indices) {
                                                    if (i < attractionsObjectsList.size-1) {
                                                        var origin = com.google.maps.model.LatLng(
                                                            attractionsObjectsList[i].latitude,
                                                            attractionsObjectsList[i].longitude
                                                        )

                                                        var destination = com.google.maps.model.LatLng(
                                                            attractionsObjectsList[i + 1].latitude, attractionsObjectsList[i + 1].longitude
                                                        )

                                                        calculateDirectionsGuide(origin,destination,false,1)
                                                        calculateDirectionsGuide(com.google.maps.model.LatLng(user!!.latitude,user.longitude),
                                                            com.google.maps.model.LatLng(attractionsObjectsList[0].latitude,attractionsObjectsList[0].longitude),true,0)

                                                    }

                                                }


                                            }

                                        }

                                    }
                                }else if(attractionList.size==2){
                                    visitingText.text = attractionList.last().toString()
                                    firebaseFirestore.collection("guides").document(ownerGuideId!!).update(
                                        "attractionsArray",
                                        FieldValue.arrayRemove(attractionList.first().toString())
                                    )
                                    attractionList.remove(attractionList.first().toString())
                                    editor.remove("attractionSet")
                                    editor.putString("attractionSet",gson.toJson(attractionList))
                                    editor.apply()
                                    if(guidesPolylines.size > 0){
                                        for(data in guidesPolylines){
                                            data.polyline.remove()
                                        }
                                        guidesPolylines.clear()
                                        guidesPolylines = ArrayList()

                                    }
                                    calculateDirectionsGuide(com.google.maps.model.LatLng(user!!.latitude,user.longitude),
                                        com.google.maps.model.LatLng(attractionsObjectsList.last().latitude,attractionsObjectsList.last().longitude),true,0)
                                }else if(attractionList.size==1){
                                    firebaseFirestore.collection("guides").document(ownerGuideId!!).update(
                                        "attractionsArray",
                                        FieldValue.arrayRemove(attractionList.first().toString())
                                    )
                                    attractionList.remove(attractionList.first().toString())
                                    attractionList.clear()
                                    editor.remove("attractionSet")
                                    editor.apply()
                                    Snackbar.make(view!!,"This is the last Attraction. Press Finish to end guide",Snackbar.LENGTH_LONG).show()
                                }else if(attractionList.isEmpty()){
                                    editor.remove("attractionSet")
                                    editor.apply()
                                }
                            }catch (e: NoSuchElementException){
                                Log.e("NoSuchElement",e.stackTrace.toString())
                            }


                        }



                    }

                }else {
                    if (joined) {

                        exitGuideButton.visibility = View.VISIBLE
                        visitingText.visibility = View.VISIBLE
                        nowText.visibility = View.VISIBLE
                        editor.remove("attractionSet")
                        editor.remove("AttractionList")
                        editor.apply()


                        var attractionsObjectsList: ArrayList<Attraction> = ArrayList()
                        docRef.get().addOnSuccessListener {user->
                            guideColRef.document(guideId).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                                if(firebaseFirestoreException!=null){

                                }else{
                                    if(documentSnapshot!=null&&documentSnapshot.exists()){

                                    var guideObject = documentSnapshot.toObject(Guide::class.java)
                                       var attractionListJoined = guideObject!!.attractionsArray!!.toMutableList()
                                        if(attractionListJoined.isNotEmpty()){
                                                val user: User? = user.toObject(User::class.java)

                                            if(guidesPolylines.size > 0){
                                                for(data in guidesPolylines){
                                                    data.polyline.remove()
                                                }
                                                guidesPolylines.clear()
                                                guidesPolylines = ArrayList()

                                            }
                                                    attractionsObjectsList = ArrayList()
                                                    visitingText.text = attractionListJoined.first().toString()
                                                    for(attraction in attractionListJoined){

                                                        attractionColRef.whereEqualTo("name",attraction).addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                                                            var attr = querySnapshot!!.first().toObject(Attraction::class.java)
                                                            attractionsObjectsList.add(attr)


                                                            for (i in attractionsObjectsList.indices) {
                                                                if (i < attractionsObjectsList.size - 1) {
                                                                    var origin = com.google.maps.model.LatLng(
                                                                        attractionsObjectsList[i].latitude,
                                                                        attractionsObjectsList[i].longitude
                                                                    )

                                                                    var destination = com.google.maps.model.LatLng(
                                                                        attractionsObjectsList[i + 1].latitude, attractionsObjectsList[i + 1].longitude
                                                                    )

                                                                    calculateDirectionsGuide(origin,destination,false,1)

                                                                }

                                                            }
                                                            calculateDirectionsGuide(com.google.maps.model.LatLng(user!!.latitude,user.longitude),
                                                                com.google.maps.model.LatLng(attractionsObjectsList.first().latitude,attractionsObjectsList.first().longitude),true,0)
                                                        }
                                                    }
                                        }

                                    }else{
                                        exitGuideButton.visibility = View.INVISIBLE
                                        visitingText.visibility = View.INVISIBLE
                                        nowText.visibility = View.INVISIBLE
                                        visitingText.visibility = View.INVISIBLE

                                        editor.putBoolean("joined", false)
                                        editor.remove("attractionSet")
                                        editor.remove("AttractionList")
                                        editor.apply()

                                        if(guidesPolylines.size > 0){
                                            for(data in guidesPolylines){
                                                data.polyline.remove()
                                            }
                                            guidesPolylines.clear()
                                            guidesPolylines = ArrayList()

                                        }
                                    }

                                }

                            }
                        }

                        docRef.get().addOnCompleteListener{
                            if(it.isSuccessful)
                            {
                                var document = it.result
                                if(document!!.exists()){

                                }else{
                                    exitGuideButton.visibility = View.INVISIBLE
                                    visitingText.visibility = View.INVISIBLE
                                    nowText.visibility = View.INVISIBLE
                                    visitingText.visibility = View.INVISIBLE

                                    editor.putBoolean("joined", false)
                                    editor.remove("attractionSet")
                                    editor.remove("AttractionList")
                                    editor.apply()

                                    if(guidesPolylines.size > 0){
                                        for(data in guidesPolylines){
                                            data.polyline.remove()
                                        }
                                        guidesPolylines.clear()
                                        guidesPolylines = ArrayList()

                                    }
                                }
                            }
                        }

                        exitGuideButton.setOnClickListener {
                            exitGuideButton.visibility = View.INVISIBLE
                            visitingText.visibility = View.INVISIBLE
                            nowText.visibility = View.INVISIBLE
                            visitingText.visibility = View.INVISIBLE

                            editor.putBoolean("joined", false)
                            editor.remove("attractionSet")
                            editor.remove("AttractionList")
                            editor.apply()

                            if(guidesPolylines.size > 0){
                                for(data in guidesPolylines){
                                    data.polyline.remove()
                                }
                                guidesPolylines.clear()
                                guidesPolylines = ArrayList()

                            }

                            FirestoreUtil.getCurrentUser { user ->
                                firebaseFirestore.collection("guides").document(guideId!!).update("usersArray", FieldValue.arrayRemove(user!!.fullName))
                        }


                            }

                    }
                }
            }



            return binding.root
    }


    private fun addPolylinesGuide(result: DirectionsResult, zoom:Boolean, i: Int){
        var duration = 999999999L
        Handler(Looper.getMainLooper()).post {

            var route = result.routes.first()

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
                if(i==0){
                    polyline.color = ContextCompat.getColor(context!!, R.color.light_blue)
                }else{
                    polyline.color = ContextCompat.getColor(context!!, R.color.lightGreyAccent)
                }
                polyline.isClickable = true
            guidesPolylines.add(PolylineData(polyline,route.legs[0]))

                var tempDuration  = route.legs[0].duration.inSeconds
                if(tempDuration < duration){
                    duration = tempDuration
                    if(zoom){
                    zoomRoute(polyline.points)
                    }
                }


        }
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


    private fun calculateDirectionsGuide(origin: com.google.maps.model.LatLng,destination:com.google.maps.model.LatLng, zoom:Boolean, i: Int) {

        val directions = DirectionsApiRequest(mGeoApiContext)

        directions.origin(origin)

        directions.destination(destination).setCallback(object : PendingResult.Callback<DirectionsResult> {
            override fun onResult(result: DirectionsResult) {
                addPolylinesGuide(result, zoom, i)
            }

            override fun onFailure(e: Throwable) {
                Log.e("DirectionsApi", "calculateDirections: Failed to get directions: " + e.message)
            }

        })
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
                    if(bundle.name!=""){
                    var attrDoc = attractionColRef.whereEqualTo("name",bundle.name)
                    attrDoc.get().addOnSuccessListener { snapshot: QuerySnapshot ->
                        var attr = snapshot.toObjects(Attraction::class.java)
                        var attraction = attr.get(0)
                        var loc: LatLng = LatLng(attraction.latitude, attraction.longitude)

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 17F))
                    }
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
                        val options = BitmapFactory.Options()
                        options.inSampleSize = 8
                        myImage = BitmapFactory.decodeFile(localFile.absolutePath,options)

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
        }else{
            var gson = Gson()
            var userObj = gson.fromJson(p0.snippet,User::class.java)
            var builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setMessage("See this user's guide?")
                .setCancelable(true)
                .setPositiveButton("Yes"){dialog, which ->
                     guideColRef.whereEqualTo("owner",userObj.fullName).orderBy("owner",Query.Direction.DESCENDING).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                         if (firebaseFirestoreException != null) {

                         } else {
                             if (querySnapshot == null) {
                                 Snackbar.make(view!!, "This user has no guide yet", Snackbar.LENGTH_LONG).show()
                                 view!!.findNavController()
                                     .navigate(MapFragmentDirections.actionMapFragmentToListFragment(userObj.fullName))
                             } else {
                                 view!!.findNavController()
                                     .navigate(MapFragmentDirections.actionMapFragmentToListFragment(userObj.fullName))
                             }

                         }
                         dialog.dismiss()
                     }
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
