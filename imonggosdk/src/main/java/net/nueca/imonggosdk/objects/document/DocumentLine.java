package net.nueca.imonggosdk.objects.document;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gama on 7/20/15.
 */
public class DocumentLine {
    protected int line_no = 0;

    protected int product_id = 0;

    protected double quantity = 0.0;

    protected ExtendedAttributes extended_attributes;

    public int getLine_no() {
        return line_no;
    }

    public void setLine_no(int line_no) {
        this.line_no = line_no;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public ExtendedAttributes getExtended_attributes() {
        return extended_attributes;
    }

    public void setExtended_attributes(ExtendedAttributes extended_attributes) {
        this.extended_attributes = extended_attributes;
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        return new JSONObject(gson.toJson(this));
    }
}
