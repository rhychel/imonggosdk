package net.nueca.imonggosdk.tools;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.accountsettings.Cutoff;
import net.nueca.imonggosdk.objects.accountsettings.DebugMode;
import net.nueca.imonggosdk.objects.accountsettings.Manual;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;
import net.nueca.imonggosdk.objects.accountsettings.ProductListing;
import net.nueca.imonggosdk.objects.accountsettings.ProductSorting;
import net.nueca.imonggosdk.objects.accountsettings.QuantityInput;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/18/15.
 */
public class ModuleSettingTools {

    public static String getModuleToString(ConcessioModule concessioModule) {
        switch (concessioModule) {
            case STOCK_REQUEST:
                return "stock_request";
            case PHYSICAL_COUNT:
                return "physical_count";
            case INVOICE:
                return "invoice";

            case RECEIVE_BRANCH:
                return "receive_branch";
            case RELEASE_BRANCH:
                return "release_branch";
            case RECEIVE_BRANCH_PULLOUT:
                return "receive_branch_pullout"; // Pullout Confirmation

            case RECEIVE_ADJUSTMENT:
                return "receive_adjustment";
            case RELEASE_ADJUSTMENT:
                return "release_adjustment";

            case RECEIVE_SUPPLIER:
                return "receive_supplier";
            case RELEASE_SUPPLIER:
                return "release_supplier";

            case CUSTOMERS:
                return "customers";

            default:
                return "app";
        }
    }

    public static String[] getModulesToString(ConcessioModule... concessioModules) {
        String[] modules = new String[concessioModules.length];
        int i = 0;
        for(ConcessioModule concessioModule : concessioModules)
            modules[i++] = concessioModule.toString();
        return modules;
    }

    public static void deleteModuleSettings(ImonggoDBHelper2 dbHelper) throws SQLException {
        dbHelper.deleteAll(ModuleSetting.class);
        dbHelper.deleteAll(ProductListing.class);
        dbHelper.deleteAll(Manual.class);
        dbHelper.deleteAll(QuantityInput.class);
        dbHelper.deleteAll(Cutoff.class);
        dbHelper.deleteAll(ProductSorting.class);
        dbHelper.deleteAll(DebugMode.class);
    }

}
