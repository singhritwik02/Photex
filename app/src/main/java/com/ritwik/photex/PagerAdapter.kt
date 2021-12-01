package com.ritwik.photex

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

public class PagerAdapter(manager:FragmentManager): FragmentStatePagerAdapter(manager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        val fragment = MemeStyleFragment()
        val data = Bundle()
        data.putInt("num",position)
        fragment.arguments = data
        return fragment
    }

}