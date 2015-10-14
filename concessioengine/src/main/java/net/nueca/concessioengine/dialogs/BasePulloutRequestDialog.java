package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.Spinner;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.document.DocumentPurpose;

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
    private List<DocumentPurpose> reasonList;

    public BasePulloutRequestDialog(Context context, ImonggoDBHelper dbHelper) {
        super(context);
        this.dbHelper = dbHelper;
        try {
            this.reasonList = dbHelper.getDocumentPurposes().queryBuilder().where().eq("status", "A").query();
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

    public BasePulloutRequestDialog(Context context, List<DocumentPurpose> reasons, ImonggoDBHelper dbHelper) {
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

    public ImonggoDBHelper getHelper() {
        return dbHelper;
    }
    //public abstract void populateBranchSelectors();

    public void showBranchSelection(boolean shouldShow) {
        spnSourceBranch.setVisibility(shouldShow? View.VISIBLE : View.GONE);
        spnDestinationBranch.setVisibility(shouldShow? View.VISIBLE : View.GONE);
    }

    public Branch getSelectedBranch(Spinner spinner) throws SQLException {
        String branchName = (String)spinner.getSelectedItem();
        return dbHelper.getBranches().queryBuilder().where().eq("name", branchName).queryForFirst();
    }

    public List<DocumentPurpose> getReasonList() {
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
