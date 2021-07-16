package com.ritwik.photex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ritwik.photex.databinding.FragmentMainMenuBinding

class MainMenuFragment(fragment: CreateFragment) : Fragment() {
    var _binding: FragmentMainMenuBinding? = null
    val binding get() = _binding!!
    val fragment = fragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        //        binding.pccSaveImage.setOnClickListener {
        binding.fmmEditText
            .setOnClickListener {
                if(fragment.selectedItem == null)
                {
                    Toast.makeText(context, "No item is selected", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if(fragment.selectedItem!!.type == "TEXT") {
                    fragment.menuFragmentManager.showTextOptions()
                }
                else
                {
//                    val bitmap = fragment.resizeBitmap(fragment.selectedItem!!.bitmap!!,100f)
//                    fragment.selectedItem!!.bitmap = bitmap
//                    fragment.reDrawBitmap()
                    fragment.showStickerResizePopup()
                }
            }
        binding.fmmTextSettings
            .setOnClickListener {
                fragment.showEditTextPopup("")
            }
        binding.fmmRotate
            .setOnClickListener {
                fragment.showRotatePopup()
            }
        binding.fmmDeleteItem
            .setOnClickListener {
                fragment.deleteItem()
            }
        binding.fmmAddWatermarkButton
            .setOnClickListener {
                fragment.showUsernames()
            }
        binding.fmmStickerSettings.setOnClickListener {
            fragment.stickerPopup.showPopup()
        }
        binding.fmmAlpha.setOnClickListener {
            fragment.showAlphaPopup()
        }
        binding.fmmAddImage.setOnClickListener {
            fragment.chooseImage(1024)

        }

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun changeToSticker() {
        if (binding.fmmEditTextLabel.text != "Resize") {
            binding.fmmEditText.animate().alpha(0f).setDuration(250).withEndAction {
                binding.fmmEditTextImage.setImageResource(R.drawable.resize_icon)
                binding.fmmEditTextLabel.text = "Resize"
                binding.fmmEditText.animate().alpha(1f).duration = 250
            }
        }
    }

    fun changeToText() {
        if (binding.fmmEditTextLabel.text != "Edit") {
            binding.fmmEditText.animate().alpha(0f).setDuration(250).withEndAction {
                binding.fmmEditTextImage.setImageResource(R.drawable.edit_text)
                binding.fmmEditTextLabel.text = "Edit"
                binding.fmmEditText.animate().alpha(1f).duration = 250
            }
        }
    }

    companion object {
        private const val TAG = "MainMenuFragment"
    }
}