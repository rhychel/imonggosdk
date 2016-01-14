package net.nueca.concessioengine.objects;

/**
 * Created by rhymartmanchus on 12/01/2016.
 */
public class AppSettings {
    public enum AppSettingType {
        APPLICATION("APPLICATION"),
        USER("USER"),
        LISTING("LISTING");

        String header;
        AppSettingType(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }

        @Override
        public String toString() {
            return header;
        }
    }

    private boolean isHeader = false;
    private AppSettingType appSettingType;
    private String label;
    private int sectionFirstPosition = 0;

    public AppSettings(boolean isHeader, AppSettingType appSettingType, String label) {
        this.isHeader = isHeader;
        this.appSettingType = appSettingType;
        this.label = label;
    }

    public AppSettings(AppSettingType appSettingType, String label) {
        this.appSettingType = appSettingType;
        this.label = label;
    }

    public AppSettings(boolean isHeader, AppSettingType appSettingType, String label, int sectionFirstPosition) {
        this.isHeader = isHeader;
        this.appSettingType = appSettingType;
        this.label = label;
        this.sectionFirstPosition = sectionFirstPosition;
    }

    public AppSettingType getAppSettingType() {
        return appSettingType;
    }

    public void setAppSettingType(AppSettingType appSettingType) {
        this.appSettingType = appSettingType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public int getSectionFirstPosition() {
        return sectionFirstPosition;
    }

    public void setSectionFirstPosition(int sectionFirstPosition) {
        this.sectionFirstPosition = sectionFirstPosition;
    }
}
