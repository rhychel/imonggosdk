package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.Spinner;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Branch;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 10/5/15.
 */
public abstract class BasePulloutRequestDialog extends BaseAppCompatDialog {
    protected Spinner spnReason, spnSourceBranch, spnDestinationBranch;

    private ImonggoDBHelper dbHelper;

    private List<String> branchListStr;
    private List<Branch> branchList;
    private List<String> reasonList;

    public BasePulloutRequestDialog(Context context, ImonggoDBHelper dbHelper) {
        super(context);
        this.dbHelper = dbHelper;
        try {
            branchList = dbHelper.getBranches().queryForAll();
            for(Branch branch : branchList) {
                if(branchListStr == null)
                    branchListStr = new ArrayList<>();
                branchListStr.add(branch.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public BasePulloutRequestDialog(Context context, List<String> reasons, ImonggoDBHelper dbHelper) {
        super(context);
        reasonList = reasons;
        this.dbHelper = dbHelper;
        try {
            branchList = dbHelper.getBranches().queryForAll();
            for(Branch branch : branchList) {
                if(branchListStr == null)
                    branchListStr = new ArrayList<>();
                branchListStr.add(branch.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setReasonList(List<String> reasonList) {
        this.reasonList = reasonList;
    }

    //public abstract void populateBranchSelectors();

    public void showBranchSelection(boolean shouldShow) {
        spnSourceBranch.setVisibility(shouldShow? View.VISIBLE : View.GONE);
        spnDestinationBranch.setVisibility(shouldShow? View.VISIBLE : View.GONE);
    }

    public Branch getSelectedBranch(Spinner spinner) {
        return branchList.get(spinner.getSelectedItemPosition());
    }

    public List<String> getReasonList() {
        return reasonList;
    }

    public List<String> getSourceBranch() {
        return branchListStr;
    }

    public List<String> getDestinationBranch() throws SQLException {
        List<Branch> destinationList = dbHelper.getBranches().queryBuilder().where().not().eq( "id", branchList.get
                (spnSourceBranch.getSelectedItemPosition()).getId() ).query();

        List<String> destinationBranches = new ArrayList<>();
        for(Branch branch : destinationList) {
            destinationBranches.add(branch.getName());
        }
        return destinationBranches;
    }
}
