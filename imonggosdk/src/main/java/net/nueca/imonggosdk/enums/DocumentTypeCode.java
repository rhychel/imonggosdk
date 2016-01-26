package net.nueca.imonggosdk.enums;

/**
 * Created by gama on 8/5/15.
 */
public enum DocumentTypeCode {
    PHYSICAL_COUNT("physical_count", ConcessioModule.PHYSICAL_COUNT),
    RECEIVE_SUPPLIER("receive_supplier", ConcessioModule.RECEIVE_SUPPLIER),
    RELEASE_SUPPLIER("release_supplier", ConcessioModule.RELEASE_SUPPLIER),
    RECEIVE_ADJUSTMENT("receive_adjustment", ConcessioModule.RECEIVE_ADJUSTMENT),
    RELEASE_ADJUSTMENT("release_adjustment", ConcessioModule.RELEASE_ADJUSTMENT),
    RECEIVE_BRANCH("receive_branch", ConcessioModule.RECEIVE_BRANCH),
    RELEASE_BRANCH("release_branch", ConcessioModule.RELEASE_BRANCH);

    private String name;
    private ConcessioModule concessioModule;
    DocumentTypeCode(String name) {
        this.name = name;
    }

    DocumentTypeCode(String name, ConcessioModule concessioModule) {
        this.name = name;
        this.concessioModule = concessioModule;
    }

    public ConcessioModule getConcessioModule() {
        return concessioModule;
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

    public static DocumentTypeCode identify(ConcessioModule concessioModule) {
        if(concessioModule == ConcessioModule.RECEIVE_BRANCH_PULLOUT)
            concessioModule = ConcessioModule.RECEIVE_BRANCH;
        for(DocumentTypeCode documentTypeCode : values()) {
            if(documentTypeCode.name.equals(concessioModule.toString()))
                return documentTypeCode;
        }
        return PHYSICAL_COUNT;
    }

}
