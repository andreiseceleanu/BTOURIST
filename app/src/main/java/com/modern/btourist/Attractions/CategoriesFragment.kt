package com.modern.btourist.Attractions


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.modern.btourist.Database.Category
import com.modern.btourist.databinding.FragmentCategories2Binding
import com.modern.btourist.R
import kotlinx.android.synthetic.main.fragment_categories2.*

class CategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentCategories2Binding>(inflater,
            R.layout.fragment_categories2, container, false)

        var culture = Category("Culture, Architecture and History",R.drawable.architecture)
        var nature = Category("Nature",R.drawable.nature)
        var nightlife = Category("Nightlife",R.drawable.club)
        var shopping = Category("Shopping",R.drawable.shopping)
        var food = Category("Food",R.drawable.food)

        var categoryList: ArrayList<Category> = ArrayList()
        categoryList.add(culture)
        categoryList.add(nature)
        categoryList.add(nightlife)
        categoryList.add(shopping)
        categoryList.add(food)

        val categoryListView = binding.categoryListView
        val adapter = CategoryListAdapter(context!!,R.layout.category_adapter,categoryList)
        categoryListView.adapter = adapter

        categoryListView.setOnItemClickListener { parent, view, position, id ->
            var category = categoryList[position]
            var catName = category.name
            view.findNavController().navigate(CategoriesFragmentDirections.actionCategoriesFragmentToAttractionListFragment(catName))
        }

        return binding.root
    }


}
