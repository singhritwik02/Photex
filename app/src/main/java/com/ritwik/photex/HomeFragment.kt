package com.ritwik.photex

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ritwik.colortest.ColorDatabase
import com.ritwik.photex.databinding.FragmentHomeBinding
import com.ritwik.photex.databinding.PopupChooseColorBinding
import com.ritwik.photex.databinding.SingleColorBinding


class HomeFragment : Fragment() {
    private var _binding:FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private  var containerId:Int = 0
    private lateinit var fragment: Fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        containerId = container?.id!!
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        binding.fhBlankButton
            .setOnClickListener {


                    val bundle = Bundle()
                    bundle.putString("MODE","BLANK")
                        showCreateFragment(bundle)




            }
        binding.fhGalleryButton
            .setOnClickListener {
                val bundle = Bundle()
                bundle.putString("MODE","GALLERY")
                showCreateFragment(bundle)
            }
        binding.fhTemplateButton.setOnClickListener {
           activity?.let {
               if (container != null) {
                   it.supportFragmentManager.beginTransaction().replace(container.id, Template(),"TEMPLATE_FRAGMENT").addToBackStack("")
                       .commit()
               }
           }

        }
        return binding.root
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
//    inner class ChooseColor {
//       private var selectedColor = "#FFFFFF"
//        fun showChooseColorPopup() {
//            val binding = PopupChooseColorBinding.inflate(LayoutInflater.from(context))
//            val window = PopupWindow(
//                binding.root,
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.WRAP_CONTENT, true
//            )
//            with(window)
//            {
//                elevation = 100f
//                animationStyle = R.style.pAnimation
//                showAtLocation(binding.root, Gravity.BOTTOM, 0, 0)
//            }
//            binding.createButton
//                .setOnClickListener {
//                    val bundle = Bundle()
//                    bundle.apply {
//                        putString("MODE","BLANK")
//                        putString("COLOR_CODE",selectedColor)
//                    }
//                   showCreateFragment(bundle)
//                    window.dismiss()
//                }
//
//            val colorHelper = ColorDatabase(context)
//            selectedColor = colorHelper.getTopColor()
//            binding.pccChooseColorImage.setColorFilter(Color.parseColor(selectedColor))
//            val recyclerClass = RecyclerClass(
//                binding.pccRecycler,
//                colorHelper.getColorList(),
//                binding.pccChooseColorImage
//            )
//            val manager = LinearLayoutManager(context)
//            manager.orientation = LinearLayoutManager.HORIZONTAL
//            binding.pccRecycler.layoutManager = manager
//            recyclerClass.showRecycler()
//
//
//        }
//
//        private inner class RecyclerClass(
//            val recyclerView: RecyclerView,
//            var list: ArrayList<String>,
//            val imageView: ImageView
//        ) {
//            fun showRecycler() {
//                val colorHelper = ColorDatabase(context)
//                val adapter = object : RecyclerView.Adapter<ViewHolder>() {
//                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//                        val view = LayoutInflater.from(context)
//                            .inflate(R.layout.single_color, parent, false)
//                        return ViewHolder(view)
//                    }
//
//                    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//                        holder.setColor(list[position])
//                        holder.itemView.setOnClickListener {
//                            selectedColor = list[position]
//                            imageView.setColorFilter(Color.parseColor(list[position]));
//                            colorHelper.addColor(list[position])
//                            list = colorHelper.getColorList()
//                           notifyItemMoved(position,0)
//                            notifyItemRangeChanged(1,list.size)
//
//                        }
//                    }
//
//                    override fun getItemCount(): Int {
//                        return list.size
//                    }
//
//                }
//                recyclerView.adapter = adapter
//                adapter.notifyDataSetChanged()
//            }
//
//            inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//                val imageView = itemView.findViewById<ImageView>(R.id.sc_Image)
//                fun setColor(colorString: String) {
//                    imageView.setColorFilter(Color.parseColor(colorString));
//                }
//            }
//
//        }
//    }
    fun showCreateFragment(bundle: Bundle)
    {
        activity?.let {
            if (containerId != 0) {
                 fragment = CreateFragment()
                fragment.arguments = bundle
                it.supportFragmentManager.beginTransaction().replace(containerId, fragment,"CREATE_FRAGMENT").addToBackStack("")
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}