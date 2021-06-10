package com.nimitsajal.studentconnectapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_event_page.*
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.activity_main_feed.btnChat
import kotlinx.android.synthetic.main.activity_main_feed.btnFeed
import kotlinx.android.synthetic.main.activity_main_feed.btnLogout
import kotlinx.android.synthetic.main.activity_main_feed.btnProfile
import kotlinx.android.synthetic.main.activity_my_post.*
import kotlinx.android.synthetic.main.activity_profile_page.*
import kotlinx.android.synthetic.main.profile_post_adapter.view.*
import kotlinx.android.synthetic.main.suggestion_adapter.view.*
import java.util.ArrayList
import java.util.HashMap
import kotlin.math.pow

class eventPage : AppCompatActivity() {

    private lateinit var detector: GestureDetectorCompat
    private lateinit var functions: FirebaseFunctions

    var mutualFriends:HashMap<String, Int> = HashMap<String, Int>()
    var mutualTaggedUsers:HashMap<String, Int> = HashMap<String, Int>()
    var friends:HashMap<String, Int> = HashMap<String, Int>()
    var tags = mutableListOf<Tags>()
    var users = mutableListOf<Users>()
    var result: MutableList<Result> = mutableListOf()
    var selectedPosition = -1
    var selected = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_page)

        val adapter = GroupAdapter<GroupieViewHolder>()
        val mLayoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        rvSuggestion.layoutManager = mLayoutManager

        functions = FirebaseFunctions.getInstance("asia-south1")
        var username = intent.getStringExtra("username")
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid.toString())
            user_table.get().addOnSuccessListener { result ->
                if(result != null){
                    username = result.getString("Username").toString()
                    Log.d("profilePage", username.toString())
                    detector = GestureDetectorCompat(this,DiaryGestureListener(username))
//                    tagCaller(username!!, "Hello", false, db)
                    suggestFriend(db, username!!)
                }
                else{
                    showToast("ERROR", 1)
                    return@addOnSuccessListener
                }
            }
        }

        detector = GestureDetectorCompat(this,DiaryGestureListener(username))

        btnAddFriend.setOnClickListener {
            information.visibility = View.GONE
            adapter.removeGroupAtAdapterPosition(selectedPosition)
            selectedPosition = -1
            selected = ""
            rvSuggestion.adapter = adapter
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
            intent.putExtra("username", username)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
            finish()
        }

        btnProfile.setOnClickListener {
            goToProfile(username!!)
        }

        btnFeed.setOnClickListener {
            goToFeed(username!!)
        }

        btnRefresh.setOnClickListener {
            btnRefresh.visibility = View.GONE
            reload(adapter, db)
        }
    }

    private fun tagCloud(username: String, tagName: String, add: Boolean): Task<Any> {
        var data: HashMap<String, Any> = hashMapOf<String, Any>()
        data.put("userName", username)
        data.put("tagName", tagName)
        if(add){
            return functions
                .getHttpsCallable("addTag")
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
        else{
            return functions
                .getHttpsCallable("removeTag")
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
    }

    private fun tagCaller(username: String, tagName: String, add: Boolean, db: FirebaseFirestore){
        tagCloud(username, tagName, add)
            .addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful){
                    if(add){
                        db.collection("Users").document(username)
                            .get()
                            .addOnSuccessListener {
                                db.collection("Users").document(username)
                                    .update("Tags", (it["Tags"].toString().toInt() + 1))
                                    .addOnSuccessListener {
                                        Log.d("tag", "Tag updated - added")
                                    }
                            }
                    }
                    else{
                        db.collection("Users").document(username)
                            .get()
                            .addOnSuccessListener {
                                db.collection("Users").document(username)
                                    .update("Tags", (it["Tags"].toString().toInt() - 1))
                                    .addOnSuccessListener {
                                        Log.d("tag", "Tag updated - subtracted")
                                    }
                            }
                    }
                }
            })
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

    private fun goToFeed(username: String)
    {
        val intent = Intent(this, mainFeed::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        finish()
    }
    private fun goToProfile(username: String)
    {
        val intent = Intent(this, profilePage::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
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
            try{
                var diffX = xAxisEvent?.x?.minus(yAxisEvent!!.x)?:0.0F
                var diffY = yAxisEvent?.y?.minus(xAxisEvent!!.y)?:0.0F
                //Toast.makeText(this@mainFeed, "Swipe Right", Toast.LENGTH_SHORT).show()
                if(Math.abs(diffX) > Math.abs(diffY))
                {
                    //Left or Right Swipe
                    if(Math.abs(diffX) > SWIPE_THREASHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THREASHOLD)
                    {
                        if(diffX>0){
                            //Right Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Right", Toast.LENGTH_SHORT).show()
                            return this@eventPage.onSwipeRight(username!!)
                        }
                        else{
                            //Left Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Left", Toast.LENGTH_SHORT).show()
                            return this@eventPage.onSwipeLeft(username!!)
                        }
                    }
                    else
                    {
                        return false
                    }
                }
                else
                {
                    //Up or down Swipe
                    if(Math.abs(diffY) > SWIPE_THREASHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THREASHOLD)
                    {
                        if(diffY>0)
                        {
                            //Up Swipe
                            return this@eventPage.onSwipeUp()
                        }
                        else
                        {
                            //Bottom Swipe
                            return this@eventPage.onSwipeBottom()

                        }
                    }
                    else
                    {
                        return false
                    }
                }

                return super.onFling(yAxisEvent, xAxisEvent, velocityX, velocityY)
            }
            catch (e: Exception)
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
        goToProfile(username)
        return true
    }

    private fun onSwipeRight(username: String): Boolean {
        //Toast.makeText(this, "Swipe Right", Toast.LENGTH_SHORT).show()

        goToFeed(username)
        return true
    }

    private fun suggestFriend(db: FirebaseFirestore, username: String){

        users.add(Users("Mutual Friends", 2))
        users.add(Users("Tagged Users", 1))

        db.collection("Users").document(username).collection("Tags")
            .get()
            .addOnSuccessListener {
                for(tag in it.documents){
                    if(tag.id != "Info"){
                        tags.add(Tags(tag.id, tag["Value"].toString().toInt(), tag["Inbuilt"].toString().toBoolean()))
                    }
                }
            }

        db.collection("Users").document(username)
            .get()
            .addOnSuccessListener {
                if(it.exists()){
                    users.add(Users(it["Semester"].toString(), 4))
                    users.add(Users(it["Branch"].toString(), 3))
                    users.add(Users(it["College"].toString(), 2))
                    users.add(Users(it["University"].toString(), 1))
                }
                db.collection("Users").document(username).collection("Friends")
                    .get()
                    .addOnSuccessListener {
                        for(friend in it.documents){
                            if(friend.id != "Info"){
                                friends.put(friend.id, 1)
                            }
                        }
                        db.collection("Users").document(username).collection("Tagged Users")
                            .get()
                            .addOnSuccessListener { it3 ->
                                for (taggedUser in it3.documents) {
                                    if (taggedUser.id != "Info" && taggedUser.id != username) {
                                        mutualTaggedUsers.put(
                                            taggedUser.id,
                                            taggedUser["Count"].toString().toInt()
                                        )
                                    }
                                }
                                db.collection("Users").document(username).collection("Chat Users")
                                    .get()
                                    .addOnSuccessListener { it5 ->
                                        for (taggedUser in it5.documents) {
                                            if (taggedUser.id != "Info" && taggedUser.id != username) {
                                                if (taggedUser.id in mutualTaggedUsers.keys) {
                                                    mutualTaggedUsers[taggedUser.id] =
                                                        mutualTaggedUsers[taggedUser.id]!! + taggedUser["Count"].toString()
                                                            .toInt()
                                                } else {
                                                    mutualTaggedUsers.put(
                                                        taggedUser.id,
                                                        taggedUser["Count"].toString().toInt()
                                                    )
                                                }
                                            }
                                        }
                                        for (friend in friends) {
                                            db.collection("Users").document(friend.key).collection("Friends")
                                                .get()
                                                .addOnSuccessListener { it1 ->
                                                    for (mutualfriends in it1.documents) {
                                                        if (mutualfriends.id != "Info" && mutualfriends.id !in friends.keys && mutualfriends.id != username) {
                                                            if (mutualfriends.id in mutualFriends.keys) {
                                                                mutualFriends[mutualfriends.id] =
                                                                    mutualFriends[mutualfriends.id]!! + 1
                                                            } else {
                                                                mutualFriends.put(mutualfriends.id, 1)
                                                            }
                                                        }
                                                    }
                                                }
                                            db.collection("Users").document(friend.key).collection("Tagged Users")
                                                .get()
                                                .addOnSuccessListener { it2 ->
                                                    for (mutualfriends in it2.documents) {
                                                        if (mutualfriends.id != "Info" && mutualfriends.id !in friends.keys && mutualfriends.id != username) {
                                                            if (mutualfriends.id in mutualTaggedUsers.keys) {
                                                                mutualTaggedUsers[mutualfriends.id] =
                                                                    mutualTaggedUsers[mutualfriends.id]!! + mutualfriends["Count"].toString()
                                                                        .toInt()
                                                            } else {
                                                                mutualTaggedUsers.put(
                                                                    mutualfriends.id,
                                                                    mutualfriends["Count"].toString().toInt()
                                                                )
                                                            }
                                                        }
                                                    }
                                                    db.collection("Users").document(friend.key).collection("Chat Users")
                                                        .get()
                                                        .addOnSuccessListener { it2 ->
                                                            for (mutualfriends in it2.documents) {
                                                                if (mutualfriends.id != "Info" && mutualfriends.id !in friends.keys && mutualfriends.id != username) {
                                                                    if (mutualfriends.id in mutualTaggedUsers.keys) {
                                                                        mutualTaggedUsers[mutualfriends.id] =
                                                                            mutualTaggedUsers[mutualfriends.id]!! + mutualfriends["Count"].toString()
                                                                                .toInt()
                                                                    } else {
                                                                        mutualTaggedUsers.put(
                                                                            mutualfriends.id,
                                                                            mutualfriends["Count"].toString().toInt()
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                }
                                        }
                                        loadLog(username!!, db)
                                    }
                            }
                    }
            }
    }

    private fun reload(adapter: GroupAdapter<GroupieViewHolder>, db: FirebaseFirestore){
        information.visibility = View.GONE
//        var resultSort = result.sortByDescending { Result -> Result.aggregate }
//        var multiplier = resultSort
        result.sortByDescending { it.aggregate }
        var multiplier = result[0].aggregate
        multiplier = (92..99).random()/multiplier
        var count = 0
        for(res in result){
            if(count > 6){
                break
            }
            res.percentage = (res.aggregate * multiplier).toInt()
            adapter.add(Suggestion_class(res, friends))
            count += 1
        }
        adapter.setOnItemClickListener { item, view ->
            selectedPosition = item.getPosition(item)
            val suggestion = item as Suggestion_class
            tvMatch1.text = ""
            tvMatch2.text = ""
            selected = suggestion.result.name
                db.collection("Users").document(suggestion.result.name).collection("Friends")
                    .get()
                    .addOnSuccessListener {
                        var n1 = ""
                        var n2 = ""
                        var n = 0
                        var linkPost = ""
                        for(doc in it.documents){
                            if(doc.id != "Info"){
                                if(doc.id in friends.keys){
                                    if(n == 0){
                                        n1 = doc.id
                                    }
                                    else if(n == 1){
                                        n2 = doc.id
                                    }
                                    n += 1
                                }
                            }
                        }
                        Log.d("recentPost", "Searching")
                        db.collection("Users").document(suggestion.result.name).collection("My Posts")
                            .orderBy("Time", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { it2 ->
                                information.visibility = View.VISIBLE
                                if(it2.isEmpty){
                                    Log.d("recentPost", "No Post")
                                    Glide.with(this).load(R.drawable.no_post).into(ivRecentPost)
                                }

                                else{
                                    linkPost = it2.documents[0].id
                                    Log.d("recentPost", linkPost)
                                    db.collection("Post").document(linkPost)
                                        .get()
                                        .addOnSuccessListener { it3 ->
                                            if(it3.exists()){
                                                linkPost = it3["Picture"].toString()
                                                Log.d("recentPost", linkPost)
//                                                Picasso.get().load(linkPost).into(ivRecentPost, object :
//                                                    Callback {
//                                                    override fun onSuccess() {
//
//                                                    }
//                                                    override fun onError(e: java.lang.Exception?) {
//                                                        Log.d("loading", "ERROR - $e")
//                                                    }
//                                                })

                                                Glide.with(this).asBitmap()
                                                    .load(linkPost)
                                                    .into(ivRecentPost)
                                            }
                                        }
                                }
                                var str = ""
                                if(n == 1){
                                    str = "You have " + n1 + " as a Mutual Friend"
                                }
                                else if(n == 2){
                                    str = "You have " + n1 + " and " + n2 + " as Mutual Friends"
                                }
                                else if(n > 2){
                                    str = "You have " + n1 + ", " + n2 + " and " + (n-2) + " other Mutual Friends"
                                }
                                var str1 = ""
                                var str2 = ""
                                if(str != ""){
                                    if(suggestion.result.topTags[0].length > str.length){
                                        str2 = suggestion.result.topTags[0]
                                        str1 = str
                                        tvMatch1.text = str1
                                        tvMatch2.text = str2
                                    }
                                    else{
                                        str2 = str
                                        str1 = suggestion.result.topTags[0]
                                        tvMatch1.text = str1
                                        tvMatch2.text = str2
                                    }
                                }
                                else{
                                    if(suggestion.result.topTags.size == 1){
                                        str1 = suggestion.result.topTags[0]
                                        tvMatch1.text = str1
                                        db.collection("Users").document(suggestion.result.name)
                                            .get()
                                            .addOnSuccessListener { it4 ->
                                                if(it4.exists()){
                                                    val str3 = (it4["Branch"].toString()).split("-")
                                                    val str4 = (it4["College"].toString()).split("-")
                                                    var str11 = str3[0]
                                                    var str22 = str4[0]
                                                    if(str3.size > 1){
                                                        str11 = str3[1]
                                                    }
                                                    if(str4.size > 1){
                                                        str22 = str4[1]
                                                    }
                                                    str2 = it4["Name"].toString() + " is pursuing " + str11 + " from " + str22
                                                    tvMatch2.text = str2
                                                }
                                            }
                                    }
                                    else{
                                        if(suggestion.result.topTags[0].length > suggestion.result.topTags[1].length){
                                            str2 = suggestion.result.topTags[0]
                                            str1 = suggestion.result.topTags[1]
                                            tvMatch1.text = str1
                                            tvMatch2.text = str2
                                        }
                                        else{
                                            str1 = suggestion.result.topTags[0]
                                            str2 = suggestion.result.topTags[1]
                                            tvMatch1.text = str1
                                            tvMatch2.text = str2
                                        }
                                    }
                                }
                            }
                    }
        }
        rvSuggestion.adapter = adapter
    }

    private fun loadUser(newUsername: String, username: String, db: FirebaseFirestore){
        var newTags = mutableListOf<Tags>()
        var newTagsLoc = mutableListOf<String>()
        var newUsers = mutableListOf<Users>()
        var topTags = mutableListOf<String>()

        if(newUsername !in friends.keys && newUsername != username){
//            Log.d("suggestion", "In $newUsername")
            if(newUsername in mutualFriends.keys){
                newUsers.add(Users("Mutual Friends", mutualFriends[newUsername]!!))
            }
            else{
                newUsers.add(Users("Mutual Friends", 0))
            }

            if(newUsername in mutualTaggedUsers.keys){
                newUsers.add(Users("Tagged Users", mutualTaggedUsers[newUsername]!!))
            }
            else{
                newUsers.add(Users("Tagged Users", 0))
            }

            db.collection("Users").document(newUsername)
                .get()
                .addOnSuccessListener {
                    if(it.exists()){
                        newUsers.add(Users(it["Semester"].toString(), 1))
                        newUsers.add(Users(it["Branch"].toString(), 1))
                        newUsers.add(Users(it["College"].toString(), 1))
                        newUsers.add(Users(it["University"].toString(), 1))

                        db.collection("Users").document(newUsername).collection("Tags")
                            .get()
                            .addOnSuccessListener { it1 ->
                                for(tag in it1.documents){
                                    if(tag.id != "Info"){
                                        newTags.add(Tags(tag.id, tag["Value"].toString().toInt(), tag["Inbuilt"].toString().toBoolean()))
                                        newTagsLoc.add(tag.id)
                                    }
                                }
//                                loadUsers(newUsername, newTags, newUsers)
                                Log.d("suggestion", "------------------------------------------------------")
//                                Log.d("suggestion", "Username = $newUsername")
//                                Log.d("suggestion", "Tags")
//                                if(!newTags.isEmpty()){
//                                    for(friend in newTags){
//                                        friend.display()
//                                    }
//                                }
//                                Log.d("suggestion", "Users")
//                                if(!newUsers.isEmpty()){
//                                    for(friend in newUsers){
//                                        friend.display()
//                                    }
//                                }
                                var userSinDistance = 0.0
                                var count = 0
                                var numerator = 0.0
                                var denominator1 = 0.0
                                var denominator2 = 0.0
                                while(count < newUsers.size){
                                    if(users[count].name == newUsers[count].name && count != 2){
                                        var x = 0
                                        var y = 0
                                        var w = 0
                                        if(users[count].value > 0){
                                            x = 1
                                        }
                                        if(newUsers[count].value > 0){
                                            y = 1
                                        }
                                        w = (newUsers[count].value + 1) * (users[count].value + 1)
//                                        if(w == 0) {
//                                            w = users[count].value
//                                        }



                                        if(users[3].name != newUsers[3].name){
                                            if(count == 4){
                                                val str = users[4].name.split("-")
                                                if(str.size > 1){
                                                    topTags.add("You both study in${str[1]}")
                                                }
                                                else{
                                                    topTags.add("You both study in${str[0]}")
                                                }
                                            }
                                        }
                                        else{
                                            if(count == 3){
                                                val str1 = users[4].name.split("-")
                                                val str2 = users[3].name.split("-")
                                                var str11 = str1[0]
                                                var str22 = str2[0]
                                                if(str1.size > 1){
                                                    str11 = str1[1]
                                                }
                                                if(str2.size > 1){
                                                    str22 = str2[1]
                                                }
                                                topTags.add("You both study in${str22} from${str11}")
                                            }
                                        }

                                        numerator += x * y * w
                                        denominator1 += w * x * x
                                        denominator2 += w * y * y
//                                        Log.d("suggestion", "Added Values $numerator / $denominator1 * $denominator2")
                                    }
                                    else if(count == 2 && users[count].name == newUsers[count].name && users[count+1].name == newUsers[count+1].name){
                                        var x = 0
                                        var y = 0
                                        var w = 0
                                        if(users[count].value > 0){
                                            x = 1
                                        }
                                        if(newUsers[count].value > 0){
                                            y = 1
                                        }
                                        w = (newUsers[count].value + 1) * (users[count].value + 1)
//                                        if(w == 0) {
//                                            w = users[count].value
//                                        }
                                        numerator += x * y * w
                                        denominator1 += w * x * x
                                        denominator2 += w * y * y
//                                        Log.d("suggestion", "Added Values $numerator / $denominator1 * $denominator2")
                                    }
                                    else{
                                        var x = 1
                                        var y = 0
                                        var w = users[count].value + 1
                                        numerator += x * y * w
                                        denominator1 += w * x * x
                                        denominator2 += w * y * y
//                                        Log.d("suggestion", "Added Values $numerator / $denominator1 * $denominator2")
                                    }
                                    count += 1
                                }
                                denominator1 *= denominator2
                                denominator1 = denominator1.toDouble().pow(0.5)
                                if(denominator1 == 0.0){
                                    numerator = 0.0
                                }
                                else{
                                    numerator /= denominator1
                                }
                                userSinDistance = numerator
//                                Log.d("suggestion", "The Cosine Distance of User: $userSinDistance")
                                var tagSinDistance = 0.0
                                numerator = 0.0
                                denominator1 = 0.0
                                denominator2 = 0.0
                                count = 0

                                var t1 = ""
                                var t2 = ""
                                var p1 = -1
                                var p2 = -1

                                while(count < tags.size){
                                    if(tags[count].name in newTagsLoc){
                                        var loc = newTagsLoc.indexOf(tags[count].name)
                                        var x = 0
                                        var y = 0
                                        var w = 0
                                        if(tags[count].value > 0){
                                            x = 1
                                        }
                                        if(newTags[loc].value > 0){
                                            y = 1
                                        }

                                        w = (newTags[loc].value + 1) * (tags[count].value + 1)
                                        if(w > p1){
                                            p2 = p1
                                            t2 = t1
                                            p1 = w
                                            t1 = tags[count].name
                                        }
                                        else if(w > p2){
                                            p2 = w
                                            t2 = tags[count].name
                                        }

//                                        if(w == 0){
//                                            w = tags[count].value
//                                        }
                                        numerator += x * y * w
                                        denominator1 += w * x * x
                                        denominator2 += w * y * y
//                                        Log.d("suggestion", "Added Values $numerator / $denominator1 * $denominator2")
                                    }
                                    else{
                                        var x = 1
                                        var y = 0
                                        var w = tags[count].value + 1
                                        numerator += x * y * w
                                        denominator1 += w * x * x
                                        denominator2 += w * y * y
//                                        Log.d("suggestion", "Added Values $numerator / $denominator1 * $denominator2")
                                    }
                                    count += 1
                                }
                                denominator1 *= denominator2
                                denominator1 = denominator1.toDouble().pow(0.5)
                                if(denominator1 == 0.0){
                                    numerator = 0.0
                                }
                                else{
                                    numerator /= denominator1
                                }
                                tagSinDistance = numerator
//                                Log.d("suggestion", "The Cosine Distance of Tag: $tagSinDistance")
                                if(p2 == -1 && p1 != -1){
                                    topTags.add("You have a common interest in ${t1}")
                                }
                                else if(p1 > -1){
                                    topTags.add("You have common interests like $t1 and $t2")
                                }
                                var aggregate = (7*tagSinDistance + 5*userSinDistance)/12
                                result.add(Result(newUsername, userSinDistance, tagSinDistance, aggregate, 0, newUsers[0].value, topTags))
                                result[result.size-1].display()
                            }
                    }
                }
        }
    }

    private fun loadUsers(username:String, newTags: MutableList<Tags>, newUsers:MutableList<Users>){
        Log.d("suggestion", "------------------------------------------------------")
        Log.d("suggestion", "Username = $username")
        Log.d("suggestion", "Tags")
        if(!newTags.isEmpty()){
            for(friend in newTags){
                friend.display()
            }
        }
        Log.d("suggestion", "Users")
        if(!newUsers.isEmpty()){
            for(friend in newUsers){
                friend.display()
            }
        }
        var userSinDistance = 0
        var count = 0
        var numerator = 0.0
        var denominator1 = 0.0
        var denominator2 = 0.0
        while(count < newUsers.size){
            if(users[count].name == newUsers[count].name){
                var x = 0
                var y = 0
                var w = 0
                if(users[count].value > 0){
                    x = 1

                }
                if(newUsers[count].value > 0){
                    y = 1
                }
                w = newUsers[count].value * users[count].value
                numerator += x * y * w
                denominator1 += w * x * x
                denominator2 += w * y * y
            }
        }
        denominator1 *= denominator2
        denominator1 = denominator1.toDouble().pow(0.5)
        if(denominator1 == 0.0){
            numerator = 0.0
        }
        else{
            numerator /= denominator1
        }

        Log.d("suggestion", "The Cosine Distance of User: $numerator")
    }

    private fun loadLog(username: String, db: FirebaseFirestore){
        Log.d("suggestion", "Friends")
        for(friend in friends){
            Log.d("suggestion", "${friend.key} = ${friend.value}")
        }
        Log.d("suggestion", "Mutual Friends")
        for(friend in mutualFriends){
            Log.d("suggestion", "${friend.key} = ${friend.value}")
        }
        Log.d("suggestion", "Mutual Tagged Users")
        for(friend in mutualTaggedUsers){
            Log.d("suggestion", "${friend.key} = ${friend.value}")
        }
        Log.d("suggestion", "Tags")
        for(friend in tags){
            friend.display()
        }
        Log.d("suggestion", "Users")
        for(friend in users){
            friend.display()
        }

        db.collection("Users")
            .get()
            .addOnSuccessListener {
                for(user in it.documents){
//                    Log.d("suggestion", "${it.documents.size}")
                    if(user.id != "Info"){
                        loadUser(user.id, username, db)
                    }
                }
                Handler().postDelayed({
                   btnRefreshBlocked.visibility = View.GONE
                    btnRefresh.visibility = View.VISIBLE
                }, 3000)
            }
    }
}



data class Tags(var name: String, var value: Int, var inbuilt: Boolean){
    public fun display(){
        Log.d("suggestion", "${name} -> ${value} (${inbuilt})")
    }
}

data class Users(var name: String, var value: Int){
    public fun display(){
        Log.d("suggestion", "${name} -> ${value}")
    }
}

data class Result(var name: String, var user: Double, var tag: Double, var aggregate: Double, var percentage: Int, var mutual: Int, var topTags: MutableList<String>){

    public fun display(){
        Log.d("suggestion", "${name} \n user -> ${user} \n tag -> ${tag} \n aggregate -> $aggregate \n percentage -> $percentage \n mutual -> $mutual \n topTags -> ${topTags.toString()}")
    }
}

class Suggestion_class(var result: Result, var friends: HashMap<String, Int>): Item<GroupieViewHolder>(){
    var db = FirebaseFirestore.getInstance()
    var link = ""
        override fun bind(viewHolder: GroupieViewHolder, position: Int){
        viewHolder.itemView.tvUsername.text = result.name
        viewHolder.itemView.tvPercentage.text = "" + result.percentage + "%"
        db.collection("Users").document(result.name)
            .get()
            .addOnSuccessListener {
                if(it.exists()){
                    viewHolder.itemView.tvName.text = it["Name"].toString()
                    link = it["Picture"].toString()
                    Glide.with(viewHolder.itemView.context).load(link)
                        .circleCrop()
                        .into(viewHolder.itemView.circularDp)
                }
            }
            viewHolder.itemView.btnProfile.setOnClickListener {
                val intent = Intent(viewHolder.itemView.context, others_profile_page::class.java)
                intent.putExtra("usernameOthers", result.name)
                viewHolder.itemView.context.startActivity(intent)
            }
    }

    override fun getLayout(): Int {
        return R.layout.suggestion_adapter
    }

}
