package com.nimitsajal.studentconnectapp

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.circularImageView
import kotlinx.android.synthetic.main.activity_profile_page.*
import kotlinx.android.synthetic.main.chat_recieve.view.*
import kotlinx.android.synthetic.main.chat_recieve.view.tvRecieve
import kotlinx.android.synthetic.main.chat_send.view.*
import kotlinx.android.synthetic.main.chat_send.view.tvSend
import kotlinx.android.synthetic.main.chat_system.view.*
import kotlinx.android.synthetic.main.current_chat_adapter.view.*
import kotlinx.android.synthetic.main.new_chat_adapter.view.*
import kotlinx.android.synthetic.main.new_chat_adapter.view.tv_usernames_newMessage
import kotlinx.android.synthetic.main.post_adapter_cardiew.view.*

class chat : AppCompatActivity() {

    private lateinit var detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        pbChat.isVisible = true

        var url = ""
        var To = intent.getStringExtra("to")
        var From = intent.getStringExtra("from")
        username.text = To
        val db = FirebaseFirestore.getInstance()
        if (To != null) {
            db.collection("Users").document(To)
                .get()
                .addOnSuccessListener {
                    if(it != null){
                        url = it.getString("Picture").toString()
                        Log.d("profilePage", "URL recieved = $url")
                        detector = GestureDetectorCompat(this,DiaryGestureListener(From))
                        loadDP(url)
                    }
                    else{
                        showToast("ERROR", 1)
                        return@addOnSuccessListener
                    }
                }
        }

        btnSendMessage_chat.setOnClickListener {
            val text = etTypeMEssage_chat.text.toString()
            if(text != null || text != ""){
                if (To != null) {
                    if (From != null) {
                        sendMessage(text, To, From)
                        etTypeMEssage_chat.text.clear()
                    }
                }

            }

        }

        detector = GestureDetectorCompat(this,DiaryGestureListener(From))

        btnBack.setOnClickListener {
            toCurrentChats(From!!)
        }

        username.setOnClickListener {
            if(To!=null)
            {
                val intent = Intent(this, others_profile_page::class.java)
                intent.putExtra("usernameOthers", To)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top)
            }
        }

        circularImageView.setOnClickListener {
            if(To!=null)
            {
                val intent = Intent(this, others_profile_page::class.java)
                intent.putExtra("usernameOthers", To)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top)
            }
        }

        if (To != null) {
            if (From != null) {
                loadChat(To, From)
            }
        }
    }

    private fun toCurrentChats(username: String){
        val intent = Intent(this, currentChats::class.java)
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
                            return this@chat.onSwipeRight(username!!)
                        } else {
                            //Left Swipe
                            //Toast.makeText(this@mainFeed, "Swipe Left", Toast.LENGTH_SHORT).show()
                            return this@chat.onSwipeLeft()
                        }
                    } else {
                        return false
                    }
                } else {
                    //Up or down Swipe
                    if (Math.abs(diffY) > SWIPE_THREASHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THREASHOLD) {
                        if (diffY > 0) {
                            //Up Swipe
                            return this@chat.onSwipeUp()
                        } else {
                            //Bottom Swipe
                            return this@chat.onSwipeBottom()

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
        return false
    }

    private fun onSwipeRight(username: String): Boolean {
        //Toast.makeText(this, "Swipe Right", Toast.LENGTH_SHORT).show()
        toCurrentChats(username)
        return true
    }

    override fun onBackPressed() {
        //super.onBackPressed()

        var username = ""
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid.toString())
            user_table.get().addOnSuccessListener { result ->
                if(result != null){
                    username = result.getString("Username").toString()
                    Log.d("profilePage", username.toString())
                    val intent = Intent(this, currentChats::class.java)
                    intent.putExtra("usernameOthers", username)
                    startActivity(intent)
                }
                else{
                    showToast("ERROR", 1)
                    return@addOnSuccessListener
                }
            }
        }
    }

    private  fun sendMessage(text: String, To: String, From: String){
        val db = FirebaseFirestore.getInstance()
        val message = hashMapOf(
            "From" to From,
            "To" to To,
            "Text" to text,
            "Time" to FieldValue.serverTimestamp()
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
            .add(message)
            .addOnFailureListener {
                showToast("Message not Sent", 1)
            }
        recieve.collection("Next")
            .add(message)
            .addOnFailureListener {
                showToast("Message not Sent", 1)
            }
    }

    private fun loadDP(url: String){
        Picasso.get().load(url).into(circularImageView, object : Callback {
            override fun onSuccess() {
                pbChat.isVisible = false
            }
            override fun onError(e: java.lang.Exception?) {
                Log.d("loading", "ERROR - $e")
            }
        })
    }

    private fun loadChat(To: String, From: String){
        val adapter = GroupAdapter<GroupieViewHolder>()
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(From).collection("Chats").document(To).collection("Next")
            .orderBy("Time", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if(value == null || error != null){
                    showToast("ERROR", 1)
                    return@addSnapshotListener
                }
                adapter.clear()
                for(document in value.documents){
                    if(document["From"] == "System"){
                        adapter.add(System(document["Text"].toString()))
                        recyclerView_chat.scrollToPosition(adapter.itemCount-1)
                    }
                    else if(document["From"] == From){
                        adapter.add(Send(document["Text"].toString()))
                        recyclerView_chat.scrollToPosition(adapter.itemCount-1)
                    }
                    else{
                        adapter.add(Recieve(document["Text"].toString()))
                        recyclerView_chat.scrollToPosition(adapter.itemCount-1)
                    }
                }
                adapter.setOnItemLongClickListener { item, view ->
                    val temp: Recieve = item as Recieve
                    val text = temp.text
                    val clipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("address", text)
                    clipboardManager.setPrimaryClip(clipData)
                    showToast("Text copied to clipboard", 3)
                    return@setOnItemLongClickListener true
                }
                recyclerView_chat.adapter = adapter
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
}

class Send(val text: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_send
    }
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvSend.text = text
        Log.d("adapter", "adapter added")
        viewHolder.itemView.tvSend.setOnClickListener(object : Send.DoubleClickListener() {
            override fun onDoubleClick(v: View?) {
                Log.d("adapter", "double press")
                //Toast.makeText(, "double", Toast.LENGTH_SHORT).show()
                //Toast.makeText(this@Send, "ERROR", Toast.LENGTH_LONG).show()
            }
        })
    }
    abstract class DoubleClickListener : View.OnClickListener {
        private val DOUBLE_CLICK_TIME_DELTA: Long = 300
        var lastClickTime: Long = 0
        override fun onClick(v: View?) {
            val clickTime = java.lang.System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                onDoubleClick(v)
            }
            lastClickTime = clickTime
        }

        open fun onDoubleClick(v: View?) {
            object {
                private val DOUBLE_CLICK_TIME_DELTA: Long = 300
            }
        }
    }
}

class Recieve(val text: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_recieve
    }
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvRecieve.text = text
        Log.d("adapter", "adapter added")
    }
}

class System(val text: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_system
    }
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvSystem.text = text
        Log.d("adapter", "adapter added")
    }
}

