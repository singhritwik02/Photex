package com.ritwik.photex

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ritwik.photex.databinding.PopupReferralBinding

class ReferralPopup(val context: Context,val activity:Activity) {
    private lateinit var window: PopupWindow
    private lateinit var binding: PopupReferralBinding
    private var availablePoints: Int = 0
    private lateinit var rewarededAd: RewarededAd
    fun showPopup(function: () -> Unit) {
        binding = PopupReferralBinding.inflate(LayoutInflater.from(context))
        val displayMetrics = context.resources.displayMetrics
        window = PopupWindow(
            binding.root,
            (displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        window.elevation = 100f
        window.showAtLocation(binding.root, Gravity.CENTER, 0, 0)

        // setting the number of points available
        Referral.getYourPoints(context)
        { points ->
            binding.prCurrentPointsCount.text = "$points"
            availablePoints = points.toInt()
            if (availablePoints > 0) {
                binding.prRemoveWatermarkButton.setTextColor(Color.BLACK)
                binding.prRemoveWatermarkButton.isEnabled = true
            } else {
                binding.prRemoveWatermarkButton.setTextColor(Color.GRAY)
                binding.prRemoveWatermarkButton.isEnabled = false
            }

        }
        binding.prRewardButton.setOnClickListener {

            Toast.makeText(context, "Loading rewared", Toast.LENGTH_SHORT).show()
            window.dismiss()
            if(!this::rewarededAd.isInitialized) {
                rewarededAd = RewarededAd(activity,function)
            }
            rewarededAd.initialiseAd()
            rewarededAd.displayAd()


        }
        binding.prRemoveWatermarkButton.setOnClickListener {
            function()
            val referal = Referral(context)
            referal.deductPoint()
            window.dismiss()
        }
        binding.prShareButton.setOnClickListener {
            window.dismiss()
            Referral.showShareMenu(context)
        }
    }
}