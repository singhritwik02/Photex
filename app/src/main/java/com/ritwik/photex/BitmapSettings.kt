package com.ritwik.photex

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.ritwik.photex.databinding.FragmentBitmapSettingsBinding

// TODO: Rename parameter arguments, choose names that match

class BitmapSettings(val layerP: Layer,val parentActivity:CreateMeme) : Fragment() {
    var _binding: FragmentBitmapSettingsBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBitmapSettingsBinding.inflate(layoutInflater)
        // setting the previous measures of the bars
        preSetBars(layerP)
        // setting the on seek changed listeners
        setSeekListeners(layerP)
        
        return binding.root
    }

    private fun setSeekListeners(layer: Layer) {
        // seek listeners for rotation bar
        binding.fbsRotationBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    p0?.let { seekbar ->
                        layer.view.rotation = seekbar.progress.toFloat()
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    Log.d(TAG, "onStartTrackingTouch: ")
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    Log.d(TAG, "onStopTrackingTouch: ")
                }

            }
        )
        // seek listener for size bar
        binding.fbsSizeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                p0?.let { seekBar ->
                    if (seekBar.progress >= 50) {
                        val tempW = layer.oWidth + 2*(seekBar.progress - 50)
                        val tempH = layer.oHeight + 2*(seekBar.progress - 50)
                        if (tempH > 0 && tempW > 0) {
                            layer.width = tempW
                            layer.height = tempH
                            val params = layer.view.layoutParams
                            params.width = layer.width
                            params.height = layer.height
                            layer.view.layoutParams = params
                            layer.view.requestLayout()
                        }

                    } else {
                        val tempW = layer.oWidth - 2*(50 - seekBar.progress )
                        val tempH = layer.oHeight - 2*(50 - seekBar.progress )
                        if (tempH > 0 && tempW > 0) {
                            layer.width = tempW
                            layer.height = tempH
                            val params = layer.view.layoutParams
                            params.width = layer.width
                            params.height = layer.height
                            layer.view.layoutParams = params
                            layer.view.requestLayout()
                        }
                    }


                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                Log.d(TAG, "onStartTrackingTouch: ")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                Log.d(TAG, "onStopTrackingTouch: ")
            }

        })
        // setting on seek listener for alpha button
        binding.fbsAlphaBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                p0?.let {seekbar->

                    (layer.view as ImageView).imageAlpha  = seekbar.progress
                    Log.d(TAG, "onProgressChanged: alpha = ${seekbar.progress.toFloat()}")
                    }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                Log.d(TAG, "onStartTrackingTouch: ")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                Log.d(TAG, "onStopTrackingTouch: ")
            }

        })
        binding.fbsDeleteIcon.setOnClickListener {
            parentActivity.layers.removeLayer()
        }
    }

    private fun preSetBars(layer: Layer) {
        val rotation = layer.view.rotation
        // setting the rotation of the rotation bar
        binding.fbsRotationBar.progress = rotation.toInt()
        val size = (layer.view as ImageView).width
        // difference between the original and the current image view
        val difference = layer.oWidth - size
        // setting the progress of the size bar
        binding.fbsSizeBar.progress = (50 + difference/2).toInt()
        binding.fbsAlphaBar.progress = (layer.view as ImageView).imageAlpha
    }

    override fun onResume() {
        Log.d(TAG, "onResume: ")
        preSetBars(layerP)
        setSeekListeners(layerP)
        super.onResume()
    }

    companion object {
        private const val TAG = "BitmapSettings"
    }
}