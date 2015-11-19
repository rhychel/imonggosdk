package net.nueca.concessio;

import android.content.Intent;

import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.AccountSettings;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.tools.ModuleSettingTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;
import java.util.List;

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
        setRequireObjectConcessioSettings(true);
    }

    @Override
    protected void successLogin() {
        super.successLogin();
        int []modulesToDownload = generateModules(true);
        setModulesToSync(generateModules(true));
        getSyncModules().initializeTablesToSync(modulesToDownload);
    }

    @Override
    protected void updateAppData() {
        super.updateAppData();
        int []modulesToDownload = generateModules(true);
        setModulesToSync(generateModules(false));

        getSyncModules().initializeTablesToSync(modulesToDownload);
    }

    @Override
    protected void showNextActivityAfterLogin() {
        finish();
        Intent intent = new Intent(this, (SettingTools.defaultBranch(this).equals("") ? C_Welcome.class : C_Dashboard.class));
        startActivity(intent);
    }

    private int[] generateModules(boolean includeUsers) {
        getSyncModules().setSyncAllModules(false);
        int modulesToDownload = includeUsers ? 4 : 2;
        boolean hasReceive = false, hasPulloutRequest = false, hasSales = false;

        try {
            List<ModuleSetting> moduleSettings = getHelper().fetchObjects(ModuleSetting.class).queryBuilder()
                    .where().in("module_type", ModuleSettingTools.getModulesToString(ConcessioModule.PULLOUT_REQUEST, ConcessioModule.SALES, ConcessioModule.RECEIVE)).query();

            for(ModuleSetting moduleSetting : moduleSettings) {
                if(!moduleSetting.is_enabled())
                    continue;
                if(moduleSetting.getModuleType() == ConcessioModule.PULLOUT_REQUEST) {
                    hasPulloutRequest = true;
                    modulesToDownload += 2;
                }
                if(moduleSetting.getModuleType() == ConcessioModule.SALES && moduleSetting.is_enabled()) {
                    hasSales = true;
                    modulesToDownload++;
                }
                if(moduleSetting.getModuleType() == ConcessioModule.RECEIVE) {
                    hasReceive = true;
                    modulesToDownload++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int index = modulesToDownload;
        int[] modules = new int[modulesToDownload];
        if(includeUsers) {
            modules[modulesToDownload - (index--)] = Table.USERS.ordinal();
            modules[modulesToDownload - (index--)] = Table.BRANCH_USERS.ordinal();
        }
        modules[modulesToDownload-(index--)] = Table.PRODUCTS.ordinal();
        modules[modulesToDownload-(index--)] = Table.UNITS.ordinal();
        if(hasPulloutRequest) {
            modules[modulesToDownload-(index--)] = Table.DOCUMENT_TYPES.ordinal();
            modules[modulesToDownload-(index--)] = Table.DOCUMENT_PURPOSES.ordinal();
        }
        if(hasReceive)
            modules[modulesToDownload-(index--)] = Table.DOCUMENTS.ordinal();
        if(hasSales)
            modules[modulesToDownload-(index--)] = Table.CUSTOMERS.ordinal();

        return modules;
    }

    @Override
    protected void onCreateLoginLayout() {
        super.onCreateLoginLayout();

        setEditTextAccountID("retailpos");
        setEditTextEmail("retailpos@test.com");
        setEditTextPassword("retailpos");
    }
}
