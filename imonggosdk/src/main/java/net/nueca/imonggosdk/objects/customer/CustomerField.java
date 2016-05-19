package net.nueca.imonggosdk.objects.customer;

import java.util.List;

/**
 * Created by rhymartmanchus on 18/05/2016.
 */
public class CustomerField<T> {

    public enum FieldType {
        EDITTEXT,
        SPINNER
    }

    private String label;
    private Customer.CustomerFields fieldName;
    private List<T> values;
    private FieldType fieldType;
    private int iconField = -1;
    private int selectedIndex = 0;
    private String editTextValue = "";
    private boolean hasTextChangedListener = false;

    public CustomerField(String label, FieldType fieldType, Customer.CustomerFields fieldName) {
        this.label = label;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
    }

    public CustomerField(String label, FieldType fieldType, Customer.CustomerFields fieldName, Customer customer) {
        this.label = label;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.editTextValue = (String) customer.getCustomerDetail(fieldName);
    }

    public CustomerField(String label, FieldType fieldType, Customer.CustomerFields fieldName, String editTextValue) {
        this.label = label;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.editTextValue = editTextValue;
    }

    public CustomerField(String label, List<T> values, FieldType fieldType, Customer.CustomerFields fieldName) {
        this.label = label;
        this.values = values;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
    }

    public CustomerField(String label, List<T> values, FieldType fieldType, Customer.CustomerFields fieldName, int selectedIndex) {
        this.label = label;
        this.values = values;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.selectedIndex = selectedIndex;
    }

    public CustomerField(String label, FieldType fieldType, int iconField, Customer.CustomerFields fieldName) {
        this.label = label;
        this.fieldType = fieldType;
        this.iconField = iconField;
        this.fieldName = fieldName;
    }

    public CustomerField(String label, FieldType fieldType, int iconField, Customer.CustomerFields fieldName, String editTextValue) {
        this.label = label;
        this.fieldType = fieldType;
        this.iconField = iconField;
        this.fieldName = fieldName;
        this.editTextValue = editTextValue;
    }

    public CustomerField(String label, FieldType fieldType, int iconField, Customer.CustomerFields fieldName, Customer customer) {
        this.label = label;
        this.fieldType = fieldType;
        this.iconField = iconField;
        this.fieldName = fieldName;
        this.editTextValue = (String) customer.getCustomerDetail(fieldName);
    }

    public CustomerField(String label, List<T> values, FieldType fieldType, int iconField, Customer.CustomerFields fieldName) {
        this.label = label;
        this.values = values;
        this.fieldType = fieldType;
        this.iconField = iconField;
        this.fieldName = fieldName;
    }

    public CustomerField(String label, List<T> values, Customer.CustomerFields fieldName) {
        this.label = label;
        this.values = values;
        this.fieldName = fieldName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<T> getValues() {
        return values;
    }

    public void setValues(List<T> values) {
        this.values = values;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public int getIconField() {
        return iconField;
    }

    public void setIconField(int iconField) {
        this.iconField = iconField;
    }

    public Customer.CustomerFields getFieldName() {
        return fieldName;
    }

    public void setFieldName(Customer.CustomerFields fieldName) {
        this.fieldName = fieldName;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public String getEditTextValue() {
        return editTextValue;
    }

    public void setEditTextValue(String editTextValue) {
        this.editTextValue = editTextValue;
    }

    public boolean isHasTextChangedListener() {
        return hasTextChangedListener;
    }

    public void setHasTextChangedListener(boolean hasTextChangedListener) {
        this.hasTextChangedListener = hasTextChangedListener;
    }
}
