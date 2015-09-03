package nueca.net.salesdashboard;


import net.nueca.concessioengine.activities.login.LoginActivity;
import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.SettingsName;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.tools.SettingTools;

public class D_Login extends LoginActivity {

    @Override
    protected void initLoginEquipments() {
        super.initLoginEquipments();
        setRequireConcessioSettings(false);
        setServer(Server.IMONGGO);
        SettingTools.updateSettings(this, SettingsName.AUTO_UPDATE, false, "");
        setModulesToSync(Table.USERS.ordinal(), Table.BRANCH_USERS.ordinal(), Table.DAILY_SALES.ordinal());
    }

}
