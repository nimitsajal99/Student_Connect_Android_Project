package com.nimitsajal.studentconnectapp

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.btnBack
import kotlinx.android.synthetic.main.activity_chat.circularImageView
import kotlinx.android.synthetic.main.activity_main_feed.*
import kotlinx.android.synthetic.main.activity_profile_page.*
import kotlinx.android.synthetic.main.activity_profile_page.tvUsername
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_upload_post.*
import kotlinx.android.synthetic.main.activity_upload_post.btnUpload
import kotlinx.android.synthetic.main.toast_login_adapter.*
import java.io.ByteArrayOutputStream
import java.util.*


class upload_post : AppCompatActivity() {

    var POS = 0
    var selectedUri: Uri? = null
    var userDpUrl: String = ""
    private lateinit var functions: FirebaseFunctions
    private lateinit var Functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_post)

        val adapter = GroupAdapter<GroupieViewHolder>()

        var faceDetect = mutableListOf<faceDetection>()
        var uploadC = mutableListOf<uploadCaller>()

        var count = 0

        var username = intent.getStringExtra("username")
        functions = FirebaseFunctions.getInstance("asia-south1")
        Functions = Firebase.functions

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if(user != null){
            val user_table = db.collection("User Table").document(user.uid.toString())
            user_table.get().addOnSuccessListener { result ->
                if(result != null){
                    username = result.getString("Username").toString()
                    Log.d("profilePage", username.toString())
                    tvUsername.text = username
                    displayCredentials(username!!, db)
                }
                else{
                    showToast("ERROR", 1)
                    return@addOnSuccessListener
                }
            }
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, mainFeed::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            finish()
        }

        btnDP_upload.setOnClickListener {
            photoPicker()
        }

        btnSearch_upload.setOnClickListener {
            loadSearch(db, adapter, faceDetect, POS, uploadC, selectedUri!!, username!!)
        }

        editTextTag.addTextChangedListener {
            if(editTextTag.text.toString() != "" && editTextTag.text != null)
            {
                loadSearch(db, adapter, faceDetect, POS, uploadC, selectedUri!!, username!!)
            }
            else{
                ImageViewTag.visibility = View.VISIBLE
            }
        }

        btnTagNone.setOnClickListener {
            POS += 1
            ImageViewTag.visibility = View.VISIBLE
            adapter.clear()
            editTextTag.setText("")
            drawDetectionResult(faceDetect[0].faceBitmap, faceDetect, POS)
            if(POS == (faceDetect.size)&& POS!=0){
                tagging(uploadC, selectedUri!!, faceDetect, username!!)
            }
        }

        btnUpload.setOnClickListener {
            val description = etDescription.text.toString()
            if(username != null){
                if(selectedUri != null){
                    if(userDpUrl != null){
                        uploadPost(username!!, description, selectedUri!!, userDpUrl, db, faceDetect, uploadC)
                    }
                }
            }
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

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var resizedWidth = maxDimension
        var resizedHeight = maxDimension
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension
            resizedWidth =
                (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension
            resizedHeight =
                (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension
            resizedWidth = maxDimension
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
    }

    private fun annotateImage(requestJson: String): Task<JsonElement> {
        Log.d(
            "cloud",
            "In annotateImage"
        )
        return Functions
            .getHttpsCallable("annotateImage")
            .call(requestJson)
//            .addOnCompleteListener {
//                val result = it.result?.data
//                JsonParser.parseString(Gson().toJson(result))
//            }
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data
                JsonParser.parseString(Gson().toJson(result))
            }
    }

    private fun imageLabeler(bitmap: Bitmap){
        Log.d("cloud", "Inside Image Labeler")

//        val image = FirebaseVisionImage.fromBitmap(bitmap)
//        val labeler = FirebaseVision.getInstance().getCloudImageLabeler()

// Or, to set the minimum confidence required:
// val options = FirebaseVisionCloudImageLabelerOptions.Builder()
//     .setConfidenceThreshold(0.7f)
//     .build()
// val labeler = FirebaseVision.getInstance().getCloudImageLabeler(options)

//        labeler.processImage(image)
//            .addOnSuccessListener { labels ->
//                // Task completed successfully
//                for (label in labels) {
//                    val text = label.text
//                    val entityId = label.entityId
//                    val confidence = label.confidence
//                    Log.d("cloud", "Text = $text, Confidence = $confidence, EntityId = $entityId")
//                }
//            }
//            .addOnFailureListener { e ->
//                // Task failed with an exception
//                Log.d("cloud", "Labelling Failed")
//            }

//        val image = InputImage.fromBitmap(bitmap, 0)
//        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
//        labeler.process(image)
//            .addOnSuccessListener { labels ->
//                // Task completed successfully
//                for (label in labels) {
//                    val text = label.text
//                    val confidence = label.confidence
//                    val index = label.index
//                    Log.d("cloud", "Text = $text, Confidence = $confidence, Index = $index")
//                }
//            }
//            .addOnFailureListener { e ->
//                // Task failed with an exception
//                Log.d("cloud", "Labelling Failed")
//            }
    }

    private fun uploadPost(
        username: String,
        description: String,
        imageUri: Uri,
        userDpUrl: String,
        db: FirebaseFirestore,
        faceDetect: MutableList<faceDetection>,
        uploadC: MutableList<uploadCaller>
    ){
        pbUpload.isVisible = true
        var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

        //imageLabeler(bitmap)

        // Scale down bitmap size
        bitmap = scaleBitmapDown(bitmap, 640)
        // Convert bitmap to base64 encoded string
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
        val base64encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        // Create json request to cloud vision
        val request = JsonObject()
        // Add image to request
        val image = JsonObject()
        image.add("content", JsonPrimitive(base64encoded))
        request.add("image", image)
        //Add features to the request
        val feature = JsonObject()
        feature.add("maxResults", JsonPrimitive(25))
        feature.add("setConfidenceThreshold", JsonPrimitive(90))
        feature.add("type", JsonPrimitive("LABEL_DETECTION"))
        val features = JsonArray()
        features.add(feature)
        request.add("features", features)

        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        var faceBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val faceImage = InputImage.fromBitmap(faceBitmap, 0)
        val detector = FaceDetection.getClient(highAccuracyOpts)
//        var faceDetect = mutableListOf<faceDetection>()
        val result = detector.process(faceImage)
            .addOnSuccessListener { faces ->
                // Task completed successfully
                var count = 1
                for (face in faces) {
                    val id = count
                    val bounds = face.boundingBox
//                    bounds.left = bounds.left - 25
//                    bounds.top = bounds.top - 25
//                    bounds.right = bounds.right + 25
//                    bounds.bottom = bounds.bottom + 25

                    var margin = 0
                    if(bounds.right-bounds.left < bounds.bottom-bounds.top){
                        margin = (bounds.right - bounds.left)/5
//                        margin = bounds.left - (bounds.left.toFloat() * (84F/100F).toFloat()).toInt()
                    }
                    else{
                        margin = (bounds.bottom - bounds.top)/5
//                        margin = bounds.top - (bounds.top.toFloat() * (84F/100F).toFloat()).toInt()
                    }

                    if(margin > 25){
                        margin = 25
                    }
                    bounds.left = bounds.left - margin
                    bounds.top = bounds.top - margin
                    bounds.right = bounds.right + margin
                    bounds.bottom = bounds.bottom + margin

                    Log.d("cloud", bounds.toString())

                    val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                    val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees
                    var smileProb: Float = 0.0f
                    // If classification was enabled:
                    if (face.smilingProbability != null) {
                        smileProb = face.smilingProbability
                    }
                    else{
                        smileProb = -1.0f
                    }
//                    if (face.rightEyeOpenProbability != null) {
//                        val rightEyeOpenProb = face.rightEyeOpenProbability
//                    }
                    if(count < 11){
                        var faceObject: faceDetection = faceDetection(
                            id.toString(),
                            bounds,
                            rotY,
                            rotZ,
                            smileProb,
                            false,
                            faceBitmap,
                            margin
                        )
                        faceObject.log()
                        faceDetect.add(faceObject)
                    }
                    else{
//                        var faceObject: faceDetection = faceDetection(id.toString(), bounds, rotY, rotZ, smileProb, false)
//                        faceObject.log()
                    }
                    count += 1
//                    if(count > 10){
//                        break
//                    }
                }

                Log.d("cloud", "Number of faces detected = ${faceDetect.size}")

//                squareImageView.setImageBitmap(drawDetectionResult(faceBitmap, faceDetect))

                var abc= mutableListOf<String>()
                var caller = uploadCaller(description, userDpUrl, username, "", request,abc)
                uploadC.add(caller)

                if(faceDetect.size == 0){
                    tagging(uploadC, imageUri, faceDetect, username)
                }
                else{
                    bottomBar_upload.visibility = View.GONE
                    squareImageView.visibility = View.GONE
                    btnDP_upload.visibility = View.GONE

                    ImageViewTag.visibility = View.VISIBLE
                    recyclerTag.visibility = View.VISIBLE
                    searchTag.visibility = View.VISIBLE

                    drawDetectionResult(faceBitmap, faceDetect, POS)

                    pbUpload.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Log.d("cloud", "Face Detection Failed - ${e.toString()}")
                //TODO: uploadPost
                tagging(uploadC, imageUri, faceDetect, username)
            }
    }

    private fun uploadPostCloud(
        description: String,
        userDp: String,
        userName: String,
        picture: String,
        results: MutableList<String>,
        confidence: MutableList<String>,
        faceDetect: MutableList<faceDetection>
    ): Task<Any> {
        //val time = FieldValue.serverTimestamp()
        var data: HashMap<String, Any> = hashMapOf<String, Any>()
        data.put("description", description)
        data.put("dp", userDp)
        data.put("userName", userName)
        data.put("picture", picture)
        var count = 0
        data.put("noOfTags", results.size.toString())
        var increment = 0
        var tagIncrement = 0
        for(tag in results){
            var string = "tagNo" + count
            var str = "confidence" + count
            data.put(string, tag)
            data.put(str, confidence[count])
            if(confidence[count].toFloat() > 0.75f){
                increment += 3
                tagIncrement += 1
            }
            else if(confidence[count].toFloat() > 0.60f){
                increment += 2
                tagIncrement += 1
            }
            else{
                increment += 1
            }
            count += 1
            Log.d("cloud", tag)
        }
        while(count < 10){
            var string = "tagNo" + count
            var str = "confidence" + count
            data.put(string, "xxxxx")
            data.put(str, "xxxxx")
            count += 1
        }
        data.put("noOfFaces", faceDetect.size.toString())
        count = 0
        var smiles = 0
        Log.d("cloud", "iIncrement = $increment")
        val db = FirebaseFirestore.getInstance()
        db.collection("Users").document(userName)
            .get()
            .addOnSuccessListener {
                db.collection("Users").document(userName)
                    .update("Tags", (it["Tags"].toString().toInt() + increment))
                    .addOnSuccessListener {
                        Log.d("others", "Tag updated - added")
                    }
            }
        for(face in faceDetect){
            var string = "faceId" + count
            data.put(string, face.faceId)

            var margin = face.margin
            string = "boundLeft" + count
            face.boundingBox.left = face.boundingBox.left + margin
            data.put(string, face.boundingBox.left.toString())
            string = "boundRight" + count
            face.boundingBox.top = face.boundingBox.top + margin
            data.put(string, face.boundingBox.right.toString())
            string = "boundTop" + count
            face.boundingBox.right = face.boundingBox.right - margin
            data.put(string, face.boundingBox.top.toString())
            string = "boundBottom" + count
            face.boundingBox.bottom = face.boundingBox.bottom - margin
            data.put(string, face.boundingBox.bottom.toString())
            string = "named" + count
            if(face.named){
                data.put(string, "true")
                db.collection("Users").document(face.faceId)
                    .get()
                    .addOnSuccessListener {
                        db.collection("Users").document(face.faceId)
                            .update("Tags", (it["Tags"].toString().toInt() + tagIncrement))
                            .addOnSuccessListener {
                                Log.d("others", "Tag updated - $tagIncrement")
                            }
                    }
            }
            else{
                data.put(string, "false")
            }
            Log.d("cloud", face.boundingBox.toString())


            string = "rotY" + count
            data.put(string, face.rotY)
            string = "rotZ" + count
            data.put(string, face.rotZ)
            string = "smile" + count
            var smile = false
            if(face.smileProb > 0.65){
                smile = true
                smiles += 1
                data.put(string, "true")
                Log.d(
                    "cloud",
                    "SmileProb = ${face.smileProb}, turned true = $smiles, data = ${data[string]}"
                )
            }
            else{
                data.put(string, "false")
            }
            count += 1
        }
        if(count > 0){
            val smileRatio: Float = (smiles.toFloat()/(count).toFloat())
            if(smileRatio > 0.70){
                data.put("smile", "Happy")
                Log.d("cloud", "Ratio Happy = $smileRatio")
            }
            else if(smileRatio < 0.26){
                data.put("smile", "Sad")
                Log.d("cloud", "Ratio Sad = $smileRatio")
            }
            else{
                data.put("smile", "Can't Say")
                Log.d("cloud", "Ratio Can't Say = $smileRatio")
            }
        }
        else{
            data.put("smile", "Can't Say")
            Log.d("cloud", "Ratio Can't Say")
        }
        while(count < 10){
            var string = "faceId" + count
            data.put(string, "xxxxx")
            string = "bound" + count
            data.put(string, "xxxxx")
            string = "rotY" + count
            data.put(string, "xxxxx")
            string = "rotZ" + count
            data.put(string, "xxxxx")
            string = "smile" + count
            var smile = false
            data.put(string, smile)
            count += 1
        }
        Log.d(
            "uploadCloud",
            "${data["description"]}, ${data["dp"]}, ${data["userName"]}, ${data["picture"]}, ${data["timeStamp"]}"
        )

        return functions
            .getHttpsCallable("uploadPost")
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

    private fun uploadPostCaller(
        description: String,
        userDp: String,
        userName: String,
        picture: String,
        request: JsonObject,
        faceDetect: MutableList<faceDetection>
    ){
        Log.d(
            "cloud",
            "sending into annotataImage"
        )
        annotateImage(request.toString())
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    // Task failed with an exception
                    Log.d("cloud", "AnnotateImage Failed ${task.exception}")
                    uploadPostCaller(description,userDp,userName,picture,request,faceDetect)
                } else {
                    Log.d("cloud", "AnnotateImage Success")
                    val jsonArray = task.result!! as JsonArray
                    var results = mutableListOf<String>()
                    var confidenceResult = mutableListOf<String>()
                    var count = 0
                    for (label in jsonArray[0].asJsonObject["labelAnnotations"].asJsonArray) {
                        val labelObj = label.asJsonObject
                        var text = labelObj["description"]
                        val entityId = labelObj["mid"]
                        val confidence = labelObj["score"]
                        var Text = text.toString().substring(1, text.toString().length - 1)
                        if(isTagValid(Text, results)){
                            Log.d(
                                "cloud",
                                "text = $Text, confidence = $confidence, entityID = $entityId"
                            )
                        }
                        if(confidence.toString().toFloat() > 0.50 && count < 10 && isTagValid(Text, results)){
                            var word = Text.toString().split(" ")
                            results.add(word[0])
                            confidenceResult.add(confidence.toString())
                            count += 1
                        }
                        else{
                            //break
                        }
                    }
                    // Task completed successfully
                    uploadPostCloud(
                        description,
                        userDp,
                        userName,
                        picture,
                        results,
                        confidenceResult,
                        faceDetect
                    )
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                val e = task.exception
                                if (e is FirebaseFunctionsException) {
                                    val code = e.code
                                    val details = e.details
                                    Log.d("cloud", "code = ${e.code}, details = $details")
                                }
                                // [START_EXCLUDE]
                                Log.d("cloud", "addMessage:onFailure", e)
//                    showSnackbar("An error occurred.")
                                showToast("Image Not Uploaded", 1)
                                val intent = Intent(this, mainFeed::class.java)
                                intent.putExtra("username", userName)
                                pbUpload.isVisible = false
                                startActivity(intent)
                                overridePendingTransition(
                                    R.anim.zoom_out_upload,
                                    R.anim.static_transition
                                )
                                finish()
                                return@OnCompleteListener
                                // [END_EXCLUDE]
                            } else {
                                showToast("Image Uploaded", 2)
                                val intent = Intent(this, mainFeed::class.java)
                                intent.putExtra("username", userName)
                                pbUpload.isVisible = false
                                startActivity(intent)
                                overridePendingTransition(
                                    R.anim.zoom_out_upload,
                                    R.anim.static_transition
                                )
                                finish()
                                return@OnCompleteListener
                            }
                        }
                        )
                }
            }
    }

    /**
     * Draw bounding boxes around objects together with the object's name.
     */
    private fun drawDetectionResult(bitmap: Bitmap, detectionResults: List<faceDetection>, pos: Int) {
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        val pen = Paint()
        pen.textAlign = Paint.Align.LEFT
        var count = 0

        detectionResults.forEach {
            Log.d("cloud", "Creating Another Square")
            // draw bounding box
            if(count < pos){
                if(it.named == true){
                    pen.color = Color.GREEN
                }
                else{
                    pen.color = Color.GRAY
                }
            }
            else if(count == pos){
                pen.color = Color.RED
            }
            else{
                pen.color = Color.TRANSPARENT
            }

            pen.strokeWidth = 4F
            pen.style = Paint.Style.STROKE
            val box = it.boundingBox
//            box.left = box.left - 25
//            box.top = box.top - 25
//            box.right = box.right + 25
//            box.bottom = box.bottom + 25
//            canvas.drawRect(box, pen)
            canvas.drawRoundRect(box.left.toFloat(), box.top.toFloat(), box.right.toFloat(), box.bottom.toFloat(), 7F, 7F, pen)

            val tagSize = Rect(0, 0, 0, 0)

            // calculate the right font size
            pen.style = Paint.Style.FILL_AND_STROKE
            pen.color = Color.YELLOW
            if(count < pos){
                if(it.named == true){
                    pen.color = Color.GREEN
                }
                else{
                    pen.color = Color.TRANSPARENT
                }
            }
            else if(count == pos){
                pen.color = Color.RED
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
        ImageViewTag.setImageBitmap(outputBitmap)
    }


//    private fun uploadPost(username: String, description: String, imageUri: Uri, userDpUrl: String, db: FirebaseFirestore){
//        pbUpload.isVisible = true
//        val filename = UUID.randomUUID().toString()
//        val ref = FirebaseStorage.getInstance().getReference("images/uploads/$username/$filename")
//        ref.putFile(imageUri)
//            .addOnSuccessListener { img ->
//                Log.d(
//                    "Registration",
//                    "Image successfully uploaded at location: ${img.metadata?.path}"
//                )
//                ref.downloadUrl
//                    .addOnSuccessListener { img_link ->
////                        val calendar = Calendar.getInstance()
////                        val time = calendar.timeInMillis
//                        val time = FieldValue.serverTimestamp()
//                        val post = hashMapOf(
//                            "From" to username,
//                            "Description" to description,
//                            "Dp" to userDpUrl,
//                            "Likes" to 0,
//                            "Picture" to img_link.toString(),
//                            "Time" to time
//                        )
//                        val info = hashMapOf(
//                            "Info" to "Info",
//                        )
//                        val linkPost = hashMapOf(
//                            "Liked" to false,
//                            "Time" to time
//                        )
//                        db.collection("Post").add(post)
//                            .addOnSuccessListener {postObject ->
//                                db.collection("Post").document(postObject.id).collection("Comments").document("Info")
//                                    .set(info)
//                                    .addOnSuccessListener {
//                                        Log.d(
//                                            "Registration",
//                                            "Comments collection created: ${img.metadata?.path}"
//                                        )
//                                    }
//
//                                db.collection("Post").document(postObject.id).collection("Tags").document("Info")
//                                    .set(info)
//                                    .addOnSuccessListener {
//                                        Log.d(
//                                            "Registration",
//                                            "Tags collection created: ${img.metadata?.path}"
//                                        )
//                                    }
//
//                                db.collection("Users").document(username).collection("My Posts").document(postObject.id)
//                                    .set(linkPost)
//                                    .addOnSuccessListener {
//                                        Log.d(
//                                            "Registration",
//                                            "Image added into my post: ${img.metadata?.path}"
//                                        )
//                                    }
//
//                                db.collection("Users").document(username).collection("Friends")
//                                    .get()
//                                    .addOnSuccessListener {friendList ->
//                                        for(document in friendList){
//                                            if(document.id != "Info"){
//                                                db.collection("Users").document(document.id).collection("My Feed").document(postObject.id)
//                                                    .set(linkPost)
//                                                    .addOnSuccessListener {
//                                                        Log.d("post", "Post in the feed of ${document.id}")
//                                                    }
//                                            }
//                                        }
//                                        val intent = Intent(this, mainFeed::class.java)
//                                        intent.putExtra("username", username)
//                                        pbUpload.isVisible = false
//                                        startActivity(intent)
//                                        overridePendingTransition(R.anim.zoom_out_upload, R.anim.static_transition)
//                                        finish()
//                                    }
//                            }
//                    }
//            }
//            .addOnFailureListener {
//                Log.d("Registration", "Image upload failed: ${it.message}")
//            }
//    }

    private fun displayCredentials(username: String, db: FirebaseFirestore){
        db.collection("Users").document(username)
            .get()
            .addOnSuccessListener {
                if(it != null){
                    userDpUrl = it.getString("Picture").toString()
                    Picasso.get().load(userDpUrl).into(circularImageView)
                }
            }
    }



    private fun photoPicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
//            Toast.makeText(this, "Photo was selected", Toast.LENGTH_SHORT).show()
            selectedUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedUri)
            squareImageView.setImageBitmap(bitmap)
            btnDP_upload.alpha = 0f

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            btnDP.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun tagging(uploadC: MutableList<uploadCaller>, imageUri: Uri, faceDetect: MutableList<faceDetection>, username: String){
        ImageViewTag.visibility = View.GONE
        recyclerTag.visibility = View.GONE
        searchTag.visibility = View.GONE
        pbUpload.visibility = View.VISIBLE
        if(faceDetect.size != 0){
            squareImageView.setImageBitmap(ImageViewTag.drawToBitmap())
        }
        squareImageView.visibility = View.VISIBLE

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("images/uploads/$username/$filename")
        ref.putFile(imageUri)
            .addOnSuccessListener { img ->
                Log.d(
                    "cloud",
                    "Image successfully uploaded at location: ${img.metadata?.path}"
                )
                ref.downloadUrl
                    .addOnSuccessListener { img_link ->
//                        val calendar = Calendar.getInstance()
//                        val time = calendar.timeInMillis
                        Log.d(
                            "cloud",
                            "Image successfully uploaded at location: ${img_link.toString()}}"
                        )
                        uploadPostCaller(uploadC[0].description, uploadC[0].userDpUrl, uploadC[0].username, img_link.toString(), uploadC[0].request, faceDetect)
                    }
            }
            .addOnFailureListener {
                Log.d("Registration", "Image upload failed: ${it.message}")
            }
    }

    private fun loadSearch(
        db: FirebaseFirestore,
        adapter: GroupAdapter<GroupieViewHolder>,
        faceDetect: MutableList<faceDetection>,
        pos: Int,
        uploadC: MutableList<uploadCaller>,
        imageUri: Uri,
        username: String
    ){
        val search = editTextTag.text.toString()
        var waiting = false
        adapter.clear()
        if(search != "" || search != null)
        {
            ImageViewTag.visibility = View.GONE
            val words = search.split("\\s+".toRegex()).map { word ->
                word.replace("""^[,\.]|[,\.]$""".toRegex(), "")
            }
            db.collection("Users")
                .addSnapshotListener { value, error ->
                    if(value == null || error != null){
                        showToast("ERROR", 1)
                        return@addSnapshotListener
                    }
                    for(document in value.documents)
                    {
                        if(document.id != "Info")
                        {
                            var contains = true
                            for(word in words)
                            {
                                val pattern = word.toRegex(RegexOption.IGNORE_CASE)

                                if((pattern.containsMatchIn(document["Name"].toString()) ||
                                            pattern.containsMatchIn(document.id) ||
                                            pattern.containsMatchIn(document["College"].toString())||
                                            pattern.containsMatchIn(document["Branch"].toString()) ||
                                            pattern.containsMatchIn(document["Semester"].toString()))
                                    && !(document.id in uploadC[0].listselected) && (document.id != username))
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
//                                val temp = usersList(document.id, "", document["Name"].toString(), document["Picture"].toString(), "")
//                                arraySearch.add(temp)
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
                        ImageViewTag.visibility = View.VISIBLE
                        val searchItem: UserSearch = item as UserSearch
                        val to = searchItem.username
                        adapter.clear()
                        faceDetect[pos].faceId = to
                        faceDetect[pos].named = true
                        waiting = true
                        POS += 1
                        uploadC[0].listselected.add(to)
                        drawDetectionResult(faceDetect[0].faceBitmap, faceDetect, POS)
                        editTextTag.setText("")
                        if(POS == (faceDetect.size) && POS != 0){
                            //TODO: uploadPost
                            tagging(uploadC, imageUri, faceDetect, username)
                        }
                        return@setOnItemClickListener
                    }
                    recyclerTag.adapter = adapter
                }
        }
//        if(waiting == true){
//            return true
//        }
    }

    private fun isTagValid(tag: String, results: MutableList<String>): Boolean{
        var blockedList = mutableListOf<String>("Tableware","Smile","Skin","Hairstyle","Facial expression","Happy","Gesture",
            "Leisure","Cosmetic dentistry","Beauty","Comfort","Thumb","Outerwear",
            "Vertebrate","White-collar worker","Fur","Hair","Head","Chin","Neck","Jaw",
            "Sleeve","Eyelash","Rectangle","Font","Red hair","Forehead","Nose","Eyebrow","Muscle",
            "Collar","Symmetry","Brand","Human","Finger","Youth","People","T-shirt","Wrist",
            "Trousers","Waist","Plaid","Lip","Beard","Facial hair","Moustache","Snapshot","Chest",
            "Flesh","Magenta","Sky","Water","Vision care","Eyewear","Shorts","Fun",

            "Pink","Red","Blue","Green","Electric blue","Purple","Peach","Neon","Fawn", "Orange","White","Grey","Yellow",

            "Pattern","Wood","Hardwood","Column",
            "Varnish","Hybrid tea rose","Woody plant","Shrub","Annual plant","Rose order",
            "Branch","Hybrid tea rose","Petal","Arm","Cool","Black hair","Long hair","Body jewelry","Facade","Signage",
            "Neon sign","Abdomen","Eye","Rite","Carmine","Twig","Wheel","Tire","Bumper",
            "Wrinkle","Woody plant","Leaf","Human body","Elbow","Joint","Shoulder","Thigh","Knee","Grass","Human leg",
            "Hand","Circle","Number","Flooring","Floor","Gas","Room","Metal","Composite material","Ceiling",
            "Cheek","Curtain",

            "Logo", "Material property", "Official", "Urban design",
            "Sharing","Bone","Tap","Window","Face","Mammal","Carnivore","Gun dog","Beak","Organism","Flightless bird",
            "Illustration","Tail","Liquid","Iris", "Ecoregion","Plate","Bowl",

            "Handwriting","Pole","Sitting","Brick","Door","Brickwork","Roof","Siding",
            "Tread","Rolling","Fender","Rim",
            "Peripheral","Layered hair","Sink","Plant stem","Trunk","Vehicle door","Hood","Windshield","Afterglow","Arecales",
            "Adaptation","Wall","Lap","Liver", "Stuffed toy","Working animal","Terrestrial animal","Sun hat",
            "Northern hardwood forest","Line","Shelving")
        if(tag in blockedList){
            return false
        }
        else{
            var word = tag.split(" ")
            return word[0] !in results
        }
    }
}

data class uploadCaller(var description: String, var userDpUrl: String, var username: String, var img_link: String, var request: JsonObject, var listselected: MutableList<String>){

}

data class faceDetection(
    var faceId: String,
    var boundingBox: Rect,
    var rotY: Float,
    var rotZ: Float,
    var smileProb: Float,
    var named: Boolean,
    var faceBitmap: Bitmap,
    var margin: Int
) {
    public fun log(){
        Log.d(
            "cloud",
            "FaceID = $faceId \n SmileProb = $smileProb \n rotY = $rotY \n rotZ = $rotZ \n Bound = $boundingBox"
        )
    }
}