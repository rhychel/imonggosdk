package net.nueca.concessio;

import android.content.Intent;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.AccountSettings;
import net.nueca.imonggosdk.tools.SettingTools;

/**
 * Created by rhymart on 8/20/15.
 * imonggosdk2 (c)2015
 */
public class C_Login extends LoginActivity {

    @Override
    protected void initLoginEquipments() {
        super.initLoginEquipments();
        setServer(Server.IRETAILCLOUD_NET);

        setRequireConcessioSettings(true);
    }

    @Override
    protected void successLogin() {
        super.successLogin();
        int []modulesToDownload = generateModules(true);
        setModules(generateModules(true));
        getSyncModules().initializeTablesToSync(modulesToDownload);
    }

    @Override
    protected void updateAppData() {
        super.updateAppData();
        int []modulesToDownload = generateModules(true);
        setModules(generateModules(false));

        getSyncModules().initializeTablesToSync(modulesToDownload);
    }

    @Override
    protected void showNextActivity() {
        finish();
        Intent intent = new Intent(this, (SettingTools.defaultBranch(this).equals("") ? C_Welcome.class : C_Dashboard.class));
        startActivity(intent);
    }

    private int[] generateModules(boolean includeUsers) {
        getSyncModules().setSyncAllModules(false);
        int modulesToDownload = includeUsers ? 4 : 2;
        if(AccountSettings.hasPullout(this)) {
            modulesToDownload+=2;
        }
//        if(AccountSettings.hasReceive(this))
//            modulesToDownload++;
        if(AccountSettings.hasSales(this))
            modulesToDownload++;

        int index = modulesToDownload;
        int[] modules = new int[modulesToDownload];
        if(includeUsers) {
            modules[modulesToDownload - (index--)] = Table.USERS.ordinal();
            modules[modulesToDownload - (index--)] = Table.BRANCH_USERS.ordinal();
        }
        modules[modulesToDownload-(index--)] = Table.PRODUCTS.ordinal();
        modules[modulesToDownload-(index--)] = Table.UNITS.ordinal();
        if(AccountSettings.hasPullout(this)) {
            modules[modulesToDownload-(index--)] = Table.DOCUMENT_TYPES.ordinal();
            modules[modulesToDownload-(index--)] = Table.DOCUMENT_PURPOSES.ordinal();
        }
//        if(AccountSettings.hasReceive(this))
//            modules[modulesToDownload-(index--)] = Table.DOCUMENTS.ordinal();
        if(AccountSettings.hasSales(this))
            modules[modulesToDownload-(index--)] = Table.CUSTOMERS.ordinal();

        return modules;
    }
}
