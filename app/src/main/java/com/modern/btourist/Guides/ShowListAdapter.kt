package com.modern.btourist.Guides

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import com.modern.btourist.Database.Attraction
import com.modern.btourist.R
import org.w3c.dom.Text

class ShowListAdapter(private val context: Context,
                             private val dataSource: ArrayList<Attraction>) : BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.attraction_show_item, parent, false)

        val id = rowView.findViewById<TextView>(R.id.idTextView)
        val name = rowView.findViewById<TextView>(R.id.nameTextView)
        val category = rowView.findViewById<TextView>(R.id.categoryTextView)

        val attraction = getItem(position) as Attraction

        id.text = (getItemId(position)+1).toString()
        name.text = attraction.name
        category.text = attraction.category

        return rowView
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return dataSource.size
    }

}