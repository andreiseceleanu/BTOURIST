package com.modern.btourist.Guides


import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.SharedPreferences
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
import com.google.gson.Gson
import com.modern.btourist.Database.FirestoreUtil
import com.modern.btourist.Database.Guide
import com.modern.btourist.Database.User


import kotlinx.android.synthetic.main.fragment_create_guide2.*
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList




class CreateGuide2Fragment : Fragment(){

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var col: CollectionReference = db.collection("users")
    private var guideCol: CollectionReference = db.collection("guides")
    lateinit var dataTimeText: TextView
    lateinit var nameEditText: EditText
    lateinit var descriptionEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(com.modern.btourist.R.layout.fragment_create_guide2, container, false)
        dataTimeText = view.findViewById(com.modern.btourist.R.id.dateTimeText)
        nameEditText = view.findViewById(com.modern.btourist.R.id.nameEditText)
        descriptionEditText = view.findViewById(com.modern.btourist.R.id.descriptionEditText)

        var bundle = CreateGuide2FragmentArgs.fromBundle(arguments!!)
        Log.d("AttractionListPassed",bundle.attractionList.toMutableList().toString())

        var attractionNamesList: ArrayList<String> = ArrayList()
        for(attraction in bundle.attractionList){
            attractionNamesList.add(attraction.name)
        }
        var gson = Gson()
        var prefs: SharedPreferences = activity!!.getSharedPreferences("com.modern.btourist.prefs",0)
        var editor = prefs.edit()
        editor.remove("attractionSet")
        editor.putString("attractionSet",gson.toJson(attractionNamesList))
        editor.apply()


        val dateButton = view.findViewById<Button>(com.modern.btourist.R.id.dateButton)
        val timeButton = view.findViewById<Button>(com.modern.btourist.R.id.timeButton)
        val finishButton = view.findViewById<Button>(com.modern.btourist.R.id.finishButton)

        dateButton.setOnClickListener {
            showDatePicker()
        }

        timeButton.setOnClickListener {
            showTimePicker()
        }

        finishButton.setOnClickListener {
            var prefs: SharedPreferences = activity!!.getSharedPreferences("com.modern.btourist.prefs",0)
            var editor = prefs.edit()
            editor.putBoolean("permited",true)

            if(validateFields()){

                lateinit var userObject: User
                FirestoreUtil.getCurrentUser { user->
                    userObject = user!!

                    var navController = activity!!.findNavController(com.modern.btourist.R.id.navHostMain)

                    var owner = userObject.fullName
                    var userList: ArrayList<String> = ArrayList()
                    userList.add(owner)
                    var dateTime = dataTimeText.text.toString()
                    var name = nameEditText.text.toString()
                    var description = descriptionEditText.text.toString()

                    val guide = Guide(attractionNamesList, owner, userList, dateTime, name, description)

                    var prefs: SharedPreferences = activity!!.getSharedPreferences("com.modern.btourist.prefs",0)
                    var editor = prefs.edit()

                    //if(prefs.getBoolean("permited",true)){

                    db.collection("guides").document(FirebaseAuth.getInstance().currentUser!!.uid).set(guide)

                    //}

                    var current = navController.currentDestination?.id
                    lateinit var guideId: String

                    guideCol.whereEqualTo("owner",owner).addSnapshotListener {it,exception->
                        if(exception!=null){
                            Log.e("GuideSnapshot","Guide snapshot listen failed",exception)
                        }else {
                            if (navController.currentDestination?.id == com.modern.btourist.R.id.createGuide2Fragment) {

                                editor.putBoolean("joined", true)
                                editor.remove("AttractionList")
                                editor.putString("owner", owner)
                                Log.d("listanext",attractionNamesList.toString())
                                editor.putBoolean("permited", false)
                                var guide = it!!.first()
                                guideId = guide.id
                                editor.putString("ownerGuideId", guideId)
                                editor.apply()
                                navController.navigate(
                                    CreateGuide2FragmentDirections.actionCreateGuide2FragmentToMapFragment(
                                        ""
                                    )
                                )

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
            val date = GregorianCalendar(year, monthOfYear, dayOfMonth).time
            var currentDateString: String = DateFormat.getDateInstance(DateFormat.FULL).format(date)
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
