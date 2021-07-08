package com.ritwik.photex

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ritwik.colortest.ColorDatabase

class ColorRecycler(
    val recyclerView: RecyclerView,
    val context: Context,
    val updateLayer:Boolean?,
    val function:(selectedColor:String)->Unit
) {
    lateinit var adapter:RecyclerView.Adapter<ViewHolder>

    fun showRecycler() {
        val update = updateLayer?:false
        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = manager
        val colorHelper = ColorDatabase(context)
        var list = colorHelper.getColorList()
         adapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.single_color, parent, false)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                holder.setColor(list[position])
                holder.itemView.setOnClickListener {

                    function(list[position])
                    if(update) {
                        Log.d(TAG, "onBindViewHolder: saving changes")
                        colorHelper.addColor(list[position])
                        list = colorHelper.getColorList()
                        notifyItemMoved(position, 0)
                        notifyItemRangeChanged(1, list.size)
                    }
                    else
                    {
                        Log.d(TAG, "onBindViewHolder: ignoring changes")
                    }

                }
            }

            override fun getItemCount(): Int {
                return list.size
            }

        }
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }
    fun updateAdapter()
    {
        if(!this::adapter.isInitialized)
        {
            showRecycler()
        }
        else
        {
            adapter.notifyDataSetChanged()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.sc_Image)
        fun setColor(colorString: String) {
            imageView.setColorFilter(Color.parseColor(colorString));
        }
    }
    companion object
    {
        private const val TAG = "ColorRecycler"
    }

}
