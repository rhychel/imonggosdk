package net.nueca.concessioengine.tools.appsettings;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.imonggosdk.enums.ConcessioModule;

import java.util.List;

/**
 * Created by rhymartmanchus on 12/01/2016.
 */
public class AppSettings {

    public enum AppSettingEntry {
        // Application
        VERSION("Version:"),
        GET_LATEST_DOCUMENT("Get latest documents"), // --
        CLEAR_CACHED_DOCS("Clear cached documents"),
        CLEAR_TRANSACTIONS("Clear transactions"),
        AUTO_UPDATE_APP("Auto-update app"),
        SHOW_HISTORY_AFTER("Show history after transaction"),
        DEBUG_MODE("Debug mode"),
        PRODUCT_SORTING("Product sorting"),
        DISABLE_IMAGE("Disable product image"),

        // User
        ENABLE_PIN_CODE("Enable PIN code"),
        CHANGE_PIN_CODE("Change PIN code");

        // -- Module dependent label
        // --

        private String label;

        AppSettingEntry(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public enum ValueType {
        LABEL,
        LABEL_CLICK,
        SWITCH,
        BUTTON,
        PIN,
        DROPDOWN
    }

    private boolean isHeader = false;
    private ConcessioModule concessioModule;
    private AppSettingEntry appSettingEntry;
    private ValueType valueType = ValueType.LABEL;
    private int sectionFirstPosition = 0;
    private Object value;
    private ArrayAdapter<?> adapter;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;
    private int selectedItem = 0;
    private OnItemClickListener onItemClickListener;


    public ConcessioModule getConcessioModule() {
        return concessioModule;
    }

    public void setConcessioModule(ConcessioModule concessioModule) {
        this.concessioModule = concessioModule;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public int getSectionFirstPosition() {
        return sectionFirstPosition;
    }

    public void setSectionFirstPosition(int sectionFirstPosition) {
        this.sectionFirstPosition = sectionFirstPosition;
    }

    public int getSelectedItem() {
        if(selectedItem < 0)
            return 0;
        return selectedItem;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public <T> T getValue(Class<T> c) {
        return (T)(c.cast(value));
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ArrayAdapter<?> getAdapter() {
        return adapter;
    }

    public void setAdapter(ArrayAdapter<?> adapter) {
        this.adapter = adapter;
    }

    public AdapterView.OnItemSelectedListener getOnItemSelectedListener() {
        return onItemSelectedListener;
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public AppSettingEntry getAppSettingEntry() {
        return appSettingEntry;
    }

    public void setAppSettingEntry(AppSettingEntry appSettingEntry) {
        this.appSettingEntry = appSettingEntry;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
