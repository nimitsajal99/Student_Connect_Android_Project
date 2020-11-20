package com.nimitsajal.studentconnectapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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

        var arrayUser = mutableListOf<usersList>()

        val adapter = GroupAdapter<GroupieViewHolder>()

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
                    adapter.clear()
                    fetchUsers(username!!, adapter, arrayUser)
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
            finish()
        }

//        btnSearch.setOnClickListener {
//            adapter.clear()
//            fetchUsers(username!!, adapter)
//        }

        etSearch.addTextChangedListener(){
            if(username != null){
                adapter.clear()
                searching(username!!, adapter, arrayUser)
            }
        }
    }

    private  fun already(from: String, to: String)
    {
        val intent = Intent(this, chat::class.java)
        intent.putExtra("from", from)
        intent.putExtra("to", to)
        startActivity(intent)
        finish()
    }

    private fun notAlready(from: String, to: String, fromName: String, toName: String)
    {
        val db = FirebaseFirestore.getInstance()

        val message = hashMapOf(
            "From" to "System",
            "To" to to,
            "Text" to "Say Hi",
            "Time" to FieldValue.serverTimestamp()
        )

        val messageFrom = hashMapOf(
            "From" to "System",
            "To" to to,
            "Text" to "Say Hi",
            "Name" to toName,
            "Time" to FieldValue.serverTimestamp()
        )

        val messageTo = hashMapOf(
            "From" to "System",
            "To" to to,
            "Text" to "Say Hi",
            "Name" to fromName,
            "Time" to FieldValue.serverTimestamp()
        )

        val info = hashMapOf(
            "Info" to "Info"
        )

        db.collection("Users").document(from).collection("Chats").document(to)
            .set(messageFrom)
            .addOnCompleteListener{
                if(it.isSuccessful)
                {
                    db.collection("Users").document(from).collection("Chats").document(to).collection("Next")
                        .add(message)
                        .addOnCompleteListener{it1->
                            if(it1.isSuccessful)
                            {
                                db.collection("Users").document(to).collection("Chats").document(from)
                                    .set(messageTo)
                                    .addOnCompleteListener{it2->
                                        if(it2.isSuccessful)
                                        {
                                            db.collection("Users").document(to).collection("Chats").document(from).collection("Next")
                                                .add(message)
                                                .addOnCompleteListener{it3->
                                                    if(it3.isSuccessful)
                                                    {
                                                        val intent = Intent(this, chat::class.java)
                                                        intent.putExtra("from", from)
                                                        intent.putExtra("to", to)
                                                        startActivity(intent)
                                                        finish()
                                                    }

                                                }
                                        }

                                    }
                            }

                        }
                }
            }
    }



    @SuppressLint("RestrictedApi")
    private fun fetchUsers(username: String, adapter: GroupAdapter<GroupieViewHolder>, arrayUser: MutableList<usersList>){
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
                        adapter.add(UserItem(document.id, document["Picture"].toString(), document["Name"].toString(), arrayUser))
                        Log.d("adapter", "Done")
                        //Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
                }
            }
            adapter.setOnItemClickListener { item, view ->
                val userItem: UserItem = item as UserItem
                //val intent = Intent(view.context, ChatActivity:class.java)
//                Toast.makeText(this, "Username = ${userItem.username}, Link = ${userItem.link}", Toast.LENGTH_SHORT).show()
                db.collection("Users").document(username).collection("Chats")
                    .get()
                    .addOnSuccessListener{
                        for(document in it)
                        {
                            if(document.id == userItem.username)
                            {
                                Toast.makeText(this, "Already exixts", Toast.LENGTH_SHORT).show()
                                already(username,userItem.username)
                                return@addOnSuccessListener
                            }
                        }
                        Toast.makeText(this, "Doesn't exist", Toast.LENGTH_SHORT).show()
                        var fromName = ""
                        var toName = ""
                        db.collection("Users").document(username)
                            .get()
                            .addOnSuccessListener {
                                if(it != null){
                                    fromName = it.getString("Name").toString()
                                    db.collection("Users").document(userItem.username)
                                        .get()
                                        .addOnSuccessListener {it2 ->
                                            if(it2 != null){
                                                toName = it2.getString("Name").toString()
                                                notAlready(username,userItem.username, fromName, toName)
                                                return@addOnSuccessListener
                                            }
                                        }
                                }
                            }

                    }
            }
            rvNewChats.adapter = adapter
        }
    }

    private fun searching(username: String, adapter: GroupAdapter<GroupieViewHolder>, arrayUser: MutableList<usersList>){
        val search = etSearch.text.toString()
        val pattern = search.toRegex(RegexOption.IGNORE_CASE)
        if(search == "" || search == null){
            for(document in arrayUser){
                adapter.add(UserItemSearch(document.username, document.url, document.name))
            }
        }
        else{
            for(document in arrayUser){
                if(pattern.containsMatchIn(document.username) || pattern.containsMatchIn(document.name)){
                    adapter.add(UserItemSearch(document.username, document.url, document.name))
                }
            }
        }
        adapter.setOnItemClickListener { item, view ->
            val userItemSearch: UserItemSearch = item as UserItemSearch
            val db = FirebaseFirestore.getInstance()
            //val intent = Intent(view.context, ChatActivity:class.java)
//                Toast.makeText(this, "Username = ${userItem.username}, Link = ${userItem.link}", Toast.LENGTH_SHORT).show()
            db.collection("Users").document(username).collection("Chats")
                .get()
                .addOnSuccessListener{
                    for(document in it)
                    {
                        if(document.id == userItemSearch.username)
                        {
                            Toast.makeText(this, "Already exixts", Toast.LENGTH_SHORT).show()
                            already(username,userItemSearch.username)
                            return@addOnSuccessListener
                        }
                    }
                    Toast.makeText(this, "Doesn't exist", Toast.LENGTH_SHORT).show()
                    var fromName = ""
                    var toName = ""
                    db.collection("Users").document(username)
                        .get()
                        .addOnSuccessListener {
                            if(it != null){
                                fromName = it.getString("Name").toString()
                                db.collection("Users").document(userItemSearch.username)
                                    .get()
                                    .addOnSuccessListener {it2 ->
                                        if(it2 != null){
                                            toName = it2.getString("Name").toString()
                                            notAlready(username,userItemSearch.username, fromName, toName)
                                            return@addOnSuccessListener
                                        }
                                    }
                            }
                        }

                }
        }
        rvNewChats.adapter = adapter
    }
}

class UserItem(val username: String,val url: String, val Name: String, val arrayUser: MutableList<usersList>): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.new_chat_adapter
    }

    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val temp = usersList(username, "", Name, url)
        arrayUser.add(temp)
        viewHolder.itemView.tv_usernames_newMessage.text = username
        Picasso.get().load(url).into(viewHolder.itemView.cv_dp_newMessage)
        Log.d("adapter", "adapter added")
    }
}

class UserItemSearch(val username: String,val url: String, val Name: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.new_chat_adapter
    }

    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tv_usernames_newMessage.text = username
        Picasso.get().load(url).into(viewHolder.itemView.cv_dp_newMessage)
        Log.d("adapter", "adapter added")
    }
}
