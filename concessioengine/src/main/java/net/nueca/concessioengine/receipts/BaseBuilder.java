package net.nueca.concessioengine.receipts;

import android.content.Context;

import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;

/**
 * Created by rhymartmanchus on 18/05/2016.
 */
public abstract class BaseBuilder<T> {

    public Context context;
    public ConcessioModule concessioModule;
    public String title, agentName;
    public Branch branch;
    public ModuleSetting moduleSetting;
    public OfflineData offlineData;
    public boolean isReprint = false;

    public BaseBuilder(Context context) {
        this.context = context;
    }

    public BaseBuilder concessio_module(ConcessioModule concessioModule) {
        this.concessioModule = concessioModule;
        return this;
    }

    public BaseBuilder title(String title) {
        this.title = title;
        return this;
    }

    public BaseBuilder agent_name(String agentName) {
        this.agentName = agentName;
        return this;
    }

    public BaseBuilder branch(Branch branch) {
        this.branch = branch;
        return this;
    }

    public BaseBuilder module_setting(ModuleSetting moduleSetting) {
        this.moduleSetting = moduleSetting;
        return this;
    }

    public BaseBuilder offline_data(OfflineData offlineData) {
        this.offlineData = offlineData;
        return this;
    }

    public BaseBuilder is_reprint(boolean reprint) {
        isReprint = reprint;
        return this;
    }

    public abstract T build();
    public abstract T print(String ...labels);
}
