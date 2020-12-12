package com.nimitsajal.studentconnectapp

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
import kotlinx.android.synthetic.main.chat_options.view.*
import kotlinx.android.synthetic.main.chat_recieve_replied.view.*
import kotlinx.android.synthetic.main.chat_send_replied.view.*

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
                        if (To != null) {
                            if (From != null) {
                                loadChat(To, From, From)
                            }
                        }
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
        val time = FieldValue.serverTimestamp()

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

        send.collection("Next")
            .add(messageCreate)
            .addOnSuccessListener {
                recieve.collection("Next").document(it.id)
                    .set(messageCreate)
                    .addOnFailureListener {
                        showToast("Message not Sent", 1)
                    }
                val message = hashMapOf(
                    "From" to From,
                    "To" to To,
                    "Text" to text,
                    "Time" to time,
                    "ID" to it.id.toString()
                )
                send.update(message)
                    .addOnFailureListener {
                        showToast("Message not Updated", 1)
                    }
                recieve.update(message)
                    .addOnFailureListener {
                        showToast("Message not Updated", 1)
                    }
            }
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

    private fun loadChat(To: String, From: String, username: String){
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
                        adapter.add(Send(document["Text"].toString(),document.id, From,To,document["Liked"].toString(),username,document["IsReply"].toString(), document["ReplyTo"].toString()))
                        recyclerView_chat.scrollToPosition(adapter.itemCount-1)
                    }
                    else{
                        adapter.add(Recieve(document["Text"].toString(),document.id, From,To,document["Liked"].toString(),username,document["IsReply"].toString(), document["ReplyTo"].toString()))
                        recyclerView_chat.scrollToPosition(adapter.itemCount-1)
                    }
                }
//                adapter.setOnItemLongClickListener { item, view ->
//                    val temp: Recieve = item as Recieve
//                    val text = temp.text
//                    val clipboardManager =
//                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                    val clipData = ClipData.newPlainText("address", text)
//                    clipboardManager.setPrimaryClip(clipData)
//                    showToast("Text copied to clipboard", 3)
//                    return@setOnItemLongClickListener true
//                }
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

class Send(val text: String, val id: String, val From: String, val To: String, val liked: String,val username: String, val isReply: String, val replyText: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        if(isReply=="true" && liked=="true")
        {
            return R.layout.chat_send_replied_like
        }
        else if(isReply!="true" && liked=="true")
        {
            return R.layout.chat_send_like
        }
        else if(isReply=="true" && liked!="true")
        {
            return R.layout.chat_send_replied
        }
        else
        {
            return R.layout.chat_send
        }
    }
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvSend.text = text
        Log.d("adapter", "adapter added")
        if(isReply=="true")
        {
            viewHolder.itemView.tvSendReply.text = replyText
        }

        viewHolder.itemView.tvSend.setOnLongClickListener {
            inflate(viewHolder)
            return@setOnLongClickListener true
        }

        if(isReply == "true"){
            viewHolder.itemView.tvSendReply.setOnLongClickListener {
                inflate(viewHolder)
                return@setOnLongClickListener true
            }
        }

        viewHolder.itemView.tvSend.setOnClickListener(object : Send.DoubleClickListener() {
            override fun onDoubleClick(v: View?) {
                Log.d("adapter", "double press")
                Reply(viewHolder)
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

    private fun inflate(viewHolder: GroupieViewHolder)
    {
        val context = viewHolder.itemView.context
        val dialog = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.chat_options,null,false)
        val btnOne = dialogView.findViewById<ImageView>(R.id.forward)
        val btnTwo = dialogView.findViewById<ImageView>(R.id.delete)
        val btnThree = dialogView.findViewById<ImageView>(R.id.copy)
        btnTwo.isVisible = true
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        val customDialogue = dialog.create()
        customDialogue.show()
        btnOne.setOnClickListener {
            customDialogue.dismiss()
            btnTwo.isVisible = false
            forward(viewHolder)
        }
        btnTwo.setOnClickListener {
            customDialogue.dismiss()
            btnTwo.isVisible = false
            delete()
        }
        btnThree.setOnClickListener {
            customDialogue.dismiss()
            btnTwo.isVisible = false
            val clipboardManager = viewHolder.itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("address", text)
            clipboardManager.setPrimaryClip(clipData)
            Log.d("chat", "Lower adapter listner over")
            Toast.makeText(viewHolder.itemView.context, "Copied", Toast.LENGTH_SHORT).show()
        }

    }

    private fun Reply(viewHolder: GroupieViewHolder)
    {
        val context = viewHolder.itemView.context
        val dialog = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.reply_dialog,null,false)
        val etReply = dialogView.findViewById<EditText>(R.id.etReply)
        val tvReply = dialogView.findViewById<TextView>(R.id.tvReply)
        var replyText = text
        var length = replyText.length
        Log.d("chatReply", length.toString())
        if(length > 160){
            replyText = replyText.dropLast(length - 157)
            replyText = replyText + "..."
            Log.d("chatReply", replyText.length.toString())
        }
        tvReply.setText(replyText)
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        //dialog.setPositiveButton("Reply",{dialogInterface: DialogInterface, i: Int ->})
        val customDialogue = dialog.create()
        val btn = dialogView.findViewById<Button>(R.id.btnReply)
        customDialogue.show()
        btn.setOnClickListener {
            if(etReply.text.toString() != ""){
                Log.d("chat", etReply.text.toString())
                customDialogue.dismiss()
                sendMessage(etReply.text.toString(),To,From,replyText)
                etReply.setText("")
            }
            else{
                Log.d("chat", "No reply sent")
                customDialogue.dismiss()
                etReply.setText("")
            }
        }
    }

    private  fun sendMessage(text: String, To: String, From: String, replyTo: String){
        val db = FirebaseFirestore.getInstance()
        //TODO: changes
        val time = FieldValue.serverTimestamp()
        var string = replyTo


        val messageCreate = hashMapOf(
            "From" to From,
            "To" to To,
            "Text" to text,
            "Time" to time,
            "Liked" to false,
            "IsReply" to true,
            "ReplyTo" to string
        )
        val send = db.collection("Users").document(From).collection("Chats").document(To)
        val recieve = db.collection("Users").document(To).collection("Chats").document(From)


        send.collection("Next")
            .add(messageCreate)
            .addOnSuccessListener {
                recieve.collection("Next").document(it.id)
                    .set(messageCreate)
                    .addOnFailureListener {
                        Log.d("chat", "Message not Sent")

                    }
                val message = hashMapOf(
                    "From" to From,
                    "To" to To,
                    "Text" to text,
                    "Time" to time,
                    "ID" to it.id.toString()
                )
                send.update(message)
                    .addOnFailureListener {
                        Log.d("chat", "Message not Updated")
                    }
                recieve.update(message)
                    .addOnFailureListener {
                        Log.d("chat", "Message not Updated")
                    }
            }
            .addOnFailureListener {
                Log.d("chat", "Message not Sent")
            }
    }

    private fun forward(viewHolder: GroupieViewHolder)
    {
        val context = viewHolder.itemView.context
        val intent = Intent(viewHolder.itemView.context, newChat::class.java)
        intent.putExtra("username", username)
        intent.putExtra("fromCurrentChat", text)
        context.startActivity(intent)
//        startActivity(intent)
//        startActivity(intent)
        //overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

    private fun delete()
    {
        val db = FirebaseFirestore.getInstance()


        db.collection("Users").document(From).collection("Chats").document(To).collection("Next")
            .document(id)
            .update("From", "System")
            .addOnSuccessListener {
                Log.d("chat", "System type made in $From")
            }
        db.collection("Users").document(From).collection("Chats").document(To).collection("Next")
            .document(id)
            .update("Text", "This message was deleted")
            .addOnSuccessListener {
                Log.d("chat", "Message updated in $From")
            }
        db.collection("Users").document(To).collection("Chats").document(From).collection("Next")
            .document(id)
            .update("From", "System")
            .addOnSuccessListener {
                Log.d("chat", "System type made in $To")
            }
        db.collection("Users").document(To).collection("Chats").document(From).collection("Next")
            .document(id)
            .update("Text", "This message was deleted")
            .addOnSuccessListener {
                Log.d("chat", "Message updated in $To")
            }
        db.collection("Users").document(From).collection("Chats").document(To)
            .get()
            .addOnSuccessListener {
                if(it!=null)
                {
                    if(it["ID"] == id.toString())
                    {
                        db.collection("Users").document(From).collection("Chats").document(To)
                            .update("Text","This message was deleted")
                            .addOnSuccessListener {
                                Log.d("chat", "Lates message updated $From")
                            }
                        db.collection("Users").document(To).collection("Chats").document(From)
                            .update("Text","This message was deleted")
                            .addOnSuccessListener {
                                Log.d("chat", "Lates message updated $From")
                            }
                    }
                }
            }
    }

}

class Recieve(val text: String, val id: String, val From: String, val To: String, val liked: String,val username: String,val isReply: String, val replyText: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        if(isReply=="true" && liked=="true")
        {
            return R.layout.chat_recieve_replied_like
        }
        else if(isReply!="true" && liked=="true")
        {
            return R.layout.chat_recieve_like
        }
        else if(isReply=="true" && liked!="true")
        {
            return R.layout.chat_recieve_replied
        }
        else
        {
            return R.layout.chat_recieve
        }
    }
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvRecieve.text = text
        Log.d("adapter", "adapter added")
        if(isReply == "true"){
            viewHolder.itemView.tvRecieveReply.text = replyText
        }

        viewHolder.itemView.tvRecieve.setOnLongClickListener {
            inflate(viewHolder)
            return@setOnLongClickListener true
        }

        if(isReply == "true"){
            viewHolder.itemView.tvRecieveReply.setOnLongClickListener {
                inflate(viewHolder)
                return@setOnLongClickListener true
            }
        }

        viewHolder.itemView.tvRecieve.setOnClickListener(object : Recieve.DoubleClickListener() {
            override fun onDoubleClick(v: View?) {
                Reply(viewHolder)
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

    private fun inflate(viewHolder: GroupieViewHolder) {
        val context = viewHolder.itemView.context
        val dialog = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.chat_options, null, false)
        val btnOne = dialogView.findViewById<ImageView>(R.id.forward)
        var btnTwo: ImageView? = null
        val btnThree = dialogView.findViewById<ImageView>(R.id.copy)

        if (liked == "true") {
            btnTwo = dialogView.findViewById<ImageView>(R.id.like)
            btnTwo.isVisible = true
        } else {
            btnTwo = dialogView.findViewById<ImageView>(R.id.dislike)
            btnTwo.isVisible = true
        }

        dialog.setView(dialogView)
        dialog.setCancelable(true)
        val customDialogue = dialog.create()
        customDialogue.show()
        btnOne.setOnClickListener {
            btnTwo.isVisible = false
            customDialogue.dismiss()
            forward(viewHolder)
        }
        btnTwo.setOnClickListener {
            btnTwo.isVisible = false
            customDialogue.dismiss()
            liking()
        }
        btnThree.setOnClickListener {
            btnTwo.isVisible = false
            customDialogue.dismiss()

            val clipboardManager = viewHolder.itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("address", text)
            clipboardManager.setPrimaryClip(clipData)
            Log.d("chat", "Lower adapter listner over")
            Toast.makeText(viewHolder.itemView.context, "Copied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun liking()
    {
        val db = FirebaseFirestore.getInstance()
        if(liked=="true")
        {
            db.collection("Users").document(From).collection("Chats").document(To).collection("Next")
                .document(id)
                .update("Liked", false)
                .addOnSuccessListener {
                    Log.d("chat", "Unliked in $From")
                }
            db.collection("Users").document(To).collection("Chats").document(From).collection("Next")
                .document(id)
                .update("Liked", false)
                .addOnSuccessListener {
                    Log.d("chat", "Unliked in $To")
                }
        }
        else
        {
            db.collection("Users").document(From).collection("Chats").document(To).collection("Next")
                .document(id)
                .update("Liked", true)
                .addOnSuccessListener {
                    Log.d("chat", "Liked in $From")
                }
            db.collection("Users").document(To).collection("Chats").document(From).collection("Next")
                .document(id)
                .update("Liked", true)
                .addOnSuccessListener {
                    Log.d("chat", "Liked in $To")
                }
        }
    }

    private fun forward(viewHolder: GroupieViewHolder)
    {
        val context = viewHolder.itemView.context
        val intent = Intent(viewHolder.itemView.context, newChat::class.java)
        intent.putExtra("username", username)

        intent.putExtra("fromCurrentChat", text)
        context.startActivity(intent)
//        startActivity(intent)
//        startActivity(intent)
        //overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

    private fun Reply(viewHolder: GroupieViewHolder)
    {
        val context = viewHolder.itemView.context
        val dialog = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.reply_dialog,null,false)
        val etReply = dialogView.findViewById<EditText>(R.id.etReply)
        val tvReply = dialogView.findViewById<TextView>(R.id.tvReply)
        var replyText = text
        var length = replyText.length
        if(length > 160){
            replyText = replyText.dropLast(length - 157)
            replyText = replyText + "..."
        }
        tvReply.setText(replyText)
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        //dialog.setPositiveButton("Reply",{dialogInterface: DialogInterface, i: Int ->})
        val customDialogue = dialog.create()
        val btn = dialogView.findViewById<Button>(R.id.btnReply)
        customDialogue.show()
        btn.setOnClickListener {
            if(etReply.text.toString() != ""){
                Log.d("chatReply", etReply.text.toString())
                customDialogue.dismiss()
                sendMessage(etReply.text.toString(),To,From,replyText)
                etReply.setText("")
            }
            else{
                Log.d("chatReply", "No reply sent")
                customDialogue.dismiss()
                etReply.setText("")
            }
        }
    }

    private  fun sendMessage(text: String, To: String, From: String, replyTo: String){
        val db = FirebaseFirestore.getInstance()
        //TODO: changes
        val time = FieldValue.serverTimestamp()
        var string = replyTo

        val messageCreate = hashMapOf(
            "From" to From,
            "To" to To,
            "Text" to text,
            "Time" to time,
            "Liked" to false,
            "IsReply" to true,
            "ReplyTo" to string
        )
        val send = db.collection("Users").document(From).collection("Chats").document(To)
        val recieve = db.collection("Users").document(To).collection("Chats").document(From)


        send.collection("Next")
            .add(messageCreate)
            .addOnSuccessListener {
                recieve.collection("Next").document(it.id)
                    .set(messageCreate)
                    .addOnFailureListener {
                        Log.d("chat", "Message not Sent")

                    }
                val message = hashMapOf(
                    "From" to From,
                    "To" to To,
                    "Text" to text,
                    "Time" to time,
                    "ID" to it.id.toString()
                )
                send.update(message)
                    .addOnFailureListener {
                        Log.d("chat", "Message not Updated")
                    }
                recieve.update(message)
                    .addOnFailureListener {
                        Log.d("chat", "Message not Updated")
                    }
            }
            .addOnFailureListener {
                Log.d("chat", "Message not Sent")
            }
    }

}

class System(val text: String): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_system
    }
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvSystem.text = text
        if(text=="This message was deleted")
        {
            viewHolder.itemView.tvSystem.setTextSize(15.0F)
            viewHolder.itemView.tvSystem.setPadding(19,6,19,6)
            val param = viewHolder.itemView.tvSystem.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(0,0,0,0)
            viewHolder.itemView.tvSystem.layoutParams = param
            //viewHolder.itemView.tvSystem.setTypeface(null, Typeface.BOLD_ITALIC)
        }
        Log.d("adapter", "adapter added")
    }
}

