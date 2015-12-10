package net.nueca.imonggosdk.objects;

import android.util.Log;
import android.util.SparseArray;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable2;
import net.nueca.imonggosdk.objects.base.BaseTransaction;
import net.nueca.imonggosdk.objects.base.BaseTransactionTable;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.DateTimeTools;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@DatabaseTable
public class OfflineData extends BaseTable2 {
    public static final transient int INVOICE = 0;
    public static final transient int ORDER = 1;
    public static final transient int DOCUMENT = 2;
    public static final transient int CUSTOMER = 3;

    @DatabaseField
    private String date;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "invoice_id")
    private Invoice invoiceData;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "order_id")
    private Order orderData;

    private transient Document documentData;
    @ForeignCollectionField(orderColumnName = "reference")
    private transient ForeignCollection<Document> documentData_fc;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "customer_id")
    private Customer customerData;
	
	@DatabaseField
	private int type = 1;
	
	@DatabaseField
	private int branch_id = -1;

    @DatabaseField
    private boolean isSyncing = false;

    @DatabaseField
    private boolean isSynced = false;

    @DatabaseField
    private boolean isCancelled = false;

    @DatabaseField
    private boolean isQueued = false;

    @DatabaseField
    private boolean isForConfirmation = false; // TODO This is new for the data sent with existing reference number

    @DatabaseField
    private boolean isPastCutoff = false; // unused

    @DatabaseField
    private boolean isFromManualReceive = true; // unused

    @DatabaseField
    private boolean isPurePullout = false; // unused

    @DatabaseField
    private String returnId = "";

    @DatabaseField
    private String branchName = ""; // unused

    @DatabaseField
    private String parameters = "";

    @DatabaseField
    private int user_id = 0; // unused

    @DatabaseField
    private String reference_no = "";

    @DatabaseField
    private boolean active = true; // unused

    @DatabaseField
    private int parent_id = -1; // unused

    @DatabaseField
    private boolean isChild = false; // unused

    @DatabaseField
    private boolean isParent = false; // should show buttons -- unused

    @DatabaseField
    private boolean isChildrenShown = false; // unused

    @DatabaseField
    private Date dateCreated;

    @DatabaseField
    private String childrenReferences = "";  // unused

    @DatabaseField
    private int offlineDataTransactionType = 1; // For COUNT ONLY!!!

    @DatabaseField
    private boolean hasExtendedAttributes = false; // unused

    @DatabaseField
    private String targetBranchTransfer = ""; // unused

    @DatabaseField
    private String category = ""; // unused

    @DatabaseField
    private String documentReason = "";

    private SparseArray<String> childrenArray = new SparseArray<String>(); // unused

    private boolean isExpanded = false; // unused

    @DatabaseField
    private boolean isPagedRequest = false;

    @DatabaseField
    private int pagedRequestCount = 0;

    @DatabaseField
    private boolean isNewPagedSend = true;

    public OfflineData() {}

    public OfflineData(Invoice invoice, OfflineDataType offlineDataType) {
        String []timestamp = DateTimeTools.getCurrentDateTimeInvoice();
        String timeId = timestamp[0]+" "+timestamp[1];
        this.date = timeId;
        this.dateCreated = Calendar.getInstance().getTime();

        this.offlineDataTransactionType = offlineDataType.getNumericValue();
        this.type = INVOICE;
        this.invoiceData = invoice;
        this.orderData = null;
        this.documentData = null;
        this.customerData = null;
        this.reference_no = invoice.getReference();

        this.isPagedRequest = false;
    }

    public OfflineData(Order order, OfflineDataType offlineDataType) {
        String []timestamp = DateTimeTools.getCurrentDateTimeInvoice();
        String timeId = timestamp[0]+" "+timestamp[1];
        this.date = timeId;
        this.dateCreated = Calendar.getInstance().getTime();

        this.offlineDataTransactionType = offlineDataType.getNumericValue();
        this.type = ORDER;
        this.invoiceData = null;
        this.orderData = order;
        this.documentData = null;
        this.customerData = null;
        this.reference_no = order.getReference();

        this.isPagedRequest = order.shouldPageRequest();
        this.pagedRequestCount = SwableTools.computePagedRequestCount(order.getOrder_lines().size(),
                Order.MAX_ORDERLINES_PER_PAGE);
    }

    public OfflineData(Document document, OfflineDataType offlineDataType) {
        String []timestamp = DateTimeTools.getCurrentDateTimeInvoice();
        String timeId = timestamp[0]+" "+timestamp[1];
        this.date = timeId;
        this.dateCreated = Calendar.getInstance().getTime();

        this.offlineDataTransactionType = offlineDataType.getNumericValue();
        this.type = DOCUMENT;
        this.invoiceData = null;
        this.orderData = null;
        this.documentData = document;
        this.customerData = null;
        this.reference_no = document.getReference();

        this.isPagedRequest = document.shouldPageRequest();
        this.pagedRequestCount = SwableTools.computePagedRequestCount(document.getDocument_lines().size(),
                Document.MAX_DOCUMENTLINES_PER_PAGE);
    }
    public OfflineData(Customer customer, OfflineDataType offlineDataType) {
        String []timestamp = DateTimeTools.getCurrentDateTimeInvoice();
        String timeId = timestamp[0]+" "+timestamp[1];
        this.date = timeId;
        this.dateCreated = Calendar.getInstance().getTime();

        this.offlineDataTransactionType = offlineDataType.getNumericValue();
        this.type = INVOICE;
        this.invoiceData = null;
        this.orderData = null;
        this.documentData = null;
        this.customerData = customer;

        this.isPagedRequest = false;
    }

    public boolean isNewPagedSend() {
        return isNewPagedSend;
    }

    public void setNewPagedSend(boolean isNewPagedSend) {
        this.isNewPagedSend = isNewPagedSend;
    }

    public JSONObject getData() throws JSONException {
        switch (type) {
            case INVOICE:
                return invoiceData.toJSONObject();
            case ORDER:
                return orderData.toJSONObject();
            case DOCUMENT:
                generateParentDocument();
                return documentData.toJSONObject();
            case CUSTOMER:
                return customerData.toJSONObject();
            default:
                return null;
        }
	}

    private Document generateParentDocument() {
        if(documentData_fc != null) {
            if(documentData != null) {
                documentData.setDocument_lines(null);
                documentData.setReference(reference_no);
            }

            for(Document document : documentData_fc) {
                if(documentData == null) {
                    documentData = document;
                    documentData.setReference(reference_no);
                }
                else {
                    if(document.getDocument_lines() != null)
                        documentData.addAllDocumentLine(document.getDocument_lines());
                }
            }
        }

        return documentData;
    }

    public List<Document> getChildDocuments() throws JSONException {
        if(documentData_fc == null)
            return documentData.getChildDocuments();
        else
            return new ArrayList<>(documentData_fc);
    }

	public void setData(JSONObject data) throws JSONException {
		switch (type) {
            case INVOICE :
                this.invoiceData = Invoice.fromJSONObject(data);
                this.orderData = null;
                this.documentData = null;
                this.customerData = null;
                break;
            case ORDER :
                this.invoiceData = null;
                this.orderData = Order.fromJSONObject(data);
                this.documentData = null;
                this.customerData = null;
                break;
            case DOCUMENT :
                this.invoiceData = null;
                this.orderData = null;
                this.documentData = Document.fromJSONObject(data);
                this.customerData = null;
                break;
            case CUSTOMER :
                this.invoiceData = null;
                this.orderData = null;
                this.documentData = null;
                this.customerData = Customer.fromJSONObject(data);
                break;
            default:
                this.invoiceData = null;
                this.orderData = null;
                this.documentData = null;
                this.customerData = null;
                break;
        }
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getBranch_id() {
		return branch_id;
	}

	public void setBranch_id(int branch_id) {
		this.branch_id = branch_id;
	}

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public String getReturnId() {
        return returnId;
    }

    public void setReturnId(String returnId) {
        this.returnId = returnId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public boolean isSyncing() {
        return isSyncing;
    }

    public void setSyncing(boolean isSyncing) {
        this.isSyncing = isSyncing;
    }

    public String getParameters() {
        if(parameters.length() > 0 && parameters.charAt(0) != '&')
            return "&" + parameters;
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public boolean isQueued() {
        return isQueued;
    }

    public void setQueued(boolean isQueued) {
        this.isQueued = isQueued;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getReference_no() {
        return reference_no;
    }

    public void setReference_no(String reference_no) {
        this.reference_no = reference_no;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getParent_id() {
        return parent_id;
    }

    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }

    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean isChild) {
        this.isChild = isChild;
    }

    public boolean isParent() {
        return isParent;
    }

    public void setParent(boolean isParent) {
        this.isParent = isParent;
    }

    public boolean isChildrenShown() {
        return isChildrenShown;
    }

    public void setChildrenShown(boolean isChildrenShown) {
        this.isChildrenShown = isChildrenShown;
    }

    public SparseArray<String> getChildrenArray() {
        return childrenArray;
    }

    public void setChildrenArray(SparseArray<String> childrenArray) {
        this.childrenArray = childrenArray;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getChildrenReferences() {
        return childrenReferences;
    }

    public void setChildrenReferences(String childrenReferences) {
        this.childrenReferences = childrenReferences;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public boolean isForConfirmation() {
        return isForConfirmation;
    }

    public void setForConfirmation(boolean isForConfirmation) {
        this.isForConfirmation = isForConfirmation;
    }

    public boolean isPastCutoff() {
        return isPastCutoff;
    }

    public void setPastCutoff(boolean isPastCutoff) {
        this.isPastCutoff = isPastCutoff;
    }

    public OfflineDataType getOfflineDataTransactionType() {
        return OfflineDataType.identify(offlineDataTransactionType);
    }

    public void setOfflineDataTransactionType(OfflineDataType offlineDataType) {
        this.offlineDataTransactionType = offlineDataType.getNumericValue();
    }

    public boolean isHasExtendedAttributes() {
        return hasExtendedAttributes;
    }

    public void setHasExtendedAttributes(boolean hasExtendedAttributes) {
        this.hasExtendedAttributes = hasExtendedAttributes;
    }

    public boolean isFromManualReceive() {
        return isFromManualReceive;
    }

    public void setFromManualReceive(boolean isFromManualReceive) {
        this.isFromManualReceive = isFromManualReceive;
    }

    public boolean isPurePullout() {
        return isPurePullout;
    }

    public void setPurePullout(boolean isPurePullout) {
        this.isPurePullout = isPurePullout;
    }

    public String getTargetBranchTransfer() {
        return targetBranchTransfer;
    }

    public void setTargetBranchTransfer(String targetBranchTransfer) {
        this.targetBranchTransfer = targetBranchTransfer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDocumentReason() {
        return documentReason;
    }

    public void setDocumentReason(String documentReason) {
        this.documentReason = documentReason;
    }

    public boolean isPagedRequest() {
        return isPagedRequest;
    }

    public int getPagedRequestCount() {
        return pagedRequestCount;
    }

    public void setPagedRequestCount(int pagedRequestCount) {
        this.pagedRequestCount = pagedRequestCount;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof OfflineData && id == ((OfflineData)o).getId();
    }

    @Override
    public void insertTo(ImonggoDBHelper2 dbHelper) {
        String typeStr = "UNKNOWN";
        switch (type) {
            case INVOICE:
                typeStr = "INVOICE";
                invoiceData.insertTo(dbHelper);
                break;
            case ORDER:
                typeStr = "ORDER";
                orderData.insertTo(dbHelper);
                break;
            case DOCUMENT:
                typeStr = "DOCUMENT";
                if(!isPagedRequest() || isNewPagedSend) // not a paged request
                    documentData.insertTo(dbHelper);
                break;
            case CUSTOMER:
                typeStr = "CUSTOMER";
                customerData.insertTo(dbHelper);
                break;
        }
        Log.e("OfflineData", "insert " + typeStr + " " + this.getReference_no());

        try {
            dbHelper.insert(OfflineData.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        switch (type) {
            case INVOICE:
                invoiceData.setOfflineData(this);
                invoiceData.updateTo(dbHelper);
                break;
            case ORDER:
                orderData.setOfflineData(this);
                orderData.updateTo(dbHelper);
                break;
            case DOCUMENT:
                if(isPagedRequest() && !isNewPagedSend) {
                    try {
                        for(Document child : documentData.getChildDocuments()) {
                            Log.e("OfflineData", "insertTo : CHILD : " + child.getReference());
                            child.setOfflineData(this);
                            child.insertTo(dbHelper);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    documentData.setOfflineData(this);
                    documentData.updateTo(dbHelper);
                }
                break;
            case CUSTOMER:
                customerData.setOfflineData(this);
                customerData.updateTo(dbHelper);
                break;
            default:
                break;
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper2 dbHelper) {
        try {
            dbHelper.delete(OfflineData.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String typeStr;
        switch (type) {
            case INVOICE:
                typeStr = "INVOICE";
                invoiceData.deleteTo(dbHelper);
                break;
            case ORDER:
                typeStr = "ORDER";
                orderData.deleteTo(dbHelper);
                break;
            case DOCUMENT:
                typeStr = "DOCUMENT";
                generateParentDocument();
                if(documentData == null)
                    break;
                if(isPagedRequest() && !isNewPagedSend) {
                    for(Document child : documentData_fc) {
                        child.deleteTo(dbHelper);
                    }
                } else {
                    documentData.deleteTo(dbHelper);
                }
                break;
            case CUSTOMER:
                typeStr = "CUSTOMER";
                customerData.deleteTo(dbHelper);
                break;
            default:
                typeStr = "UNKNOWN";
                break;
        }
        Log.e("OfflineData", "delete " + typeStr + " " + this.getReference_no());
    }

    @Override
    public void updateTo(ImonggoDBHelper2 dbHelper) {
        String typeStr;
        switch (type) {
            case INVOICE:
                invoiceData.setOfflineData(this);
                typeStr = "INVOICE";
                if( (getReturnIdList() != null && getReturnIdList().size() > 0 && getReturnIdList().get(0).length() > 0)
                        && (invoiceData.getId() != Integer.parseInt(getReturnIdList().get(0))) ) {
                    invoiceData.deleteTo(dbHelper);
                    invoiceData.setId(Integer.parseInt(getReturnIdList().get(0)));
                    invoiceData.insertTo(dbHelper);
                } else
                    invoiceData.updateTo(dbHelper);
                break;
            case ORDER:
                orderData.setOfflineData(this);
                typeStr = "ORDER";
                if( (getReturnIdList() != null && getReturnIdList().size() > 0 && getReturnIdList().get(0).length() > 0)
                        && (orderData.getId() != Integer.parseInt(getReturnIdList().get(0))) ) {
                    orderData.deleteTo(dbHelper);
                    orderData.setId(Integer.parseInt(getReturnIdList().get(0)));
                    orderData.insertTo(dbHelper);
                } else
                    orderData.updateTo(dbHelper);
                break;
            case DOCUMENT:
                generateParentDocument();
                documentData.setOfflineData(this);
                typeStr = "DOCUMENT";
                if(getReturnIdList() != null && getReturnIdList().size() > 0 && getReturnIdList().get(0).length() > 0) {
                    if(isPagedRequest() && !isNewPagedSend) {
                        try {
                            List<Document> children = getChildDocuments();

                            List<String> ids = getReturnIdList();
                            for(int i=0; i<children.size(); i++) {
                                if(i >= ids.size())
                                    break;
                                Document child = children.get(i);


                                if(!getReturnIdList().get(0).equals("@")) {
                                    child.deleteTo(dbHelper);
                                    child.setId(Integer.parseInt(ids.get(i)));
                                    child.setOfflineData(this);
                                    child.insertTo(dbHelper);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if(!getReturnIdList().get(0).equals("@")) {
                            documentData.deleteTo(dbHelper);
                            documentData.setId(Integer.parseInt(getReturnIdList().get(0)));
                            documentData.insertTo(dbHelper);
                        }
                    }
                } else {
                    if(isPagedRequest() && !isNewPagedSend) {
                        for(Document child : documentData_fc)
                            child.updateTo(dbHelper);
                    } else
                        documentData.updateTo(dbHelper);
                }
                break;
            case CUSTOMER:
                customerData.setOfflineData(this);
                typeStr = "CUSTOMER";
                if( (getReturnIdList() != null && getReturnIdList().size() > 0 && getReturnIdList().get(0).length() > 0)
                        && (customerData.getId() != Integer.parseInt(getReturnIdList().get(0))) ) {
                    customerData.deleteTo(dbHelper);
                    customerData.setId(Integer.parseInt(getReturnIdList().get(0)));
                    customerData.insertTo(dbHelper);
                } else
                    customerData.updateTo(dbHelper);
                break;
            default:
                typeStr = "UNKNOWN";
                break;
        }
        Log.e("OfflineData", "update " + typeStr + " " + this.getReference_no() + " returnId:" + getReturnId());

        try {
            dbHelper.update(OfflineData.class, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Object getObjectFromData() {
        if(type == INVOICE)
            return invoiceData;
        else if(type == ORDER)
            return orderData;
        else if(type == DOCUMENT) {
            return generateParentDocument();
        }
        else if(type == CUSTOMER)
            return customerData;
        else
            return null;
    }

    public List<String> getReturnIdList() {
        if(!returnId.contains(","))
            return Arrays.asList(returnId);
        return Arrays.asList(returnId.split(","));
    }

    public String getReturnIdListAt(int position) {
        if(position >= getReturnIdList().size())
            return null;
        return getReturnIdList().get(position);
    }

    public void insertReturnIdAt(int index, String returnId) {
        if(!isPagedRequest()) {
            Log.e("OfflineData", "insertReturnIdAt : not a paged transaction, invalid action");
            return;
        }
        ArrayList<String> retIds = new ArrayList<>(getReturnIdList());
        if(retIds.size() >= index+1 )
            retIds.set(index, returnId);
        else {
            while(retIds.size() < index)
                retIds.add("@");
            retIds.add(returnId);
        }

        this.returnId = StringUtils.join(retIds, ',');
    }

    public String getChildReferenceNo() {
        if(!isPagedRequest()) {
            Log.e("OfflineData", "getChildReferenceNo : not a paged transaction, use getReference_no() instead");
            return reference_no;
        }

        String childRefs = "";
        for(int i = 0; i < pagedRequestCount; i++) {
            if(i != 0)
                childRefs += ",";
            childRefs += reference_no + "-" + (i+1);
        }
        return childRefs;
    }

    public boolean isAllPageSynced() {
        return getReturnIdList().size() == getPagedRequestCount() && !getReturnId().contains("@");
    }

}
