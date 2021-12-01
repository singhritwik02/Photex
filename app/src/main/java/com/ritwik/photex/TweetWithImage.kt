package com.ritwik.photex

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ritwik.photex.databinding.ActivityTweetWithImageBinding
import com.ritwik.photex.databinding.PopupAdLoadingBinding
import com.ritwik.photex.databinding.PopupChooseImageOptionsBinding
import com.unity3d.ads.IUnityAdsListener
import com.unity3d.ads.UnityAds
import java.text.SimpleDateFormat
import java.util.*

class TweetWithImage : AppCompatActivity() {
    val profileImageCode = 1024
    val tweetImageCode = 1025
    private var verified = false
    val deviceArray = arrayOf("Twitter for Android", "Twitter for Web", "Twitter for iPhone")
    private lateinit var binding: ActivityTweetWithImageBinding
    private lateinit var chooseTemplate: PopupChooseTemplate
    private lateinit var adLoadingWindow:PopupWindow
    //
    private lateinit var imageOptionsBinding: PopupChooseImageOptionsBinding
    private lateinit var imageOptionsWindow:PopupWindow
    private var imageSelected:Boolean = false
    private var currentMode = "T"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTweetWithImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.post {
            getDefaultMode()
        }
        UnityAds.initialize(this, "4218265",UnityAdsListener(), false)
        try {
            val details = TwitterAccountDatabase.getDetails(this)
            binding.atwiName.setText(details[0])
            binding.atwiUsername.setText(details[1])
            TwitterAccountDatabase.getProfileThumbnail(this)?.let {
                binding.atwiProfilePic.setImageBitmap(it)
            }
        }
        catch (e:java.lang.Exception)
        {
            e.printStackTrace()
        }
        binding.atwiSave.setOnClickListener {

            binding.atwiLooseFocus.requestFocus()
            val bmp = loadBitmapFromView(binding.atwiMainTweet)
            if (bmp != null) {
                val name = binding.atwiLooseFocus.text.toString()
                name.let {
                    showAd()
                    if (it == "") {
                        saveImage(
                            bmp, null
                        )
                    } else {

                        saveImage(bmp, it)
                    }
                }

            } else {
                Toast.makeText(this, "Failed to save Image", Toast.LENGTH_SHORT).show()
            }
        }
        binding.atwiModeSwitch.setOnClickListener {
            changeMode()
            setDefaultMode(currentMode)
        }
        binding.atwiProfilePic.setOnClickListener {
            changeProfileImage()
        }
        binding.atwiDevice.setOnClickListener {
            changeDevice()
        }
        binding.atwiVerification.setOnClickListener {
            changeVerification()
        }
        binding.root.post {

            setCurrentDate()
            setCurrentTime()
        }
        binding.atwiWatermarkButton.setOnClickListener {
            showRewardedAd()

        }
        binding.atwiTweetImage.setOnClickListener {

          showImageOptions()
        }

    }

    private fun chooseTweetImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false)
        startActivityForResult(intent, tweetImageCode)
    }

    fun loadBitmapFromView(v: View): Bitmap? {
        v.clearFocus()
        val b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.draw(c)
        return b
    }

    private fun changeProfileImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false)
        startActivityForResult(intent, profileImageCode)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == profileImageCode && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.d(TAG, "onActivityResult: Data is null")
                return
            }
            data.data.let {
                binding.atwiProfilePic.setImageURI(it)
            }
        }
        if (requestCode == tweetImageCode && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.d(TAG, "onActivityResult: Data is null")
                return
            }
            data.data.let {
                binding.atwiTweetImage.setImageURI(it)
            }
        }
    }

    private fun changeDevice() {
        val currentIndex = deviceArray.indexOf(binding.atwiDevice.text)
        var newIndex =
            if (currentIndex == -1) {
                0
            } else if (currentIndex == 0) {
                1
            } else if (currentIndex == 1) {
                2
            } else if (currentIndex == 2) {
                0
            } else {
                0
            }
        binding.atwiDevice.animate().alpha(0.2f).setDuration(150).withEndAction {
            binding.atwiDevice.setText(deviceArray[newIndex])
            binding.atwiDevice.animate().alpha(1f).setDuration(150)
        }

    }

    private fun setCurrentTime() {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentDateandTime: String = sdf.format(Date())
        binding.atwiTime.setText(currentDateandTime)
    }

    private fun setCurrentDate() {
        val sdf = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
        val currentDateandTime: String = sdf.format(Date())
        binding.atwiDate.setText(currentDateandTime)
    }

    fun saveImage(bitmap: Bitmap, nameP: String?) {
        CloudDatabase.incrementNoOfSaves()
        val contentValues = ContentValues()
        val name = nameP ?: getRandomName()
        Log.d(TAG, "saveImage: Saving image as $name")
        contentValues.apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }
        val resolver = contentResolver
        try {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            val uri = resolver.insert(contentUri, contentValues)
            if (uri == null) {
                Log.d(TAG, "saveImage: Uri is null")
                Toast.makeText(this, "Failed to save Image", Toast.LENGTH_SHORT).show()
                return
            }
            val outputStream = resolver.openOutputStream(uri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(this, "Image saved as $name", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "saveImage: Output stream is null")
            }


        saveDetails()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to Save Image", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }

    fun getRandomName(): String {
        var name = "PhotexTweet"
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        name = "$name$n"
        return name
    }

    private fun changeVerification() {
        if (verified) {
            binding.atwiVerification.animate().alpha(0.2f).setDuration(150).withEndAction {
                verified = false
                binding.atwiVerificationImage.setImageResource(R.drawable.twitter_not_verified)
                binding.atwiVerification.animate().alpha(1f).setDuration(150)
                binding.atwiVerifiedIcon.visibility = View.GONE
            }
        } else {
            binding.atwiVerification.animate().alpha(0.2f).setDuration(150).withEndAction {
                verified = true
                binding.atwiVerificationImage.setImageResource(R.drawable.twitter_verified)
                binding.atwiVerification.animate().alpha(1f).setDuration(150)
                binding.atwiVerifiedIcon.visibility = View.VISIBLE
            }

        }
    }

    private fun saveWithoutWatermark() {
        saveDetails()
        binding.atwiLooseFocus.requestFocus()
        binding.atwiWatermark.visibility = View.INVISIBLE
        val bmp = loadBitmapFromView(binding.atwiMainTweet)
        binding.atwiWatermark.visibility = View.VISIBLE

        if (bmp != null) {
            val name = binding.atwiLooseFocus.text.toString()
            name.let {
                if (it == "") {
                    saveImage(
                        bmp, null
                    )
                } else {
                    saveImage(bmp, it)
                }
            }

        } else {
            Toast.makeText(this, "Failed to save Image", Toast.LENGTH_SHORT).show()
        }
    }
    inner class UnityAdsListener: IUnityAdsListener
    {
        override fun onUnityAdsReady(p0: String?) {
            Log.d(TAG, "onUnityAdsReady: ")
        }

        override fun onUnityAdsStart(p0: String?) {
            Log.d(TAG, "onUnityAdsStart: ")
        }

        override fun onUnityAdsFinish(p0: String?, p1: UnityAds.FinishState?) {
            Log.d(TAG, "onUnityAdsFinish: ")
        }

        override fun onUnityAdsError(p0: UnityAds.UnityAdsError?, p1: String?) {
            Log.d(TAG, "onUnityAdsError: ")
        }

    }
    inner class RewardedAdListener: IUnityAdsListener
    {
        override fun onUnityAdsReady(p0: String?) {
            Log.d(TAG, "onUnityAdsReady: ")



        }

        override fun onUnityAdsStart(p0: String?) {
            Log.d(TAG, "onUnityAdsStart: ad started")


        }
        override fun onUnityAdsError(p0: UnityAds.UnityAdsError?, p1: String?) {

            Log.d(TAG, "onUnityAdsError: error ${p1?.toString()}" )
        }
        override fun onUnityAdsFinish(p0: String?, finishState: UnityAds.FinishState?) {

            finishState?.let { state ->
                if (state == UnityAds.FinishState.COMPLETED) {
                    saveWithoutWatermark()


                } else if (state == UnityAds.FinishState.SKIPPED) {
                    Log.d(TAG, "onUnityAdsFinish: Skipped")
                } else if (state == UnityAds.FinishState.ERROR) {
                    Log.d(TAG, "onUnityAdsFinish: error")
                } else {
                    Log.d(TAG, "onUnityAdsFinish")
                }
            }
        }

    }
    private fun showAd() {
        val view = PopupAdLoadingBinding.inflate(layoutInflater)
        if(!this::adLoadingWindow.isInitialized)
        {

            adLoadingWindow = PopupWindow(view.root, WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,false)
            adLoadingWindow.elevation = 20f
            adLoadingWindow.showAtLocation(view.root,Gravity.CENTER,0,0)
        }
        else if(!adLoadingWindow.isShowing)
        {
            adLoadingWindow.showAtLocation(view.root,Gravity.CENTER,0,0)
        }
        val timer = object: CountDownTimer(6000,1000)
        {
            override fun onTick(p0: Long) {
                Log.e(TAG, "onTick:  = $p0", )
                if(UnityAds.isReady("Interstitial_Android"))
                {
                    Log.e(TAG, "onTick: ready", )
                    UnityAds.show(this@TweetWithImage,"Interstitial_Android")
                    Log.d(TAG, "showAd: showing ad")
                    if(adLoadingWindow.isShowing)
                    {
                        adLoadingWindow.dismiss()
                    }
                    this.cancel()
                }
                else
                {
                    Log.e(TAG, "onTick: not ready", )
                }
            }

            override fun onFinish() {
                if(adLoadingWindow.isShowing)
                {
                    adLoadingWindow.dismiss()
                }
            }


        }
        timer.start()

    }

    private fun showRewardedAd()
    {
        AlertDialog.Builder(this)
            .setTitle("Remove Watermark")
            .setMessage("You can remove watermark for once by watching a rewarded Ad") // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Watch Ad",
                DialogInterface.OnClickListener { dialog, which ->
                    // showing the rewarded ad
                    val view = PopupAdLoadingBinding.inflate(layoutInflater)
                    UnityAds.initialize(this@TweetWithImage,"4218265",RewardedAdListener(),false)
                    if(!this::adLoadingWindow.isInitialized)
                    {

                        adLoadingWindow = PopupWindow(view.root, WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,false)
                        adLoadingWindow.elevation = 20f
                        adLoadingWindow.showAtLocation(view.root,Gravity.CENTER,0,0)
                    }
                    else if(!adLoadingWindow.isShowing)
                    {
                        adLoadingWindow.showAtLocation(view.root,Gravity.CENTER,0,0)
                    }
                    val timer = object: CountDownTimer(10000,1000)
                    {
                        override fun onTick(p0: Long) {

                            if(UnityAds.isReady("watermark_rewarded"))
                            {
                                UnityAds.show(this@TweetWithImage,"watermark_rewarded")
                                if(adLoadingWindow.isShowing)
                                {
                                    adLoadingWindow.dismiss()
                                }
                                this.cancel()
                            }
                        }

                        override fun onFinish() {
                            if(adLoadingWindow.isShowing)
                            {
                                adLoadingWindow.dismiss()
                            }
                            Toast.makeText(this@TweetWithImage, "Failed to load ad,Try Again!!", Toast.LENGTH_SHORT).show()
                        }

                    }
                    timer.start()
                    Toast.makeText(this, "PLease Wait, Loading Ad", Toast.LENGTH_SHORT).show()



                }) // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun saveDetails()
    {
        try {


            val name = binding.atwiName.text.toString()
            val username = binding.atwiUsername.text.toString()
            val bitmap = ((binding.atwiProfilePic.drawable) as BitmapDrawable).bitmap

            TwitterAccountDatabase.addToSharedPref(name, username, bitmap, this)
        }
        catch (e:java.lang.Exception)
        {
            e.printStackTrace()
        }
    }
    private fun showImageOptions()
    {
        if(!this::imageOptionsBinding.isInitialized) {
            imageOptionsBinding = PopupChooseImageOptionsBinding.inflate(layoutInflater)
            imageOptionsWindow = PopupWindow(
                imageOptionsBinding.root,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                true
            )
        }
        imageOptionsWindow.elevation = 100f
        imageOptionsWindow.animationStyle = R.style.slideAnimation
        imageOptionsWindow.showAtLocation(imageOptionsBinding.root,Gravity.BOTTOM,0,20)
        imageOptionsBinding.pcioGallery
            .setOnClickListener {
                imageOptionsWindow.dismiss()
                chooseTweetImage()

            }
        imageOptionsBinding.pcioTemplate.setOnClickListener {
            if(!this::chooseTemplate.isInitialized)
            {
                chooseTemplate = PopupChooseTemplate(this)
                {bitmap,_->
                    val croppedBitmap = bitmap
                    binding.atwiTweetImage.setImageBitmap(croppedBitmap)
                }
            }
            chooseTemplate.showPopup()
            imageOptionsWindow.dismiss()
        }
    }

    override fun onDestroy() {

        super.onDestroy()
    }
    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Discard Image")
            .setMessage("Discard the current image") // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Discard",
                DialogInterface.OnClickListener { dialog, which ->
                    finish()

                }) // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton("Cancel", null)
            .show()

    }
    private fun adjustAspectRatio(bitmap:Bitmap):Bitmap
    {
        val oWidth = bitmap.width
        val oHeight = bitmap.height
        val nWidth = oWidth - (oWidth%16)
        val nHeight = (nWidth*9)/16
        // creating the scaled bitmap
        val result = Bitmap.createScaledBitmap(bitmap,nWidth,nHeight,false)

        return result

    }
    private fun switchToBasic()
    {
        binding.atwiExtrasLayout.visibility = View.GONE
        binding.atwiImageCard.visibility = View.GONE
        currentMode = "B"
    }
    private fun switchToTextOnly()
    {
        binding.atwiExtrasLayout.visibility = View.VISIBLE
        binding.atwiImageCard.visibility = View.GONE
        currentMode = "T"
    }
    private fun switchToImageTweet()
    {
        binding.atwiExtrasLayout.visibility = View.VISIBLE
        binding.atwiImageCard.visibility = View.VISIBLE
        currentMode = "I"
    }
    override fun onBackPressed() {
        showExitConfirmation()
    }
    private fun changeMode()
    {
        animateModeButton()
        if(currentMode == "T")
        {
            switchToImageTweet()
        }
        else if(currentMode == "I")
        {

            switchToBasic()
        }
        else if(currentMode == "B")
        {
            switchToTextOnly()
        }
    }
    private fun animateModeButton()
    {
        binding.atwiModeSwitch.animate().alpha(0.1f).setDuration(200).withEndAction {
            binding.atwiModeSwitch.animate().alpha(1f).setDuration(200)
        }
    }
    companion object {
        private const val TAG = "TweetWithImage"
    }
    private fun getDefaultMode()
    {
        val sharedPreferences = getSharedPreferences("TWEET_MODE", MODE_PRIVATE)
        var mode = "T"
        if(sharedPreferences.contains("MODE"))
        {
            mode =  sharedPreferences.getString("MODE","T")?:"T"
        }
        when(mode)
        {
            "B"->
            {
                switchToBasic()
            }
            "T"->
            {
                switchToTextOnly()
            }
            "I"->
            {
                switchToImageTweet()
            }
        }
    }
    private fun setDefaultMode(modeString:String)
    {
        val sharedPreferences = getSharedPreferences("TWEET_MODE", MODE_PRIVATE)
        with(sharedPreferences.edit())
        {
            putString("MODE",modeString)
            commit()
        }
    }
}