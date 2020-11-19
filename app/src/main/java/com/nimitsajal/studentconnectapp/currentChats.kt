package com.nimitsajal.studentconnectapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_current_chats.*
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.activity_main_feed.btnFeed
import kotlinx.android.synthetic.main.activity_main_feed.btnLogout
import kotlinx.android.synthetic.main.activity_main_feed.btnProfile
import kotlinx.android.synthetic.main.current_chat_adapter.view.*
import kotlinx.android.synthetic.main.new_chat_adapter.view.*

class currentChats : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_chats)
//        var username = ""
        var username = intent.getStringExtra("username")
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid.toString())
            user_table.get().addOnSuccessListener { result ->
                if(result != null){
                    username = result.getString("Username").toString()
                    Log.d("profilePage", username.toString())
                    fetchUser(username!!)
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

//        btnChat.setOnClickListener {
//            val intent = Intent(this, currentChats::class.java)
//            intent.putExtra("username", username)
//            startActivity(intent)
//        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnFeed.setOnClickListener {
            val intent = Intent(this, mainFeed::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnNewChat.setOnClickListener {
            val intent = Intent(this, newChat::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }


    }

    @SuppressLint("RestrictedApi")
    private fun fetchUser(username: String){
        val adapter = GroupAdapter<GroupieViewHolder>()
        val db = FirebaseFirestore.getInstance()
        val user = db.collection("Users").document(username).collection("Chats")
        user.addSnapshotListener { value, error ->
            if(error != null || value == null){
                Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            for(document in value.documents){
                if(document.id != "Info"){
                    Log.d("adapter", "Document id = ${document.id}")
                    //Toast.makeText(this, "Document id = ${document.id}", Toast.LENGTH_SHORT).show()
                    adapter.add(CurrentChat_class(document.id, document["Text"].toString()))
                    Log.d("adapter", "Done")
                    //Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
                }
            }
            adapter.setOnItemClickListener { item, view ->
                val currentChat_class: CurrentChat_class = item as CurrentChat_class
                val to = currentChat_class.username
                val intent = Intent(this, chat::class.java)
                intent.putExtra("from", username)
                intent.putExtra("to", to)
                startActivity(intent)
                finish()
            }
            rvCurrentChats.adapter = adapter
        }
    }

}



class CurrentChat_class(val username: String, val text: String): Item<GroupieViewHolder>(){
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val db = FirebaseFirestore.getInstance()
        var url = ""
        db.collection("Users").document(username)
            .get()
            .addOnSuccessListener {
                url = it.getString("Picture").toString()
                viewHolder.itemView.tv_usernames_latestMessage.text = username
                viewHolder.itemView.tv_text_latestMessage.text = text
                Picasso.get().load(url).into(viewHolder.itemView.cv_dp_currentMessage)
                Log.d("adapter", "adapter added")
            }
    }

    override fun getLayout(): Int {
        return R.layout.current_chat_adapter
    }

}
