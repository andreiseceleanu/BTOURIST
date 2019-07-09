package com.modern.btourist.Guides

import android.content.Context
import com.modern.btourist.Database.StorageUtil
import com.modern.btourist.Database.User
import com.modern.btourist.Map.GlideApp
import com.modern.btourist.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_profile_picture.*
import kotlinx.android.synthetic.main.user_card.*

class UserItem(val user: User, val userId:String, private val context:Context): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_name.text = user.firstName+""+user.lastName
        viewHolder.textView_bio.text = user.age.toString()
        viewHolder.interestText1.text = user.interest1
        viewHolder.interestText2.text = user.interest2
        viewHolder.interestText3.text = user.interest3
        viewHolder.languageText1.text = user.language1
        viewHolder.languageText2.text = user.language2
        if(user.profilePicturePath!= null){
            GlideApp.with(context).load(StorageUtil.pathToReference(user.profilePicturePath!!)).placeholder(R.drawable.people).into(viewHolder.profilePictureView)
        }
    }

    override fun getLayout() = R.layout.user_card

}