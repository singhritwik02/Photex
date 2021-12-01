package com.ritwik.photex

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ritwik.photex.databinding.FragmentMemeStyleBinding


class MemeStyleFragment : Fragment() {
    private var _binding: FragmentMemeStyleBinding? = null
    val binding get() = _binding!!
    val themeImageArray = arrayOf(
        R.drawable.upper_text_light,
        R.drawable.bottom_text_light,
        R.drawable.both_text_light
    )
    val darkThemeArray =
        arrayOf(R.drawable.upper_text_dark, R.drawable.bottom_text_dark, R.drawable.both_text_dark)
    private var num = -1
    private var mode = "LIGHT"
    private var style = "UPPER_TEXT"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMemeStyleBinding.inflate(layoutInflater, container, false)
        arguments?.let { arguments ->
            num = arguments["num"]!! as Int
            binding.fmsThemeImage.setImageResource(themeImageArray[num as Int])
        }
        style = if (num == 0) {
            binding.fmsStyleLabel.setText("Top Caption with image")
            "UPPER_TEXT"
        } else if (num == 1) {
            binding.fmsStyleLabel.setText("Bottom Caption with image")
            "BOTTOM_TEXT"
        } else if (num == 2) {
            binding.fmsStyleLabel.setText("Top and Bottom Caption with image")
            "BOTH_TEXT"
        } else {
            "BOTTOM_TEXT"
        }

        binding.fmsThemeGroup.setOnCheckedChangeListener { radioGroup, id ->
            if (id == R.id.fms_ThemeModeLight) {
                binding.fmsThemeImage.setImageResource(themeImageArray[num])
                binding.root.setBackgroundColor(Color.WHITE)
                binding.fmsStyleLabel.setTextColor(Color.BLACK)
                binding.fmsThemeLabel.setTextColor(Color.BLACK)
                mode = "LIGHT"
                binding.fmsThemeModeLight.setTextColor(Color.BLACK)
                binding.fmsThemeModeDark.setTextColor(Color.BLACK)
            } else if (id == R.id.fms_ThemeModeDark) {
                binding.fmsThemeImage.setImageResource(darkThemeArray[num])
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(
                        context!!,
                        R.color.matte_black_light
                    )
                )
                binding.fmsStyleLabel.setTextColor(Color.WHITE)
                binding.fmsThemeLabel.setTextColor(Color.WHITE)
                binding.fmsThemeModeLight.setTextColor(Color.WHITE)
                binding.fmsThemeModeDark.setTextColor(Color.WHITE)

                mode = "DARK"
            }
        }
        binding.fmsNextButton.setOnClickListener {
            val intent = Intent(context!!, CreateMeme::class.java)
            intent.putExtra("STYLE", style)
            intent.putExtra("MODE", mode)

            startActivity(intent)
            activity?.finish()
        }
        return binding.root
    }

    companion object {
        private const val TAG = "MemeStyleFragment"
    }
}