package net.nueca.imonggosdk.objects;

import android.util.Log;
import android.util.SparseArray;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.enums.DatabaseOperation;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.base.BaseTable2;
import net.nueca.imonggosdk.objects.base.BaseTransaction;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.DateTimeTools;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

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

    @DatabaseField
    private String date;

	@DatabaseField
	private String data;
	
	@DatabaseField
	private int type = 1; // 1 - Sales | 2 - Inventory | 3 - Order Taking
	
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

    public OfflineData() {}

    public OfflineData(Invoice invoice, OfflineDataType offlineDataType) {
        String []timestamp = DateTimeTools.getCurrentDateTimeInvoice();
        String timeId = timestamp[0]+" "+timestamp[1];
        this.date = timeId;
        this.dateCreated = Calendar.getInstance().getTime();

        this.offlineDataTransactionType = offlineDataType.getNumericValue();
        this.type = INVOICE;
        this.data = invoice.toJSONString();
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
        this.data = order.toJSONString();
        this.reference_no = order.getReference();

        this.isPagedRequest = order.shouldPageRequest();
        this.pagedRequestCount = SwableTools.computePagedRequestCount(order.getOrderLines().size(),
                Order.MAX_ORDERLINES_PER_PAGE);
    }

    public OfflineData(Document document, OfflineDataType offlineDataType) {
        String []timestamp = DateTimeTools.getCurrentDateTimeInvoice();
        String timeId = timestamp[0]+" "+timestamp[1];
        this.date = timeId;
        this.dateCreated = Calendar.getInstance().getTime();

        this.offlineDataTransactionType = offlineDataType.getNumericValue();
        this.type = DOCUMENT;
        this.data = document.toJSONString();
        this.reference_no = document.getReference();

        this.isPagedRequest = document.shouldPageRequest();
        this.pagedRequestCount = SwableTools.computePagedRequestCount(document.getDocument_lines().size(),
                Document.MAX_DOCUMENTLINES_PER_PAGE);
    }

    public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
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
    public void insertTo(ImonggoDBHelper dbHelper) {
        String typeStr;
        switch (type) {
            case INVOICE: typeStr = "INVOICE"; break;
            case ORDER: typeStr = "ORDER"; break;
            case DOCUMENT: typeStr = "DOCUMENT"; break;
            default: typeStr = "UNKNOWN"; break;
        }
        Log.e("OfflineData", "insert " + typeStr + " " + this.getReference_no());

        try {
            dbHelper.dbOperations(this, Table.OFFLINEDATA, DatabaseOperation.INSERT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTo(ImonggoDBHelper dbHelper) {
        String typeStr;
        switch (type) {
            case INVOICE: typeStr = "INVOICE"; break;
            case ORDER: typeStr = "ORDER"; break;
            case DOCUMENT: typeStr = "DOCUMENT"; break;
            default: typeStr = "UNKNOWN"; break;
        }
        Log.e("OfflineData", "delete " + typeStr + " " + this.getReference_no());
        try {
            dbHelper.dbOperations(this, Table.OFFLINEDATA, DatabaseOperation.DELETE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTo(ImonggoDBHelper dbHelper) {
        String typeStr;
        switch (type) {
            case INVOICE: typeStr = "INVOICE"; break;
            case ORDER: typeStr = "ORDER"; break;
            case DOCUMENT: typeStr = "DOCUMENT"; break;
            default: typeStr = "UNKNOWN"; break;
        }
        Log.e("OfflineData", "update " + typeStr + " " + this.getReference_no());
        try {
            dbHelper.dbOperations(this, Table.OFFLINEDATA, DatabaseOperation.UPDATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public BaseTransaction generateObjectFromData() throws JSONException {
        if(type == INVOICE)
            return Invoice.fromJSONString(data);
        else if(type == ORDER)
            return Order.fromJSONString(data);
        else if(type == DOCUMENT)
            return Document.fromJSONString(data);
        else
            return null;
    }

    public List<String> parseReturnID() {
        //if(returnId.length() <= 0)
        //    return new ArrayList<>();
        return Arrays.asList(returnId.split(","));
    }

    public void insertReturnIdAt(int index, String returnId) {
        ArrayList<String> retIds = new ArrayList<>(parseReturnID());
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
        if(!isPagedRequest())
            return reference_no;

        String childRefs = "";
        for(int i = 0; i < pagedRequestCount; i++) {
            if(i != 0)
                childRefs += ",";
            childRefs += reference_no + "-" + (i+1);
        }
        return childRefs;
    }
}
