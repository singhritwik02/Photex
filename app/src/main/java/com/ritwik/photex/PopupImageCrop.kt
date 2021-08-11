package com.ritwik.photex

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import android.widget.SeekBar
import com.ritwik.photex.databinding.PopupImageCropBinding
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlin.math.roundToInt

class PopupImageCrop(
    val context: Context,
    val bitmap: Bitmap,
    val function: (bitmap: Bitmap) -> Unit
) {
    private lateinit var toReturnBitmap: Bitmap
    private lateinit var imageOrientation: String
    private lateinit var window: PopupWindow
    private lateinit var binding: PopupImageCropBinding
    private var color: String = "#FFFFFF"
    private var margins = Margins()
    var status = "O"
    fun showWindow() {
        Log.d(TAG, "showWindow: showing window")
        if (!this::binding.isInitialized) {
            binding = PopupImageCropBinding.inflate(LayoutInflater.from(context))
        }
        if (!this::window.isInitialized) {
            window = PopupWindow(
                binding.root,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                true
            )
            window.animationStyle = R.style.pAnimation

        }
        window.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
        binding.picImageView.setImageBitmap(bitmap)
        binding.picCenterAlign.setOnClickListener {
            toReturnBitmap = bitmap
            addCenter(0f)
            status = "C"
        }
        binding.picTopAlign.setOnClickListener {
            toReturnBitmap = bitmap
            addBottom(0f)
            status = "B"
        }
        binding.picBottomAlign.setOnClickListener {
            toReturnBitmap = bitmap
            addTop(0f)
            status = "T"
        }
        binding.picReset.setOnClickListener {
            toReturnBitmap = bitmap
            binding.picImageView.setImageBitmap(toReturnBitmap)
            status = ""
        }
        binding.picSquareCrop.setOnClickListener {
            cropToSquare()


        }
        binding.picPresetBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    p0?.let { seekBar ->
                        val extra = seekBar.progress.toFloat() / 100
                        Log.d(TAG, "onProgressChanged: extra = ${extra}")
                        when (status) {
                            "T" -> {
                                addTop(extra)
                            }
                            "B" -> {
                                addBottom(extra)
                            }
                            "C" -> {
                                addCenter(extra)
                            }
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
        binding.picChooseColorImage
            .setOnClickListener {
                ColorPickerDialog.Builder(context)
                    .setTitle("Choose Color")
                    .setPreferenceName("ChooseColor")
                    .setPositiveButton("Save",
                        ColorEnvelopeListener { envelope, fromUser ->
                            Log.d(TAG, "showChooseColorPopup: ")
                            color = "#${envelope.hexCode}"

                        })
                    .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                    .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                    .show()

            }
        binding.picNextButton.setOnClickListener {
            if (!this::toReturnBitmap.isInitialized) {
                toReturnBitmap = bitmap
            }
            function(toReturnBitmap)
            window.dismiss()
        }
        binding.picBorderBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                Log.d(TAG, "onProgressChanged: ${p0?.progress}")
                val progress = p0!!.progress
                val percent = (.01) + ((progress - 1) * 0.001)
                if (!this@PopupImageCrop::toReturnBitmap.isInitialized) {
                    toReturnBitmap = BitmapFunctions.setBorders(bitmap, percent.toFloat(), color)
                } else {
                    toReturnBitmap = BitmapFunctions.setBorders(bitmap, percent.toFloat(), color)
                }
                binding.picAllBorderPercent.setText("${p0.progress}%")
                binding.picImageView.setImageBitmap(toReturnBitmap)

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                Log.d(TAG, "onStartTrackingTouch: ")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                Log.d(TAG, "onStopTrackingTouch: ")
            }

        })
        val slideUp = AnimationUtils.loadAnimation(context!!, R.anim.slide_up) as Animation
        val slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down) as Animation


        binding.picPresetButton.setOnClickListener { view ->
            if (binding.picPresetLayout.visibility != View.VISIBLE) {
                binding.picPresetButtonImage.animate().rotation(180f).setDuration(150)
                    .withEndAction {
                        binding.picPresetLayout.startAnimation(slideDown)
                        binding.picPresetLayout.visibility = View.VISIBLE
                        binding.picPresetLayout.animate().alpha(1f).setDuration(150)

                    }

            } else {
                binding.picPresetButtonImage.animate().rotation(0f).setDuration(150).withEndAction {

                    binding.picPresetLayout.startAnimation(slideUp)
                    binding.picPresetLayout.visibility = View.GONE


                }


            }


        }


    }

    private fun cropToSquare() {
        // calculating the dominating dimension
        val dominatingDimen = if (bitmap.height >= bitmap.width) {
            imageOrientation = "P"
            bitmap.height
        } else {
            imageOrientation = "H"
            bitmap.width
        }
        // creating a new bitmap with dominating dimension
        toReturnBitmap =
            Bitmap.createBitmap(dominatingDimen, dominatingDimen, Bitmap.Config.ARGB_8888)
        val paint = Paint()

        paint.color = Color.parseColor(color)
        // painting the background
        val canvas = Canvas(toReturnBitmap)
        canvas.drawPaint(paint)
        val top = 0f
        val left: Float = (toReturnBitmap.width / 2 - bitmap.width / 2).toFloat()
        val paintB = Paint()
        canvas.drawBitmap(bitmap, left, top, paintB)
        binding.picImageView.setImageBitmap(toReturnBitmap)

    }

    fun addTop(extraMargin: Float) {

        toReturnBitmap = bitmap

        val topHeight = (0.4 + extraMargin) * toReturnBitmap.height
        // creating a new Bitmap
        val tempBitmap = Bitmap.createBitmap(
            toReturnBitmap.width,
            (toReturnBitmap.height + topHeight).roundToInt(), Bitmap.Config.ARGB_8888
        )
        // adding the main bitmap
        val canvas = Canvas(tempBitmap)
        val cPaint = Paint()

        cPaint.color = Color.parseColor(color)
        canvas.drawPaint(cPaint)

        canvas.drawBitmap(bitmap, 0f, topHeight.toFloat(), null)
        toReturnBitmap = tempBitmap
        binding.picImageView.setImageBitmap(toReturnBitmap)
    }

    fun addCenter(extraMargin: Float) {
        toReturnBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val leftMargin = (0.1 + extraMargin) * bitmap.width
        val topMargin = (0.1 + extraMargin) * bitmap.height
        val canvas = Canvas(toReturnBitmap)
        val right = toReturnBitmap.width - leftMargin
        val bottom = toReturnBitmap.height - topMargin
        val paint = Paint()
        paint.color = Color.parseColor(color)
        canvas.drawRect(
            leftMargin.toFloat(), topMargin.toFloat(), right.toFloat(),
            bottom.toFloat(), paint
        )
        binding.picImageView.setImageBitmap(toReturnBitmap)


    }

    fun addBottom(extraMargin: Float) {
        toReturnBitmap = bitmap
        val bottomHeight = (0.4 + extraMargin) * toReturnBitmap.height
        // creating a new Bitmap
        val tempBitmap = Bitmap.createBitmap(
            toReturnBitmap.width,
            (toReturnBitmap.height + bottomHeight).roundToInt(), Bitmap.Config.ARGB_8888
        )
        // adding the main bitmap
        val canvas = Canvas(tempBitmap)
        val cPaint = Paint()
        if (color != null) {
            cPaint.color = Color.parseColor(color)
            canvas.drawPaint(cPaint)
        } else {
            cPaint.color = Color.WHITE
            canvas.drawPaint(cPaint)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        toReturnBitmap = tempBitmap
        binding.picImageView.setImageBitmap(toReturnBitmap)


    }

    fun manipulateTop(extraMargin: Float) {
        if (!this::toReturnBitmap.isInitialized) {
            toReturnBitmap = bitmap
        }
        val topHeight = (0.4 + extraMargin) * toReturnBitmap.height
        // creating a new Bitmap
        val tempBitmap = Bitmap.createBitmap(
            toReturnBitmap.width,
            (toReturnBitmap.height + topHeight).roundToInt(), Bitmap.Config.ARGB_8888
        )
        // adding the main bitmap
        val canvas = Canvas(tempBitmap)
        val cPaint = Paint()

        cPaint.color = Color.parseColor(color)
        canvas.drawPaint(cPaint)

        canvas.drawBitmap(bitmap, 0f, topHeight.toFloat(), null)
        toReturnBitmap = tempBitmap
        binding.picImageView.setImageBitmap(toReturnBitmap)
    }

    fun manipulateCenter(extraMargin: Float) {
        toReturnBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val leftMargin = (0.1 + extraMargin) * bitmap.width
        val topMargin = (0.1 + extraMargin) * bitmap.height
        val canvas = Canvas(toReturnBitmap)
        val right = toReturnBitmap.width - leftMargin
        val bottom = toReturnBitmap.height - topMargin
        val paint = Paint()
        paint.color = Color.parseColor(color)
        canvas.drawRect(
            leftMargin.toFloat(), topMargin.toFloat(), right.toFloat(),
            bottom.toFloat(), paint
        )
        binding.picImageView.setImageBitmap(toReturnBitmap)


    }

    fun manipulateBottom(extraMargin: Float) {
        if (!this::toReturnBitmap.isInitialized) {
            toReturnBitmap = bitmap
        }
        val bottomHeight = (0.4 + extraMargin) * toReturnBitmap.height
        // creating a new Bitmap
        val tempBitmap = Bitmap.createBitmap(
            toReturnBitmap.width,
            (toReturnBitmap.height + bottomHeight).roundToInt(), Bitmap.Config.ARGB_8888
        )
        // adding the main bitmap
        val canvas = Canvas(tempBitmap)
        val cPaint = Paint()
        if (color != null) {
            cPaint.color = Color.parseColor(color)
            canvas.drawPaint(cPaint)
        } else {
            cPaint.color = Color.WHITE
            canvas.drawPaint(cPaint)
        }
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        toReturnBitmap = tempBitmap
        binding.picImageView.setImageBitmap(toReturnBitmap)


    }

    private fun returnBitmap(): Bitmap {
        return if (this::toReturnBitmap.isInitialized) {
            toReturnBitmap
        } else {
            bitmap
        }
    }

    inner class Margins {
        var lMarginPercent = 0f
        var rMarginPercent = 0f
        var tMarginPercent = 0f
        var bMarginPercent = 0f
        var allMarginsPercent = 0f
    }

    companion object {
        private const val TAG = "PopupImageCrop"
    }
}