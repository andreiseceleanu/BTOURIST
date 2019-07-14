package com.modern.btourist.Guides


import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*
import android.app.DatePickerDialog.OnDateSetListener




class DatePickerFragment : DialogFragment(){

    lateinit var ondateSet:OnDateSetListener
    private val year:Int = 0
    private val month:Int = 0
    private val day:Int = 0
    fun setCallBack(ondate:OnDateSetListener) {
        ondateSet = ondate
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var c: Calendar = Calendar.getInstance()
        var year: Int = c.get(Calendar.YEAR)
        var month: Int = c.get(Calendar.MONTH)
        var day: Int = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(activity, ondateSet, year, month, day)
    }
}
