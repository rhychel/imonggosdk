package net.nueca.imonggosdk.tools;

/**
 * Created by rhymartmanchus on 11/01/2016.
 */
public class FieldValidatorMessage {

    private boolean isPassed = true;
    private String message = "";
    private int appendCount = 0;

    public FieldValidatorMessage() {
    }

    public FieldValidatorMessage(boolean isPassed, String message) {
        this.isPassed = isPassed;
        this.message = message;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public void setPassed(boolean passed) {
        isPassed = passed;
    }

    public void appendMessage(String message, boolean isLast) {
        if(message.isEmpty())
            return;
        if(appendCount > 0) {
            if(isLast)
                this.message += " and "+message;
            else
                this.message += ", "+message;
        }
        else if(isLast)
            this.message += message+".";
        else
            this.message += message;
        appendCount++;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
