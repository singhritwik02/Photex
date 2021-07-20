package com.ritwik.photex

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ritwik.photex.databinding.FragmentHomeBinding
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var containerId: Int = 0
    private lateinit var fragment: Fragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        containerId = container?.id!!
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.fhBlankButton
            .setOnClickListener {

                if(!checkPermission())
                {
                    return@setOnClickListener
                }
                val bundle = Bundle()
                bundle.putString("MODE", "BLANK")
                showCreateFragment(bundle)


            }
        binding.fhGalleryButton
            .setOnClickListener {
                if(!checkPermission())
                {
                    return@setOnClickListener
                }
                val bundle = Bundle()
                bundle.putString("MODE", "GALLERY")
                showCreateFragment(bundle)
            }

        UnityAds.initialize(context, "4218265", false);
        val banner = setUpTopBanner()
        binding.fhTemplateButton.setOnClickListener {
            if(!checkPermission())
            {
                return@setOnClickListener
            }
            activity?.let {
                if (container != null) {
                    it.supportFragmentManager.beginTransaction()
                        .replace(container.id, Template(), "TEMPLATE_FRAGMENT").addToBackStack("")
                        .commit()
                    banner.destroy()

                }
            }

        }

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

    fun showCreateFragment(bundle: Bundle) {
        activity?.let {
            if (containerId != 0) {
                fragment = CreateFragment()
                fragment.arguments = bundle
                it.supportFragmentManager.beginTransaction()
                    .replace(containerId, fragment, "CREATE_FRAGMENT").addToBackStack("")
                    .commit()
            }
        }
    }

    fun checkPermission() :Boolean{
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}