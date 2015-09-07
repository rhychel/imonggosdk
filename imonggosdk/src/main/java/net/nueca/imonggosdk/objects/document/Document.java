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
    public static transient final int MAX_DOCUMENTLINES_PER_PAGE = 50;

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
    protected Integer document_purpose_id;
    @DatabaseField
    protected String document_purpose_name;

    @DatabaseField
    protected String intransit_status;
    @DatabaseField
    protected Integer user_id;
    @DatabaseField
    protected String status;


    public Document() {
        super(null);
        remark = "page=1/1";
    }

    public Document(Builder builder) {
        super(builder);
        //remark = builder.remark;
        remark = "page=1/1";
        document_type_code = builder.document_type_code;
        document_lines = builder.document_lines;
        target_branch_id = builder.target_branch_id;
        document_purpose_name = builder.document_purpose_name;
        id = builder.id;
        intransit_status = builder.intransit_status;

        document_purpose_id = builder.document_purpose_id;
        user_id = builder.user_id;
        status = builder.status;
        utc_created_at = builder.utc_created_at;
        utc_updated_at = builder.utc_updated_at;
        utc_document_date = builder.utc_document_date;
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
        refresh();
        return document_lines;
    }

    public void setDocument_lines(List<DocumentLine> document_lines) {
        this.document_lines = document_lines;
    }

    public int getTarget_branch_id() {
        return target_branch_id;
    }

    public void setTarget_branch_id(Integer target_branch_id) {
        this.target_branch_id = target_branch_id;
    }

    public String getDocument_purpose_name() {
        return document_purpose_name;
    }

    public void setDocument_purpose_name(String document_purpose_name) {
        this.document_purpose_name = document_purpose_name;
    }

    public String getIntransit_status() {
        return intransit_status;
    }

    public void setIntransit_status(String intransit_status) {
        this.intransit_status = intransit_status;
    }

    public void setDocument_type_code(String document_type_code) {
        this.document_type_code = document_type_code;
    }

    public Integer getDocument_purpose_id() {
        return document_purpose_id;
    }

    public void setDocument_purpose_id(Integer document_purpose_id) {
        this.document_purpose_id = document_purpose_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void addDocumentLine(DocumentLine documentLine) {
        if(document_lines == null)
            document_lines = new ArrayList<>();

        if(documentLine.autoLine_no)
            documentLine.setLine_no(document_lines.size()+1);

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
        //return document_lines.size() > MAX_DOCUMENTLINES_PER_PAGE;
        return false;
    }

    @Override
    public int getChildCount() {
        refresh();
        return SwableTools.computePagedRequestCount(document_lines.size(), MAX_DOCUMENTLINES_PER_PAGE);
    }

    @Override
    public void insertTo(ImonggoDBHelper dbHelper) {
        if(shouldPageRequest()) {
            try {
                List<Document> documents = getChildDocuments();
                for (Document child : documents)
                    child.insertTo(dbHelper);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

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
        if(shouldPageRequest()) {
            try {
                List<Document> documents = getChildDocuments();
                for (Document child : documents)
                    child.deleteTo(dbHelper);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

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
        if(shouldPageRequest()) {
            try {
                List<Document> documents = getChildDocuments();
                for (Document child : documents)
                    child.updateTo(dbHelper);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

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
        protected String intransit_status;
        protected Integer document_purpose_id;
        protected Integer user_id;
        protected String status;
        protected String utc_created_at;
        protected String utc_updated_at;
        protected String utc_document_date;

        public Builder utc_created_at(String utc_created_at) {
            this.utc_created_at = utc_created_at;
            return this;
        }
        public Builder utc_updated_at(String utc_updated_at) {
            this.utc_updated_at = utc_updated_at;
            return this;
        }
        public Builder utc_document_date(String utc_document_date) {
            this.utc_document_date = utc_document_date;
            return this;
        }

        public Builder user_id(Integer user_id) {
            this.user_id = user_id;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder document_purpose_id(Integer document_purpose_id) {
            this.document_purpose_id = document_purpose_id;
            return this;
        }

        public Builder intransit_status(String intransit_status) {
            this.intransit_status = intransit_status;
            return this;
        }

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

            if(documentLine.autoLine_no)
                documentLine.setLine_no(document_lines.size()+1);

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
        list.addAll(SwableTools.partition(position, document_lines, MAX_DOCUMENTLINES_PER_PAGE));
        return list;
    }

    public Document getChildDocumentAt(int position) throws JSONException {
        Document document = Document.fromJSONString(toJSONString());
        document.setId(id + position);
        document.setDocument_lines(getDocumentLineAt(position));
        document.setReference(reference + "-" + (position + 1));
        document.setRemark("page=" + (position + 1) + "/" + getChildCount());
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
        if(document_lines_fc != null && document_lines == null) {
            for(DocumentLine documentLine : document_lines_fc) {
                addDocumentLine(documentLine);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Document) && ((Document)o).getId() == id;
    }

    /** Overriding equals() requires an Overridden hashCode() **/
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id;
        return result;
    }
}
