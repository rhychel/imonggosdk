package net.nueca.imonggosdk.objects.accountsettings;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.base.DBTable;

import java.sql.SQLException;

/**
 * Created by rhymart on 11/17/15.
 */
@DatabaseTable
public class QuantityInput extends DBTable {

    @DatabaseField(generatedId = true)
    private transient int id;
    @DatabaseField
    private boolean has_one_unit = true;
    @DatabaseField
    private boolean has_unit = true;
    @DatabaseField
    private boolean has_outright_return = false;
    @DatabaseField
    private boolean is_multiinput = false;
    @DatabaseField
    private boolean has_brand = false;
    @DatabaseField
    private boolean has_batch_number = false;
    @DatabaseField
    private boolean has_delivery_date = false;
    @DatabaseField
    private boolean has_remark = false;
    @DatabaseField
    private boolean input_custom_price = false;
    @DatabaseField
    private boolean input_custom_discount = false;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "module_setting_id")
    private transient ModuleSetting moduleSetting;
    @DatabaseField(foreign=true, foreignAutoRefresh = true, columnName = "manual_id")
    private transient Manual manual;

    public QuantityInput() {
    }

    public boolean isHas_one_unit() {
        return has_one_unit;
    }

    public void setHas_one_unit(boolean has_one_unit) {
        this.has_one_unit = has_one_unit;
    }

    public boolean isHas_unit() {
        return has_unit;
    }

    public void setHas_unit(boolean has_unit) {
        this.has_unit = has_unit;
    }

    public boolean isHas_outright_return() {
        return has_outright_return;
    }

    public void setHas_outright_return(boolean has_outright_return) {
        this.has_outright_return = has_outright_return;
    }

    public boolean is_multiinput() {
        return is_multiinput;
    }

    public void setIs_multiinput(boolean is_multiinput) {
        this.is_multiinput = is_multiinput;
    }

    public boolean isHas_brand() {
        return has_brand;
    }

    public void setHas_brand(boolean has_brand) {
        this.has_brand = has_brand;
    }

    public boolean isHas_batch_number() {
        return has_batch_number;
    }

    public void setHas_batch_number(boolean has_batch_number) {
        this.has_batch_number = has_batch_number;
    }

    public boolean isHas_delivery_date() {
        return has_delivery_date;
    }

    public void setHas_delivery_date(boolean has_delivery_date) {
        this.has_delivery_date = has_delivery_date;
    }

    public boolean isHas_remark() {
        return has_remark;
    }

    public void setHas_remark(boolean has_remark) {
        this.has_remark = has_remark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ModuleSetting getModuleSetting() {
        return moduleSetting;
    }

    public void setModuleSetting(ModuleSetting moduleSetting) {
        this.moduleSetting = moduleSetting;
    }

    public Manual getManual() {
        return manual;
    }

    public void setManual(Manual manual) {
        this.manual = manual;
    }

    public boolean isInput_custom_price() {
        return input_custom_price;
    }

    public void setInput_custom_price(boolean input_custom_price) {
        this.input_custom_price = input_custom_price;
    }

    public boolean isInput_custom_discount() {
        return input_custom_discount;
    }

    public void setInput_custom_discount(boolean input_custom_discount) {
        this.input_custom_discount = input_custom_discount;
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(QuantityInput.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(QuantityInput.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.insert(QuantityInput.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
