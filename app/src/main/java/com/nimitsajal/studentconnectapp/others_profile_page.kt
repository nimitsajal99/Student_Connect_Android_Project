package com.nimitsajal.studentconnectapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.activity_my_post.*
import kotlinx.android.synthetic.main.activity_others_profile_page.*
import kotlinx.android.synthetic.main.activity_profile_page.*
import kotlinx.android.synthetic.main.details_adapter.view.*

class others_profile_page : AppCompatActivity() {

    private lateinit var detector: GestureDetectorCompat
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_others_profile_page)
        functions = FirebaseFunctions.getInstance("asia-south1")

        var username: String = ""
        var isFriend: Boolean = false
        var friend: isFriend = isFriend(false, 0, "", "")

        var usernameOthers = intent.getStringExtra("usernameOthers")
        tvUsernameOthers.setText("").toString()
        //tvUsername.setText(username).toString()

        val mLayoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        rvProfilePageOthers.layoutManager = mLayoutManager

        tvDescriptionOthers.setText("").toString()

        tvNameOthers.setText("").toString()

        btnFriendsOthers.setText("").toString()

        btnMessageOthers.setText("Message").toString()

        val adapterPost = GroupAdapter<GroupieViewHolder>()

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid.toString())
            user_table.get().addOnSuccessListener { result ->
                if(result != null){
                    username = result.getString("Username").toString()
                    Log.d("profilePage", username.toString())
                    if (usernameOthers != null) {
                        db.collection("Users").document(username).collection("Friends")
                            .document(usernameOthers)
                            .get()
                            .addOnCompleteListener {
                                if(it.isSuccessful){
                                    val temp = it.getResult()
                                    if (temp != null) {
                                        if(temp.exists()){
                                            btnFriendsOthers.text = "Unfriend"
                                            friend.isFriend = true
                                            //Toast.makeText(this, "isFriend = true", Toast.LENGTH_SHORT).show()
                                        }
                                        else{
                                            btnFriendsOthers.text = "Add Friend"
                                            friend.isFriend = false
                                            //Toast.makeText(this, "isFriend = false", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                detector = GestureDetectorCompat(this,DiaryGestureListener(username))
                                getUser(auth,usernameOthers, db, friend, adapterPost)
                            }
                    }
                }
                else{
                    showToast("ERROR", 1)
                    return@addOnSuccessListener
                }
            }
        }

        detector = GestureDetectorCompat(this,DiaryGestureListener(username))

        btnChatOthers.setOnClickListener {
            val intent = Intent(this, currentChats::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnLogoutOthers.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        btnProfileOthers.setOnClickListener {
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        btnFeedOthers.setOnClickListener {
            goToFeed(username)
        }

        btnEventOthers.setOnClickListener {
            goToEvent(username)
        }

        btnFriendsOthers.setOnClickListener {
            if (usernameOthers != null && username != null) {
                btnFriendsOthers.isEnabled = false
                friend(username, usernameOthers, db, friend, adapterPost)
            }
        }

        btnMessageOthers.setOnClickListener {
            if (usernameOthers != null && username != null){
                btnMessageOthers.isEnabled = false
                chat(username, db, friend, usernameOthers)
            }
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        moveTaskToBack(false)
    }

    private fun goToEvent(username: String){
        val intent = Intent(this, eventPage::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        finish()
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

    private fun goToFeed(username: String){
        val intent = Intent(this, mainFeed::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        finish()
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
                            return this@others_profile_page.onSwipeRight(username!!)
                        } else {
                            //Left Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Left", Toast.LENGTH_SHORT).show()
                            return this@others_profile_page.onSwipeLeft(username!!)
                        }
                    } else {
                        return false
                    }
                } else {
                    //Up or down Swipe
                    if (Math.abs(diffY) > SWIPE_THREASHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THREASHOLD) {
                        if (diffY > 0) {
                            //Up Swipe
                            return this@others_profile_page.onSwipeUp()
                        } else {
                            //Bottom Swipe
                            return this@others_profile_page.onSwipeBottom()

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

    private fun loadPost(db: FirebaseFirestore, username: String, adapter: GroupAdapter<GroupieViewHolder>, dp: String){
        rvProfilePageOthers.adapter = null
        adapter.clear()
        db.collection("Users").document(username).collection("My Posts")
            .orderBy("Time", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                if(it != null){
                    for(document in it){
                        if(document.id != "Info"){
                            var comCount = 0
                            db.collection("Post").document(document.id)
                                .get()
                                .addOnSuccessListener { it2 ->
                                    if(it2 != null){
                                        db.collection("Post").document(document.id).collection("Comments")
                                            .get()
                                            .addOnSuccessListener {it3 ->
                                                if(it3 != null){
                                                    comCount = it3.size()-1
                                                    adapter.add(profile_post_class(it2["Picture"].toString(), it2["Likes"].toString().toInt(), comCount, it2["Description"].toString(), username, db, document.id, false, ""))
                                                }
                                            }
                                    }
                                }
                        }
                    }
                }
                adapter.setOnItemLongClickListener { item, view ->
//                    Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
                    val post: profile_post_class = item as profile_post_class
                    val intent = Intent(this, myPost::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("picture", post.url)
                    intent.putExtra("uid", post.uid)
                    intent.putExtra("description", post.description)
                    intent.putExtra("dp", dp)
                    intent.putExtra("others", "true")
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top)
                    return@setOnItemLongClickListener true
                }
                rvProfilePageOthers.adapter = adapter
            }
    }

    private fun deletefriend(From: String, db: FirebaseFirestore, Removing: String)
    {
        Log.d("others", "in delete")
        var list = mutableListOf<String>()
        db.collection("Users").document(From).collection("Friends").document(Removing)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful)
                {
                    var i = 0
                    val temp = it.getResult()
                    while(i<temp["HashTags"].toString().toInt())
                    {
                        var string = "Tag" + i
                        list.add(temp[string].toString())
                        Log.d("others", "$string -> ${list[i]}")
//                        tagCaller(From,list[i],false,db)
                        i+=1
                    }
                    db.collection("Users").document(From).collection("Friends").document(Removing)
                        .delete()
                        .addOnCompleteListener {it2->
                            if(it2.isSuccessful)
                            {
                                Log.d("others", "$From is removed")
                            }
                        }
                    db.collection("Users").document(From)
                        .get()
                        .addOnSuccessListener {
                            db.collection("Users").document(From)
                                .update("Tags", (it["Tags"].toString().toInt() - list.size))
                                .addOnSuccessListener {
                                    Log.d("others", "Tag updated - subtracted")
                                }
                        }
                    i = 0
                    for(abc in list){
                        db.collection("Users").document(From).collection("Tags").document(abc)
                            .get()
                            .addOnSuccessListener { it2 ->
                                if(it2.exists()){
                                    if(it2["Value"].toString().toInt() > 1){
                                        db.collection("Users").document(From).collection("Tags").document(abc)
                                            .update("Value", (it2["Value"].toString().toInt() - 1))
                                            .addOnCompleteListener {
                                                Log.d("others", "$From updated - subtracted, ${abc}")
                                            }
                                    }
                                    else{
                                        db.collection("Users").document(From).collection("Tags").document(abc)
                                            .delete()
                                            .addOnCompleteListener {
                                                Log.d("others", "$From updated - subtracted, ${abc}")
                                            }
                                    }
                                }
                            }
                            .addOnFailureListener {
                                db.collection("Users").document(From).collection("Tags").document(abc)
                                    .delete()
                                    .addOnCompleteListener {
                                        Log.d("others", "$From updated - subtracted, ${abc}")
                                    }
                            }
                        i += 1
                    }
                }
            }
    }

    private fun addfriend(From: String, db: FirebaseFirestore, FromHash: HashMap<String, String>, To: String)
    {
        var percent = 0.0F
        var list = mutableListOf<String>()
        db.collection("Users").document(From)
            .get()
            .addOnSuccessListener { count ->
                db.collection("Users").document(From).collection("Tags")
                    .get()
                    .addOnSuccessListener {
                        if (it.size() != 0)
                        {
                            for (tag in it.documents)
                            {

//                                percent = tag["Value"].toString().toFloat()/count["Tags"].toString().toFloat()
                                if (tag["Inbuilt"].toString().toBoolean())
                                {
                                    list.add(tag.id)
                                }
                                else if(count["Tags"].toString().toInt() > 25 && percent >= 0.20F)
                                {
                                    list.add(tag.id)
                                }
                            }
                            FromHash.put("HashTags", list.size.toString())
                            var i = 0
                            while(i<list.size)
                            {
                                var string = "Tag" + i
                                FromHash.put(string, list[i])
                                Log.d("others", "$string -> ${list[i]}")
//                                tagCaller(To,list[i],true,db)
                                i+=1
                            }
                            db.collection("Users").document(To).collection("Friends").document(From)
                                .set(FromHash)
                                .addOnCompleteListener {it3 ->
                                    if(it3.isSuccessful){
                                        Log.d("others", "$From is added")
                                    }
                                    else{
                                        Log.d("others", "inner user not entered = $it3")
                                    }
                                }
                            db.collection("Users").document(To)
                                .get()
                                .addOnSuccessListener {
                                    db.collection("Users").document(To)
                                        .update("Tags", (it["Tags"].toString().toInt() + list.size))
                                        .addOnSuccessListener {
                                            Log.d("others", "Tag updated - added")
                                        }
                                }
                            i = 0
                            for(abc in list){
                                db.collection("Users").document(To).collection("Tags").document(abc)
                                    .get()
                                    .addOnSuccessListener { it2 ->
                                        if(it2.exists()){
                                            db.collection("Users").document(To).collection("Tags").document(abc)
                                                .update("Value", (it2["Value"].toString().toInt() + 1))
                                                .addOnCompleteListener {
                                                    Log.d("others", "$To updated - added, ${abc}")
                                                }
                                        }
                                        else{
                                            var data: HashMap<String, Any> = hashMapOf<String, Any>()
                                            data.put("Inbuilt", false)
                                            data.put("Value", 1)
                                            db.collection("Users").document(To).collection("Tags").document(abc)
                                                .set(data)
                                                .addOnCompleteListener{
                                                    Log.d("others", "$To updated - added, ${abc}")
                                                }
                                        }
                                    }
                                    .addOnFailureListener {
                                        var data: HashMap<String, Any> = hashMapOf<String, Any>()
                                        data.put("Inbuilt", false)
                                        data.put("Value", 1)
                                        db.collection("Users").document(To).collection("Tags").document(abc)
                                            .set(data)
                                            .addOnCompleteListener{
                                                Log.d("others", "$To updated - added, ${abc}")
                                            }
                                    }
                                i += 1
                            }
                        }
                    }
            }
    }

    private fun friend(username: String, usernameOthers: String, db: FirebaseFirestore, friend: isFriend, adapterPost: GroupAdapter<GroupieViewHolder>){
        if(friend.isFriend)
        {
            deletefriend(username,db,usernameOthers)
            deletefriend(usernameOthers,db,username)
            showToast("$usernameOthers is no longer a Friend", 2)
            friend.isFriend = false
            friend.count = friend.count-1

            var double: Double = friend.count.toDouble()
            if(friend.count >= 1000000){
                double = (double - double%100000)/1000000
                val str = "Add Friend (" + double + "M)"
                btnFriendsOthers.text = str
            }
            else if(friend.count >= 1000){
                double = (double - double%100)/1000
                val str = "Add Friend (" + double + "K)"
                btnFriendsOthers.text = str
            }
            else{
                val str = "Add Friend (" + friend.count + ")"
                btnFriendsOthers.text = str
            }

            btnFriendsOthers.isEnabled = true
            rvProfilePageOthers.adapter = null
        }
        else{
            var dpUsername = ""
            var name = ""
            db.collection("Users").document(username)
                .get()
                .addOnSuccessListener {
                    if(it != null){
                        dpUsername = it.getString("Picture").toString()
                        name = it.getString("Name").toString()
                        Log.d("others", "user name = $name, dp = $dpUsername")
                        val user = hashMapOf(
                            "Picture" to dpUsername,
                            "Name" to name
                        )
                        val userOthers = hashMapOf(
                            "Picture" to friend.url,
                            "Name" to friend.name
                        )
                        addfriend(usernameOthers,db,userOthers,username)
                        addfriend(username,db,user,usernameOthers)
                        friend.isFriend = true
                        friend.count = friend.count+1

                        var double: Double = friend.count.toDouble()
                        if(friend.count >= 1000000){
                            double = (double - double%100000)/1000000
                            val str = "Unfriend (" + double + "M)"
                            btnFriendsOthers.text = str
                        }
                        else if(friend.count >= 1000){
                            double = (double - double%100)/1000
                            val str = "Unfriend (" + double + "K)"
                            btnFriendsOthers.text = str
                        }
                        else{
                            val str = "Unfriend (" + friend.count + ")"
                            btnFriendsOthers.text = str
                        }

                        btnFriendsOthers.isEnabled = true
                        loadPost(db, usernameOthers, adapterPost, friend.url)
                    }
                }
        }
    }

//    private fun friend(username: String, usernameOthers: String, db: FirebaseFirestore, friend: isFriend, adapterPost: GroupAdapter<GroupieViewHolder>){
//        if(friend.isFriend){
//            db.collection("Users").document(username).collection("Friends").document(usernameOthers)
//                .delete()
//                .addOnCompleteListener{
//                    if(it.isSuccessful){
//                        Log.d("others", "userOther removed from user")
//                }
//            db.collection("Users").document(usernameOthers).collection("Friends").document(username)
//                .delete()
//                .addOnCompleteListener {it2 ->
//                    if(it2.isSuccessful){
//                        showToast("$usernameOthers is no longer a Friend", 2)
//                        friend.isFriend = false
//                        friend.count = friend.count-1
//
//                        var double: Double = friend.count.toDouble()
//                        if(friend.count >= 1000000){
//                            double = (double - double%100000)/1000000
//                            val str = "Add Friend (" + double + "M)"
//                            btnFriendsOthers.text = str
//                        }
//                        else if(friend.count >= 1000){
//                            double = (double - double%100)/1000
//                            val str = "Add Friend (" + double + "K)"
//                            btnFriendsOthers.text = str
//                        }
//                        else{
//                            val str = "Add Friend (" + friend.count + ")"
//                            btnFriendsOthers.text = str
//                        }
//
//                        btnFriendsOthers.isEnabled = true
//                    }
//                }
//            rvProfilePageOthers.adapter = null
//        }
//        else{
//            var dpUsername = ""
//            var name = ""
//            db.collection("Users").document(username)
//                .get()
//                .addOnSuccessListener {
//                    if(it != null){
//                        dpUsername = it.getString("Picture").toString()
//                        name = it.getString("Name").toString()
//                        Log.d("others", "user name = $name, dp = $dpUsername")
//                                    val user = hashMapOf(
//                                        "Picture" to dpUsername,
//                                        "Name" to name
//                                    )
//                                    val userOthers = hashMapOf(
//                                        "Picture" to friend.url,
//                                        "Name" to friend.name
//                                    )
//                                    db.collection("Users").document(username).collection("Friends").document(usernameOthers)
//                                        .set(userOthers)
//                                        .addOnCompleteListener {it3 ->
//                                            if(it3.isSuccessful){
//                                                Log.d("others", "user other added in user")
//                                            }
//                                            else{
//                                                Log.d("others", "inner user not entered = $it3")
//                                            }
//                                        }
//                                    db.collection("Users").document(usernameOthers).collection("Friends").document(username)
//                                        .set(user)
//                                        .addOnCompleteListener {it4 ->
//                                            if(it4.isSuccessful){
//                                                showToast("${friend.name} added as a Friend", 2)
//                                                friend.isFriend = true
//                                                friend.count = friend.count+1
//
//                                                var double: Double = friend.count.toDouble()
//                                                if(friend.count >= 1000000){
//                                                    double = (double - double%100000)/1000000
//                                                    val str = "Unfriend (" + double + "M)"
//                                                    btnFriendsOthers.text = str
//                                                }
//                                                else if(friend.count >= 1000){
//                                                    double = (double - double%100)/1000
//                                                    val str = "Unfriend (" + double + "K)"
//                                                    btnFriendsOthers.text = str
//                                                }
//                                                else{
//                                                    val str = "Unfriend (" + friend.count + ")"
//                                                    btnFriendsOthers.text = str
//                                                }
//
//                                                btnFriendsOthers.isEnabled = true
//                                            }
//                                        }
//                                        .addOnFailureListener {it5 ->
//                                            Log.d("others", "outer user not entered = ${it5.message}")
//                                        }
//                        loadPost(db, usernameOthers, adapterPost, friend.url)
//                                }
//                }
//        }
//    }

    //TODO: search if document exists optimisation
    private fun chat(username: String, db: FirebaseFirestore, friend: isFriend, usernameOthers: String){
        db.collection("Users").document(username).collection("Chats").document(usernameOthers)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val temp = it.getResult()
                    if (temp != null) {
                        if(temp.exists()){
                            val intent = Intent(this, chat::class.java)
                            intent.putExtra("from", username)
                            intent.putExtra("to", usernameOthers)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top)
                            finish()
                        }
                        else{
                            Log.d("others", "inner")
                            newChat(username, db, friend, usernameOthers)
                        }
                    }
                }
            }
    }

    private fun newChat(username: String, db: FirebaseFirestore, friend: isFriend, usernameOthers: String){

        if(friend.isFriend){
            val message = hashMapOf(
                "From" to "System",
                "To" to usernameOthers,
                "Text" to "Say Hi",
                "Time" to FieldValue.serverTimestamp(),
                "Count" to 0
            )

            val messageFrom = hashMapOf(
                "From" to "System",
                "To" to usernameOthers,
                "Text" to "Say Hi",
                "Name" to usernameOthers,
                "Time" to FieldValue.serverTimestamp(),
                "Count" to 0
            )

            val messageTo = hashMapOf(
                "From" to "System",
                "To" to usernameOthers,
                "Text" to "Say Hi",
                "Name" to username,
                "Time" to FieldValue.serverTimestamp(),
                "Count" to 0
            )

            val info = hashMapOf(
                "Info" to "Info"
            )

            db.collection("Users").document(username).collection("Chats").document(usernameOthers)
                .set(messageFrom)
                .addOnCompleteListener{
                    if(it.isSuccessful)
                    {
                        db.collection("Users").document(username).collection("Chats").document(usernameOthers).collection("Next")
                            .add(message)
                            .addOnCompleteListener{it1->
                                if(it1.isSuccessful)
                                {
                                    Log.d("others", "chat created in user")
                                }

                            }
                    }
                }
            db.collection("Users").document(usernameOthers).collection("Chats").document(username)
                .set(messageTo)
                .addOnCompleteListener{it2->
                    if(it2.isSuccessful)
                    {
                        db.collection("Users").document(usernameOthers).collection("Chats").document(username).collection("Next")
                            .add(message)
                            .addOnCompleteListener{it3->
                                if(it3.isSuccessful)
                                {
                                    val intent = Intent(this, chat::class.java)
                                    intent.putExtra("from", username)
                                    intent.putExtra("to", usernameOthers)
                                    startActivity(intent)
                                    btnMessageOthers.isEnabled = true
                                    finish()
                                }

                            }
                    }

                }
        }
        else{
            val message = hashMapOf(
                "From" to "System",
                "To" to usernameOthers,
                "Text" to "Say Hi",
                "Time" to FieldValue.serverTimestamp(),
                "Count" to 0
            )

            val messageFrom = hashMapOf(
                "From" to "System",
                "To" to usernameOthers,
                "Text" to "Why don't you add $usernameOthers as a Friend",
                "Name" to usernameOthers,
                "Time" to FieldValue.serverTimestamp(),
                "Count" to 0
            )

            val messageTo = hashMapOf(
                "From" to "System",
                "To" to usernameOthers,
                "Text" to "$username is not in your Friend List",
                "Name" to username,
                "Time" to FieldValue.serverTimestamp(),
                "Count" to 0
            )

            db.collection("Users").document(username).collection("Chats").document(usernameOthers)
                .set(messageFrom)
                .addOnCompleteListener{
                    if(it.isSuccessful)
                    {
                        db.collection("Users").document(username).collection("Chats").document(usernameOthers).collection("Next")
                            .add(message)
                            .addOnCompleteListener{it1->
                                if(it1.isSuccessful)
                                {
                                    Log.d("others", "chat created in user")
                                }

                            }
                    }
                }
            db.collection("Users").document(usernameOthers).collection("Chats").document(username)
                .set(messageTo)
                .addOnCompleteListener{it2->
                    if(it2.isSuccessful)
                    {
                        db.collection("Users").document(usernameOthers).collection("Chats").document(username).collection("Next")
                            .add(message)
                            .addOnCompleteListener{it3->
                                if(it3.isSuccessful)
                                {
                                    val intent = Intent(this, chat::class.java)
                                    intent.putExtra("from", username)
                                    intent.putExtra("to", usernameOthers)
                                    startActivity(intent)
                                    btnMessageOthers.isEnabled = true
                                    finish()
                                }

                            }
                    }

                }
        }


    }

    private fun getUser(auth: FirebaseAuth, username: String, db: FirebaseFirestore, friend: isFriend, adapterPost: GroupAdapter<GroupieViewHolder>){


        tvUsernameOthers.setText(username.toString()).toString()

        val user_info = db.collection("Users").document(username)
        user_info.get().addOnSuccessListener {
            if(it != null){

                var selectedPhotoUrl_string = it.getString("Picture").toString()
                val selectedPhotoUrl = Uri.parse(selectedPhotoUrl_string)
//                val selectedPhotoUrl = selectedPhotoUrl_string.toUri()
//                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUrl)
//                circularImageView.setImageBitmap(bitmap)

                friend.url = selectedPhotoUrl_string

                Glide.with(this).load(selectedPhotoUrl_string)
                    .circleCrop()
                    .into(circularImageViewOthers)

//                Picasso.get().load(selectedPhotoUrl_string).into(object :
//                    com.squareup.picasso.Target {
//                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//                        // loaded bitmap is here (bitmap)
//                        circularImageViewOthers.setImageBitmap(bitmap)
//                    }
//
//                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
//
//                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
//                })

                tvNameOthers.setText(it.getString("Name").toString()).toString()
                friend.name = it.getString("Name").toString()
                if(friend.isFriend == true){
                    loadPost(db, username, adapterPost, friend.url)
                }

                if(it.getString("Description").toString() == ""){
                    collapse(tvDescriptionOthers)
//                    tvDescription.setText(it.getString("Description").toString()).toString()
//                    expand(tvName, 999)
                }
                else{
                    expand(tvDescriptionOthers, 999)
                    tvDescriptionOthers.setText(it.getString("Description").toString()).toString()
                }

            }
        }

        val arrayDetails = mutableListOf<String>()
        val adapter = GroupAdapter<GroupieViewHolder>()
        db.collection("Users").document(username).collection("Medals")
            .get()
            .addOnSuccessListener {
                for(document in it){
                    arrayDetails.add(document.id.toString())
                }
                for(i in 0 until arrayDetails.size){
                    var num = (0 until arrayDetails.size).random()
                    adapter.add(details_class(arrayDetails.get(num)))
                    arrayDetails.removeAt(num)
                }
                rvDetailsOthers.adapter = adapter
            }

        var friends = ""
        if(friend.isFriend == true){
            friends = "Unfriend ("
        }
        else{
            friends = "Add Friend ("
        }

        val FriendsCount = user_info.collection("Friends").get()
            .addOnSuccessListener {
                if(it != null){
                    friend.count = it.size() - 1
                    var double: Double = friend.count.toDouble()
                    if(friend.count >= 1000000){
                        double = (double - double%100000)/1000000
                        friends = friends + double + "M)"
                    }
                    else if(friend.count >= 1000){
                        double = (double - double%100)/1000
                        friends = friends + double + "K)"
                    }
                    else{
                        friends = friends + friend.count + ")"
                    }
                    btnFriendsOthers.setText(friends).toString()
                }
            }

    }

    private fun collapse(textView: TextView) {
        textView.height = 0
    }
    private fun expand(textView: TextView, height: Int) {
        if(height == 999){
            textView.layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
        }
    }
}

data class isFriend(var isFriend: Boolean, var count: Int, var name: String, var url: String){

}
