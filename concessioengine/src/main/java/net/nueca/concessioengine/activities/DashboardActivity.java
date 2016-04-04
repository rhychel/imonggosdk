package net.nueca.concessioengine.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.objects.DashboardTile;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.tools.DialogTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 11/19/15.
 */
public abstract class DashboardActivity extends ImonggoAppCompatActivity {

    private Class<?> nextActivityClass;

    protected void setNextActivityClass(Class<?> nextActivityClass) {
        this.nextActivityClass = nextActivityClass;
    }

    public void moduleSelected(View view) {
        Log.e("moduleSelected", "here");
        DashboardTile dashboardTile = new DashboardTile(ConcessioModule.STOCK_REQUEST, "Orders");
        if(view.getTag() != null) {
            dashboardTile = (DashboardTile) view.getTag();
        }
        else {
            Log.e("Ooops!", "no tag for this button. e.g.: view.setTag(ConcessioModule.STOCK_REQUEST)");
            return;
        }
        if(nextActivityClass == null) {
            Log.e("Ooops!", "Please define the proper next activity");
            return;
        }
        switch (dashboardTile.getConcessioModule()) {
            case RECEIVE_BRANCH:
            case RECEIVE_BRANCH_PULLOUT:
                DialogTools.showDialog(this, "Coming Soon", "Willing to wait?", "Yes!", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }, R.style.AppCompatDialogStyle_Light);
                break;
            default: { // TEMPORARY
                Intent intent = new Intent(this, nextActivityClass);
                Bundle bundle = addExtras(dashboardTile);
                if(bundle != null)
                    intent.putExtras(bundle);
                intent.putExtra(ModuleActivity.CONCESSIO_MODULE, dashboardTile.getConcessioModule().ordinal());
                startActivity(intent);
            }
        }
    }

    protected abstract Bundle addExtras(DashboardTile dashboardTile);


    /**
     * Generate the user's branches.
     * @return
     */
    public List<Branch> getBranches() {
        List<Branch> assignedBranches = new ArrayList<>();
        try {
            List<BranchUserAssoc> branchUserAssocs = getHelper().fetchObjects(BranchUserAssoc.class).queryBuilder().where().eq("user_id", getUser()).query();
            for(BranchUserAssoc branchUser : branchUserAssocs) {
                if(branchUser.getBranch().getId() == getUser().getHome_branch_id())
                    assignedBranches.add(0, branchUser.getBranch());
                else
                    assignedBranches.add(branchUser.getBranch());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assignedBranches;
    }
}
