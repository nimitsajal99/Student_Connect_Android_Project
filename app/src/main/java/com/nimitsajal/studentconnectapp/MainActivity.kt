package com.nimitsajal.studentconnectapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toast_login_adapter.*
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = FirebaseFirestore.getInstance()

        val auth = FirebaseAuth.getInstance()
        if(auth.currentUser != null){
            val layout = layoutInflater.inflate(R.layout.toast_login_adapter, toast_constraint_layout)
            Toast(this).apply {
                duration = Toast.LENGTH_SHORT
                setGravity(Gravity.CENTER, 0, 0)
                view = layout
            }.show()
//            Toast.makeText(this,"Welcome Student", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, mainFeed::class.java)
            startActivity(intent)
        }

        toSignup_login.setOnClickListener {
            val intent = Intent(this, Sign_up::class.java)
            startActivity(intent)
        }

        btnLogin_login.setOnClickListener{
            btnLogin_login.isEnabled = false
            val userEmail = etEmail_login.text.toString()
            val userPassword = etPassword_login.text.toString()
            if(userEmail.isBlank())
            {
                showToast("Enter Email", 3)
                btnLogin_login.isEnabled = true
                return@setOnClickListener
            }
            if(userPassword.isBlank()) {
                showToast("Enter Password", 3)
                btnLogin_login.isEnabled = true
                return@setOnClickListener
            }
            if(!isEmailValid(userEmail)){
                showToast("Enter a Valid Email", 1)
                btnLogin_login.isEnabled = true
                return@setOnClickListener
            }
            if(!isPasswordValid(userPassword)){
                showToast("Enter a Valid Password", 1)
                btnLogin_login.isEnabled = true
                return@setOnClickListener
            }
            Log.d("login", "Going to database")
            auth.signInWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener {
                btnLogin_login.isEnabled = true
                Log.d("login", "Went to database")
                if(it.isSuccessful) {
//                    Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
                    val layout = layoutInflater.inflate(R.layout.toast_login_adapter, toast_constraint_layout)
                    Toast(this).apply {
                        duration = Toast.LENGTH_SHORT
                        setGravity(Gravity.CENTER, 0, 0)
                        view = layout
                    }.show()
                    val intent = Intent(this, mainFeed::class.java)
                    val user = auth.currentUser
                    var username = ""
                    val user_table = db.collection("User Table").document(user!!.uid.toString())
                    user_table.get().addOnSuccessListener {result ->
                        if(result != null){
                            username = result.getString("Username").toString()
                        }
                        else{
                            showToast("ERROR", 1)
                            btnLogin_login.isEnabled = true
                            return@addOnSuccessListener
                        }
                    }
                    intent.putExtra("username", username)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    showToast("Email or Password Invalid", 1)
                    btnLogin_login.isEnabled = true
                }
            }
        }
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

    private fun isEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
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
}
