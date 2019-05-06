package com.modern.btourist.LoginRegister


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.modern.btourist.Database.FirestoreUtil
import com.modern.btourist.Database.StorageUtil
import com.modern.btourist.R
import com.modern.btourist.databinding.FragmentProfilePictureBinding
import kotlinx.android.synthetic.main.fragment_profile_picture.*
import java.io.ByteArrayOutputStream


class ProfilePictureFragment : Fragment() {

    private val RC_SELECT_IMAGE = 2
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
                val intent = Intent().apply{
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg" , "image/png"))
                }
           startActivityForResult(Intent.createChooser(intent,"Select Image"),RC_SELECT_IMAGE)
        }



        binding.finishUserButton.setOnClickListener {
            var ageText = binding.ageEditText
            var ageString: String = ageText.getText().toString().trim()



            if (validateAge() && validateSex()) {

                var radioGroup: RadioGroup = binding.radioGroup
                var selectedId: Int = radioGroup.checkedRadioButtonId
                var radioButton: RadioButton = view!!.findViewById(selectedId)
                var sex: String = radioButton.getText().toString().trim()
                var age: Int = Integer.parseInt(ageString)
                if (::selectedImageBytes.isInitialized)
                    StorageUtil.uploadProfilePicture(selectedImageBytes) { imagePath ->
                        FirestoreUtil.updateCurrentUser(
                            args.email,
                            args.lastName,
                            args.firstName,
                            args.phone,
                            args.interest1,
                            args.interest2,
                            args.interest3,
                            age,
                            sex,
                            args.language1,
                            args.language2,
                            imagePath
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
                            args.email,
                            args.lastName,
                            args.firstName,
                            args.phone,
                            args.interest1,
                            args.interest2,
                            args.interest3,
                            age,
                            sex,
                            args.language1,
                            args.language2,
                            imagePath
                        )
                    }
                }

                view!!.findNavController()
                    .navigate(ProfilePictureFragmentDirections.actionProfilePictureFragmentToLoginFragment())

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

    fun rotateImage(src: Bitmap, degree: Float): Bitmap {
        // create new matrix
        val matrix = Matrix()
        // setup rotation degree
        matrix.postRotate(degree)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==RC_SELECT_IMAGE && resultCode== Activity.RESULT_OK
            && data!=null && data.data!=null){
            val selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImagePath)

            var width = selectedImageBmp.getWidth()
            var height = selectedImageBmp.getHeight()
            val outputStream = ByteArrayOutputStream()

            val selectedImageBmpRotated = rotateImage(selectedImageBmp, 90F)
            selectedImageBmpRotated.compress(Bitmap.CompressFormat.JPEG,90,outputStream)
            selectedImageBytes = outputStream.toByteArray()

            //Picasso.get().load(selectedImagePath).rotate(90F).into(profilePictureView)
            imageView.setImageBitmap(selectedImageBmpRotated)

            pictureJustChanged = true
        }


    }
}
