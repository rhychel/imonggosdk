package net.nueca.concessioengine.objects;

import net.nueca.imonggosdk.enums.Table;

/**
 * Created by rhymartmanchus on 23/01/2016.
 */
public class UpdateTable {
    private Table table;
    private boolean isSelected = false;

    public UpdateTable(Table table) {
        this.table = table;
    }

    public UpdateTable(Table table, boolean isSelected) {
        this.table = table;
        this.isSelected = isSelected;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
