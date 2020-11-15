package com.nimitsajal.studentconnectapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.activity_main_feed.btnFeed
import kotlinx.android.synthetic.main.activity_main_feed.btnLogout
import kotlinx.android.synthetic.main.activity_profile_page.*
import kotlinx.android.synthetic.main.activity_profile_page.circularImageView
import kotlinx.android.synthetic.main.activity_sign_up.*

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
        tvDetails.setText("").toString()

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid.toString())
            user_table.get().addOnSuccessListener {result ->
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


    private fun getUser(auth: FirebaseAuth, username: String){
        val db = FirebaseFirestore.getInstance()
        tvUsername.setText(username.toString()).toString()
        val user_info = db.collection("Users").document(username)
        user_info.get().addOnSuccessListener {
            if(it != null){
                tvName.setText(it.getString("Name").toString()).toString()
                tvDescription.setText(it.getString("Description").toString()).toString()
//                val selectedPhotoUrl_string = it.getString("Picture").toString()
//                val selectedPhotoUrl = Uri.parse(selectedPhotoUrl_string)
//                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUrl)
//                circularImageView.setImageBitmap(bitmap)
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