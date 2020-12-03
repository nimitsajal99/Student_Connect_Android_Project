package com.nimitsajal.studentconnectapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.activity_main_feed.btnFeed
import kotlinx.android.synthetic.main.activity_main_feed.btnLogout
import kotlinx.android.synthetic.main.activity_profile_page.*
import kotlinx.android.synthetic.main.activity_profile_page.btnChat
import kotlinx.android.synthetic.main.details_adapter.view.*
import kotlinx.android.synthetic.main.new_chat_adapter.view.*
import kotlinx.android.synthetic.main.profile_post_adapter.view.*
import java.lang.System


class profilePage : AppCompatActivity() {

    private lateinit var detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        val adapter = GroupAdapter<GroupieViewHolder>()

        val mLayoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        rvProfilePage.layoutManager = mLayoutManager


        var username = intent.getStringExtra("username")
        tvUsername.setText("").toString()
        //tvUsername.setText(username).toString()


        tvDescription.setText("").toString()



        tvName.setText("").toString()



        btnFriends.setText("Friends").toString()



        btnEdit.setText("Edit").toString()



//        tvDetails.setText("").toString()

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
                    getUser(auth, username!!)
                    loadPost(db, username!!, adapter)
                }
                else{
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
            }
        }

        detector = GestureDetectorCompat(this,DiaryGestureListener(username))

        btnChat.setOnClickListener {
            goToChat(username!!)
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

//        btnProfile.setOnClickListener {
//            val intent = Intent(this, profilePage::class.java)
//            intent.putExtra("username", username)
//            startActivity(intent)
//        }

        btnFriends.setOnClickListener {
            friendList(db, username!!)
        }

        btnEventProfile.setOnClickListener {
            goToEvent(username!!)
        }

        btnFeed.setOnClickListener {
            val intent = Intent(this, mainFeed::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
        }

        btnEdit.setOnClickListener {
            val intent = Intent(this, editProfile::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        //TODO: Double Click edit
        tvName.setOnClickListener(object : profilePage.DoubleClickListener(){
            override fun onDoubleClick(v: View?){
                Log.d("double", "double press")
                toEdit(username!!)
            }
        })

        circularImageView.setOnClickListener(object : profilePage.DoubleClickListener(){
            override fun onDoubleClick(v: View?){
                Log.d("double", "double press")
                toEdit(username!!)
            }
        })

        tvDescription.setOnClickListener(object : profilePage.DoubleClickListener(){
            override fun onDoubleClick(v: View?){
                Log.d("double", "double press")
                toEdit(username!!)
            }
        })

    }

    private fun loadPost(db: FirebaseFirestore, username: String, adapter: GroupAdapter<GroupieViewHolder>){
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
                                                    adapter.add(profile_post_class(it2["Picture"].toString(), it2["Likes"].toString().toInt(), comCount, it2["Description"].toString(), username, db, document.id))
                                                }
                                            }
                                    }
                                }
                        }
                    }
                }
                adapter.setOnItemLongClickListener { item, view ->
                    Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
                    return@setOnItemLongClickListener true
                }
                rvProfilePage.adapter = adapter
            }
    }

    private fun friendList(db: FirebaseFirestore, username: String)
    {

        //TODO: PopUp Dialog Link -> https://www.youtube.com/watch?v=Em7LJddHAbQ&t=588s


        var list: ListView = ListView(this)
//        val adapter = GroupAdapter<GroupieViewHolder>()
//        var data  = ArrayList<UserItemSearch>()
        var ddata  = ArrayList<String>()
        db.collection("Users").document(username!!).collection("Friends")
            .get()
            .addOnSuccessListener {
                if(it == null)
                {
                    return@addOnSuccessListener
                }
                for(document in it)
                {
                    if(document.id!="Info")
                    {
//                        data.add(UserItemSearch(document.id, document["Picture"].toString(), username))
                        ddata.add(document.id)
                        //adapter.add(UserItemSearch(document.id, document["Picture"].toString(), username))
                    }
                }
                //var adda = ArrayAdapter<UserItemSearch>(this,R.layout.new_chat_adapter, data)
                var adda =  ArrayAdapter<String>(this,R.layout.new_chat_adapter, R.id.tv_usernames_newMessage, ddata)

//                var addaa = GroupAdapter<GroupieViewHolder>()
//                addaa.add()
                list.adapter = adda
                var builder = AlertDialog.Builder(this)
                builder.setCancelable(true)
                builder.setView(list)
                var dialog = builder.create()
                dialog.show()
                //TODO: make it work
//                list.setOnItemClickListener { parent, view, position, id ->
//
//                    Toast.makeText(this@profilePage,"in",Toast.LENGTH_SHORT).show()
//                    Toast.makeText(this@profilePage,"parent -> $parent , view -> $view , position -> $position , id -> $id", Toast.LENGTH_SHORT).show()
//                    dialog.dismiss()
//                }
            }

    }


    private fun goToChat(username: String)
    {
        val intent = Intent(this, currentChats::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
        finish()
    }

    private fun goToEvent(username: String)
    {
        val intent = Intent(this, eventPage::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
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
                            return this@profilePage.onSwipeRight(username!!)
                        } else {
                            //Left Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Left", Toast.LENGTH_SHORT).show()
                            return this@profilePage.onSwipeLeft(username!!)
                        }
                    } else {
                        return false
                    }
                } else {
                    //Up or down Swipe
                    if (Math.abs(diffY) > SWIPE_THREASHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THREASHOLD) {
                        if (diffY > 0) {
                            //Up Swipe
                            return this@profilePage.onSwipeUp()
                        } else {
                            //Bottom Swipe
                            return this@profilePage.onSwipeBottom()

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
        Toast.makeText(this, "Swipe Up", Toast.LENGTH_SHORT).show()

        return false
    }

    private fun onSwipeBottom(): Boolean {
        Toast.makeText(this, "Swipe Down", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun onSwipeLeft(username: String): Boolean {
        Toast.makeText(this, "Swipe Left", Toast.LENGTH_SHORT).show()
        goToChat(username)
        return true
    }

    private fun onSwipeRight(username: String): Boolean {
        Toast.makeText(this, "Swipe Right", Toast.LENGTH_SHORT).show()
        goToEvent(username)
        return true
    }

    private fun toEdit(username: String)
    {
        val intent = Intent(this,editProfile::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
        finish()
    }

    abstract class DoubleClickListener: View.OnClickListener {
        private val DOUBLE_CLICK_TIME_DELTA: Long = 300
        var lastClickTime: Long = 0
        override fun onClick(v: View?) {
            val clickTime = System.currentTimeMillis()
            if(clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
                onDoubleClick(v)
            }
            lastClickTime = clickTime
        }
        open fun onDoubleClick(v: View?)
        {
            object {
                private val DOUBLE_CLICK_TIME_DELTA: Long = 300
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

    private fun getUser(auth: FirebaseAuth, username: String){
        val db = FirebaseFirestore.getInstance()

//        if(username.toString() == ""){
//            collapse((tvUsername))
//        }
//        else{
//            expand(tvUsername, 999)
//            tvUsername.setText(username.toString()).toString()
//        }

        tvUsername.setText(username.toString()).toString()

        val user_info = db.collection("Users").document(username)
        user_info.get().addOnSuccessListener {
            if(it != null){

                var selectedPhotoUrl_string = it.getString("Picture")

                val selectedPhotoUrl = Uri.parse(selectedPhotoUrl_string)

//                val selectedPhotoUrl = selectedPhotoUrl_string.toUri()
//                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUrl)
//                circularImageView.setImageBitmap(bitmap)


                Picasso.get().load(selectedPhotoUrl_string).into(object :
                    com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        // loaded bitmap is here (bitmap)

                        circularImageView.setImageBitmap(bitmap)
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                })

                tvName.setText(it.getString("Name").toString()).toString()

                if(it.getString("Description").toString() == ""){
                    collapse(tvDescription)
//                    tvDescription.setText(it.getString("Description").toString()).toString()
//                    expand(tvName, 999)
                }
                else{
                    expand(tvDescription, 999)
                    tvDescription.setText(it.getString("Description").toString()).toString()
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
                rvDetails.adapter = adapter
            }

        var friends = "Friends ("
        val FriendsCount = user_info.collection("Friends").get()
            .addOnSuccessListener {
                if(it != null){
                    val count = it.size() - 1
                    friends = friends + count + ")"
                    btnFriends.setText(friends).toString()
                }
            }
    }

}

class details_class(val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvdetails.text = text
    }

    override fun getLayout(): Int {
        return R.layout.details_adapter
    }

}

class profile_post_class(val url:  String, var likeCount: Int, val commentCount: Int, val description: String, val username: String, val db: FirebaseFirestore, var uid: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Picasso.get().load(url).into(viewHolder.itemView.postImageProfile)
        var count = description.length
        if(count > 19){
            var remString = description.dropLast(count-17)
            remString = "$remString..."
            viewHolder.itemView.tvDescriptionProfile.text = remString
        }
        else{
            viewHolder.itemView.tvDescriptionProfile.text = description
        }
        viewHolder.itemView.tvLikeCount.text = likeCount.toString()
        viewHolder.itemView.tvComCount.text = commentCount.toString()
        viewHolder.itemView.btnLikeCount.setOnClickListener {
            Log.d("profilepage", "clicked like button")
            viewHolder.itemView.btnLikeCount.isEnabled = false
            likePost(viewHolder)

        }
//        viewHolder.itemView.postImageProfile.setOnClickListener {
//            val intent = Intent(this, myPost::class.java)
//            startActivity(intent)
//        }
    }

    private fun likePost(viewHolder: GroupieViewHolder){
        db.collection("Users").document(username).collection("My Posts").document(uid)
            .get()
            .addOnSuccessListener {
                if(it != null){
                    if(it["Liked"].toString().toBoolean()){
                        Log.d("profilepage", "disliking")
                        db.collection("Users").document(username).collection("My Posts").document(uid)
                            .update("Liked", false)
                            .addOnSuccessListener { it2 ->
                                if(it2 != null){
                                    Log.d("profilepage", "user updated")
                                }
                            }
                        db.collection("Post").document(uid)
                            .get()
                            .addOnSuccessListener { it3 ->
                                if(it3 != null){
                                    var count = it3["Likes"].toString().toInt()
                                    count-=1
                                    db.collection("Post").document(uid)
                                        .update("Likes", count)
                                        .addOnSuccessListener { it4 ->
                                            if(it4 != null){
                                                Log.d("profilepage", "post updated")
                                            }
                                            viewHolder.itemView.tvLikeCount.text = (count).toString()
                                            viewHolder.itemView.btnLikeCount.isEnabled = true
                                        }
                                }
                            }
                    }
                    else{
                        Log.d("profilepage", "liking")
                        db.collection("Users").document(username).collection("My Posts").document(uid)
                            .update("Liked", true)
                            .addOnSuccessListener { it2 ->
                                if(it2 != null){
                                    Log.d("profilepage", "user updated")
                                }
                            }
                        db.collection("Post").document(uid)
                            .get()
                            .addOnSuccessListener { it3 ->
                                if(it3 != null){
                                    var count = it3["Likes"].toString().toInt()
                                    count+=1
                                    db.collection("Post").document(uid)
                                        .update("Likes", count)
                                        .addOnSuccessListener { it4 ->
                                            if(it4 != null){
                                                Log.d("profilepage", "post updated")
                                            }
                                            viewHolder.itemView.tvLikeCount.text = (count).toString()
                                            viewHolder.itemView.btnLikeCount.isEnabled = true
                                        }
                                }
                            }
                    }
                }
            }
    }

    override fun getLayout(): Int {
        return R.layout.profile_post_adapter
    }

}

//useless text