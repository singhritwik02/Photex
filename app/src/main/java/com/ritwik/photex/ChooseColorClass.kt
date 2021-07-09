package com.ritwik.photex

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ritwik.colortest.ColorDatabase
import com.ritwik.photex.databinding.PopupChooseColorBinding
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class ChooseColorClass(val context: Context) {
    private var selectedColor = "#FFFFFF"
    private lateinit var binding: PopupChooseColorBinding
    private var cropped = false;
    private var mode: String = ""
    var mainBitmap: Bitmap? = null
    var toReturnBitmap: Bitmap? = null
    var orientation = "VERTICAL"
    private var marginExtra = 0
    var selectedAlignment = "CENTER"
    fun showChooseColorPopup(
        mode: String,
        imageBitmap: Bitmap?,
        function: (editedBitmap: Bitmap) -> Unit
    ) {
        binding = PopupChooseColorBinding.inflate(LayoutInflater.from(context))
        val window = PopupWindow(
            binding.root,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT, true
        )
        window.animationStyle = R.style.pAnimation

        val colorHelper = ColorDatabase(context)
        try {
            selectedColor = colorHelper.getTopColor()
            Log.d(TAG, "showChooseColorPopup: $selectedColor")

            val color = Color.parseColor(selectedColor)
        } catch (e: Exception) {
            colorHelper.clearTable()
        }

        this.mode = mode
        if (mode == "BLANK") {
            val bitmap = Bitmap.createBitmap(2048, 2048, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            Log.d(TAG, "showChooseColorPopup: $selectedColor")
            canvas.drawColor(Color.parseColor(selectedColor))
            binding.pccChooseColorImage.setImageBitmap(bitmap)
            cropped = true
            toReturnBitmap = bitmap
           // binding.pccPresetLayout.visibility = View.GONE
            //binding.pccModifyLayout.visibility = View.GONE


        }
        else {
            Log.d(TAG, "showChooseColorPopup: Unidentified Mode")
            return
        }
        with(window)
        {

            showAtLocation(binding.root, Gravity.BOTTOM, 0, 0)
        }
        val recyclerClass =
            RecyclerClass(
                binding.pccRecycler,
                colorHelper.getColorList()
            )

        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        binding.pccRecycler.layoutManager = manager
        binding.pccColorPalette
            .setOnClickListener {
                ColorPickerDialog.Builder(context)
                    .setTitle("Choose Color")
                    .setPreferenceName("ChooseColor")
                    .setPositiveButton("Save",
                        ColorEnvelopeListener { envelope, fromUser ->
                            Log.d(TAG, "showChooseColorPopup: ")
                            selectedColor = "#${envelope.hexCode}"
                            colorHelper.addColor(selectedColor)
                            changeColor(selectedColor)
                            recyclerClass.updateRecycler()
                        })
                    .setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                    .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                    .show()

            }
        binding.pccCreateButton
            .setOnClickListener {

                toReturnBitmap?.let { function(it) }

                window.dismiss()
            }



        recyclerClass.showRecycler()


    }

    private fun fillImage() {
        val mB = mainBitmap!!
        val bitmap: Bitmap =
            Bitmap.createScaledBitmap(mB, 720, 720, false)
        toReturnBitmap = bitmap
        binding.pccChooseColorImage.setImageBitmap(toReturnBitmap)

    }

    private fun cropSquare() {
        //addBackground(selectedColor)
        createNew()
        cropped = true
    }


    private fun bottomOrLeftAlign() {

    }

    private inner class RecyclerClass(
        val recyclerView: RecyclerView,
        var list: ArrayList<String>
    ) {
        val colorHelper = ColorDatabase(context)
        var adapter: RecyclerView.Adapter<ViewHolder>? = null
        fun showRecycler() {

            adapter = object : RecyclerView.Adapter<ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    val view = LayoutInflater.from(context)
                        .inflate(R.layout.single_color, parent, false)
                    return ViewHolder(view)
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    holder.setColor(list[position])
                    holder.itemView.setOnClickListener {
                        selectedColor = list[position]
                        changeColor(selectedColor)
                        colorHelper.addColor(list[position])
                        list = colorHelper.getColorList()
                        notifyItemMoved(position, 0)
                        notifyItemRangeChanged(1, list.size)

                    }
                }

                override fun getItemCount(): Int {
                    return list.size
                }

            }
            recyclerView.adapter = adapter
            adapter?.notifyDataSetChanged()
        }

        fun updateRecycler() {

            list = colorHelper.getColorList()
            adapter?.notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView = itemView.findViewById<ImageView>(R.id.sc_Image)
            fun setColor(colorString: String) {
                imageView.setColorFilter(Color.parseColor(colorString));
            }
        }

    }
    private fun createNew()
    {
        if(mainBitmap!=null) {
            val bitmap = Bitmap.createBitmap(mainBitmap!!.width,mainBitmap!!.height,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint()
            canvas.drawBitmap(mainBitmap!!,0f,0f,paint)
            toReturnBitmap = bitmap
            binding.pccChooseColorImage.setImageBitmap(toReturnBitmap)
        }
        else
        {
            Log.d(TAG, "createNew: Main bitmap is null")
        }
    }
    private fun changeColor(color: String) {
        if (mode == "BLANK") {
            val bitmap = createNewBitmap(2048)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.parseColor(color))

            toReturnBitmap = bitmap
            binding.pccChooseColorImage.setImageBitmap(bitmap)
        } else if (mode == "TEMPLATE") {
            if (cropped) {
                addBackground(color)
                cropped = true

            } else {
                Toast.makeText(context, "Crop image to square first!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun addBackground(colorString: String?) {
        val alignment = selectedAlignment
        val color: String = colorString ?: "#FFFFFF"
        val toDrawBitmap = getSquaredBitmap(mainBitmap!!)
        val bitmap = createNewBitmap(toDrawBitmap.height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor(color))
        if (mainBitmap == null) {
            Toast.makeText(context, "Unable to Crop Image", Toast.LENGTH_SHORT).show()
            return
        }
        val bmp = mainBitmap!!

        var left = 0f
        var top = 0f
        if (alignment == "CENTER") {
            left =
                (toDrawBitmap.width / 2) - (bmp.width / 2).toFloat()
            top = (toDrawBitmap.height / 2) - (bmp.height / 2).toFloat()
        }
        if (alignment == "END") {
            left =
                (toDrawBitmap.width / 2) - (bmp.width / 2).toFloat()
            top = (toDrawBitmap.height / 2) - (bmp.height / 2).toFloat()
            left *= 2
            top *= 2
        }
        if (alignment == "START") {
            left = 0f
            top = 0f
        }
        if (orientation == "HORIZONTAL") {
            left += marginExtra
        } else {
            top += marginExtra
        }
        Log.d(TAG, "addBackground: value of left = $left")
        canvas.drawBitmap(bmp, left, top, null)
        toReturnBitmap = bitmap
        binding.pccChooseColorImage.setImageBitmap(toReturnBitmap)
    }

    private fun getSquaredBitmap(bitmap: Bitmap): Bitmap {
        var finalBitmap: Bitmap?
        val newDimension = if (bitmap.width > bitmap.height) {
            bitmap.width
        } else {
            bitmap.height
        }

        finalBitmap = Bitmap.createScaledBitmap(bitmap, newDimension, newDimension, false)


        return finalBitmap
    }

    private fun createNewBitmap(dmension: Int): Bitmap {

        val config = Bitmap.Config.ARGB_8888
        val bitmap = Bitmap.createBitmap(dmension, dmension, config)

        return bitmap
    }



    companion object {
        private const val TAG = "ChooseColorClass"
    }

}