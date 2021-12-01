package com.ritwik.photex;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import io.feeeei.circleseekbar.CircleSeekBar;

public class CustomCircleSeek extends CircleSeekBar {
    private static final String TAG = "Custom Circle";
    private boolean scrollEnabled = true;
    private CustomScrollView customScrollView;

    public CustomScrollView getCustomScrollView() {
        return customScrollView;
    }

    public void setCustomScrollView(CustomScrollView customScrollView) {
        this.customScrollView = customScrollView;
    }

    public boolean isScrollEnabled() {
        return scrollEnabled;
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        this.scrollEnabled = scrollEnabled;
    }

    public CustomCircleSeek(Context context) {
        super(context);
    }

    public CustomCircleSeek(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCircleSeek(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(customScrollView!=null)
        {
           switch ((event.getAction()))
           {
               case MotionEvent.ACTION_DOWN:
               {
                   customScrollView.setEnableScrolling(false);
                   Log.d(TAG, "onTouchEvent: disabling ");
                   break;

               }

               case MotionEvent.ACTION_UP:
               {
                   customScrollView.setEnableScrolling(true);
                   Log.d(TAG, "onTouchEvent: enabling");
                   break;

               }
               default:
                   Log.d(TAG, "onTouchEvent: default enabling");
                   customScrollView.setEnableScrolling(false);

               break;
           }
           return super.onTouchEvent(event);
        }
        else
        {
        return super.onTouchEvent(event);

        }
    }
}
