package com.ritwik.photex

import android.view.View
import androidx.viewpager.widget.ViewPager

class PageTransformAnimation : ViewPager.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        if (position < -1 || position > 1) {
            page.setAlpha(0f);
        } else if (position <= 0 || position <= 1) {
            // Calculate alpha. Position is decimal in [-1,0] or [0,1]
            val alpha: Float = if (position <= 0)
                position + 1
            else
                1 - position;
            page.setAlpha(alpha);
        } else if (position == 0f) {
            page.setAlpha(1f);
        }
    }
}

