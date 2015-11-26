package net.nueca.concessioengine.objects;

import net.nueca.imonggosdk.enums.ConcessioModule;

/**
 * Created by rhymart on 11/24/15.
 */
public class DashboardTile {

    private ConcessioModule concessioModule;
    private String label;

    public DashboardTile(ConcessioModule concessioModule, String label) {
        this.concessioModule = concessioModule;
        this.label = label;
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

    @Override
    public String toString() {
        return label;
    }
}
