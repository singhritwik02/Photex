package com.ritwik.photex

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ritwik.photex.databinding.PopupChooseTemplateBinding
import com.ritwik.photex.databinding.PopupShowFilterBinding
import java.io.File

class PopupChooseTemplate(
    val context: Context,
    val function: (bitmap: Bitmap, styleCode: String?) -> Unit
) {
    private lateinit var binding: PopupChooseTemplateBinding
    private lateinit var window: PopupWindow
    val filterPopup: Filters = Filters()
    private lateinit var recyclerClass: DefaultRecyclerClass
    private lateinit var firebaseRecycler: FirebaseRec
    private lateinit var loadingWindow: PopupWindow
    fun showPopup() {
        binding = PopupChooseTemplateBinding.inflate(LayoutInflater.from(context))
        window = PopupWindow(
            binding.root,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            true
        )
        window.animationStyle = R.style.slideAnimation
        window.showAtLocation(binding.root, Gravity.NO_GRAVITY, 0, 0)
        val recyclerView = binding.pctpRecycler
        val manager = GridLayoutManager(context,2,GridLayoutManager.VERTICAL,false)
        recyclerView.layoutManager = manager
        binding.pctpTemplateLabel.text = "Loading Templates"
        recyclerClass = DefaultRecyclerClass(recyclerView)
        firebaseRecycler = FirebaseRec()
        firebaseRecycler.showRecycler(recyclerView)
        binding.pctpSearchField.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "beforeTextChanged: ")
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "onTextChanged: ")
                }

                override fun afterTextChanged(p0: Editable?) {
                    setRecycler()
                }

            }
        )
        // Inflate the layout f or this fragment
        binding.pctpTrendingTab.setOnClickListener {
            firebaseRecycler.showTrending()
        }
        binding.pctpHotTab.setOnClickListener {
            firebaseRecycler.showMostDownloaded()
        }
        binding.pctpFilterButton.setOnClickListener {
            filterPopup.showFilterPopup()

        }


    }


    inner class DefaultRecyclerClass(val recyclerView: RecyclerView) {
        fun showRecycler(searchString: String) {
            getData(searchString) { imageList ->
                Log.d(TAG, "showRecycler: size of imageLIst - ${imageList.size}")
                finalShowRecycler(imageList)
            }
        }

        private fun finalShowRecycler(imageList: ArrayList<TemplateData>) {
            val imageList = imageList
            val adapter = object : RecyclerView.Adapter<ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    val view = LayoutInflater.from(context)
                        .inflate(R.layout.single_template, parent, false)
                    return ViewHolder(view)
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    Log.d(TAG, "onBindViewHolder: ")
                    if (binding.pctpTemplateLabel.text != "Available Templates") {
                        binding.pctpTemplateLabel.text = "Available Templates"
                    }
                    if(binding.pctLoadingView.visibility == View.VISIBLE)
                    {
                        binding.pctLoadingView.visibility = View.GONE
                    }
                    holder.setTemplateImage(imageList[position].downloadLink)
                    holder.itemView.setOnClickListener {
                        showLoading()
                        window.dismiss()
                        Toast.makeText(context, "Please Wait!", Toast.LENGTH_SHORT).show()
                        getBitmapFromURL(imageList[position].downloadLink)
                        {
                            if (it != null) {
                                function(it, imageList[position].styleCode)
                                hideLoading()
                            }
                        }
                    }
                }

                override fun getItemCount(): Int {
                    return imageList.size
                }


            }
            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        private fun getData(
            searchString: String,
            function: (imageList: ArrayList<TemplateData>) -> Unit
        ) {
            var imageList = arrayListOf<TemplateData>()
            val database =
                FirebaseDatabase.getInstance().reference.child("Templates").addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                Log.d(TAG, "onDataChange: Snapshot does not exist")
                                return
                            }
                            if (!snapshot.hasChildren()) {
                                Log.d(TAG, "onDataChange: snapshot does not have chldren")
                            } else {
                                Log.d(TAG, "onDataChange: snapshot has child")

                                for (child in snapshot.children) {
                                    val tempData = TemplateData()
                                    Log.d(TAG, "onDataChange: ${child.key}")

                                    var key = child.key.toString()
                                    key.replace("_", " ")
                                    val link = if (child.hasChild("LINK")) {
                                        val temp = child.child("LINK").value.toString()
                                        Log.d(TAG, "onDataChange: link = $temp")
                                        temp
                                    } else {
                                        Log.d(TAG, "onDataChange: does not have link")
                                        ""
                                    }
                                    val styleCode: String? = if (child.hasChild("STYLE_CODE")) {
                                        val code = child.child("STYLE_CODE").value.toString()
                                        Log.d(TAG, "onDataChange: style code = $code")
                                        code
                                    } else {
                                        null
                                    }
                                    // new algorithm
                                    if (filterPopup.filterList.size > 0) {
                                        // filter applied
                                        if (child.hasChild("STYLE_CODE")) {
                                            for (filter in filterPopup.filterList) {
                                                if (filter == filterPopup.TopText && styleCode == "T") {
                                                    if (searchString == "") {
                                                        tempData.downloadLink = link
                                                        tempData.styleCode = styleCode
                                                        imageList.add(tempData)
                                                    } else if (key.contains(searchString, true)) {
                                                        Log.d(TAG, "onDataChange: key matches")
                                                        tempData.downloadLink = link
                                                        tempData.styleCode = styleCode
                                                        imageList.add(tempData)
                                                    } else {
                                                        Log.d(
                                                            TAG,
                                                            "onDataChange: key does not match"
                                                        )
                                                        continue
                                                    }
                                                } else if (filter == filterPopup.BottomText && styleCode == "B") {
                                                    if (searchString == "") {
                                                        tempData.downloadLink = link
                                                        tempData.styleCode = styleCode
                                                        imageList.add(tempData)
                                                    } else
                                                        if (key.contains(searchString, true)) {
                                                            Log.d(TAG, "onDataChange: key matches")
                                                            tempData.downloadLink = link
                                                            tempData.styleCode = styleCode
                                                            imageList.add(tempData)
                                                        } else {
                                                            Log.d(
                                                                TAG,
                                                                "onDataChange: key does not match"
                                                            )
                                                            continue
                                                        }
                                                    break
                                                } else if (filter == filterPopup.Comparison && styleCode!!.length > 1) {
                                                    if (searchString == "") {
                                                        tempData.downloadLink = link
                                                        tempData.styleCode = styleCode
                                                        imageList.add(tempData)
                                                    } else
                                                        if (key.contains(searchString, true)) {
                                                            Log.d(TAG, "onDataChange: key matches")
                                                            tempData.downloadLink = link
                                                            tempData.styleCode = styleCode
                                                            imageList.add(tempData)
                                                        } else {
                                                            Log.d(
                                                                TAG,
                                                                "onDataChange: key does not match"
                                                            )
                                                            continue
                                                        }
                                                    break
                                                }

                                            }
                                        }
                                    } else {
                                        if (searchString == "") {
                                            Log.d(TAG, "onDataChange: search string empty")
                                            tempData.downloadLink = link
                                            tempData.styleCode = styleCode
                                            imageList.add(tempData)
                                        } else {
                                            Log.d(TAG, "onDataChange: search string not empty")
                                            // replacing _ with spaces in the key

                                            if (key.contains(searchString, true)) {
                                                Log.d(TAG, "onDataChange: key matches")
                                                tempData.downloadLink = link
                                                tempData.styleCode = styleCode
                                                imageList.add(tempData)
                                            } else {
                                                Log.d(TAG, "onDataChange: key does not match")
                                                continue
                                            }
                                        }
                                    }
//                                    if (searchString == "") {
//                                        Log.d(TAG, "onDataChange: search string empty")
//                                        tempData.downloadLink = link
//                                        tempData.styleCode = styleCode
//                                        imageList.add(tempData)
//                                    }
//                                    else {
//                                        Log.d(TAG, "onDataChange: search string not empty")
//                                        // replacing _ with spaces in the key
//                                        var key = child.key.toString()
//                                        key.replace("_", " ")
//                                        if (filterPopup.filterList.isEmpty()) {
//                                            if (key.contains(searchString, true)) {
//                                                Log.d(TAG, "onDataChange: key matches")
//                                                tempData.downloadLink = link
//                                                tempData.styleCode = styleCode
//                                                imageList.add(tempData)
//                                            } else {
//                                                Log.d(TAG, "onDataChange: key does not match")
//                                                continue
//                                            }
//                                        } else {
//                                            if (styleCode == null) {
//                                                if (key.contains(searchString, true)) {
//                                                    Log.d(TAG, "onDataChange: key matches")
//                                                    tempData.downloadLink = link
//                                                    tempData.styleCode = styleCode
//                                                    imageList.add(tempData)
//                                                } else {
//                                                    Log.d(TAG, "onDataChange: key does not match")
//                                                    continue
//                                                }
//                                            }
//                                            for (filter in filterPopup.filterList) {
//                                                if (filter == filterPopup.TopText && styleCode == "T") {
//                                                    if (key.contains(searchString, true)) {
//                                                        Log.d(TAG, "onDataChange: key matches")
//                                                        tempData.downloadLink = link
//                                                        tempData.styleCode = styleCode
//                                                        imageList.add(tempData)
//                                                    } else {
//                                                        Log.d(
//                                                            TAG,
//                                                            "onDataChange: key does not match"
//                                                        )
//                                                        continue
//                                                    }
//                                                } else if (filter == filterPopup.BottomText && styleCode == "B") {
//                                                    if (key.contains(searchString, true)) {
//                                                        Log.d(TAG, "onDataChange: key matches")
//                                                        tempData.downloadLink = link
//                                                        tempData.styleCode = styleCode
//                                                        imageList.add(tempData)
//                                                    } else {
//                                                        Log.d(
//                                                            TAG,
//                                                            "onDataChange: key does not match"
//                                                        )
//                                                        continue
//                                                    }
//                                                    break
//                                                } else if (filter == filterPopup.Comparison && styleCode!!.length > 1) {
//                                                    if (key.contains(searchString, true)) {
//                                                        Log.d(TAG, "onDataChange: key matches")
//                                                        tempData.downloadLink = link
//                                                        tempData.styleCode = styleCode
//                                                        imageList.add(tempData)
//                                                    } else {
//                                                        Log.d(
//                                                            TAG,
//                                                            "onDataChange: key does not match"
//                                                        )
//                                                        continue
//                                                    }
//                                                    break
//                                                }
//
//                                            }
//
//
//                                        }
//
//                                    }
                                }
                                function(imageList)

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d(TAG, "onCancelled: ")
                        }

                    }
                )
        }


        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val image = itemView.findViewById<ImageView>(R.id.st_Image)
            fun setTemplateImage(imageUrl: String) {
                Log.d(TAG, "setTemplateImage: loading image $imageUrl")
                context?.let { Glide.with(context!!).load(imageUrl).into(image)
                }
            }
        }
    }


    companion object {
        private const val TAG = "Template"
    }

    fun showTrending() {

    }

    fun showNew() {

    }

    inner class FirebaseRec {
        val latestQuery =
            FirebaseDatabase.getInstance().reference.child("Templates").orderByChild("TIMESTAMP")
        val mostDownloadedQuery =
            FirebaseDatabase.getInstance().reference.child("Templates").orderByChild("TIMES_USED")
        var mostDownloadedOptions: FirebaseRecyclerOptions<TemplateModel> =
            FirebaseRecyclerOptions.Builder<TemplateModel>()
                .setQuery(mostDownloadedQuery, TemplateModel::class.java)
                .build()
        var latestOptions: FirebaseRecyclerOptions<TemplateModel> =
            FirebaseRecyclerOptions.Builder<TemplateModel>()
                .setQuery(latestQuery, TemplateModel::class.java)
                .build()
        val firebaseAdapter =
            object : FirebaseRecyclerAdapter<TemplateModel, ViewHolder>(latestOptions) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    val view = LayoutInflater.from(context)
                        .inflate(R.layout.single_template, parent, false)
                    return ViewHolder(view)
                }

                override fun onBindViewHolder(
                    holder: ViewHolder,
                    position: Int,
                    model: TemplateModel
                ) {
                    Log.d(TAG, "onBindViewHolder: ")
                    if (binding.pctpTemplateLabel.text != "Available Templates") {
                        binding.pctpTemplateLabel.text = "Available Templates"
                    }
                    if(binding.pctLoadingView.visibility == View.VISIBLE)
                    {
                        binding.pctLoadingView.visibility = View.GONE
                    }
                    holder.setImage(model.link)
                    val styleCode: String? = if (model.STYLE_CODE != null) {
                        model.STYLE_CODE
                    } else {
                        null
                    }
                    holder.itemView.setOnClickListener {
                        window.dismiss()
                        if (CloudDatabase.isLoggedIn()) {
                            val key = model.name
                            val timesUsed: Long = model.TIMES_USED
                            Log.d(TAG, "onBindViewHolder: key = $key, Times Used = $timesUsed")
                            incrementCount(key, timesUsed)
                        } else {
                            Log.d(TAG, "onBindViewHolder: User not Logged In")
                        }
                        Toast.makeText(context, "Please Wait!", Toast.LENGTH_SHORT).show()
                        showLoading()
                        getBitmapFromURL(model.link)
                        {
                            if (it != null) {
                                function(it, styleCode)

                            }
                        }
                    }
                }


            }

        fun showRecycler(recyclerView: RecyclerView) {
            recyclerView.adapter = firebaseAdapter
            firebaseAdapter.startListening()
            firebaseAdapter.notifyDataSetChanged()


        }

        fun showTrending() {
            binding.pctpTrendingTab.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
            binding.pctpTrendingText.setTextColor(Color.BLACK)
            binding.pctpHotTab.setBackgroundResource(0)
            binding.pctpNewText.setTextColor(Color.DKGRAY)
            firebaseAdapter.updateOptions(latestOptions)
            firebaseAdapter.notifyDataSetChanged()

        }

        fun showMostDownloaded() {
            binding.pctpTrendingTab.setBackgroundResource(0)
            binding.pctpTrendingText.setTextColor(Color.DKGRAY)
            binding.pctpHotTab.setBackgroundResource(R.drawable.bottom_line_yellow_bold)
            binding.pctpNewText.setTextColor(Color.BLACK)
            firebaseAdapter.updateOptions(mostDownloadedOptions)
            firebaseAdapter.notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val image = itemView.findViewById<ImageView>(R.id.st_Image)
            fun setImage(link: String) {
                context.let { Glide.with(context).load(link).into(image) }
            }

        }


    }

    fun getBitmapFromURL(src: String, function: (bitmap: Bitmap?) -> Unit) {
        var bitmap: Bitmap?
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(src)

        val localFile = File.createTempFile("template", "jpg")

        storageRef.getFile(localFile).addOnSuccessListener {
            Log.d(TAG, "getBitmapFromURL: Success")
            bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            hideLoading()
            function(bitmap)
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load Template", Toast.LENGTH_SHORT).show()
            // Handle any errors
        }
    }

    fun incrementCount(key: String, timesUsed: Long) {
        FirebaseDatabase.getInstance().reference.child("Templates").child(key).child("TIMES_USED")
            .setValue(timesUsed - 1)
    }

    inner class Filters {
        val TopText = "TOP_TEXT"
        val BottomText = "BOTTOM_TEXT"
        val Comparison = "COMPARISON"
        private lateinit var window: PopupWindow
        private lateinit var filterBinding: PopupShowFilterBinding
        val filterList = arrayListOf<String>()

        fun showFilterPopup() {
            filterBinding = PopupShowFilterBinding.inflate(LayoutInflater.from(context))
            val displayMetrics = context.resources.displayMetrics
            window = PopupWindow(
                filterBinding.root,
                (displayMetrics.widthPixels * 0.95).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT,
                true
            )
            window.animationStyle = R.style.pAnimation
            window.elevation = 100f
            window.showAtLocation(filterBinding.root, Gravity.CENTER, 0, 0)
            for (filter in filterList) {
                if (filter == TopText) {
                    filterBinding.psftTopTextCheckBox.isChecked = true
                }
                if (filter == BottomText) {
                    filterBinding.psftBottomTextCheckBox.isChecked = true
                }
                if (filter == Comparison) {
                    filterBinding.psftComparisionCheckBox.isChecked = true
                }
            }
            // setting on check changed listener for top text check box
            filterBinding.psftTopTextCheckBox.setOnCheckedChangeListener { compoundButton, checked ->
                if (checked) {
                    if (!filterList.contains(TopText)) {
                        filterList.add(TopText)
                    }
                } else {
                    if (filterList.contains(TopText)) {
                        filterList.remove(TopText)
                    }
                }
                setRecycler()
            }
            // bottom text
            filterBinding.psftBottomTextCheckBox.setOnCheckedChangeListener { compoundButton, checked ->
                if (checked) {
                    if (!filterList.contains(BottomText)) {
                        filterList.add(BottomText)
                    }
                } else {
                    if (filterList.contains(BottomText)) {
                        filterList.remove(BottomText)
                    }
                }
                setRecycler()
            }
            // comparison
            filterBinding.psftComparisionCheckBox.setOnCheckedChangeListener { compoundButton, checked ->
                if (checked) {
                    if (!filterList.contains(Comparison)) {
                        filterList.add(Comparison)
                    }
                } else {
                    if (filterList.contains(Comparison)) {
                        filterList.remove(Comparison)
                    }
                }
                setRecycler()
            }
            // on click listener for reset button
            filterBinding.psftClearButton.setOnClickListener {
                clearFilterList()
                filterBinding.psftTopTextCheckBox.isChecked = false
                filterBinding.psftBottomTextCheckBox.isChecked = false
                filterBinding.psftComparisionCheckBox.isChecked = false
                setRecycler()
            }
        }

        fun clearFilterList() {
            filterList.clear()
        }


    }

    private fun setRecycler() {
        val p0 = binding.pctpSearchField.text
        if (p0?.toString() != "" || !filterPopup.filterList.isEmpty()) {
            val searchText = p0?.toString() ?: ""
            recyclerClass.showRecycler(searchText)
            Log.d(TAG, "afterTextChanged: showing search recycler")

        } else {
            Log.d(TAG, "afterTextChanged: Showing no search recycler")
            firebaseRecycler.showRecycler(binding.pctpRecycler)
        }
    }

    private fun showLoading() {
        val view = LayoutInflater.from(context).inflate(R.layout.popup_ad_loading, null)
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

}