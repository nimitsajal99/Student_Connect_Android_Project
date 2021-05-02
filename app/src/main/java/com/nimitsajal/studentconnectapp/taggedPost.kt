package com.nimitsajal.studentconnectapp

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_my_post.*
import kotlinx.android.synthetic.main.activity_my_post.btnBack
import kotlinx.android.synthetic.main.activity_my_post.circularImageViewCard
import kotlinx.android.synthetic.main.activity_my_post.cvBehindImage
import kotlinx.android.synthetic.main.activity_my_post.pbDpMyPost
import kotlinx.android.synthetic.main.activity_my_post.pbMyPost
import kotlinx.android.synthetic.main.activity_my_post.postImageCard
import kotlinx.android.synthetic.main.activity_my_post.rvComments
import kotlinx.android.synthetic.main.activity_my_post.tvDescriptionCard
import kotlinx.android.synthetic.main.activity_my_post.tvLikeCount
import kotlinx.android.synthetic.main.activity_my_post.tvUsernameCard
import kotlinx.android.synthetic.main.activity_tagged_post.*
import kotlinx.android.synthetic.main.activity_upload_post.*
import java.util.HashMap

class taggedPost : AppCompatActivity() {

    private lateinit var detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tagged_post)

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

        btnBack.setOnClickListener {
            goToProfile(username!!, isOther!!, 1)
        }

        tvRemoveTag.setOnClickListener {
            tvRemoveTag.visibility = View.GONE
            rvComments_tagged.visibility = View.GONE
            pbRemoveTag.visibility = View.VISIBLE
            removeTag(username, uid!!, db)
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
                            return this@taggedPost.onSwipeRight(username!!)
                        } else {
                            //Left Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Left", Toast.LENGTH_SHORT).show()
                            return this@taggedPost.onSwipeLeft()
                        }
                    } else {
                        return false
                    }
                } else {
                    //Up or down Swipe
                    if (Math.abs(diffY) > SWIPE_THREASHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THREASHOLD) {
                        if (diffY > 0) {
                            //Up Swipe
                            return this@taggedPost.onSwipeUp()
                        } else {
                            //Bottom Swipe
                            return this@taggedPost.onSwipeBottom()

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
                            rvComments_tagged.adapter = null
                            rvComments_tagged.adapter = adapterComments
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

        Glide.with(this).load(dp)
            .circleCrop()
            .into(circularImageViewCard)
        pbDpMyPost.isVisible = false

        var faceDetect = mutableListOf<faceDetection>()

        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    db.collection("Post").document(uid).collection("Faces")
                        .get()
                        .addOnSuccessListener {
                            for(doc in it.documents){
                                if(doc.id != "Info"){
                                    var rect = Rect(doc["Left"].toString().toInt(), doc["Top"].toString().toInt(), doc["Right"].toString().toInt(), doc["Bottom"].toString().toInt())
                                    var faceObject = faceDetection(doc.id.toString(), rect, doc["RotY"].toString().toFloat(), doc["RotZ"].toString().toFloat(), 0.0f, doc["Tagged"].toString().toBoolean(), resource, 0)
                                    faceDetect.add(faceObject)
                                }
                            }
                            postImageCard.setImageBitmap(drawDetectionResult(resource, faceDetect))
                        }
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                    // this is called when imageView is cleared on lifecycle call or for
                    // some other reason.
                    // if you are referencing the bitmap somewhere else too other than this imageView
                    // clear it here as you can no longer have the bitmap
                }
            })



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
                    rvComments_tagged.adapter = adapterComments
                }
            }

    }

    private fun drawDetectionResult(bitmap: Bitmap, detectionResults: List<faceDetection>): Bitmap? {
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
        return outputBitmap
    }

    private fun removeTag(username: String, uid: String, db: FirebaseFirestore){
        db.collection("Users").document(username).collection("My Tags").document(uid)
            .delete()
            .addOnSuccessListener {
                db.collection("Post").document(uid).collection("Faces").document(username)
                    .get()
                    .addOnSuccessListener {it2 ->
                        var data: HashMap<String, Any> = hashMapOf<String, Any>()
                        data.put("Bottom", it2["Bottom"].toString())
                        data.put("Left", it2["Left"].toString())
                        data.put("Right", it2["Right"].toString())
                        data.put("Top", it2["Top"].toString())
                        data.put("RotY", it2["RotY"].toString().toFloat())
                        data.put("RotZ", it2["RotZ"].toString().toFloat())
                        data.put("Smile", it2["Smile"].toString().toBoolean())
                        data.put("Tagged", false)

                        db.collection("Post").document(uid).collection("Faces")
                            .add(data)
                            .addOnSuccessListener {
                                db.collection("Post").document(uid).collection("Faces").document(username)
                                    .delete()
                                    .addOnSuccessListener {
                                        val intent = Intent(this, profilePage::class.java)
                                        intent.putExtra("username", username)
                                        startActivity(intent)
                                        overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom)
                                        finish()
                                    }
                            }
                    }
            }
    }


}