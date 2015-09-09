package net.nueca.imonggosdk.objects.document;

/**
 * Created by gama on 9/8/15.
 */
public class RemarkBuilder {
    private boolean isManual = false;
    private String delivery_reference_no;
    private int page = 1;
    private int pageTotal = 1;

    public RemarkBuilder isManual(boolean isManual) {
        this.isManual = isManual;
        return this;
    }
    public RemarkBuilder delivery_reference_no(String delivery_reference_no) {
        this.delivery_reference_no = delivery_reference_no;
        return this;
    }
    public RemarkBuilder page(int current, int max) {
        this.page = current;
        this.pageTotal = max;
        return this;
    }

    public String build() {
        String remark = "manual=" + isManual;
        if(delivery_reference_no != null)
            remark += ",delivery_reference_no=" + delivery_reference_no;

        remark += ",page=" + page + "/" + pageTotal;
        return remark;
    }
}
