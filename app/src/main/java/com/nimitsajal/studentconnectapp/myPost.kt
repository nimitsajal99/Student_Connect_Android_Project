package com.nimitsajal.studentconnectapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_my_post.*
import kotlinx.android.synthetic.main.activity_profile_page.*
import kotlinx.android.synthetic.main.post_adapter_cardiew.view.*
import kotlinx.android.synthetic.main.profile_post_adapter.view.*

class myPost : AppCompatActivity() {

    private lateinit var detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_post)

        var username = ""
        var myUsername = intent.getStringExtra("username")
        var url = intent.getStringExtra("picture")
        var uid = intent.getStringExtra("uid")
        var description = intent.getStringExtra("description")
        var dp = intent.getStringExtra("dp")
        var isOther = intent.getStringExtra("others")



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
                    loadPost(db, username!!, url!!, uid!!, description!!, dp!!, myUsername!!, isOther!!)
                }
                else{
                    showToast("ERROR", 1)
                    return@addOnSuccessListener
                }
            }
        }

        detector = GestureDetectorCompat(this,DiaryGestureListener(username))

        btnUnlike.setOnClickListener {
            btnLike.isVisible = true
            btnUnlike.isVisible = false
            liked(true, username!!, uid!!, db)
        }
        btnLike.setOnClickListener {
            btnLike.isVisible = false
            btnUnlike.isVisible = true
            liked(false, username!!, uid!!, db)
        }
        btnBack.setOnClickListener {
            goToProfile(username!!, isOther!!, 1)
        }

        etCommentBox.addTextChangedListener{
            if(etCommentBox.text.toString() == ""){
                btnCommentDisabled.isVisible = true
                btnCommentEnabled.isVisible = false
            }
            else{
                btnCommentDisabled.isVisible = false
                btnCommentEnabled.isVisible = true
            }
        }

        btnCommentEnabled.setOnClickListener {
            if(etCommentBox.text.toString() != ""){
                val comment = etCommentBox.text.toString()
                etCommentBox.setText("")
                commented(comment, username!!, uid!!, db)
            }
        }

        btnDeletePost.setOnClickListener {
            //TODO: menu inflate / dialog box inflate "Are you sure?"
            deletePost(username!!, uid!!, db)
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
                            return this@myPost.onSwipeRight(username!!)
                        } else {
                            //Left Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Left", Toast.LENGTH_SHORT).show()
                            return this@myPost.onSwipeLeft()
                        }
                    } else {
                        return false
                    }
                } else {
                    //Up or down Swipe
                    if (Math.abs(diffY) > SWIPE_THREASHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THREASHOLD) {
                        if (diffY > 0) {
                            //Up Swipe
                            return this@myPost.onSwipeUp()
                        } else {
                            //Bottom Swipe
                            return this@myPost.onSwipeBottom()

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

    private fun onSwipeLeft(): Boolean {
        //Toast.makeText(this, "Swipe Left", Toast.LENGTH_SHORT).show()

        return true
    }

    private fun onSwipeRight(username: String): Boolean {
        //Toast.makeText(this, "Swipe Right", Toast.LENGTH_SHORT).show()
        goToProfile(username, "true", 1)
        return true
    }

    private fun deletePost(username: String, uid: String, db: FirebaseFirestore){
        db.collection("Users").document(username).collection("My Posts").document(uid)
            .delete()
            .addOnSuccessListener {
                Log.d("mainfeed", "post deleted from $username")
            }
        db.collection("Post").document(uid)
            .delete()
            .addOnSuccessListener {
                Log.d("mainfeed", "post deleted from Posts")
            }
        showToast("Post DELETED", 2)
        goToProfile(username, "false", 2)
    }

    private fun commented(comment: String, username: String, uid: String, db: FirebaseFirestore){
        val time = FieldValue.serverTimestamp()
        val commenting = hashMapOf(
            "Text" to comment,
            "Timestamp" to time,
            "From" to username
        )
        db.collection("Post").document(uid).collection("Comments")
            .add(commenting)
            .addOnSuccessListener {
                if(it != null){
                    Log.d("mainfeed", "Post updated - comment added")
                }
                val adapterComments = GroupAdapter<GroupieViewHolder>()
                db.collection("Post").document(uid).collection("Comments")
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
                            rvComments.adapter = null
                            rvComments.adapter = adapterComments
                        }
                    }
            }
    }

    private fun goToProfile(username: String, isOther: String, type: Int){
        if(isOther == "true"){
            onBackPressed()
        }
        else if(isOther != "true" && type == 1){
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            finish()
        }
        else{
            val intent = Intent(this, profilePage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom)
            finish()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        finish()
    }

    private fun loadPost(db: FirebaseFirestore, username: String, url: String, uid: String, description: String, dp: String, myUsername: String, isOther: String){
        Picasso.get().load(url).into(postImageCard, object : Callback {
            override fun onSuccess() {
                pbMyPost.isVisible = false
            }

            override fun onError(e: java.lang.Exception?) {
                Log.d("loading", "ERROR - $e")
            }
        })
        tvUsernameCard.text = myUsername
        tvDescriptionCard.text = description

        if(isOther != "true"){
            btnDeletePost.isVisible = true
        }

        Glide.with(this).load(dp)
            .circleCrop()
            .into(circularImageViewCard)
        pbDpMyPost.isVisible = false

//        Picasso.get().load(dp).into(object :
//            com.squareup.picasso.Target {
//            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//                // loaded bitmap is here (bitmap)
//
//                circularImageViewCard.setImageBitmap(bitmap)
//                pbDpMyPost.isVisible = false
//            }
//            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
//            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
//        })

        Glide.with(this)
            .asBitmap()
            .load(url)
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
                                cvBehindImage.setCardBackgroundColor(muted)
//                        Picasso.get().load(imageUrl).into(viewHolder.itemView.postImageCard)
                                Log.d("colorset", "color set $muted")
                            }
                        }
                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })

        db.collection("Post").document(uid)
            .get()
            .addOnSuccessListener {
                if(it != null){
                    var likeCount = it["Likes"].toString().toInt()
                    if(likeCount == 0){
                        val likes = "No Likes Yet"
                        tvLikeCount.text = likes
                    }
                    else if(likeCount == 1){
                        val likes = "1 Like"
                        tvLikeCount.text = likes
                    }
                    else{
                        val likes = "$likeCount Likes"
                        tvLikeCount.text = likes
                    }
                }
            }

        val adapterComments = GroupAdapter<GroupieViewHolder>()
        db.collection("Post").document(uid).collection("Comments")
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
                    rvComments.adapter = adapterComments
                }
            }
        if(isOther == "true"){
            db.collection("Users").document(username).collection("My Feed").document(uid)
                .get()
                .addOnSuccessListener {
                    if(it != null){
                        if(it["Liked"].toString().toBoolean()){
                            btnLike.isVisible = true
                            btnUnlike.isVisible = false
                        }
                        else{
                            btnLike.isVisible = false
                            btnUnlike.isVisible = true
                        }
                    }
                }
        }
        else{
            db.collection("Users").document(username).collection("My Posts").document(uid)
                .get()
                .addOnSuccessListener {
                    if(it != null){
                        if(it["Liked"].toString().toBoolean()){
                            btnLike.isVisible = true
                            btnUnlike.isVisible = false
                        }
                        else{
                            btnLike.isVisible = false
                            btnUnlike.isVisible = true
                        }
                    }
                }
        }
    }

    private fun liked(liking: Boolean, username: String, uid: String, db: FirebaseFirestore){
//        adapter.clear()

        db.collection("Post").document(uid)
            .get()
            .addOnSuccessListener {
                if(it  != null){
                    var likeCount = it["Likes"].toString().toInt()
                    if(liking){
                        likeCount += 1
                    }
                    else{
                        likeCount -= 1
                    }
                    db.collection("Post").document(uid)
                        .update("Likes", likeCount.toString())
                        .addOnSuccessListener {
                            Log.d("mypost", "Post likes updated")
                        }
                    if(likeCount == 0){
                        val likes = "No Likes Yet"
                        tvLikeCount.text = likes
                    }
                    else if(likeCount == 1){
                        val likes = "1 Like"
                        tvLikeCount.text = likes
                    }
                    else{
                        val likes = "$likeCount Likes"
                        tvLikeCount.text = likes
                    }
                }
            }

        if(liking){
            db.collection("Users").document(username).collection("My Posts").document(uid)
                .update("Liked", true)
                .addOnSuccessListener {
                    if(it != null){
                        Log.d("mainfeed", "User updated - like updated")
                    }
                }
        }
        else{
            db.collection("Users").document(username).collection("My Posts").document(uid)
                .update("Liked", false)
                .addOnSuccessListener {
                    if(it != null){
                        Log.d("mainfeed", "User updated - like updated")
                    }
                }
        }
    }
}