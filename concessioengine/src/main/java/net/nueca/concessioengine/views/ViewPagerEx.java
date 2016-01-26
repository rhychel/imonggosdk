package net.nueca.concessioengine.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import net.nueca.concessioengine.R;

/**
 * Created by rhymartmanchus on 19/01/2016.
 */
public class ViewPagerEx extends ViewPager {

    private boolean swipePagingEnabled = true;

    public ViewPagerEx(Context context) {
        super(context);
    }

    public ViewPagerEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerEx);
        swipePagingEnabled = typedArray.getBoolean(R.styleable.ViewPagerEx_swipePagingEnabled, true);
        Log.e("ViewPagerEx", swipePagingEnabled+"");
        typedArray.recycle();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(swipePagingEnabled)
            return super.onInterceptTouchEvent(ev);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (swipePagingEnabled)
            return super.onTouchEvent(ev);
        return false;
    }

    public boolean isSwipePagingEnabled() {
        return swipePagingEnabled;
    }

    public void setSwipePagingEnabled(boolean swipePagingEnabled) {
        this.swipePagingEnabled = swipePagingEnabled;
    }
}
