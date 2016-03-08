package net.nueca.concessioengine.objects;

import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.accountsettings.ModuleSetting;

/**
 * Created by rhymart on 11/24/15.
 */
public class DashboardTile {

    private ConcessioModule concessioModule;
    private String label;
    private int imageResource;

    public DashboardTile(ModuleSetting moduleSetting, int imageResource) {
        this.concessioModule = moduleSetting.getModuleType();
        this.label = moduleSetting.getLabel();
        this.imageResource = imageResource;
    }

    public DashboardTile(ConcessioModule concessioModule, String label) {
        this.concessioModule = concessioModule;
        if(concessioModule == ConcessioModule.CUSTOMERS)
            this.label = "Customers";
        else
            this.label = label;
        this.imageResource = concessioModule.getLogo();
    }

    public DashboardTile(ConcessioModule concessioModule, String label, int imageResource) {
        this.concessioModule = concessioModule;
        this.label = label;
        this.imageResource = imageResource;
    }

    public ConcessioModule getConcessioModule() {
        return concessioModule;
    }

    public void setConcessioModule(ConcessioModule concessioModule) {
        this.concessioModule = concessioModule;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    @Override
    public String toString() {
        return label;
    }
}
