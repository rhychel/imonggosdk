package net.nueca.concessio;

import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.nueca.concessioengine.activities.AddCustomerActivity;
import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
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
        Log.e("generateModules", "here");
        getSyncModules().setSyncAllModules(false);
        int modulesToDownload = includeUsers ? 5 : 3;
        boolean hasReceive = false, hasPulloutRequest = false, hasSales = false;

        try {
            List<ModuleSetting> moduleSettings = getHelper().fetchObjects(ModuleSetting.class).queryBuilder()
                    .where().in("module_type", ModuleSettingTools.getModulesToString(ConcessioModule.RELEASE_BRANCH, ConcessioModule.RELEASE_ADJUSTMENT, ConcessioModule.INVOICE, ConcessioModule.RECEIVE_BRANCH)).query();

            for(ModuleSetting moduleSetting : moduleSettings) {
                Log.e("FOR---"+moduleSetting.getModule_type(), moduleSetting.is_enabled()+"");
                if(!moduleSetting.is_enabled())
                    continue;
                switch (moduleSetting.getModuleType()) {
                    case RELEASE_ADJUSTMENT:
                    case RELEASE_BRANCH: {
                        if(!hasPulloutRequest) {
                            hasPulloutRequest = true;
                            modulesToDownload += 2;
                        }
                    } break;
                    case INVOICE: {
                        hasSales = true;
                        modulesToDownload += 4;
                    } break;
                    case RECEIVE_BRANCH: {
                        hasReceive = true;
                        modulesToDownload++;
                    } break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int index = modulesToDownload;
        int[] modules = new int[modulesToDownload];
        if(includeUsers) {
            modules[modulesToDownload - (index--)] = Table.USERS_ME.ordinal();
            modules[modulesToDownload - (index--)] = Table.BRANCH_USERS.ordinal();

        }
        modules[modulesToDownload-(index--)] = Table.SETTINGS.ordinal();
//        modules[modulesToDownload-(index--)] = Table.PRODUCTS.ordinal();
//        modules[modulesToDownload-(index--)] = Table.UNITS.ordinal();
        modules[modulesToDownload-(index--)] = Table.BRANCH_PRODUCTS.ordinal();
        modules[modulesToDownload-(index--)] = Table.BRANCH_UNITS.ordinal();
        Log.e("hasPulloutRequest", hasPulloutRequest+"");
        if(hasPulloutRequest) {
            modules[modulesToDownload-(index--)] = Table.DOCUMENT_TYPES.ordinal();
            modules[modulesToDownload-(index--)] = Table.DOCUMENT_PURPOSES.ordinal();
        }
        if(hasReceive)
            modules[modulesToDownload-(index--)] = Table.DOCUMENTS.ordinal();
        if(hasSales) {
            modules[modulesToDownload - (index--)] = Table.CUSTOMER_CATEGORIES.ordinal();
            modules[modulesToDownload - (index--)] = Table.BRANCH_CUSTOMERS.ordinal();
            modules[modulesToDownload - (index--)] = Table.CUSTOMER_CATEGORIES.ordinal();
            modules[modulesToDownload - (index--)] = Table.PAYMENT_TERMS.ordinal();
        }

        return modules;
    }

    @Override
    protected void onCreateLoginLayout() {
        super.onCreateLoginLayout();
        setContentView(R.layout.c_login);

        setupLayoutEquipments((EditText)findViewById(R.id.etAccountId),
                (EditText)findViewById(R.id.etEmail),
                (EditText)findViewById(R.id.etPassword),
                (Button)findViewById(R.id.btnLogin));

        setEditTextAccountID("C5015");
        setEditTextEmail("OSS1@test.com");
        setEditTextPassword("OSS1");
    }
}
