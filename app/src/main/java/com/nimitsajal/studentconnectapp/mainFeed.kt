package com.nimitsajal.studentconnectapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.branchnames_adapter.view.*
import kotlinx.android.synthetic.main.comment_adapter.view.*
import kotlinx.android.synthetic.main.new_chat_adapter.view.*
import kotlinx.android.synthetic.main.post_adapter_cardiew.view.*
import java.util.HashMap


class mainFeed : AppCompatActivity() {

    private lateinit var detector: GestureDetectorCompat
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_feed)

        functions = Firebase.functions
        val adapter = GroupAdapter<GroupieViewHolder>()

        var arrayPost = mutableListOf<postList>()
        var arraySearch: MutableList<usersList> = mutableListOf<usersList>()

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
                    detector = GestureDetectorCompat(
                        this, DiaryGestureListener(
                            username,
                            adapter,
                            db,
                            arraySearch
                        )
                    )
                    db.collection("Users").document(username!!)
                        .update("Logged", true)
                        .addOnSuccessListener {
                            Log.d("profilePage", "User Logged as TRUE")
                        }
                    loadFeed(arrayPost, adapter, username!!, db)
                    //TODO: friendsuggestion called
//                    friendSuggestion(db,username!!,adapter)
                }
                else{
                    showToast("ERROR", 1)
                    return@addOnSuccessListener
                }
            }
        }

        detector = GestureDetectorCompat(
            this, DiaryGestureListener(
                username,
                adapter,
                db,
                arraySearch
            )
        )

        btnEvent.setOnClickListener {
            goToEvent(username!!)
        }

        btnUpload.setOnClickListener{
            val intent = Intent(this, upload_post::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            overridePendingTransition(R.anim.zoom_in_upload, R.anim.static_transition)
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
//            intent.putExtra("type", "2")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
//            intent.putExtra("type", "1")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }

//        btnFeed.setOnClickListener {
//            val intent = Intent(this, mainFeed::class.java)
//            intent.putExtra("username", username)
//            startActivity(intent)
//        }

        btnSearch.setOnClickListener {
            if(etSearchMainFeed.isVisible==true){
                closeSearchBar()
                if(adapter.itemCount==0)
                {
                    tvNoNewPost.visibility = View.VISIBLE
                }
            }
            else
            {
                tvNoNewPost.isVisible = false
                if(adapter.itemCount==0)
                {
                    tvNoNewPost.visibility = View.GONE
                }
               openSearchBar(username!!, adapter, db, arraySearch)
            }
        }

        etSearchMainFeed.addTextChangedListener(){
            adapter.clear()
            if(etSearchMainFeed.text.toString() == "" || etSearchMainFeed==null)
            {
                etSearchMainFeed.isEnabled = false
                etSearchMainFeed.isVisible=false
                logoMainFeed.isVisible = true
                adapter.clear()
                adapter.clear()
                loadFeed(arrayPost, adapter, username!!, db)
            }
            else{
                etSearchMainFeed.isVisible = true
                logoMainFeed.isVisible = false
                adapter.clear()
                if(username != null){
                    adapter.clear()
                    loadSearch(username!!, db, adapter, arraySearch)
                }
            }
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        moveTaskToBack(true)
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

    inner class DiaryGestureListener(
        username: String?,
        adapter: GroupAdapter<GroupieViewHolder>,
        db: FirebaseFirestore,
        arraySearch: MutableList<usersList>
    ) : GestureDetector.SimpleOnGestureListener()
    {

        //TODO: Swipe Link - > https://www.youtube.com/watch?v=j1aydFEOEA0
        private val db = db
        private val arraySearch = arraySearch
        private val adapter = adapter
        private val username = username
        private val SWIPE_THREASHOLD = 100
        private val SWIPE_VELOCITY_THREASHOLD = 100

        override fun onFling(yAxisEvent: MotionEvent?, xAxisEvent: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
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
                            return this@mainFeed.onSwipeRight()
                        }
                        else{
                            //Left Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Left", Toast.LENGTH_SHORT).show()
                            return this@mainFeed.onSwipeLeft(username!!)
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
                            return this@mainFeed.onSwipeUp(adapter)
                        }
                        else
                        {
                            //Bottom Swipe
                            return this@mainFeed.onSwipeBottom(username!!, adapter, db, arraySearch)

                        }
                    }
                    else
                    {
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

    private fun onSwipeUp(adapter: GroupAdapter<GroupieViewHolder>):Boolean {
        //Toast.makeText(this, "Swipe Up", Toast.LENGTH_SHORT).show()
        if(adapter.itemCount==0)
        {
            tvNoNewPost.visibility = View.VISIBLE
        }
        closeSearchBar()
        return true
    }

    private fun onSwipeBottom(
        username: String,
        adapter: GroupAdapter<GroupieViewHolder>,
        db: FirebaseFirestore,
        arraySearch: MutableList<usersList>
    ): Boolean {
        openSearchBar(username, adapter, db, arraySearch)
        if(adapter.itemCount==0)
        {
            tvNoNewPost.visibility = View.GONE
        }
        //Toast.makeText(this, "Swipe Down", Toast.LENGTH_SHORT).show()
        return true
    }

    private fun onSwipeLeft(username: String): Boolean {
        //Toast.makeText(this, "Swipe Left", Toast.LENGTH_SHORT).show()
        goToEvent(username)
        return true
    }

    private fun onSwipeRight(): Boolean {
        //Toast.makeText(this, "Swipe Right", Toast.LENGTH_SHORT).show()
        return false
    }

    fun goToEvent(username: String)
    {
        val intent = Intent(this, eventPage::class.java)
        intent.putExtra("username", username)
//        intent.flags
        startActivity(intent)

        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)

//            Log.d("profilePage", "in also")
//
////            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left)
//            CustomIntent.customType(this@mainFeed, "right-to-left")
//            Log.d("profilePage", "out of also")
//        showToast("NEW toast")

    }

    fun openSearchBar(
        username: String,
        adapter: GroupAdapter<GroupieViewHolder>,
        db: FirebaseFirestore,
        arraySearch: MutableList<usersList>
    )
    {

        etSearchMainFeed.isEnabled = true
        etSearchMainFeed.isVisible=true
        adapter.clear()
        if(etSearchMainFeed.text.toString() != "")
        {
            adapter.clear()
            loadSearch(username!!, db, adapter, arraySearch)
        }
        logoMainFeed.isVisible = false

    }

    fun closeSearchBar()
    {
        etSearchMainFeed.isVisible=false
        etSearchMainFeed.setText("").toString()
        logoMainFeed.isVisible = true
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

    private fun loadSearch(
        username: String,
        db: FirebaseFirestore,
        adapter: GroupAdapter<GroupieViewHolder>,
        arraySearch: MutableList<usersList>
    ){
        val search = etSearchMainFeed.text.toString()
        val str = search[0]
        val remString = search.drop(1)
        if(search != "" || search != null)
        {
            if(str != '@')
            {
                val words = search.split("\\s+".toRegex()).map { word ->
                    word.replace("""^[,\.]|[,\.]$""".toRegex(), "")
                }
//                for(word in words)
//                {
//                    Log.d("mainfeed", word)
//                }
                db.collection("Users")
                    .addSnapshotListener { value, error ->
                        if(value == null || error != null){
                            showToast("ERROR", 1)
                            return@addSnapshotListener
                        }
                        for(document in value.documents){
                            if(document.id != "Info" && document.id != username){
                                var contains = true
                                for(word in words){
                                    val pattern = word.toRegex(RegexOption.IGNORE_CASE)
                                    if(pattern.containsMatchIn(document["Name"].toString()) || pattern.containsMatchIn(
                                            document.id
                                        ) || pattern.containsMatchIn(document["College"].toString())|| pattern.containsMatchIn(
                                            document["Branch"].toString()
                                        ) || pattern.containsMatchIn(document["Semester"].toString()))
                                    {

                                    }
                                    else
                                    {
                                        contains = false
                                        break
                                    }
                                }
                                if(contains)
                                {
                                    val temp = usersList(document.id, "", document["Name"].toString(), document["Picture"].toString(), "")
                                    arraySearch.add(temp)
                                    adapter.add(
                                        UserSearch(
                                            document.id,
                                            document["Picture"].toString(),
                                            document["Name"].toString(),
                                            true
                                        )
                                    )
                                }

                            }
                        }

                        adapter.setOnItemClickListener { item, view ->
                            val searchItem: UserSearch = item as UserSearch
                            val to = searchItem.username
                            val intent = Intent(this, others_profile_page::class.java)
                            intent.putExtra("usernameOthers", to)
                            startActivity(intent)
                            adapter.clear()

                        }
                        rvFeed.adapter = adapter
                    }
            }
            else
            {
                val words = remString.split("\\s+".toRegex()).map { word ->
                    word.replace("""^[,\.]|[,\.]$""".toRegex(), "")
                }
//                for(word in words)
//                {
//                    Log.d("mainfeed", word)
//                }
//                val patternRem = remString.toRegex(RegexOption.IGNORE_CASE)
                db.collection("University")
                    .addSnapshotListener { value, error ->
                        if(value == null || error != null){
                            showToast("ERROR", 1)
                            return@addSnapshotListener
                        }
                        for(document in value.documents)
                        {
                            if(document.id != "Next")
                            {
                                db.collection("University").document("Next").collection(document.id)
                                    .addSnapshotListener { value2, error2 ->
                                        if(value2 == null || error2 != null){
                                            showToast("ERROR", 1)
                                            return@addSnapshotListener
                                        }
                                        for(doc in value2.documents)
                                        {
                                            if(doc.id!="Next")
                                            {
                                                var contains = true
                                                for(word in words){
                                                    val pattern = word.toRegex(RegexOption.IGNORE_CASE)
                                                    if(pattern.containsMatchIn(doc.id.toString()))
                                                    {

                                                    }
                                                    else
                                                    {
                                                        contains = false
                                                        break
                                                    }
                                                }
                                                if(contains)
                                                {
                                                    adapter.add(
                                                        UserSearch(
                                                            doc.id,
                                                            "",
                                                            document.id,
                                                            false
                                                        )
                                                    )
                                                    rvFeed.adapter = adapter
                                                }
                                            }
                                        }
                                    }
                            }
                        }
                        adapter.setOnItemClickListener { item, view ->
                            val searchItem: UserSearch = item as UserSearch

                            val intent = Intent(this, mapCollegeProfile::class.java)
                            intent.putExtra("username", username)
                            intent.putExtra("collegeName", searchItem.username)
                            intent.putExtra("universityName", searchItem.Name)
                            startActivity(intent)
                            adapter.clear()

                        }
                    }
            }

        }

    }

    private fun loadFeed(
        arrayPost: MutableList<postList>,
        adapter: GroupAdapter<GroupieViewHolder>,
        username: String,
        db: FirebaseFirestore
    ) {
        adapter.clear()
        var flag = 0
        pbFeed.isVisible = true
        val user = db.collection("Users").document(username).collection("My Feed")
        user.orderBy("Time", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener {
                if (it != null) {
                    adapter.clear()
                    for (document in it) {
                        if (document.id != "Info") {
                            flag += 1
                            db.collection("Post").document(document.id)
                                .get()
                                .addOnSuccessListener { it2 ->
                                    if(it2 != null){
                                        try {
                                            val temp = postList(
                                                it2["From"].toString(),
                                                it2["Picture"].toString(),
                                                it2["Dp"].toString(),
                                                it2["Description"].toString(),
                                                it2["Likes"].toString().toInt(),
                                                document.id.toString()
                                            )

                                            //TODO: temp added
                                            arrayPost.add(temp)
                                            val adapterComments = GroupAdapter<GroupieViewHolder>()
                                            db.collection("Post").document(document.id).collection("Comments")
                                                .orderBy("Timestamp", Query.Direction.ASCENDING)
                                                .get()
                                                .addOnSuccessListener { it3 ->
                                                    if (it3 != null) {
                                                        adapterComments.clear()
                                                        for (doc in it3.documents) {
                                                            if (doc.id != "Info") {
                                                                adapterComments.add(
                                                                    Comment_class(
                                                                        doc["From"].toString(),
                                                                        doc["Text"].toString()
                                                                    )
                                                                )
                                                            }
                                                        }
                                                        adapter.add(
                                                            post_class(
                                                                it2["From"].toString(),
                                                                it2["Picture"].toString(),
                                                                it2["Dp"].toString(),
                                                                it2["Description"].toString(),
                                                                it2["Likes"].toString().toInt(),
                                                                document.id.toString(),
                                                                username,
                                                                adapter,
                                                                adapterComments,
                                                                false,
                                                                it2["Faces"].toString().toInt(),
                                                                0
                                                            )
                                                        )
                                                    }
                                                }
                                        }
                                        catch (e: Exception){
                                            Log.d("mainfeed", "$e")
                                            db.collection("Users").document(username).collection("My Feed").document(
                                                document.id
                                            )
                                                .delete()
                                                .addOnSuccessListener {
                                                    Log.d(
                                                        "mainfeed",
                                                        "TRY document ${document.id} deleted from $username "
                                                    )
                                                }
                                                .addOnFailureListener {
                                                    Log.d(
                                                        "mainfeed",
                                                        "TRY document ${document.id} not deleted from $username "
                                                    )
                                                }
//                                            db.collection("Post").document(document.id)
//                                                .delete()
//                                                .addOnSuccessListener {
//                                                    Log.d("mainfeed", "TRY document ${document.id} deleted AGAIN")
//                                                }
                                        }

                                    }
                                }

                        }
                    }
                    if(flag == 0){
                        tvNoNewPost.isVisible = true
                        pbFeed.isVisible = false
                    }
                    else{
                        rvFeed.adapter = adapter
                        pbFeed.isVisible = false
                    }
                }
            }
    }

    //TODO: Friend reccomendation fuction
    /*
    private fun friendSuggestion(db: FirebaseFirestore, username: String, adapter: GroupAdapter<GroupieViewHolder>)
    {
        //Log.d("friendsuggestion", "in")
        var arr = ArrayList<recommendation>()
        var alreadyAFriend = hashSetOf<String>()
        var alreadyInArr = hashSetOf<String>()
        db.collection("Users").document(username).collection("Friends")
            .get()
            .addOnSuccessListener {
                if(it!=null)
                {
                    for(document in it) {
                        if (document.id != "Info") {

                            alreadyAFriend.add(document.id)

                        }
                    }
                    db.collection("Users").document(username)
                        .get()
                        .addOnSuccessListener { it2->
                            if(it2!=null)
                            {
                                db.collection("University").document("Next").collection(it2["University"].toString())
                                    .document("Next").collection(it2["College"].toString()).document("Next")
                                    .collection(it2["Branch"].toString()).document(it2["Semester"].toString()).collection("Student")
                                    .get()
                                    .addOnSuccessListener { it3->
                                        if(it3!=null)
                                        {
                                            var length = it3.documents.size
                                            if (length > 2)
                                            {
                                                var times = 0
                                                var added = 0
                                                var num = 0
                                                while (true)
                                                {
                                                    //Log.d("friendsuggestion", "in while 1")
                                                    num = (0 until length).random()
//                                                    if(num>=length)
//                                                    {
//                                                        num = num % length
//                                                    }
                                                    Log.d("friendsuggestion", "$num")

                                                    if (it3.documents[num].id != "Info" && !alreadyInArr.contains(it3.documents[num].id) && !alreadyAFriend.contains(it3.documents[num].id) && it3.documents[num].id != username)
                                                    {
                                                        //.d("friendsuggestion", "True")
                                                        arr.add(recommendation(it3.documents[num].id, it2["Semester"].toString()))
                                                        alreadyInArr.add(it3.documents[num].id)
                                                        added += 1
                                                        if (added > 8)
                                                            break
                                                    }
                                                    if (it3.documents[num].id != "Info")
                                                    {
                                                        times += 1
                                                    }
                                                    if (times >= length || times >= 30)
                                                    {
                                                        break
                                                    }
                                                }
                                                //Log.d("friendsuggestion", "out while 1")
                                            }
//                                            for(doccument in it3.documents)
//                                            {
//                                                if(doccument.id!="Info" && !alreadyAFriend.contains(doccument.id) && doccument.id != username)
//                                                {
//                                                    //Log.d("friendsuggestion", "adding")
//                                                    arr.add(recommendation(doccument.id, it2["University"].toString()))
//                                                }
//                                            }
                                            db.collection("University").document("Next").collection(it2["University"].toString()).document("Next").collection(it2["College"].toString()).document(it2["Branch"].toString()).collection("Student")
                                                .get()
                                                .addOnSuccessListener { it3 ->
                                                    if (it3 != null) {
                                                        var length = it3.documents.size
                                                        //Log.d("friendsuggestion", "$length")
                                                        if (length > 2)
                                                        {
                                                            var times = 0
                                                            var added = 0
                                                            while (true)
                                                            {
                                                                //Log.d("friendsuggestion", "in while 2")
                                                                var num = (0 until length).random()
                                                                times += 1
                                                                if (it3.documents[num].id != "Info" && !alreadyInArr.contains(it3.documents[num].id) && !alreadyAFriend.contains(it3.documents[num].id) && it3.documents[num].id != username)
                                                                {
                                                                    // Log.d("friendsuggestion", "True")
                                                                    arr.add(recommendation(it3.documents[num].id, it2["Branch"].toString()))
                                                                    alreadyInArr.add(it3.documents[num].id)
                                                                    added += 1
                                                                    if (added > 4)
                                                                        break
                                                                }
                                                                if (times >= length || times >= 30)
                                                                {
                                                                    break
                                                                }
                                                            }
                                                            //Log.d("friendsuggestion", "out while 2")
                                                        }
//                                                        for(doccument in it3)
//                                                        {
//                                                            if(doccument.id!="Info" && !alreadyAFriend.contains(doccument.id) && doccument.id != username)
//                                                            {
//                                                                arr.add(recommendation(doccument.id, it2["College"].toString()))
//                                                            }
//                                                        }

                                                        db.collection("University").document("Next").collection(it2["University"].toString()).document(it2["College"].toString()).collection("Student")
                                                            .get()
                                                            .addOnSuccessListener { it3 ->
                                                                if (it3 != null)
                                                                {
                                                                    var length = it3.documents.size
                                                                    //Log.d("friendsuggestion", "$length")
                                                                    if (length > 2)
                                                                    {
                                                                        var times = 0
                                                                        var added = 0
                                                                        while (true)
                                                                        {
                                                                            //Log.d("friendsuggestion", "in while 3")
                                                                            var num = (0 until length).random()
                                                                            times += 1
                                                                            if (it3.documents[num].id != "Info" && !alreadyInArr.contains(it3.documents[num].id) && !alreadyAFriend.contains(it3.documents[num].id) && it3.documents[num].id != username)
                                                                            {
                                                                                //Log.d("friendsuggestion", "True")
                                                                                arr.add(recommendation(it3.documents[num].id, it2["College"].toString()))
                                                                                alreadyInArr.add(it3.documents[num].id)
                                                                                added += 1
                                                                                if (added > 2)
                                                                                    break
                                                                            }
                                                                            if (times >= length || times >= 20)
                                                                            {
                                                                                //Log.d("friendsuggestion", "break out -> times -> $times")
                                                                                break

                                                                            }
                                                                        }
                                                                        //Log.d("friendsuggestion", "out while 3")
                                                                    }
//                                                                    for(doccument in it3)
//                                                                    {
//                                                                        if(doccument.id!="Info" && !alreadyAFriend.contains(doccument.id)&&doccument.id != username)
//                                                                        {
//                                                                            arr.add(recommendation(doccument.id, it2["Branch"].toString()))
//                                                                        }
//                                                                    }

                                                                    db.collection("Users").document(username).collection("Friends")
                                                                        .get()
                                                                        .addOnSuccessListener { it3 ->
                                                                            if (it3 != null)
                                                                            {
                                                                                var length = it3.size()
                                                                                var temp = 4
                                                                                if (it3.size() < 4)
                                                                                {
                                                                                    temp = it3.size()-1
                                                                                }
                                                                                while(temp>0)
                                                                                {
                                                                                    var num = (0 until length).random()
                                                                                    var namefriend = it3.documents[num].id
                                                                                    if (namefriend != "Info")
                                                                                    {
                                                                                        db.collection("Users").document(namefriend).collection("Friends")
                                                                                            .get()
                                                                                            .addOnSuccessListener { it5 ->
                                                                                                var length = it5.documents.size
                                                                                                if (length > 2)
                                                                                                {
                                                                                                    var i = 0
                                                                                                    var smaller = true
                                                                                                    while (i < 4)
                                                                                                    {
                                                                                                        i += 1
                                                                                                        var num = (0 until length).random()
                                                                                                        if (it5.documents[num].id != "Info" && !alreadyInArr.contains(it5.documents[num].id) && !alreadyAFriend.contains(it5.documents[num].id) && it5.documents[num].id != username)
                                                                                                        {
                                                                                                            arr.add(recommendation(it5.documents[num].id, "You have $namefriend as a mutual friend"))
                                                                                                            alreadyInArr.add(it5.documents[num].id)
                                                                                                            if (smaller) {
                                                                                                                smaller = false
                                                                                                            }
                                                                                                            else
                                                                                                            {
                                                                                                                break
                                                                                                            }

                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                    }
                                                                                    temp-=1
                                                                                }

                                                                    db.collection("Users").document(username).collection("Tags")
                                                                        .get()
                                                                        .addOnSuccessListener { it3 ->
                                                                            if (it3 != null)
                                                                            {
                                                                                Log.d("friendsuggestion", "for tags")
                                                                                for (doc in it3.documents)
                                                                                {
                                                                                    if (doc.id != "Info")
                                                                                    {
                                                                                        //Log.d("friendsuggestion", "for tag -> ${doc.id}")
                                                                                        var small = false
                                                                                        if(arr.size<6)
                                                                                        {
                                                                                            small = true
                                                                                        }
                                                                                        db.collection("Tags").document(doc.id).collection("Student")
                                                                                            .get()
                                                                                            .addOnSuccessListener { it5 ->
                                                                                                var length = it5.documents.size
                                                                                                Log.d("friendsuggestion", " tags length -> $length")
                                                                                                if (length > 2)
                                                                                                {
                                                                                                    var i = 0
                                                                                                    while (i < 6)
                                                                                                    {
                                                                                                        i += 1
                                                                                                        var num = (0 until length).random()
                                                                                                        if (it5.documents[num].id != "Info" && !alreadyInArr.contains(it5.documents[num].id) && !alreadyAFriend.contains(it5.documents[num].id) && it5.documents[num].id != username) {
                                                                                                            Log.d("friendsuggestion", "adding tag -> ${it5.documents[num].id}")
                                                                                                            arr.add(recommendation(it5.documents[num].id, doc.id))
                                                                                                            alreadyInArr.add(it5.documents[num].id)
                                                                                                            if (small)
                                                                                                            {
                                                                                                                //Log.d("friendsuggestion", "will add more")
                                                                                                                small = false
                                                                                                            }
                                                                                                            else
                                                                                                            {
                                                                                                                break
                                                                                                            }

                                                                                                        }
                                                                                                    }


                                                                                                }
                                                                                            }
                                                                                    }
                                                                                }



                                                                                db.collection("University").document(it2["University"].toString()).collection("Student")
                                                                                    .get()
                                                                                    .addOnSuccessListener { it3 ->
                                                                                        if (it3 != null) {
                                                                                            var length =
                                                                                                it3.documents.size
                                                                                            //Log.d("friendsuggestion", "$length")
                                                                                            if (length > 2) {
                                                                                                var times4 =
                                                                                                    0
                                                                                                var added =
                                                                                                    0
                                                                                                while (true) {
                                                                                                    // Log.d("friendsuggestion", "in while 4")
                                                                                                    var num =
                                                                                                        (0 until length).random()
                                                                                                    times4 += 1
                                                                                                    if (it3.documents[num].id != "Info" && !alreadyInArr.contains(
                                                                                                            it3.documents[num].id
                                                                                                        ) && !alreadyAFriend.contains(
                                                                                                            it3.documents[num].id
                                                                                                        ) && it3.documents[num].id != username
                                                                                                    ) {
                                                                                                        // Log.d("friendsuggestion", "True")
                                                                                                        arr.add(
                                                                                                            recommendation(
                                                                                                                it3.documents[num].id,
                                                                                                                it2["University"].toString()
                                                                                                            )
                                                                                                        )
                                                                                                        alreadyInArr.add(
                                                                                                            it3.documents[num].id
                                                                                                        )
                                                                                                        added += 1
                                                                                                        if (arr.size < 10) {
                                                                                                            if (added > 3) {
                                                                                                                break
                                                                                                            }
                                                                                                        } else {
                                                                                                            if (added > 1)
                                                                                                                break
                                                                                                        }

                                                                                                    }
                                                                                                    if (times4 >= length || times4 >= 40) {
                                                                                                        break
                                                                                                    }
                                                                                                }
                                                                                            }
//                                                                                for(doccument in it3)
//                                                                                {
//                                                                                    if(doccument.id!="Info" && !alreadyAFriend.contains(doccument.id) && doccument.id != username)
//                                                                                    {
//                                                                                        arr.add(recommendation(doccument.id, it2["Semester"].toString()))
//                                                                                    }
//                                                                                }
                                                                                            if (arr.size < 5) {
                                                                                                Log.d(
                                                                                                    "friendsuggestion",
                                                                                                    "size less than 10"
                                                                                                )
                                                                                                db.collection(
                                                                                                    "Users"
                                                                                                )
                                                                                                    .get()
                                                                                                    .addOnSuccessListener { it3 ->
                                                                                                        if (it3 != null) {
                                                                                                            var length =
                                                                                                                it3.documents.size
                                                                                                            //Log.d("friendsuggestion", "length -> $length")
                                                                                                            var i =
                                                                                                                0
                                                                                                            while (arr.size <= 5 && i < 50) {
                                                                                                                // Log.d("friendsuggestion", "in while")
                                                                                                                var num =
                                                                                                                    (0 until length).random()
                                                                                                                if (it3.documents[num].id != "Info" && !alreadyInArr.contains(
                                                                                                                        it3.documents[num].id
                                                                                                                    ) && !alreadyAFriend.contains(
                                                                                                                        it3.documents[num].id
                                                                                                                    ) && it3.documents[num].id != username
                                                                                                                ) {
                                                                                                                    // Log.d("friendsuggestion", "True")
                                                                                                                    arr.add(
                                                                                                                        recommendation(
                                                                                                                            it3.documents[num].id,
                                                                                                                            ""
                                                                                                                        )
                                                                                                                    )
                                                                                                                    alreadyInArr.add(
                                                                                                                        it3.documents[num].id
                                                                                                                    )
                                                                                                                }
                                                                                                                i += 1
                                                                                                            }
                                                                                                            // Log.d("friendsuggestion", "out while 5 -> $i")
                                                                                                            loadFriendSuggestion(
                                                                                                                arr,
                                                                                                                db,
                                                                                                                adapter
                                                                                                            )
                                                                                                        }
                                                                                                    }
                                                                                            } else {
                                                                                                loadFriendSuggestion(
                                                                                                    arr,
                                                                                                    db,
                                                                                                    adapter
                                                                                                )
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

                            }
                        }

                }
            }
    }

     */

    //TODO: Friend recommendaton engine

    /*
    private fun loadFriendSuggestion(arr: ArrayList<recommendation>,db: FirebaseFirestore,adapter: GroupAdapter<GroupieViewHolder>)
    {

       // val addapter = GroupAdapter<GroupieViewHolder>()
        Log.d("friendsuggestion", "going in for ${arr.size}")
        var i = 0
        while(i<arr.size)
        {
            Log.d("friendsuggestion", "${arr[i].name} -> ${arr[i].association}")
            i+=1
        }
        if(arr.size==3)
        {
            db.collection("Users").document(arr[0].name)
                .get()
                .addOnSuccessListener {
                    if(it!=null)
                    {
                        Log.d("friendsuggestion", "adding ${arr[0].name}")
                        adapter.add(recommendation_Adapter_class(arr[0].name, arr[0].association,it["Picture"].toString(),it["Name"].toString()))
                    }
                }


            db.collection("Users").document(arr[1].name)
                .get()
                .addOnSuccessListener {
                    if(it!=null)
                    {
                        Log.d("friendsuggestion", "adding ${arr[1].name}")
                        adapter.add(recommendation_Adapter_class(arr[1].name, arr[1].association,it["Picture"].toString(),it["Name"].toString()))
                    }
                }


            db.collection("Users").document(arr[1].name)
                .get()
                .addOnSuccessListener {
                    if(it!=null)
                    {
                        Log.d("friendsuggestion", "adding ${arr[1].name}")
                        adapter.add(recommendation_Adapter_class(arr[1].name, arr[1].association,it["Picture"].toString(),it["Name"].toString()))
                    }
                }

            rvFeed.adapter = adapter
        }
        else if(arr.size>3)
        {
            var num = (0 until arr.size).random()
            if(num > arr.size/2)
            {
                var again = (0 until 100).random()
                if(again < 35)
                {
                    num = (0 until arr.size/2).random()
                }
            }
            var name1 = arr[num].name
            var association1 = arr[num].association
            arr.removeAt(num)

            db.collection("Users").document(name1)
                .get()
                .addOnSuccessListener {
                   if(it!=null)
                   {
                       Log.d("friendsuggestion", "adding $name1")
                       adapter.add(recommendation_Adapter_class(name1, association1,it["Picture"].toString(),it["Name"].toString()))
                   }
                }

            num = (0 until arr.size).random()
            if(num > arr.size/2)
            {
                var again = (0 until 100).random()
                if(again < 35)
                {
                    num = (0 until arr.size/2).random()
                }
            }
            var name2 = arr[num].name
            var association2 = arr[num].association
            arr.removeAt(num)
            db.collection("Users").document(name2)
                .get()
                .addOnSuccessListener {
                    if(it!=null)
                    {
                        Log.d("friendsuggestion", "adding $name2")
                        adapter.add(recommendation_Adapter_class(name2, association2,it["Picture"].toString(),it["Name"].toString()))

                    }
                }

            num = (0 until arr.size).random()
            if(num > arr.size/2)
            {
                var again = (0 until 100).random()
                if(again < 35)
                {
                    num = (0 until arr.size/2).random()
                }
            }
            var name3 = arr[num].name
            var association3= arr[num].association
            arr.removeAt(num)
            db.collection("Users").document(name3)
                .get()
                .addOnSuccessListener {
                    if(it!=null)
                    {
                        Log.d("friendsuggestion", "adding $name3")
                        adapter.add(recommendation_Adapter_class(name3, association3,it["Picture"].toString(),it["Name"].toString()))

                    }
                }

            num = (0 until arr.size).random()
            if(num > arr.size/2)
            {
                var again = (0 until 100).random()
                if(again < 35)
                {
                    num = (0 until arr.size/2).random()
                }
            }
            var name4 = arr[num].name
            var association4= arr[num].association
            arr.removeAt(num)
            db.collection("Users").document(name4)
                .get()
                .addOnSuccessListener {
                    if(it!=null)
                    {
                        Log.d("friendsuggestion", "adding $name4")
                        adapter.add(recommendation_Adapter_class(name4, association4,it["Picture"].toString(),it["Name"].toString()))

                    }
                }
            rvFeed.adapter = adapter
        }
    }----------
     */


//    private fun loadFeed(arrayPost: MutableList<postList>, adapter: GroupAdapter<GroupieViewHolder>, username: String, db: FirebaseFirestore)
//    {
//
//        adapter.clear()
//        val user = db.collection("Users").document(username).collection("My Feed")
//        user
//            .orderBy("Time", Query.Direction.DESCENDING)
//            .addSnapshotListener { value, error ->
//                if(value == null || error != null){
//                    Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
//                    return@addSnapshotListener
//                }
//                adapter.clear()
//                for(document in value.documents){
//                    if(document.id != "Info"){
//                        db.collection("Post").document(document.id)
//                            .get()
//                            .addOnSuccessListener {
//                                val temp = postList(it["From"].toString(), it["Picture"].toString(), it["Dp"].toString(), it["Description"].toString(), it["Likes"].toString().toInt(), document.id.toString())
//                                arrayPost.add(temp)
//                                val adapterComments = GroupAdapter<GroupieViewHolder>()
//                                db.collection("Post").document(document.id).collection("Comments")
//                                    .orderBy("Timestamp", Query.Direction.ASCENDING)
//                                    .addSnapshotListener { value, error ->
//                                        if(value == null || error != null){
//                                            Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
//                                            return@addSnapshotListener
//                                        }
//                                        adapterComments.clear()
//                                        for(doc in value.documents){
//                                            if(doc.id != "Info"){
//                                                adapterComments.add(Comment_class(doc["From"].toString(), doc["Text"].toString()))
//                                            }
//                                        }
//                                        adapter.add(post_class(it["From"].toString(), it["Picture"].toString(), it["Dp"].toString(), it["Description"].toString(), it["Likes"].toString().toInt(), document.id.toString(), username, adapter, adapterComments, false))
//                                    }
//                            }
//                    }
//                }
//                rvFeed.adapter = adapter
//            }
//    }
}


//TODO: Recommendation data class created
data class recommendation(var name: String, var association: String)
{

}

data class postList(
    var username: String,
    var imageUrl: String,
    var dpUrl: String,
    var description: String,
    var likeCount: Int,
    var uid: String
)
{

}

//TODO: new recommendation adapter class
//class recommendation_class(var adapter: GroupAdapter<GroupieViewHolder>): Item<GroupieViewHolder>()
//{
//    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
////        val mLayoutManager = GridLayoutManager(viewHolder.itemView.context, 2, GridLayoutManager.HORIZONTAL, false)
////        viewHolder.itemView.rvRecommend.layoutManager = mLayoutManager
//        Log.d("friendsuggestion", "adapter added")
//        viewHolder.itemView.rvRecommend.adapter = adapter
//    }
//    override fun getLayout(): Int {
//        return R.layout.friend_recommendation
//    }
//}

//TODO: new recommendation class
//class recommendation_Adapter_class(var username: String, var common: String, var url: String, var name: String): Item<GroupieViewHolder>()
//{
//    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
//        Log.d("friendsuggestion", "$username added")
//        viewHolder.itemView.tvSimilarity.text = common
//        viewHolder.itemView.tvName.text = username
//        Picasso.get().load(url)
//            .into(viewHolder.itemView.postImageProfile, object : Callback {
//                override fun onSuccess() {
//
//                }
//
//                override fun onError(e: java.lang.Exception?) {
//                    Log.d("loading", "ERROR - $e")
//                }
//            })
//
//    }
//
//    override fun getLayout(): Int {
//        return R.layout.friend_recommendation_addapter
//    }
//}

class post_class(
    var username: String,
    var imageUrl: String,
    var dpUrl: String,
    var description: String,
    var likeCount: Int,
    var uid: String,
    var myusername: String,
    var adapter: GroupAdapter<GroupieViewHolder>,
    var adapterComment: GroupAdapter<GroupieViewHolder>,
    var isComBox: Boolean,
    var faces: Int,
    var tagged: Int
): Item<GroupieViewHolder>()
{
    var taggedBitmap: Bitmap? = null
    var untaggedBitmap: Bitmap? = null
    var showTagged = false
    private var functions: FirebaseFunctions = Firebase.functions

    @SuppressLint("RestrictedApi", "ResourceType")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.pbFeed.isVisible = true
        if(likeCount == 0){
            val likes = "No Likes Yet"
            viewHolder.itemView.tvLikeCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,15f)
            viewHolder.itemView.tvLikeCount.setTypeface(null, Typeface.ITALIC)
            viewHolder.itemView.tvLikeCount.text = likes
        }
        else if(likeCount == 1){
            val likes = "1 Like"
            viewHolder.itemView.tvLikeCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,17f)
            viewHolder.itemView.tvLikeCount.setTypeface(null, Typeface.NORMAL)
            viewHolder.itemView.tvLikeCount.text = likes
        }
        else{
            viewHolder.itemView.tvLikeCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,17f)
            viewHolder.itemView.tvLikeCount.setTypeface(null, Typeface.NORMAL)
            var count: Double = likeCount.toDouble()
            if(likeCount >= 1000000){
                var ans: Double = ((count - count%10000)/1000000).toDouble()
                val likes = "$ans" + "M Likes"
                viewHolder.itemView.tvLikeCount.text = likes
            }
            else if(likeCount >= 1000){
                var ans: Double = ((count - count%10)/1000).toDouble()
                val likes = "$ans" + "K Likes"
                viewHolder.itemView.tvLikeCount.text = likes
            }
            else{
                val likes = "$likeCount Likes"
                viewHolder.itemView.tvLikeCount.text = likes
            }
        }
        if(faces > 0){
            Glide.with(viewHolder.itemView.context)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        untaggedBitmap = resource
                        getTagged(viewHolder)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        // this is called when imageView is cleared on lifecycle call or for
                        // some other reason.
                        // if you are referencing the bitmap somewhere else too other than this imageView
                        // clear it here as you can no longer have the bitmap
                    }
                })
        }
        viewHolder.itemView.rvComments.adapter = adapterComment
        viewHolder.itemView.tvTagCount.setOnClickListener {
            if(tagged > 0){
                if(showTagged){
                    showTagged = false
                    viewHolder.itemView.postImageCard.setImageBitmap(untaggedBitmap)
                    var string = viewHolder.itemView.tvTagCount.text
                    val htmlString = "$string"
                    viewHolder.itemView.tvTagCount.text = HtmlCompat.fromHtml(
                        htmlString,
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                }
                else{
                    showTagged = true
                    viewHolder.itemView.postImageCard.setImageBitmap(taggedBitmap)
                    var string = viewHolder.itemView.tvTagCount.text
                    val htmlString = "<u>$string</>"
                    viewHolder.itemView.tvTagCount.text = HtmlCompat.fromHtml(
                        htmlString,
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                }
            }
        }
        viewHolder.itemView.tvUsernameCard.text = username
        viewHolder.itemView.tvDescriptionCard.text = description
        Picasso.get().load(dpUrl).into(viewHolder.itemView.circularImageViewCard, object : Callback {
            override fun onSuccess() {
                viewHolder.itemView.pbDpFeed.isVisible = false
            }
            override fun onError(e: java.lang.Exception?) {
                Log.d("loading", "ERROR - $e")
            }
        })
        //Picasso.get().load(imageUrl).into(viewHolder.itemView.postImageCard)


        Picasso.get().load(imageUrl)
            .into(viewHolder.itemView.postImageCard, object : Callback {
                override fun onSuccess() {
                    viewHolder.itemView.pbFeed.isVisible = false
                }

                override fun onError(e: java.lang.Exception?) {
                    Log.d("loading", "ERROR - $e")
                }
            })


        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(myusername).collection("My Feed").document(uid)
            .get()
            .addOnSuccessListener {
                if(it != null){
                    if(it["Liked"].toString().toBoolean()){
                        viewHolder.itemView.btnLike.isVisible = true
                        viewHolder.itemView.btnUnlike.isVisible = false
                    }
                    else{
                        viewHolder.itemView.btnLike.isVisible = false
                        viewHolder.itemView.btnUnlike.isVisible = true
                    }
                }
            }

        viewHolder.itemView.tvUsernameCard.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, others_profile_page::class.java)
            intent.putExtra("usernameOthers", username)
            viewHolder.itemView.context.startActivity(intent)
        }

        viewHolder.itemView.circularImageViewCard.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, others_profile_page::class.java)
            intent.putExtra("usernameOthers", username)
            viewHolder.itemView.context.startActivity(intent)
        }

        viewHolder.itemView.btnUnlike.setOnClickListener {
            viewHolder.itemView.btnLike.isVisible = true
            viewHolder.itemView.btnUnlike.isVisible = false
            liked(true)
            if(likeCount == 0){
                val likes = "No Likes Yet"
                viewHolder.itemView.tvLikeCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,15f)
                viewHolder.itemView.tvLikeCount.setTypeface(null, Typeface.ITALIC)
                viewHolder.itemView.tvLikeCount.text = likes
            }
            else if(likeCount == 1){
                val likes = "1 Like"
                viewHolder.itemView.tvLikeCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,17f)
                viewHolder.itemView.tvLikeCount.setTypeface(null, Typeface.NORMAL)
                viewHolder.itemView.tvLikeCount.text = likes
            }
            else{
                viewHolder.itemView.tvLikeCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,17f)
                viewHolder.itemView.tvLikeCount.setTypeface(null, Typeface.NORMAL)
                var count: Double = likeCount.toDouble()
                if(likeCount >= 1000000){
                    var ans: Double = ((count - count%10000)/1000000).toDouble()
                    val likes = "$ans" + "M Likes"
                    viewHolder.itemView.tvLikeCount.text = likes
                }
                else if(likeCount >= 1000){
                    var ans: Double = ((count - count%10)/1000).toDouble()
                    val likes = "$ans" + "K Likes"
                    viewHolder.itemView.tvLikeCount.text = likes
                }
                else{
                    val likes = "$likeCount Likes"
                    viewHolder.itemView.tvLikeCount.text = likes
                }
            }
        }
        viewHolder.itemView.btnLike.setOnClickListener {
            viewHolder.itemView.btnLike.isVisible = false
            viewHolder.itemView.btnUnlike.isVisible = true
            liked(false)
            if(likeCount == 0){
                val likes = "No Likes Yet"
                viewHolder.itemView.tvLikeCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,15f)
                viewHolder.itemView.tvLikeCount.setTypeface(null, Typeface.ITALIC)
                viewHolder.itemView.tvLikeCount.text = likes
            }
            else if(likeCount == 1){
                val likes = "1 Like"
                viewHolder.itemView.tvLikeCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,17f)
                viewHolder.itemView.tvLikeCount.setTypeface(null, Typeface.NORMAL)
                viewHolder.itemView.tvLikeCount.text = likes
            }
            else{
                viewHolder.itemView.tvLikeCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,17f)
                viewHolder.itemView.tvLikeCount.setTypeface(null, Typeface.NORMAL)
                var count: Double = likeCount.toDouble()
                if(likeCount >= 1000000){
                    var ans: Double = ((count - count%10000)/1000000).toDouble()
                    val likes = "$ans" + "M Likes"
                    viewHolder.itemView.tvLikeCount.text = likes
                }
                else if(likeCount >= 1000){
                    var ans: Double = ((count - count%10)/1000).toDouble()
                    val likes = "$ans" + "K Likes"
                    viewHolder.itemView.tvLikeCount.text = likes
                }
                else{
                    val likes = "$likeCount Likes"
                    viewHolder.itemView.tvLikeCount.text = likes
                }
            }
        }

        viewHolder.itemView.etCommentBox.addTextChangedListener{
            if(viewHolder.itemView.etCommentBox.text.toString() == ""){
                viewHolder.itemView.btnCommentDisabled.isVisible = true
                viewHolder.itemView.btnCommentEnabled.isVisible = false
                viewHolder.itemView.etCommentBox.isVisible = false
            }
            else{
                viewHolder.itemView.btnCommentDisabled.isVisible = false
                viewHolder.itemView.btnCommentEnabled.isVisible = true
            }
        }

//        viewHolder.itemView.circularImageViewCard.setOnClickListener {
//            val intent  = Intent(this, others_profile_page::class.java)
//            intent.
//        }

        viewHolder.itemView.btnCommentDisabled.setOnClickListener {
            if(isComBox){
                isComBox = false
                viewHolder.itemView.rvComments.isVisible = false
            }
            else if(viewHolder.itemView.etCommentBox.isVisible && !isComBox){
                viewHolder.itemView.etCommentBox.isVisible = false
                viewHolder.itemView.rvComments.isVisible = false
            }
            else{
                viewHolder.itemView.etCommentBox.isVisible = true
                viewHolder.itemView.rvComments.isVisible = true
            }
        }

        viewHolder.itemView.btnCommentEnabled.setOnClickListener {
            if(viewHolder.itemView.etCommentBox.text.toString() != ""){
                val comment = viewHolder.itemView.etCommentBox.text.toString()
                viewHolder.itemView.etCommentBox.setText("")
                commented(comment)
                adapterComment.add(Comment_class(myusername, comment))

                isComBox = true
            }
        }

        Glide.with(viewHolder.itemView.context)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(
                    bitmap: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    // loaded bitmap is here (bitmap)
                    Log.d("colorset", "bitmap loaded")

                    if (bitmap != null) {
                        Palette.Builder(bitmap).generate {
                            it?.let { palette ->
                                val vibrant: Int = palette.getVibrantColor(0x000000) // <=== color you want
                                val vibrantLight: Int = palette.getLightVibrantColor(0x000000)
                                val vibrantDark: Int = palette.getDarkVibrantColor(0x000000)
                                val muted: Int = palette.getMutedColor(0x000000)
                                val mutedLight: Int = palette.getLightMutedColor(0x000000)
                                val mutedDark: Int = palette.getDarkMutedColor(0x000000)
                                val dominant: Int = palette.getDominantColor(0x000000)

                                viewHolder.itemView.cvBehindImage.setCardBackgroundColor(muted)
//                        Picasso.get().load(imageUrl).into(viewHolder.itemView.postImageCard)
                                Log.d("colorset", "color set $muted")

                            }
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

            })

//        Picasso.get().load(imageUrl).into(object : com.squareup.picasso.Target {
//            @RequiresApi(Build.VERSION_CODES.O)
//            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//                // loaded bitmap is here (bitmap)
//                Log.d("colorset", "bitmap loaded")
//
//                if (bitmap != null) {
//                    Palette.Builder(bitmap).generate {
//                        it?.let { palette ->
//                            val vibrant: Int =
//                                palette.getVibrantColor(0x000000) // <=== color you want
//                            val vibrantLight: Int = palette.getLightVibrantColor(0x000000)
//                            val vibrantDark: Int = palette.getDarkVibrantColor(0x000000)
//                            val muted: Int = palette.getMutedColor(0x000000)
//                            val mutedLight: Int = palette.getLightMutedColor(0x000000)
//                            val mutedDark: Int = palette.getDarkMutedColor(0x000000)
//                            val dominant: Int = palette.getDominantColor(0x000000)
//
//                            viewHolder.itemView.cvBehindImage.setCardBackgroundColor(muted)
////                        Picasso.get().load(imageUrl).into(viewHolder.itemView.postImageCard)
//                            Log.d("colorset", "color set $muted")
//
//                        }
//                    }
//                }
//            }
//
//            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
//
//            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
//        })
    }

    override fun getLayout(): Int {
        return R.layout.post_adapter_cardiew
    }

    private fun getTagged(viewHolder: GroupieViewHolder){
        val db = FirebaseFirestore.getInstance()
        var faceDetect = mutableListOf<faceDetection>()
        var count = 0
        db.collection("Post").document(uid).collection("Faces")
            .get()
            .addOnSuccessListener {
                for(doc in it.documents){
                    if(doc.id != "Info"){
                        var rect = Rect(doc["Left"].toString().toInt(), doc["Top"].toString().toInt(), doc["Right"].toString().toInt(), doc["Bottom"].toString().toInt())
                        var faceObject = faceDetection(doc.id.toString(), rect, doc["RotY"].toString().toFloat(), doc["RotZ"].toString().toFloat(), 0.0f, doc["Tagged"].toString().toBoolean(), untaggedBitmap!!, 0)
                        faceDetect.add(faceObject)
                        if(doc["Tagged"].toString().toBoolean()){
                            count += 1
                        }
                    }
                }
                tagged = count
                if(tagged > 0){
                    var string = tagged.toString() + " Tags"
                    viewHolder.itemView.tvTagCount.text = string
                    viewHolder.itemView.tvTagCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,20f)
                    viewHolder.itemView.tvTagCount.setTypeface(null, Typeface.BOLD)
                    viewHolder.itemView.tvTagCount.setTextColor(ContextCompat.getColor(viewHolder.itemView.context, R.color.base2));

                }
                drawDetectionResult(untaggedBitmap!!, faceDetect)
            }
    }



    private fun drawDetectionResult(bitmap: Bitmap, detectionResults: List<faceDetection>) {
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        val pen = Paint()
        pen.textAlign = Paint.Align.LEFT
        var count = 0

        detectionResults.forEach {
            Log.d("cloud", "Creating Another Square")
            // draw bounding box
            if(it.named){
                pen.color = Color.GREEN
            }
            else{
                pen.color = Color.GRAY
            }

            pen.strokeWidth = 4F
            pen.style = Paint.Style.STROKE
            val box = it.boundingBox
//            box.left = box.left - 25
//            box.top = box.top - 25
//            box.right = box.right + 25
//            box.bottom = box.bottom + 25
//            canvas.drawRect(box, pen)
            canvas.drawRoundRect(box.left.toFloat(), box.top.toFloat(), box.right.toFloat() - 5f, box.bottom.toFloat(), 7F, 7F, pen)

            val tagSize = Rect(0, 0, 0, 0)

            // calculate the right font size
            pen.style = Paint.Style.FILL_AND_STROKE
            pen.color = Color.YELLOW

            if(it.named){
                pen.color = Color.GREEN
            }
            else{
                pen.color = Color.TRANSPARENT
            }

            pen.strokeWidth = 2F

            pen.textSize = 40F
            pen.getTextBounds(it.faceId, 0, it.faceId.length, tagSize)
            val fontSize: Float = pen.textSize * box.width() / tagSize.width()

            // adjust the font size so texts are inside the bounding box
            if (fontSize < pen.textSize) pen.textSize = fontSize

            var margin = (box.width() - tagSize.width()) / 2.0F
            if (margin < 0F) margin = 0F
            if(box.top-tagSize.height().times(1.3F) > 0){
                canvas.drawText(
                    it.faceId, box.left + margin,
                    box.top - tagSize.height().times(0.3F), pen
                )
            }
            else{
                canvas.drawText(
                    it.faceId, box.left + margin,
                    box.bottom + tagSize.height().times(1.3F), pen
                )
            }

            count += 1
        }
        Log.d("cloud", "NEW BITMAP CREATED")
        taggedBitmap = outputBitmap
    }

    private fun commented(comment: String){
        val time = FieldValue.serverTimestamp()
        val db = FirebaseFirestore.getInstance()

        val commenting = hashMapOf(
            "Text" to comment,
            "Timestamp" to time,
            "From" to myusername
        )

        db.collection("Post").document(uid).collection("Comments")
            .add(commenting)
            .addOnSuccessListener {
                if(it != null){
                    Log.d("mainfeed", "Post updated - comment added")
                }
            }
    }

    private fun likeCloud(liking: Boolean, results: MutableList<String>): Task<Any?> {
        var data: HashMap<String, Any> = hashMapOf<String, Any>()
        data.put("userName", myusername)
        data.put("uid", uid)
        data.put("noOfTags", results.size.toString())
        var count = 0
        for(tag in results){
            var string = "tagNo" + count
            data.put(string, tag)
            count += 1
            Log.d("cloud", tag)
        }
        while(count < 10){
            var string = "tagNo" + count
            data.put(string, "xxxxx")
            count += 1
        }
        if(liking){
            return functions
                .getHttpsCallable("likePost")
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
                .getHttpsCallable("dislikePost")
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

    private fun likeCaller(liking: Boolean){
        var db = FirebaseFirestore.getInstance()
        db.collection("Post").document(uid).collection("Tags")
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    var result = it.result
                    var count = 0
                    var list = mutableListOf<String>()
                    for(doc in result.documents){
                        if(doc.id != "Info" && doc["Confidence"].toString().toFloat() > 0.60f){
                            count += 1
                            list.add(doc.id)
                        }
                    }
                    likeCloud(liking, list).addOnCompleteListener(OnCompleteListener { task ->
                        if(task.isSuccessful){
                            db.collection("Users").document(myusername)
                                .get()
                                .addOnSuccessListener {it2 ->
                                    if(liking){
                                        db.collection("Users").document(myusername)
                                            .update("Tags", (it2["Tags"].toString().toInt() + count))
                                            .addOnCompleteListener {
                                                Log.d("cloud", "Liked")
                                            }
                                    }
                                    else{
                                        db.collection("Users").document(myusername)
                                            .update("Tags", (it2["Tags"].toString().toInt() - count))
                                            .addOnCompleteListener {
                                                Log.d("cloud", "Liked")
                                            }
                                    }
                                }
                        }
                    })
                }
            }
    }

    private fun liked(liking: Boolean){
//        adapter.clear()
        if(liking){
            likeCount += 1
        }
        else{
            likeCount -= 1
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("Post").document(uid)
            .update("Likes", likeCount)
            .addOnSuccessListener {
                if(it != null){
                    Log.d("mainfeed", "Post updated - like updated")
                }
            }
        if(liking){
            db.collection("Users").document(myusername).collection("My Feed").document(uid)
                .update("Liked", true)
                .addOnSuccessListener {
                    if(it != null){
                        Log.d("mainfeed", "User updated - like updated")
                    }
                }
            likeCaller(liking)
        }
        else{
            db.collection("Users").document(myusername).collection("My Feed").document(uid)
                .update("Liked", false)
                .addOnSuccessListener {
                    if(it != null){
                        Log.d("mainfeed", "User updated - like updated")
                    }
                }
            likeCaller(liking)
        }
    }
}

class Comment_class(val username: String, val comment: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.username.text = "$username: "
        viewHolder.itemView.comment.text = comment

        viewHolder.itemView.username.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, others_profile_page::class.java)
            intent.putExtra("usernameOthers", username)
            viewHolder.itemView.context.startActivity(intent)
        }
    }

    override fun getLayout(): Int {
        return R.layout.comment_adapter
    }

}

class UserSearch(val username: String, val url: String, val Name: String, val isUser: Boolean): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        if (isUser) {
            return R.layout.new_chat_adapter
        }
        else
        {
            return R.layout.branchnames_adapter
        }
    }

    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        if(isUser)
        {
            viewHolder.itemView.pbNewChat.isVisible = true
            viewHolder.itemView.tv_usernames_newMessage.text = username
            Picasso.get().load(url).into(viewHolder.itemView.cv_dp_newMessage, object : Callback {
                override fun onSuccess() {
                    viewHolder.itemView.pbNewChat.isVisible = false
                }
                override fun onError(e: java.lang.Exception?) {
                    Log.d("loading", "ERROR - $e")
                    TODO("PROGRESS BAR")
                }
            })
            Log.d("adapter", "adapter added")
        }
        else
        {
            viewHolder.itemView.tvBranchNames.text = username
        }
    }
}

