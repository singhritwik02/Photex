package com.ritwik.photex

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ritwik.photex.databinding.FragmentChangeTextBinding

class ChangeTextFragment(val fragment: CreateFragment) : Fragment() {
    var _binding: FragmentChangeTextBinding? = null
    val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChangeTextBinding.inflate(layoutInflater,container,false)
        val selectedItem = fragment.selectedItem!!
        binding.fctEditTextField.hint = selectedItem.text
        binding.fctDoneButton
            .setOnClickListener {
                selectedItem.text = binding.fctEditTextField.text.toString()
                binding.fctEditTextField.setText("")
                binding.fctEditTextField.hint = selectedItem.text
                fragment.reDrawBitmap()
            }

        return binding.root
    }

    companion object {
        private const val TAG = "ChangeTextFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}