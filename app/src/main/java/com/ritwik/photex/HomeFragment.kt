package com.ritwik.photex

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ritwik.photex.databinding.FragmentHomeBinding
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var containerId: Int = 0
    private lateinit var fragment: Fragment
    private var memeWallStatus = MemeWallStatus()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        containerId = container?.id!!
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        // getting the notification string if exists
        arguments?.let {
            val notificationString = it["NOTIFICATION"].toString()
            if(notificationString.equals("TEMPLATE"))
            {
                val intent = Intent(context!!, CreateMeme::class.java)
                intent.putExtra("STYLE", "UPPER_TEXT")
                intent.putExtra("NOTIFICATION","TEMPLATE")
                startActivity(intent)
            }

        }
        binding.fhBlankButton
            .setOnClickListener {

                if (!checkPermission()) {
                    return@setOnClickListener
                }
//                val bundle = Bundle()
//                bundle.putString("MODE", "BLANK")
//                showCreateFragment(bundle)
                val intent = Intent(context!!, CreateMeme::class.java)
                intent.putExtra("STYLE", "TEXT_ONLY")
                startActivity(intent)


            }
        binding.fhTwitterMainLayout.setOnClickListener {
            if (!checkPermission()) {
                return@setOnClickListener
            }
            startActivity(Intent(context, TweetWithImage::class.java))
        }

        binding.fhTemplateButton.setOnClickListener {
            if (!checkPermission()) {
                return@setOnClickListener
            }
            startActivity(Intent(context, CreateMeme::class.java))


        }

        val versionName = getVersionCode()
        getLatestVersionName { latestVersion ->
            if (!versionName.equals(latestVersion)) {
                binding.fhUpdateButton.visibility = View.VISIBLE
            } else {
                binding.fhUpdateButton.visibility = View.GONE
            }

        }
        binding.fhUpdateButton.setOnClickListener {
            val packageName = "com.ritwik.photex"
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }
        Log.d(TAG, "onCreateView: version Name = $versionName")

        return binding.root
    }

    fun setUpTopBanner(): BannerView {
        val topBanner = BannerView(activity!!, "Home_Screen_Banner", UnityBannerSize(320, 50))
        val bannerListener = topBannerListener()
        topBanner.listener = bannerListener
        topBanner.load()
        (binding.fhBanner as ViewGroup).addView(topBanner)
        return topBanner
    }


    inner class topBannerListener : BannerView.IListener {
        override fun onBannerLoaded(p0: BannerView?) {
            Log.d(TAG, "onBannerLoaded: ")
        }

        override fun onBannerClick(p0: BannerView?) {
            Log.d(TAG, "onBannerClick: ")
        }

        override fun onBannerFailedToLoad(p0: BannerView?, p1: BannerErrorInfo?) {
            Log.d(TAG, "onBannerFailedToLoad: ${p1?.errorMessage}")
        }

        override fun onBannerLeftApplication(p0: BannerView?) {
            Log.d(TAG, "onBannerLeftApplication: ")
        }

    }

    companion object {
        private const val TAG = "HomeFragment"
    }


    fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                context!!,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context!!,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Permission required to proceed!!", Toast.LENGTH_SHORT).show()
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                102
            )
            return false
        }
        return true
    }

    inner class MemeWallStatus {
        var message = "Coming Soon"
        var status = "AWAITED"


    }
    override fun onDestroyView() {
        super.onDestroyView()
    }

    fun getVersionCode(): String {
        return try {
            val packageInfo = context!!.packageManager.getPackageInfo(context!!.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
            "1"
        }
    }

    fun getLatestVersionName(function: (String) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("LATEST_VERSION_NAME").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val versionName = snapshot.value.toString()
                    function(versionName)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: ")
                }

            }
        )

    }
}