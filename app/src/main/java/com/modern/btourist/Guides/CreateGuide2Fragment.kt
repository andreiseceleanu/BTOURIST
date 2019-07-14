package com.modern.btourist.Guides


import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.modern.btourist.Database.FirestoreUtil
import com.modern.btourist.Database.Guide
import com.modern.btourist.Database.User

import com.modern.btourist.R
import kotlinx.android.synthetic.main.fragment_create_guide2.*
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class CreateGuide2Fragment : Fragment(){

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var col: CollectionReference = db.collection("users")
    lateinit var dataTimeText: TextView
    lateinit var nameEditText: EditText
    lateinit var descriptionEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_guide2, container, false)
        dataTimeText = view.findViewById(R.id.dateTimeText)
        nameEditText = view.findViewById(R.id.nameEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)

        var bundle = CreateGuide2FragmentArgs.fromBundle(arguments!!)
        Log.d("AttractionListPassed",bundle.attractionList.toMutableList().toString())

        var attractionNamesList: ArrayList<String> = ArrayList()
        for(attraction in bundle.attractionList){
            attractionNamesList.add(attraction.name)
        }

        val dateButton = view.findViewById<Button>(R.id.dateButton)
        val timeButton = view.findViewById<Button>(R.id.timeButton)
        val finishButton = view.findViewById<Button>(R.id.finishButton)

        dateButton.setOnClickListener {
            showDatePicker()
        }

        timeButton.setOnClickListener {
            showTimePicker()
        }

        finishButton.setOnClickListener {
            if(validateFields()){
                lateinit var userObject: User
                col.addSnapshotListener(activity as Activity) { querySnapshot, firebaseFirestoreException ->

                    if( firebaseFirestoreException!=null){
                        Log.e("MapFragment","Attraction snapshot listen failed",firebaseFirestoreException)
                    }
                    if(querySnapshot!=null) {
                        for(user in querySnapshot){
                            var userId = user.id
                            userId = userId.replace("\\s".toRegex(), "")
                            if(userId==FirebaseAuth.getInstance().currentUser!!.uid){
                                userObject = user.toObject(User::class.java)

                                var navController = activity!!.findNavController(R.id.navHostMain)

                                var owner = userObject.fullName
                                var userList: ArrayList<String> = ArrayList()
                                userList.add(owner)
                                var dateTime = dataTimeText.text.toString()
                                var name = nameEditText.text.toString()
                                var description = descriptionEditText.text.toString()

                                val guide = Guide(attractionNamesList, owner, userList, dateTime, name, description)
                                db.collection("guides").document(FirebaseAuth.getInstance().currentUser!!.uid).set(guide)

                                var current = navController.currentDestination?.id

                                if (navController.currentDestination?.id == R.id.createGuide2Fragment){
                                     navController.navigate(CreateGuide2FragmentDirections.actionCreateGuide2FragmentToMapFragment(""))
                                }
                            }
                        }
                    }
                }

            }
        }

        return view
    }

    fun validateFields(): Boolean{
        var valid = true

        if("".equals(nameEditText.text.toString())){
            valid=false
            nameEditText.error = "Name cannot be empty"
        }

        if("".equals(arrayOf(descriptionEditText.text.toString()))||descriptionEditText.text.toString().length<10){
            valid=false
            descriptionEditText.error = "Description lenght has to be > 10"
        }
        if(dateButton.visibility==View.VISIBLE){
            valid=false
            dateButton.error = "Pick a Date"
        }

        if(timeButton.visibility==View.VISIBLE){
            valid=false
            timeButton.error = "Pick a Time"
        }

        return valid
    }

    private fun showDatePicker() {
        val date = DatePickerFragment()

        val calender = Calendar.getInstance()
        val args = Bundle()
        args.putInt("year", calender.get(Calendar.YEAR))
        args.putInt("month", calender.get(Calendar.MONTH))
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH))
        date.arguments = args

        date.setCallBack(ondate)
        date.show(fragmentManager!!, "Date Picker")
    }

    var ondate: DatePickerDialog.OnDateSetListener = object: DatePickerDialog.OnDateSetListener {
        override fun onDateSet(view:DatePicker, year:Int, monthOfYear:Int,
                               dayOfMonth:Int) {
            var c: Calendar = Calendar.getInstance()
            var currentDateString: String = DateFormat.getDateInstance(DateFormat.FULL).format(c.time)
            dateTimeText.text = currentDateString
            Log.d("Date",currentDateString)

            dateButton.visibility = View.INVISIBLE
            timeButton.visibility = View.VISIBLE
        }
    }

    private fun showTimePicker() {
        val time = TimePickerFragment()

        val calender = Calendar.getInstance()
        val args = Bundle()
        args.putInt("hour", calender.get(Calendar.HOUR_OF_DAY))
        args.putInt("minute", calender.get(Calendar.MINUTE))
        time.arguments = args

        time.setCallBack(onTime)
        time.show(fragmentManager!!, "Time Picker")
    }

    var onTime: TimePickerDialog.OnTimeSetListener = object: TimePickerDialog.OnTimeSetListener {
        override fun onTimeSet(view:TimePicker, hour:Int, minute:Int) {

            dateTimeText.text = dateTimeText.text.toString()+" "+hour.toString()+":"+minute.toString()
            timeButton.visibility = View.INVISIBLE
        }
    }


}
