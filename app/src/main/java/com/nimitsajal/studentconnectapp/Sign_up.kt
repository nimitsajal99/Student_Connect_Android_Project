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
            Toast.makeText(this, "Don't leave any fields blank!", Toast.LENGTH_SHORT).show()
            btnContinueToClgDetails.isEnabled = true
            return
        }

        if (!isEmailValid(userEmail)) {
            Toast.makeText(this, "Email is Invalid!", Toast.LENGTH_SHORT).show()
            //TODO: Switching The button Back On
            btnContinueToClgDetails.isEnabled = true
            return
        }

        if (!isValidMobile(userPhone)) {
            Toast.makeText(this, "Phone number is Invalid!", Toast.LENGTH_SHORT).show()
            //TODO: Switching The button Back On
            btnContinueToClgDetails.isEnabled = true
            return
        }

        if (!isPasswordValid(userPassword)) {
            Toast.makeText(this, "Weak Password!", Toast.LENGTH_SHORT).show()
            //TODO: Switching The button Back On
            btnContinueToClgDetails.isEnabled = true
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("Users")
            .get()
            .addOnSuccessListener { result ->
                var truth = false
                for (document in result) {
                    Log.d("database", "-${document.id}-")
                    if (document.id == userUserName) {
                        truth = true
                        break
                    }
                }
                Log.d("database", "Username present -$truth-")
                //TODO: Switching The button Back On
                btnContinueToClgDetails.isEnabled = true
                if (result.contains(userUserName))
                    Log.d("database", "Username present -$userUserName-")

                if (!truth) {
                    val intent = Intent(this, collegeDetailsDatabase::class.java)
                    intent.putExtra("userName_signup", userName)
                    intent.putExtra("userEmail_signup", userEmail)
                    intent.putExtra("userPassword_signup", userPassword)
                    intent.putExtra("userUserName_signup", userUserName)
                    intent.putExtra("userPhone_signup", userPhone)
                    intent.putExtra("dpImage_string", selectedPhotoUrl.toString())
                    startActivity(intent)
                    //finish()
                } else {
                    Toast.makeText(this, "Username already exists!", Toast.LENGTH_SHORT).show()
                }
            }


            .addOnFailureListener { exception ->
                Log.d("ret", " in failure, isUserPresent = $exception")
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