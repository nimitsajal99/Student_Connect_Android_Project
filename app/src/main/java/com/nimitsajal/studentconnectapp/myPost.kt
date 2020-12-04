package com.nimitsajal.studentconnectapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_my_post.*
import kotlinx.android.synthetic.main.activity_profile_page.*
import kotlinx.android.synthetic.main.post_adapter_cardiew.view.*
import kotlinx.android.synthetic.main.profile_post_adapter.view.*

class myPost : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_post)

        var username = ""
        var myUsername = intent.getStringExtra("username")
        var url = intent.getStringExtra("picture")
        var uid = intent.getStringExtra("uid")
        var description = intent.getStringExtra("description")
        var dp = intent.getStringExtra("dp")
        var isOther = intent.getStringExtra("others")



        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid.toString())
            user_table.get().addOnSuccessListener { result ->
                if(result != null){
                    username = result.getString("Username").toString()
                    Log.d("profilePage", username.toString())
                    loadPost(db, username!!, url!!, uid!!, description!!, dp!!, myUsername!!, isOther!!)
                }
                else{
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
            }
        }

        btnUnlike.setOnClickListener {
            btnLike.isVisible = true
            btnUnlike.isVisible = false
            liked(true, username!!, uid!!, db)
        }
        btnLike.setOnClickListener {
            btnLike.isVisible = false
            btnUnlike.isVisible = true
            liked(false, username!!, uid!!, db)
        }
        btnBack.setOnClickListener {
            goToProfile(username!!, isOther!!)
        }

        etCommentBox.addTextChangedListener{
            if(etCommentBox.text.toString() == ""){
                btnCommentDisabled.isVisible = true
                btnCommentEnabled.isVisible = false
            }
            else{
                btnCommentDisabled.isVisible = false
                btnCommentEnabled.isVisible = true
            }
        }

        btnCommentEnabled.setOnClickListener {
            if(etCommentBox.text.toString() != ""){
                val comment = etCommentBox.text.toString()
                etCommentBox.setText("")
                commented(comment, username!!, uid!!, db)
            }
        }

        btnDeletePost.setOnClickListener {
            //TODO: menu inflate / dialog box inflate "Are you sure?"
            deletePost(username!!, uid!!, db)
        }

    }

    private fun deletePost(username: String, uid: String, db: FirebaseFirestore){
        db.collection("Users").document(username).collection("My Posts").document(uid)
            .delete()
            .addOnSuccessListener {
                Log.d("mainfeed", "post deleted from $username")
            }
        db.collection("Post").document(uid)
            .delete()
            .addOnSuccessListener {
                Log.d("mainfeed", "post deleted from Posts")
            }
        Toast.makeText(this, "Post DELETED", Toast.LENGTH_SHORT).show()
        goToProfile(username, "false")
    }

    private fun commented(comment: String, username: String, uid: String, db: FirebaseFirestore){
        val time = FieldValue.serverTimestamp()
        val commenting = hashMapOf(
            "Text" to comment,
            "Timestamp" to time,
            "From" to username
        )
        db.collection("Post").document(uid).collection("Comments")
            .add(commenting)
            .addOnSuccessListener {
                if(it != null){
                    Log.d("mainfeed", "Post updated - comment added")
                }
                val adapterComments = GroupAdapter<GroupieViewHolder>()
                db.collection("Post").document(uid).collection("Comments")
                    .orderBy("Timestamp", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener { it3 ->
                        if (it3 != null) {
                            adapterComments.clear()
                            for (doc in it3.documents) {
                                if (doc.id != "Info") {
                                    adapterComments.add(Comment_class(doc["From"].toString(), doc["Text"].toString()))
                                }
                            }
                            rvComments.adapter = null
                            rvComments.adapter = adapterComments
                        }
                    }
            }
    }

    private fun goToProfile(username: String, isOther: String){
        Toast.makeText(this, "$isOther", Toast.LENGTH_SHORT).show()
        if(isOther == "true"){
            onBackPressed()
        }
        else{
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun loadPost(db: FirebaseFirestore, username: String, url: String, uid: String, description: String, dp: String, myUsername: String, isOther: String){
        Picasso.get().load(url).into(postImageCard)
        tvUsernameCard.text = myUsername
        tvDescriptionCard.text = description

        if(isOther != "true"){
            btnDeletePost.isVisible = true
        }

        Picasso.get().load(dp).into(object :
            com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                // loaded bitmap is here (bitmap)

                circularImageViewCard.setImageBitmap(bitmap)
            }
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
        })

        db.collection("Post").document(uid)
            .get()
            .addOnSuccessListener {
                if(it != null){
                    var likeCount = it["Likes"].toString().toInt()
                    if(likeCount == 0){
                        val likes = "No Likes Yet"
                        tvLikeCount.text = likes
                    }
                    else if(likeCount == 1){
                        val likes = "1 Like"
                        tvLikeCount.text = likes
                    }
                    else{
                        val likes = "$likeCount Likes"
                        tvLikeCount.text = likes
                    }
                }
            }

        val adapterComments = GroupAdapter<GroupieViewHolder>()
        db.collection("Post").document(uid).collection("Comments")
            .orderBy("Timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { it3 ->
                if (it3 != null) {
                    adapterComments.clear()
                    for (doc in it3.documents) {
                        if (doc.id != "Info") {
                            adapterComments.add(Comment_class(doc["From"].toString(), doc["Text"].toString()))
                        }
                    }
                    rvComments.adapter = adapterComments
                }
            }

        db.collection("Users").document(username).collection("My Posts").document(uid)
            .get()
            .addOnSuccessListener {
                if(it != null){
                    if(it["Liked"].toString().toBoolean()){
                        btnLike.isVisible = true
                        btnUnlike.isVisible = false
                    }
                    else{
                        btnLike.isVisible = false
                        btnUnlike.isVisible = true
                    }
                }
            }
    }

    private fun liked(liking: Boolean, username: String, uid: String, db: FirebaseFirestore){
//        adapter.clear()

        db.collection("Post").document(uid)
            .get()
            .addOnSuccessListener {
                if(it  != null){
                    var likeCount = it["Likes"].toString().toInt()
                    if(liking){
                        likeCount += 1
                    }
                    else{
                        likeCount -= 1
                    }
                    db.collection("Post").document(uid)
                        .update("Likes", likeCount.toString())
                        .addOnSuccessListener {
                            Log.d("mypost", "Post likes updated")
                        }
                    if(likeCount == 0){
                        val likes = "No Likes Yet"
                        tvLikeCount.text = likes
                    }
                    else if(likeCount == 1){
                        val likes = "1 Like"
                        tvLikeCount.text = likes
                    }
                    else{
                        val likes = "$likeCount Likes"
                        tvLikeCount.text = likes
                    }
                }
            }

        if(liking){
            db.collection("Users").document(username).collection("My Posts").document(uid)
                .update("Liked", true)
                .addOnSuccessListener {
                    if(it != null){
                        Log.d("mainfeed", "User updated - like updated")
                    }
                }
        }
        else{
            db.collection("Users").document(username).collection("My Posts").document(uid)
                .update("Liked", false)
                .addOnSuccessListener {
                    if(it != null){
                        Log.d("mainfeed", "User updated - like updated")
                    }
                }
        }
    }
}