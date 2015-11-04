package nueca.net.salesdashboard.adapters;

import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;

import java.util.ArrayList;
import java.util.List;

import nueca.net.salesdashboard.R;

/**
 * Jn Cld
 * Imonggo Sales Dashboard(c) 2015
 */
public class UserBranchesSpinnerAdapter extends BaseAdapter {

    private List<BranchUserAssoc> mItems = new ArrayList<>();
    private LayoutInflater mLayoutInflater;

    public UserBranchesSpinnerAdapter(LayoutInflater layoutInflater, List<BranchUserAssoc> items) {
        this.mItems = items;
        this.mLayoutInflater = layoutInflater;
    }

    public void addItem(BranchUserAssoc item) {
        mItems.add(item);
    }

    public void addItems(List<BranchUserAssoc> items) {
        mItems.addAll(items);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !convertView.getTag().toString().equals("NON_DROPDOWN")) {

            convertView = mLayoutInflater.inflate(R.layout.app_bar_spinner_action_bar, parent, false);
            convertView.setTag("NON_DROPDOWN");
        }

        Typeface AvenirNextRegular = Typeface.createFromAsset(convertView.getContext().getAssets(), "fonts/AvenirNext-Regular.ttf");

        TextView textView = (TextView) convertView.findViewById(R.id.tag_name);
        textView.setTypeface(AvenirNextRegular);
        textView.setTextSize(18);
        textView.setText(getTitle(position));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = mLayoutInflater.inflate(R.layout.app_bar_spinner_dropdown, parent, false);
            view.setTag("DROPDOWN");
        }
        Typeface AvenirNextRegular = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/AvenirNext-Regular.ttf");

        TextView textView = (TextView) view.findViewById(R.id.tag_name);
        textView.setTypeface(AvenirNextRegular);
        textView.setTextSize(18);
        textView.setText(getTitle(position));

        return view;
    }

    public Branch getBranch(int position) {
        BranchUserAssoc branchUserAssoc = (BranchUserAssoc) getItem(position);

        return position >= 0 && position < mItems.size() ? branchUserAssoc.getBranch() : null;
    }

    private String getTitle(int position) {
        BranchUserAssoc branchUserAssoc = (BranchUserAssoc) getItem(position);

        return position >= 0 && position < mItems.size() ? branchUserAssoc.getBranch().getName() : "";
    }

    public void updateDeleteBranch(BranchUserAssoc b) {

        List<BranchUserAssoc> branchUserItems = mItems;

        for (BranchUserAssoc bUser : mItems) {
            Log.e("UserBranchesOLD", bUser.getBranch().getName());

            if (bUser.getBranch().getId() == b.getBranch().getId()) {
                Log.e("UserBranches", "removing " + bUser.getBranch().getName());
                branchUserItems.remove(b);
            }
        }

        for (BranchUserAssoc branchUsers : mItems) {
            Log.e("UserBranchesNew", branchUsers.getBranch().getName());
        }

        mItems = branchUserItems;

        notifyDataSetChanged();
    }


}
