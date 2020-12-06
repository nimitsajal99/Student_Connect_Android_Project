package com.nimitsajal.studentconnectapp

import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.palette.graphics.Palette
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.auth.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.activity_main_feed.view.*
import kotlinx.android.synthetic.main.branchnames_adapter.view.*
import kotlinx.android.synthetic.main.comment_adapter.view.*
import kotlinx.android.synthetic.main.new_chat_adapter.view.*
import kotlinx.android.synthetic.main.post_adapter_cardiew.view.*


class mainFeed : AppCompatActivity() {

    private lateinit var detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_feed)

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
                    detector = GestureDetectorCompat(this,DiaryGestureListener(username,adapter,db,arraySearch))
                    loadFeed(arrayPost, adapter, username!!, db)
                }
                else{
                    showToast("ERROR", 1)
                    return@addOnSuccessListener
                }
            }
        }

        detector = GestureDetectorCompat(this,DiaryGestureListener(username,adapter,db,arraySearch))

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
            startActivity(intent)
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

//        btnFeed.setOnClickListener {
//            val intent = Intent(this, mainFeed::class.java)
//            intent.putExtra("username", username)
//            startActivity(intent)
//        }

        btnSearch.setOnClickListener {
            if(etSearchMainFeed.isVisible==true){
                closeSearchBar()
            }
            else
            {
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
                    loadSearch(username!!,db,adapter,arraySearch)
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

    inner class DiaryGestureListener(username: String?, adapter: GroupAdapter<GroupieViewHolder>, db: FirebaseFirestore, arraySearch: MutableList<usersList>) : GestureDetector.SimpleOnGestureListener()
    {

        //TODO: Swipe Link - > https://www.youtube.com/watch?v=j1aydFEOEA0
        private val db = db
        private val arraySearch = arraySearch
        private val adapter = adapter
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
                            return this@mainFeed.onSwipeUp()
                        }
                        else
                        {
                            //Bottom Swipe
                            return this@mainFeed.onSwipeBottom(username!!,adapter,db,arraySearch)

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

    private fun onSwipeUp():Boolean {
        //Toast.makeText(this, "Swipe Up", Toast.LENGTH_SHORT).show()
        closeSearchBar()
        return true
    }

    private fun onSwipeBottom(username: String, adapter: GroupAdapter<GroupieViewHolder>, db: FirebaseFirestore, arraySearch: MutableList<usersList>): Boolean {
        openSearchBar(username,adapter,db,arraySearch)
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

    fun openSearchBar(username: String, adapter: GroupAdapter<GroupieViewHolder>, db: FirebaseFirestore, arraySearch: MutableList<usersList>)
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

    private fun loadSearch(username: String, db: FirebaseFirestore, adapter: GroupAdapter<GroupieViewHolder>, arraySearch: MutableList<usersList>){
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
                                    if(pattern.containsMatchIn(document["Name"].toString()) || pattern.containsMatchIn(document.id) || pattern.containsMatchIn(document["College"].toString())|| pattern.containsMatchIn(document["Branch"].toString()))
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
                                    val temp = usersList(document.id, "", document["Name"].toString(), document["Picture"].toString())
                                    arraySearch.add(temp)
                                    adapter.add(UserSearch(document.id, document["Picture"].toString(), document["Name"].toString(),true))
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
                                                    adapter.add(UserSearch(doc.id, "", document.id,false))
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

    private fun loadFeed(arrayPost: MutableList<postList>, adapter: GroupAdapter<GroupieViewHolder>, username: String, db: FirebaseFirestore) {
        adapter.clear()
        val user = db.collection("Users").document(username).collection("My Feed")
        user.orderBy("Time", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                if (it != null) {
                    adapter.clear()
                    for (document in it) {
                        if (document.id != "Info") {
                            db.collection("Post").document(document.id)
                                .get()
                                .addOnSuccessListener { it2 ->
                                    if(it2 != null){
                                        try {
                                            val temp = postList(it2["From"].toString(), it2["Picture"].toString(), it2["Dp"].toString(), it2["Description"].toString(), it2["Likes"].toString().toInt(), document.id.toString())
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
                                                                adapterComments.add(Comment_class(doc["From"].toString(), doc["Text"].toString()))
                                                            }
                                                        }
                                                        adapter.add(post_class(it2["From"].toString(), it2["Picture"].toString(), it2["Dp"].toString(), it2["Description"].toString(), it2["Likes"].toString().toInt(), document.id.toString(), username, adapter, adapterComments, false))
                                                    }
                                                }
                                        }
                                        catch (e: Exception){
                                            Log.d("mainfeed", "$e")
                                            db.collection("Users").document(username).collection("My Feed").document(document.id)
                                                .delete()
                                                .addOnSuccessListener {
                                                    Log.d("mainfeed", "TRY document ${document.id} deleted from $username ")
                                                }
                                                .addOnFailureListener {
                                                    Log.d("mainfeed", "TRY document ${document.id} not deleted from $username ")
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
                    rvFeed.adapter = adapter
                }
            }
    }

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

data class postList(var username: String, var imageUrl: String, var dpUrl: String, var description: String, var likeCount: Int, var uid: String)
{

}

class post_class(var username: String, var imageUrl: String, var dpUrl: String, var description: String, var likeCount: Int, var uid: String, var myusername: String, var adapter: GroupAdapter<GroupieViewHolder>, var adapterComment: GroupAdapter<GroupieViewHolder>, var isComBox: Boolean): Item<GroupieViewHolder>()
{
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        if(likeCount == 0){
            val likes = "No Likes Yet"
            viewHolder.itemView.tvLikeCount.text = likes
        }
        else if(likeCount == 1){
            val likes = "1 Like"
            viewHolder.itemView.tvLikeCount.text = likes
        }
        else{
            val likes = "$likeCount Likes"
            viewHolder.itemView.tvLikeCount.text = likes
        }

        viewHolder.itemView.rvComments.adapter = adapterComment

        viewHolder.itemView.tvUsernameCard.text = username
        viewHolder.itemView.tvDescriptionCard.text = description
        Picasso.get().load(dpUrl).into(viewHolder.itemView.circularImageViewCard)
        Picasso.get().load(imageUrl).into(viewHolder.itemView.postImageCard)

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

        viewHolder.itemView.btnUnlike.setOnClickListener {
            viewHolder.itemView.btnLike.isVisible = true
            viewHolder.itemView.btnUnlike.isVisible = false
            liked(true)
            if(likeCount == 0){
                val likes = "No Likes Yet"
                viewHolder.itemView.tvLikeCount.text = likes
            }
            else if(likeCount == 1){
                val likes = "1 Like"
                viewHolder.itemView.tvLikeCount.text = likes
            }
            else{
                val likes = "$likeCount Likes"
                viewHolder.itemView.tvLikeCount.text = likes
            }
        }
        viewHolder.itemView.btnLike.setOnClickListener {
            viewHolder.itemView.btnLike.isVisible = false
            viewHolder.itemView.btnUnlike.isVisible = true
            liked(false)
            if(likeCount == 0){
                val likes = "No Likes Yet"
                viewHolder.itemView.tvLikeCount.text = likes
            }
            else if(likeCount == 1){
                val likes = "1 Like"
                viewHolder.itemView.tvLikeCount.text = likes
            }
            else{
                val likes = "$likeCount Likes"
                viewHolder.itemView.tvLikeCount.text = likes
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

        Picasso.get().load(imageUrl).into(object : com.squareup.picasso.Target {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                // loaded bitmap is here (bitmap)
                Log.d("colorset", "bitmap loaded")

                if (bitmap != null) {
                    Palette.Builder(bitmap).generate { it?.let {  palette ->
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

                    } }
                }
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
        })
    }

    override fun getLayout(): Int {
        return R.layout.post_adapter_cardiew
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
        }
        else{
            db.collection("Users").document(myusername).collection("My Feed").document(uid)
                .update("Liked", false)
                .addOnSuccessListener {
                    if(it != null){
                        Log.d("mainfeed", "User updated - like updated")
                    }
                }
        }
    }
}

class Comment_class(val username: String, val comment: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.username.text = "$username: "
        viewHolder.itemView.comment.text = comment
    }

    override fun getLayout(): Int {
        return R.layout.comment_adapter
    }

}

class UserSearch(val username: String,val url: String, val Name: String, val isUser: Boolean): Item<GroupieViewHolder>(){
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
            viewHolder.itemView.tv_usernames_newMessage.text = username
            Picasso.get().load(url).into(viewHolder.itemView.cv_dp_newMessage)
            Log.d("adapter", "adapter added")
        }
        else
        {
            viewHolder.itemView.tvBranchNames.text = username
        }

    }
}

