package com.ritwik.photex

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.transition.Fade
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ritwik.photex.databinding.*
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.unity3d.ads.IUnityAdsListener
import com.unity3d.ads.UnityAds
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt


class CreateMeme : AppCompatActivity() {

    private val chooseImageCode = 1024
    lateinit var binding: ActivityCreateMemeBinding
    private var padded = false
    val layers = Layers()
    private var imageSelected = false
    private lateinit var fragmentTextSettings: TextSettings
    private lateinit var emptyFragment: emptyOptions
    private lateinit var bottomTextLayer: Layer
    private lateinit var upperTextLayer: Layer
    private val theme = MemeTheme()
    private var style = ""
    private var mode = ""
    private lateinit var presets: Presets
    private lateinit var templatePopup: PopupChooseTemplate
    private lateinit var stickerPopup: StickerPopup
    private lateinit var adLoadingWindow: PopupWindow
    private val chooseStickerImageCode = 1025
    val nonRemovableLayer = arrayListOf<Layer>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateMemeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.enterTransition = Fade()

//        if(!UnityAds.isInitialized())
//        {
//            UnityAds.initialize(binding.root.context,"4218265",false)
//        }
        UnityAds.initialize(this, "4218265", UnityAdsListener(), false)
        presets = Presets(this)
        if (intent.extras != null) {
            val extras = intent.extras!!
            style = extras["STYLE"] as String
            mode = (extras["MODE"] ?: "") as String
            binding.root.post {
                setViews()
                if (style == "UPPER_TEXT") {
                    changeLayout("T")
                    //theme.setTopText()
                } else if (style == "BOTTOM_TEXT") {
                    //theme.setBottomText()
                    changeLayout("B")
                } else if (style == "BOTH_TEXT") {
                    //theme.setBothText()
                    changeLayout("BT")
                } else if (style == "NO_TEXT") {
                    //theme.setImageOnly()
                } else if (style == "TEXT_ONLY") {
                    theme.setTextOnly()
                } else {
                    theme.setBottomText()

                }
                if (mode == "LIGHT") {
                    theme.setLightTheme()
                } else if (mode == "DARK") {
                    theme.setDarkTheme()
                } else if (mode == "CUSTOM") {

                } else {

                }
            }
        }

        binding.acmMemeImage.setOnClickListener {
            if (style == "TEXT_ONLY") {
                return@setOnClickListener
            }
            if (!imageSelected)
                showImageChooseOptions()
        }
        binding.acmOptionBackground.setOnClickListener {
            if (style == "TEXT_ONLY") {
                ColorPickerDialog.Builder(this@CreateMeme)
                    .setTitle("Choose Color")
                    .setPreferenceName("ChooseColor")
                    .setPositiveButton("Save",
                        ColorEnvelopeListener { envelope, fromUser ->
                            Log.d(TAG, "showChooseColorPopup: ")
                            val colorCode = "#${envelope.hexCode}"
                            val color = Color.parseColor(colorCode)
                            // creating a bitmap of the chosen color
                            val bitmap = createColoredBitmap(color)
                            binding.acmMemeImage.setImageBitmap(bitmap)

                        })
                    .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                    .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                    .show()
                return@setOnClickListener
            }
            showImageChooseOptions()
//            chooseImage()


        }
        binding.acmOptionTheme.setOnClickListener {
            theme.showChangeThemePopup()


        }
        binding.acmOptionImageSticker.setOnClickListener {
            chooseStickerImage()
        }
        binding.acmMemeFrameBottomHorizontalLayout.weightSum = 1f
        binding.acmOptionPadding.setOnClickListener {
            setPadding()
        }
        binding.acmOptionSave.setOnClickListener {
            val bitmap = loadBitmapFromView(binding.acmMemeFrame)
            if (bitmap != null) {
                //
                val finalPopup = PopupShowFinalExport()
                finalPopup.showPopup(bitmap)
                //saveImage(bitmap, null)
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
        binding.acmOptionAddText
            .setOnClickListener {
                showTextPopup(null)
                {

                    layers.addLayer(Layer.LT_TEXT, it)
                }


            }
        bottomTextLayer = Layer()
        upperTextLayer = Layer()
        upperTextLayer.isRemovable = false
        bottomTextLayer.isRemovable = false
        binding.acmEmptyImageLayout
            .setOnClickListener {
                showImageChooseOptions()
            }
        binding.acmOptionSticker.setOnClickListener {
            showStickerPopup()
        }

    }

    fun removeEmptyLayout() {
        binding.acmEmptyImageLayout.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == chooseImageCode && resultCode == Activity.RESULT_OK) {
            if (data?.data != null) {
                // binding.acmMemeImage.setImageURI(data.data)
//                val image = binding.acmMemeImage.drawable
//                val bitmapDrawable = image as BitmapDrawable
//                val bitmap = bitmapDrawable.bitmap
//                changeColors(bitmap)

                val file = File(
                    getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "photextempfiledownloaded"
                )
                val uri = Uri.fromFile(file)
                imageSelected = true
                val options = UCrop.Options()
                options.setAspectRatioOptions(
                    1,
                    AspectRatio("16:9", 16f, 9f),
                    AspectRatio("1:1", 1f, 1f),
                    AspectRatio("3:2", 3f, 2f)
                )

                options.setFreeStyleCropEnabled(false)
                UCrop.of(data.data!!, uri)
                    .withOptions(options)
                    .start(this)

            }
        }
        if (requestCode == chooseStickerImageCode && resultCode == Activity.RESULT_OK) {
            if (data?.data != null) {
                // binding.acmMemeImage.setImageURI(data.data)
//                val image = binding.acmMemeImage.drawable
//                val bitmapDrawable = image as BitmapDrawable
//                val bitmap = bitmapDrawable.bitmap
//                changeColors(bitmap)

                val file = File(
                    getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "photextempfiledownloaded"
                )
                val uri = Uri.fromFile(file)
                val options = UCrop.Options()
                options.setFreeStyleCropEnabled(true)
                UCrop.of(data.data!!, uri)
                    .withOptions(options)
                    .start(this, 1026)

            }
        }
        if (requestCode == 1026 && resultCode == RESULT_OK) {
            val filePath: Uri? = UCrop.getOutput(data!!)
            Log.d(TAG, "onActivityResult: crop")
            try { //Getting the Bitmap from Gallery
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                val sticker = adjustCropImage(bitmap)
                layers.addLayer(Layer.LT_BITMAP, sticker)
            } catch (e: IOException) {
                e.printStackTrace()
            }


        }
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Log.d(TAG, "onActivityResult: crop received")
            val resultUri = UCrop.getOutput(data!!);
            binding.acmMemeImage.setImageURI(resultUri)
            val image = binding.acmMemeImage.drawable
            val bitmapDrawable = image as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            changeColors(bitmap)
            removeEmptyLayout()
            // deleting the temp file
            val file = File(
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "photextempfiledownloaded"
            )
            if (file.exists()) {
                file.delete()
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Log.d(TAG, "onActivityResult: ${UCrop.getError(data!!)}")
        }

    }

    private fun setPadding() {
        padded = if (!padded) {
            binding.acmMemeFrame.setPadding(16)
            true
        } else {
            binding.acmMemeFrame.setPadding(0)
            false
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        startActivityForResult(intent, chooseImageCode)
    }

    inner class Layers {
        private var currentLayerIndex = 0
        private var layerArray = arrayListOf<Layer>()
        private lateinit var onTouchListenr: View.OnTouchListener
        fun resetLayers() {
            AlertDialog.Builder(this@CreateMeme)
                .setTitle("Keep Changes")
                .setMessage("Keep the changes and items.?") // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Discard Changes",
                    DialogInterface.OnClickListener { dialog, which ->
                        for (n in layerArray.indices) {
                            removeLayer(n)
                        }

                    }) // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton("Keep Changes", null)
                .show()

        }

        fun addLayer(type: String, data: Any) {
            // setting up the view
            val temp = Layer()
            temp.type = type
            if (type == Layer.LT_TEXT) {

                temp.view = TextView(this@CreateMeme)
                (temp.view as TextView).text = data as String

                val preSize = presets.preSize / 2
                val preColor = try {
                    Color.parseColor(presets.preTextColor)
                } catch (e: Exception) {
                    Color.BLACK
                }
                (temp.view as TextView).apply {
                    temp.baseTypeface = this.typeface
                    textSize = preSize
                    setTextColor(preColor)
                    if (presets.preFont != presets.DEFAULT_FONT) {

                        val file =
                            context!!.getExternalFilesDir("${Environment.DIRECTORY_DOCUMENTS}/Fonts/${presets.preFont}")
                        file?.let {
                            if (it.exists()) {
                                typeface = Typeface.createFromFile(it)
                            } else {
                                Toast.makeText(
                                    context!!,
                                    "File ${presets.preFont} does not exist",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } else {
                temp.oHeight = (data as Bitmap).height
                temp.height = temp.oHeight
                temp.oWidth = (data as Bitmap).width
                temp.width = temp.oWidth
                temp.view = ImageView(this@CreateMeme)
                (temp.view as ImageView).setImageBitmap(data as Bitmap)
            }
            (temp.view)
            layerArray.add(temp)
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.CENTER
            temp.view.layoutParams = layoutParams
            temp.view.setBackgroundResource(R.drawable.rect_text_back)
            setOnTouchListener(temp.view)
            // getting the index of watermark
            binding.acmMemeFrame.removeView(binding.acmWaterMark)
            binding.acmMemeFrame.addView(temp.view)
            binding.acmMemeFrame.addView(
                binding.acmWaterMark
            )

            currentLayerIndex = layerArray.indexOf(temp)
            if (type == Layer.LT_TEXT) {
                showTextSettings(temp)
            } else {
                showBitmapSettings(temp)
            }


        }

        fun removeLayer() {
            val currentView = layerArray[currentLayerIndex].view
            layerArray.removeAt(currentLayerIndex)
            // removing the view from the main layout
            binding.acmMemeFrame.removeView(currentView)
            showNoOptionSelected()

        }

        fun removeLayer(index: Int) {
            val currentView = layerArray[index].view
            layerArray.removeAt(index)
            // removing the view from the main layout
            binding.acmMemeFrame.removeView(currentView)
            showNoOptionSelected()

        }

        fun getAllLayers() {

        }

        fun getLayer(): Layer {
            return layerArray[currentLayerIndex]
        }

        fun getLayer(currentIndex: Int): Layer {
            return layerArray[currentIndex]
        }


        fun changeLayerData() {

        }

        fun getLayerIndexFromView(view: View): Int {
            if (layerArray.size == 0) {
                return -1
            }
            for (n in layerArray.indices) {
                if (layerArray[n].view == view) {
                    return n
                }
            }
            return -1
        }


        @SuppressLint("ClickableViewAccessibility")
        private fun setOnTouchListener(view: View) {
            var initX = 0f
            var initY = 0f
            if (!this::onTouchListenr.isInitialized) {
                onTouchListenr = OnTouchListener { view, event ->
                    val x = event.rawX
                    val y = event.rawY

                    if (event.action == MotionEvent.ACTION_DOWN) {
                        binding.acmMemeScroll.isEnableScrolling = false
                        currentLayerIndex = getLayerIndexFromView(view)
                        if (view is TextView) {
                            showTextSettings(layerArray[currentLayerIndex])
                        } else {
                            showBitmapSettings(layerArray[currentLayerIndex])
                            Log.d(TAG, "setOnTouchListener: Not text view")
                        }
                        val params = view.layoutParams as FrameLayout.LayoutParams
                        initX = x - params.leftMargin
                        initY = y - params.topMargin
                    }
                    if (event.action == MotionEvent.ACTION_UP) {
                        binding.acmMemeScroll.isEnableScrolling = true
                    }

                    val params = view.layoutParams as FrameLayout.LayoutParams

                    params.leftMargin = (x - initX).toInt()
                    params.topMargin = (y - initY).toInt()
                    params.rightMargin = 0
                    params.bottomMargin = 0
                    view.layoutParams = params
                    view.requestLayout()




                    return@OnTouchListener true
                }
            }
            view.setOnTouchListener(onTouchListenr)

        }


    }

    private fun showTextSettings(layer: Layer) {
        fragmentTextSettings = TextSettings(layer, this)
        supportFragmentManager.beginTransaction()
            .replace(binding.acmSettingsContainer.id, fragmentTextSettings).commit()

    }

    private fun showBitmapSettings(layer: Layer) {

        val fragmentBitmapSettings = BitmapSettings(layer, this@CreateMeme)

        supportFragmentManager.beginTransaction()
            .replace(binding.acmSettingsContainer.id, fragmentBitmapSettings).commit()
    }

    fun showTextPopup(preText: String?, function: (newString: String) -> Unit) {
        val popupBinding = PopupAddTextBinding.inflate(layoutInflater)
        val window = PopupWindow(
            popupBinding.root,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        window.animationStyle = R.style.slideAnimation
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        window.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        window.showAtLocation(popupBinding.root, Gravity.BOTTOM, 0, 0)
        if (preText != null) {
            popupBinding.patTextField.setText(preText)
        }
        popupBinding.patAddButton
            .setOnClickListener {
                function(popupBinding.patTextField.text.toString())
                window.dismiss()
            }
    }

    fun showNoOptionSelected() {
        emptyFragment = emptyOptions()
        supportFragmentManager.beginTransaction()
            .replace(binding.acmSettingsContainer.id, emptyFragment).commit()

    }

    private fun setViews() {
        showNoOptionSelected()
        //showTextSettings(binding.acmMemeText)
    }

    private fun changeColors(bitmap: Bitmap) {


        Palette.from(bitmap).generate { palette ->
            if (palette == null) {
                Log.d(TAG, "changeColors: palette is null")
            }
            palette?.let {
                val swatch =
                    palette.vibrantSwatch ?: palette.darkVibrantSwatch ?: palette.dominantSwatch
                    ?: palette.darkMutedSwatch
                if (swatch == null) {
                    Log.d(TAG, "changeColors: swatch is null")
                    return@generate
                }
                val mainColor = swatch.rgb
                val titleTextColor = swatch.titleTextColor
                binding.acmMenuCard.setBackgroundColor(mainColor)
                window.statusBarColor = mainColor
                binding.acmOptionPadding.setColorFilter(
                    titleTextColor,
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                binding.acmOptionPaddingText.setTextColor(titleTextColor)
                // text
                binding.acmOptionText.setColorFilter(
                    titleTextColor,
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                binding.acmOptionTextText.setTextColor(titleTextColor)
                //
                binding.acmOptionSticker.setColorFilter(
                    titleTextColor,
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                binding.acmOptionStickerText.setTextColor(titleTextColor)
                //
                binding.acmOptionBackground.setColorFilter(
                    titleTextColor,
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                binding.acmOptionBackgroundText.setTextColor(titleTextColor)
                //
                binding.acmOptionSave.setColorFilter(
                    titleTextColor,
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                binding.acmOptionSaveText.setTextColor(titleTextColor)
                //
                binding.acmOptionThemeText.setTextColor(titleTextColor)
                binding.acmOptionThemeImage.setColorFilter(
                    titleTextColor,
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
                binding.acmOptionImageStickerText.setTextColor(titleTextColor)
                binding.acmOptionImageStickerImage.setColorFilter(
                    titleTextColor,
                    android.graphics.PorterDuff.Mode.SRC_IN
                );
            }
        }

    }

    inner class MemeTheme {

        private val THEME_LIGHT = "LIGHT"
        private val THEME_DARK = "DARK"
        private val THEME_CUSTOM = "CUSTOM"
        private var currentTheme = THEME_LIGHT

        fun showChangeThemePopup() {
            val themeBinding = PopupChangeThemeBinding.inflate(layoutInflater)
            val displayMetrics = resources.displayMetrics
            val window = PopupWindow(
                themeBinding.root,
                (displayMetrics.widthPixels * 0.5).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT,
                true
            )
            window.animationStyle = R.style.pAnimation
            window.showAtLocation(themeBinding.root, Gravity.CENTER, 0, 0)
            themeBinding.pcthLightMode.setOnClickListener {
                setLightTheme()
                window.dismiss()
            }
            themeBinding.ptchDarkMode.setOnClickListener {
                setDarkTheme()
                window.dismiss()
            }
            themeBinding.ptchCustomMode.setOnClickListener {
                setCustomTheme()
                window.dismiss()
            }

        }

        fun setDarkTheme() {
            binding.acmMemeFrame.setBackgroundColor(
                ContextCompat.getColor(
                    this@CreateMeme,
                    R.color.matte_black
                )
            )
            currentTheme = THEME_DARK
        }

        fun setLightTheme() {
            binding.acmMemeFrame.setBackgroundColor(Color.WHITE)
            currentTheme = THEME_LIGHT
        }

        fun setCustomTheme() {
            ColorPickerDialog.Builder(this@CreateMeme)
                .setTitle("Choose Color")
                .setPreferenceName("ChooseColor")
                .setPositiveButton("Save",
                    ColorEnvelopeListener { envelope, fromUser ->
                        Log.d(TAG, "showChooseColorPopup: ")
                        val colorCode = "#${envelope.hexCode}"
                        val color = Color.parseColor(colorCode)
                        binding.acmMemeFrame.setBackgroundColor(color)
                        currentTheme = THEME_CUSTOM

                    })
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .show()
        }

        fun setBottomText() {
            // removing upper text
        }

        fun setTopText() {
            // removing bottom text
        }

        fun setBothText() {

        }


        fun setTextOnly() {
            // removing both text views
            //
            val colorBitmap = createColoredBitmap(Color.WHITE)
            binding.acmMemeImage.setImageBitmap(colorBitmap)
            binding.acmEmptyImageLayout.visibility = View.GONE
            binding.acmWaterMark.alpha = 0.1f

        }
    }

    fun loadBitmapFromView(v: View): Bitmap? {
        v.clearFocus()
        val b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.draw(c)
        return b
    }

    fun saveImage(bitmap: Bitmap, nameP: String?) {
        CloudDatabase.incrementNoOfSaves()
        val contentValues = ContentValues()
        val name = nameP ?: getRandomName()
        Log.d(TAG, "saveImage: Saving image as $name")
        contentValues.apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }
        val resolver = contentResolver
        try {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            val uri = resolver.insert(contentUri, contentValues)
            if (uri == null) {
                Log.d(TAG, "saveImage: Uri is null")
                Toast.makeText(this, "Failed to save Image", Toast.LENGTH_SHORT).show()
                return
            }
            val outputStream = resolver.openOutputStream(uri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                if (binding.acmMemeFrame.visibility != View.VISIBLE) {
                    Toast.makeText(
                        this,
                        "Image saved as $name without watermark",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Image saved as $name", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d(TAG, "saveImage: Output stream is null")
            }


        } catch (e: Exception) {
            Toast.makeText(this, "Failed to Save Image", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }


    }

    fun getRandomName(): String {
        var name = "PhotexTweet"
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        name = "$name$n"
        return name
    }

    // creating colored bitmap
    private fun createColoredBitmap(color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(720, 720, Bitmap.Config.ARGB_8888)
        // painting the bitmap
        val paint = Paint()
        paint.color = color
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)
        return bitmap
    }

    private fun createColoredBitmap(color: String): Bitmap {
        val bitmap = Bitmap.createBitmap(720, 720, Bitmap.Config.ARGB_8888)
        // painting the bitmap
        val paint = Paint()
        paint.color = Color.parseColor(color)
        val canvas = Canvas(bitmap)
        canvas.drawPaint(paint)
        return bitmap
    }

    private fun showImageChooseOptions() {
        val optionBinding = PopupImageOptionsBinding.inflate(layoutInflater)
        val window = PopupWindow(
            optionBinding.root,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        window.animationStyle = R.style.pAnimation
        window.elevation = 20f
        window.showAsDropDown(binding.acmOptionBackground)
        optionBinding.pioGallery.setOnClickListener {
            chooseImage()
            window.dismiss()
        }
        optionBinding.pioTemplates
            .setOnClickListener {
                if (!this::templatePopup.isInitialized) {
                    templatePopup = PopupChooseTemplate(this)
                    { bitmap, styleCode ->
                        if (binding.acmEmptyImageLayout.visibility != View.GONE) {
                            binding.acmEmptyImageLayout.visibility = View.GONE
                        }
                        binding.acmMemeImage.setImageBitmap(bitmap)
                        changeColors(bitmap)
                        if (styleCode != null) {
                            showStyleChangePopup(styleCode)
                        } else {
                            Log.d(TAG, "showImageChooseOptions: style code is null")
                        }
                        imageSelected = true

                    }
                }
                templatePopup.showPopup()
                window.dismiss()

            }


    }

    inner class StickerPopup {
        private lateinit var loadingWindow: PopupWindow
        var stickerLinks = arrayListOf<String>()
        private lateinit var stickerRecycler: recyclerClass
        private lateinit var stickersBinding: PopupShowStickersBinding
        private lateinit var window: PopupWindow

        inner class recyclerClass(val recyclerView: RecyclerView) {
            lateinit var adapter: RecyclerView.Adapter<ViewHolder>

            fun showRecycler() {
                if (!this::adapter.isInitialized) {
                    adapter = object : RecyclerView.Adapter<ViewHolder>() {
                        override fun onCreateViewHolder(
                            parent: ViewGroup,
                            viewType: Int
                        ): ViewHolder {
                            val view = LayoutInflater.from(this@CreateMeme)
                                .inflate(R.layout.single_sticker_show, parent, false)
                            return ViewHolder(view)
                        }

                        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                            holder.setImage(stickerLinks[position])
                            stickersBinding.pssLoadingView.visibility = View.GONE
                            holder.itemView.setOnClickListener {
                                showLoading()
                                window.dismiss()
                                Toast.makeText(
                                    this@CreateMeme,
                                    "Please Wait!! ",
                                    Toast.LENGTH_SHORT
                                ).show()
                                getBitmapFromURL(stickerLinks[position])
                                {
                                    if (it != null) {
                                        layers.addLayer(Layer.LT_BITMAP, it)
                                        hideLoading()
                                    }
                                }
                            }
                        }

                        override fun getItemCount(): Int {
                            return stickerLinks.size
                        }

                    }
                }
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }

            inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                private val stickerImage =
                    itemView.findViewById<ImageView>(R.id.sss_StickerImage)

                fun setImage(link: String) {
                    Glide.with(stickerImage).load(link).into(stickerImage)
                }

            }


            fun addLink(link: String) {
                stickerLinks.add(link)
                if (!this::adapter.isInitialized) {

                } else {
                    adapter.notifyItemInserted(stickerLinks.size)
                }

            }

        }


        private fun showLoading() {
            val view = LayoutInflater.from(this@CreateMeme).inflate(R.layout.popup_ad_loading, null)
            loadingWindow = PopupWindow(
                view,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                false
            )
            loadingWindow.elevation = 100f
            if (!loadingWindow.isShowing) {
                loadingWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
            }

        }

        private fun hideLoading() {
            Log.d(TAG, "hideLoading: ")
            if (!this::loadingWindow.isInitialized) {
                Log.d(TAG, "hideLoading: not initialised")
                return
            }
            if (loadingWindow.isShowing) {
                Log.d(TAG, "hideLoading: dismissing")
                loadingWindow.dismiss()
            }
        }

        fun showPopup() {
            stickerLinks.clear()
            stickersBinding = PopupShowStickersBinding.inflate(layoutInflater)
            window = PopupWindow(
                stickersBinding.root,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                true
            )
            window.showAtLocation(stickersBinding.root, Gravity.CENTER, 0, 0)
            stickerRecycler = recyclerClass(stickersBinding.pssRecyclerView)
            val manager = GridLayoutManager(this@CreateMeme, 4)
            stickersBinding.pssRecyclerView.layoutManager = manager
            stickerRecycler.showRecycler()
            getData("")
            // setting search algo
            stickersBinding.pssSearchField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "beforeTextChanged: ")
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    p0?.let {
                        getData(it.toString())
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                    Log.d(TAG, "afterTextChanged: ")
                }
            })


        }

        fun getData(searchString: String) {
            val reference = FirebaseDatabase.getInstance().reference
            reference.child("STICKERS")
                .addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                Log.d(TAG, "onDataChange: Snapshot does not exist")
                                return
                            }
                            if (!snapshot.hasChildren()) {
                                Log.d(TAG, "onDataChange: snapshot does not have children")
                                return
                            }
                            stickerLinks.clear()
                            stickerRecycler.adapter.notifyDataSetChanged()
                            for (child in snapshot.children) {

                                if (TextUtils.isEmpty(searchString)) {

                                    Log.d(TAG, "onDataChange: No search string")
                                    if (this@StickerPopup::stickerRecycler.isInitialized) {
                                        stickerRecycler.addLink(child.value.toString())
                                    }
                                } else {
                                    if (child.key?.contains(searchString, true) == true) {

                                        Log.d(TAG, "onDataChange: searching for $searchString")
                                        if (this@StickerPopup::stickerRecycler.isInitialized) {
                                            Log.d(
                                                TAG,
                                                "onDataChange: found ${child.key ?: "no key found"}"
                                            )

                                            stickerRecycler.addLink(child.value.toString())
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d(TAG, "onCancelled: cancelled")
                        }

                    }
                )
        }
    }

    private fun getBitmapFromURL(src: String, function: (bitmap: Bitmap?) -> Unit) {
        var bitmap: Bitmap?
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(src)

        val localFile = File.createTempFile("template", "jpg")

        storageRef.getFile(localFile).addOnSuccessListener {
            Log.d(TAG, "getBitmapFromURL: Success")
            bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            function(bitmap)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load Template", Toast.LENGTH_SHORT).show()
            // Handle any errors
        }
    }

    private fun showStickerPopup() {
        if (!this::stickerPopup.isInitialized) {
            stickerPopup = StickerPopup()
        }
        stickerPopup.showPopup()
    }

    inner class PopupShowFinalExport {
        var watermarked: Boolean = false
        private lateinit var popupBinding: PopupFinalExportBinding
        private lateinit var window: PopupWindow
        private lateinit var originalBitmap: Bitmap
        private lateinit var croppedBitmap: Bitmap
        private var squared = false
        fun showPopup(bitmap: Bitmap) {
            if (!this::popupBinding.isInitialized) {
                popupBinding = PopupFinalExportBinding.inflate(layoutInflater)
                window = PopupWindow(
                    popupBinding.root,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    true
                )

            }
            originalBitmap = bitmap
            window.showAtLocation(popupBinding.root, Gravity.CENTER, 0, 0)
            popupBinding.pfeMainImage.setImageBitmap(bitmap)
            popupBinding.pfeSaveButton.setOnClickListener {
                showAd()
                saveImage(bitmap, null)
            }
            popupBinding.pfeWatermarkButton.setOnClickListener {
                showRewardedAd()
            }
            popupBinding.pfeSquareCropButton.setOnClickListener {
                val presets = Presets(this@CreateMeme)
                if (!squared) {
                    val color = Color.parseColor(presets.preBackColor)
                    cropSquare(color)
                    popupBinding.pfeBackColorButton.visibility = View.VISIBLE
                    popupBinding.pfeSquareCropText.setText("Reset")
                } else {
                    popupBinding.pfeMainImage.setImageBitmap(originalBitmap)
                    squared = false
                    popupBinding.pfeBackColorButton.visibility = View.GONE
                    popupBinding.pfeSquareCropText.setText("Square")
                }
            }
            // setting on click listener for color Button
            popupBinding.pfeBackColorButton.setOnClickListener {
                ColorPickerDialog.Builder(this@CreateMeme)
                    .setTitle("Choose Color")
                    .setPreferenceName("ChooseColor")
                    .setPositiveButton("Save",
                        ColorEnvelopeListener { envelope, fromUser ->
                            Log.d(TAG, "showChooseColorPopup: ")
                            val colorCode = "#${envelope.hexCode}"
                            val color = Color.parseColor(colorCode)
                            cropSquare(color)
                            presets.preBackColor = colorCode

                        })
                    .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                    .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                    .show()
            }
        }

        fun setImage(bitmap: Bitmap) {
            popupBinding.pfeMainImage.setImageBitmap(bitmap)
        }

        private fun cropSquare(backgroundColor: Int) {
            // getting the larger side of the original bitmap
            val largerSide = if (originalBitmap.width > originalBitmap.height) {
                originalBitmap.width
            } else {
                originalBitmap.height
            }
            // creating a bitmap of the larger side
            croppedBitmap = Bitmap.createBitmap(largerSide, largerSide, Bitmap.Config.ARGB_8888)
            // painting the bitmap
            val canvas = Canvas(croppedBitmap)
            canvas.drawColor(backgroundColor)
            var x = 0
            var y = 0
            if (originalBitmap.width > originalBitmap.height) {
                x = 0
                y = (largerSide / 2) - originalBitmap.height / 2
            } else {
                y = 0
                x = (largerSide / 2) - originalBitmap.width / 2
            }
            // printing the bitmap on the new canvas
            canvas.drawBitmap(originalBitmap, x.toFloat(), y.toFloat(), null)
            // setting the bitmap as main Image
            setImage(croppedBitmap)
            squared = true
        }
    }

    inner class UnityAdsListener : IUnityAdsListener {
        override fun onUnityAdsReady(p0: String?) {
            Log.d(TAG, "onUnityAdsReady: ")
        }

        override fun onUnityAdsStart(p0: String?) {
            Log.d(TAG, "onUnityAdsStart: ")
        }

        override fun onUnityAdsFinish(p0: String?, p1: UnityAds.FinishState?) {
            Log.d(TAG, "onUnityAdsFinish: ")
        }

        override fun onUnityAdsError(p0: UnityAds.UnityAdsError?, p1: String?) {
            Log.d(TAG, "onUnityAdsError: ")
        }

    }

    inner class RewardedAdListener : IUnityAdsListener {
        override fun onUnityAdsReady(p0: String?) {
            Log.d(TAG, "onUnityAdsReady: ")


        }

        override fun onUnityAdsStart(p0: String?) {
            Log.d(TAG, "onUnityAdsStart: ad started")


        }

        override fun onUnityAdsError(p0: UnityAds.UnityAdsError?, p1: String?) {

            Log.d(TAG, "onUnityAdsError: error ${p1?.toString()}")
        }

        override fun onUnityAdsFinish(p0: String?, finishState: UnityAds.FinishState?) {

            finishState?.let { state ->
                if (state == UnityAds.FinishState.COMPLETED) {
                    Log.d(TAG, "onUnityAdsFinish: completed")
                    binding.acmWaterMark.visibility = View.INVISIBLE
                    val noWatermarkBitmap = loadBitmapFromView(binding.acmMemeFrame)
                    if (noWatermarkBitmap != null) {
                        saveImage(noWatermarkBitmap, null)
                        binding.acmWaterMark.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            this@CreateMeme,
                            "Failed to save Image",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }


                } else if (state == UnityAds.FinishState.SKIPPED) {
                    Log.d(TAG, "onUnityAdsFinish: Skipped")
                } else if (state == UnityAds.FinishState.ERROR) {
                    Log.d(TAG, "onUnityAdsFinish: error")
                } else {
                    Log.d(TAG, "onUnityAdsFinish")
                }
            }
        }

    }

    private fun showAd() {
        val view = PopupAdLoadingBinding.inflate(layoutInflater)
        if (!this::adLoadingWindow.isInitialized) {

            adLoadingWindow = PopupWindow(
                view.root,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                false
            )
            adLoadingWindow.elevation = 20f
            adLoadingWindow.showAtLocation(view.root, Gravity.CENTER, 0, 0)
        } else if (!adLoadingWindow.isShowing) {
            adLoadingWindow.showAtLocation(view.root, Gravity.CENTER, 0, 0)
        }
        val timer = object : CountDownTimer(6000, 1000) {
            override fun onTick(p0: Long) {
                Log.e(TAG, "onTick:  = $p0")
                if (UnityAds.isReady("Interstitial_Android")) {
                    Log.e(TAG, "onTick: ready")
                    UnityAds.show(this@CreateMeme, "Interstitial_Android")
                    Log.d(TAG, "showAd: showing ad")
                    if (adLoadingWindow.isShowing) {
                        adLoadingWindow.dismiss()
                    }
                    this.cancel()
                } else {
                    Log.e(TAG, "onTick: not ready")
                }
            }

            override fun onFinish() {
                if (adLoadingWindow.isShowing) {
                    adLoadingWindow.dismiss()
                }
            }


        }
        timer.start()

    }

    private fun showRewardedAd() {
        AlertDialog.Builder(this)
            .setTitle("Remove Watermark")
            .setMessage("You can remove watermark for once by watching a rewarded Ad") // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Watch Ad",
                DialogInterface.OnClickListener { dialog, which ->
                    // showing the rewarded ad
                    val view = PopupAdLoadingBinding.inflate(layoutInflater)
                    if (!this::adLoadingWindow.isInitialized) {

                        adLoadingWindow = PopupWindow(
                            view.root,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            false
                        )
                        adLoadingWindow.elevation = 20f
                        adLoadingWindow.showAtLocation(view.root, Gravity.CENTER, 0, 0)
                    } else if (!adLoadingWindow.isShowing) {
                        adLoadingWindow.showAtLocation(view.root, Gravity.CENTER, 0, 0)
                    }
                    val timer = object : CountDownTimer(10000, 1000) {
                        override fun onTick(p0: Long) {
                            UnityAds.initialize(
                                this@CreateMeme,
                                "4218265",
                                RewardedAdListener(),
                                true
                            )
                            if (UnityAds.isReady("watermark_rewarded")) {
                                UnityAds.show(this@CreateMeme, "watermark_rewarded")
                                if (adLoadingWindow.isShowing) {
                                    adLoadingWindow.dismiss()
                                }
                                this.cancel()
                            }
                        }

                        override fun onFinish() {
                            if (adLoadingWindow.isShowing) {
                                adLoadingWindow.dismiss()
                            }
                            Toast.makeText(
                                this@CreateMeme,
                                "Failed to load ad,Try Again!!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                    timer.start()
                    Toast.makeText(this, "PLease Wait, Loading Ad", Toast.LENGTH_SHORT).show()


                }) // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Discard Image")
            .setMessage("Discard the current image") // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Discard",
                DialogInterface.OnClickListener { dialog, which ->
                    finish()

                }) // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton("Cancel", null)
            .show()

    }

    override fun onBackPressed() {
        showExitConfirmation()
    }

    private fun changeLayout(layoutString: String) {
        // iterating through each letter

        // dividing top text into two parts
        // setting the weight sum
        nonRemovableLayer.clear()
        binding.acmMemeFrameBottomHorizontalLayout.weightSum = 0f
        binding.acmMemeFrameBottomHorizontalLayout.removeAllViews()
        binding.acmMemeFrameTopHorizontalLayout.weightSum = 0f
        binding.acmMemeFrameTopHorizontalLayout.visibility = View.GONE
        binding.acmMemeFrameTopHorizontalLayout.removeAllViews()
        binding.acmMainImageLayout.weightSum = 1f
        val imageParams = binding.acmMemeImage.layoutParams as LinearLayout.LayoutParams
        imageParams.weight = 1f
        binding.acmMemeImage.layoutParams = imageParams
        binding.acmMemeVerticalLayout.weightSum = 0f
        binding.acmMemeVerticalLayout.removeAllViews()
        for (char in layoutString) {
            Log.d(TAG, "changeLayout: ${binding.acmMemeFrameBottomHorizontalLayout.weightSum}")
            // adding the second text view
            val textView = TextView(this)
            val layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.weight = 1f
            textView.layoutParams = layoutParams
            // adding the text view to the bottom layout
            textView.text = "Tap to edit text"
            val presets = Presets(this)
            textView.setTextColor(Color.parseColor(presets.preTextColor))
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.avenir_next_bold))
            textView.setBackgroundColor(Color.TRANSPARENT)
            textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            textView.textSize = 20f
            // adding the textView to the layers
            val layer = Layer()
            layer.view = textView
            layer.isRemovable = false
            nonRemovableLayer.add(layer)
            textView.setOnClickListener(onClickListener(layer))
            //
            when (char) {
                'B' -> {
                    binding.acmMemeFrameBottomHorizontalLayout.weightSum += 1F
                    binding.acmMemeFrameBottomHorizontalLayout.addView(textView)
                }
                'T' -> {
                    binding.acmMemeFrameTopHorizontalLayout.visibility = View.VISIBLE
                    binding.acmMemeFrameTopHorizontalLayout.weightSum += 1F
                    binding.acmMemeFrameTopHorizontalLayout.addView(textView)
                }
                'H' -> {
                    binding.acmMainImageLayout.weightSum = 2f
                    val layoutParams =
                        binding.acmMemeImage.layoutParams as LinearLayout.LayoutParams
                    layoutParams.weight = 1f
                    binding.acmMemeImage.layoutParams = layoutParams
                    val lp = binding.acmMemeVerticalLayout.layoutParams as LinearLayout.LayoutParams
                    lp.weight = 1f
                    binding.acmMemeVerticalLayout.layoutParams = lp
                    val textParams =
                        LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0)
                    textParams.weight = 1f
                    textParams.gravity = Gravity.CENTER and Gravity.CENTER_VERTICAL
                    textView.gravity = Gravity.CENTER
                    textView.layoutParams = textParams

                    if (binding.acmMemeVerticalLayout.weightSum < 1f) {
                        binding.acmMemeVerticalLayout.weightSum = 1f
                        binding.acmMemeVerticalLayout.addView(textView)
                    } else {
                        binding.acmMemeVerticalLayout.weightSum += 1f
                        binding.acmMemeVerticalLayout.addView(textView)
                    }


                }
            }

        }

    }

    inner class onClickListener(val layer: Layer) : View.OnClickListener {
        override fun onClick(p0: View?) {
            val view = p0!! as TextView
            if (view.text != "Tap to edit text") {
                showTextSettings(layer)
                return
            }
            showTextPopup(null)
            { newText ->
                view.text = newText
                showTextSettings(layer)
            }
        }

    }

    private fun showStyleChangePopup(styleCode: String) {
        AlertDialog.Builder(this)
            .setTitle("Meme Style")
            .setMessage("Use predefined Meme style for this template.?") // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Use",
                DialogInterface.OnClickListener { dialog, which ->
                    changeLayout(styleCode)

                }) // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton("Cancel", null)
            .show()

    }

    private fun adjustCropImage(bitmap: Bitmap): Bitmap {
        Log.d(TAG, "adjustCropImage: ")
        // getting the width and height of the original meme image

        val width = binding.acmMemeImage.width
        val height = binding.acmMemeImage.height
        val newScale = if (width < 1 || height < 1) {
            1f
        } else {
            width / height.toFloat() * 0.5f
        }

        // setting the new dimensions for the new bitmap
        val newW = bitmap.width * newScale
        Log.d(TAG, "adjustCropImage: new Width = $newW")
        val newH = bitmap.height * newScale
        Log.d(TAG, "adjustCropImage: new Height = $newH")
        // creating a new scaled Bitmap
        val scaledBitmap =
            Bitmap.createScaledBitmap(bitmap, newW.roundToInt(), newH.roundToInt(), false)
        return scaledBitmap

    }

    private fun chooseStickerImage() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        startActivityForResult(intent, chooseStickerImageCode)
    }

    companion object {
        private const val TAG = "CreateMeme"
    }
}