package net.nueca.imonggosdk.objects.document;

import android.content.Context;

import com.google.gson.Gson;

import net.nueca.imonggosdk.objects.base.BaseTransaction;
import net.nueca.imonggosdk.tools.ReferenceNumberTool;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 7/20/15.
 */
public class Document extends BaseTransaction {
    public static transient final int MAX_DOCUMENTLINES_PER_PAGE = 1;

    protected String remark;

    protected String document_type_code;

    protected List<DocumentLine> document_lines;

    public Document(Builder builder) {
        super(builder);
        remark = builder.remark;
        document_type_code = builder.document_type_code;
        document_lines = builder.document_lines;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDocument_type_code() {
        return document_type_code;
    }

    public void setDocument_type_code(String document_type_code) {
        this.document_type_code = document_type_code;
    }

    public List<DocumentLine> getDocument_lines() {
        return document_lines;
    }

    public void setDocument_lines(List<DocumentLine> document_lines) {
        this.document_lines = document_lines;
    }

    public static Document fromJSONString(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        return fromJSONObject(jsonObject);
    }

    public static Document fromJSONObject(JSONObject jsonObject) throws JSONException {
        Gson gson = new Gson();
        if(jsonObject.has("document")) {
            jsonObject = jsonObject.getJSONObject("document");
        }
        Document document = gson.fromJson(jsonObject.toString(),Document.class);
        return document;
    }

    @Override
    public boolean shouldPageRequest() {
        return document_lines.size() > MAX_DOCUMENTLINES_PER_PAGE;
    }

    public static class Builder extends BaseTransaction.Builder<Builder> {
        protected String remark;
        protected String document_type_code;
        protected List<DocumentLine> document_lines;

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }
        public Builder document_type_code(String document_type_code) {
            this.document_type_code = document_type_code;
            return this;
        }
        public Builder document_lines(List<DocumentLine> document_lines) {
            this.document_lines = document_lines;
            return this;
        }

        public Document build() {
            return new Document(this);
        }
    }
}
