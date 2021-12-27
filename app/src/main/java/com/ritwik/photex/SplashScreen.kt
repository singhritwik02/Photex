package com.ritwik.photex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.ritwik.photex.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    private lateinit var binding:ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val timer = object: CountDownTimer(3000,1000)
        {
            override fun onTick(p0: Long) {
                Log.d(TAG, "onTick: tick")
            }

            override fun onFinish() {
                showAnimation {
                    val intent = Intent(this@SplashScreen, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
            }

        }
        timer.start()
    }
    private fun showAnimation(function: () -> Unit) {
        binding.ssMainLogo.animate().scaleX(2f).scaleY(2f)
            .setInterpolator(DecelerateInterpolator()).setDuration(200).withEndAction {
                binding.ssMainLogo.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(100)
                    .setInterpolator(AccelerateInterpolator()).withEndAction {
                        function()
                    }
            }
    }
    private fun showAnimationTranslate(function: () -> Unit)
    {
        val displayMetrics = resources.displayMetrics
        binding.ssMainLogo.animate().translationY(-20f).setInterpolator(DecelerateInterpolator()).setDuration(200).withEndAction {
            binding.ssMainLogo.animate().translationY((displayMetrics.heightPixels+binding.ssMainLogo.height).toFloat()).setInterpolator(AccelerateInterpolator()).setDuration(200).withEndAction {
                function()
            }
        }
    }
    companion object
    {
        private const val TAG = "SplashScreen"
    }
}