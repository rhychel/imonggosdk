package net.nueca.concessioengine.adapters.base;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Created by rhymart on 8/7/15.
 * imonggosdk2 (c)2015
 */
public abstract class BaseAdapter<T> extends ArrayAdapter<T> {

    public BaseAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
    }

    /**
     * Update a specific list item only.
     * @param lvList listview
     * @param position index of on the listview
     */
    public void notifyItemChanged(ListView lvList, int position) {
        View v = lvList.getChildAt(position - lvList.getFirstVisiblePosition());

        if(v == null)
            return;

        getView(position, v, null);
    }
}
