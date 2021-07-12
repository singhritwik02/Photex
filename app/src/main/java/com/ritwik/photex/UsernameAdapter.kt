package com.ritwik.photex

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class UsernameAdapter(context: Context, resource: Int, list: ArrayList<UsernameData>) :
    ArrayAdapter<UsernameData>(context, resource, list) {
    val list = list
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
       return initView(position,convertView,parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position,convertView,parent)
    }
    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val item = list[position]
        val view = LayoutInflater.from(context).inflate(R.layout.single_social_media,parent,false)
        val imageView = view.findViewById<ImageView>(R.id.ssm_Image)
        val text = view.findViewById<TextView>(R.id.ssm_Name)
        text.text = item.getUsername()
        return view

    }

}