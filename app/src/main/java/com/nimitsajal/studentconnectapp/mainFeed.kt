package com.nimitsajal.studentconnectapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main_feed.*

class mainFeed : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_feed)

        val username = intent.getStringExtra("username")

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        btnChat.setOnClickListener {
            val intent = Intent(this, currentChats::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

//        btnFeed.setOnClickListener {
//            val intent = Intent(this, mainFeed::class.java)
//            intent.putExtra("username", username)
//            startActivity(intent)
//        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        moveTaskToBack(true)
    }
}
