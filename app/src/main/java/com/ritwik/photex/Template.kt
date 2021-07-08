package com.ritwik.photex

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ritwik.photex.databinding.FragmentTemplateBinding

class Template : Fragment() {


    private var _binding: FragmentTemplateBinding? = null
    private val binding get() = _binding!!
    private var containerId = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        containerId = container?.id!!
        _binding = FragmentTemplateBinding.inflate(inflater, container, false)
        val recyclerView = binding.ftRecycler
        val manager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = manager
        binding.ftTemplateLabel.text = "Loading Templates"
        val recyclerClass = DefaultRecyclerClass(recyclerView)
        recyclerClass.showRecycler("")
        binding.ftSearchField.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "beforeTextChanged: ")
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "onTextChanged: ")
                }

                override fun afterTextChanged(p0: Editable?) {
                    if (p0?.toString() == "") {
                        recyclerClass.showRecycler("")
                    } else {
                        val searchText = p0?.toString() ?: ""
                        recyclerClass.showRecycler(searchText)
                    }
                }

            }
        )
        // Inflate the layout f or this fragment

        return binding.root
    }

    inner class RecyclerClass(val recyclerView: RecyclerView) {
        fun showReycler() {
            val query = FirebaseDatabase.getInstance().reference.child("Templates")
                .orderByChild("TIMES_USED")
            val options = FirebaseRecyclerOptions.Builder<TemplateModel>()
                .setQuery(query, TemplateModel::class.java).build()
            val adapter = object : FirebaseRecyclerAdapter<TemplateModel, ViewHolder>(options) {
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
                    if (binding.ftTemplateLabel.text != "Available Templates") {
                        binding.ftTemplateLabel.text = "Available Templates"
                    }
                    holder.setTemplateImage(model.LINK)
                }

            }
            recyclerView.adapter = adapter
            adapter.startListening()

        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val image = itemView.findViewById<ImageView>(R.id.st_Image)
            fun setTemplateImage(imageUrl: String) {
                Log.d(TAG, "setTemplateImage: loading image $imageUrl")
                context?.let { Glide.with(it).load(imageUrl).into(image) }
            }
        }
    }

    inner class DefaultRecyclerClass(val recyclerView: RecyclerView) {
        fun showRecycler(searchString: String) {
            getData(searchString) { imageList ->
                Log.d(TAG, "showRecycler: size of imageLIst - ${imageList.size}")
                finalShowRecycler(imageList)
            }
        }

        private fun finalShowRecycler(imageList: ArrayList<String>) {
            val imageList = imageList
            val adapter = object : RecyclerView.Adapter<ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    val view = LayoutInflater.from(context)
                        .inflate(R.layout.single_template, parent, false)
                    return ViewHolder(view)
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    Log.d(TAG, "onBindViewHolder: ")
                    if (binding.ftTemplateLabel.text != "Available Templates") {
                        binding.ftTemplateLabel.text = "Available Templates"
                    }
                    holder.setTemplateImage(imageList[position])
                    holder.itemView.setOnClickListener {
                        val bundle = Bundle()
                        bundle.apply {
                            putString("MODE", "TEMPLATE")
                            putString("LINK", imageList[position])
                            showCreateFragment(bundle)
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
            function: (imageList: ArrayList<String>) -> Unit
        ) {
            var imageList = arrayListOf<String>()
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
                                    Log.d(TAG, "onDataChange: ${child.key}")

                                    val link = if (child.hasChild("LINK")) {
                                        val temp = child.child("LINK").value.toString()
                                        Log.d(TAG, "onDataChange: link = $temp")
                                        temp
                                    } else {
                                        Log.d(TAG, "onDataChange: does not have link")
                                        ""
                                    }
                                    if (searchString == "") {
                                        Log.d(TAG, "onDataChange: search string empty")
                                        imageList.add(link)
                                    } else {
                                        Log.d(TAG, "onDataChange: search string not empty")
                                        if (child.key.toString().contains(searchString, true)) {
                                            Log.d(TAG, "onDataChange: key matches")
                                            imageList.add(link)
                                        } else {
                                            Log.d(TAG, "onDataChange: key does not match")
                                            continue
                                        }
                                    }
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
                context?.let { Glide.with(context!!).load(imageUrl).into(image) }
            }
        }
    }

    fun showCreateFragment(bundle: Bundle) {
        activity?.let {
            if (containerId != 0) {
                val fragment = CreateFragment()

                fragment.arguments = bundle
                it.supportFragmentManager.beginTransaction()
                    .replace(containerId, fragment, "CREATE_FRAGMENT").addToBackStack("")
                    .commit()
            }
        }
    }

    companion object {
        private const val TAG = "Template"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}