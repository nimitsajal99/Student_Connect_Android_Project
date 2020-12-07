package com.nimitsajal.studentconnectapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

class blank : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank)

        //  1 -> home to profile
        //  2 -> home to chat
        //  3 -> event to chat
        //  4 -> profile to home
        //  5 -> chat to event
        //  6 -> chat to home

        var type = intent.getStringExtra("type")
        var username = intent.getStringExtra("username")

        when (type) {
            "1" -> {
                Log.d("blank", "in 1")
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    val intent = Intent(this, profilePage::class.java)
                    intent.putExtra("username", username)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_from_right_fast, R.anim.slide_to_left_fast)
                    finish()
                }, 200)
            }
            "2" -> {
                Log.d("blank", "in 2")
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    val intent = Intent(this, blank::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("type", "3")
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_from_right_fast, R.anim.slide_to_left_fast)
                    finish()
                }, 100)
            }
            "3" -> {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    val intent = Intent(this, currentChats::class.java)
                    intent.putExtra("username", username)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_from_right_fast, R.anim.slide_to_left_fast)
                    finish()
                }, 200)
            }
            "4" -> {
                val intent = Intent(this, mainFeed::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
                finish()
            }
            "5" -> {
                val intent = Intent(this, eventPage::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
                finish()
            }
            "6" -> {
                val intent = Intent(this, blank::class.java)
                intent.putExtra("username", username)
                intent.putExtra("type", "4")
                startActivity(intent)
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
                finish()
            }
            else -> { // Note the block
                Log.d("blank", "else")
            }
        }
    }
}