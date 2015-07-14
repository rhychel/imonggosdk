package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.tools.DateTimeTools;

@DatabaseTable
public class OfflineInventory {
	
	@DatabaseField(id=true)
	private String id;
	
	@DatabaseField
	private String inventoryJSON = "", unit = "";
	
	@DatabaseField
	private int branch_id = 0;
	
	public OfflineInventory() { }
	
	public OfflineInventory(int branch_id, String inventory) {
		String []timestamp = DateTimeTools.getCurrentDateTimeInvoice();
		String timeId = timestamp[0]+" "+timestamp[1];
		setId(timeId);
		setBranch_id(branch_id);
		setInventory(inventory);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInventory() {
		return inventoryJSON;
	}

	public void setInventory(String inventory) {
		this.inventoryJSON = inventory;
	}

	public int getBranch_id() {
		return branch_id;
	}

	public void setBranch_id(int branch_id) {
		this.branch_id = branch_id;
	}
	
}
