package com.ritwik.photex

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ritwik.photex.databinding.ActivityTweetWithImageBinding
import com.unity3d.ads.UnityAds
import java.text.SimpleDateFormat
import java.util.*

class TweetWithImage : AppCompatActivity() {
    val profileImageCode = 1024
    val tweetImageCode = 1025
    private var verified = false
    val deviceArray = arrayOf("Twitter for Android", "Twitter for Web", "Twitter for iPhone")
    private lateinit var binding: ActivityTweetWithImageBinding
    private lateinit var referralPopup: ReferralPopup
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTweetWithImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        UnityAds.initialize(this, "4218265", false)
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
            if (!this::referralPopup.isInitialized) {
                referralPopup = ReferralPopup(this, this)
            }
            referralPopup.showPopup()
            {
                saveWithoutWatermark()
            }

        }
        binding.atwiTweetImage.setOnClickListener {
            chooseTweetImage()
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

    fun saveImage(bitmap: Bitmap, name: String?) {
        CloudDatabase.incrementNoOfSaves()
        var contentValues = ContentValues()
        val name = name ?: getRandomName()
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
    private fun showAd() {
        val unityInterstital = UnityInterstital(this,this)
        unityInterstital.initialiseAd()
        unityInterstital.displayAd()
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
    companion object {
        private const val TAG = "TweetWithImage"
    }
}