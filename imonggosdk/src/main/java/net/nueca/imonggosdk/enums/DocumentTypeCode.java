package net.nueca.imonggosdk.enums;

/**
 * Created by gama on 8/5/15.
 */
public enum DocumentTypeCode {
    PHYSICAL_COUNT("physical_count"),
    RECEIVE_SUPPLIER("receive_supplier"),
    RELEASE_SUPPLIER("release_supplier"),
    RECEIVE_ADJUSTMENT("receive_adjustment"),
    RELEASE_ADJUSTMENT("release_adjustment"),
    RECEIVE_BRANCH("receive_branch"),
    RELEASE_BRANCH("release_branch");

    private String name;
    DocumentTypeCode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
    
    public static DocumentTypeCode identify(String documentTypeCodeStr) {
        for(DocumentTypeCode documentTypeCode : DocumentTypeCode.values()) {
            if(documentTypeCode.name.equals(documentTypeCodeStr))
                return documentTypeCode;
        }
        return PHYSICAL_COUNT;
    }
}
