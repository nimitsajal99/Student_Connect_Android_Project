package com.nimitsajal.studentconnectapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_current_chats.*
import kotlinx.android.synthetic.main.activity_current_chats.etSearch
import kotlinx.android.synthetic.main.activity_main_feed.btnFeed
import kotlinx.android.synthetic.main.activity_main_feed.btnLogout
import kotlinx.android.synthetic.main.activity_main_feed.btnProfile
import kotlinx.android.synthetic.main.activity_new_chat.*
import kotlinx.android.synthetic.main.current_chat_adapter.view.*
import kotlinx.android.synthetic.main.new_chat_adapter.*

class currentChats : AppCompatActivity() {
    private lateinit var detector: GestureDetectorCompat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_chats)
//        var username = ""

        val adapter = GroupAdapter<GroupieViewHolder>()


        //TODO: GRID VIEW
//        val mLayoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
//        rvCurrentChats.layoutManager = mLayoutManager

        var arrayUser = mutableListOf<usersList>()

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
                    adapter.clear()
                    fetchUser(username!!, adapter, arrayUser)
                }
                else{
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
            }
        }

        detector = GestureDetectorCompat(this,DiaryGestureListener(username))
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

//        btnChat.setOnClickListener {
//            val intent = Intent(this, currentChats::class.java)
//            intent.putExtra("username", username)
//            startActivity(intent)
//        }

        btnProfile.setOnClickListener {
            goToProfile(username!!)
        }

        btnFeed.setOnClickListener {
            val intent = Intent(this, mainFeed::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
        }

        btnNewChat.setOnClickListener {
            goToNewChats(username!!)
        }

        etSearch.addTextChangedListener(){
            if(etSearch.text.toString() == "")
            {
                etSearch.isVisible=false

                studentConnectCurrentChat.isVisible = true
            }
            else{
                etSearch.isVisible = true

                studentConnectCurrentChat.isVisible = false
            }

            if(username != null){
                adapter.clear()
                searching(arrayUser, adapter, username!!)
            }
        }

        btnSearch_currentChat.setOnClickListener {
            if(etSearch.isVisible==true){
               closeSearchBar()
            }
            else
            {
                openSearchBar()
            }
        }

        btnEvent.setOnClickListener {
            val intent = Intent(this, eventPage::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
        }
    }

    private fun goToProfile(username: String)
    {
        val intent = Intent(this, profilePage::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
        finish()
    }

    private fun openSearchBar()
    {
        etSearch.isVisible=true
        studentConnectCurrentChat.isVisible = false
    }

    private fun closeSearchBar()
    {
        etSearch.isVisible=false
        etSearch.setText("").toString()
        studentConnectCurrentChat.isVisible = true
    }

    private fun goToNewChats(username: String)
    {
        val intent = Intent(this, newChat::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
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
                            return this@currentChats.onSwipeRight(username!!)
                        } else {
                            //Left Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Left", Toast.LENGTH_SHORT).show()
                            return this@currentChats.onSwipeLeft(username!!)
                        }
                    } else {
                        return false
                    }
                } else {
                    //Up or down Swipe
                    if (Math.abs(diffY) > SWIPE_THREASHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THREASHOLD) {
                        if (diffY > 0) {
                            //Up Swipe
                            return this@currentChats.onSwipeUp()
                        } else {
                            //Bottom Swipe
                            return this@currentChats.onSwipeBottom()

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
        closeSearchBar()
        return false
    }

    private fun onSwipeBottom(): Boolean {
        Toast.makeText(this, "Swipe Down", Toast.LENGTH_SHORT).show()
        openSearchBar()
        return false
    }

    private fun onSwipeLeft(username: String): Boolean {
        Toast.makeText(this, "Swipe Left", Toast.LENGTH_SHORT).show()
        goToNewChats(username)
        return false
    }

    private fun onSwipeRight(username: String): Boolean {
        Toast.makeText(this, "Swipe Right", Toast.LENGTH_SHORT).show()
        goToProfile(username)
        return false
    }

    @SuppressLint("RestrictedApi")
    private fun fetchUser(
        username: String,
        adapter: GroupAdapter<GroupieViewHolder>,
        arrayUser: MutableList<usersList>
    ){
        val db = FirebaseFirestore.getInstance()
        val user = db.collection("Users").document(username).collection("Chats")
        user.addSnapshotListener { value, error ->
            if(error != null || value == null){
                Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            for(document in value.documents){
                if(document.id != "Info"){
                        Log.d("adapter", "Document id = ${document.id}")
                        //Toast.makeText(this, "Document id = ${document.id}", Toast.LENGTH_SHORT).show()
                        adapter.add(CurrentChat_class(document.id, document["Text"].toString(), document["Name"].toString(), arrayUser))
                        Log.d("adapter", "Done")
                        //Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
                }
            }
            adapter.setOnItemClickListener { item, view ->
                val currentChat_class: CurrentChat_class = item as CurrentChat_class
                val to = currentChat_class.username
                val intent = Intent(this, chat::class.java)
                intent.putExtra("from", username)
                intent.putExtra("to", to)
                startActivity(intent)
                finish()
            }
            rvCurrentChats.adapter = adapter
        }
    }

    private fun searching(arrayUser: MutableList<usersList>, adapter: GroupAdapter<GroupieViewHolder>, username: String){
        val search = etSearch.text.toString()
        val pattern = search.toRegex(RegexOption.IGNORE_CASE)
        if(search == "" || search == null){
            for(document in arrayUser){
                adapter.add(CurrentChatSearch_class(document.username, document.text, document.name, document.url))
            }
        }
        else{
            for(document in arrayUser){
                if(pattern.containsMatchIn(document.name) || pattern.containsMatchIn(document.username)){
                    adapter.add(CurrentChatSearch_class(document.username, document.text, document.name, document.url))
                }
            }
        }
        adapter.setOnItemClickListener { item, view ->
            val currentChatSearch_class: CurrentChatSearch_class = item as CurrentChatSearch_class
            val to = currentChatSearch_class.username
            val intent = Intent(this, chat::class.java)
            intent.putExtra("from", username)
            intent.putExtra("to", to)
            startActivity(intent)
            finish()
        }
        rvCurrentChats.adapter = adapter
    }
}

data class usersList(var username: String, var text: String, var name: String, var url: String){
}

class CurrentChat_class(val username: String, val text: String, val Name: String, val arrayUser: MutableList<usersList>): Item<GroupieViewHolder>(){
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val db = FirebaseFirestore.getInstance()
        var url = ""
        db.collection("Users").document(username)
            .get()
            .addOnSuccessListener {
                url = it.getString("Picture").toString()
                val temp = usersList(username, text, Name, url)
                arrayUser.add(temp)
                viewHolder.itemView.tv_usernames_latestMessage.text = username
                viewHolder.itemView.tv_text_latestMessage.text = text
                Picasso.get().load(url).into(viewHolder.itemView.cv_dp_currentMessage)
                Log.d("adapter", "adapter added")

                //TODO: Tap on specific item in recyclerview
//                viewHolder.itemView.tv_usernames_latestMessage.setOnClickListener {
//                    Log.d("clicked", "username clicked")
//                }
//                viewHolder.itemView.cv_dp_currentMessage.setOnClickListener {
//                    Log.d("clicked", "dp clicked")
//                }
            }
    }

    override fun getLayout(): Int {
        return R.layout.current_chat_adapter
    }
}

class CurrentChatSearch_class(val username: String, val text: String, val Name: String, val url: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tv_usernames_latestMessage.text = username
        viewHolder.itemView.tv_text_latestMessage.text = text
        Picasso.get().load(url).into(viewHolder.itemView.cv_dp_currentMessage)
        Log.d("adapter", "adapter added")
    }

    override fun getLayout(): Int {
        return R.layout.current_chat_adapter
    }

}
