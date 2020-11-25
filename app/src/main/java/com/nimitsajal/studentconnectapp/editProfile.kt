package com.nimitsajal.studentconnectapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.tvUsername
import kotlinx.android.synthetic.main.activity_new_chat.btnBack
import kotlinx.android.synthetic.main.activity_sign_up.*

class editProfile : AppCompatActivity() {

    private var selectedPhotoUrl: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        var username = intent.getStringExtra("username")

        var userinfo = userInfo("", "", "", "", "", null)

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid.toString())
            user_table.get().addOnSuccessListener { result ->
                if(result != null){
                    username = result.getString("Username").toString()
                    Log.d("profilePage", username.toString())
                    val user_info = db.collection("Users").document(username!!)
                    user_info.get().addOnSuccessListener {
                        if (it != null) {
                            userinfo.username = username.toString()
                            userinfo.url = it.getString("Picture").toString()
                            userinfo.name = it.getString("Name").toString()
                            userinfo.phonenumber = it.getString("Phone Number").toString()
                            userinfo.description = it.getString("Description").toString()
                            getUser(userinfo)
                        }
                    }
                    //getUser(username!!, db)
                }
                else{
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
            }
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
        }

        btnSave.setOnClickListener {
            updateDetails(userinfo, selectedPhotoUrl!!, db)
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
            selectedPhotoUrl = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUrl)
            circularImageView.setImageBitmap(bitmap)
            btnDP.alpha = 0f

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            btnDP.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun updateDetails(userinfo: userInfo, selectedPhotoUrl: Uri?, db: FirebaseFirestore){
        val description = etDescriptionEdit.text.toString()
        val name = etNameEdit.text.toString()
        val phonenumber = etPhoneEdit.text.toString()

        if(description != userinfo.description){
            db.collection("Users").document(userinfo.username)
                .update("Description", description)
                .addOnSuccessListener {
                    Log.d("editprofile", "Description updated $description")
                }
        }
        if(name != userinfo.name){
            db.collection("Users").document(userinfo.username)
                .update("Name", name)
                .addOnSuccessListener {
                    Log.d("editprofile", "Name updated $name")
                }
        }
        if(phonenumber != userinfo.phonenumber){
            db.collection("Users").document(userinfo.username)
                .update("Phone Number", phonenumber)
                .addOnSuccessListener {
                    Log.d("editprofile", "Phone Number updated $phonenumber")
                }
        }
    }

    private fun getUser(userinfo: userInfo)
    {
        tvUsername.setText(userinfo.username.toString()).toString()

                userinfo.uri = Uri.parse(userinfo.url)
//                val selectedPhotoUrl = selectedPhotoUrl_string.toUri()
//                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUrl)
//                circularImageView.setImageBitmap(bitmap)


                Picasso.get().load(userinfo.url).into(object :
                    com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        // loaded bitmap is here (bitmap)
                        circularImageViewEdit.setImageBitmap(bitmap)
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                })


                etNameEdit.setText(userinfo.name).toString()

                etPhoneEdit.setText(userinfo.phonenumber).toString()

                if(userinfo.description != ""){
                    etDescriptionEdit.setText(userinfo.description).toString()
                }

            }

}

data class userInfo(var username: String, var name: String, var url: String, var phonenumber: String, var description: String, var uri: Uri?){

}

