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
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.tvUsername
import kotlinx.android.synthetic.main.activity_new_chat.btnBack
import kotlinx.android.synthetic.main.activity_profile_page.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_up.circularImageView
import java.util.regex.Pattern

class editProfile : AppCompatActivity() {

    private var selectedPhotoUrl: Uri? = null
    private var Dpempty: Boolean = false
    private lateinit var detector: GestureDetectorCompat

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
                            detector = GestureDetectorCompat(this,DiaryGestureListener(username))
                            getUser(userinfo)
                        }
                    }
                    //getUser(username!!, db)
                }
                else{
                    showToast("ERROR", 1)
                    return@addOnSuccessListener
                }
            }
        }

        detector = GestureDetectorCompat(this,DiaryGestureListener(username))

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

        btnExit.setOnClickListener {
            goToProfile(username!!, 2)
        }
        btnBack.setOnClickListener {
            goToProfile(username!!, 1)
        }
        btnSave.setOnClickListener {
//            Toast.makeText(this,"Clicked name: ${etNameEdit.text.toString()} , phone number ${etPhoneEdit.text.toString()} ", Toast.LENGTH_SHORT).show()
            updateDetails(userinfo,selectedPhotoUrl,db,Dpempty)
//            Toast.makeText(this,"Clicked name: ${etNameEdit.text.toString()} , phone number ${etPhoneEdit.text.toString()} ", Toast.LENGTH_SHORT).show()

            Log.d("Editprofile", "name: ${etNameEdit.text.toString()} , phone number ${etPhoneEdit.text.toString()}, description ${etDescriptionEdit.text.toString()}")
            Log.d("Editprofile", "name ${userinfo.name} , phone number ${userinfo.phonenumber} , description ${userinfo.description}")
        }
    }

    private fun goToProfile(username: String, type: Int)
    {
        //1 -> back
        //2 -> exit
        //3 -> save

        if(type == 1){
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            finish()
        }
        else if(type == 2){
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            overridePendingTransition(R.anim.zoom_out_exit, R.anim.static_transition)
            finish()
        }
        else{
            val intent = Intent(this, mainFeed::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            overridePendingTransition(R.anim.zoom_out_upload, R.anim.static_transition)
            finish()
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //Toast.makeText(this, "Swipe", Toast.LENGTH_SHORT).show()
        if(detector.onTouchEvent(event))
        {
            return true
        }
        else
        {
            return super.onTouchEvent(event)
        }

    }

    inner class DiaryGestureListener(username: String?) : GestureDetector.SimpleOnGestureListener()
    {
        private val username = username
        private val SWIPE_THREASHOLD = 100
        private val SWIPE_VELOCITY_THREASHOLD = 100


        override fun onFling(
            yAxisEvent: MotionEvent?,
            xAxisEvent: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            try {
                var diffX = xAxisEvent?.x?.minus(yAxisEvent!!.x) ?: 0.0F
                var diffY = yAxisEvent?.y?.minus(xAxisEvent!!.y) ?: 0.0F
                //Toast.makeText(this@mainFeed, "Swipe Right", Toast.LENGTH_SHORT).show()
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    //Left or Right Swipe
                    if (Math.abs(diffX) > SWIPE_THREASHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THREASHOLD) {
                        if (diffX > 0) {
                            //Right Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Right", Toast.LENGTH_SHORT).show()
                            return this@editProfile.onSwipeRight(username!!)
                        } else {
                            //Left Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Left", Toast.LENGTH_SHORT).show()
                            return this@editProfile.onSwipeLeft()
                        }
                    } else {
                        return false
                    }
                } else {
                    //Up or down Swipe
                    if (Math.abs(diffY) > SWIPE_THREASHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THREASHOLD) {
                        if (diffY > 0) {
                            //Up Swipe
                            return this@editProfile.onSwipeUp()
                        } else {
                            //Bottom Swipe
                            return this@editProfile.onSwipeBottom()

                        }
                    } else {
                        return false
                    }
                }

                return super.onFling(yAxisEvent, xAxisEvent, velocityX, velocityY)
            }
            catch (e: java.lang.Exception)
            {
                return false
            }
        }

    }

    private fun showToast(message: String, type: Int)
    {   //1 -> error
        //2 -> success
        //3 -> information

        if(type == 1){
            Log.d("toast", "$message")
            val toastView = layoutInflater.inflate(
                R.layout.toast_text_adapter,
                findViewById(R.id.toastLayout)
            )
            // Link Youtube -> https://www.youtube.com/watch?v=__GRhyvf6oE
            val textMessage = toastView.findViewById<TextView>(R.id.toastText)
            textMessage.text = message
            Log.d("toast", "${textMessage.text}")
            with(Toast(applicationContext))
            {
                duration = Toast.LENGTH_SHORT
                view = toastView
                show()
            }
        }
        else if(type == 2){
            Log.d("toast", "$message")
            val toastView = layoutInflater.inflate(
                R.layout.toast_text_successful,
                findViewById(R.id.toastLayoutSuccessful)
            )
            // Link Youtube -> https://www.youtube.com/watch?v=__GRhyvf6oE
            val textMessage = toastView.findViewById<TextView>(R.id.toastText)
            textMessage.text = message
            Log.d("toast", "${textMessage.text}")
            with(Toast(applicationContext))
            {
                duration = Toast.LENGTH_SHORT
                view = toastView
                show()
            }
        }
        else{
            Log.d("toast", "$message")
            val toastView = layoutInflater.inflate(
                R.layout.toast_text_information,
                findViewById(R.id.toastLayoutInformation)
            )
            // Link Youtube -> https://www.youtube.com/watch?v=__GRhyvf6oE
            val textMessage = toastView.findViewById<TextView>(R.id.toastText)
            textMessage.text = message
            Log.d("toast", "${textMessage.text}")
            with(Toast(applicationContext))
            {
                duration = Toast.LENGTH_SHORT
                view = toastView
                show()
            }
        }

    }

    private fun onSwipeUp():Boolean {
        //Toast.makeText(this, "Swipe Up", Toast.LENGTH_SHORT).show()

        return false
    }

    private fun onSwipeBottom(): Boolean {
        //Toast.makeText(this, "Swipe Down", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun onSwipeLeft(): Boolean {
        //Toast.makeText(this, "Swipe Left", Toast.LENGTH_SHORT).show()

        return true
    }

    private fun onSwipeRight(username: String): Boolean {
        //Toast.makeText(this, "Swipe Right", Toast.LENGTH_SHORT).show()
        goToProfile(username, 1)
        return true
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
                            showToast("ERROR", 1)
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
                                                        showToast("ERROR", 1)
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
                                                        showToast("ERROR", 1)
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
            goToProfile(userinfo.username, 3)
        }
        else
        {
            showToast("Phone number $phonenumber Invalid!", 1)
            return
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()

        var username = ""
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid.toString())
            user_table.get().addOnSuccessListener { result ->
                if(result != null){
                    username = result.getString("Username").toString()
                    Log.d("profilePage", username.toString())
                    val intent = Intent(this, profilePage::class.java)
                    intent.putExtra("usernameOthers", username)
                    startActivity(intent)
                }
                else{
                    showToast("ERROR", 1)
                    return@addOnSuccessListener
                }
            }
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


        Glide.with(this).load(userinfo.url)
            .circleCrop()
            .into(circularImageViewEdit)

//        Picasso.get().load(userinfo.url).into(object :
//            com.squareup.picasso.Target {
//            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//                // loaded bitmap is here (bitmap)
//                circularImageViewEdit.setImageBitmap(bitmap)
//            }
//
//            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
//
//            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
//        })


        etNameEdit.setText(userinfo.name).toString()

        etPhoneEdit.setText(userinfo.phonenumber).toString()

        if(userinfo.description != ""){
            etDescriptionEdit.setText(userinfo.description).toString()
        }

    }

}

data class userInfo(var username: String, var name: String, var url: String, var phonenumber: String, var description: String, var uri: Uri?){

}