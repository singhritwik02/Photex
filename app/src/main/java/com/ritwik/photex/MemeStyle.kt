package com.ritwik.photex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Fade
import com.ritwik.photex.databinding.ActivityMemeStyleBinding

class MemeStyle : AppCompatActivity() {
    private lateinit var binding:ActivityMemeStyleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemeStyleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val adapter = PagerAdapter(supportFragmentManager)
        binding.amsViewPager.setPageTransformer(false,PageTransformAnimation())
        binding.amsViewPager.adapter = adapter
        window.exitTransition = Fade()
    }
}