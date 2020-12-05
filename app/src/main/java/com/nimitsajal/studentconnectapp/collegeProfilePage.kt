package com.nimitsajal.studentconnectapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.LatLng
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_college_profile_page.*
import kotlinx.android.synthetic.main.activity_college_profile_page.btnChat
import kotlinx.android.synthetic.main.activity_college_profile_page.btnEvent
import kotlinx.android.synthetic.main.activity_college_profile_page.btnLogout
import kotlinx.android.synthetic.main.activity_college_profile_page.btnProfile
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.branchnames_adapter.view.*
import kotlinx.android.synthetic.main.current_chat_adapter.view.*

class collegeProfilePage : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_college_profile_page)

        val adapter = GroupAdapter<GroupieViewHolder>()

        var collegeName = ""
        var username = ""
        var universityName = ""

        collegeName = intent.getStringExtra("collegeName").toString()
        username = intent.getStringExtra("username").toString()
        universityName = intent.getStringExtra("universityName").toString()

//        btnEvent.setOnClickListener {
//            val intent = Intent(this, collegeProfilePage::class.java)
//            intent.putExtra("username", username)
//            startActivity(intent)
//        }


        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        btnChat.setOnClickListener {
            val intent = Intent(this, currentChats::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnFeedCollege.setOnClickListener {
            val intent = Intent(this, mainFeed::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        if(collegeName != "" || universityName != ""){
            if (collegeName != null) {
                if (universityName != null) {
                    loadCollege(collegeName, universityName, adapter)
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

    private fun loadCollege(collegeName: String, universityName: String, adapter: GroupAdapter<GroupieViewHolder>){
        val db = FirebaseFirestore.getInstance()
        db.collection("University").document("Next").collection(universityName).document(collegeName)
            .get()
            .addOnSuccessListener {
                if(it != null){
                    Picasso.get().load(it.getString("Picture").toString()).into(ivCollege)
                    tvCollegeName.text = collegeName
                    tvCollegeAbout.text = it.getString("About").toString()
                    tvAddress.text = it.getString("Address").toString()

                }
            }
        db.collection("University").document("Next").collection(universityName).document("Next").collection(collegeName)
            .get()
            .addOnSuccessListener {
                if(it != null){
                    for(document in it){
                        if(document.id != "Next"){
                            adapter.add(College_class(document.id))
                        }
                    }
                }
                rvBranchNames.adapter = adapter
            }
    }
}

class College_class(val collegeName: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvBranchNames.text = collegeName
    }
    override fun getLayout(): Int {
        return R.layout.branchnames_adapter
    }
}
//useless text