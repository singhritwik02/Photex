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
import com.ritwik.photex.databinding.ActivityBasicTweetBinding
import com.unity3d.ads.UnityAds
import java.util.*

class BasicTweet : AppCompatActivity() {
    private lateinit var binding: ActivityBasicTweetBinding
    private lateinit var referralPopup: ReferralPopup
    private val profileImageCode = 1024
    private var verified = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBasicTweetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        UnityAds.initialize(this, "4218265", false)
        try {
            val details = TwitterAccountDatabase.getDetails(this)
            binding.abtName.setText(details[0])
            binding.abtUsername.setText(details[1])
            TwitterAccountDatabase.getProfileThumbnail(this)?.let {
                binding.abtProfilePic.setImageBitmap(it)
            }


        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        binding.abtSave.setOnClickListener {

            binding.abtLooseFocus.requestFocus()
            val bmp = loadBitmapFromView(binding.abtMainTweet)
            if (bmp != null) {
                val name = binding.abtLooseFocus.text.toString()
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
        binding.abtProfilePic.setOnClickListener {
            changeProfileImage()
        }

        binding.abtVerification.setOnClickListener {
            changeVerification()
        }

        binding.abtWatermarkButton.setOnClickListener {
            if (!this::referralPopup.isInitialized) {
                referralPopup = ReferralPopup(this, this)
            }
            referralPopup.showPopup()
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
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
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
                binding.abtProfilePic.setImageURI(it)
            }
        }
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
            binding.abtVerification.animate().alpha(0.2f).setDuration(150).withEndAction {
                verified = false
                binding.abtVerificationImage.setImageResource(R.drawable.twitter_not_verified)
                binding.abtVerification.animate().alpha(1f).setDuration(150)
                binding.abtVerifiedIcon.visibility = View.GONE
            }
        } else {
            binding.abtVerification.animate().alpha(0.2f).setDuration(150).withEndAction {
                verified = true
                binding.abtVerificationImage.setImageResource(R.drawable.twitter_verified)
                binding.abtVerification.animate().alpha(1f).setDuration(150)
                binding.abtVerifiedIcon.visibility = View.VISIBLE
            }

        }
    }

    private fun saveWithoutWatermark() {
        saveDetails()
        binding.abtLooseFocus.requestFocus()
        binding.abtWatermark.visibility = View.INVISIBLE
        val bmp = loadBitmapFromView(binding.abtMainTweet)
        binding.abtWatermark.visibility = View.VISIBLE

        if (bmp != null) {
            val name = binding.abtLooseFocus.text.toString()
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
        val unityInterstital = UnityInterstital(this, this)
        unityInterstital.initialiseAd()
        unityInterstital.displayAd()
    }

    private fun saveDetails() {
        try {


            val name = binding.abtName.text.toString()
            val username = binding.abtUsername.text.toString()
            val bitmap = ((binding.abtProfilePic.drawable) as BitmapDrawable).bitmap

            TwitterAccountDatabase.addToSharedPref(name, username, bitmap, this)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    companion object
    {
        private const val TAG = "BasicTweet"
    }
}

