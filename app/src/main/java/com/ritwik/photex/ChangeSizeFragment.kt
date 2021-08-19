package com.ritwik.photex

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ritwik.photex.databinding.FragmentChangeSizeBinding


class ChangeSizeFragment (val fragment: CreateFragment): Fragment() {
   var _binding:FragmentChangeSizeBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChangeSizeBinding.inflate(layoutInflater,container,false)
        val selectedItem = fragment.selectedItem!!
        val paint = fragment.selectedItem!!.paint
        val presets = Presets(context!!)
        binding.fcsField.setText(paint.textSize.toString())

        binding.fcsIncrease.setOnClickListener {
            val sizeInFloat = paint.textSize + 5f
            binding.fcsField.setText(sizeInFloat.toString())

            paint.textSize = sizeInFloat
            presets.preSize = selectedItem!!.paint.textSize
            fragment.reDrawBitmapRefined()

        }
        binding.fcsDecrease.setOnClickListener {
            val sizeInFloat = paint.textSize - 5f
            binding.fcsField.setText(sizeInFloat.toString())
            paint.textSize = sizeInFloat
            presets.preSize = selectedItem!!.paint.textSize
            fragment.reDrawBitmapRefined()

        }
        return binding.root
    }

    companion object {
        private const val TAG = "ChangeSizeFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}