package net.nueca.concessioengine.views;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;


/**
 * Created by rhymart on 7/8/15.
 * imonggosdk (c)2015
 */
public class BottomSheet extends LinearLayout {

    private final ViewDragHelper dragHelper;
    private View dragView;

    public BottomSheet(Context context) {
        this(context, null);
    }

    public BottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        dragHelper = ViewDragHelper.create(this, 1.0f, new BottomSheetCallback());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        dragView = findViewById(R.id.btnSample);
    }

    private class BottomSheetCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == dragView;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop();
            final int bottomBound = getHeight() - dragView.getHeight();

            final int newTop = Math.min(Math.max(top, topBound), bottomBound);

            return newTop;
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            dragHelper.cancel();
            return false;
        }
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        dragHelper.processTouchEvent(ev);
        return true;
    }

}
