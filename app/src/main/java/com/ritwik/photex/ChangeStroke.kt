package com.ritwik.photex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ritwik.photex.databinding.FragmentChangeStrokeBinding


class ChangeStroke(val fragment: CreateFragment) : Fragment() {
    var _binding: FragmentChangeStrokeBinding? = null
    val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChangeStrokeBinding.inflate(layoutInflater, container, false)
        val selectedItem = fragment.selectedItem!!
        var selectedColor = "#FFFFFF"
        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        binding.fcsColorRecycler.layoutManager = manager
        selectedItem!!.strokePaint?.let {
            val width = it.strokeWidth
            binding.fcsStrokeWidthField.setText(width.toString())
        }
        val colorRecycler = ColorRecycler(binding.fcsColorRecycler, context!!, false)
        { color ->
            selectedColor = color
            fragment.addStroke(2, color)
            fragment.reDrawBitmap()
        }
        colorRecycler.showRecycler()
        binding.fcsClearButton
            .setOnClickListener {
                fragment.addStroke(0, null)
                fragment.reDrawBitmap()
            }
        binding.fcsStrokeWidthUp
            .setOnClickListener {

                var width = if (binding.fcsStrokeWidthField.text.toString() != "") {
                    binding.fcsStrokeWidthField.text.toString().toInt() + 1
                } else {
                    2
                }
                binding.fcsStrokeWidthField.setText(width.toString())
                fragment.addStroke(width, selectedColor)

                fragment.reDrawBitmap()

            }
        binding.fcsStrokeWidthDown
            .setOnClickListener {

                var width = if (binding.fcsStrokeWidthField.text.toString() != "") {
                    if (binding.fcsStrokeWidthField.text.toString().toInt() - 1 >= 0) {
                        binding.fcsStrokeWidthField.text.toString().toInt() - 1
                    } else {
                        0
                    }

                } else {
                    2
                }
                binding.fcsStrokeWidthField.setText(width.toString())
                fragment.addStroke(width, selectedColor)

                fragment.reDrawBitmap()

            }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ChangeStroke"
    }
}