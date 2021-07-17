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
import android.view.WindowManager
import android.widget.PopupWindow
import com.ritwik.photex.databinding.PopupImageCropBinding
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlin.math.roundToInt

class PopupImageCrop(val context: Context, val bitmap: Bitmap, val function: (bitmap: Bitmap) -> Unit) {
    private lateinit var toReturnBitmap: Bitmap
    private lateinit var imageOrientation: String
    private lateinit var window: PopupWindow
    private lateinit var binding: PopupImageCropBinding
    private var color: String = "#FFFFFF"
    var  status = "O"
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
            addCenter()
            status = "C"
        }
        binding.picTopAlign.setOnClickListener {
            toReturnBitmap = bitmap
         addBottom()
            status = "B"
        }
        binding.picBottomAlign.setOnClickListener {
            toReturnBitmap = bitmap
            addTop()
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
            if(!this::toReturnBitmap.isInitialized)
            {
                toReturnBitmap = bitmap
            }
            function(toReturnBitmap)
            window.dismiss()
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

    fun addTop() {
        if (!this::toReturnBitmap.isInitialized) {
            toReturnBitmap = bitmap
        }
        val topHeight = 0.4 * toReturnBitmap.height
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

    fun addCenter() {
        toReturnBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val leftMargin = 0.1 * bitmap.width
        val topMargin = 0.1 * bitmap.height
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

    fun addBottom() {
        if (!this::toReturnBitmap.isInitialized) {
            toReturnBitmap = bitmap
        }
        val bottomHeight = 0.4 * toReturnBitmap.height
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

    companion object {
        private const val TAG = "PopupImageCrop"
    }
}