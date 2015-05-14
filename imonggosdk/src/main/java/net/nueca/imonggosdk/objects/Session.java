package net.nueca.imonggosdk.objects;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by rhymart on 5/12/15.
 * imonggosdk (c)2015
 */
@DatabaseTable
public class Session {

    @DatabaseField(generatedId=true)
    private int id;
    @DatabaseField
    private String email = "";
    @DatabaseField
    private String password = "";
    @DatabaseField
    private String api_token = "";
    @DatabaseField
    private String account_id = "";
    @DatabaseField
    private String account_url = "";
    @DatabaseField
    private String api_authentication = "";
    @DatabaseField
    private int order_taken = 0;
    @DatabaseField
    private String today = "2000-01-01";
    @DatabaseField
    private int device_id = 0;
    @DatabaseField
    private boolean hasLoggedIn = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApiToken() {
        return api_token;
    }

    public void setApiToken(String api_token) {
        this.api_token = api_token;
    }

    public String getAccountId() {
        return account_id;
    }

    public void setAccountId(String account_id) {
        this.account_id = account_id;
    }

    public String getAccountUrl() {
        return account_url;
    }

    public void setAccountUrl(String account_url) {
        this.account_url = account_url;
    }

    public String getAcctUrlWithoutProtocol() {
        return account_url.replace("http://", "").replace("https://", "");
    }

    public String getApiAuthentication() {
        return api_authentication;
    }

    public void setApiAuthentication(String api_authentication) {
        this.api_authentication = api_authentication;
    }

    public int getOrder_taken() {
        return order_taken;
    }

    public void setOrder_taken(int order_taken) {
        this.order_taken = order_taken;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    public int getDevice_id() {
        return device_id;
    }

    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }

    public boolean isHasLoggedIn() {
        return hasLoggedIn;
    }

    public void setHasLoggedIn(boolean hasLoggedIn) {
        this.hasLoggedIn = hasLoggedIn;
    }
}
