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
import com.modern.btourist.Database.FirestoreUtil
import com.modern.btourist.Database.StorageUtil
import com.modern.btourist.Database.User
import com.modern.btourist.databinding.FragmentMapBinding
import android.app.Activity
import android.net.nsd.NsdManager
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.util.Listener
import com.google.gson.Gson
import com.modern.btourist.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.*
import java.lang.Exception
import java.net.URL
import java.net.URLConnection


class MapFragment : Fragment(),OnMapReadyCallback {

    lateinit var binding: FragmentMapBinding
    private lateinit var mMapView: MapView
    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

    private var firebaseFirestore = FirebaseFirestore.getInstance()
    private var docRef = firebaseFirestore.document("users/${FirebaseAuth.getInstance().uid
    ?: throw NullPointerException("UID is null.")} ")
    private var colRef = firebaseFirestore.collection("users")
    private var markerMap: HashMap<String,Marker> = HashMap()
    private lateinit var registration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

            // Inflate the layout for this fragment
            binding = DataBindingUtil.inflate(inflater, com.modern.btourist.R.layout.fragment_map,container,false)

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMapView = binding.mapView
        mMapView.onCreate(mapViewBundle)

        mMapView.getMapAsync(this)

            return binding.root
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
        mMapView.onStop()
    }

    override fun onMapReady(map: GoogleMap) {



        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, com.modern.btourist.R.raw.mapstyle_night))


        docRef.get().addOnSuccessListener(activity as Activity) {
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
                        try{
                        if (previousMarker != null) {
                            var latLng = LatLng(user.latitude, user.longitude)

                            if (FirebaseAuth.getInstance().currentUser!!.email == user.email) {
                                // Add a border around rounded corners bitmap
                                myImage = addBorderToRoundedBitmap(myImage, 150F, 10F, Color.rgb(216, 120, 45))

                                // Add a border around rounded corners bitmap as shadow
                                myImage = addBorderToRoundedBitmap(myImage, 150F, 3F, Color.LTGRAY)

                                var markerOptionsMyUser: MarkerOptions =
                                    MarkerOptions().position(LatLng(user!!.latitude, user.longitude))
                                        .title(user.firstName)
                                        .snippet("Snippet Test")
                                        .icon(BitmapDescriptorFactory.fromBitmap(myImage))
                                        .zIndex(1F)
                                        .flat(true)
                                previousMarker.setIcon(BitmapDescriptorFactory.fromBitmap(myImage))
                                previousMarker.setPosition(latLng)

                            } else {

                                myImage = addBorderToRoundedBitmap(myImage, 150F, 10F, Color.WHITE)

                                // Add a border around rounded corners bitmap as shadow
                                myImage = addBorderToRoundedBitmap(myImage, 150F, 3F, Color.LTGRAY)
                                var markerOptionsMyUser: MarkerOptions =
                                    MarkerOptions().position(LatLng(user!!.latitude, user.longitude))
                                        .title(user.firstName)
                                        .snippet("Snippet Test")
                                        .icon(BitmapDescriptorFactory.fromBitmap(myImage))
                                        .zIndex(1F)
                                        .flat(true)
                                previousMarker.setIcon(BitmapDescriptorFactory.fromBitmap(myImage))
                                previousMarker.setPosition(latLng)
                            }
                        } else {

                            if (FirebaseAuth.getInstance().currentUser!!.email == user.email) {
                                var gson: Gson = Gson()
                                var userInfoString: String = gson.toJson(user)

                                myImage = addBorderToRoundedBitmap(myImage, 150F, 10F, Color.rgb(216, 120, 45))

                                // Add a border around rounded corners bitmap as shadow
                                myImage = addBorderToRoundedBitmap(myImage, 150F, 3F, Color.LTGRAY)
                                var markerOptionsMyUser: MarkerOptions =
                                    MarkerOptions().position(LatLng(user!!.latitude, user.longitude))
                                        .title(user.firstName)
                                        .snippet(userInfoString)
                                        .icon(BitmapDescriptorFactory.fromBitmap(myImage))
                                        .zIndex(1F)
                                        .flat(true)
                                var marker = map.addMarker(markerOptionsMyUser)
                                markerMap[user.email] = marker
                            } else {
                                var gson: Gson = Gson()
                                var userInfoString: String = gson.toJson(user)

                                // Add a border around rounded corners bitmap
                                myImage = addBorderToRoundedBitmap(myImage, 150F, 10F, Color.WHITE)

                                // Add a border around rounded corners bitmap as shadow
                                myImage = addBorderToRoundedBitmap(myImage, 150F, 3F, Color.LTGRAY)
                                var markerOptionsUsers: MarkerOptions =
                                    MarkerOptions().position(LatLng(user!!.latitude, user.longitude))
                                        .title(user.firstName)
                                        .snippet(userInfoString)
                                        .icon(BitmapDescriptorFactory.fromBitmap(myImage))
                                        .flat(true)
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
                            var user: User = gson.fromJson(marker.snippet,User::class.java)


                            var fullName: String = user.firstName+" "+user.lastName
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
                    })

                    }

                }


        }

    }


     override fun onPause() {
        mMapView.onPause()
        super.onPause()
    }

     override fun onDestroy() {
        mMapView.onDestroy()
         registration.remove()
        super.onDestroy()

    }

    override fun onLowMemory() {
        super.onLowMemory()

        mMapView.onLowMemory()
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
        return dstBitmap;
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
        );

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
    class MarkerCallback : Callback{

        var marker: Marker? = null

         constructor(marker: Marker){
             this.marker = marker
         }

        override fun onSuccess() {
            if (marker != null && marker!!.isInfoWindowShown) {
                marker!!.showInfoWindow()
            }
        }

        override fun onError(e: Exception?) {
            Log.e("MarkerCallback", "Error loading thumbnail!")
        }
    }

}
