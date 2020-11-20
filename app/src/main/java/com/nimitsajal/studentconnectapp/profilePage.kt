package com.nimitsajal.studentconnectapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.activity_main_feed.btnFeed
import kotlinx.android.synthetic.main.activity_main_feed.btnLogout
import kotlinx.android.synthetic.main.activity_profile_page.*
import kotlinx.android.synthetic.main.activity_profile_page.btnChat
import kotlinx.android.synthetic.main.details_adapter.view.*


class profilePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)


        var username = intent.getStringExtra("username")
        tvUsername.setText("").toString()
        //tvUsername.setText(username).toString()


        tvDescription.setText("").toString()



        tvName.setText("").toString()



        btnFriends.setText("Friends").toString()



        btnEdit.setText("Edit").toString()



//        tvDetails.setText("").toString()

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid.toString())
            user_table.get().addOnSuccessListener { result ->
                if(result != null){
                    username = result.getString("Username").toString()
                    Log.d("profilePage", username.toString())
                    getUser(auth, username!!)
                }
                else{
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
            }
        }

        btnChat.setOnClickListener {
            val intent = Intent(this, currentChats::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

//        btnProfile.setOnClickListener {
//            val intent = Intent(this, profilePage::class.java)
//            intent.putExtra("username", username)
//            startActivity(intent)
//        }

        btnFeed.setOnClickListener {
            val intent = Intent(this, mainFeed::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

    }


    private fun collapse(textView: TextView) {
        textView.height = 0
    }
    private fun expand(textView: TextView, height: Int) {
        if(height == 999){
            textView.layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
        }
    }

    private fun getUser(auth: FirebaseAuth, username: String){
        val db = FirebaseFirestore.getInstance()

//        if(username.toString() == ""){
//            collapse((tvUsername))
//        }
//        else{
//            expand(tvUsername, 999)
//            tvUsername.setText(username.toString()).toString()
//        }

        tvUsername.setText(username.toString()).toString()

        val user_info = db.collection("Users").document(username)
        user_info.get().addOnSuccessListener {
            if(it != null){

//                if(it.getString("Name").toString() == ""){
//                    //collapse(tvName)
//                    tvName.setText(it.getString("Name").toString()).toString()
//                    expand(tvName, 999)
//                }
//                else{
//                    expand(tvName, 999)
//                    tvName.setText(it.getString("Name").toString()).toString()
//                }

                tvName.setText(it.getString("Name").toString()).toString()


                if(it.getString("Description").toString() == ""){
                    collapse(tvDescription)
//                    tvDescription.setText(it.getString("Description").toString()).toString()
//                    expand(tvName, 999)
                }
                else{
                    expand(tvDescription, 999)
                    tvDescription.setText(it.getString("Description").toString()).toString()
                }
                val arrayDetails = mutableListOf<String>()
                val adapter = GroupAdapter<GroupieViewHolder>()
                db.collection("Users").document(username).collection("Medals")
                    .get()
                    .addOnSuccessListener {
                        for(document in it){
                            arrayDetails.add(document.id.toString())
                        }
                        for(i in 0 until arrayDetails.size){
                            var num = (0 until arrayDetails.size).random()
                            adapter.add(details_class(arrayDetails.get(num)))
                            arrayDetails.removeAt(num)
                        }
                        rvDetails.adapter = adapter
                    }

                var selectedPhotoUrl_string = it.getString("Picture")
                val selectedPhotoUrl = Uri.parse(selectedPhotoUrl_string)
//                val selectedPhotoUrl = selectedPhotoUrl_string.toUri()
//                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUrl)
//                circularImageView.setImageBitmap(bitmap)


                Picasso.get().load(selectedPhotoUrl_string).into(object :
                    com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        // loaded bitmap is here (bitmap)
                        circularImageView.setImageBitmap(bitmap)
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                })
            }
        }



        var friends = "Friends ("
        val FriendsCount = user_info.collection("Friends").get()
            .addOnSuccessListener {
                if(it != null){
                    val count = it.size() - 1
                    friends = friends + count + ")"
                    btnFriends.setText(friends).toString()
                }
            }
    }

}

class details_class(val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvdetails.text = text
    }

    override fun getLayout(): Int {
        return R.layout.details_adapter
    }

}

//useless text