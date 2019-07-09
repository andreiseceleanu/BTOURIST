package com.modern.btourist.Guides

import android.content.Context
import com.modern.btourist.Database.Guide
import com.modern.btourist.Database.StorageUtil
import com.modern.btourist.Database.User
import com.modern.btourist.Map.GlideApp
import com.modern.btourist.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_profile_picture.*
import kotlinx.android.synthetic.main.guide_item.*
import kotlinx.android.synthetic.main.user_card.*

class GuideItem (val guide:Guide, val userId:String, private val context: Context): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {


    }

    override fun getLayout() = R.layout.guide_item

}