package net.nueca.imonggosdk.objects.base;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import net.nueca.imonggosdk.tools.ReferenceNumberTool;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gama on 7/21/15.
 */
@Deprecated
public abstract class BaseTransaction {
    @Expose
    @DatabaseField
    protected String reference;

    public BaseTransaction() {}

    public BaseTransaction(Builder builder) {
        if(builder != null)
            reference = builder.reference;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String generateNewReferenceNumber(Context context, int deviceId) {
        this.reference = ReferenceNumberTool.generateRefNo(context,
                deviceId);
        return this.reference;
    }

    public String toJSONString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return (gson.toJson(this));
    }

    @Override
    public String toString() {
        return toJSONString();
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return new JSONObject(gson.toJson(this));
    }

    protected static class Builder <T extends Builder> {
        private String reference = "";

        public Builder() {}

        public T reference(String reference) {
            this.reference = reference;
            return (T)this;
        }

        public T generateReference(Context context, int deviceId) {
            this.reference = ReferenceNumberTool.generateRefNo(context, deviceId);
            return (T)this;
        }

        public <T extends BaseTransaction> T build() throws NoSuchMethodException {
            throw new NoSuchMethodException("Builder method build() was not implemented");
        }
    }

    public abstract boolean shouldPageRequest();
    public abstract int getId();
    public abstract int getChildCount();
}
