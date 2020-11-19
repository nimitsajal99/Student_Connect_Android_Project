package com.nimitsajal.studentconnectapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_recieve.view.*
import kotlinx.android.synthetic.main.chat_recieve.view.tvRecieve
import kotlinx.android.synthetic.main.chat_send.view.*
import kotlinx.android.synthetic.main.chat_send.view.tvSend
import kotlinx.android.synthetic.main.chat_system.view.*
import kotlinx.android.synthetic.main.current_chat_adapter.view.*
import kotlinx.android.synthetic.main.new_chat_adapter.view.*
import kotlinx.android.synthetic.main.new_chat_adapter.view.tv_usernames_newMessage

class chat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        var url = ""
        var To = intent.getStringExtra("to")
        var From = intent.getStringExtra("from")
        username.text = To
        val db = FirebaseFirestore.getInstance()
        if (To != null) {
            db.collection("Users").document(To)
                .get()
                .addOnSuccessListener {
                    if(it != null){
                        url = it.getString("Picture").toString()
                        Log.d("profilePage", "URL recieved = $url")
                        loadDP(url)
                    }
                    else{
                        Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }
                }
        }

        btnSendMessage_chat.setOnClickListener {
            val text = etTypeMEssage_chat.text.toString()
            if(text != null || text != ""){
                if (To != null) {
                    if (From != null) {
                        sendMessage(text, To, From)
                        etTypeMEssage_chat.text.clear()
                    }
                }

            }

        }

        btnBack.setOnClickListener {

        }

        if (To != null) {
            if (From != null) {
                loadChat(To, From)
            }
        }
    }

    private  fun sendMessage(text: String, To: String, From: String){
        val db = FirebaseFirestore.getInstance()
        val message = hashMapOf(
            "From" to From,
            "To" to To,
            "Text" to text,
            "Time" to FieldValue.serverTimestamp()
        )
        val send = db.collection("Users").document(From).collection("Chats").document(To)
        val recieve = db.collection("Users").document(To).collection("Chats").document(From)
        send.update(message)
            .addOnFailureListener {
                Toast.makeText(this, "Message not Updated", Toast.LENGTH_LONG).show()
            }
        recieve.update(message)
            .addOnFailureListener {
                Toast.makeText(this, "Message not Updated", Toast.LENGTH_LONG).show()
            }
        send.collection("Next")
            .add(message)
            .addOnFailureListener {
                Toast.makeText(this, "Message not Sent", Toast.LENGTH_LONG).show()
            }
        recieve.collection("Next")
            .add(message)
            .addOnFailureListener {
                Toast.makeText(this, "Message not Sent", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadDP(url: String){
        Picasso.get().load(url).into(circularImageView)
    }

    private fun loadChat(To: String, From: String){
        val adapter = GroupAdapter<GroupieViewHolder>()
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(From).collection("Chats").document(To).collection("Next")
            .orderBy("Time", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if(value == null || error != null){
                    Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                adapter.clear()
                for(document in value.documents){
                    if(document["From"] == "System"){
                        adapter.add(System(document["Text"].toString()))
                        recyclerView_chat.scrollToPosition(adapter.itemCount-1)
                    }
                    else if(document["From"] == From){
                        adapter.add(Send(document["Text"].toString()))
                        recyclerView_chat.scrollToPosition(adapter.itemCount-1)
                    }
                    else{
                        adapter.add(Recieve(document["Text"].toString()))
                        recyclerView_chat.scrollToPosition(adapter.itemCount-1)
                    }
                }
                recyclerView_chat.adapter = adapter
            }
    }
}

class Send(val text: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_send
    }
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvSend.text = text
        Log.d("adapter", "adapter added")
    }
}

class Recieve(val text: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_recieve
    }
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvRecieve.text = text
        Log.d("adapter", "adapter added")
    }
}

class System(val text: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_system
    }
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvSystem.text = text
        Log.d("adapter", "adapter added")
    }
}

