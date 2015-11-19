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
            case ORDERS:
                return "order";
            case PHYSICAL_COUNT:
                return "count";
            case RECEIVE:
                return "receive";
            case PULLOUT_REQUEST:
                return "pullout_request";
            case PULLOUT_CONFIRMATION:
                return "pullout_confirmation";
            case INVENTORY:
                return "inventory";
            case SALES:
                return "sales";
            default:
                return "app";
        }
    }

    public static String[] getModulesToString(ConcessioModule... concessioModules) {
        String[] modules = new String[concessioModules.length];
        int i = 0;
        for(ConcessioModule concessioModule : concessioModules)
            modules[i++] = getModuleToString(concessioModule);
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
