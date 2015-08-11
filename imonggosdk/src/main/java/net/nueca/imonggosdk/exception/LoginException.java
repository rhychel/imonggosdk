package net.nueca.imonggosdk.exception;

/**
 * Created by Jn on 6/8/2015.
 */
public class LoginException extends Exception {

    private static final long serialVersionUID = 6666L;

    public LoginException(){}

    public LoginException(String message){
        super(message);
    }

    public LoginException(Throwable cause){
        super(cause);
    }

    public LoginException(String message, Throwable cause){
        super(message, cause);
    }
}
