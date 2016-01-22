package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.interfaces.LoginListener;
import net.nueca.imonggosdk.tools.LoggingTools;
import net.nueca.imonggosdk.tools.TableTools;

import java.sql.SQLException;
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
    private List<Table> mTableNames;
    private LoginListener mLoginListener;
    private static String TAG = "CustomDialogFrameLayout";

    @Deprecated
    public CustomDialogFrameLayout(Context context, List<String> moduleName) {
        super(context);
        //customDialogFrameLayout(mContext, moduleName);
    }

    public CustomDialogFrameLayout(final List<Table> moduleName, final Context context) {
       super(context);
        customDialogFrameLayout(context, moduleName);
    }

    private void customDialogFrameLayout(final Context context, final List<Table> moduleName) {
        this.mContext = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            mView = inflater.inflate(R.layout.concessioengine_download_layout, null, false);
        }

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.rvModulesToSync);
        mLinearLayoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(context);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLinearLayoutManager.scrollToPosition(0);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        //customModuleAdapter = new CustomModuleAdapter(mContext, R.layout.item_module, mModuleName);
        customModuleAdapter = new CustomModuleAdapter(moduleName, mContext, R.layout.item_module);

        customModuleAdapter.setOnItemClickListener(new BaseCustomDialogRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) throws SQLException {
                Log.e(TAG, "OnItemClicked " + getCustomModuleAdapter().getModuleAt(position));
               /* LoggingTools.showToast(mContext, "OnItemClicked" + getCustomModuleAdapter().getModuleAt(position));
                view.setBackgroundColor(mContext.getResources().getColor(android.R.color.darker_gray));*/
                //mLoginListener.onRetryButtonPressed(TableTools.convertStringToTableName(getCustomModuleAdapter().getModuleAt(position)));
                mLoginListener.onRetryButtonPressed(getCustomModuleAdapter().getTableAt(position));
            }
        });

        customModuleAdapter.setOnItemLongClickListener(new BaseCustomDialogRecyclerAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClicked(View view, int position) {
                /*LoggingTools.showToast(mContext, "OnItemLongClicked" + getCustomModuleAdapter().getModuleAt(position));*/
            }
        });

        mRecyclerView.setAdapter(customModuleAdapter);
        addView(mView);
    }

    public void scrollToPositionWithOffset(int position, int offset) {
        mLinearLayoutManager.scrollToPositionWithOffset(position, offset);
    }



    public void setLoginListener(LoginListener loginListener) {
        this.mLoginListener = loginListener;
    }

    public CustomModuleAdapter getCustomModuleAdapter() {
        return customModuleAdapter;
    }
}
