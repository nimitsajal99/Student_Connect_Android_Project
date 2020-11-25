package com.nimitsajal.studentconnectapp

import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.palette.graphics.Palette
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.auth.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.branchnames_adapter.view.*
import kotlinx.android.synthetic.main.new_chat_adapter.view.*
import kotlinx.android.synthetic.main.post_adapter_cardiew.view.*


class mainFeed : AppCompatActivity() {
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
                    loadFeed(arrayPost, adapter, username!!, db)
                }
                else{
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
            }
        }

//        btnEvent.setOnClickListener {
//            val layout = layoutInflater.inflate(R.layout.toast_login_adapter, toast_text)
//            tvToast.text = "This is a Sample Toast"
//            Toast(this).apply {
//                duration = Toast.LENGTH_SHORT
//                setGravity(Gravity.CENTER, 0, 0)
//                view = layout
//            }.show()
//        }

//        btnEvent.setOnClickListener {
//            val intent = Intent(this, mapCollegeProfile::class.java)
//            intent.putExtra("username", username)
//            intent.putExtra("collegeName", " BMSCE - BMS College of Engineering")
//            intent.putExtra("universityName", "BMS University")
//            startActivity(intent)
//        }

        btnUpload.setOnClickListener{
            val intent = Intent(this, upload_post::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
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
                etSearchMainFeed.isVisible=false
                etSearchMainFeed.setText("").toString()
                adapter.clear()
                loadFeed(arrayPost, adapter, username!!, db)
                logoMainFeed.isVisible = true
            }
            else
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
                            Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
                            return@addSnapshotListener
                        }
                        for(document in value.documents)
                        {
                            if(document.id != "Next")
                            {
                                db.collection("University").document("Next").collection(document.id)
                                    .addSnapshotListener { value2, error2 ->
                                        if(value2 == null || error2 != null){
                                            Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
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

    private fun loadFeed(arrayPost: MutableList<postList>, adapter: GroupAdapter<GroupieViewHolder>, username: String, db: FirebaseFirestore)
    {

        adapter.clear()
        val user = db.collection("Users").document(username).collection("My Feed")
        user
            .orderBy("Time", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if(value == null || error != null){
                    Toast.makeText(this, "ERRRRRRRRROR", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                for(document in value.documents){
                    if(document.id != "Info"){
                        db.collection("Post").document(document.id)
                            .get()
                            .addOnSuccessListener {
                                val temp = postList(it["From"].toString(), it["Picture"].toString(), it["Dp"].toString(), it["Description"].toString())
                                //TODO: temp added
                                arrayPost.add(temp)
                                adapter.add(post_class(it["From"].toString(), it["Picture"].toString(), it["Dp"].toString(), it["Description"].toString()))
                            }
                    }
                }
                rvFeed.adapter = adapter
            }
    }
}

data class postList(
    var username: String,
    var imageUrl: String,
    var dpUrl: String,
    var description: String
){
}

class post_class(
    var username: String,
    var imageUrl: String,
    var dpUrl: String,
    var description: String
): Item<GroupieViewHolder>(){
    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvUsernameCard.text = username
        viewHolder.itemView.tvDescriptionCard.text = description
        Picasso.get().load(dpUrl).into(viewHolder.itemView.circularImageViewCard)
        Picasso.get().load(imageUrl).into(viewHolder.itemView.postImageCard)



//        Picasso.get().load(imageUrl).into(object : com.squareup.picasso.Target {
//            @RequiresApi(Build.VERSION_CODES.O)
//            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//                // loaded bitmap is here (bitmap)
//                Log.d("colorset", "bitmap loaded")
//
//                if (bitmap != null) {
//                    Palette.Builder(bitmap).generate { it?.let {  palette ->
//                        val vibrant: Int = palette.getVibrantColor(0x000000) // <=== color you want
//                        val vibrantLight: Int = palette.getLightVibrantColor(0x000000)
//                        val vibrantDark: Int = palette.getDarkVibrantColor(0x000000)
//                        val muted: Int = palette.getMutedColor(0x000000)
//                        val mutedLight: Int = palette.getLightMutedColor(0x000000)
//                        val mutedDark: Int = palette.getDarkMutedColor(0x000000)
//                        val dominant: Int = palette.getDominantColor(0x000000)
//
//                        viewHolder.itemView.cvBehindImage.setCardBackgroundColor(muted)
//                        Log.d("colorset", "color set $muted")
//
//                    } }
//                }
//
////                Palette.from(android.R.attr.bitmap).generate { palette ->
////                    val vibrant: Int = palette.getVibrantColor(0x000000) // <=== color you want
////                    val vibrantLight: Int = palette.getLightVibrantColor(0x000000)
////                    val vibrantDark: Int = palette.getDarkVibrantColor(0x000000)
////                    val muted: Int = palette.getMutedColor(0x000000)
////                    val mutedLight: Int = palette.getLightMutedColor(0x000000)
////                    val mutedDark: Int = palette.getDarkMutedColor(0x000000)
////                }
////
////                var newBitmap: Bitmap? = bitmap?.let { Bitmap.createScaledBitmap(it, 1, 1, true) };
////                var color = newBitmap?.getPixel(0, 0);
////                newBitmap?.recycle();
////                if (color != null) {
////                    viewHolder.itemView.cvBehindImage.setCardBackgroundColor(color)
////                    Log.d("colorset", "color set ${color}")
////                }
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

