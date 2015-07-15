package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.tools.LoggingTools;

import java.util.List;

/**
 * Created by Jn on 7/6/2015.
 * imonggosdk (c)2015
 */
public class CustomDialogFrameLayout extends FrameLayout {

    private Context mContext;
    private View mView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private CustomModuleAdapter customModuleAdapter;
    private List<String> mModuleName;

    public CustomDialogFrameLayout(final Context context, final List<String> moduleName) {
        super(context);
        this.mContext = context;
        this.mModuleName = moduleName;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            mView = inflater.inflate(R.layout.concessioengine_download_layout, null);
        }

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.cs_recyclerview);
        mLinearLayoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(context);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLinearLayoutManager.scrollToPosition(0);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        customModuleAdapter = new CustomModuleAdapter(mContext, R.layout.item_module, mModuleName);
        customModuleAdapter.setOnItemClickListener(new BaseCustomDialogRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                LoggingTools.showToast(context, "OnItemClicked" + getCustomModuleAdapter().getModuleAt(position));
            }

            @Override
            public void onItemLongClicked(View view, int position) {
                LoggingTools.showToast(context, "OnItemLongClicked" + getCustomModuleAdapter().getModuleAt(position));
            }
        });

        mRecyclerView.setAdapter(customModuleAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View v, int position) {
               // LoggingTools.showToast(mContext, moduleName.get(position));

                v.setBackgroundColor(mContext.getResources().getColor(android.R.color.darker_gray));

            }

            @Override
            public void onLongClick(View v, int position) {
                //LoggingTools.showToast(mContext, mRecyclerView.getT);
            }
        }));

        addView(mView);

    }

    public interface ClickListener {
        void onClick(View v, int position);

        void onLongClick(View v, int position);
    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector detector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView pRecyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    //Log.d("JN", "onSingleTap" + e);
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                    View child = pRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, pRecyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && detector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public CustomModuleAdapter getCustomModuleAdapter() {
        return customModuleAdapter;
    }
}
