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
import net.nueca.imonggosdk.tools.SettingTools;

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
//            case RECEIVE_BRANCH:
//                DialogTools.showDialog(this, "Coming Soon", "Willing to wait?", "Yes!", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                }, R.style.AppCompatDialogStyle_Light);
//                break;
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

    public List<Branch> getBranches() {
        return getBranches(false);
    }

    /**
     * Generate the user's branches.
     * @return
     */
    public List<Branch> getBranches(boolean warehouseOnly) {
        List<Branch> assignedBranches = new ArrayList<>();
        try {
            List<BranchUserAssoc> branchUserAssocs = getHelper().fetchObjects(BranchUserAssoc.class).queryBuilder().where().eq("user_id", getUser()).query();
            for(BranchUserAssoc branchUser : branchUserAssocs) {
                Log.e("Branches", branchUser.getBranch().getName());
                if(warehouseOnly) {
                    if (branchUser.getBranch().getSite_type() == null || branchUser.getBranch().getSite_type().equals("null"))
                        continue;
                }
                else if(branchUser.getBranch().getSite_type() != null && branchUser.getBranch().getSite_type().equals("warehouse"))
                    continue;
                if(branchUser.getBranch().getStatus().equals("D"))
                    continue;

                if(branchUser.getBranch().getId() == Integer.valueOf(SettingTools.defaultBranch(this))) //getUser().getHome_branch_id())
                    assignedBranches.add(0, branchUser.getBranch());
                else
                    assignedBranches.add(branchUser.getBranch());
//                if(branchUser.getBranch().getId() == getUser().getHome_branch_id())
//                    assignedBranches.add(0, branchUser.getBranch());
//                else
//                    assignedBranches.add(branchUser.getBranch());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assignedBranches;
    }
}
