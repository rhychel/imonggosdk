package net.nueca.imonggosdk.enums;

public enum Server {
	IMONGGO("imonggo", "http://www.imonggo.com"),
	IMONGGO_NET("imonggo_net", "http://www.imonggo.net"),
	IRETAILCLOUD_NET("iretailcloud_net", "http://www.iretailcloud.net"),
	IRETAILCLOUD_COM("iretailcloud_com", "http://www.iretailcloud.com"),
	PLDTRETAILCLOUD("pldtretailcloud_com", "http://www.pldtretailcloud.com"),
	PETRONDIS_COM("petrondis_com", "http://www.petrondis.com"),
	PETRONDIS_NET("petrondis_net", "http://www.petrondis.net"),
	REBISCO_DEV("rebisco", "http://www.rbcsadr.net"),
	REBISCO_LIVE("rebisco_live", "http://www.webrdas.com");

	private String label;
	private String serverUrl;

	Server(String label) {
		this.label = label;
	}

	Server(String label, String serverUrl) {
		this.label = label;
		this.serverUrl = serverUrl;
	}

	public static Server getServer(String label) {
		for(Server server : values())
			if(server.label.equals(label))
				return server;
		return IRETAILCLOUD_NET; // TEMP, should be irc.net
	}

	@Override
	public String toString() {
		return serverUrl;
	}
}
