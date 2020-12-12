package com.nimitsajal.studentconnectapp

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_chat.*
import kotlinx.android.synthetic.main.current_chat_adapter.view.*
import kotlinx.android.synthetic.main.new_chat_adapter.view.*
import kotlinx.android.synthetic.main.post_adapter_cardiew.view.*

class newChat : AppCompatActivity() {

    private lateinit var detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_chat)

        var arrayUser = mutableListOf<usersList>()

        val adapter = GroupAdapter<GroupieViewHolder>()

        var fromCurrentChat = intent.getStringExtra("fromCurrentChat")
        var username = intent.getStringExtra("username")
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            val user_table = db.collection("User Table").document(user.uid)
            user_table.get().addOnSuccessListener { result ->
                if (result != null) {
                    username = result.getString("Username").toString()
                    Log.d("profilePage", username.toString())
                    adapter.clear()
                    detector = GestureDetectorCompat(this, DiaryGestureListener(username))
                    fetchUsers(username!!, adapter, arrayUser, fromCurrentChat!!)
                } else {
                    showToast("ERROR", 1)
                    return@addOnSuccessListener
                }
            }
        }

        detector = GestureDetectorCompat(this, DiaryGestureListener(username))

        btnBack.setOnClickListener {
            goToChats(username!!)
        }

        btnSearch.setOnClickListener {
            if (etSearch.isVisible == true) {
                closeSearchBar()
            } else {
                openSearchBar()
            }
        }

        etSearch.addTextChangedListener() {
            if (etSearch.text.toString() == "") {
                etSearch.isVisible = false
                studentConnectNewChat.isVisible = true
            } else {
                etSearch.isVisible = true
                studentConnectNewChat.isVisible = false
            }

            if (username != null) {
                adapter.clear()
                searching(username!!, adapter, arrayUser, fromCurrentChat!!)
            }
        }
    }

    private fun showToast(message: String, type: Int) {   //1 -> error
        //2 -> success
        //3 -> information

        if (type == 1) {
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
        } else if (type == 2) {
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
        } else {
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

    private fun goToChats(username: String) {
        val intent = Intent(this, currentChats::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        finish()
    }

    private fun closeSearchBar() {
        etSearch.isVisible = false
        etSearch.setText("").toString()
        studentConnectNewChat.isVisible = true
    }

    private fun openSearchBar() {
        etSearch.isVisible = true
        studentConnectNewChat.isVisible = false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //Toast.makeText(this, "Swipe", Toast.LENGTH_SHORT).show()
        if (detector.onTouchEvent(event)) {
            return true
        } else {
            return super.onTouchEvent(event)
        }

    }

    inner class DiaryGestureListener(username: String?) :
        GestureDetector.SimpleOnGestureListener() {
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
                            return this@newChat.onSwipeRight(username!!)
                        } else {
                            //Left Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Left", Toast.LENGTH_SHORT).show()
                            return this@newChat.onSwipeLeft()
                        }
                    } else {
                        return false
                    }
                } else {
                    //Up or down Swipe
                    if (Math.abs(diffY) > SWIPE_THREASHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THREASHOLD) {
                        if (diffY > 0) {
                            //Up Swipe
                            return this@newChat.onSwipeUp()
                        } else {
                            //Bottom Swipe
                            return this@newChat.onSwipeBottom()

                        }
                    } else {
                        return false
                    }
                }

                return super.onFling(yAxisEvent, xAxisEvent, velocityX, velocityY)
            } catch (e: java.lang.Exception) {
                return false
            }
        }

    }

    private fun onSwipeUp(): Boolean {
        //Toast.makeText(this, "Swipe Up", Toast.LENGTH_SHORT).show()
        closeSearchBar()
        return false
    }

    private fun onSwipeBottom(): Boolean {
        //Toast.makeText(this, "Swipe Down", Toast.LENGTH_SHORT).show()
        openSearchBar()
        return false
    }

    private fun onSwipeLeft(): Boolean {
        //Toast.makeText(this, "Swipe Left", Toast.LENGTH_SHORT).show()

        return true
    }

    private fun onSwipeRight(username: String): Boolean {
        //Toast.makeText(this, "Swipe Right", Toast.LENGTH_SHORT).show()
        goToChats(username)
        return true
    }

    private fun already(from: String, to: String) {
        val intent = Intent(this, chat::class.java)
        intent.putExtra("from", from)
        intent.putExtra("to", to)
        startActivity(intent)
        finish()
    }

    private fun notAlready(from: String, to: String, fromName: String, toName: String) {
        val db = FirebaseFirestore.getInstance()

        val time = FieldValue.serverTimestamp()

        val message = hashMapOf(
            "From" to "System",
            "To" to to,
            "Text" to "Say Hi",
            "Time" to time,
            "ID" to ""
        )

        val messageFrom = hashMapOf(
            "From" to "System",
            "To" to to,
            "Text" to "Say Hi",
            "Name" to toName,
            "Time" to time
        )

        val messageTo = hashMapOf(
            "From" to "System",
            "To" to to,
            "Text" to "Say Hi",
            "Name" to fromName,
            "Time" to time
        )

        val info = hashMapOf(
            "Info" to "Info"
        )

        db.collection("Users").document(from).collection("Chats").document(to)
            .set(messageFrom)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    db.collection("Users").document(from).collection("Chats").document(to)
                        .collection("Next")
                        .add(message)
                        .addOnCompleteListener { it1 ->
                            if (it1.isSuccessful) {
                                Log.d("adapter", "chat created in user")
                            }
                        }
                }
            }
        db.collection("Users").document(to).collection("Chats").document(from)
            .set(messageTo)
            .addOnCompleteListener { it2 ->
                if (it2.isSuccessful) {
                    db.collection("Users").document(to).collection("Chats").document(from)
                        .collection("Next")
                        .add(message)
                        .addOnCompleteListener { it3 ->
                            if (it3.isSuccessful) {
                                val intent = Intent(this, chat::class.java)
                                intent.putExtra("from", from)
                                intent.putExtra("to", to)
                                startActivity(intent)
                                finish()
                            }
                        }
                }

            }

    }

    private fun notAlready(
        from: String,
        to: String,
        fromName: String,
        toName: String,
        text: String
    ) {
        val db = FirebaseFirestore.getInstance()

        val time = FieldValue.serverTimestamp()

        val message = hashMapOf(
            "From" to "System",
            "To" to to,
            "Text" to "Say Hi",
            "Time" to time,
            "ID" to ""
        )

        val messageFrom = hashMapOf(
            "From" to "System",
            "To" to to,
            "Text" to "Say Hi",
            "Name" to toName,
            "Time" to time
        )

        val messageTo = hashMapOf(
            "From" to "System",
            "To" to to,
            "Text" to "Say Hi",
            "Name" to fromName,
            "Time" to time
        )

        val info = hashMapOf(
            "Info" to "Info"
        )

        db.collection("Users").document(from).collection("Chats").document(to)
            .set(messageFrom)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    db.collection("Users").document(from).collection("Chats").document(to)
                        .collection("Next")
                        .add(message)
                        .addOnCompleteListener { it1 ->
                            if (it1.isSuccessful) {
                                Log.d("adapter", "chat created in user")
                            }
                        }
                }
            }
        db.collection("Users").document(to).collection("Chats").document(from)
            .set(messageTo)
            .addOnCompleteListener { it2 ->
                if (it2.isSuccessful) {
                    db.collection("Users").document(to).collection("Chats").document(from)
                        .collection("Next")
                        .add(message)
                        .addOnCompleteListener { it3 ->
                            if (it3.isSuccessful) {
                                sendMessage(text, to, from)
                                onBackPressed()
                            }
                        }
                }

            }

    }

    @SuppressLint("RestrictedApi")
    private fun fetchUsers(
        username: String,
        adapter: GroupAdapter<GroupieViewHolder>,
        arrayUser: MutableList<usersList>,
        fromCurrentChat: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val user = db.collection("Users").document(username).collection("Friends")
        user.addSnapshotListener { value, error ->
            if (error != null || value == null) {
                showToast("ERROR", 1)
                return@addSnapshotListener
            }

            for (document in value.documents) {
                if (document.id != "Info") {
                    Log.d("adapter", "Document id = ${document.id}")
                    //Toast.makeText(this, "Document id = ${document.id}", Toast.LENGTH_SHORT).show()
                    adapter.add(
                        UserItem(
                            document.id,
                            document["Picture"].toString(),
                            document["Name"].toString(),
                            arrayUser
                        )
                    )
                    Log.d("adapter", "Done")
                    //Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
                }
            }
            adapter.setOnItemClickListener { item, view ->
                val userItem: UserItem = item as UserItem
                //val intent = Intent(view.context, ChatActivity:class.java)
//                Toast.makeText(this, "Username = ${userItem.username}, Link = ${userItem.link}", Toast.LENGTH_SHORT).show()
                db.collection("Users").document(username).collection("Chats")
                    .document(userItem.username)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val temp = it.getResult()
                            if (temp != null) {
                                if (temp.exists()) {
                                    if (fromCurrentChat == "true") {
                                        showToast("Continue Your Chat With ${userItem.username}", 3)
                                        already(username, userItem.username)
                                        return@addOnCompleteListener
                                    } else {
                                        sendMessage(fromCurrentChat, userItem.username, username)
                                        onBackPressed()
                                    }
                                } else {
                                    showToast("New Chat With ${userItem.username} Created", 2)
                                    var fromName = ""
                                    var toName = ""
                                    db.collection("Users").document(username)
                                        .get()
                                        .addOnSuccessListener {
                                            if (it != null) {
                                                fromName = it.getString("Name").toString()
                                                db.collection("Users").document(userItem.username)
                                                    .get()
                                                    .addOnSuccessListener { it2 ->
                                                        if (it2 != null) {
                                                            toName =
                                                                it2.getString("Name").toString()
                                                            if (fromCurrentChat == "true") {
                                                                notAlready(
                                                                    username,
                                                                    userItem.username,
                                                                    fromName,
                                                                    toName
                                                                )
                                                                return@addOnSuccessListener
                                                            } else {
                                                                notAlready(
                                                                    username,
                                                                    userItem.username,
                                                                    fromName,
                                                                    toName,
                                                                    fromCurrentChat
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
            rvNewChats.adapter = adapter
        }
    }

    private fun searching(
        username: String,
        adapter: GroupAdapter<GroupieViewHolder>,
        arrayUser: MutableList<usersList>,
        fromCurrentChat: String
    ) {
        val search = etSearch.text.toString()
        val pattern = search.toRegex(RegexOption.IGNORE_CASE)
        if (search == "" || search == null) {
            for (document in arrayUser) {
                adapter.add(UserItemSearch(document.username, document.url, document.name))
            }
        } else {
            for (document in arrayUser) {
                if (pattern.containsMatchIn(document.username) || pattern.containsMatchIn(document.name)) {
                    adapter.add(UserItemSearch(document.username, document.url, document.name))
                }
            }
        }
        adapter.setOnItemClickListener { item, view ->
            val userItemSearch: UserItemSearch = item as UserItemSearch
            val db = FirebaseFirestore.getInstance()
            //val intent = Intent(view.context, ChatActivity:class.java)
//                Toast.makeText(this, "Username = ${userItem.username}, Link = ${userItem.link}", Toast.LENGTH_SHORT).show()
            db.collection("Users").document(username).collection("Chats")
                .document(userItemSearch.username)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val temp = it.getResult()
                        if (temp != null) {
                            if (temp.exists()) {
                                if (fromCurrentChat == "true") {
                                    showToast("Continue Your Chat With ${username}", 3)
                                    already(username, userItemSearch.username)
                                    return@addOnCompleteListener
                                } else {
                                    sendMessage(fromCurrentChat, userItemSearch.username, username)
                                    onBackPressed()
                                }
                            } else {
                                showToast("New Chat With ${username} Created", 2)
                                var fromName = ""
                                var toName = ""
                                db.collection("Users").document(username)
                                    .get()
                                    .addOnSuccessListener {
                                        if (it != null) {
                                            fromName = it.getString("Name").toString()
                                            db.collection("Users").document(userItemSearch.username)
                                                .get()
                                                .addOnSuccessListener { it2 ->
                                                    if (it2 != null) {
                                                        toName = it2.getString("Name").toString()
                                                        if (fromCurrentChat == "true") {
                                                            notAlready(
                                                                username,
                                                                userItemSearch.username,
                                                                fromName,
                                                                toName
                                                            )
                                                            return@addOnSuccessListener
                                                        } else {
                                                            notAlready(
                                                                username,
                                                                userItemSearch.username,
                                                                fromName,
                                                                toName,
                                                                fromCurrentChat
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
            rvNewChats.adapter = adapter
        }
    }

    private  fun sendMessage(text: String, To: String, From: String){
        val db = FirebaseFirestore.getInstance()

        val time = FieldValue.serverTimestamp()
        val message = hashMapOf(
            "From" to From,
            "To" to To,
            "Text" to text,
            "Time" to time
        )
        val messageCreate = hashMapOf(
            "From" to From,
            "To" to To,
            "Text" to text,
            "Time" to time,
            "Liked" to false,
            "IsReply" to false,
            "ReplyTo" to ""
        )
        val send = db.collection("Users").document(From).collection("Chats").document(To)
        val recieve = db.collection("Users").document(To).collection("Chats").document(From)
        send.update(message)
            .addOnFailureListener {
                showToast("Message not Updated", 1)
            }
        recieve.update(message)
            .addOnFailureListener {
                showToast("Message not Updated", 1)
            }

        send.collection("Next")
            .add(messageCreate)
            .addOnSuccessListener {
                recieve.collection("Next").document(it.id)
                    .set(messageCreate)
                    .addOnFailureListener {
                        showToast("Message not Sent", 1)
                    }
            }
            .addOnFailureListener {
                showToast("Message not Sent", 1)
            }
    }
}

class UserItem(val username: String,val url: String, val Name: String, val arrayUser: MutableList<usersList>): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.new_chat_adapter
    }

    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.pbNewChat.isVisible = true
        val temp = usersList(username, "", Name, url)
        arrayUser.add(temp)
        viewHolder.itemView.tv_usernames_newMessage.text = username
        Picasso.get().load(url).into(viewHolder.itemView.cv_dp_newMessage, object : Callback {
            override fun onSuccess() {
                viewHolder.itemView.pbNewChat.isVisible = false
            }
            override fun onError(e: java.lang.Exception?) {
                Log.d("loading", "ERROR - $e")
            }
        })
        Log.d("adapter", "adapter added")
    }
}

class UserItemSearch(val username: String,val url: String, val Name: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.new_chat_adapter
    }

    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.pbNewChat.isVisible = true
        viewHolder.itemView.tv_usernames_newMessage.text = username
        Picasso.get().load(url).into(viewHolder.itemView.cv_dp_newMessage, object : Callback {
            override fun onSuccess() {
                viewHolder.itemView.pbNewChat.isVisible = false
            }
            override fun onError(e: java.lang.Exception?) {
                Log.d("loading", "ERROR - $e")
            }
        })
        Log.d("adapter", "adapter added")
    }
}
