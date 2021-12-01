package com.ritwik.photex

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.*
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ritwik.photex.databinding.FragmentTextSettingsBinding
import com.ritwik.photex.databinding.PopupBackgroundOptionsBinding
import com.ritwik.photex.databinding.PopupShowFontsBinding
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlin.math.roundToInt


class TextSettings(
    layer: Layer, private var parentActivity: CreateMeme,

    ) : Fragment() {
    var _binding: FragmentTextSettingsBinding? = null
    val binding get() = _binding!!
    var textUnit = -1
    private val layer = layer
    private lateinit var scrollView: CustomScrollView
    private lateinit var textView: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTextSettingsBinding.inflate(layoutInflater, container, false)
        val max = 100
        val min = 1
        val total = max - min
        textView = layer.view as TextView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            textUnit = textView.textSizeUnit
        }
        Log.d(TAG, "onCreateView: ${textView.textSize}")
        val presets = Presets(context!!)
        val size = textView.textSize / 2
        if (textView.text != "")
            binding.ftsTextPreview.setText("${textView.text}")
        else
            binding.ftsTextPreview.setText("Preview")
        val tempColor = textView.currentTextColor

        //   setColors(tempColor,getHexFromInt(tempColor))

        val colorHex = getHexFromInt(textView.currentTextColor)
        binding.ftsTextColorCode.setText(colorHex)
        binding.ftsSizeBar.progress = size.roundToInt()
        if(textView.typeface.isBold)
        {
            setBoldSelected(true)
        }
        else
        {
            setBoldSelected(false)
        }
        if(textView.typeface.isItalic)
        {
            setItalicSelected(true)
        }
        else
        {
            setItalicSelected(false)
        }
        scrollView = binding.ftsScroll as CustomScrollView
        binding.ftsTextColorButton.setOnClickListener {
            ColorPickerDialog.Builder(context)
                .setTitle("Choose Color")
                .setPreferenceName("ChooseColor")
                .setPositiveButton("Save",
                    ColorEnvelopeListener { envelope, fromUser ->
                        Log.d(TAG, "showChooseColorPopup: ")
                        val colorCode = "#${envelope.hexCode}"
                        val color = Color.parseColor(colorCode)
                        setColors(color, colorCode)
                        presets.preTextColor = colorCode

                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .show()
        }
        binding.ftsSizeBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    p0?.let { seekbar ->
                        textView.textSize = seekbar.progress.toFloat()
                        presets.preSize = textView.textSize

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



        binding.ftsRotationBar.customScrollView = scrollView
        binding.ftsRotationBar.setOnSeekBarChangeListener { seekbar, curValue ->
            textView.rotation = curValue.toFloat()
        }

        binding.ftsRotationBar.setOnLongClickListener {
            binding.ftsRotationBar.curProcess = 0
            return@setOnLongClickListener true
        }
        binding.ftsRotationResetButton.setOnClickListener {
            binding.ftsRotationBar.curProcess = 0
            textView.rotation = 0f
        }

        binding.ftsEditButton.setOnClickListener {
            parentActivity.showTextPopup(textView.text.toString())
            {
                textView.setText(it)
                binding.ftsTextPreview.setText(it)
            }
        }

        binding.ftsBackgroundButton.setOnClickListener {
            binding.ftsBackgroundButton.animate().alpha(0.2f).setDuration(200).withEndAction {
                changeBackgroundOption()
                binding.ftsBackgroundButton.animate().alpha(1f).setDuration(200)
            }
        }
        binding.ftsBackgroundButton.setOnLongClickListener {
            showBackgroundOptions()
            return@setOnLongClickListener true
        }
        binding.ftsStretched.setOnCheckedChangeListener { compoundButton, checked ->
            if (checked) {
                val params = textView.layoutParams
                params.width = FrameLayout.LayoutParams.MATCH_PARENT
                textView.layoutParams = params
                textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                textView.requestLayout()
            } else {
                val params = textView.layoutParams
                params.width = FrameLayout.LayoutParams.WRAP_CONTENT
                textView.gravity = Gravity.CENTER_HORIZONTAL
                textView.layoutParams = params
                textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

                textView.requestLayout()
            }
        }
        binding.ftsShadowButton.setOnClickListener {

            it.animate().alpha(0.2f).setDuration(150).withEndAction {
                it.animate().alpha(1f).setDuration(150)
                setShadow()

            }
            binding.ftsShadowButton.setOnLongClickListener {
                ColorPickerDialog.Builder(context)
                    .setTitle("Choose Color")
                    .setPreferenceName("ChooseColor")
                    .setPositiveButton("Save",
                        ColorEnvelopeListener { envelope, fromUser ->
                            Log.d(TAG, "showChooseColorPopup: ")
                            val colorCode = "#${envelope.hexCode}"
                            val color = Color.parseColor(colorCode)
                            setShadow(color)


                        })
                    .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                    .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                    .show()
                return@setOnLongClickListener true
            }


        }
        // checking for background

        if (layer.isRemovable) {

            if (layer.backgroundColorInt != 0) {
                textView.setBackgroundColor(layer.backgroundColorInt)
            } else {
                Log.d(TAG, "onCreateView: layer background color = 0")
            }
            binding.ftsRotationBar.curProcess = textView.rotation.toInt()
        } else {
            binding.ftsBackgroundButton.visibility = View.GONE
            binding.ftsRotationBarLayout.visibility = View.GONE
            binding.ftsStretched.visibility = View.GONE
            binding.ftsDeleteButton.setImageResource(R.drawable.clear_text_icon)
            binding.ftsRotateRightButton.visibility = View.GONE
        }
        if (layer.backgroundColorInt != 0) {
            binding.ftsBackgroundButton.setCardBackgroundColor(layer.backgroundColorInt)
        }
        when (layer.alignment) {
            Layer.ALIGNMENT_LEFT -> {
                binding.ftsAlignmentAnimation.setImageDrawable(
                    ContextCompat.getDrawable(
                        context!!,
                        R.drawable.left_to_center
                    )
                )
            }
            Layer.ALIGNMENT_CENTER -> {
                binding.ftsAlignmentAnimation.setImageDrawable(
                    ContextCompat.getDrawable(
                        context!!,
                        R.drawable.center_to_right
                    )
                )
            }
            Layer.ALIGNMENT_RIGHT -> {
                binding.ftsAlignmentAnimation.setImageDrawable(
                    ContextCompat.getDrawable(
                        context!!,
                        R.drawable.right_to_left
                    )
                )
            }
        }
        binding.ftsRotateRightButton.setOnClickListener {
            rotateRight()
        }
        binding.ftsAlignmentButton.setOnClickListener {
            when (layer.alignment) {
                Layer.ALIGNMENT_LEFT -> {
                    binding.ftsAlignmentAnimation.setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!,
                            R.drawable.left_to_center
                        )
                    )
                    (binding.ftsAlignmentAnimation.drawable as Animatable).start()
                    layer.alignment = Layer.ALIGNMENT_CENTER
                    textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    textView.gravity = Gravity.CENTER
                }
                Layer.ALIGNMENT_CENTER -> {
                    binding.ftsAlignmentAnimation.setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!,
                            R.drawable.center_to_right
                        )
                    )
                    (binding.ftsAlignmentAnimation.drawable as Animatable).start()
                    layer.alignment = Layer.ALIGNMENT_RIGHT
                    textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                    textView.gravity = Gravity.START
                }
                Layer.ALIGNMENT_RIGHT -> {
                    binding.ftsAlignmentAnimation.setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!,
                            R.drawable.right_to_left
                        )
                    )
                    (binding.ftsAlignmentAnimation.drawable as Animatable).start()
                    layer.alignment = Layer.ALIGNMENT_LEFT
                    textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    textView.gravity = Gravity.END
                }
            }
        }
        binding.ftsDeleteButton.setOnClickListener {
            if (!layer.isRemovable) {
                textView.setText("Tap to edit text")
                binding.ftsTextPreview.setText("Tap to edit text")
                parentActivity.showNoOptionSelected()
            } else {
                parentActivity.layers.removeLayer()
                parentActivity.showNoOptionSelected()
            }
        }
        binding.ftsBackgroundColor.setOnClickListener {
            ColorPickerDialog.Builder(context)
                .setTitle("Choose Color")
                .setPreferenceName("ChooseColor")
                .setPositiveButton("Save",
                    ColorEnvelopeListener { envelope, fromUser ->
                        Log.d(TAG, "showChooseColorPopup: ")
                        val colorCode = "#${envelope.hexCode}"
                        val color = Color.parseColor(colorCode)
                        changeBackgroundOption(color)
                        binding.ftsTransformationLayout.finishTransform()


                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .show()
        }

        binding.ftsFontNameButton.setOnClickListener {
            showFontSelectionPopup()
        }
        binding.ftsStyleBold.setOnClickListener {
            toggleBold()
        }
        binding.ftsStyleItalic.setOnClickListener {
            toggleItalic()
        }
        return binding.root
    }

    private fun changeShadow() {
        textView.setShadowLayer(10f, 5f, 5f, textView.currentTextColor)
    }

    private fun setShadow() {

        if (textView.shadowColor == 0) {
            textView.setShadowLayer(10f, 5f, 5f, textView.currentTextColor)
            binding.ftsShadowButton.setCardBackgroundColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.colorBlueType1
                )
            )
        } else {
            textView.setShadowLayer(0f, 0f, 0f, 0)
            binding.ftsShadowButton.setCardBackgroundColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.matte_black_light
                )
            )
        }
    }

    private fun setShadow(color: Int) {


        textView.setShadowLayer(10f, 5f, 5f, color)
        binding.ftsShadowButton.setCardBackgroundColor(
            ContextCompat.getColor(
                context!!,
                R.color.colorBlueType1
            )
        )
    }

    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
    }

    fun resetViews(newText: TextView) {
        textView = newText


    }

    private fun showFontSelectionPopup() {
        val fontBinding = PopupShowFontsBinding.inflate(layoutInflater)
        val window = PopupWindow(
            fontBinding.root,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            true
        )
        window.showAtLocation(fontBinding.root, Gravity.CENTER, 0, 0)
        val fontDatabase = FontDatabase(context!!)
        var offlineFonts = fontDatabase.getOfflineFonts()

        class OfflineFonts {
            var recyclerView: RecyclerView = fontBinding.psfOfflineFontsRecycler
            val presets = Presets(context!!)

            constructor() {
                recyclerView.layoutManager = LinearLayoutManager(context)
            }

            private lateinit var adapter: RecyclerView.Adapter<ViewHolder>
            fun showRecycler() {
                adapter = object : RecyclerView.Adapter<ViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                        val view = LayoutInflater.from(context)
                            .inflate(R.layout.single_offline_font, parent, false)
                        return ViewHolder(view)
                    }

                    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                        holder.setIsRecyclable(false)

                        holder.setFontName(offlineFonts[position])
                        holder.setTypeFace(offlineFonts[position])
                        holder.itemView.setOnClickListener {

                            val file =
                                context!!.getExternalFilesDir("${Environment.DIRECTORY_DOCUMENTS}/Fonts/${offlineFonts[position]}")
                            file?.let {
                                if (it.exists()) {
                                    (layer.view as TextView).setTypeface(Typeface.createFromFile(it))
                                    presets.preFont = it.name
                                    layer.baseTypeface = Typeface.createFromFile(it)
                                } else {
                                    Toast.makeText(context!!, "Cannot set Font", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                            window.dismiss()
                        }
                    }

                    override fun getItemCount(): Int {
                        return offlineFonts.size
                    }


                }
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()

            }

            fun updateRecycler() {
                offlineFonts = fontDatabase.getOfflineFonts()
                if (!this::adapter.isInitialized) {
                    showRecycler()
                } else {
                    adapter.notifyDataSetChanged()
                }
            }

            private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                private val fontName = itemView.findViewById<TextView>(R.id.soff_FontName)
                fun setFontName(name: String) {
                    val editedName = name.substring(0, (name.length - 4))
                    fontName.text = editedName
                }

                fun setTypeFace(name: String) {
                    val file =
                        context!!.getExternalFilesDir("${Environment.DIRECTORY_DOCUMENTS}/Fonts/${name}")
                    file?.let {
                        if (it.exists()) {
                            fontName.setTypeface(Typeface.createFromFile(it))
                        } else {
                            Toast.makeText(
                                context!!,
                                "File $name does not exist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

        }

        val offlineFontsRecycler = OfflineFonts()
        offlineFontsRecycler.showRecycler()
        offlineFontsRecycler.updateRecycler()
        fontDatabase.checkForUpdate { updateAvailable ->
            if (updateAvailable) {
                fontBinding.psfUpdateLayout.visibility = View.VISIBLE
            } else {
                fontBinding.psfUpdateLayout.visibility = View.GONE
            }
        }

        fontBinding.psfUpdate.setOnClickListener {
            fontBinding.psfUpdateLayout.visibility = View.GONE
            fontBinding.psfUpdatingLayout.visibility = View.VISIBLE
            fontDatabase.update()
            { status ->
                if (status == 0) {
                    Log.d(TAG, "showFontSelectionPopup: Update Complete")
                    fontBinding.psfUpdatingLayout.visibility = View.GONE
                    offlineFontsRecycler.updateRecycler()
                } else {
                    offlineFontsRecycler.updateRecycler()
                }
            }
        }
        fontBinding.psfDismiss.setOnClickListener {
            fontBinding.psfUpdateLayout.visibility = View.GONE
        }


        // on click listener for offline device fonts


    }

    private fun setColors(colorP: Int, colorHex: String) {
        var color = colorP
        Palette.from(getColorBitmap(color)).generate(object : Palette.PaletteAsyncListener {
            override fun onGenerated(palette: Palette?) {
                palette?.let {
                    val swatch = palette.dominantSwatch ?: palette.darkVibrantSwatch
                    ?: palette.darkMutedSwatch ?: palette.vibrantSwatch
                    textView.setTextColor(color)
                    if (textView.shadowColor != 0) {
                        changeShadow()
                    }
                    val dominantColor = if (swatch != null) {
                        Log.d(TAG, "onGenerated: not null")
                        swatch.titleTextColor
                    } else {
                        color = Color.BLACK
                        Color.WHITE
                    }

                    Log.d(TAG, "onGenerated: dominant color = ${dominantColor.toString()}")
                    binding.ftsTextColorButton.setCardBackgroundColor(color)
                    binding.ftsTextColorIcon.setColorFilter(dominantColor, PorterDuff.Mode.SRC_IN)
                    binding.ftsTextColorLabel.setTextColor(dominantColor)
                    binding.ftsTextColorCode.setText(colorHex)
                    binding.ftsTextColorCode.setTextColor(dominantColor)
                    binding.ftsSizeBar.thumb.setTint(color)


                }
            }

        })
    }

    private fun getColorBitmap(color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)
        return bitmap
    }

    private fun getHexFromInt(colorInt: Int): String {
        return String.format("#%06X", 0xFFFFFF and colorInt)
    }

    private fun changeBackgroundOption() {
        if (layer.backgroundColorInt == 0) {
            getColorSwatch(textView.currentTextColor) { swatch ->
                if (swatch != null) {
                    val backColor = swatch.titleTextColor
                    textView.background.alpha = 255
                    textView.setBackgroundColor(backColor)
                    layer.backgroundColorInt = backColor
                    binding.ftsBackgroundButton.setCardBackgroundColor(backColor)
                    binding.ftsBackgroundButtonText.setTextColor(swatch.rgb)
                } else {
                    ColorPickerDialog.Builder(context)
                        .setTitle("Choose Color")
                        .setPreferenceName("ChooseColor")
                        .setPositiveButton("Save",
                            ColorEnvelopeListener { envelope, fromUser ->
                                Log.d(TAG, "showChooseColorPopup: ")
                                val colorCode = "#${envelope.hexCode}"
                                val color = Color.parseColor(colorCode)
                                textView.background.alpha = 255
                                textView.setBackgroundColor(color)
                                layer.backgroundColorInt = color
                                binding.ftsBackgroundButton.setCardBackgroundColor(color)
                                getColorSwatch(color)
                                {
                                    if (it != null) {
                                        val textColor = it.titleTextColor
                                        binding.ftsBackgroundButtonText.setTextColor(textColor)
                                    } else {
                                        binding.ftsBackgroundButtonText.setTextColor(Color.WHITE)
                                    }
                                }


                            })
                        .setNegativeButton("Cancel",
                            DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                        .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                        .show()
                }
            }
        } else {
            textView.background.alpha = 255
            textView.setBackgroundColor(0)
            binding.ftsBackgroundButton.setCardBackgroundColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.matte_black_light
                )
            )
            layer.backgroundColorInt = 0
        }


    }

    private fun changeBackgroundOption(color: Int) {
        getColorSwatch(color) { swatch ->
            textView.background.alpha = 255
            textView.setBackgroundColor(color)
            layer.backgroundColorInt = color
            binding.ftsBackgroundButton.setCardBackgroundColor(color)
            if (swatch != null) {
                val backColor = swatch.titleTextColor
                binding.ftsBackgroundButtonText.setTextColor(backColor)
            } else {
                Log.d(TAG, "changeBackgroundOption: colos swatch is null")
                binding.ftsBackgroundButtonText.setTextColor(Color.WHITE)
            }
        }


    }

    private fun showBackgroundOptions() {
        val optionBinding = PopupBackgroundOptionsBinding.inflate(layoutInflater)
        val displayMetrics = resources.displayMetrics
        val window = PopupWindow(
            optionBinding.root,
            (displayMetrics.widthPixels * 0.95).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        window.animationStyle = R.style.slideAnimation
        binding.ftsTransformationLayout.startTransform()
        binding.ex3.setOnClickListener {
            binding.ftsTransformationLayout.finishTransform()
            binding.ftsTarget.visibility = View.GONE
        }
    }

    private fun getColorSwatch(mainColor: Int, function: (Palette.Swatch?) -> Unit) {
        val bitmap = getColorBitmap(mainColor)
        Palette.from(bitmap).generate { palette ->
            if (palette == null) {
                function(null)
                return@generate
            }
            function(
                palette.dominantSwatch ?: palette.darkVibrantSwatch
                ?: palette.darkMutedSwatch ?: palette.vibrantSwatch
            )


        }


    }

    fun rotateRight() {
        val currentQuad = ((textView.rotation / 45f))
        Log.d(TAG, "rotateRight: Current quad = $currentQuad")
        val curVal = if (currentQuad < 1f) {
            45
        } else if (currentQuad >= 1f && currentQuad < 2f) {
            90
        } else if (currentQuad >= 2f && currentQuad < 3f) {
            135
        } else if (currentQuad >= 3 && currentQuad < 4) {
            180
        } else if (currentQuad >= 4 && currentQuad < 5) {
            225
        } else if (currentQuad >= 5 && currentQuad < 6) {
            270
        } else if (currentQuad >= 6 && currentQuad < 7) {
            315
        } else {
            360
        }
        binding.ftsRotationBar.curProcess = curVal


    }

    fun toggleBold() {
        if (textView.typeface.isBold) {
            Log.d(TAG, "toggleBold: is bold")
            setBoldSelected(false)
            textView.setTypeface(layer.baseTypeface)
        } else {
            Log.d(TAG, "toggleBold: is not bold")
            setBoldSelected(true)
            textView.setTypeface(textView.typeface, Typeface.BOLD)
        }
    }

    fun toggleItalic() {
        if (textView.typeface.isItalic) {
            Log.d(TAG, "toggleItalic: is italic")
            setItalicSelected(false)
            if(textView.typeface.isBold)
            {
                textView.setTypeface(textView.typeface, Typeface.BOLD)
            }
            else {
                textView.setTypeface(layer.baseTypeface)
            }
        } else {
            setItalicSelected(true)
            Log.d(TAG, "toggleItalic: is not italic")
            if(textView.typeface.isBold)
            {
                textView.setTypeface(textView.typeface, Typeface.BOLD_ITALIC)
            }
            else {
                textView.setTypeface(textView.typeface, Typeface.ITALIC)
            }
        }
    }

    fun setBoldSelected(selected: Boolean) {
        if (selected) {
            binding.ftsStyleBold.setBackgroundResource(R.drawable.text_style_background)
        } else {
            binding.ftsStyleBold.setBackgroundResource(0)
        }
    }

    fun setItalicSelected(selected: Boolean) {
        if (selected) {
            binding.ftsStyleItalic.setBackgroundResource(R.drawable.text_style_background)
        } else {
            binding.ftsStyleItalic.setBackgroundResource(0)
        }
    }

    companion object {
        private const val TAG = "TextSettings"
        val TYPE_EDIT = "EDIT_TEXT"
        val TYPE_TEXT = "TEXT_VIEW"
    }
}