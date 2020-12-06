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
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
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

    private lateinit var detector: GestureDetectorCompat

    var clginfo = clgInfo("", "", "")

//    var collegeName = "RVCA - RV College of Architecture"
//    var username = ""
//    var universityName = "RV Educational Institutions\n"

//    var collegeName = intent.getStringExtra("collegeName").toString()
//    var username = intent.getStringExtra("username").toString()
//    var universityName = intent.getStringExtra("universityName").toString()

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
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid.toString())
            user_table.get().addOnSuccessListener { result ->
                if(result != null){
                    clginfo.username = result.getString("Username").toString()
                    Log.d("profilePage", clginfo.username.toString())
                    detector = GestureDetectorCompat(this, DiaryGestureListener(clginfo.username))
                }
                else{
                    showToast("ERROR", 1)
                    return@addOnSuccessListener
                }
            }
        }

        detector = GestureDetectorCompat(this, DiaryGestureListener(clginfo.username))

        val adapter = GroupAdapter<GroupieViewHolder>()

        btnEventMap.setOnClickListener {
            goToEvent(clginfo.username)
        }

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
           goToFeed(clginfo.username)
        }

        tvAddress.setOnLongClickListener {
            val textToCopy = tvAddress.text.toString()
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("address", textToCopy)
            clipboardManager.setPrimaryClip(clipData)

            showToast("Text copied to clipboard", 3)
            return@setOnLongClickListener true
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
    //TODO: GO to feed
    private fun goToFeed(username: String)
    {
        val intent = Intent(this, mainFeed::class.java)
        intent.putExtra("username", username)
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        startActivity(intent)
    }
    //TODO: go to event
    private fun goToEvent(username: String)
    {
        val intent = Intent(this, eventPage::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        finish()
    }
    //TODO: On touch event
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
                            return this@mapCollegeProfile.onSwipeRight(username!!)
                        } else {
                            //Left Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Left", Toast.LENGTH_SHORT).show()
                            return this@mapCollegeProfile.onSwipeLeft(username!!)
                        }
                    } else {
                        return false
                    }
                } else {
                    //Up or down Swipe
                    if (Math.abs(diffY) > SWIPE_THREASHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THREASHOLD) {
                        if (diffY > 0) {
                            //Up Swipe
                            return this@mapCollegeProfile.onSwipeUp()
                        } else {
                            //Bottom Swipe
                            return this@mapCollegeProfile.onSwipeBottom()

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

    private fun onSwipeUp():Boolean {
        //Toast.makeText(this, "Swipe Up", Toast.LENGTH_SHORT).show()

        return false
    }

    private fun onSwipeBottom(): Boolean {
        //Toast.makeText(this, "Swipe Down", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun onSwipeLeft(username: String): Boolean {
        //Toast.makeText(this, "Swipe Left", Toast.LENGTH_SHORT).show()
        goToEvent(username)
        return true
    }

    private fun onSwipeRight(username: String): Boolean {
        //Toast.makeText(this, "Swipe Right", Toast.LENGTH_SHORT).show()
        goToFeed(username)
        return true
    }
}

data class clgInfo(var universityName: String, var collegename: String, var username: String){

}
