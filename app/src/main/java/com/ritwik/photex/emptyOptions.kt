package com.ritwik.photex

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ritwik.photex.databinding.FragmentEmptyOptionsBinding


class emptyOptions : Fragment() {
    var _binding:FragmentEmptyOptionsBinding? = null
    val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEmptyOptionsBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    companion object {
        private const val TAG = "emptyOptions"
    }
}