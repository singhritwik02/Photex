package com.ritwik.photex

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toRectF
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ritwik.photex.databinding.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.math.roundToInt


class CreateFragment : Fragment() {

    lateinit var modifiedBitmap: Bitmap
    var _binding: FragmentCreateBinding? = null
    val binding get() = _binding!!
    private lateinit var backGroundColor: String
    private lateinit var imageLink: String
    lateinit var mainBitmap: Bitmap
    private var hasBackGround: Boolean = true
    private lateinit var mode: String
    private val itemArray = arrayListOf<Items>()
    var selectedItem: Items? = null
    private lateinit var layerRecycler: LayerRecycler
    lateinit var menuFragmentManager: MenuFragmentManager
    val GALLERY_IMAGE = 101
    var stickerPopup = StickerPopup()
    private var fragmentContainer: Int = 0
    private lateinit var watermarkBitmap: Bitmap

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        val bundle = arguments

        if (container != null) {
            fragmentContainer = container.id
        }
        if (bundle != null) {
            val mode = bundle.getString("MODE", "")
            if (mode == "BLANK") {
                this.mode = "BLANK"
                hasBackGround = true
                val chooseColorClass = context?.let { ChooseColorClass(it) }
                binding.fcWaitingImage.visibility = View.GONE
                binding.root.post {
                    mainBitmap = createNewBitmap("#FFFFFF", 2048)
                    binding.fcMainImage.setImageBitmap(mainBitmap)
                    chooseColorClass?.showChooseColorPopup("BLANK", null)
                    {
                        binding.fcMainImage.setImageBitmap(it)
                        mainBitmap = it
                        modifiedBitmap = it
                    }
                }
            }
            if (mode == "TEMPLATE") {
                this.mode = mode
                hasBackGround = false
                imageLink = bundle.getString("LINK", "")

            }
            if (mode == "GALLERY") {
                Log.d(TAG, "onCreateView: Gallery Mode")
                this.mode = mode
                binding.fcWaitingImage.visibility = View.GONE
                chooseImage(GALLERY_IMAGE)


            }
        }
        binding.root.post {

            menuFragmentManager = MenuFragmentManager()
            menuFragmentManager.showMainMenuFragment()
        }
        if (this::backGroundColor.isInitialized) {
            mainBitmap = setBitmap()
            val canvas = Canvas(mainBitmap)
            canvas.drawColor(Color.parseColor(backGroundColor))
            setMainBitmapAs(mainBitmap)
            modifiedBitmap = mainBitmap

        } else if (this::imageLink.isInitialized) {
            getBitmapFromURL(imageLink)
            {


                if (it != null) {
                    mainBitmap = it
                }
                if (it != null) {
                    Log.d(TAG, "onCreateView: Bitmap is not null")
                    setMainBitmapAs(it)
                    val chooseColorClass = context?.let { ChooseColorClass(it) }
                    binding.root.post {

//                        chooseColorClass?.showChooseColorPopup("TEMPLATE", it)
//                        {
//                            mainBitmap = it
//                            binding.fcMainImage.setImageBitmap(it)
//                            Log.d(
//                                TAG,
//                                "onCreateView: image View width = ${binding.fcMainImage.width}, height = ${binding.fcMainImage.height}"
//                            )
//                            Log.d(
//                                TAG,
//                                "onCreateView: bitmap width = ${it.height}, height  = ${it.height}"
//                            )
//                        }
                        val cropClass = PopupImageCrop(context!!, it)
                        {

                        }
                        cropClass.showWindow()


                    }
                }
            }
        }



        binding.fcMainImage
            .setOnTouchListener { p0, p1 ->
                p1?.let {
                    var initX = 0f
                    var initY = 0f
                    if (it.action == MotionEvent.ACTION_DOWN) {
                        initX = it.x
                        initY = it.y
                        Log.d(TAG, "onCreateView: touch down")
                    }
                    if (it.action == MotionEvent.ACTION_UP) {
                        if (initX == it.x && initY == it.y) {
                            Log.d(TAG, "onCreateView: tapped")
                        } else {
                            Log.d(TAG, "onCreateView: Dragged")
                        }
                    }

                    if (selectedItem != null) {
                        val index = itemArray.indexOf(selectedItem)
                        val x = it.x
                        val y = it.y
                        val imageMatrix: Matrix = binding.fcMainImage.getImageMatrix()
                        val inverseMatrix = Matrix()
                        imageMatrix.invert(inverseMatrix)

                        val dst = FloatArray(2)
                        inverseMatrix.mapPoints(
                            dst,
                            floatArrayOf(x, y)
                        )
                        val dstX: Float = dst[0]
                        val dstY: Float = dst[1]
                        if (selectedItem!!.type == "TEXT") {
                            val item = selectedItem!!
                            val paint = item.paint
                            val bounds = Rect()
                            paint.getTextBounds(item.text, 0, item.text.length, bounds)

                            itemArray[index].locationX = dstX - bounds.width() / 2
                            itemArray[index].locationY = dstY - bounds.height() / 2
                        } else {
                            val bitmap = selectedItem!!.getStickerBitmap()
                            itemArray[index].locationX = dstX - bitmap.width / 2
                            itemArray[index].locationY = dstY - bitmap.height / 2
                        }

                        reDrawBitmap()
                    } else {
                        Toast.makeText(context, "Select an item to edit", Toast.LENGTH_SHORT).show()
                    }

                }
                true
            }

        return binding.root
    }

    fun getWatermark() {
        val assetManager = context!!.assets

        val istr: InputStream
        var bitmap: Bitmap? = null
        try {
            istr = assetManager.open("created_with_photex.png")
            bitmap = BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            // handle exception
            e.printStackTrace()
        }
        if (bitmap != null) {
            watermarkBitmap = BitmapFunctions.getResizedBitmap(bitmap, mainBitmap, 0.05F, "H")
        }

    }

    fun showRotatePopup() {
        if (selectedItem == null) {
            Toast.makeText(context, "Select an item to Rotate", Toast.LENGTH_SHORT).show()
            return
        }
        val rotateBinding = PopupShowRotationBinding.inflate(layoutInflater)
        val window = PopupWindow(
            rotateBinding.root,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        with(window)
        {
            elevation = 100f
            showAtLocation(rotateBinding.root, Gravity.BOTTOM, 0, 0)

        }
        val item = selectedItem!!
        rotateBinding.psrCustomDegree.setText(item.rotation.toString())
        rotateBinding.psrRotateLeft
            .setOnClickListener {
                var temp = item.rotation - 90f
                if (temp < 0) {
                    temp = 360 - (0 - temp)
                }

                val percent = ((100 / 360).toFloat() * temp)
                Log.d(TAG, "showRotatePopup: percent = $percent")
                rotateBinding.psrRotationBar.progress = percent.roundToInt()
            }
        rotateBinding.psrRotateRight
            .setOnClickListener {
                var temp = item.rotation + 90f
                if (temp >= 360f) {
                    temp -= 360
                }
                val percent = (100 / 360.toFloat()) * temp
                Log.d(TAG, "showRotatePopup: percent = $percent")
                rotateBinding.psrRotationBar.progress = percent.toInt()
            }
        rotateBinding.psrResetButton
            .setOnClickListener {
                val percent = ((100 / 360).toFloat() * 0)
                Log.d(TAG, "showRotatePopup: percent = $percent")
                rotateBinding.psrRotationBar.progress = percent.roundToInt()
            }
        rotateBinding.psrRotationBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    p0?.let {

                        Log.d(TAG, "onProgressChanged: ${p0.progress}")
                        val degreeRotation = (360 / 100.toFloat()) * it.progress
                        Log.d(TAG, "onProgressChanged: $degreeRotation")
                        item.rotation = degreeRotation
                        rotateBinding.psrCustomDegree.setText(item.rotation.toString())
                        reDrawBitmap()
                    }

                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    Log.d(TAG, "onStartTrackingTouch: ")
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            }
        )
        reDrawBitmap()

    }


    private fun setMainBitmapAs(bitmap: Bitmap) {
        binding.fcWaitingImage.visibility = View.GONE
        binding.fcMainImage.setImageBitmap(bitmap)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addText(text: String) {
        val itemRef = Items()
        val paint = Paint()
        itemRef.type = "TEXT"
        itemRef.text = text
        val presets = Presets(context!!)
        paint.textSize = presets.preSize.toFloat()
        if (presets.preFont != presets.DEFAULT_FONT) {

            val file =
                context!!.getExternalFilesDir("${Environment.DIRECTORY_DOCUMENTS}/Fonts/${presets.preFont}")
            file?.let {
                if (it.exists()) {
                    paint.setTypeface(Typeface.createFromFile(it))
                } else {
                    Toast.makeText(
                        context!!,
                        "File ${presets.preFont} does not exist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        paint.color = try {
            Color.parseColor(presets.preTextColor)
        } catch (e: Exception) {
            Color.WHITE
        }
        paint.typeface = if (presets.preFont != presets.DEFAULT_FONT) {
            val file =
                context!!.getExternalFilesDir("${Environment.DIRECTORY_DOCUMENTS}/Fonts/${presets.preFont}")
            val face = Typeface.createFromFile(file!!)
            face
        } else {
            Typeface.DEFAULT
        }

        itemRef.paint = paint
        itemRef.locationX = (mainBitmap.width / 2).toFloat()
        itemRef.locationY = (mainBitmap.height / 2).toFloat()
        itemRef.size = 14f
        itemArray.add(itemRef)
        layerRecycler.updateAdapter()
        selectedItem = itemRef
        reDrawBitmap()
        itemArray.indexOf(itemRef)

        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.NO_GRAVITY
        )
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
            Toast.makeText(context, "Failed to load Template", Toast.LENGTH_SHORT).show()
            // Handle any errors
        }
    }


    private fun setBitmap(): Bitmap {
        val w = 720
        val h = 720
        val config = Bitmap.Config.ARGB_8888
        val bitmap = Bitmap.createBitmap(w, h, config)
        return bitmap
    }


    companion object {
        private const val TAG = "CreateFragment"
        val watermarkPaint = Paint()

    }


    fun saveBitmap(bm: Bitmap) {
        val bmp = bm.copy(Bitmap.Config.ARGB_8888, true)
        // val canvas = Canvas(bmp)
        //val position = getScaledLocation(itemArray[0].locationX, itemArray[0].locationY)
        //val paint = itemArray[0].paint

        //canvas.drawText("hello", position[0], position[1], paint)

        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/req_images")
        myDir.mkdirs()
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val fname = "Image-$n.jpg"
        val file = File(myDir, fname)
        Log.i(TAG, "" + file)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPreview(): Bitmap {
        if (this::modifiedBitmap.isInitialized) {
            return modifiedBitmap
        } else {
            return mainBitmap
        }
    }

    fun reDrawBitmap() {
        // setting presets

        val proxy = mainBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(proxy!!)
        // setting the thumbnail


        for (item in itemArray) {
            if (item.type == "TEXT") {
                canvas.save()
                val paint = item.paint
                val bounds = Rect()
                paint.getTextBounds(item.text, 0, item.text.length, bounds)
                canvas.rotate(
                    item.rotation, item.locationX + bounds.width() / 2,
                    item.locationY + bounds.height() / 2
                )

                val text = item.text

                if (item.backgroundMargins != null) {
                    val xMargin = item.backgroundMargins!!.marginX
                    val yMargin = item.backgroundMargins!!.marginY
                    val fontMetrics = paint.fontMetrics
                    paint.getFontMetrics(fontMetrics)
                    val rect = Rect(
                        (item.locationX - xMargin).toInt(),
                        (item.locationY + fontMetrics.top - yMargin).toInt(),
                        (paint.measureText(text) + item.locationX + xMargin).toInt(),
                        (fontMetrics.bottom + item.locationY + yMargin).toInt()
                    )
                    val bPaint = item.backgroundMargins!!.backPaint
                    Log.d(TAG, "reDrawBitmap: textX = ${item.locationX},y = ${item.locationY}")
                    Log.d(
                        TAG,
                        "reDrawBitmap: left = ${rect.left}, top = ${rect.top}, right = ${rect.right}, bottom = ${rect.bottom}"
                    )
                    bPaint.alpha = item.backgroundAlpha
                    canvas.drawRect(rect, bPaint)
                    Log.d(TAG, "reDrawBitmap: Drawn Rect")

                } else {
                    Log.d(TAG, "reDrawBitmap: Null Back")
                }
                canvas.drawText(text, item.locationX, item.locationY, paint)
                item.strokePaint?.let {
                    it.textSize = paint.textSize
                    it.typeface = paint.typeface
                    canvas.drawText(text, item.locationX, item.locationY, it)
                }
                if (item.rotation != 0f) {
                    canvas.restore()
                }
            } else if (item.type == "STICKER") {

                item.getStickerBitmap().let {
                    canvas.save()
                    canvas.rotate(
                        item.rotation, item.locationX + it.width / 2,
                        item.locationY + it.height / 2
                    )
                    canvas.drawBitmap(
                        it,
                        (item.locationX),
                        (item.locationY),
                        item.paint
                    )
                    if (item.rotation != 0f) {
                        canvas.restore()
                    }
                }
            }
        }
        if (!this::watermarkBitmap.isInitialized) {
            val temp = BitmapFunctions.getBitmapFromAssets(context!!, "created_with_photex.png")
            if (temp != null) {

                watermarkBitmap = BitmapFunctions.getResizedBitmap(temp, mainBitmap, 0.05f, "H")
            }

        }
        // drawing the bitmap at the bottom left
        if (this::watermarkBitmap.isInitialized) {
            canvas.drawBitmap(
                watermarkBitmap,
                20f,
                (mainBitmap.height - (20 + watermarkBitmap.height)).toFloat(),
                null
            )
        }
        binding.fcMainImage.setImageBitmap(proxy)
        modifiedBitmap = proxy


    }

    inner class LayerRecycler(val recyclerView: RecyclerView) {
        private lateinit var adapter: RecyclerView.Adapter<ViewHolder>
        fun showRecycler() {
            adapter = object : RecyclerView.Adapter<ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    val view =
                        LayoutInflater.from(context).inflate(R.layout.single_layer, parent, false)
                    return ViewHolder(view)
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    val selectedIndex = itemArray.indexOf(selectedItem)
                    if (position == selectedIndex) {
                        if (selectedItem!!.type == "TEXT") {
                            if (this@CreateFragment::menuFragmentManager.isInitialized) {
                                menuFragmentManager.showEditOption()
                            }
                        } else if (selectedItem!!.type == "STICKER") {

                            if (this@CreateFragment::menuFragmentManager.isInitialized) {
                                menuFragmentManager.showStickerOptions()
                            }
                        }
                        holder.setSelected()
                    } else {
                        holder.setUnselected()
                    }
                    if (itemArray[position].type == "TEXT") {
                        holder.image.visibility = View.GONE
                        holder.text.text = itemArray[position].text
                    } else {
                        holder.image.visibility = View.VISIBLE
                        holder.image.setImageBitmap(itemArray[position].getStickerThumbail())

                    }
                    holder.itemView.setOnClickListener {
                        selectedItem = itemArray[position]
                        updateAdapter()
                        if (selectedItem!!.type == "TEXT") {
                            if (this@CreateFragment::menuFragmentManager.isInitialized) {
                                menuFragmentManager.showEditOption()
                            }
                        } else if (selectedItem!!.type == "STICKER") {

                            if (this@CreateFragment::menuFragmentManager.isInitialized) {
                                menuFragmentManager.showStickerOptions()
                            }
                        }

                    }
                }

                override fun getItemCount(): Int {
                    return itemArray.size
                }
            }

            recyclerView.adapter = adapter
            updateAdapter()
        }

        fun updateAdapter() {
            if (itemArray.size == 0) {
                // binding.fcAddItemsLabel.visibility = View.VISIBLE
            } else {
                // binding.fcAddItemsLabel.visibility = View.GONE
            }
            if (this::adapter.isInitialized) {
                adapter.notifyDataSetChanged()
            }
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val mainBack = itemView.findViewById<ConstraintLayout>(R.id.sl_mainBack)
            val text = itemView.findViewById<TextView>(R.id.sl_Text)
            val image = itemView.findViewById<ImageView>(R.id.sl_Image)
            fun setSelected() {
                mainBack.setBackgroundResource(R.drawable.layer_background_selected)
            }

            fun setUnselected() {
                mainBack.setBackgroundResource(R.drawable.layer_background)
            }

        }
    }

    fun showEditTextPopup(action: String) {
        if (action == "EDIT") {
            if (selectedItem == null) {
                return
            }
        }
        val binding = PopupChangeTextBinding.inflate(layoutInflater)
        val displaymetrics = resources.displayMetrics
        val window = PopupWindow(
            binding.root,
            (displaymetrics.widthPixels * 0.95).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        with(window)
        {
            showAtLocation(binding.root, Gravity.CENTER, 0, 0)
        }
        window.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        if (action == "EDIT") {
            binding.pctEditTextField.hint = selectedItem?.text ?: ""
        } else {
            binding.pctEditTextField.hint = "Enter text"
        }
        binding.pctDoneButton
            .setOnClickListener {
                if (binding.pctEditTextField.text.toString() != "") {
                    if (action == "EDIT") {
                        this.selectedItem!!.text = binding.pctEditTextField.text.toString()
                        reDrawBitmap()
                        layerRecycler.updateAdapter()
                    } else {
                        addText(binding.pctEditTextField.text.toString())
                    }
                }
                window.dismiss()
            }


    }

    fun addStroke(width: Int, colorS: String?) {

        if (selectedItem == null) {
            return
        }
        val item = selectedItem!!
        if (colorS == null) {
            item.strokePaint = null
            return
        }
        item.strokePaint = Paint()
        item.strokePaint?.apply {
            style = Paint.Style.STROKE
            strokeWidth = width.toFloat()
            this.color = Color.parseColor(colorS)
        }
    }


    private fun createNewBitmap(selectedColor: String, dmension: Int): Bitmap {

        val config = Bitmap.Config.ARGB_8888
        val bitmap = Bitmap.createBitmap(dmension, dmension, config)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.parseColor(selectedColor)
        canvas.drawPaint(paint)
        return bitmap
    }


    fun getFontsList() {
        var map = mapOf<String, String>()
        map += map.plus(Pair("Abhaya Libre", "abhaya_libre.ttf"))
        map += map.plus(Pair("Actor", "actor.ttf"))
        for (font in map) {
            Log.d(TAG, "getFontsList: ${font.key} = ${font.value}")
        }
        val fontDatabase = FontDatabase(context!!)
        val fontList = fontDatabase.getOfflineFonts()

    }

    inner class StickerPopup {
        private lateinit var waitingWindow: PopupWindow
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
                            val view = LayoutInflater.from(context)
                                .inflate(R.layout.single_sticker_show, parent, false)
                            return ViewHolder(view)
                        }

                        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                            holder.setImage(stickerLinks[position])
                            holder.itemView.setOnClickListener {
                                showWaiting()
                                getBitmapFromURL(stickerLinks[position])
                                {
                                    if (it != null) {
                                        addSticker(it)
                                        hideWaiting()
                                        window.dismiss()
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
                private val stickerImage = itemView.findViewById<ImageView>(R.id.sss_StickerImage)
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

        fun showWaiting() {
            val waitBinding = PopupLoadingStickerBinding.inflate(layoutInflater)
            val displayMetrics = resources.displayMetrics
            waitingWindow = PopupWindow(
                waitBinding.root,
                (displayMetrics.widthPixels * 0.95).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT,
                false
            )
            waitingWindow.elevation = 100f
            waitingWindow.showAtLocation(waitBinding.root, Gravity.CENTER, 0, 0)

        }

        fun hideWaiting() {
            if (this::waitingWindow.isInitialized) {
                if (waitingWindow.isShowing) {
                    waitingWindow.dismiss()
                }
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
            val manager = GridLayoutManager(context, 4)
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
                                    if (child.key?.contains(searchString) == true) {
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

    fun addSticker(bitmap: Bitmap) {
        val itemRef = Items()
        itemRef.type = "STICKER"
        itemRef.setStickerBitmap(bitmap)
        itemRef.locationY = (mainBitmap.height / 2).toFloat()
        itemRef.locationX - mainBitmap.width / 2
        itemRef.paint = Paint()
        itemArray.add(itemRef)
        selectedItem = itemRef
        layerRecycler.updateAdapter()
        reDrawBitmap()

    }

    inner class MenuFragmentManager() {
        lateinit var mainMenuFragment: MainMenuFragment
        private val container = binding.fcMenuFragment.id
        private lateinit var recyclerView: RecyclerView
        private lateinit var textOptionsBinding: LayoutTextOptionsBinding
        private lateinit var changeTextFragment: ChangeTextFragment
        private lateinit var changeSizeFragment: ChangeSizeFragment
        private lateinit var changeColorFragment: ChangeColorFragment
        private lateinit var changeStrokeFragment: ChangeStroke
        private lateinit var changeBackground: ChangeBackground
        val FRAGMENT_MAIN = "MAIN_MENU"
        val FRAGMENT_CHANGE_TEXT = "CHANGE_TEXT"
        val FRAGMENT_CHANGE_SIZE = "CHANGE_SIZE"
        val FRAGMENT_CHANGE_COLOR = "CHANGE_COLOR"
        val FRAGMENT_CHANGE_STROKE = "CHANGE_STROKE"
        val FRAGMENT_CHANGE_BACKGROUND = "CHANGE_BACKGROUND"
        var selectedFragment = FRAGMENT_CHANGE_TEXT
        fun showMainMenuFragment() {

            mainMenuFragment = MainMenuFragment(this@CreateFragment)
            activity!!.supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
                .replace(container, mainMenuFragment, FRAGMENT_MAIN).commit()
            showLayerRecycler()

        }

        fun showEditOption() {
            if (this::mainMenuFragment.isInitialized) {
                mainMenuFragment.changeToText()
            }
        }

        fun showStickerOptions() {
            if (this::mainMenuFragment.isInitialized) {
                mainMenuFragment.changeToSticker()
            }
        }

        fun showLayerRecycler() {
            val parent = binding.fcLayerSelectorLayout as ViewGroup
            parent.removeAllViews()
            recyclerView = RecyclerView(context!!)
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            params.gravity = Gravity.CENTER
            recyclerView.layoutParams = params
            parent.addView(recyclerView)
            val layoutManager =
                LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.layoutManager = layoutManager
            layerRecycler = LayerRecycler(recyclerView)
            layerRecycler.showRecycler()

        }

        fun showTextOptions() {
            val parent = binding.fcLayerSelectorLayout as ViewGroup
            parent.removeAllViews()
            if (!this::textOptionsBinding.isInitialized) {
                textOptionsBinding = LayoutTextOptionsBinding.inflate(layoutInflater)
            }
            if (textOptionsBinding.root.parent != null) {
                (textOptionsBinding.root.parent as ViewGroup).removeView(textOptionsBinding.root)
            }
            parent.addView(textOptionsBinding.root)

            with(textOptionsBinding)
            {
                ltoBackArrow
                    .setOnClickListener {
                        showMainMenuFragment()
                    }
                ltoSelectSize
                    .setOnClickListener {
                        showChangeSize()
                        setSelected(it)
                    }
                ltoSelectColor
                    .setOnClickListener {
                        showChangeColor()
                        setSelected(it)
                    }
                ltoEditText.setOnClickListener {
                    showChangeText()
                    setSelected(it)
                }
                ltoSelectStroke
                    .setOnClickListener {
                        showStroke()
                        setSelected(it)
                    }
                ltoSelectBackground.setOnClickListener {
                    showChangeBackground()
                    setSelected(it)
                }
                ltoSelectFont
                    .setOnClickListener {
                        showFontSelectionPopup()
                    }
            }
            setStartingFragment()
        }

        private fun setSelected(view: View) {
            deselectAll()
            (view as TextView).setBackgroundResource(R.drawable.selected_back)
        }

        private fun deselectAll() {
            if (!this::textOptionsBinding.isInitialized) {
                return
            }
            with(textOptionsBinding)
            {
                ltoSelectBackground.setBackgroundResource(0)
                ltoSelectStroke.setBackgroundResource(0)
                ltoEditText.setBackgroundResource(0)
                ltoSelectColor.setBackgroundResource(0)
                ltoSelectSize.setBackgroundResource(0)
                ltoSelectFont.setBackgroundResource(0)

            }
        }


        private fun showChangeBackground() {
            if (!this::changeBackground.isInitialized) {
                changeBackground = ChangeBackground(this@CreateFragment)
            }
            activity!!.supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
                .replace(container, changeBackground, FRAGMENT_CHANGE_BACKGROUND).commit()
            selectedFragment = FRAGMENT_CHANGE_BACKGROUND
        }

        private fun showStroke() {
            if (!this::changeStrokeFragment.isInitialized) {
                changeStrokeFragment = ChangeStroke(this@CreateFragment)
            }
            activity!!.supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
                .replace(container, changeStrokeFragment, FRAGMENT_CHANGE_STROKE).commit()
            selectedFragment = FRAGMENT_CHANGE_STROKE
        }


        private fun showChangeColor() {
            if (!this::changeColorFragment.isInitialized) {
                changeColorFragment = ChangeColorFragment(this@CreateFragment)
            }
            activity!!.supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
                .replace(container, changeColorFragment, FRAGMENT_CHANGE_COLOR).commit()
            selectedFragment = FRAGMENT_CHANGE_COLOR
        }

        fun showChangeSize() {
            if (!this::changeSizeFragment.isInitialized) {
                changeSizeFragment = ChangeSizeFragment(this@CreateFragment)


            }
            activity!!.supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
                .replace(container, changeSizeFragment, FRAGMENT_CHANGE_SIZE).commit()
            selectedFragment = FRAGMENT_CHANGE_SIZE
        }

        fun showChangeText() {
            if (!this::changeTextFragment.isInitialized) {
                changeTextFragment = ChangeTextFragment(this@CreateFragment)
            }
            activity!!.supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.scale_in, R.anim.scale_out)
                .replace(container, changeTextFragment, FRAGMENT_CHANGE_TEXT).commit()
            selectedFragment = FRAGMENT_CHANGE_TEXT
        }

        fun setStartingFragment() {
            Log.d(TAG, "setStartingFragment: $selectedFragment")
            when (selectedFragment) {
                FRAGMENT_CHANGE_TEXT -> {

                    showChangeText()
                }
                FRAGMENT_CHANGE_SIZE -> {
                    showChangeSize()
                }
                FRAGMENT_CHANGE_COLOR -> {
                    showChangeColor()
                }
                FRAGMENT_CHANGE_STROKE -> {
                    Log.d(TAG, "setStartingFragment: Showing change stroke fragment!")
                    showStroke()
                }
                FRAGMENT_CHANGE_BACKGROUND -> {
                    showChangeBackground()
                }
            }
        }

        fun getSelectedTag(): String {
            val fragment = activity!!.supportFragmentManager.findFragmentById(container)
            return fragment?.tag?.toString() ?: FRAGMENT_MAIN
        }
    }


    fun deleteItem() {
        if (selectedItem != null) {
            val index = itemArray.indexOf(selectedItem)
            itemArray.removeAt(index)
            layerRecycler.updateAdapter()
            reDrawBitmap()
            selectedItem = null
        } else {
            Toast.makeText(context, "Select an item to delete", Toast.LENGTH_SHORT).show()
        }
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
        fontBinding.psfLoadingLabel.visibility = View.VISIBLE
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
                            val paint = selectedItem!!.paint
                            val file =
                                context!!.getExternalFilesDir("${Environment.DIRECTORY_DOCUMENTS}/Fonts/${offlineFonts[position]}")
                            file?.let {
                                if (it.exists()) {
                                    paint.setTypeface(Typeface.createFromFile(it))
                                    presets.preFont = it.name
                                } else {
                                    Toast.makeText(context!!, "Cannot set Font", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                            reDrawBitmap()
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
        fontDatabase.loadFonts {

            if (fontBinding.psfLoadingLabel.visibility == View.VISIBLE) {
                fontBinding.psfLoadingLabel.visibility = View.GONE
            }
            offlineFontsRecycler.updateRecycler()

        }


        // on click listener for offline device fonts


    }

    fun getRandomName(): String {
        var name = "PhotexImg"
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        name = "$name$n"
        return name
    }

    fun saveImage() {
        var contentValues = ContentValues()
        val name = getRandomName()
        Log.d(TAG, "saveImage: Saving image as $name")
        contentValues.apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/*")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }
        val resolver = context!!.contentResolver
        try {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            val uri = resolver.insert(contentUri, contentValues)
            if (uri == null) {
                Log.d(TAG, "saveImage: Uri is null")
                Toast.makeText(context, "Failed to save Image", Toast.LENGTH_SHORT).show()
                return
            }
            val outputStream = resolver.openOutputStream(uri)
            if (outputStream != null) {
                modifiedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(context, "Image saved as $name", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "saveImage: Output stream is null")
            }


        } catch (e: Exception) {
            Toast.makeText(context, "Failed to Save Image", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }


    fun resizeBitmap(percent: Float): Bitmap {
        val defaultDimen = selectedItem!!.getDefaultStickerDimen()
        val targetBitmap = selectedItem!!.getOriginalStickerBitmap()
        val percent = percent
        val targettedWidth = defaultDimen[0] * percent
        Log.d(TAG, "resizeBitmap: targetted width = $targettedWidth")
        val targettedHeight = defaultDimen[1] * percent
        val scaledBitmap = Bitmap.createScaledBitmap(
            targetBitmap,
            targettedWidth.toInt(), targettedHeight.toInt(), false
        )
        return scaledBitmap
    }

    fun manipulateBitmapDimensions(change: Int): Bitmap {
        val targetBitmap = selectedItem!!.getOriginalStickerBitmap()
        val percent = change
        val targettedWidth = targetBitmap.width + percent
        Log.d(TAG, "resizeBitmap: targetted width = $targettedWidth")
        val targettedHeight = targetBitmap.height + percent
        val scaledBitmap = Bitmap.createScaledBitmap(
            targetBitmap,
            targettedWidth.toInt(), targettedHeight.toInt(), false
        )
        return scaledBitmap
    }

    fun showStickerResizePopup() {
        val srpBinding = PopupChangeSizeBinding.inflate(layoutInflater)
        val window = PopupWindow(
            srpBinding.root,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        window.animationStyle = R.style.pAnimation
        if (selectedItem == null) {
            return
        }
        when (selectedItem!!.stickerDimension) {
            "ORIGINAL" -> {
                srpBinding.pcsLarge.setBackgroundResource(0)
                srpBinding.pcsMedium.setBackgroundResource(0)
                srpBinding.pcsOriginal.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
                srpBinding.pcsSmall.setBackgroundResource(0)
            }
            "SMALL" -> {
                srpBinding.pcsLarge.setBackgroundResource(0)
                srpBinding.pcsMedium.setBackgroundResource(0)
                srpBinding.pcsOriginal.setBackgroundResource(0)
                srpBinding.pcsSmall.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
            }
            "MEDIUM" -> {
                srpBinding.pcsLarge.setBackgroundResource(0)
                srpBinding.pcsMedium.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
                srpBinding.pcsOriginal.setBackgroundResource(0)
                srpBinding.pcsSmall.setBackgroundResource(0)
            }
            "LARGE" -> {
                srpBinding.pcsLarge.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
                srpBinding.pcsMedium.setBackgroundResource(0)
                srpBinding.pcsOriginal.setBackgroundResource(0)
                srpBinding.pcsSmall.setBackgroundResource(0)
            }
        }
        var sticker = selectedItem!!.getStickerBitmap()
        window.showAtLocation(srpBinding.root, Gravity.BOTTOM, 0, 0)
        srpBinding.pcsStickerImage.setImageBitmap(sticker)
        srpBinding.pcsSmall.setOnClickListener {
            srpBinding.pcsLarge.setBackgroundResource(0)
            srpBinding.pcsMedium.setBackgroundResource(0)
            srpBinding.pcsOriginal.setBackgroundResource(0)
            srpBinding.pcsSmall.animate().alpha(0.2f).setDuration(150).withEndAction {

                srpBinding.pcsSmall.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
                srpBinding.pcsSmall.animate().alpha(1f).setDuration(150)

            }
            val temp = resizeBitmap(0.5f)
            sticker = temp
            selectedItem!!.setStickerBitmap(sticker)
            srpBinding.pcsStickerImage.animate().alpha(0.5f).setDuration(150).withEndAction {

                srpBinding.pcsStickerImage.setImageBitmap(sticker)
                srpBinding.pcsStickerImage.animate().alpha(1f).duration = 150

            }
            reDrawBitmap()
            selectedItem!!.stickerDimension = "SMALL"

        }
        srpBinding.pcsOriginal.setOnClickListener {
            srpBinding.pcsLarge.setBackgroundResource(0)
            srpBinding.pcsMedium.setBackgroundResource(0)
            srpBinding.pcsOriginal.animate().alpha(0.2f).setDuration(150).withEndAction {

                srpBinding.pcsOriginal.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
                srpBinding.pcsOriginal.animate().alpha(1f).duration = 150
            }
            srpBinding.pcsSmall.setBackgroundResource(0)
            val temp = resizeBitmap(1f)
            sticker = temp
            selectedItem!!.setStickerBitmap(sticker)
            srpBinding.pcsStickerImage.animate().alpha(0.5f).setDuration(150).withEndAction {

                srpBinding.pcsStickerImage.setImageBitmap(sticker)
                srpBinding.pcsStickerImage.animate().alpha(1f).duration = 150

            }
            reDrawBitmap()
            selectedItem!!.stickerDimension = "ORIGINAL"
        }
        srpBinding.pcsMedium.setOnClickListener {
            srpBinding.pcsLarge.setBackgroundResource(0)
            srpBinding.pcsMedium.animate().alpha(0.2f).withEndAction {

                srpBinding.pcsMedium.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
                srpBinding.pcsMedium.animate().alpha(1f).duration = 150
            }
            srpBinding.pcsOriginal.setBackgroundResource(0)
            srpBinding.pcsSmall.setBackgroundResource(0)
            val temp = resizeBitmap(1.5f)
            sticker = temp
            selectedItem!!.setStickerBitmap(sticker)
            srpBinding.pcsStickerImage.animate().alpha(0.5f).setDuration(150).withEndAction {

                srpBinding.pcsStickerImage.setImageBitmap(sticker)
                srpBinding.pcsStickerImage.animate().alpha(1f).duration = 150

            }
            reDrawBitmap()
            selectedItem!!.stickerDimension = "MEDIUM"
        }
        srpBinding.pcsLarge.setOnClickListener {
            srpBinding.pcsLarge.animate().alpha(0.2f).setDuration(150).withEndAction {

                srpBinding.pcsLarge.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
                srpBinding.pcsLarge.animate().alpha(1f).duration = 150
            }
            srpBinding.pcsMedium.setBackgroundResource(0)
            srpBinding.pcsOriginal.setBackgroundResource(0)
            srpBinding.pcsSmall.setBackgroundResource(0)
            val temp = resizeBitmap(2f)
            sticker = temp
            selectedItem!!.setStickerBitmap(sticker)
            srpBinding.pcsStickerImage.animate().alpha(0.5f).setDuration(150).withEndAction {

                srpBinding.pcsStickerImage.setImageBitmap(sticker)
                srpBinding.pcsStickerImage.animate().alpha(1f).duration = 150

            }
            reDrawBitmap()
            selectedItem!!.stickerDimension = "LARGE"
        }
        srpBinding.pcsExtraAdd.setOnClickListener {

            val originalWidth = selectedItem!!.getOriginalStickerBitmap().width
            val difference = selectedItem!!.getStickerBitmap().width - originalWidth
            val newDifference = difference + 50
            if (originalWidth + newDifference > mainBitmap.height) {
                Toast.makeText(context, "Cannot increase Size anymore!!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val temp = manipulateBitmapDimensions(+newDifference)
            sticker = temp
            selectedItem!!.setStickerBitmap(sticker)
            srpBinding.pcsStickerImage.animate().alpha(0.5f).setDuration(150).withEndAction {

                srpBinding.pcsStickerImage.setImageBitmap(sticker)
                srpBinding.pcsStickerImage.animate().alpha(1f).duration = 150

            }

            reDrawBitmap()
        }
        srpBinding.pcsExtraMinus.setOnClickListener {
            val difference =
                selectedItem!!.getStickerBitmap().width - selectedItem!!.getOriginalStickerBitmap().width
            val newDiff = difference - 50
            if (selectedItem!!.getOriginalStickerBitmap().width - newDiff < 50) {
                Toast.makeText(context, "Cannot decrease Size anymore!!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val temp = manipulateBitmapDimensions(newDiff)
            sticker = temp
            selectedItem!!.setStickerBitmap(sticker)
            srpBinding.pcsStickerImage.animate().alpha(0.5f).setDuration(150).withEndAction {

                srpBinding.pcsStickerImage.setImageBitmap(sticker)
                srpBinding.pcsStickerImage.animate().alpha(1f).duration = 150

            }

            reDrawBitmap()
        }

    }

    fun showAlphaPopup() {
        if (selectedItem == null) {
            return
        }
        val item = selectedItem!!
        val alphaBinding = PopupShowAlphaBinding.inflate(layoutInflater)
        val window = PopupWindow(
            alphaBinding.root,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        window.showAtLocation(alphaBinding.root, Gravity.BOTTOM, 0, 0)
        val progressF = (100 / 255.toFloat()) * item.paint.alpha
        val progress = progressF.toInt()
        alphaBinding.psaAlphaBar.progress = if (progress > 100) {
            Log.d(TAG, "setBackground: setting progress to $progress")
            progress
        } else if (progress < 0) {
            Log.d(TAG, "setBackground: setting progress to $progress")
            0
        } else {
            Log.d(TAG, "setBackground: setting progress to $progress")
            progress
        }
        alphaBinding.psaAlphaBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    p0?.let {
                        val alpha = (255 / 100.toFloat()) * it.progress
                        Log.d(TAG, "onProgressChanged: alpha = ${alpha.toInt()}")
                        item.paint.alpha = alpha.toInt()
                        reDrawBitmap()
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
    }

    fun getBitmapFromUri(uri: Uri): Bitmap {
        val inputStream = context!!.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        return bitmap

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1024 && resultCode == Activity.RESULT_OK) {
            data?.let {
                Log.d(TAG, "onActivityResult: Getting bitmap from uri, data is not null")
                val bitmap = it.data?.let { it1 -> getBitmapFromUri(it1) }
                if (bitmap != null) {
                    Log.d(TAG, "onActivityResult: Adding bitmap")
                    val cropImage = CropImageClass(bitmap)
                    cropImage.showPopup()
                    //val resizedBitmap = resizeGalleryImage(bitmap)

                    // addSticker(resizedBitmap)

                } else {
                    Log.d(TAG, "onActivityResult: Bitmap is null")
                }
            }
        }
        if (requestCode == GALLERY_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.let {
                Log.d(TAG, "onActivityResult: Getting bitmap from uri, data is not null")
                val bitmap = it.data?.let { it1 -> getBitmapFromUri(it1) }
                if (bitmap != null) {
                    mainBitmap = bitmap
                    setMainBitmapAs(bitmap)
                    val cropClass = PopupImageCrop(context!!, bitmap)
                    {
                        mainBitmap = it
                        setMainBitmapAs(it)
                        modifiedBitmap = mainBitmap
                    }
                    cropClass.showWindow()

                } else {

                    Toast.makeText(
                        context,
                        "Unable to load image,Please try again!!",
                        Toast.LENGTH_SHORT
                    ).show()
                    context?.let {
                        activity!!.supportFragmentManager.beginTransaction()
                            .replace(fragmentContainer, HomeFragment(), "HOME_FRAGMENT").commit()
                    }
                    Log.d(TAG, "onActivityResult: Bitmap is null")
                }
            }
        }
    }

    fun resizeGalleryImage(bitmap: Bitmap): Bitmap {
        val mainHeight = mainBitmap.height * 0.5.toFloat()
        val mainWidth = mainBitmap.width * 0.5.toFloat()
        var targetHeight = mainHeight
        var targetWidth = mainWidth
        val scale = bitmap.height / bitmap.width.toFloat()
        // getting the orientation of the image
        val orientation = if (bitmap.height >= bitmap.width) {
            "P"
        } else {
            "H"
        }
        if (orientation == "P") {
            targetWidth = targetHeight / scale
            Log.d(TAG, "resizeGalleryImage: target width = $targetWidth")
        } else {
            targetHeight = targetWidth * scale
            Log.d(TAG, "resizeGalleryImage: target height = $targetHeight")
        }
        if (targetHeight < 300) {
            targetHeight = 300f
            targetWidth = targetHeight / scale
            Log.d(
                TAG,
                "resizeGalleryImage: targetWidth = $targetWidth, targetHeight = $targetHeight"
            )

        }
        if (targetWidth < 300) {
            targetWidth = 300f
            targetHeight = targetWidth * scale
            Log.d(
                TAG,
                "resizeGalleryImage: targetWidth = $targetWidth, targetHeight = $targetHeight"
            )

        }
        val finalBitmap = Bitmap.createScaledBitmap(
            bitmap,
            targetWidth.roundToInt(), targetHeight.roundToInt(), false
        )
        return finalBitmap

    }

    fun chooseImage(code: Int) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), code)
    }

    inner class CropImageClass(val image: Bitmap) {
        private lateinit var cropBinding: PopupSelectImagePartBinding
        private lateinit var window: PopupWindow
        private lateinit var finalBitmap: Bitmap
        var selected = "SQUARE"

        @SuppressLint("ClickableViewAccessibility")
        fun showPopup() {
            cropBinding = PopupSelectImagePartBinding.inflate(layoutInflater)
            window = PopupWindow(
                cropBinding.root,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                true
            )
            window.animationStyle = R.style.pAnimation
            window.showAtLocation(cropBinding.root, Gravity.CENTER, 0, 0)
            cropBinding.psipImage.setImageBitmap(image)
            var mode = "SELECT"
            var startX = 0
            var startY = 0
            var endX = 0
            var endY = 0
            var selectionRect = Rect()
            var width = endX - startX
            var height = endY - startY
            cropBinding.psipShapeRectangle.setOnClickListener {
                cropBinding.psipShapeRectangle.animate().alpha(0.3f).setDuration(150)
                    .withEndAction {
                        cropBinding.psipShapeRectangle.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
                        cropBinding.psipShapeOval.setBackgroundResource(0)
                        cropBinding.psipMove.setBackgroundResource(0)
                        mode = "SELECT"
                        selected = "RECTANGLE"
                        cropBinding.psipShapeRectangle.animate().alpha(1f).setDuration(150)
                        redrawBitmap(selectionRect)
                    }

            }

            cropBinding.psipShapeOval.setOnClickListener {
                cropBinding.psipShapeOval.animate().alpha(0.3f).setDuration(150).withEndAction {
                    cropBinding.psipShapeRectangle.setBackgroundResource(0)
                    cropBinding.psipShapeOval.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
                    mode = "SELECT"
                    selected = "OVAL"
                    cropBinding.psipMove.setBackgroundResource(0)
                    cropBinding.psipShapeOval.animate().alpha(1f).setDuration(150)
                    redrawBitmap(selectionRect)
                }

            }
            cropBinding.psipMove.setOnClickListener {
                cropBinding.psipMove.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
                cropBinding.psipShapeRectangle.setBackgroundResource(0)
                cropBinding.psipShapeOval.setBackgroundResource(0)
                mode = "MOVE"
            }
            cropBinding.psipReset.setOnClickListener {
                cropBinding.psipReset.animate().alpha(0.2f).setDuration(200).withEndAction {

                    resetBitmap()
                    cropBinding.psipReset.animate().alpha(1f).setDuration(200)
                    startX = 0
                    startY = 0
                    endX = 0
                    endY = 0
                    width = 0
                    height = 0
                }
            }
            cropBinding.psipInsertButton.setOnClickListener {
                if (width == 0 || height == 0) {
                    val sticker = resizeGalleryImage(image)
                    addSticker(sticker)
                    window.dismiss()
                } else {
                    Toast.makeText(context, "Saving image", Toast.LENGTH_SHORT).show()
                    val temp = createBitmapFromRect(selectionRect)
                    if (temp != null) {
                        var sticker = if (selected == "OVAL") {
                            getCroppedBitmap(temp)
                        } else {
                            temp
                        }
                        if (sticker != null)
                            addSticker(sticker)
                        window.dismiss()
                    } else
                        Log.d(TAG, "showPopup:Null sticker")
                    startX = 0
                    startY = 0
                    endX = 0
                    endY = 0
                    width = 0
                    height = 0
                    this.redrawBitmap(selectionRect)

                }
            }
            cropBinding.psipImage.setOnTouchListener { view, motionEvent ->
                if (mode == "SELECT") {
                    if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                        Log.d(TAG, "showPopup: actiondown")
                        val x = motionEvent.x
                        val y = motionEvent.y
                        val imageMatrix: Matrix = cropBinding.psipImage.imageMatrix
                        val inverseMatrix = Matrix()
                        imageMatrix.invert(inverseMatrix)

                        val dst = FloatArray(2)
                        inverseMatrix.mapPoints(
                            dst,
                            floatArrayOf(x, y)
                        )
                        val dstX: Float = dst[0]
                        val dstY: Float = dst[1]

                        startX = dstX.toInt()

                        startY = dstY.toInt()

                    }
                    val x = motionEvent.x
                    val y = motionEvent.y
                    val imageMatrix: Matrix = cropBinding.psipImage.imageMatrix
                    val inverseMatrix = Matrix()
                    imageMatrix.invert(inverseMatrix)

                    val dst = FloatArray(2)
                    inverseMatrix.mapPoints(
                        dst,
                        floatArrayOf(x, y)
                    )
                    val dstX: Float = dst[0]
                    val dstY: Float = dst[1]
                    val tempX = dstX.toInt()
                    val tempY = dstY.toInt()
                    if (tempX < 1) {
                        Log.d(TAG, "showPopup: x is less than 1")
                    } else if (tempX > image.width) {
                        Log.d(TAG, "showPopup: temp x is greter than wisth")
                    } else if (tempY < 1) {
                        Log.d(TAG, "showPopup: y is less than 1")
                    } else if (tempY > image.height) {
                        Log.d(TAG, "showPopup: y is greater than image height")
                    } else {
                        endX = tempX
                        endY = tempY
                        selectionRect = Rect(startX, startY, endX, endY)
                        width = endX - startX
                        height = endY - startY
                        Log.d(TAG, "showPopup: rect = ${selectionRect.toString()}")
                        redrawBitmap(selectionRect)
                    }
                } else if (mode == "MOVE") {

                    val x = motionEvent.x
                    val y = motionEvent.y
                    val imageMatrix: Matrix = cropBinding.psipImage.imageMatrix
                    val inverseMatrix = Matrix()
                    imageMatrix.invert(inverseMatrix)

                    val dst = FloatArray(2)
                    inverseMatrix.mapPoints(
                        dst,
                        floatArrayOf(x, y)
                    )
                    val dstX: Float = dst[0]
                    val dstY: Float = dst[1]
                    startX = dstX.toInt()
                    startY = dstY.toInt()
                    if (startX + width / 2 > image.width)
                        Log.d(TAG, "showPopup: startX +width/2 >image.width ")
                    else if (startY + height / 2 > image.height)
                        Log.d(TAG, "showPopup: startY +height/2> image.height")
                    else if (startX - width / 2 < 0)
                        Log.d(TAG, "showPopup:startX -width/2<0 ")
                    else if (startY - height / 2 < 0)
                        Log.d(TAG, "showPopup:  startY -height/2 >0")
                    else {
                        startX -= width / 2
                        startY -= height / 2
                        selectionRect = Rect(startX, startY, startX + width, startY + height)
                        Log.d(TAG, "showPopup: rect = ${selectionRect}")
                        redrawBitmap(selectionRect)
                    }

                }

                return@setOnTouchListener true
            }
        }


        fun hidePopup() {
            if (this@CropImageClass::window.isInitialized) {
                if (window.isShowing)
                    window.dismiss()
            }
        }

        fun getCroppedBitmap(bitmap: Bitmap): Bitmap? {
            val output = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(output)
            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            canvas.drawOval(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
            //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
            //return _bmp;
            return output
        }

        //        fun getCircularBitmap(srcBitmap: Bitmap,rectP:Rect): Bitmap?
//        {
//
//             // Initialize a new instance of Bitmap
//             // Initialize a new instance of Bitmap
//             val dstBitmap = Bitmap.createBitmap(
//                 rectP.width(),  // Width
//                 rectP.height(),  // Height
//                 Bitmap.Config.ARGB_8888 // Config
//             )
//             val canvas = Canvas(dstBitmap)
//             // Initialize a new Paint instance
//             // Initialize a new Paint instance
//             val paint = Paint()
//             paint.isAntiAlias = true
//             val rectT = Rect(0, 0, rectP.width(), rectP.height())
//             val rectF = RectF(rectT)
//             canvas.drawOval(rectF, paint)
//             paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
//             // Calculate the left and top of copied bitmap
//             // Calculate the left and top of copied bitmap
//             val left = ((rectP.width() - srcBitmap.width) / 2).toFloat()
//             val top = ((rectP.width() - srcBitmap.height) / 2).toFloat()
//             val bitmap = createBitmapFromRect(rectP)
//             canvas.drawBitmap(bitmap, left, top, paint)
//             // Free the native object associated with this bitmap.
//             // Free the native object associated with this bitmap.
//             srcBitmap.recycle()
//             // Return the circular bitmap
//             // Return the circular bitmap
//             return dstBitmap
//        }
        fun resetBitmap() {
            finalBitmap = image.copy(Bitmap.Config.ARGB_8888, true)
            cropBinding.psipImage.setImageBitmap(finalBitmap)
        }

        fun createBitmapFromRect(rect: Rect): Bitmap? {
            try {

                val bitmap = Bitmap.createBitmap(
                    image,
                    rect.left,
                    rect.top,
                    rect.right - rect.left,
                    rect.bottom - rect.top
                )
                return bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    "Failed to select area, Try another section",
                    Toast.LENGTH_SHORT
                ).show()
                return null
            }

        }

        fun redrawBitmap(rect: Rect) {
            finalBitmap = image.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(finalBitmap)
            val paint = Paint()
            paint.color = Color.WHITE
            paint.alpha = 200
            if (selected == "OVAL") {
                canvas.drawOval(rect.toRectF(), paint)
            } else {
                canvas.drawRect(rect, paint)
            }
            cropBinding.psipImage.setImageBitmap(finalBitmap)

        }

    }

    fun getNamePrint() {
        val temp = BitmapFunctions.createWatermark(context!!, "TWITTER", "singhritwik02", mainBitmap)
        if (temp != null) {
            Log.d(TAG, "getNamePrint: temp is not null")
            addSticker(temp)
        } else {
            Log.d(TAG, "getNamePrint: temp is null")
        }

    }

}
