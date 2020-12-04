package com.nimitsajal.studentconnectapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.activity_others_profile_page.*
import kotlinx.android.synthetic.main.activity_profile_page.*
import kotlinx.android.synthetic.main.details_adapter.view.*

class others_profile_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_others_profile_page)

        var username: String = ""
        var isFriend: Boolean = false
        var friend: isFriend = isFriend(false, 0, "", "")

        var usernameOthers = intent.getStringExtra("usernameOthers")
        tvUsernameOthers.setText("").toString()
        //tvUsername.setText(username).toString()

        val mLayoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        rvProfilePageOthers.layoutManager = mLayoutManager

        tvDescriptionOthers.setText("").toString()

        tvNameOthers.setText("").toString()

        btnFriendsOthers.setText("").toString()

        btnMessageOthers.setText("Message").toString()

        val adapterPost = GroupAdapter<GroupieViewHolder>()

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid.toString())
            user_table.get().addOnSuccessListener { result ->
                if(result != null){
                    username = result.getString("Username").toString()
                    Log.d("profilePage", username.toString())
                    if (usernameOthers != null) {
                        db.collection("Users").document(username).collection("Friends")
                            .document(usernameOthers)
                            .get()
                            .addOnCompleteListener {
                                if(it.isSuccessful){
                                    val temp = it.getResult()
                                    if (temp != null) {
                                        if(temp.exists()){
                                            btnFriendsOthers.text = "Unfriend"
                                            friend.isFriend = true
                                            //Toast.makeText(this, "isFriend = true", Toast.LENGTH_SHORT).show()
                                        }
                                        else{
                                            btnFriendsOthers.text = "Add Friend"
                                            friend.isFriend = false
                                            //Toast.makeText(this, "isFriend = false", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                getUser(auth,usernameOthers, db, friend, adapterPost)
                            }
                    }
                }
                else{
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
            }
        }

        btnChatOthers.setOnClickListener {
            val intent = Intent(this, currentChats::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnLogoutOthers.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        btnProfileOthers.setOnClickListener {
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnFeedOthers.setOnClickListener {
            val intent = Intent(this, mainFeed::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnFriendsOthers.setOnClickListener {
            if (usernameOthers != null && username != null) {
                btnFriendsOthers.isEnabled = false
                friend(username, usernameOthers, db, friend, adapterPost)
            }
        }

        btnMessageOthers.setOnClickListener {
            if (usernameOthers != null && username != null){
                btnMessageOthers.isEnabled = false
                chat(username, db, friend, usernameOthers)
            }
        }
    }

    private fun loadPost(db: FirebaseFirestore, username: String, adapter: GroupAdapter<GroupieViewHolder>, dp: String){
        rvProfilePageOthers.adapter = null
        adapter.clear()
        db.collection("Users").document(username).collection("My Posts")
            .orderBy("Time", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                if(it != null){
                    for(document in it){
                        if(document.id != "Info"){
                            var comCount = 0
                            db.collection("Post").document(document.id)
                                .get()
                                .addOnSuccessListener { it2 ->
                                    if(it2 != null){
                                        db.collection("Post").document(document.id).collection("Comments")
                                            .get()
                                            .addOnSuccessListener {it3 ->
                                                if(it3 != null){
                                                    comCount = it3.size()-1
                                                    adapter.add(profile_post_class(it2["Picture"].toString(), it2["Likes"].toString().toInt(), comCount, it2["Description"].toString(), username, db, document.id))
                                                }
                                            }
                                    }
                                }
                        }
                    }
                }
                adapter.setOnItemLongClickListener { item, view ->
//                    Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
                    val post: profile_post_class = item as profile_post_class
                    val intent = Intent(this, myPost::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("picture", post.url)
                    intent.putExtra("uid", post.uid)
                    intent.putExtra("description", post.description)
                    intent.putExtra("dp", dp)
                    intent.putExtra("others", "true")
                    startActivity(intent)
                    return@setOnItemLongClickListener true
                }
                rvProfilePageOthers.adapter = adapter
            }
    }

    private fun friend(username: String, usernameOthers: String, db: FirebaseFirestore, friend: isFriend, adapterPost: GroupAdapter<GroupieViewHolder>){
        if(friend.isFriend){
            db.collection("Users").document(username).collection("Friends").document(usernameOthers)
                .delete()
                .addOnCompleteListener{
                    if(it.isSuccessful){
                        Log.d("others", "userOther removed from user")
                    }
                }
            db.collection("Users").document(usernameOthers).collection("Friends").document(username)
                .delete()
                .addOnCompleteListener {it2 ->
                    if(it2.isSuccessful){
                        Toast.makeText(this, "$usernameOthers is no longer a Friend", Toast.LENGTH_SHORT).show()
                        friend.isFriend = false
                        friend.count = friend.count-1
                        val str = "Add Friend (" + friend.count + ")"
                        btnFriendsOthers.text = str
                        btnFriendsOthers.isEnabled = true
                    }
                }
            rvProfilePageOthers.adapter = null
        }
        else{
            var dpUsername = ""
            var name = ""
            db.collection("Users").document(username)
                .get()
                .addOnSuccessListener {
                    if(it != null){
                        dpUsername = it.getString("Picture").toString()
                        name = it.getString("Name").toString()
                        Log.d("others", "user name = $name, dp = $dpUsername")
                                    val user = hashMapOf(
                                        "Picture" to dpUsername,
                                        "Name" to name
                                    )
                                    val userOthers = hashMapOf(
                                        "Picture" to friend.url,
                                        "Name" to friend.name
                                    )
                                    db.collection("Users").document(username).collection("Friends").document(usernameOthers)
                                        .set(userOthers)
                                        .addOnCompleteListener {it3 ->
                                            if(it3.isSuccessful){
                                                Log.d("others", "user other added in user")
                                            }
                                            else{
                                                Log.d("others", "inner user not entered = $it3")
                                            }
                                        }
                                    db.collection("Users").document(usernameOthers).collection("Friends").document(username)
                                        .set(user)
                                        .addOnCompleteListener {it4 ->
                                            if(it4.isSuccessful){
                                                Toast.makeText(this, "${friend.name} added as a Friend", Toast.LENGTH_SHORT).show()
                                                friend.isFriend = true
                                                friend.count = friend.count+1
                                                val str = "Unfriend (" + friend.count + ")"
                                                btnFriendsOthers.text = str
                                                btnFriendsOthers.isEnabled = true
                                            }
                                        }
                                        .addOnFailureListener {it5 ->
                                            Log.d("others", "outer user not entered = ${it5.message}")
                                        }
                        loadPost(db, usernameOthers, adapterPost, friend.url)
                                }
                }
        }
    }

    //TODO: search if document exists optimisation
    private fun chat(username: String, db: FirebaseFirestore, friend: isFriend, usernameOthers: String){
        db.collection("Users").document(username).collection("Chats").document(usernameOthers)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val temp = it.getResult()
                    if (temp != null) {
                        if(temp.exists()){
                            val intent = Intent(this, chat::class.java)
                            intent.putExtra("from", username)
                            intent.putExtra("to", usernameOthers)
                            startActivity(intent)
                            finish()
                        }
                        else{
                            Log.d("others", "inner")
                            newChat(username, db, friend, usernameOthers)
                        }
                    }
                }
            }
    }

    private fun newChat(username: String, db: FirebaseFirestore, friend: isFriend, usernameOthers: String){

        if(friend.isFriend){
            val message = hashMapOf(
                "From" to "System",
                "To" to usernameOthers,
                "Text" to "Say Hi",
                "Time" to FieldValue.serverTimestamp()
            )

            val messageFrom = hashMapOf(
                "From" to "System",
                "To" to usernameOthers,
                "Text" to "Say Hi",
                "Name" to usernameOthers,
                "Time" to FieldValue.serverTimestamp()
            )

            val messageTo = hashMapOf(
                "From" to "System",
                "To" to usernameOthers,
                "Text" to "Say Hi",
                "Name" to username,
                "Time" to FieldValue.serverTimestamp()
            )

            val info = hashMapOf(
                "Info" to "Info"
            )

            db.collection("Users").document(username).collection("Chats").document(usernameOthers)
                .set(messageFrom)
                .addOnCompleteListener{
                    if(it.isSuccessful)
                    {
                        db.collection("Users").document(username).collection("Chats").document(usernameOthers).collection("Next")
                            .add(message)
                            .addOnCompleteListener{it1->
                                if(it1.isSuccessful)
                                {
                                    Log.d("others", "chat created in user")
                                }

                            }
                    }
                }
            db.collection("Users").document(usernameOthers).collection("Chats").document(username)
                .set(messageTo)
                .addOnCompleteListener{it2->
                    if(it2.isSuccessful)
                    {
                        db.collection("Users").document(usernameOthers).collection("Chats").document(username).collection("Next")
                            .add(message)
                            .addOnCompleteListener{it3->
                                if(it3.isSuccessful)
                                {
                                    val intent = Intent(this, chat::class.java)
                                    intent.putExtra("from", username)
                                    intent.putExtra("to", usernameOthers)
                                    startActivity(intent)
                                    btnMessageOthers.isEnabled = true
                                    finish()
                                }

                            }
                    }

                }
        }
        else{
            val message = hashMapOf(
                "From" to "System",
                "To" to usernameOthers,
                "Text" to "Say Hi",
                "Time" to FieldValue.serverTimestamp()
            )

            val messageFrom = hashMapOf(
                "From" to "System",
                "To" to usernameOthers,
                "Text" to "Why don't you add $usernameOthers as a Friend",
                "Name" to usernameOthers,
                "Time" to FieldValue.serverTimestamp()
            )

            val messageTo = hashMapOf(
                "From" to "System",
                "To" to usernameOthers,
                "Text" to "$username is not in your Friend List",
                "Name" to username,
                "Time" to FieldValue.serverTimestamp()
            )

            db.collection("Users").document(username).collection("Chats").document(usernameOthers)
                .set(messageFrom)
                .addOnCompleteListener{
                    if(it.isSuccessful)
                    {
                        db.collection("Users").document(username).collection("Chats").document(usernameOthers).collection("Next")
                            .add(message)
                            .addOnCompleteListener{it1->
                                if(it1.isSuccessful)
                                {
                                    Log.d("others", "chat created in user")
                                }

                            }
                    }
                }
            db.collection("Users").document(usernameOthers).collection("Chats").document(username)
                .set(messageTo)
                .addOnCompleteListener{it2->
                    if(it2.isSuccessful)
                    {
                        db.collection("Users").document(usernameOthers).collection("Chats").document(username).collection("Next")
                            .add(message)
                            .addOnCompleteListener{it3->
                                if(it3.isSuccessful)
                                {
                                    val intent = Intent(this, chat::class.java)
                                    intent.putExtra("from", username)
                                    intent.putExtra("to", usernameOthers)
                                    startActivity(intent)
                                    btnMessageOthers.isEnabled = true
                                    finish()
                                }

                            }
                    }

                }
        }


    }

    private fun getUser(auth: FirebaseAuth, username: String, db: FirebaseFirestore, friend: isFriend, adapterPost: GroupAdapter<GroupieViewHolder>){


        tvUsernameOthers.setText(username.toString()).toString()

        val user_info = db.collection("Users").document(username)
        user_info.get().addOnSuccessListener {
            if(it != null){

                var selectedPhotoUrl_string = it.getString("Picture").toString()
                val selectedPhotoUrl = Uri.parse(selectedPhotoUrl_string)
//                val selectedPhotoUrl = selectedPhotoUrl_string.toUri()
//                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUrl)
//                circularImageView.setImageBitmap(bitmap)

                friend.url = selectedPhotoUrl_string

                Picasso.get().load(selectedPhotoUrl_string).into(object :
                    com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        // loaded bitmap is here (bitmap)
                        circularImageViewOthers.setImageBitmap(bitmap)
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                })

                tvNameOthers.setText(it.getString("Name").toString()).toString()
                friend.name = it.getString("Name").toString()
                if(friend.isFriend == true){
                    loadPost(db, username, adapterPost, friend.url)
                }

                if(it.getString("Description").toString() == ""){
                    collapse(tvDescriptionOthers)
//                    tvDescription.setText(it.getString("Description").toString()).toString()
//                    expand(tvName, 999)
                }
                else{
                    expand(tvDescriptionOthers, 999)
                    tvDescriptionOthers.setText(it.getString("Description").toString()).toString()
                }

            }
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
                rvDetailsOthers.adapter = adapter
            }

        var friends = ""
        if(friend.isFriend == true){
            friends = "Unfriend ("
        }
        else{
            friends = "Add Friend ("
        }

        val FriendsCount = user_info.collection("Friends").get()
            .addOnSuccessListener {
                if(it != null){
                    friend.count = it.size() - 1
                    friends = friends + friend.count + ")"
                    btnFriendsOthers.setText(friends).toString()
                }
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
}

data class isFriend(var isFriend: Boolean, var count: Int, var name: String, var url: String){

}
