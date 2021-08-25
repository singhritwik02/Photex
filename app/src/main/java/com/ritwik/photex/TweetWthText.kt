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
import com.ritwik.photex.databinding.ActivityTweetWthTextBinding
import com.skydoves.colorpickerview.kotlin.colorPickerDialog
import com.unity3d.ads.UnityAds
import java.lang.reflect.Executable
import java.text.SimpleDateFormat
import java.util.*

class TweetWthText : AppCompatActivity() {
    val deviceArray = arrayOf("Twitter for Android", "Twitter for Web", "Twitter for iPhone")
    val profileImageCode = 1024
    var verified = false
    private lateinit var binding: ActivityTweetWthTextBinding
    private lateinit var referalPopup:ReferralPopup
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTweetWthTextBinding.inflate(layoutInflater)
        setContentView(binding.root)
        UnityAds.initialize(this, "4218265", false)
        try {
            val details = TwitterAccountDatabase.getDetails(this)
            binding.atwtName.setText(details[0])
            binding.atwtUsername.setText(details[1])
            TwitterAccountDatabase.getProfileThumbnail(this)?.let {
                binding.atwtProfilePic.setImageBitmap(it)
            }


        }
        catch (e:java.lang.Exception)
        {
            e.printStackTrace()
        }
        binding.atwtSave.setOnClickListener {

           binding.atwtLooseFocus.requestFocus()
            val bmp = loadBitmapFromView(binding.atwtMainTweet)
            if (bmp != null) {
                val name = binding.atwtLooseFocus.text.toString()
                name.let {
                    showAd()
                    if(it == "") {
                        saveImage(bmp,null
                        )
                    } else {

                        saveImage(bmp,it)
                    }
                }

            } else {
                Toast.makeText(this, "Failed to save Image", Toast.LENGTH_SHORT).show()
            }
        }
        binding.atwtProfilePic.setOnClickListener {
            changeProfileImage()
        }
        binding.atwtDevice.setOnClickListener {
            changeDevice()
        }
        binding.atwtVerification.setOnClickListener {
            changeVerification()
        }
        binding.root.post {

            setCurrentDate()
            setCurrentTime()
        }
        binding.atwtWatermarkButton.setOnClickListener {
            if(!this::referalPopup.isInitialized)
            {
                referalPopup = ReferralPopup(this,this)
            }
            referalPopup.showPopup()
            {
                saveWithoutWatermark()
            }

        }
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
                binding.atwtProfilePic.setImageURI(it)
            }
        }
    }

    private fun changeDevice() {
        val currentIndex = deviceArray.indexOf(binding.atwtDevice.text)
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
        binding.atwtDevice.animate().alpha(0.2f).setDuration(150).withEndAction {
            binding.atwtDevice.setText(deviceArray[newIndex])
            binding.atwtDevice.animate().alpha(1f).setDuration(150)
        }

    }

    private fun setCurrentTime() {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentDateandTime: String = sdf.format(Date())
        binding.atwtTime.setText(currentDateandTime)
    }

    private fun setCurrentDate() {
        val sdf = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
        val currentDateandTime: String = sdf.format(Date())
        binding.atwtDate.setText(currentDateandTime)
    }

    fun saveImage(bitmap: Bitmap,name:String?) {

        CloudDatabase.incrementNoOfSaves()
        var contentValues = ContentValues()
        val name = name?:getRandomName()
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


        } catch (e: Exception) {
            Toast.makeText(this, "Failed to Save Image", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
        saveDetails()

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
            binding.atwtVerification.animate().alpha(0.2f).setDuration(150).withEndAction {
                verified = false
                binding.atwtVerificationImage.setImageResource(R.drawable.twitter_not_verified)
                binding.atwtVerification.animate().alpha(1f).setDuration(150)
                binding.atwtVerifiedIcon.visibility = View.GONE
            }
        } else {
            binding.atwtVerification.animate().alpha(0.2f).setDuration(150).withEndAction {
                verified = true
                binding.atwtVerificationImage.setImageResource(R.drawable.twitter_verified)
                binding.atwtVerification.animate().alpha(1f).setDuration(150)
                binding.atwtVerifiedIcon.visibility = View.VISIBLE
            }

        }
    }
    private fun saveWithoutWatermark()
    {
        saveDetails()
        binding.atwtLooseFocus.requestFocus()
        binding.atwtWatermark.visibility = View.INVISIBLE
        val bmp = loadBitmapFromView(binding.atwtMainTweet)
        binding.atwtWatermark.visibility = View.VISIBLE

        if (bmp != null) {
            val name = binding.atwtLooseFocus.text.toString()
            name.let {
                if(it == "") {
                    saveImage(bmp,null
                    )
                } else {
                    saveImage(bmp,it)
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


            val name = binding.atwtName.text.toString()
            val username = binding.atwtUsername.text.toString()
            val bitmap = ((binding.atwtProfilePic.drawable) as BitmapDrawable).bitmap

            TwitterAccountDatabase.addToSharedPref(name, username, bitmap, this)
        }
        catch (e:java.lang.Exception)
        {
            e.printStackTrace()
        }
    }
    companion object {
        private const val TAG = "TweetWthText"
    }
}