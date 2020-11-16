package com.nimitsajal.studentconnectapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
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
            val user_table = db.collection("User Table").document(user.uid.toString())
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
                    Toast.makeText(this, "Document id = ${document.id}", Toast.LENGTH_SHORT).show()
                    adapter.add(UserItem(document.id))
                }
            }
            rvNewChats.adapter = adapter
        }

//        adapter.setOnItemClickListener { item, view ->
//
//        }

//        db.addListenerForSingleValueEvent(object: ValueEventListener{
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val adapter = GroupAdapter<GroupieViewHolder>()
//                snapshot.children.forEach {
//                    Log.d("NewMessage", it.toString())
//                    val user = it.getValue(User::class.java)
//                    if(user != null){
//                        adapter.add(UserItem(user))
//                    }
//                }
//                adapter.setOnItemClickListener { item, view ->
//                    val userItem = item as UserItem
//                    val intent = Intent(view.context, ChatLogActivity::class.java)
//                    intent.putExtra(USER_KEY, userItem.user)
//                    startActivity(intent)
//                    finish()
//                }
//                recyclerView_newMessage.adapter = adapter
//            }
//        })
    }
}

class UserItem(val user: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.new_chat_adapter
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val db = FirebaseFirestore.getInstance()
        var url = ""
        val temp = db.collection("Users").document(user).get()
            .addOnSuccessListener {
                if(it != null){
                    url = it.getString("Picture").toString()
                }
            }
        viewHolder.itemView.tv_usernames_newMessage.text = user
        Picasso.get().load(url).into(viewHolder.itemView.cv_dp_newMessage)
    }
}