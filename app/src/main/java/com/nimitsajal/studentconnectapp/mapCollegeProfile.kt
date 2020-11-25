package com.nimitsajal.studentconnectapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_college_profile_page.*

class mapCollegeProfile : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    var collegeName = " BMSCE - BMS College of Engineering"
    var username = ""
    var universityName = "BMS University"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_college_profile)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        val adapter = GroupAdapter<GroupieViewHolder>()

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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val db = FirebaseFirestore.getInstance()
        var geoPoint: GeoPoint? = null
        // Add a marker in Sydney and move the camera
        db.collection("University").document("Next").collection(" BMSCE - BMS College of Engineering").document("BMS University")
            .get()
            .addOnSuccessListener {
                if(it != null){
                    geoPoint = it.getGeoPoint("Map Location")
                }
            }

        if(geoPoint != null){
            val lat = geoPoint!!.latitude
            val lng = geoPoint!!.longitude
            Log.d("map", "lat = $lat")
            Log.d("map", "lng = $lng")
        }
        else{
            Log.d("map", "geoPoint = NULL")
        }

        collegeName = " BMSCE - BMS College of Engineering"
        val lat = 12.941294511779427
        val lng = 77.56551506851775

        val location = LatLng(lat, lng)

//        Log.d("map", "location = $location")

        mMap.addMarker(MarkerOptions().position(location).title("Marker in $collegeName"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f))
    }

    private fun loadCollege(
        collegeName: String,
        universityName: String,
        adapter: GroupAdapter<GroupieViewHolder>
    ){
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
        db.collection("University").document("Next").collection(universityName).document("Next").collection(
            collegeName
        )
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
