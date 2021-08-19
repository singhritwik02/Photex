package com.ritwik.photex

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ritwik.colortest.ColorDatabase
import com.ritwik.photex.databinding.FragmentChangeColorBinding
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class ChangeColorFragment (val fragment:CreateFragment): Fragment() {
   var _binding:FragmentChangeColorBinding? = null
    val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChangeColorBinding.inflate(layoutInflater,container,false)
        val selectedItem = fragment.selectedItem!!
        val context = context!!
        val presets = Presets(context)
        var selectedColor = "#000000"
        val colorRecycler = ColorRecycler(binding.fccColorRecycler, context,false)
        { color ->
            selectedColor = color
            selectedItem.paint.color = Color.parseColor(selectedColor)
            presets.preTextColor = selectedColor
            fragment.reDrawBitmapRefined()
        }
        colorRecycler.showRecycler()
        binding.fccChooseColor
            .setOnClickListener {
                ColorPickerDialog.Builder(context)
                    .setTitle("Choose Color")
                    .setPreferenceName("ChooseColor")
                    .setPositiveButton("Save",
                        ColorEnvelopeListener { envelope, fromUser ->
                            Log.d(TAG, "showChooseColorPopup: ")
                            selectedColor = "#${envelope.hexCode}"
                            selectedItem.paint.color = Color.parseColor(selectedColor)
                            presets.preTextColor = selectedColor
                            fragment.reDrawBitmapRefined()

                        })
                    .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                    .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                    .show()
            }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ChangeColorFragment"
    }
}