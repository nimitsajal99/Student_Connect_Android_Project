package com.nimitsajal.studentconnectapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.io.*
import java.util.regex.Pattern

class Sign_up : AppCompatActivity() {

    private var selectedPhotoUrl: Uri? = null
    private lateinit var auth: FirebaseAuth

    private var isUserPresent = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        auth = FirebaseAuth.getInstance()


        toLoginPage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnDP.setOnClickListener {
            photoPicker()
//            putDpInCircularView()
        }

        btnContinueToClgDetails.setOnClickListener {
            btnContinueToClgDetails.isEnabled = false
            goToCollegeDetails()
        }

//        btnContinueToClgDetails.setOnClickListener {
//            performRegister()
//        }
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

    private fun isUserNameValid(userName: String): Boolean {
        return true     //BACKEND INTEGRATION PENDING
    }

    private fun isEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidMobile(phone: String): Boolean {
//        if(Pattern.matches("(6-9) + [0-9]+", phone) && phone.length == 10 ) {
//            return true
//        }
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

    private fun isPasswordValid(password: String): Boolean {
        if (Pattern.matches(
                "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$",
                password
            ) && password.length >= 6
        ) {
            return true
        }
        return false
    }


    @Synchronized
    fun goToCollegeDetails() {

        val userName = etName_signup.text.toString()
        val userEmail = etEmail_signup.text.toString()
        val userPassword = etPassword_signup.text.toString()
        val userUserName = etUserName_signup.text.toString()
        val userPhone = etPhone_signup.text.toString()

        if (userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty() || userUserName.isEmpty() || userPhone.isEmpty()) {
            showToast("Don't leave any fields blank!", 3)
            btnContinueToClgDetails.isEnabled = true
            return
        }

        if (!isEmailValid(userEmail)) {
            showToast("Email is Invalid!", 1)
            btnContinueToClgDetails.isEnabled = true
            return
        }

        if (!isValidMobile(userPhone)) {
            showToast("Phone number is Invalid!", 1)
            btnContinueToClgDetails.isEnabled = true
            return
        }

        if (!isPasswordValid(userPassword)) {
            showToast("Weak Password!", 1)
            btnContinueToClgDetails.isEnabled = true
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("Users").document(userUserName)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful)
                {
                    val temp = it.getResult()
                    if (temp != null) {
                        if(temp.exists()) {
                            Log.d("database", "Username present -$userUserName-")
                            showToast("Username already exists!", 1)
                            btnContinueToClgDetails.isEnabled = true
                        }
                        else
                        {
                            btnContinueToClgDetails.isEnabled = true
                            val intent = Intent(this, collegeDetailsDatabase::class.java)
                            intent.putExtra("userName_signup", userName)
                            intent.putExtra("userEmail_signup", userEmail)
                            intent.putExtra("userPassword_signup", userPassword)
                            intent.putExtra("userUserName_signup", userUserName)
                            intent.putExtra("userPhone_signup", userPhone)
                            intent.putExtra("dpImage_string", selectedPhotoUrl.toString())
                            startActivity(intent)
                        }
                    }
                }
            }
    }

//    private fun putDpInCircularView(){
//        val file = File(selectedPhotoUrl!!.path.toString())
////        val compressedImageFile = Compressor.compress(this, file)
//        val bitmap_original = BitmapFactory.decodeFile(file.path)
//        val bitmap_compressed = Bitmap.createScaledBitmap(bitmap_original, 110, 110, true)
//        Toast.makeText(this, "in putDpCircularView function", Toast.LENGTH_SHORT).show()
//        circularImageView.setImageBitmap(bitmap_compressed)
//        btnDP.alpha = 0f
//    }


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

    private fun photoPicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }
}