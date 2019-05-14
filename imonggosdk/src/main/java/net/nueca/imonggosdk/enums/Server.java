package net.nueca.imonggosdk.enums;

public enum Server {
	IMONGGO("imonggo"),
	IMONGGO_NET("imonggo_net"),
	IRETAILCLOUD_NET("iretailcloud_net"),
	IRETAILCLOUD_COM("iretailcloud_com"),
	PLDTRETAILCLOUD("pldtretailcloud_com"),
	PETRONDIS_COM("petrondis_com"),
	PETRONDIS_NET("petrondis_net"),
	REBISCO("rebisco");

	private String label;

	Server(String label) {
		this.label = label;
	}

	public static Server getServer(String label) {
		for(Server server : values())
			if(server.label.equals(label))
				return server;
		return REBISCO; // TEMP, should be irc.net
	}
}
