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
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.tvUsername
import kotlinx.android.synthetic.main.activity_new_chat.btnBack
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.regex.Pattern

class editProfile : AppCompatActivity() {

    private var selectedPhotoUrl: Uri? = null
    private var Dpempty: Boolean = false

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

        btnDp.setOnClickListener {
            photoPicker()
//            putDpInCircularView()
        }

        btnRemoveDP.setOnClickListener{
            Dpempty = true
            selectedPhotoUrl = null
            //circularImageViewEdit.isVisible=false
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/student-connect-b96e6.appspot.com/o/user_dp%2Fuser_default_dp.png?alt=media&token=4a2736ef-c5cb-4845-9d0f-894e7bf3c6a2").into(object :
                com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    // loaded bitmap is here (bitmap)
                    circularImageViewEdit.setImageBitmap(bitmap)
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
            })
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
        }
        btnSave.setOnClickListener {
//            Toast.makeText(this,"Clicked name: ${etNameEdit.text.toString()} , phone number ${etPhoneEdit.text.toString()} ", Toast.LENGTH_SHORT).show()
            updateDetails(userinfo,selectedPhotoUrl,db,Dpempty)
//            Toast.makeText(this,"Clicked name: ${etNameEdit.text.toString()} , phone number ${etPhoneEdit.text.toString()} ", Toast.LENGTH_SHORT).show()

            Log.d("Editprofile", "name: ${etNameEdit.text.toString()} , phone number ${etPhoneEdit.text.toString()}, description ${etDescriptionEdit.text.toString()}")
            Log.d("Editprofile", "name ${userinfo.name} , phone number ${userinfo.phonenumber} , description ${userinfo.description}")
        }
    }

    private fun photoPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
//            Toast.makeText(this, "Photo was selected", Toast.LENGTH_SHORT).show()
            selectedPhotoUrl = data.data
            Dpempty = false
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUrl)
            circularImageViewEdit.setImageBitmap(bitmap)
            btnDp.alpha = 0f

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            btnDP.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun updateDetails(userinfo: userInfo, selectedPhotoUrl: Uri?, db: FirebaseFirestore, Dpempty: Boolean){
        val description = etDescriptionEdit.text.toString()
        val name = etNameEdit.text.toString()
        val phonenumber = etPhoneEdit.text.toString()
        Log.d("Editprofile", "name: $name , phone number $phonenumber, description $description")

        if(isValidMobile(phonenumber))
        {
            if(Dpempty)
            {
                val url = "https://firebasestorage.googleapis.com/v0/b/student-connect-b96e6.appspot.com/o/user_dp%2Fuser_default_dp.png?alt=media&token=4a2736ef-c5cb-4845-9d0f-894e7bf3c6a2"
                db.collection("Users").document(userinfo.username)
                    .update("Picture", url)
                    .addOnSuccessListener {
                        Log.d("Editprofile", "Profile Updated $url")
                    }
                db.collection("Users").document(userinfo.username).collection("Friends")
                    .addSnapshotListener { value, error ->
                        if(value == null || error != null){
                            Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
                            return@addSnapshotListener
                        }
                        for(document in value.documents){
                            if(document.id != "Info"){
                                db.collection("Users").document(document.id).collection("Friends")
                                    .document(userinfo.username)
                                    .update("Picture", url)
                                    .addOnSuccessListener {
                                        Log.d("Editprofile", "Profile of ${document.id} Updated $url")
                                    }
                            }
                        }
                    }
                val filename = userinfo.username.toString()
                val storageRef = FirebaseStorage.getInstance()
                val ref = storageRef.getReference("images/dp/$filename")
                    .delete()
                    .addOnSuccessListener {
                        Log.d("editprofile","Old dp deleted")
                    }
                    .addOnFailureListener{
                        //Already No dp
                        Log.d("editprofile","Already no dp")
                    }
            }
            else
            {
                if(selectedPhotoUrl!=null)
                {
                    Log.d("Editprofile", "Selected photo url not null")
                    val filename = userinfo.username.toString()
                    val storageRef = FirebaseStorage.getInstance()
                    val ref = storageRef.getReference("images/dp/$filename")
                        .delete()
                        .addOnSuccessListener {
                            Log.d("editprofile", "Deleted")
                            val uploading = storageRef.getReference("images/dp/$filename")
                            uploading.putFile(selectedPhotoUrl)
                                .addOnSuccessListener { img ->
                                    Log.d("Editprofile", "Image successfully uploaded at location: ${img.metadata?.path}")
                                    uploading.downloadUrl
                                        .addOnSuccessListener {img_link->
                                            val url = img_link.toString()
                                            db.collection("Users").document(userinfo.username)
                                                .update("Picture", url)
                                                .addOnSuccessListener {
                                                    Log.d("Editprofile", "Profile Updated $url")
                                                }
                                            db.collection("Users").document(userinfo.username).collection("Friends")
                                                .addSnapshotListener { value, error ->
                                                    if(value == null || error != null){
                                                        Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
                                                        return@addSnapshotListener
                                                    }
                                                    for(document in value.documents){
                                                        if(document.id != "Info"){
                                                            db.collection("Users").document(document.id).collection("Friends")
                                                                .document(userinfo.username)
                                                                .update("Picture", url)
                                                                .addOnSuccessListener {
                                                                    Log.d("Editprofile", "Profile of ${document.id} Updated $url")
                                                                }
                                                        }
                                                    }
                                                }

                                        }
                                }

                        }
                        .addOnFailureListener{
                            Log.d("editprofile", "Not Deleted")
                            val uploading = storageRef.getReference("images/dp/$filename")
                            uploading.putFile(selectedPhotoUrl)
                                .addOnSuccessListener { img ->
                                    Log.d("Editprofile", "Image successfully uploaded at location: ${img.metadata?.path}")
                                    uploading.downloadUrl
                                        .addOnSuccessListener {img_link->
                                            val url = img_link.toString()
                                            db.collection("Users").document(userinfo.username)
                                                .update("Picture", url)
                                                .addOnSuccessListener {
                                                    Log.d("Editprofile", "Profile Updated $url")
                                                }
                                            db.collection("Users").document(userinfo.username).collection("Friends")
                                                .addSnapshotListener { value, error ->
                                                    if(value == null || error != null){
                                                        Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
                                                        return@addSnapshotListener
                                                    }
                                                    for(document in value.documents){
                                                        if(document.id != "Info"){
                                                            db.collection("Users").document(document.id).collection("Friends")
                                                                .document(userinfo.username)
                                                                .update("Picture", url)
                                                                .addOnSuccessListener {
                                                                    Log.d("Editprofile", "Profile of ${document.id} Updated $url")
                                                                }
                                                        }
                                                    }
                                                }

                                        }
                                }
                        }

                }
            }
            if(description != userinfo.description){
                db.collection("Users").document(userinfo.username)
                    .update("Description", description)
                    .addOnSuccessListener {
                        Log.d("Editprofile", "Description updated $description")
                    }
            }
            if(name != userinfo.name){
                db.collection("Users").document(userinfo.username)
                    .update("Name", name)
                    .addOnSuccessListener {
                        Log.d("Editprofile", "Name updated $name")
                    }
            }
            if(phonenumber != userinfo.phonenumber){
                db.collection("Users").document(userinfo.username)
                    .update("Phone Number", phonenumber)
                    .addOnSuccessListener {
                        Log.d("Editprofile", "Phone Number updated $phonenumber")
                    }
            }
            val intent = Intent(this, mainFeed::class.java)
            intent.putExtra("username", userinfo.username)
            startActivity(intent)
            finish()
        }
        else
        {
            Toast.makeText(this, "Phone number is Invalid!", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun isValidMobile(phone: String): Boolean {
        if ((Pattern.matches("6[0-9]+", phone) or Pattern.matches(
                "7[0-9]+",
                phone
            ) or Pattern.matches("8[0-9]+", phone) or Pattern.matches(
                "9[0-9]+",
                phone
            )) && phone.length == 10
        ) {
            return true
        }
        return false
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