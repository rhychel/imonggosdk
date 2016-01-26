package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.nueca.imonggosdk.enums.ConcessioModule;

import java.util.List;

/**
 * Created by rhymartmanchus on 11/01/2016.
 */
public class TransactionTypesAdapter extends ArrayAdapter<ConcessioModule> {

    private int layout;
    private int dropdownLayout = -1;

    public TransactionTypesAdapter(Context context, int resource, List<ConcessioModule> objects) {
        super(context, resource, objects);
        this.layout = resource;
    }

    private static class ViewHolder {
        TextView text1;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(dropdownLayout == -1)
            return super.getDropDownView(position, convertView, parent);
        ViewHolder vh = null;
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(dropdownLayout, null);
            vh = new ViewHolder();

            vh.text1 = (TextView) convertView.findViewById(android.R.id.text1);

            convertView.setTag(vh);
        }
        else
            vh = (ViewHolder) convertView.getTag();

        vh.text1.setText(getItem(position).getLabel());

        return convertView;
    }

    public void setDropdownLayout(int dropdownLayout) {
        this.dropdownLayout = dropdownLayout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layout, null);
            vh = new ViewHolder();

            vh.text1 = (TextView) convertView.findViewById(android.R.id.text1);

            convertView.setTag(vh);
        }
        else
            vh = (ViewHolder) convertView.getTag();

        vh.text1.setText(getItem(position).getLabel());

        return convertView;
    }
}
