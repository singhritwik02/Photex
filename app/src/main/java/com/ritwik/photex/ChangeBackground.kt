package com.ritwik.photex

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.ritwik.photex.databinding.FragmentChangeBackgroundBinding


class ChangeBackground (val fragment: CreateFragment): Fragment() {
    var _binding: FragmentChangeBackgroundBinding? = null
    val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        
        _binding = FragmentChangeBackgroundBinding.inflate(layoutInflater, container, false)
        val item = fragment.selectedItem!!
        val mainBitmap = fragment.mainBitmap
        val progressF = (225 / 100.toFloat()) * item.backgroundAlpha
        val progress = progressF.toInt()
        binding.ptbAlphaSeekBar.progress = if (progress > 100) {
            Log.d(TAG, "setBackground: setting progress to $progress")
            progress
        } else if (progress < 0) {
            Log.d(TAG, "setBackground: setting progress to $progress")
            0
        } else {
            Log.d(TAG, "setBackground: setting progress to $progress")
            progress
        }
        if (item.backgroundMargins == null) {

            binding.ptbVerticalSeekBar.progress = 0
            binding.ptbHorizontalSeekBar.progress = 0
        } else {

            val xMargin = item.backgroundMargins!!.marginX
            val yMargin = item.backgroundMargins!!.marginY
            val progressH = (xMargin / mainBitmap.width.toFloat()) * 100
            val progressV = (yMargin / mainBitmap.height.toFloat()) * 100
            Log.d(TAG, "setBackground: Horizontal Progress = $progressH")
            Log.d(TAG, "setBackground: Vertical Progress = $progressV")
            binding.ptbHorizontalSeekBar.progress = progressH.toInt()
            binding.ptbVerticalSeekBar.progress = progressV.toInt()


        }


        binding.ptbClearButton.setOnClickListener {
            item.removeBackground()
            item.backgroundAlpha = 225
            fragment.reDrawBitmap()
            val progressF = (225 / 100.toFloat()) * item.backgroundAlpha
            val progress = progressF.toInt()
            binding.ptbAlphaSeekBar.progress = if (progress > 100) {
                Log.d(TAG, "setBackground: setting progress to $progress")
                progress
            } else if (progress < 0) {
                Log.d(TAG, "setBackground: setting progress to $progress")
                0
            } else {
                Log.d(TAG, "setBackground: setting progress to $progress")
                progress
            }
            if (item.backgroundMargins == null) {
                binding.ptbVerticalSeekBar.progress = 0
                binding.ptbHorizontalSeekBar.progress = 0
            } else {

                val xMargin = item.backgroundMargins!!.marginX
                val yMargin = item.backgroundMargins!!.marginY
                val progressH = (xMargin / mainBitmap.width.toFloat()) * 100
                val progressV = (yMargin / mainBitmap.height.toFloat()) * 100
                Log.d(TAG, "setBackground: Horizontal Progress = $progressH")
                Log.d(TAG, "setBackground: Vertical Progress = $progressV")
                binding.ptbHorizontalSeekBar.progress = progressH.toInt()
                binding.ptbVerticalSeekBar.progress = progressV.toInt()


            }
        }
        binding.ptbHorizontalSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    p0?.let {
                        val max = mainBitmap.width
                        val width = (max / 100.toFloat()) * it.progress
                        if (item.backgroundMargins == null) {
                            item.setBackground(width, 2f, null)
                            fragment.reDrawBitmap()
                        } else {
                            val y = item.backgroundMargins!!.marginY
                            item.setBackground(width, y, null)
                            fragment.reDrawBitmap()
                        }
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
        binding.ptbVerticalSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    p0?.let {
                        val max = mainBitmap.height
                        val height = (max / 100.toFloat()) * it.progress
                        if (item.backgroundMargins == null) {
                            item.setBackground(2f, height, null)
                            fragment.reDrawBitmap()
                        } else {
                            val x = item.backgroundMargins!!.marginX
                            item.setBackground(x, height, null)
                            fragment.reDrawBitmap()
                        }
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
        binding.ptbAlphaSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    p0?.let {
                        val alpha = (255 / 100.toFloat()) * it.progress

                        Log.d(TAG, "onProgressChanged: alpha = ${alpha.toInt()}")
                        item.backgroundAlpha = alpha.toInt()
                        fragment.reDrawBitmap()
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
        val colorRecycler = ColorRecycler(binding.ptbColorRecycler, context!!,false)
        { colorString ->
            if (item.backgroundMargins != null) {
                var x = item.backgroundMargins!!.marginX
                var y = item.backgroundMargins!!.marginY

                item.setBackground(x, y, colorString)
                fragment.reDrawBitmap()
            } else {
                item.setBackground(2f, 2f, colorString)
                fragment.reDrawBitmap()
            }
        }
        colorRecycler.showRecycler()
        fragment.reDrawBitmap()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "changeBackground"
    }
}