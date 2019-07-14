package com.modern.btourist.Guides


import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

import com.modern.btourist.R
import java.text.DateFormat
import java.util.*

class TimePickerFragment : DialogFragment() {

    lateinit var onTimeSet: TimePickerDialog.OnTimeSetListener
    fun setCallBack(onTime: TimePickerDialog.OnTimeSetListener) {
        onTimeSet = onTime
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_picker, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c: Calendar = Calendar.getInstance()
        var hour = c.get(Calendar.HOUR_OF_DAY)
        var minute = c.get(Calendar.MINUTE)

        return TimePickerDialog(activity, onTimeSet, hour, minute, android.text.format.DateFormat.is24HourFormat(activity))
    }
}
