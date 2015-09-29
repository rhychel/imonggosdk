package net.nueca.imonggosdk.objects.document;

/**
 * Created by gama on 9/8/15.
 */
public class RemarkBuilder {
    private boolean isManual = false;
    private String delivery_reference_no;
    private int page = 1;
    private int pageTotal = 1;

    public RemarkBuilder parse(String remark) {
        String elements[] = remark.split(",");
        for(String element : elements) {
            String keyvalue[] = element.split("=");
            if(keyvalue[0].equals("manual"))
                isManual = keyvalue[1].equals("true");
            else if(keyvalue[0].equals("delivery_reference_no"))
                delivery_reference_no = keyvalue[1];
            else if(keyvalue[0].equals("page")) {
                String paging[] = keyvalue[1].split("/");
                page = Integer.parseInt(paging[0]);
                pageTotal = Integer.parseInt(paging[1]);
            }
        }
        return this;
    }

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
