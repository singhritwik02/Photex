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
        val mainBitmap = fragment.mainBitmap
        val progressF = (225 / 100.toFloat()) * fragment.selectedItem!!.backgroundAlpha
        Log.d(TAG, "onCreateView: background alpha = ${fragment.selectedItem!!.backgroundAlpha}")
        val progress = progressF.toInt()
        binding.ptbRoundedCornersButtton.isChecked  = if(fragment.selectedItem!!.backgroundMargins!=null)
        {
            fragment.selectedItem!!.backgroundMargins!!.rounded
        }
        else
        {
            false
        }
        binding.pcbAlphaSeekBar.progress = if (progress > 100) {
            Log.d(TAG, "setBackground: setting progress to $progress")
            progress
        } else if (progress < 0) {
            Log.d(TAG, "setBackground: setting progress to $progress")
            0
        } else {
            Log.d(TAG, "setBackground: setting progress to $progress")
            progress
        }
        if (fragment.selectedItem!!.backgroundMargins == null) {

          binding.ptbHorizontalSeekBar.progress = 0
        } else {

            val xMargin = fragment.selectedItem!!.backgroundMargins!!.marginX
            val yMargin = fragment.selectedItem!!.backgroundMargins!!.marginY
            val progressH = (xMargin / mainBitmap.width.toFloat()) * 100
            val progressV = (yMargin / mainBitmap.height.toFloat()) * 100
            Log.d(TAG, "setBackground: Horizontal Progress = $progressH")
            Log.d(TAG, "setBackground: Vertical Progress = $progressV")
            binding.ptbHorizontalSeekBar.progress = progressH.toInt()
        }
        binding.pcbClearButton.setOnClickListener {
            fragment.selectedItem!!.removeBackground()
            fragment.selectedItem!!.backgroundAlpha = 225
            fragment.reDrawBitmapRefined()
            val progressF = (225 / 100.toFloat()) * fragment.selectedItem!!.backgroundAlpha
            val progress = progressF.toInt()
            binding.pcbAlphaSeekBar.progress = if (progress > 100) {
                Log.d(TAG, "setBackground: setting progress to $progress")
                progress
            } else if (progress < 0) {
                Log.d(TAG, "setBackground: setting progress to $progress")
                0
            } else {
                Log.d(TAG, "setBackground: setting progress to $progress")
                progress
            }
            if (fragment.selectedItem!!.backgroundMargins == null) {
                binding.ptbHorizontalSeekBar.progress = 0
            } else {

                val xMargin = fragment.selectedItem!!.backgroundMargins!!.marginX
                val yMargin = fragment.selectedItem!!.backgroundMargins!!.marginY
                val progressH = (xMargin / mainBitmap.width.toFloat()) * 100
                val progressV = (yMargin / mainBitmap.height.toFloat()) * 100
                Log.d(TAG, "setBackground: Horizontal Progress = $progressH")
                Log.d(TAG, "setBackground: Vertical Progress = $progressV")
                binding.ptbHorizontalSeekBar.progress = progressH.toInt()


            }
        }
        binding.fcbLineSpacingBar.setOnSeekBarChangeListener(
            object :SeekBar.OnSeekBarChangeListener
            {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    p0?.let {
                        val spacing = it.progress * 0.001
                        fragment.selectedItem!!.lineSpacing.setSpacing(spacing.toFloat())
                        fragment.reDrawBitmapRefined()
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
        binding.ptbHorizontalSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    p0?.let {
                        val max = mainBitmap.width
                        val width = (max / 100.toFloat()) * it.progress
                        if (fragment.selectedItem!!.backgroundMargins == null) {
                            fragment.selectedItem!!.setBackground(width, 2f, null)
                            fragment.reDrawBitmapRefined()
                        } else {
                            val y = fragment.selectedItem!!.backgroundMargins!!.marginY
                            fragment.selectedItem!!.setBackground(width, y, null)
                            fragment.reDrawBitmapRefined()
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
        binding.ptbRoundedCornersButtton.setOnCheckedChangeListener { compoundButton, b ->
            if(fragment.selectedItem!!.backgroundMargins!=null)
            {
                fragment.selectedItem!!.backgroundMargins!!.rounded = b
            }
            else
            {
                fragment.selectedItem!!.setBackground(2f,2f,null)
                fragment.selectedItem!!.backgroundMargins!!.rounded = b
            }
            fragment.reDrawBitmapRefined()

        }
        binding.pcbAlphaSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    p0?.let {
                        val alpha = (255 / 100.toFloat()) * it.progress

                        Log.d(TAG, "onProgressChanged: alpha = ${alpha.toInt()}")
                        fragment.selectedItem!!.backgroundAlpha = alpha.toInt()
                        fragment.reDrawBitmapRefined()
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
            if (fragment.selectedItem!!.backgroundMargins != null) {
                var x = fragment.selectedItem!!.backgroundMargins!!.marginX
                var y = fragment.selectedItem!!.backgroundMargins!!.marginY

                fragment.selectedItem!!.setBackground(x, y, colorString)
                fragment.reDrawBitmapRefined()
            } else {
                fragment.selectedItem!!.setBackground(2f, 2f, colorString)
                fragment.reDrawBitmapRefined()
            }
        }
        colorRecycler.showRecycler()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    companion object {
        private const val TAG = "changeBackground"
    }

}