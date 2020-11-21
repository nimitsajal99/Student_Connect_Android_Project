package com.nimitsajal.studentconnectapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.muddzdev.styleabletoast.StyleableToast
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.current_chat_adapter.view.*
import kotlinx.android.synthetic.main.toast_login_adapter.*
import kotlinx.android.synthetic.main.toast_text_adapter.*
import kotlinx.android.synthetic.main.post_adapter.*
import kotlinx.android.synthetic.main.post_adapter.view.*

class mainFeed : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_feed)

        val adapter = GroupAdapter<GroupieViewHolder>()

        var arrayPost = mutableListOf<postList>()

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
                    loadFeed(arrayPost, adapter, username!!, db)
                }
                else{
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
            }
        }

//        btnEvent.setOnClickListener {
//            val layout = layoutInflater.inflate(R.layout.toast_login_adapter, toast_text)
//            tvToast.text = "This is a Sample Toast"
//            Toast(this).apply {
//                duration = Toast.LENGTH_SHORT
//                setGravity(Gravity.CENTER, 0, 0)
//                view = layout
//            }.show()
//        }

        btnUpload.setOnClickListener{
            val intent = Intent(this, upload_post::class.java)
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

    private fun loadFeed(arrayPost: MutableList<postList>, adapter: GroupAdapter<GroupieViewHolder>, username: String, db: FirebaseFirestore){
        val user = db.collection("Users").document(username).collection("My Feed")
        user
            .orderBy("Time", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
            if(value == null || error != null){
                Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            for(document in value.documents){
                if(document.id != "Info"){
                    db.collection("Post").document(document.id)
                        .get()
                        .addOnSuccessListener {
                            val temp = postList(it["From"].toString(), it["Picture"].toString(), it["Dp"].toString(), it["Description"].toString())
                            adapter.add(post_class(it["From"].toString(), it["Picture"].toString(), it["Dp"].toString(), it["Description"].toString()))
                        }
                }
            }
                rvFeed.adapter = adapter
        }
    }
}

data class postList(var username: String, var imageUrl: String, var dpUrl: String, var description: String){
}

class post_class(var username: String, var imageUrl: String, var dpUrl: String, var description: String): Item<GroupieViewHolder>(){
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvUsername.text = username
        viewHolder.itemView.tvDescription.text = description
        Picasso.get().load(dpUrl).into(viewHolder.itemView.circularImageView)
        Picasso.get().load(imageUrl).into(viewHolder.itemView.postImage)
    }

    override fun getLayout(): Int {
        return R.layout.post_adapter
    }
}
