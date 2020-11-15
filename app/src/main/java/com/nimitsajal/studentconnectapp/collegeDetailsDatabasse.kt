package com.nimitsajal.studentconnectapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import kotlinx.android.synthetic.main.activity_college_details_databasse.*
import kotlinx.android.synthetic.main.activity_sign_up.toLoginPage
import java.util.*
import kotlin.collections.HashMap

class collegeDetailsDatabase : AppCompatActivity() {
    var university_name: String = "University"
    var college_name: String = "College"
    var branch_name: String = "Branch"
    var semester_name: String = "Semester"

    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_college_details_databasse)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        storageReference = FirebaseStorage.getInstance().reference

        toLoginPage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            btnRegister.isEnabled = false
            performRegister(auth)
            displaySavedData()
        }

        val University = setArrayUniversity()

        val College = mutableListOf<String>("College")
//        val College = setArrayCollege(university_name!!, College_temp)

        val Branch = mutableListOf<String>("Branch")

        val Semester = mutableListOf<String>("Year")


        var university_position = 0
        var college_position = 0
        var branch_position = 0
        var semester_position = 0

        val spinnerUniversity = ddUniversity
        if (spinnerUniversity != null) {
            val adapter = ArrayAdapter(this, R.layout.style_spinner, University)
            spinnerUniversity.adapter = adapter

            spinnerUniversity.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    university_name = University[position]
                    if (position != university_position) {
                        Log.d("database", "university_name = $university_name, university_position = $position")
//                        College.clear()
                        university_position = position
                        college_position = 0
                        branch_position = 0
                        semester_position = 0
                        setArrayCollege(university_name, College)
                        Branch.clear()
                        Branch.add("Branch")
                        college_name = College[0]
                        branch_name = Branch[0]
                        Semester.clear()
                        Semester.add("Semester")
                        semester_name = Semester[0]
                    }
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                    university_name = University[0]
                }
            }
        }

        val spinnerCollege = ddCollege
        if (spinnerCollege != null) {
            val adapter = ArrayAdapter(this, R.layout.style_spinner, College)
            spinnerCollege.adapter = adapter

            spinnerCollege.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    college_name = College[position]
                    if (position != college_position) {

//                        Branch.clear()
//                        Branch.add("Branch")

                        setArrayBranch(university_name, college_name, Branch)

                        college_position = position
                        branch_position = 0
                        semester_position = 0
                        branch_name = Branch[0]
                        Semester.clear()
                        Semester.add("Semester")
                        semester_name = Semester[0]
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    college_name = College[0]
                }
            }
        }

        val spinnerBranch = ddBranch
        if (spinnerBranch != null) {
            val adapter = ArrayAdapter(this, R.layout.style_spinner, Branch)
            spinnerBranch.adapter = adapter

            spinnerBranch.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    branch_name = Branch[position]
                    if (position != branch_position) {

//                        Semester.clear()
                        setArraySemester(university_name, college_name, branch_name, Semester)
                        branch_position = position
                        semester_position = 0
                        semester_name = Semester[0]
                    }

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    branch_name = Branch[0]
                }
            }
        }

        val spinnerSemester = ddSemester
        if (spinnerSemester != null) {
            val adapter = ArrayAdapter(this, R.layout.style_spinner, Semester)
            spinnerSemester.adapter = adapter

            spinnerSemester.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    semester_name = Semester[position]

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    semester_name = Semester[0]
                }
            }
        }
    }

    private fun setArrayUniversity(): MutableList<String> {
        val db = FirebaseFirestore.getInstance()

        val University = mutableListOf<String>("University")

        db.collection("University")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("database", "Document id = ${document.id}")
                    if(document.id == "Next"){
                        continue
                    }
                    else{
                        University.add(document.id)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("database", "Error getting documents: ", exception)
            }
        return University
    }

    private fun setArrayCollege(university_name: String, College: MutableList<String>){
        val db = FirebaseFirestore.getInstance()

        College.clear()
        College.add("College")

        db.collection("University").document("Next").collection(university_name)
            .get()
            .addOnSuccessListener {result ->
                for (document in result) {
                    Log.d("database", document.id)
                    if(document.id != "Next"){
                        College.add(document.id)
                    }
//                    College.add(document.id)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("database", "Error getting documents: ", exception)
            }
    }

    private fun setArrayBranch(university_name: String, college_name: String, Branch: MutableList<String>){
        val db = FirebaseFirestore.getInstance()

        Branch.clear()
        Branch.add("Branch")

        db.collection("University").document("Next").collection(university_name).document("Next").collection(college_name)
            .get()
            .addOnSuccessListener {result ->
                for (document in result) {
                    Log.d("database", document.id)
                    if(document.id != "Next"){
                        Branch.add(document.id)
                    }
//                    Branch.add(document.id)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("database", "Error getting documents: ", exception)
            }
    }

    private fun setArraySemester(university_name: String, college_name: String, branch_name: String, Semester: MutableList<String>){
        val db = FirebaseFirestore.getInstance()

        Semester.clear()
        Semester.add("Year")

        db.collection("University").document("Next").collection(university_name).document("Next").collection(college_name).document("Next").collection(branch_name)
            .get()
            .addOnSuccessListener {result ->
                for (document in result) {
                    Log.d("database", document.id)
                    if(document.id != "0"){
                        Semester.add(document.id)
                    }
//                    Branch.add(document.id)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("database", "Error getting documents: ", exception)
            }
    }

    private fun displaySavedData() {
        Log.d("display", university_name.toString())
        Log.d("display", college_name.toString())
        Log.d("display", branch_name.toString())
        Log.d("display", semester_name.toString())

    }

//    private fun uploadImageToFirebaseStorage(selectedPhotoUrl: Uri?, userName: String){
//        if(selectedPhotoUrl == null){
//            Log.d("dp", "nothing")
//            return
////            return "https://firebasestorage.googleapis.com/v0/b/student-connect-b96e6.appspot.com/o/user_dp%2Fuser_default_dp.png?alt=media&token=4a2736ef-c5cb-4845-9d0f-894e7bf3c6a2"
//        }
//
//        val filename = UUID.randomUUID().toString()
//        val ref = FirebaseStorage.getInstance().getReference("/user_dp/$userName")
//        ref.putFile(selectedPhotoUrl)
//            .addOnSuccessListener {
//                Log.d(
//                    "Registration",
//                    "Image successfully uploaded at location: ${it.metadata?.path}"
//                )
//                ref.downloadUrl
//                    .addOnSuccessListener { result ->
//                        Log.d("dp", "image url: ${result.toString()}")
////                        return result.toString()
//                    }
//            }
//            .addOnFailureListener {
//                Log.d("dp", "Image upload failed: ${it.message}")
//            }
//    }


    private fun uploadImageToFirebaseStorage(selectedPhotoUrl: Uri?, userName: String) {
        if(selectedPhotoUrl == null){
            return
        }
        else{
            val photoReference = storageReference.child("https://console.firebase.google.com/project/student-connect-b96e6/storage/student-connect-b96e6.appspot.com/files/user_dp/${userName}-photo.jpg")
            photoReference.putFile(selectedPhotoUrl)
                .addOnSuccessListener {
                    Log.d("dp", it.totalByteCount.toString())
                }
                .addOnFailureListener {
                    Log.d("dp", it.message.toString())
                }
        }
    }


    private fun uploadImageToFirebaseStorage(selectedPhotoUrl: Uri?){
        if(selectedPhotoUrl == null) {
            return
        }

        val filename = UUID.randomUUID().toString()
//        val filename = "morty.jpg"
        val ref = FirebaseStorage.getInstance().getReference("images/dp/$filename")
        ref.putFile(selectedPhotoUrl)
            .addOnSuccessListener {
                Log.d("Registration", "Image successfully uploaded at location: ${it.metadata?.path}")
                ref.downloadUrl
                    .addOnSuccessListener {
                        Log.d("Registration","image url: $it")
//                        saveUserToFirebaseDatabase(it.toString())
                    }
            }
            .addOnFailureListener {
                Log.d("Registration", "Image upload failed: ${it.message}")
            }
    }

    private fun performRegister(auth: FirebaseAuth) {

        Log.d("database", "${university_name.toString()} -> ${college_name.toString()} -> ${branch_name.toString()} -> ${semester_name.toString()}")
        if(university_name == "University")
        {
            btnRegister.isEnabled = true
            Toast.makeText(this, "Enter The University", Toast.LENGTH_SHORT).show()
            return
        }
        if(college_name == "College")
        {
            btnRegister.isEnabled = true
            Toast.makeText(this, "Enter The College", Toast.LENGTH_SHORT).show()
            return
        }
        if(branch_name == "Branch")
        {
            btnRegister.isEnabled = true
            Toast.makeText(this, "Enter The Branch", Toast.LENGTH_SHORT).show()
            return
        }
        if(semester_name == "Semester")
        {
            btnRegister.isEnabled = true
            Toast.makeText(this, "Enter The Semester", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Entered Perform Register", Toast.LENGTH_SHORT).show()

        val userName = intent.getStringExtra("userName_signup")
        val userEmail = intent.getStringExtra("userEmail_signup")
        val userPassword = intent.getStringExtra("userPassword_signup")
        val userUserName = intent.getStringExtra("userUserName_signup")
        val userPhone = intent.getStringExtra("userPhone_signup")
        val selectedPhotoUrl_string: String? = intent.getStringExtra("dpImage_string")

        val selectedPhotoUrl = Uri.parse(selectedPhotoUrl_string)



        var url = ""

        if (userEmail != null) {
            if (userPassword != null && userName != null && userUserName != null && userPhone != null) {
                auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnSuccessListener {
                        btnRegister.isEnabled = true
                        Log.d(
                            "Registration",
                            "Registration successful for uid: ${it.user.toString()}"
                        )
                        var user = auth.currentUser
                        if (user != null) {
                            Toast.makeText(this, "Registration Successful $userUserName", Toast.LENGTH_SHORT).show()
                            if(selectedPhotoUrl == null) {
                                url = "https://firebasestorage.googleapis.com/v0/b/student-connect-b96e6.appspot.com/o/user_dp%2Fuser_default_dp.png?alt=media&token=4a2736ef-c5cb-4845-9d0f-894e7bf3c6a2"
                                savedata(userName, userEmail, userUserName, userPhone, url)
                            }
                            else{
                                val filename = userUserName.toString()
                                val ref = FirebaseStorage.getInstance().getReference("images/dp/$filename")
                                ref.putFile(selectedPhotoUrl)
                                    .addOnSuccessListener { img ->
                                        Log.d("Registration", "Image successfully uploaded at location: ${img.metadata?.path}")
                                        ref.downloadUrl
                                            .addOnSuccessListener {img_link ->
                                                Log.d("Registration","image url: $img_link")
                                                url = img_link.toString()
                                                savedata(userName, userEmail, userUserName, userPhone, url)
                                            }
                                    }
                                    .addOnFailureListener {
                                        Log.d("Registration", "Image upload failed: ${it.message}")
                                    }
                            }
                        }

                        val db = FirebaseFirestore.getInstance()
                        var Emailing: HashMap<String, Any> = hashMapOf<String,Any>()
                        if (user != null) {
                            Emailing.put("Username", userUserName)
                            db.collection("User Table").document(user.uid)
                                .set(Emailing)
                                .addOnCompleteListener{
                                    if(it.isSuccessful)
                                    {
                                        Log.d(
                                            "Registration",
                                            "Added The Link"
                                        )
                                    }
                                    else
                                    {
                                        Log.d(
                                            "Registration",
                                            "Link not added"
                                        )
                                    }
                                }
                        }
                        Toast.makeText(this,"Logging In", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, mainFeed::class.java)
                        intent.putExtra("username", userUserName)
                        startActivity(intent)
                        finish()
                        finish()
                    }
                    .addOnFailureListener {
                        btnRegister.isEnabled = true
                        Log.d("Registration", "Registration failed! : ${it.message}")
                        Toast.makeText(
                            this,
                            "Registration Failed: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }
    }

    private fun savedata(userName: String, userEmail: String, userUserName: String, userPhone: String, selectedPhotoUrl: String)
    {
        var university: HashMap<String, Any> = hashMapOf<String,Any>()
        university.put("University", university_name)
        university.put("College", college_name)
        university.put("Branch", branch_name)
        university.put("Semester", semester_name)

        var user: HashMap<String, Any> = hashMapOf<String,Any>()
        user.put("Name", userName)
        user.put("Email", userEmail)
        user.put("Phone Number", userPhone)
        user.put("Picture", selectedPhotoUrl)
        user.put("Description", "")
        user.put("College", university)

        val db = FirebaseFirestore.getInstance()
        var inner: HashMap<String, Any> = hashMapOf<String,Any>()
        inner.put("Info", "Info")

        db.collection("Users").document(userUserName)
            .set(user)
            .addOnCompleteListener {
                if(it.isSuccessful)
                {
                    db.collection("Users").document(userUserName).collection("Friends").document("Info")
                        .set(inner)
                        .addOnCompleteListener { it2 ->
                            if (it2.isSuccessful) {
                                db.collection("Users").document(userUserName).collection("Medals")
                                    .document("Info")
                                    .set(inner)
                                    .addOnCompleteListener { it3 ->
                                        if (it3.isSuccessful) {
                                            db.collection("Users").document(userUserName)
                                                .collection("Medals").document("Info")
                                                .set(inner)
                                                .addOnCompleteListener { it4 ->
                                                    if (it4.isSuccessful) {
                                                        db.collection("Users").document(userUserName)
                                                            .collection("My Feed").document("Info")
                                                            .set(inner)
                                                            .addOnCompleteListener { it5 ->
                                                                if (it5.isSuccessful) {
                                                                    db.collection("Users").document(userUserName)
                                                                        .collection("My Posts").document("Info")
                                                                        .set(inner)
                                                                        .addOnCompleteListener { it6 ->
                                                                            if (it6.isSuccessful) {
                                                                                db.collection("Users").document(userUserName)
                                                                                    .collection("Tags").document("Info")
                                                                                    .set(inner)
                                                                                    .addOnCompleteListener{it7->
                                                                                        if(it7.isSuccessful)
                                                                                        {
                                                                                            Log.d("database", "User uploaded")
                                                                                            db.collection("University").document(university_name)
                                                                                                .collection("Student").document(userUserName)
                                                                                                .set(inner)
                                                                                                .addOnCompleteListener { it8->
                                                                                                    if (it8.isSuccessful)
                                                                                                    {
                                                                                                        db.collection("University").document("Next")
                                                                                                            .collection(university_name).document(college_name)
                                                                                                            .collection("Student").document(userUserName)
                                                                                                            .set(inner)
                                                                                                            .addOnCompleteListener { it9->
                                                                                                                if (it9.isSuccessful)
                                                                                                                {
                                                                                                                    db.collection("University").document("Next")
                                                                                                                        .collection(university_name).document("Next")
                                                                                                                        .collection(college_name).document(branch_name)
                                                                                                                        .collection("Student").document(userUserName)
                                                                                                                        .set(inner)
                                                                                                                        .addOnCompleteListener { it10->
                                                                                                                            if (it10.isSuccessful)
                                                                                                                            {
                                                                                                                                db.collection("University").document("Next")
                                                                                                                                    .collection(university_name).document("Next")
                                                                                                                                    .collection(college_name).document("Next")
                                                                                                                                    .collection(branch_name).document(semester_name)
                                                                                                                                    .collection("Student").document(userUserName)
                                                                                                                                    .set(inner)
                                                                                                                                    .addOnCompleteListener { it11->
                                                                                                                                        if (it11.isSuccessful)
                                                                                                                                        {
                                                                                                                                            Log.d("database", "User enrolled in College")
                                                                                                                                        }
                                                                                                                                        else
                                                                                                                                        {
                                                                                                                                            Log.d("database", "College records not updated")
                                                                                                                                        }
                                                                                                                                    }
                                                                                                                            }
                                                                                                                        }
                                                                                                                }
                                                                                                            }
                                                                                                    }
                                                                                                }
                                                                                        }
                                                                                        else
                                                                                        {
                                                                                            Log.d("database", "User not uploaded")
                                                                                        }
                                                                                    }
                                                                            }
                                                                        }
                                                                }
                                                            }
                                                    }
                                                }
                                        }
                                    }
                            }
                        }
                }
                else
                {
                    Log.d("database", "User not uploaded")
                }
            }

    }
}