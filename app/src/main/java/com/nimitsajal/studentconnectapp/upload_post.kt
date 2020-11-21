package com.nimitsajal.studentconnectapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.btnBack
import kotlinx.android.synthetic.main.activity_chat.circularImageView
import kotlinx.android.synthetic.main.activity_profile_page.*
import kotlinx.android.synthetic.main.activity_profile_page.tvUsername
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_up.btnDP
import kotlinx.android.synthetic.main.activity_upload_post.*
import java.util.*

class upload_post : AppCompatActivity() {

    var selectedUri: Uri? = null
    var userDpUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_post)

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
                    tvUsername.text = username
                    displayCredentials(username!!, db)
                }
                else{
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
            }
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, mainFeed::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
        }

        btnDP.setOnClickListener {
            photoPicker()
        }

        btnUpload.setOnClickListener {
            val description = etDescription.text.toString()
            if(username != null){
                if(selectedUri != null){
                    if(userDpUrl != null){
                        uploadPost(username!!, description, selectedUri!!, userDpUrl, db)
                    }
                }
            }
        }
    }

    private fun uploadPost(username: String, description: String, imageUri: Uri, userDpUrl: String, db: FirebaseFirestore){
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("images/uploads/$username/$filename")
        ref.putFile(imageUri)
            .addOnSuccessListener { img ->
                Log.d(
                    "Registration",
                    "Image successfully uploaded at location: ${img.metadata?.path}"
                )
                ref.downloadUrl
                    .addOnSuccessListener { img_link ->
                        val time = FieldValue.serverTimestamp()
                        val post = hashMapOf(
                            "From" to username,
                            "Description" to description,
                            "Dp" to userDpUrl,
                            "Likes" to 0,
                            "Picture" to img_link.toString(),
                            "Time" to time
                        )
                        val info = hashMapOf(
                            "Info" to "Info",
                        )
                        val linkPost = hashMapOf(
                            "Liked" to false,
                            "Time" to time
                        )
                        db.collection("Post").add(post)
                            .addOnSuccessListener {postObject ->
                                db.collection("Post").document(postObject.id).collection("Comments").document("Info")
                                    .set(info)
                                    .addOnSuccessListener {
                                        db.collection("Post").document(postObject.id).collection("Tags").document("Info")
                                            .set(info)
                                            .addOnSuccessListener {
                                                db.collection("Users").document(username).collection("My Posts").document(postObject.id)
                                                    .set(linkPost)
                                                    .addOnSuccessListener {
                                                        db.collection("Users").document(username).collection("Friends")
                                                            .get()
                                                            .addOnSuccessListener {friendList ->
                                                                for(document in friendList){
                                                                    if(document.id != "Info"){
                                                                        db.collection("Users").document(document.id).collection("My Feed").document(postObject.id)
                                                                            .set(linkPost)
                                                                            .addOnSuccessListener {
                                                                                Log.d("post", "Post in the feed of ${document.id}")
                                                                            }
                                                                    }
                                                                }
                                                                val intent = Intent(this, mainFeed::class.java)
                                                                intent.putExtra("username", username)
                                                                startActivity(intent)
                                                                finish()
                                                            }
                                                    }
                                            }
                                    }
                            }
                    }
            }
            .addOnFailureListener {
                Log.d("Registration", "Image upload failed: ${it.message}")
            }
    }

    private fun displayCredentials(username: String, db: FirebaseFirestore){
        db.collection("Users").document(username)
            .get()
            .addOnSuccessListener {
                if(it != null){
                    userDpUrl = it.getString("Picture").toString()
                    Picasso.get().load(userDpUrl).into(circularImageView)
                }
            }
    }



    private fun photoPicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
//            Toast.makeText(this, "Photo was selected", Toast.LENGTH_SHORT).show()
            selectedUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedUri)
            squareImageView.setImageBitmap(bitmap)
            btnDP.alpha = 0f

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            btnDP.setBackgroundDrawable(bitmapDrawable)
        }
    }

}