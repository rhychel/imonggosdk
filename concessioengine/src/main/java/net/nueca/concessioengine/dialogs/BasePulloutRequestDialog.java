package net.nueca.concessioengine.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
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
    protected LinearLayout llSourceBranch, llDestinationBranch;

    protected DocumentPurpose currentReason = null;

    private ImonggoDBHelper2 dbHelper;

    private List<String> branchListStr;
    private List<Branch> branchList;
    private List<DocumentPurpose> reasonList;

    protected boolean shouldShowBranchSelection = false;


    public BasePulloutRequestDialog(Context context, ImonggoDBHelper2 dbHelper) {
        super(context);
        initializeData(dbHelper, true);
    }

    public BasePulloutRequestDialog(Context context, ImonggoDBHelper2 dbHelper, int theme) {
        super(context, theme);
        initializeData(dbHelper, true);
    }

    public BasePulloutRequestDialog(Context context, List<DocumentPurpose> reasons, ImonggoDBHelper2 dbHelper) {
        super(context);
        initializeData(dbHelper, reasons == null);
    }

    public BasePulloutRequestDialog(Context context, List<DocumentPurpose> reasons, ImonggoDBHelper2 dbHelper, int theme) {
        super(context, theme);
        initializeData(dbHelper, reasons == null);
    }

    private void initializeData(ImonggoDBHelper2 dbHelper, boolean queryReasons) {
        this.dbHelper = dbHelper;
        try {
            if(queryReasons)
                this.reasonList = dbHelper.fetchObjects(DocumentPurpose.class).queryBuilder().where().eq("status", "A").query();
            branchList = dbHelper.fetchObjects(Branch.class).queryForAll();
            for(Branch branch : branchList) {
                if(branchListStr == null)
                    branchListStr = new ArrayList<>();
                branchListStr.add(branch.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ImonggoDBHelper2 getHelper() {
        return dbHelper;
    }
    //public abstract void populateBranchSelectors();


    public void setShouldShowBranchSelection(boolean shouldShowBranchSelection) {
        this.shouldShowBranchSelection = shouldShowBranchSelection;
    }

    protected void showBranchSelection(boolean shouldShow) {
        llSourceBranch.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        llDestinationBranch.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
    }

    public Branch getSelectedBranch(Spinner spinner) throws SQLException {
        String branchName = (String)spinner.getSelectedItem();
        if(branchName == null)
            return null;
        return dbHelper.fetchObjects(Branch.class).queryBuilder().where().eq("name", branchName).queryForFirst();
    }

    public List<DocumentPurpose> getReasonList() {
        return reasonList;
    }

    public List<String> getSourceBranch() {
        return branchListStr;
    }

    public List<String> getDestinationBranch() throws SQLException {
        List<Branch> destinationList = dbHelper.fetchObjects(Branch.class).queryBuilder().where().not().eq( "id", branchList.get
                (spnSourceBranch.getSelectedItemPosition()).getId() ).query();

        List<String> destinationBranches = new ArrayList<>();
        for(Branch branch : destinationList) {
            destinationBranches.add(branch.getName());
        }
        return destinationBranches;
    }

    public void setCurrentReason(DocumentPurpose currentReason) {
        this.currentReason = currentReason;
    }
}
