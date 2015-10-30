package nueca.net.salesdashboard.interfaces;


public interface OnReloadBranches {
    void finishedReloading();
    void showBasicDialogMessageReloadBranches(String message, String title);
    void showProgressHudReloadBranches(String message);
}
