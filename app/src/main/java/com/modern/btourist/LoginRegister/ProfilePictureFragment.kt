package com.modern.btourist.LoginRegister


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.modern.btourist.Database.FirestoreUtil
import com.modern.btourist.Database.StorageUtil
import com.modern.btourist.Map.MainActivity
import com.modern.btourist.R
import com.modern.btourist.databinding.FragmentProfilePictureBinding
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile_picture.*
import java.io.ByteArrayOutputStream
import java.io.InputStream


@SuppressLint("ByteOrderMark")
class ProfilePictureFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private val RC_SELECT_IMAGE = 2
    private val RECORD_REQUEST_CODE = 101
    private lateinit var selectedImageBytes: ByteArray
    private var pictureJustChanged = false
    private lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DataBindingUtil.inflate<FragmentProfilePictureBinding>(inflater,
            R.layout.fragment_profile_picture,container,false)
        //val view = inflater.inflate(R.layout.fragment_profile_picture,container,false)

        imageView = binding.profilePictureView
        var args = ProfilePictureFragmentArgs.fromBundle(arguments!!)


        binding.profilePictureView.setOnClickListener{
            val permission = ContextCompat.checkSelfPermission(activity as Context,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            fun makeRequest() {
                requestPermissions(activity as Activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    RECORD_REQUEST_CODE)
            }

            if (permission != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(activity as Activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                ) {
                    val builder = AlertDialog.Builder(activity as Activity)
                    builder.setMessage("Permission to access external storage is needed, to get a custom avatar.")
                            .setTitle("Permission required")

                                builder.setPositiveButton("OK"
                                ) { _, _ -> makeRequest()
                        }

                        val dialog = builder.create()
                    dialog.show()
                } else {
                    makeRequest()
                }
                Snackbar.make( view!! , "Read External Storage Permission Denied: Please turn it on for Custom Avatar",Snackbar.LENGTH_LONG).show()
                Log.i("ProfilePictureFragment", "Permission to record denied")
            }else {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }
                startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_SELECT_IMAGE)
            }
        }


        binding.finishUserButton.setOnClickListener {
            var ageText = binding.ageEditText
            var ageString: String = ageText.getText().toString().trim()
            var progressBar: ProgressBar =binding.progressBar
            progressBar.visibility = View.VISIBLE



            if (validateAge() && validateSex()) {

                mAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(args.email, args.password)
                    .addOnCompleteListener { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            //Registration OK

                            FirestoreUtil.initCurrentUserIfFirstTime {
                                // val firebaseUser = mAuth.currentUser!!

                            }

                            var radioGroup: RadioGroup = binding.radioGroup
                            var selectedId: Int = radioGroup.checkedRadioButtonId
                            var radioButton: RadioButton = view!!.findViewById(selectedId)
                            var sex: String = radioButton.getText().toString().trim()
                            var age: Int = Integer.parseInt(ageString)
                            if (::selectedImageBytes.isInitialized)
                                StorageUtil.uploadProfilePicture(selectedImageBytes) { imagePath ->
                                    FirestoreUtil.updateCurrentUser(
                                        args.firstName,
                                        args.lastName,
                                        args.email,
                                        args.phone,
                                        args.interest1,
                                        args.interest2,
                                        args.interest3,
                                        age,
                                        sex,
                                        args.language1,
                                        args.language2,
                                        imagePath,
                                        0.0,
                                        0.0,
                                        args.firstName+" "+args.lastName
                                    )
                                }
                            else {

                                val selectedImagePath =
                                    Uri.parse("android.resource://" + context!!.getPackageName() + "/drawable/people")
                                val selectedImageBmp =
                                    MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImagePath)
                                val outputStream = ByteArrayOutputStream()
                                selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                                selectedImageBytes = outputStream.toByteArray()

                                StorageUtil.uploadProfilePicture(selectedImageBytes) { imagePath ->
                                    FirestoreUtil.updateCurrentUser(
                                        args.firstName,
                                        args.lastName,
                                        args.email,
                                        args.phone,
                                        args.interest1,
                                        args.interest2,
                                        args.interest3,
                                        age,
                                        sex,
                                        args.language1,
                                        args.language2,
                                        imagePath,
                                        0.0,
                                        0.0,
                                        args.firstName+" "+args.lastName
                                    )
                                }
                            }

                        view!!.findNavController()
                            .navigate(ProfilePictureFragmentDirections.actionProfilePictureFragmentToLoginFragment())

                        } else {
                            //Registration error
                            Snackbar.make(view!!, "Registration Failed", Snackbar.LENGTH_LONG).show()
                        }

                    }
            }

        }

        return binding.root
    }

    fun validateAge(): Boolean{
        val age: String = ageEditText.text.toString().trim()

        if (age.isEmpty()) {
            ageEditText.error = "Enter age"
            ageEditText.requestFocus()
            return false
        }else if (Integer.parseInt(age)<0||Integer.parseInt(age)>100) {
            ageEditText.error = "Enter a Valid Age"
            ageEditText.requestFocus()
            return false
        }else {
            ageEditText.error = null
            return true
        }
    }

    fun validateSex(): Boolean{

        if (radioGroup.checkedRadioButtonId==-1) {
            sexText.error = "Select the sex"
            sexText.requestFocus()
            return false
        }else {
            sexText.error = null
            return true
        }
    }


    private fun modifyOrientation(bitmap: Bitmap, image_absolute_path: InputStream): Bitmap {
        val ei = ExifInterface(image_absolute_path)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipImage(bitmap, true, false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipImage(bitmap, false, true)
            else -> bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flipImage(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale((if (horizontal) -1 else 1).toFloat(), (if (vertical) -1 else 1).toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                  Log.d("ProfilePictureFragment","READ_EXTERNAL_STORAGE Granted")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==RC_SELECT_IMAGE && resultCode== Activity.RESULT_OK
            && data!=null && data.data!=null){
            val selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImagePath)

            var resolver: ContentResolver = (activity as Activity).contentResolver
            var inputStream: InputStream = resolver.openInputStream(selectedImagePath!!)!!

            val orientatedImageBmp = modifyOrientation(selectedImageBmp,inputStream)

            val outputStream = ByteArrayOutputStream()

            orientatedImageBmp.compress(Bitmap.CompressFormat.JPEG,90,outputStream)
            selectedImageBytes = outputStream.toByteArray()

            //Picasso.get().load(selectedImagePath).into(profilePictureView)
            imageView.setImageBitmap(orientatedImageBmp)

            pictureJustChanged = true
        }


    }
}
