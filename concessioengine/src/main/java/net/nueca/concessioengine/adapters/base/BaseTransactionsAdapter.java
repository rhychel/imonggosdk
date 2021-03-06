package net.nueca.concessioengine.adapters.base;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import net.nueca.concessioengine.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.OfflineData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by rhymart on 8/4/15.
 * imonggosdk2 (c)2015
 */
public abstract class BaseTransactionsAdapter extends BaseAdapter<OfflineData> {

    public BaseTransactionsAdapter(Context context, int resource, List<OfflineData> objects) {
        super(context, resource, objects);
    }

    public abstract boolean updateList(List<OfflineData> offlineDatas);
}
