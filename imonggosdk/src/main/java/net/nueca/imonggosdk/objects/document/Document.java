package net.nueca.imonggosdk.objects.document;

import com.google.gson.Gson;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTransaction;
import net.nueca.imonggosdk.objects.base.BaseTransactionDB;
import net.nueca.imonggosdk.swable.SwableTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 7/20/15.
 */
public class Document extends BaseTransactionDB {
    public static transient final int MAX_DOCUMENTLINES_PER_PAGE = 2;

    @DatabaseField
    protected String remark;

    @DatabaseField
    protected String document_type_code;

    protected List<DocumentLine> document_lines;

    @ForeignCollectionField
    private transient ForeignCollection<DocumentLine> document_lines_fc;

    @DatabaseField
    protected Integer target_branch_id;

    @DatabaseField
    protected String document_purpose_name;

    public Document() {
        super(null);
    }

    public Document(Builder builder) {
        super(builder);
        remark = builder.remark;
        document_type_code = builder.document_type_code;
        document_lines = builder.document_lines;
        target_branch_id = builder.target_branch_id;
        document_purpose_name = builder.document_purpose_name;
        id = builder.id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public DocumentTypeCode getDocument_type_code() {
        return DocumentTypeCode.identify(document_type_code);
    }

    public void setDocument_type_code(DocumentTypeCode document_type_code) {
        this.document_type_code = document_type_code.toString();
    }

    public List<DocumentLine> getDocument_lines() {
        return document_lines;
    }

    public void setDocument_lines(List<DocumentLine> document_lines) {
        this.document_lines = document_lines;
    }

    public int getTarget_branch_id() {
        return target_branch_id;
    }

    public void setTarget_branch_id(int target_branch_id) {
        this.target_branch_id = target_branch_id;
    }

    public String getDocument_purpose_name() {
        return document_purpose_name;
    }

    public void setDocument_purpose_name(String document_purpose_name) {
        this.document_purpose_name = document_purpose_name;
    }

    public void addDocumentLine(DocumentLine documentLine) {
        if(document_lines == null)
            document_lines = new ArrayList<>();
        document_lines.add(documentLine);
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
        refresh();
        return document_lines.size() > MAX_DOCUMENTLINES_PER_PAGE;
    }

    @Override
    public int getChildCount() {
        refresh();
        return SwableTools.computePagedRequestCount(document_lines.size(), MAX_DOCUMENTLINES_PER_PAGE);
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DOCUMENTS, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        refresh();
        if(document_lines == null)
            return;
        for(DocumentLine documentLine : document_lines) {
            documentLine.setDocument(this);
            documentLine.insertTo(dbHelper);
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DOCUMENTS, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        refresh();
        if(document_lines == null)
            return;
        for(DocumentLine documentLine : document_lines) {
            documentLine.deleteTo(dbHelper);
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        try {
            dbHelper.dbOperations(this, Table.DOCUMENTS, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Builder extends BaseTransaction.Builder<Builder> {
        protected String remark;
        protected String document_type_code;
        protected List<DocumentLine> document_lines;
        protected Integer target_branch_id;
        protected String document_purpose_name;
        protected int id;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }
        public Builder document_type_code(DocumentTypeCode document_type_code) {
            this.document_type_code = document_type_code.toString();
            return this;
        }
        public Builder document_lines(List<DocumentLine> document_lines) {
            this.document_lines = document_lines;
            return this;
        }
        public Builder target_branch_id(int target_branch_id) {
            this.target_branch_id = target_branch_id;
            return this;
        }
        public Builder document_purpose_name(String document_purpose_name) {
            this.document_purpose_name = document_purpose_name;
            return this;
        }

        public Builder addDocumentLine(DocumentLine documentLine) {
            if(document_lines == null)
                document_lines = new ArrayList<>();
            document_lines.add(documentLine);
            return this;
        }

        @Override
        public Document build() {
            return new Document(this);
        }
    }

    public List<DocumentLine> getDocumentLineAt(int position) {
        refresh();

        List<DocumentLine> list = new ArrayList<>();
        list.addAll(SwableTools.partition(position,document_lines,MAX_DOCUMENTLINES_PER_PAGE));
        return list;
    }

    public Document getChildDocumentAt(int position) throws JSONException {
        Document document = Document.fromJSONString(toJSONString());
        document.setDocument_lines(getDocumentLineAt(position));
        document.setReference(reference + "-" + (position + 1));
        return document;
    }

    public List<Document> getChildDocuments() throws JSONException {
        List<Document> documentList = new ArrayList<>();
        for(int i = 0; i < getChildCount(); i++) {
            documentList.add(getChildDocumentAt(i));
        }
        return documentList;
    }

    @Override
    public String toJSONString() {
        refresh();
        return super.toJSONString();
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        refresh();
        return super.toJSONObject();
    }

    public void refresh() {
        if(document_lines_fc != null) {
            for(DocumentLine documentLine : document_lines_fc)
                addDocumentLine(documentLine);
        }
    }
}
