package com.nimitsajal.studentconnectapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
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
import kotlinx.android.synthetic.main.toast_login_adapter.*
import java.lang.System.currentTimeMillis
import java.util.*

class upload_post : AppCompatActivity() {

    var selectedUri: Uri? = null
    var userDpUrl: String = ""
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_post)

        var username = intent.getStringExtra("username")
        functions = Firebase.functions


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
                    showToast("ERROR", 1)
                    return@addOnSuccessListener
                }
            }
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, mainFeed::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
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

    private fun uploadPost(username: String, description: String, imageUri: Uri, userDpUrl: String, db: FirebaseFirestore){
        pbUpload.isVisible = true
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
//                        val calendar = Calendar.getInstance()
//                        val time = calendar.timeInMillis
                        uploadPostCaller(description, userDpUrl, username, img_link.toString())
                    }
            }
            .addOnFailureListener {
                Log.d("Registration", "Image upload failed: ${it.message}")
            }
    }

    private fun uploadPostCloud(description: String, userDp: String, userName: String, picture: String): Task<Any> {
        //val time = FieldValue.serverTimestamp()
        var data: HashMap<String, Any> = hashMapOf<String, Any>()
        data.put("description", description)
        data.put("dp", userDp)
        data.put("userName", userName)
        data.put("picture", picture)

        Log.d("uploadCloud", "${data["description"]}, ${data["dp"]}, ${data["userName"]}, ${data["picture"]}, ${data["timeStamp"]}")

        return functions
            .getHttpsCallable("uploadPost")
            .call(data)
//            .addOnCompleteListener { task ->
//                val result = task.result?.data
//                result
//            }
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data
                result
            }
    }

    private fun uploadPostCaller(description: String, userDp: String, userName: String, picture: String){
        uploadPostCloud(description, userDp, userName, picture)
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    val e = task.exception
                    if (e is FirebaseFunctionsException) {
                        val code = e.code
                        val details = e.details
                        Log.d("cloud", "code = ${e.code}, details = $details")
                    }
                    // [START_EXCLUDE]
                    Log.d("cloud", "addMessage:onFailure", e)
//                    showSnackbar("An error occurred.")
                    showToast("Image Not Uploaded", 1)
                    val intent = Intent(this, mainFeed::class.java)
                    intent.putExtra("username", userName)
                    pbUpload.isVisible = false
                    startActivity(intent)
                    overridePendingTransition(R.anim.zoom_out_upload, R.anim.static_transition)
                    finish()
                    return@OnCompleteListener
                    // [END_EXCLUDE]
                } else {
                    showToast("Image Uploaded", 2)
                    val intent = Intent(this, mainFeed::class.java)
                    intent.putExtra("username", userName)
                    pbUpload.isVisible = false
                    startActivity(intent)
                    overridePendingTransition(R.anim.zoom_out_upload, R.anim.static_transition)
                    finish()
                    return@OnCompleteListener
                }
            }
            )
    }


//    private fun uploadPost(username: String, description: String, imageUri: Uri, userDpUrl: String, db: FirebaseFirestore){
//        pbUpload.isVisible = true
//        val filename = UUID.randomUUID().toString()
//        val ref = FirebaseStorage.getInstance().getReference("images/uploads/$username/$filename")
//        ref.putFile(imageUri)
//            .addOnSuccessListener { img ->
//                Log.d(
//                    "Registration",
//                    "Image successfully uploaded at location: ${img.metadata?.path}"
//                )
//                ref.downloadUrl
//                    .addOnSuccessListener { img_link ->
////                        val calendar = Calendar.getInstance()
////                        val time = calendar.timeInMillis
//                        val time = FieldValue.serverTimestamp()
//                        val post = hashMapOf(
//                            "From" to username,
//                            "Description" to description,
//                            "Dp" to userDpUrl,
//                            "Likes" to 0,
//                            "Picture" to img_link.toString(),
//                            "Time" to time
//                        )
//                        val info = hashMapOf(
//                            "Info" to "Info",
//                        )
//                        val linkPost = hashMapOf(
//                            "Liked" to false,
//                            "Time" to time
//                        )
//                        db.collection("Post").add(post)
//                            .addOnSuccessListener {postObject ->
//                                db.collection("Post").document(postObject.id).collection("Comments").document("Info")
//                                    .set(info)
//                                    .addOnSuccessListener {
//                                        Log.d(
//                                            "Registration",
//                                            "Comments collection created: ${img.metadata?.path}"
//                                        )
//                                    }
//
//                                db.collection("Post").document(postObject.id).collection("Tags").document("Info")
//                                    .set(info)
//                                    .addOnSuccessListener {
//                                        Log.d(
//                                            "Registration",
//                                            "Tags collection created: ${img.metadata?.path}"
//                                        )
//                                    }
//
//                                db.collection("Users").document(username).collection("My Posts").document(postObject.id)
//                                    .set(linkPost)
//                                    .addOnSuccessListener {
//                                        Log.d(
//                                            "Registration",
//                                            "Image added into my post: ${img.metadata?.path}"
//                                        )
//                                    }
//
//                                db.collection("Users").document(username).collection("Friends")
//                                    .get()
//                                    .addOnSuccessListener {friendList ->
//                                        for(document in friendList){
//                                            if(document.id != "Info"){
//                                                db.collection("Users").document(document.id).collection("My Feed").document(postObject.id)
//                                                    .set(linkPost)
//                                                    .addOnSuccessListener {
//                                                        Log.d("post", "Post in the feed of ${document.id}")
//                                                    }
//                                            }
//                                        }
//                                        val intent = Intent(this, mainFeed::class.java)
//                                        intent.putExtra("username", username)
//                                        pbUpload.isVisible = false
//                                        startActivity(intent)
//                                        overridePendingTransition(R.anim.zoom_out_upload, R.anim.static_transition)
//                                        finish()
//                                    }
//                            }
//                    }
//            }
//            .addOnFailureListener {
//                Log.d("Registration", "Image upload failed: ${it.message}")
//            }
//    }

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