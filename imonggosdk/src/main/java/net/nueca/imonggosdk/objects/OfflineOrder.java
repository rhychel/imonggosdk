package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.tools.DateTimeTools;

@DatabaseTable
public class OfflineOrder {
	
	@DatabaseField(id=true)
	private String id;
	
	@DatabaseField
	private String orderData;
	
	public OfflineOrder() { }
	
	public OfflineOrder(String orderData) {
		String []timestamp = DateTimeTools.getCurrentDateTimeInvoice();
		String timeId = timestamp[0]+" "+timestamp[1];
		setId(timeId);
		setOrderData(orderData);
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getOrderData() {
		return orderData;
	}

	public void setOrderData(String orderData) {
		this.orderData = orderData;
	}
	
}
