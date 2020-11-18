package com.nimitsajal.studentconnectapp

import android.annotation.SuppressLint
import android.content.Intent
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
import kotlinx.android.synthetic.main.activity_new_chat.*
import kotlinx.android.synthetic.main.new_chat_adapter.view.*

class newChat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_chat)

        var username = intent.getStringExtra("username")
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid)
            user_table.get().addOnSuccessListener { result ->
                if(result != null){
                    username = result.getString("Username").toString()
                    Log.d("profilePage", username.toString())
                }
                else{
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
            }
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, currentChats::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        fetchUsers(username!!)

    }

    @SuppressLint("RestrictedApi")
    private fun fetchUsers(username: String){
        val adapter = GroupAdapter<GroupieViewHolder>()
        val db = FirebaseFirestore.getInstance()
        val user = db.collection("Users").document(username).collection("Friends")
        user.addSnapshotListener { value, error ->
            if(error != null || value == null){
                Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            for(document in value.documents){
                if(document.id != "Info"){
                    Log.d("adapter", "Document id = ${document.id}")
                    //Toast.makeText(this, "Document id = ${document.id}", Toast.LENGTH_SHORT).show()
                    adapter.add(UserItem(document.id, document["Picture"].toString()))
                    Log.d("adapter", "Done")
                    //Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
                }
            }
            adapter.setOnItemClickListener { item, view ->
                val userItem: UserItem = item as UserItem
                //val intent = Intent(view.context, ChatActivity:class.java)
                Toast.makeText(this, "Username = ${userItem.username}, Link = ${userItem.link}", Toast.LENGTH_SHORT).show()
            }
            rvNewChats.adapter = adapter
        }
    }
}

class UserItem(val username: String, url: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.new_chat_adapter
    }

    val link = url
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val db = FirebaseFirestore.getInstance()
//        var url = ""
//        db.collection("Users").document(username)
//            .get()
//            .addOnSuccessListener {
//                url = it.getString("Picture").toString()
//            }
//        val selectedPhotoUri = Uri.parse(url)
        viewHolder.itemView.tv_usernames_newMessage.text = username
        Picasso.get().load(link).into(viewHolder.itemView.cv_dp_newMessage)
        Log.d("adapter", "adapter added")
    }
}