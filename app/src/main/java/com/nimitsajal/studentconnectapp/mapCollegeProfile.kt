package com.nimitsajal.studentconnectapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_college_profile_page.*
import kotlinx.android.synthetic.main.activity_college_profile_page.btnChat
import kotlinx.android.synthetic.main.activity_college_profile_page.btnFeedCollege
import kotlinx.android.synthetic.main.activity_college_profile_page.btnLogout
import kotlinx.android.synthetic.main.activity_college_profile_page.btnProfile
import kotlinx.android.synthetic.main.activity_college_profile_page.ivCollege
import kotlinx.android.synthetic.main.activity_college_profile_page.rvBranchNames
import kotlinx.android.synthetic.main.activity_college_profile_page.tvAddress
import kotlinx.android.synthetic.main.activity_college_profile_page.tvCollegeAbout
import kotlinx.android.synthetic.main.activity_college_profile_page.tvCollegeName
import kotlinx.android.synthetic.main.activity_map_college_profile.*

class mapCollegeProfile : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

//    var collegeName = "RVCA - RV College of Architecture"
//    var username = ""
//    var universityName = "RV Educational Institutions\n"

//    var collegeName = intent.getStringExtra("collegeName").toString()
//    var username = intent.getStringExtra("username").toString()
//    var universityName = intent.getStringExtra("universityName").toString()


    var clginfo = clgInfo("", "", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_college_profile)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)





        clginfo.collegename = intent.getStringExtra("collegeName").toString()
        clginfo.username = intent.getStringExtra("username").toString()
        clginfo.universityName= intent.getStringExtra("universityName").toString()

        val adapter = GroupAdapter<GroupieViewHolder>()

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
            intent.putExtra("username", clginfo.username)
            startActivity(intent)
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", clginfo.username)
            startActivity(intent)
        }

        btnFeedCollege.setOnClickListener {
            val intent = Intent(this, mainFeed::class.java)
            intent.putExtra("username", clginfo.username)
            startActivity(intent)
        }

        tvAddress.setOnClickListener {
            val textToCopy = tvAddress.text.toString()
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("address", textToCopy)
            clipboardManager.setPrimaryClip(clipData)

            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
        }

        if(clginfo.collegename != "" || clginfo.universityName != ""){
            loadCollege(clginfo.collegename, clginfo.universityName, adapter)
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
        mMap.uiSettings.isZoomControlsEnabled = true

        val geoCoder = Geocoder(this)
        var locationName: String = clginfo.collegename
        var addressList: List<Address>? = null
        addressList = geoCoder.getFromLocationName(locationName, 1)
        val address = addressList!![0]
        val latLng = LatLng(address.latitude, address.longitude)

        mMap.isBuildingsEnabled = true
        mMap.isTrafficEnabled = true
        mMap.isIndoorEnabled = true
        mMap.mapType = (GoogleMap.MAP_TYPE_NORMAL)

        mMap.addMarker(MarkerOptions().position(latLng).title(clginfo.collegename))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
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

data class clgInfo(var universityName: String, var collegename: String, var username: String){

}
